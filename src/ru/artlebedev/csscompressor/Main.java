/**
 * Author: Alexander Samilyak (aleksam241@gmail.com)
 * Created: 2012.02.18
 * Copyright 2012 Art. Lebedev Studio. All Rights Reserved.
 */

package ru.artlebedev.csscompressor;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public final class Main {

  private static Options options = createOptions();


  public static void main(String args[]) throws IOException {
    CommandLine cmdLine = createCommandLine(args);

    if (cmdLine.hasOption("help") || cmdLine.getArgs().length == 0) {
      printUsage();
      System.exit(1);
    }

    ConfigBuilder builder = new ConfigBuilder(cmdLine);
    new CssCompressor(builder.build()).compress();
  }


  private static Options createOptions() {
    Options options = new Options();
    options
        .addOption(
            OptionBuilder
                .withLongOpt("replace")
                .withArgName("regex-search::replace")
                .withDescription(
                    "String replacement performing on a result css string. " +
                    "This argument's value must contain :: as a delimiter " +
                    "between search and replace portions. " +
                    "Be aware that <regex-search> is a regex pattern, so " +
                    "regex special chars escaping is up to you. " +
                    "$1, $2,.. in a <replace> portion are links to groups " +
                    "in a regex pattern." +
                    "\nYou can use this option many times.")
                .hasArg()
                .create())
        .addOption(
            OptionBuilder
                .withDescription(
                    "do not print service messages to stdout " +
                     "(like preprocessing commands)")
                .withLongOpt("quiet")
                .create())
        .addOption(
            OptionBuilder
                .withDescription("print this message")
                .withLongOpt("help")
                .create("h"));

    return options;
  }


  private static CommandLine createCommandLine(String args[])
      throws RuntimeException {

    CommandLineParser parser = new BasicParser();
    CommandLine cmdLine;

    try {
      cmdLine = parser.parse(options, args);
    } catch (ParseException e) {
      throw new RuntimeException(
          "Command line arguments parsing has failed: " + e.getMessage());
    }

    return cmdLine;
  }


  private static void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.setSyntaxPrefix("Usage: ");

    formatter.printHelp(
        "java -jar css-compressor.jar [options] config-json-file",
        options);
  }

}
