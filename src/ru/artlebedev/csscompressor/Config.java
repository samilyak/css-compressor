/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.19
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Config {

  private String rootPath;
  
  private String charset;

  private String outputPath;

  private ArrayList<Module> modules;

  
  private Config(
      String rootPath,
      String charset,
      String outputPath,
      ArrayList<Module> modules){
    
    this.rootPath = rootPath;
    this.charset = charset;
    this.outputPath = outputPath;
    this.modules = modules;
  }

  public static Builder createBuilder(String configFilePath){
    return new Builder(configFilePath);
  }

  public String getRootPath(){
    return rootPath;
  }

  public String getCharset() {
    return charset;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public ArrayList<Module> getModules() {
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

    private String outputPath;

    private ArrayList<Module> modules;


    private Builder(String configFilePath) {
      this.configFileCatalog = new File(configFilePath).getParent();
    }


    public Config build() {
      buildFullPathToRoot();
      buildCharset();
      buildOutputPath();
      buildModules();

      return new Config(fullPathToRoot, charset, outputPath, modules);
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

    private void buildOutputPath() {
      if (outputPathRaw == null) {
        throw new RuntimeException(
            "Option '" + ConfigOption.OUTPUT_PATH.getName() + "' " +
            "is required.");
      }

      outputPath = calculateFullPath(outputPathRaw);
    }
    

    private void buildModules() {
      if (modulesInfoRaw == null) {
        throw new RuntimeException(
            "Option '" + ConfigOption.MODULES.getName() + "' " +
            "is required.");
      }


      ArrayList<Module> modules = new ArrayList<Module>();
      
      for (
          Map.Entry<String, JsonElement> moduleInfo :
          modulesInfoRaw.entrySet()) {

        String name = moduleInfo.getKey();
        JsonElement inputs = moduleInfo.getValue();
        ArrayList<String> inputsAsList = new ArrayList<String>();

        String str = Utils.toJsonStringOrNull(inputs);
        if (str != null) {
          inputsAsList.add(calculateFullPath(str));
        } else {
          if (!inputs.isJsonArray()) {
            throw new IllegalArgumentException(
                "Module inputs must be either a single string, " +
                "or an array of strings, but was: " + inputs);
          }

          for (JsonElement element : inputs.getAsJsonArray()) {
            String elementAsString = Utils.toJsonStringOrNull(element);
            if (elementAsString == null) {
              throw new IllegalArgumentException(
                  "Module inputs contained an element " +
                  "that was not a string literal: " + element);
            }

            inputsAsList.add(calculateFullPath(elementAsString));
          }
        }

        modules.add(
            new Module(name, inputsAsList, moduleNameToOutputPath(name)));
      }

      this.modules = modules;
    }

    
    private String moduleNameToOutputPath(String moduleName) {
      return String.format(this.outputPath, moduleName);
    }
    
    private String calculateFullPath(String path) {
      return new File(fullPathToRoot, path).getPath();
    }

  }


  public final static class Module {

    private final String name;
    private final ArrayList<String> inputs;
    private final String outputPath;

    private Module(String name, ArrayList<String> inputs, String outputPath) {
      this.name = name;
      this.inputs = inputs;
      this.outputPath = outputPath;
    }

    public String getName() {
      return name;
    }

    public ArrayList<String> getInputs() {
      return inputs;
    }

    public String getOutputPath() {
      return outputPath;
    }
  }

}
