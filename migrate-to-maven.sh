#!/usr/bin/env bash

#############################################
## Clean up some zombie directories form
## previous conversions.
#############################################
rm -rf compiler/src/main
rm -rf compiler/src/test
rm -rf compiler.jx/src/main
rm -rf compiler.jx/src/test
rm -rf flex-compiler-oem/src/main
rm -rf externs/GCL/src/main/flex

#############################################
## Convert the flex-compiler-oem module
#############################################
mkdir -p flex-compiler-oem/src/main/java
git mv flex-compiler-oem/src/flex2 flex-compiler-oem/src/main/java
git mv flex-compiler-oem/src/macromedia flex-compiler-oem/src/main/java

#############################################
## Convert the compiler module
#############################################
# Extract the UnknownTreePatternInputOutput generator.
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/UnknownTreePatternInputOutput.java compiler-build-tools/src/main/java/org/apache/flex/compiler/internal/as/codegen
# Extract the test-adapter code.
mkdir -p compiler-build-tools/src/main/java/org/apache/flex/utils
git mv compiler.tests/src/org/apache/flex/utils/* compiler-build-tools/src/main/java/org/apache/flex/utils
# Create a copy of the FilenameNormalization as we need this in both projects.
cp compiler/src/org/apache/flex/utils/FilenameNormalization.java compiler-build-tools/src/main/java/org/apache/flex/utils
git add compiler-build-tools/src/main/java/org/apache/flex/utils/FilenameNormalization.java
# Extract the IASNodeAdapter as this is needed for generating code in the compiler.
mkdir -p compiler-jburg-types/src/main/java/org/apache/flex/compiler/internal/as/codegen
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/IASNodeAdapter.java compiler-jburg-types/src/main/java/org/apache/flex/compiler/internal/as/codegen

# Convert the compiler project itself
mkdir -p compiler/src/main/java
mkdir -p compiler/src/main/resources
# Move the scripts
mkdir -p compiler/src/assembly/scripts
git mv compiler/commandline/* compiler/src/assembly/scripts
rm -r compiler/commandline

mkdir -p compiler/src/main/jflex/org/apache/flex/compiler/internal/parsing/as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/*.lex compiler/src/main/jflex/org/apache/flex/compiler/internal/parsing/as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/skeleton.* compiler/src/main/jflex/org/apache/flex/compiler/internal/parsing/as
mkdir -p compiler/src/main/jflex/org/apache/flex/compiler/internal/parsing/mxml
git mv compiler/src/org/apache/flex/compiler/internal/parsing/mxml/*.lex compiler/src/main/jflex/org/apache/flex/compiler/internal/parsing/mxml
mkdir -p compiler/src/main/antlr/org/apache/flex/compiler/internal/parsing/as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/ASParser.g compiler/src/main/antlr/org/apache/flex/compiler/internal/parsing/as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/MetadataParser.g compiler/src/main/antlr/org/apache/flex/compiler/internal/parsing/as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/ImportMetadataTokenTypes.txt compiler/src/main/antlr/org/apache/flex/compiler/internal/parsing/as
mkdir -p compiler/src/main/jburg/org/apache/flex/compiler/internal/css/codegen
git mv compiler/src/org/apache/flex/compiler/internal/css/codegen/*.jbg compiler/src/main/jburg/org/apache/flex/compiler/internal/css/codegen
mkdir -p compiler/src/main/antlr3/org/apache/flex/compiler/internal/css
git mv compiler/src/org/apache/flex/compiler/internal/css/*.g compiler/src/main/antlr3/org/apache/flex/compiler/internal/css
mkdir -p compiler/src/main/jburg/org/apache/flex/compiler/internal/as/codegen
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/*.jbg compiler/src/main/jburg/org/apache/flex/compiler/internal/as/codegen
git mv compiler/src/main/jburg/org/apache/flex/compiler/internal/as/codegen/cmc.jbg compiler/src/main/jburg/org/apache/flex/compiler/internal/as/codegen/CmcEmitter.jbg
git mv compiler/src/main/jburg/org/apache/flex/compiler/internal/css/codegen/css.jbg compiler/src/main/jburg/org/apache/flex/compiler/internal/css/codegen/CSSEmitter.jbg
mkdir -p compiler/src/main/unknowntreehandler/org/apache/flex/compiler/internal/as/codegen
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/UnknownTreeHandlerPatterns.xml compiler/src/main/unknowntreehandler/org/apache/flex/compiler/internal/as/codegen
git mv compiler/src/org compiler/src/main/java
git mv compiler/src/META-INF compiler/src/main/resources
git mv compiler/src/overview.html compiler/src/main/resources
git add compiler/src/main
# Separate the resource bundles.
mkdir -p compiler/src/main/resources/org/apache/flex/compiler
git mv compiler/src/main/java/org/apache/flex/compiler/messages_*.properties compiler/src/main/resources/org/apache/flex/compiler
# Clean up some invalidly named files
git mv compiler/src/main/java/org/apache/flex/compiler/internal/css/package.hmtl compiler/src/main/java/org/apache/flex/compiler/internal/css/package.html
git mv compiler/src/main/java/org/apache/flex/compiler/tree/mxml/index.html compiler/src/main/java/org/apache/flex/compiler/tree/mxml/package.html
# Move the tests from the separate project into the compiler project
mkdir -p compiler/src/test/java
mkdir -p compiler/temp
mkdir -p compiler/results
git mv compiler.tests/unit-tests/org compiler/src/test/java
git mv compiler.tests/feature-tests/as compiler/src/test/java
git mv compiler.tests/feature-tests/mxml compiler/src/test/java
git mv compiler.tests/feature-tests/properties compiler/src/test/java
git mv compiler.tests/functional-tests/f compiler/src/test/java

# Clean up
rm -r compiler/.settings
rm -r compiler/lib
git rm -r compiler/results
git rm -r compiler/temp
git rm -r compiler/tools
rm -r compiler/utils
rm compiler/.classpath
rm compiler/.project
git rm compiler/build.xml
git rm compiler/downloads.xml
git rm compiler/flexTasks.tasks
git rm -r compiler.tests
rm -r compiler.tests

#############################################
## Remove the compiler.js module
#############################################

git rm -r compiler.js
rm -r compiler.js

#############################################
## Convert the compiler.jx module
#############################################
mkdir -p compiler.jx/src/main/java
mkdir -p compiler.jx/src/main/resources
# Move the scripts
mkdir -p compiler.jx/src/assembly/scripts
git mv compiler.jx/bin/* compiler.jx/src/assembly/scripts
rm compiler.jx/bin

git mv compiler.jx/src/META-INF compiler.jx/src/main/resources
git mv compiler.jx/src/com compiler.jx/src/main/java
git mv compiler.jx/src/org compiler.jx/src/main/java
# compiler.jx/src/main/java/org/apache/flex/compiler/internal/codegen/js/flexjs/Notes_JSFlexJSEmitter.txt
# Move the tests from the separate project into the compiler.jx project
mkdir -p compiler.jx/src/test/java
git mv compiler.jx.tests/src/org compiler.jx/src/test/java
# Remove the duplicate EnvProperties
git rm -r compiler.jx.tests/src/org compiler.jx/src/test/java/org/apache/flex/utils
mkdir -p compiler.jx/src/test/resources
git mv compiler.jx.tests/test-files/* compiler.jx/src/test/resources

# Clean up
rm -r compiler.jx/in
rm -r compiler.jx/lib
git rm -r compiler.jx/templ
rm compiler.jx/.classpath
rm compiler.jx/.project
git rm -r compiler.jx/build.xml
git rm -r compiler.jx/downloads.xml
git rm -r compiler.jx/local-template.properties
git rm -r compiler.jx.tests
rm -r compiler.jx.tests

#############################################
## Convert the externs
#############################################

# asdocs

# cordova
mkdir -p externs/cordova/src/main/javascript
git mv externs/cordova/externs/* externs/cordova/src/main/javascript
rm -r externs/cordova/externs

#createjs
mkdir -p externs/createjs/src/main/javascript
git mv externs/createjs/missing.js externs/createjs/src/main/javascript/missing.js

# GCL
mkdir -p externs/GCL/src/main/flex
git mv externs/GCL/src/goog externs/GCL/src/main/flex
git rm -r externs/GCL/externs
rm -r externs/GCL/out

# google_maps

# jasmine

# jquery

# js
mkdir -p externs/js/src/main/flex/__AS3__/vec
git mv externs/js/src/AS3.as externs/js/src/main/flex/AS3.as
git mv externs/js/src/Vector.as externs/js/src/main/flex/__AS3__/vec/Vector.as
git rm externs/js/src/Vector-template.as
mkdir -p externs/js/src/main/javascript
git mv externs/js/missing.js externs/js/src/main/javascript/missing.js

# node
mkdir -p externs/node/src/main/javascript
git mv externs/node/externs/* externs/node/src/main/javascript
rm -r externs/node/externs

# clean up
git rm -r generated
git rm -r installer.properties
rm -r swfutils
git rm -r utils
rm -r generated
git rm -r maven
rm -r utils
git rm maven.txt
git rm maven.xml
git rm jenkins.xml
git rm installer.xml
git rm implicit_imports.patch
git rm FalconJXFormat.xml
git rm build.xml
git rm build.properties
git rm releasecandidate.xml
rm -r compiler.jx/bin
rm -r compiler.jx/temp
rm -r compiler/temp
rm -r compiler/results
git rm -r compiler/dist
git rm externs/flex-config.xml