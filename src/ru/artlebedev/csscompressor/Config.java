/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.19
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import java.util.List;


class Config {

  public static final String OUTPUT_WRAPPER_MARKER = "%output%";


  private String rootPath;
  
  private String charset;

  private String outputWrapper;

  private List<Module> modules;

  private List<Replace> replaces;

  
  Config(
      String rootPath,
      String charset,
      String outputWrapper,
      List<Module> modules,
      List<Replace> replaces){
    
    this.rootPath = rootPath;
    this.charset = charset;
    this.outputWrapper = outputWrapper;
    this.modules = modules;
    this.replaces = replaces;
  }

  public String getRootPath() {
    return rootPath;
  }

  public String getCharset() {
    return charset;
  }

  public String getOutputWrapper() {
    return outputWrapper;
  }

  public List<Module> getModules() {
    return modules;
  }

  public List<Replace> getReplaces() {
    return replaces;
  }


  final static class Module {

    final String name;
    final List<String> inputs;
    final String outputPath;

    Module(String name, List<String> inputs, String outputPath) {
      this.name = name;
      this.inputs = inputs;
      this.outputPath = outputPath;
    }

  }


  final static class Replace {

    final String search;
    final String replacement;

    Replace(String search, String replacement) {
      this.search = search;
      this.replacement = replacement;
    }

  }

}
