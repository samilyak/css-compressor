/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.12
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.google.gson.JsonElement;


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
    } finally {
      inputStream.close();
      fileStream.close();
    }
  }


  public static void writeToFile(String path, String content)
      throws IOException {

    writeToFile(path, content, "UTF-8");
  }

  public static void writeToFile(
      String path, String content, String charset) throws IOException {

    FileOutputStream fileStream = new FileOutputStream(path);
    OutputStreamWriter outputStream =
        new OutputStreamWriter(fileStream, charset);
    
    try {
      outputStream.write(content);
    } finally {
      outputStream.close();
      fileStream.close();
    }
  }
  
  
  

  /**
   * @return  If element is a JsonPrimitive that corresponds to a string, then
   *          return the value of that string; otherwise, return null.
   */
  public static String jsonElementToStringOrNull(JsonElement element) {
    if (element == null || element.isJsonNull() || !isJsonString(element)) {
      return null;
    }

    return element.getAsString();
  }


  public static boolean isJsonString (JsonElement element) {
    return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
  }

}
