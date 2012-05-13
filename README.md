CSS Compressor based on YUI Compressor (its CSS related class
com.yahoo.platform.yui.compressor.CssCompressor that is called for
internal processing).

The main goal of this compressor is to parse CSS imports `@import` inside
CSS sources and inline them (code containing in imported CSS file).
So you need to specify only root file or files. Then all its transitive imports
will be concatenated in one file that will be compressed with CSS part of
YUI Compressor.


Basic usage:

```
java -jar css-compressor.jar config-json-file
```

Example of a config-json-file containing all possible options:

```json
{

  "modules": {
    "main": "main.css",

    "home": ["home1.css", "home2.css"],

    "special1": {
      "inputs": "special.css",
      "output": "my-dir/my-special-build.css"
    },

    "special2": {
      "inputs": ["special1.css", "special2.css"],
      "output": "my-dir/%s.css"
    }
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

<br />
`modules` **Object**, required <br />
An object literal containing mappings from module name to module definition.
A definition can be either a string, an array of strings, or an object:

 1. If a string, it's used as an input file path. Global option `output-path`
 is used to construct output file path.
 2. If an array of strings, it's used as input file paths to multiple files
 that should be built together. Global option `output-path` is used to construct
 output file path.
 3. If an object, it's used as input and output file paths. Object keys:
   * `inputs` – either a string, or an array of strings (see above). Required.
   * `output` – string. Optional if global option `output-path` is presented,
   required otherwise. %s in this string will be replaced with module name.
   This option if presented will be used instead of global `output-path`.

All paths are relative to global option `root`.

<br />
`output-path` **string**, required if some modules are in format 1 or 2
(see above) <br />
A mask using for creating output path for result modules build files.
%s will be replaced with module name from `modules` option. This option is
required for modules whose definition is either a string,
or an array of strings (that don't have its own output).
This path is relative to `root` option.

<br />
`output-wrapper` **string** or **Array**, optional <br />
A template into which compiled css will be written. The placeholder
for compressed css is %output%. Array of strings will be concatenated together.

<br />
`root` **string**, optional <br />
A path relative to which module inputs and outputs will be calculated.
This path itself is calculated relative to json config file location.
Defaults to current catalog ".".

<br />
`charset` **string**, optional <br />
File charset using for reading inputs and writing outputs. Defaults to UTF-8.
