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

######## Build HTML for gh-pages ########
PARAMS="-a stylesheet=foundation.css -D $GEN_FOLDER/"

asciispec $PARAMS -a docinfodir=html-templates index.adoc
asciispec $PARAMS -a docinfodir=html-templates/userguide userguide.adoc

# running "./build.sh -p" (preview) will skip PDF and launch index.html
if [ "${1}" == "--preview" ] || [ "${1}" == "-p" ]; then
open ./$GEN_FOLDER/index.html
exit 0
fi

####### Build PDF for gh-pages download #######
asciispec -b docbook index.adoc
asciispec -b docbook userguide.adoc
fopub index.xml && fopub userguide.xml
rm *.xml && mv *.pdf ./$GEN_FOLDER/

echo DONE: AsciiSpec conversion finished.
