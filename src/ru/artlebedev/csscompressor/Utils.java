/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.12
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;


public class Utils {

  private Utils() {}


  public static String readFile(String path) throws IOException {
    return readFile(path, "UTF-8");
  }

  public static String readFile(String path, String charset)
      throws IOException {

    FileInputStream fileStream = new FileInputStream(path);
    InputStreamReader inputStream = new InputStreamReader(fileStream, charset);

    try {
      StringBuilder sb = new StringBuilder();

      int c;
      while ((c = inputStream.read()) != -1) {
        sb.append((char) c);
      }

      return sb.toString();
    }
    finally {
      fileStream.close();
      inputStream.close();
    }
  }


  /**
   * @return  If element is a JsonPrimitive that corresponds to a string, then
   *          return the value of that string; otherwise, return null.
   */
  public static String toJsonStringOrNull(JsonElement element) {
    if (element == null) {
      return null;
    }
    if (element.isJsonPrimitive()) {
      JsonPrimitive primitive = element.getAsJsonPrimitive();
      if (primitive.isString()) {
        return primitive.getAsString();
      }
    }
    return null;
  }

}
