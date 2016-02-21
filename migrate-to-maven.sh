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
# Extract the IASNodeAdapter as this is needed for generating code in the compiler.
mkdir -p compiler-jburg-types/src/main/java/org/apache/flex/compiler/internal/as/codegen
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/IASNodeAdapter.java compiler-jburg-types/src/main/java/org/apache/flex/compiler/internal/as/codegen
# Convert the compiler project itself
mkdir -p compiler/src/main/java
mkdir -p compiler/src/main/resources
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
git mv compiler.tests/src/org/apache/flex/utils compiler/src/test/java/org/apache/flex

#############################################
## Convert the compiler.jx module
#############################################
mkdir -p compiler.jx/src/main/java
mkdir -p compiler.jx/src/main/resources
git mv compiler.jx/src/META-INF compiler.jx/src/main/resources
git mv compiler.jx/src/com compiler.jx/src/main/java
git mv compiler.jx/src/org compiler.jx/src/main/java
# compiler.jx/src/main/java/org/apache/flex/compiler/internal/codegen/js/flexjs/Notes_JSFlexJSEmitter.txt
# Move the tests from the separate project into the compiler.jx project
mkdir -p compiler.jx/src/test/java
git mv compiler.jx.tests/src/org compiler.jx/src/test/java
mkdir -p compiler.jx/src/test/resources
git mv compiler.jx.tests/test-files/* compiler.jx/src/test/resources

