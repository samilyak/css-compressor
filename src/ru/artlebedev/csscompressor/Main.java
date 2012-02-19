/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.18
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import java.io.IOException;

public final class Main {

  public static void main(String args[]) throws IOException {
    if (args.length == 0 || args[0].isEmpty()) {
      usage();
      System.exit(1);
    }

    Config config = ConfigParser.parse(args[0]);
    new CssCompressor(config).compress();
  }

  private static void usage() {
    System.err.println("CSS Compressor");
    System.err.println("Usage: java -jar css-compressor.jar config-json-file");
  }

}
