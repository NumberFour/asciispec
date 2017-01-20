#!/bin/bash
## Syntax for following set command:
# -e  == exit immediately
# -x  == enable debug. (+x for disable)
# -v  == Print shell input lines as they are read.
set -e +x -v

########## Directory Locations ###########
# output folder:
GEN_FOLDER=generated-docs/html

rm -rf ./$GEN_FOLDER/; mkdir -p ./$GEN_FOLDER/

echo INFO: Copying resources to ./$GEN_FOLDER/
cp -r scripts styles images ./$GEN_FOLDER/

echo INFO: AsciiSpec Generating HTML

####################### Build HTML for gh-pages #######################
asciispec -a stylesheet=foundation.css -a docinfodir=html-templates -D $GEN_FOLDER/ index.adoc

echo INFO: AsciiSpec HTML conversion Done

# running "./build.sh -p" (preview) will skip PDF and launch index.html
if [ "${1}" == "--preview" ] || [ "${1}" == "-p" ]; then
open ./$GEN_FOLDER/index.html
exit 0
fi

####### Build PDF for gh-pages download #######
echo INFO: AsciiSpec Generating PDF
asciispec -b docbook index.adoc
fopub index.xml
rm index.xml
mv index.pdf ./$GEN_FOLDER/

echo DONE: AsciiSpec conversion finished.
