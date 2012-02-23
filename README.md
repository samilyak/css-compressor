CSS Compressor based on part of YUI Compressor related to compressing CSS
(ie class com.yahoo.platform.yui.compressor.CssCompressor is called for
internal CSS processing).

The main goal of this compressor is to parse CSS imports `@import` in CSS sources
and inline them with imported source.
So you need to specify only root file or files. Then all its transitive imports
will be concatenated in one file that will be compressed with CSS part of
YUI Compressor.


Basic usage

```
java -jar css-compressor.jar config-json-file
```

Example of a config-json-file with all possible options:

```
{

  "modules": {
    "main": [
      "main1.css", "main2.css"
    ],

    "home": "home/home.css"
  },

  "output-path": "min/%s.build.css",

  "output-wrapper": [
    "/*\n",
    "  @copyright 2012 Super Company. All Rights Reserved.\n",
    " */\n\n",
    "%output%"
  ],

  "root": ".",

  "charset": "UTF-8"

}
```

`modules` – an object literal that contains mappings
from module names to module definition.
Definition is file paths that belong to this module.
The value of each definition may be either a single string literal
or an array of string literals. All paths are relative to `root` option.

`output-path` – mask using for creating save path for result modules build files.
%s will be replaced with module name from `modules` option.
This path is relative to `root` option.

`output-wrapper` – a template into which compressed css will be written.
The placeholder for compressed css is %output%.
The value may be a string or an array of strings.
Array of strings will be concatenated together.
Optional.

`root` – path relative to which module inputs and outputs will be calculated.
This path itself is calculated relative to json config file location.
Optional, defaults to current catalog.

`charset` – files charset using for reading inputs and writing outputs.
Optional, defaults to UTF-8.
