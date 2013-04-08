## Downloads

Latest binary version on SourceForge – [css-compressor-54b604.jar](http://sourceforge.net/projects/css-compressor/files/css-compressor-54b604.jar/download)


## Usage

CSS Compressor based on YUI Compressor (its CSS related class
com.yahoo.platform.yui.compressor.CssCompressor that is called for
internal processing).

The main goal of this compressor is to parse CSS imports `@import` inside
CSS sources and inline them (code containing in imported CSS file).
So you need to specify only root file or files. Then all its transitive imports
will be concatenated in one file that will be compressed with CSS part of
YUI Compressor.


Usage:

```
java -jar css-compressor.jar [options] config-json-file
  -h,--help                           print this message
  --quiet                             do not print service messages to stdout (like preprocessing commands)
  --replace <regex-search::replace>   String replacement performing on a result css string. This argument's value must
                                      contain :: as a delimiter between search and replace portions. Be aware that
                                      <regex-search> is a regex pattern, so regex special chars escaping is up to you.
                                      $1, $2,.. in a <replace> portion are links to groups in a regex pattern.
                                      You can use this option many times.
```

--replace command line option is useful for example when you want to append
revision to css background urls. This is a command line option rather than
a config file option to allow generate search::replace string dynamically.

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

  "preprocess": "sass %s",

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
`preprocess` **string**, optional <br />
A command to run for each module to preprocess css (e.g. SASS or LESS).
%s inside string will be replaced with an input file path.
Command must exit with a 0 exit code. Command stdout will be used as css string
for further compressing, stderr will be printed to jar's stdout.
Please note that command is run in a current catalog as a working directory.

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
