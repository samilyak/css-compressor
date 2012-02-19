#!/bin/sh

# running of this script will create catalog example/min
# containing compiled CSS according to example/config.json

java -jar ../build/css-compressor-0.1.jar config.json
