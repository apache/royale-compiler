REM #############################################
REM ## Clean up some zombie directories form
REM ## previous conversions.
REM #############################################
rmdir compiler\src\main
rmdir compiler\src\test
rmdir compiler.jx\src\main
rmdir compiler.jx\src\test
rmdir flex-compiler-oem\src\main

REM #############################################
REM ## Convert the flex-compiler-oem module
REM #############################################
mkdir flex-compiler-oem\src\main\java
git mv flex-compiler-oem/src/flex2 flex-compiler-oem/src/main/java
git mv flex-compiler-oem/src/macromedia flex-compiler-oem/src/main/java

REM #############################################
REM ## Convert the compiler module
REM #############################################
REM # Extract the UnknownTreePatternInputOutput generator.
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/UnknownTreePatternInputOutput.java compiler-build-tools/src/main/java/org/apache/flex/compiler/internal/as/codegen
REM # Extract the test-adapter code.
mkdir compiler-build-tools\src\main\java\org\apache\flex\utils
git mv compiler.tests/src/org/apache/flex/utils/* compiler-build-tools/src/main/java/org/apache/flex/utils
REM # Create a copy of the FilenameNormalization as we need this in both projects.
cp compiler/src/org/apache/flex/utils/FilenameNormalization.java compiler-build-tools/src/main/java/org/apache/flex/utils
git add compiler-build-tools/src/main/java/org/apache/flex/utils/FilenameNormalization.java
REM # Extract the IASNodeAdapter as this is needed for generating code in the compiler.
mkdir compiler-jburg-types\src\main\java\org\apache\flex\compiler\internal\as\codegen
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/IASNodeAdapter.java compiler-jburg-types/src/main/java/org/apache/flex/compiler/internal/as/codegen

REM # Convert the compiler project itself
mkdir compiler\src\main\java
mkdir compiler\src\main\resources
mkdir compiler\src\main\jflex\org\apache\flex\compiler\internal\parsing\as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/*.lex compiler/src/main/jflex/org/apache/flex/compiler/internal/parsing/as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/skeleton.* compiler/src/main/jflex/org/apache/flex/compiler/internal/parsing/as
mkdir compiler\src\main\jflex\org\apache\flex\compiler\internal\parsing\mxml
git mv compiler/src/org/apache/flex/compiler/internal/parsing/mxml/*.lex compiler/src/main/jflex/org/apache/flex/compiler/internal/parsing/mxml
mkdir compiler\src\main\antlr\org\apache\flex\compiler\internal\parsing\as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/ASParser.g compiler/src/main/antlr/org/apache/flex/compiler/internal/parsing/as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/MetadataParser.g compiler/src/main/antlr/org/apache/flex/compiler/internal/parsing/as
git mv compiler/src/org/apache/flex/compiler/internal/parsing/as/ImportMetadataTokenTypes.txt compiler/src/main/antlr/org/apache/flex/compiler/internal/parsing/as
mkdir compiler\src\main\jburg\org\apache\flex\compiler\internal\css\codegen
git mv compiler/src/org/apache/flex/compiler/internal/css/codegen/*.jbg compiler/src/main/jburg/org/apache/flex/compiler/internal/css/codegen
mkdir compiler\src\main\antlr3\org\apache\flex\compiler\internal\css
git mv compiler/src/org/apache/flex/compiler/internal/css/*.g compiler/src/main/antlr3/org/apache/flex/compiler/internal/css
mkdir compiler\src\main\jburg\org\apache\flex\compiler\internal\as\codegen
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/*.jbg compiler/src/main/jburg/org/apache/flex/compiler/internal/as/codegen
git mv compiler/src/main/jburg/org/apache/flex/compiler/internal/as/codegen/cmc.jbg compiler/src/main/jburg/org/apache/flex/compiler/internal/as/codegen/CmcEmitter.jbg
git mv compiler/src/main/jburg/org/apache/flex/compiler/internal/css/codegen/css.jbg compiler/src/main/jburg/org/apache/flex/compiler/internal/css/codegen/CSSEmitter.jbg
mkdir compiler\src\main\unknowntreehandler\org\apache\flex\compiler\internal\as\codegen
git mv compiler/src/org/apache/flex/compiler/internal/as/codegen/UnknownTreeHandlerPatterns.xml compiler/src/main/unknowntreehandler/org/apache/flex/compiler/internal/as/codegen
git mv compiler/src/org compiler/src/main/java
git mv compiler/src/META-INF compiler/src/main/resources
git mv compiler/src/overview.html compiler/src/main/resources
git add compiler/src/main
REM # Separate the resource bundles.
mkdir compiler\src\main\resources\org\apache\flex\compiler
git mv compiler/src/main/java/org/apache/flex/compiler/messages_*.properties compiler/src/main/resources/org/apache/flex/compiler
REM # Clean up some invalidly named files
git mv compiler/src/main/java/org/apache/flex/compiler/internal/css/package.hmtl compiler/src/main/java/org/apache/flex/compiler/internal/css/package.html
git mv compiler/src/main/java/org/apache/flex/compiler/tree/mxml/index.html compiler/src/main/java/org/apache/flex/compiler/tree/mxml/package.html
REM # Move the tests from the separate project into the compiler project
mkdir compiler\src\test\java
mkdir compiler\temp
mkdir compiler\results
git mv compiler.tests/unit-tests/org compiler/src/test/java
git mv compiler.tests/feature-tests/as compiler/src/test/java
git mv compiler.tests/feature-tests/mxml compiler/src/test/java
git mv compiler.tests/feature-tests/properties compiler/src/test/java
git mv compiler.tests/functional-tests/f compiler/src/test/java
REM # Clean up
git rm -r generated
git rm -r lib
git rm -r results
git rm -r temp
git rm -r tools
git rm -r utils

REM #############################################
REM ## Convert the compiler.jx module
REM #############################################
mkdir compiler.jx\src\main\java
mkdir compiler.jx\src\main\resources
git mv compiler.jx/src/META-INF compiler.jx/src/main/resources
git mv compiler.jx/src/com compiler.jx/src/main/java
git mv compiler.jx/src/org compiler.jx/src/main/java
REM # compiler.jx/src/main/java/org/apache/flex/compiler/internal/codegen/js/flexjs/Notes_JSFlexJSEmitter.txt
REM # Move the tests from the separate project into the compiler.jx project
mkdir compiler.jx\src\test\java
git mv compiler.jx.tests/src/org compiler.jx/src/test/java
REM # Remove the duplicate EnvProperties
git rm -r compiler.jx.tests/src/org compiler.jx/src/test/java/org/apache/flex/utils
mkdir compiler.jx\src\test\resources
git mv compiler.jx.tests/test-files/* compiler.jx/src/test/resources

REM #############################################
REM ## Convert the externs
REM #############################################

REM # asdocs

REM # cordova
mkdir externs\cordova\src\main\javascript
git mv externs/cordova/externs/* externs/cordova/src/main/javascript
rmdir externs\cordova\externs

REM #createjs
mkdir externs\createjs\src\main\javascript
git mv externs/createjs/missing.js externs/createjs/src/main/javascript

REM # GCL
mkdir externs\GCL\src\main\flex
git mv externs/GCL/src/goog externs/GCL/src/main/flex
git rm -r externs/GCL/externs
rmdir externs\GCL\out

REM # google_maps

REM # jasmine

REM # jquery

REM # js
mkdir externs\js\src\main\flex\__AS3__\vec
git mv externs/js/src/AS3.as externs/js/src/main/flex/AS3.as
git mv externs/js/src/Vector.as externs/js/src/main/flex/__AS3__/vec/Vector.as
git rm externs/js/src/Vector-template.as
mkdir externs\js\src\main\javascript
git mv externs/js/missing.js externs/js/src/main/javascript

REM # node
mkdir externs\node\src\main\javascript
git mv externs/node/externs/* externs/node/src/main/javascript
rmdir externs\node\externs

