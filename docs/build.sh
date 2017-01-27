#!/bin/bash
## Syntax for following set command:
# -e  == exit immediately
# -x  == enable debug. (+x for disable)
# -v  == Print shell input lines as they are read.
set -e +x -v

########## Directory Locations ###########
# Output folder:
GEN_FOLDER=generated-docs/html
rm -rf ./$GEN_FOLDER/; mkdir -p ./$GEN_FOLDER/

# Copy resources to ./$GEN_FOLDER/
cp -r scripts styles images ./$GEN_FOLDER/

######## Set HTML conversion parameters for all docs ########
PARAMS="-a stylesheet=foundation.css -D $GEN_FOLDER/"
HEADERS="-a docinfodir=html-templates"

######## Build HTML with params ########
asciispec $PARAMS $HEADERS index.adoc
asciispec $PARAMS $HEADERS/userguide userguide.adoc
asciispec $PARAMS $HEADERS/spec spec.adoc
asciispec $PARAMS $HEADERS/setup setup.adoc

# running "./build.sh -p" (preview) will skip PDF and launch index.html
if [ "${1}" == "--preview" ] || [ "${1}" == "-p" ]; then
open ./$GEN_FOLDER/index.html
exit 0
fi

####### Build PDF for gh-pages download #######
asciispec -b docbook spec.adoc
asciispec -b docbook userguide.adoc
asciispec -b docbook setup.adoc
fopub spec.xml && fopub userguide.xml && fopub setup.xml
rm *.xml && mv *.pdf ./$GEN_FOLDER/

echo DONE: AsciiSpec conversion finished.
