/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.19
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


class ConfigBuilder {

  private final String REPLACE_SPLITTER = "::";


  private final CommandLine cmdLine;

  private String configFilePath;

  private String rootPath;

  private String charset;

  private String outputPath;

  private JsonObject modulesInfo;

  private String outputWrapper;

  private String preprocessCommand;


  ConfigBuilder(CommandLine cmdLine) {
    this.cmdLine = cmdLine;
    configFilePath = cmdLine.getArgs()[0];
  }


  public Config build() throws IOException {
    parseConfigFile();

    return new Config(
        getRootFullPath(),
        getCharset(),
        outputWrapper,
        getModules(),
        getReplaces(),
        preprocessCommand,
        isQuiet());
  }


  private void parseConfigFile() throws IOException {
    JsonElement root = new JsonParser().parse(Utils.readFile(configFilePath));

    if (!root.isJsonObject()) {
      throw new RuntimeException(String.format(
          "Config file %s contains not a JSON object as its root",
          configFilePath));
    }

    JsonObject jsonConfig = root.getAsJsonObject();

    // Keep track of the keys in the 'options' object so that we can warn
    // about unused options in the config file.
    Set<String> options = new HashSet<String>();
    for (Map.Entry<String, JsonElement> entry : jsonConfig.entrySet()) {
      options.add(entry.getKey());
    }

    for (ConfigOption configOption : ConfigOption.values()) {
      String optionName = configOption.getName();

      if (jsonConfig.has(optionName)) {
        configOption.update(jsonConfig.get(optionName), this);
        options.remove(optionName);
      }
    }

    for (String unusedOption : options) {
      System.err.printf(
          "WARNING: Unused option \"%s\" in %s\n",
          unusedOption,
          configFilePath);
    }
  }


  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  public void setModulesInfo(JsonObject modulesInfo) {
    this.modulesInfo = modulesInfo;
  }

  public void setOutputWrapper(String outputWrapper) {
    this.outputWrapper = outputWrapper;
  }

  public void setPreprocessCommand(String command) {
    this.preprocessCommand = command;
  }






  private String getRootFullPath() {
    String configCatalog = new File(configFilePath).getParent();

    String rootPath;
    if (this.rootPath != null) {
      rootPath = this.rootPath;
    } else {
      rootPath = ConfigOption.ROOT.getDefaultValue();
    }

    return new File(configCatalog, rootPath).getPath();
  }

  private String getCharset() {
    if (charset != null) {
      return charset;
    } else {
      return ConfigOption.CHARSET.getDefaultValue();
    }
  }

  private List<Config.Module> getModules() {
    if (modulesInfo == null) {
      throw new RuntimeException(
          "Option '" + ConfigOption.MODULES.getName() + "' " +
              "is required.");
    }


    List<Config.Module> modules = new ArrayList<Config.Module>();

    for (Map.Entry<String, JsonElement> moduleData :
        modulesInfo.entrySet()) {

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
          new Config.Module(name, inputs, getModuleOutputPath(name, output)));
    }

    return modules;
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
    if (moduleOutput == null && outputPath == null) {
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
      moduleOutput = outputPath;
    }

    return calculateFullPath(
        // replace %s with a module name
        String.format(moduleOutput, moduleName));
  }


  private List<Config.Replace> getReplaces() {
    String[] replaces = cmdLine.getOptionValues("replace");
    List<Config.Replace> processedReplaces = new ArrayList<Config.Replace>();

    if (replaces != null) {
      for (String replaceStr : replaces) {
        if (replaceStr.contains(REPLACE_SPLITTER)) {
          String[] split = replaceStr.split(REPLACE_SPLITTER, 2);
          processedReplaces.add(
              new Config.Replace(split[0], split[1]));

          if (!isQuiet()) {
            System.out.println("Replace: " + split[0] + " => " +  split[1]);
          }

        } else {
          throw new RuntimeException(
              String.format(
                  "Replace '%s' did not contain splitter '%s'",
                  replaceStr, REPLACE_SPLITTER));
        }
      }
    }

    return processedReplaces;
  }


  private boolean isQuiet() {
    return cmdLine.hasOption("quiet");
  }


  private String calculateFullPath(String path) {
    return new File(getRootFullPath(), path).getPath();
  }

}
