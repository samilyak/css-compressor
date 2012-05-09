/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.11
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CssCompressor {

  /*
    CSS imports are allowed in 2 syntaxes:
      1. @import url("style.css")
      2. @import "style.css"
    So this regex is expecting a valid input CSS.

    TODO(samilyak): Consider using more bulletproof regex -
    it tracks a paring of quotes and parenthesis
    @import\s+(?:url\(\s*(?=[^;$]+?\)))?(["']?)([\w\\\/\-\_\.]+?\.css)\1(?!["'])[^;$]*?(;|$)

    TODO(samilyak): Prevent from matching @import inside CSS comments
  */
  private static final Pattern cssImportPattern = Pattern.compile(
      "@import\\s+" +

      // optional 'url(' part (non capturing subpattern) with optional quote
      "(?:url\\(\\s*)?" + "[\"']?" +

      // file path ending with '.css' in capturing subpattern 1
      // word characters, slashes, dash, underscore, dot,
      // colon and question mark (possible for absolute urls) are allowed
      "([\\w\\\\/\\-_.:?]+?\\.css)" +

      // the rest of the line until semicolon or line break
      "[^;$]*?(;|$)",
      Pattern.MULTILINE);

  private final Config config;



  public CssCompressor(Config config) {
    this.config = config;
  }


  public void compress() throws IOException {
    for (Config.Module module : config.getModules()) {
      prepareModuleOutputCatalog(module);

      String css = concatCssFiles(module.inputs);

      com.yahoo.platform.yui.compressor.CssCompressor compressor =
          new com.yahoo.platform.yui.compressor.CssCompressor(
              new StringReader(css));

      StringWriter stringWriter = new StringWriter();
      compressor.compress(stringWriter, -1);
      css = wrapCssWithOutputWrapper(stringWriter.toString());
      Utils.writeToFile(module.outputPath, css, config.getCharset());
    }
  }

  private void prepareModuleOutputCatalog(Config.Module module) {
    File outputCatalog = new File(module.outputPath).getParentFile();
    if (outputCatalog != null) {
      // null means outputPath doesn't contain catalog part, just filename -
      // it's OK, we'll write to current catalog

      outputCatalog.mkdirs();
      if (!outputCatalog.exists()) {
        throw new RuntimeException(
            "Unable to write to catalog " + outputCatalog.getPath());
      }
    }
  }

  private String wrapCssWithOutputWrapper(String css) {
    if (config.getOutputWrapper() != null) {
      if (config.getOutputWrapper().contains(Config.OUTPUT_WRAPPER_MARKER)) {
        css = config.getOutputWrapper().replace(
            Config.OUTPUT_WRAPPER_MARKER, css);
      } else {
        throw new RuntimeException(
            String.format(
                "Option '%s' did not contain placeholder %s",
                ConfigOption.OUTPUT_WRAPPER.getName(),
                Config.OUTPUT_WRAPPER_MARKER));
      }
    }

    return css;
  }


  private String concatCssFiles(List<String> paths)
      throws IOException {

    StringBuilder stringResult = new StringBuilder();
    List<String> processedFiles = new ArrayList<String>(0);

    for (String path : paths) {
      CssProcessingResult pathProcessingResult =
          processCssFile(path, processedFiles);

      stringResult.append(pathProcessingResult.content);
      processedFiles = pathProcessingResult.processedFiles;
    }
    
    return stringResult.toString();
  }


  private CssProcessingResult processCssFile(
      String path, List<String> processedFiles) throws IOException {
    /*
      We need to prevent from processing same files more than once,
      to minify result build file and more importantly to avoid cyclic imports.
      That's why we need 2nd argument
      containing paths of already processed files.
    */

    File fileAtPath = new File(path);
    String fileCanonicalPath = fileAtPath.getCanonicalPath();
    String fileCatalog = fileAtPath.getParent();

    if (processedFiles.contains(fileCanonicalPath)) {
      return new CssProcessingResult("", processedFiles);
    }

    processedFiles.add(fileCanonicalPath);

    String inputContent = Utils.readFile(path, config.getCharset());
    Matcher matcher = cssImportPattern.matcher(inputContent);

    StringBuffer stringResult = new StringBuffer();
    while(matcher.find()){
      String importPath = matcher.group(1);
      
      String importFileContent = "";
      if (!isCssImportAbsolute(importPath)) {
        File importFile = new File(fileCatalog, importPath);
        CssProcessingResult importProcessingResult =
            processCssFile(importFile.getPath(), processedFiles);

        importFileContent = importProcessingResult.content;
      }

      matcher.appendReplacement(stringResult, importFileContent);
    }
    matcher.appendTail(stringResult);


    return new CssProcessingResult(stringResult.toString(), processedFiles);
  }


  private static boolean isCssImportAbsolute(String path) {
    boolean isAbsoluteUri;
    try{
      URI uri = new URI(path);
      isAbsoluteUri = uri.isAbsolute();
    } catch (URISyntaxException e) {
      isAbsoluteUri = false;
    }
    
    return isAbsoluteUri || new File(path).isAbsolute();
  }



  private final static class CssProcessingResult {

    final String content;
    final List<String> processedFiles;

    public CssProcessingResult(
        String content, List<String> processedFiles){

      this.content = content;
      this.processedFiles = processedFiles;
    }

  }

}
