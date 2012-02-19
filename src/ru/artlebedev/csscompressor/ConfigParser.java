/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.19
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class ConfigParser {

  private ConfigParser() {}

  public static Config parse(String filePath) throws IOException {
    JsonElement root = new JsonParser().parse(Utils.readFile(filePath));

    if (!root.isJsonObject()) {
      throw new RuntimeException(String.format(
          "Config file %s contains not a JSON object as its root",
          filePath));
    }

    JsonObject jsonConfig = root.getAsJsonObject();
    Config.Builder configBuilder = Config.createBuilder(filePath);


    // Keep track of the keys in the 'options' object so that we can warn
    // about unused options in the config file.
    Set<String> options = new HashSet<String>();
    for (Map.Entry<String, JsonElement> entry : jsonConfig.entrySet()) {
      options.add(entry.getKey());
    }

    for (ConfigOption configOption : ConfigOption.values()) {
      String optionName = configOption.getName();

      if (jsonConfig.has(optionName)) {
        configOption.update(jsonConfig.get(optionName), configBuilder);
        options.remove(optionName);
      }
    }

    for (String unusedOption : options) {
      System.err.printf(
          "WARNING: Unused option \"%s\" in %s\n",
          unusedOption, filePath);
    }

    return configBuilder.build();
  }

}
