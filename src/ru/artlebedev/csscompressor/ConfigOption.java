/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.19
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public enum ConfigOption {

  ROOT(
      "root", "string",
      new Updater(){
        @Override
        public void update(String rootPath, Config.Builder builder) {
          builder.setRootPath(rootPath);
        }
      },
      "."), // relative to location of config json file

  OUTPUT_PATH(
      "output-path", "string",
      new Updater(){
        @Override
        public void update(String outputPath, Config.Builder builder){
          builder.setOutputPath(outputPath);
        }
      }),

  OUTPUT_WRAPPER(
      "output-wrapper", "string or array",
      new Updater(){
        @Override
        public void update(String outputWrapper, Config.Builder builder){
          builder.setOutputWrapper(outputWrapper);
        }

        /**
         * output-wrapper can also be an array of strings that should be
         * concatenated together.
         */
        @Override
        public void update(
            JsonArray outputWrapperParts, Config.Builder builder) {
          
          StringBuilder outputWrapper = new StringBuilder();
          for (JsonElement item : outputWrapperParts) {
            String part = Utils.jsonElementToStringOrNull(item);
            if (part == null) {
              throw new RuntimeException(
                  String.format(
                      "Some parts of array '%s' are not string: %s",
                      ConfigOption.OUTPUT_WRAPPER.getName(), item));
            }

            outputWrapper.append(part);
          }
          update(outputWrapper.toString(), builder);
        }
      }),
  
  MODULES(
      "modules", "object",
      new Updater(){
        @Override
        public void update(JsonObject modules, Config.Builder builder){
          builder.setModulesInfo(modules);
        }
      }),

  CHARSET(
      "charset", "string",
      new Updater(){
        @Override
        public void update(String charset, Config.Builder builder){
          builder.setCharset(charset);
        }
      },
      "UTF-8")
  ;



  private final String name;

  private final String allowedTypes;

  private final Updater updater;
  
  private final String defaultValue;


  ConfigOption(String name, String allowedTypes, Updater updater) {
    this(name, allowedTypes, updater, null);
  }

  ConfigOption(
      String name, String allowedTypes, Updater updater, String defaultValue) {

    this.name = name;
    this.allowedTypes = allowedTypes;
    this.updater = updater;
    this.defaultValue = defaultValue;

    this.updater.setOptionName(name);
    this.updater.setOptionAllowedTypes(allowedTypes);
  }

  public String getName() {
    return name;
  }

  public String getAllowedTypes() {
    return allowedTypes;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void update(JsonElement jsonElement, Config.Builder builder) {
    updater.update(jsonElement, builder);
  }



  private static class Updater {

    private String optionName;

    private String optionAllowedTypes;


    public void update(boolean value, Config.Builder builder) {
      throwExceptionOnOptionWrongType(Boolean.toString(value));
    }

    public void update(Number value, Config.Builder builder) {
      throwExceptionOnOptionWrongType(value.toString());
    }

    public void update(String value, Config.Builder builder) {
      throwExceptionOnOptionWrongType(value);
    }

    public void update(JsonArray value, Config.Builder builder) {
      throwExceptionOnOptionWrongType(value.toString());
    }

    public void update(JsonObject value, Config.Builder builder) {
      throwExceptionOnOptionWrongType(value.toString());
    }

    private void update(JsonElement jsonElement, Config.Builder builder) {
      if (jsonElement.isJsonPrimitive()) {
        JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();

        if (primitive.isString()) {
          update(primitive.getAsString(), builder);
        } else if (primitive.isBoolean()) {
          update(primitive.getAsBoolean(), builder);
        } else if (primitive.isNumber()) {
          update(primitive.getAsNumber(), builder);
        }
      } else if (jsonElement.isJsonArray()) {
        update(jsonElement.getAsJsonArray(), builder);
      } else if (jsonElement.isJsonObject()) {
        update(jsonElement.getAsJsonObject(), builder);
      }
    }

    public void setOptionName(String name) {
      this.optionName = name;
    }

    public void setOptionAllowedTypes(String types) {
      this.optionAllowedTypes = types;
    }

    private void throwExceptionOnOptionWrongType(String jsonElementValue) {
      throw new IllegalArgumentException(
          String.format(
              "Option '%s' must be %s. Found: %s",
              this.optionName, this.optionAllowedTypes, jsonElementValue));
    }
  }

}
