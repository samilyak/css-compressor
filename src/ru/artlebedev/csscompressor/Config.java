/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.19
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Config {

  public static final String OUTPUT_WRAPPER_MARKER = "%output%";


  private String rootPath;
  
  private String charset;

  private String outputWrapper;

  private List<Module> modules;

  
  private Config(
      String rootPath,
      String charset,
      String outputWrapper,
      List<Module> modules){
    
    this.rootPath = rootPath;
    this.charset = charset;
    this.outputWrapper = outputWrapper;
    this.modules = modules;
  }

  public static Builder createBuilder(String configFilePath){
    return new Builder(configFilePath);
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


  public final static class Builder {

    private String rootPathRaw;

    private String charsetRaw;

    private String outputPathRaw;

    private JsonObject modulesInfoRaw;


    private String configFileCatalog;

    private String fullPathToRoot;
    
    private String charset;

    private String outputWrapper;

    private List<Module> modules;


    private Builder(String configFilePath) {
      configFileCatalog = new File(configFilePath).getParent();
    }


    public Config build() {
      buildFullPathToRoot();
      buildCharset();
      buildModules();

      return new Config(
          fullPathToRoot,
          charset,
          outputWrapper,
          modules);
    }


    public void setRootPath(String rootPath) {
      rootPathRaw = rootPath;
    }

    public void setCharset(String charset) {
      charsetRaw = charset;
    }
    
    public void setOutputPath(String outputPath) {
      outputPathRaw = outputPath;
    }

    public void setOutputWrapper(String outputWrapper) {
      this.outputWrapper = outputWrapper;
    }

    public void setModulesInfo(JsonObject modulesInfo) {
      modulesInfoRaw = modulesInfo;
    }


    
    private void buildFullPathToRoot() {
      String path = rootPathRaw != null ?
          rootPathRaw :
          ConfigOption.ROOT.getDefaultValue();

      fullPathToRoot = new File(configFileCatalog, path).getPath();
    }
    
    private void buildCharset() {
      charset = charsetRaw != null ?
          charsetRaw :
          ConfigOption.CHARSET.getDefaultValue();
    }

    private void buildModules() {
      if (modulesInfoRaw == null) {
        throw new RuntimeException(
            "Option '" + ConfigOption.MODULES.getName() + "' " +
            "is required.");
      }


      List<Module> modules = new ArrayList<Module>();
      
      for (Map.Entry<String, JsonElement> moduleData :
          modulesInfoRaw.entrySet()) {

        String name = moduleData.getKey();
        JsonElement info = moduleData.getValue();


        List<String> inputs;
        String output;

        if (Utils.isJsonString(info) || info.isJsonArray()) {
          inputs = extractModuleInputs(info);
          output = null;

        } else if (info.isJsonObject()) {
          JsonObject infoAsObject = info.getAsJsonObject();

          JsonElement inputsData = infoAsObject.get("inputs");
          if (inputsData == null) {
            throw new IllegalArgumentException(
                "Module '" + name + "' must specify inputs " +
                    "using \"inputs\" key. Found: " + inputsData);
          }
          inputs = extractModuleInputs(inputsData);

          JsonElement outputRaw = infoAsObject.get("output");
          if (outputRaw != null && !Utils.isJsonString(outputRaw)) {
            throw new IllegalArgumentException(
                "Output of module '" + name + "' must be a string. Found: " +
                    outputRaw);
          }

          output = Utils.jsonElementToStringOrNull(outputRaw);

        } else {
          throw new IllegalArgumentException(
              "Module info must be either a single string, " +
                  "an array of strings, or an object. Found: " + info);
        }

        modules.add(
            new Module(name, inputs, getModuleOutputPath(name, output)));
      }

      this.modules = modules;
    }


    private List<String> extractModuleInputs(JsonElement element) {
      List<String> inputs = new ArrayList<String>();

      if (Utils.isJsonString(element)) {
        inputs.add(
            calculateFullPath(element.getAsString()));

      } else if (element.isJsonArray()) {
        for (JsonElement input : element.getAsJsonArray()) {
          String str = Utils.jsonElementToStringOrNull(input);

          if (str == null) {
            throw new IllegalArgumentException(
                "Module inputs contained an element " +
                    "that was not a string literal: " + input);
          }

          inputs.add(calculateFullPath(str));
        }
      } else {
        throw new IllegalArgumentException(
            "Module inputs must be either a single string, " +
                "or an array of strings. Found: " + element);
      }

      return inputs;
    }


    private String getModuleOutputPath(String moduleName, String moduleOutput) {
      if (moduleOutput == null && outputPathRaw == null) {
        throw new IllegalArgumentException(
            String.format(
                "Module '%s' didn't have output path. " +
                "You must specify output path either by global key '%s' or " +
                "by module own object key 'output' containing string value.",
                moduleName,
                ConfigOption.OUTPUT_PATH.getName())
        );
      }

      if (moduleOutput == null) {
        moduleOutput = outputPathRaw;
      }

      return calculateFullPath(
          String.format(moduleOutput, moduleName));
    }


    private String calculateFullPath(String path) {
      return new File(fullPathToRoot, path).getPath();
    }

  }


  public final static class Module {

    final String name;
    final List<String> inputs;
    final String outputPath;

    private Module(String name, List<String> inputs, String outputPath) {
      this.name = name;
      this.inputs = inputs;
      this.outputPath = outputPath;
    }

  }

}
