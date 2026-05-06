For additional information on recent issues that have been closed, see [Github Issues List](https://github.com/apache/royale-compiler/issues?q=is%3Aissue+is%3Aclosed)

Apache Royale Compiler 1.0.0
============================

- compiler: Added _arrow function_ syntax, similar to JavaScript. Example: `var f:Function = (x:Object) => x.toString();`
- compiler: Added _function type expression_ syntax, similar to TypeScript, to allow stricter checking of signatures, including parameters and return types. Compile-time only with conversion to `Function` at run-time. Example: `var f:(x:String, y?:Number, ...rest)=>void;`
- compiler: Added `js-include-script` compiler option to specify a _.js_ file to always be included with the compiled output. Will automatically insert a `<script src>` tag into the HTML. Available for both SWC libraries and application projects.
- compiler: Added `js-include-css` compiler option to specify a _.css_ file to always be included with the compiled output. Will automatically insert a `<link href>` tag into the HTML. Available for both SWC libraries and application projects.
- compiler: Added `js-include-asset` compiler option to specify an additional asset file to always be included with the compiled output. Available for both SWC libraries and application projects.
- compiler: Added `[JSIncludeScript]` metadata to specify a _.js_ file to be included with the compiled output only if the current class is included in the compiled output too. Otherwise, works similarly to the `js-include-script` option.
- compiler: Added `[JSIncludeCSS]` metadata to specify a _.css_ file to be included with the compiled output only if the current class is included in the compiled output too. Otherwise, works similarly to the `js-include-css` option.
- compiler: Added `[JSIncludeAsset]` metadata to specify an additional asset file to be included with the compiled output only if the current class is included in the compiled output too. Otherwise, works similarly to the `js-include-asset` option.
- compiler: Added ability to specify empty string for `html-output-filename` to optionally skip creation of HTML file.
- compiler: Added support for more modern HTML elements in CSS.
- compiler: Added support for many more modern CSS pseudo-classes that have function-like syntax.
- compiler: Added support for embedding TTF/OTF files with `[Embed]` when targeting SWF.
- compiler: Added `js-vector-emulation-literal-function` option to optionally customize how `new <T>[]` vectors are emitted.
- compiler: Added `js-vector-emulation-element-types` option to optionally customize how parameters are passed to the `js-vector-emulation-class` type.
- compiler: Fixed missing function call signature checks on `super.method()` member access.
- compiler: Fixed incorrect method name in error message for undefined method when name starts with `@` symbol.
- compiler: Fixed exception when parsing CSS `@font-face` rule and `src` value is an unexpected type.
- compiler: Fixed missing source location for `[Embed]` metadata node.
- compiler: Fixed exception when parsing `[Embed]` metadata node and containing source path is unknown.
- compiler: Fixed array literal getting incorrectly treated as metadata when comments are parsed.
- compiler: Fixed ASDoc comments in unexpected contexts not being treated as regular multiline comments.
- compiler: Fixed exception thrown when `html-template` file is not found. Reports a problem instead.
- compiler: Fixed `<link>` being incorrectly emitted in HTML when a CSS file is not emitted.
- compiler: Fixed empty `<script>` tag being incorrectly emitted in HTML in some situations.
- compiler: Fixed cases where the `Language` class was not referenced when it should have been.
- compiler: Fixed unicode escape sequences not getting handled in MXML strings.
- compiler: Fixed MXML string output to better match Flex SDK compiler.
- compiler: Fixed whitespace handling in ASDoc tag parser.
- compiler: Fixed some problems getting ignored when syntax tree request returns null instead of an AST.
- compiler: Fixed some problems getting ignored when emitting ASDoc JSON.
- compiler: Fixed exception when parts of the AST are garbage collected by recreating definitions when necessary.
- compiler: Fixed CSS `font-weight` incorrectly emitted with `font-style` value.
- compiler: Fixed ASDoc JSON output by outputting empty values instead of skipping fields because ASDoc app expects all fields to exist.
- compiler: Fixed escaping ASDoc strings for valid JSON.
- compiler: Fixed exception when stripping quotes by checking if the quotes exist first.
- compiler: Fixed parsing of members following null conditional (`?.`) operator.
- compiler: Fixed failure to resolve function object definition.
- compiler: Fixed output for MXML when `children-as-data` is `false`.
- compiler: Fixed ability to use `<mx:Component>` and `<mx:Model>` elements as direct children of the root element when using MXML 2006 dialect.
- compiler: Fixed exception in MXML parsing when `<fx:Component>` does not have a correct closing tag.
- compiler: Fixed ignored `id` on core types in `<fx:Declarations>` when targeting SWF.
- compiler: Fixed ignored `id` on core types outside of `<fx:Declarations>` when targeting both SWF and JS.
- compiler: Fixed ignored `id` on `Class` and `Function` in `<fx:Declarations>` when targeting both SWF and JS.
- compiler: Fixed empty value not being allowed for MXML `Class` and `Function`, which should be treated as `null`.
- compiler: Fixed ignored `<fx:RegExp>` when targeting both SWF and JS.
- compiler: Fixed `rgb()` and `rgba()` parsing in CSS.
- compiler: Fixed generation of selectors for some component class names in CSS.
- compiler: Fixed parsing of CSS attribute selectors to require both operator and value, if either is present (both may still be omitted).
- compiler: Fixed parsing of CSS attribute selectors that use an indentifier instead of a string.
- compiler: Fixed `not` and other function-like pseudo-classes being allowed as identifiers in CSS.
- compiler: Fixed problem not getting reported when `<fx:Component>` specifies a `className` that isn't an ActionScript identifier.
- compiler: Fixed `<fx:Component>` class being unable to access `internal` fields in the same package, like the Flex SDK compiler.
- compiler: Fixed exception when parsing certain hexadecimal color values and number values and now reports a problem.
- compiler: Fixed low-level asc compiler failure due to an unexpected dependency on a specific project type.
- compiler: Fixed `js-vector-emulation-class` being ignored for `is` and `as` operators, and certain casts.
- compiler: Fixed `as *` incorrectly emitting `*` in JS, which doesn't exist.
- compiler: Optimized tracking of used qnames by using a set instead of a list.
- compiler: Tests may be optionally run using Adobe AIR instead of Adobe Flash Player.
- compiler: Fixed top-level children failing to be detected as declarations in MXML 2006 namespace.
- compiler: Fixed validation of MXML `<fx:DesignLayer>` when used with `[ArrayElementType]`.
- compiler: Added basic implementation of `-children-as-data=false` for JavaScript.
- compiler: Fixed invalid MXML children sometimes getting ignored completely. Now reports an appropriate error.
- compiler: Fixed MXML `<fx:Vector>` not being emitted to JavaScript.
- compiler: Fixed property assignment in MXML factory methods when using data binding.
- compiler: Fixed `<fx:DesignLayer>` allowing children without `[DefaultProperty]` metadata.
- compiler: Added `${mainClass}` token to HTML template replacement.
- compiler: Fixed resolution of `[InstanceType]` classes when they are in a package.
- compiler: JS target doesn't call `start()` for MXML classes, similar to AS3 classes.
- compiler: Fixed `mx.core.DeferredInstanceFromFunction` not getting emitted when targeting JavaScript.
- compiler: Fixed intermittent missing `goog.require()` calls when using JS target.
- compiler: Added support for `mx.core.IDeferredInstance` for default properties with `-children-as-data=false` for JavaScript.
- compiler: Added support for `mx.core.UIComponentDescriptor` with `-children-as-data=false` for JavaScript.
- compiler: Improved parsing of `[Exclude]` metadata to make the member name an identifier for tooling.
- compiler: Fixed namespace URI when namespace is defined in package.
- compiler: Fixed warning for comparison of boolean or numeric type with `null` when using `-js-default-initializers=false` compiler option.
- compiler: Fixed JavaScript code generation for E4X wildcard (.*) syntax.
- debugger: Added missing isolate ID to SWF load and unload events.
- debugger: Fixed debugger targeting the current JDK version instead of the intended minimum JDK version.
- debugger: Fixed localized messages appearing as unprocessed tokens.
- debugger: Fixed version build number.
- debugger: Fixed failure to find fdbhelp_XX.txt resources for help command.
- externc: Fixed some warnings about bad return values for certain core types.
- externc: Fixed some warnings about returning `null` for certain core types.
- externc: Fixed incorrectly returning empty string instead of `null` for `String` type.
- externc: Fixed inconsistent creation of output directories based on file system case sensitivity.
- externc: Fixed some classes that should have been `dynamic`, but were not.
- externc: Added list of top-level classes that should be `final`.
- externc: Fixed detection of super method/field when it is excluded.
- externc: Improved search for field and method overrides from interfaces.
- externc: Fixed type of fields in classes where the field is defined on an implemented interface.
- formatter: Added `indent-package-contents` option to match Flash Builder formatter option.
- formatter: Added `indent-switch-contents` option to match Flash Builder formatter option.
- formatter: Added `mxml-indent-cdata` option to match Flash Builder formatter option.
- formatter: Fixed indentation of nested `else`.
- formatter: Fixed missing space before `instanceof`.
- formatter: Fixed escape sequences in literal `RegExp`.
- formatter: Fixed exception when control flow statements are nested with mixed braces.
- formatter: Fixed exception getting printed to output multiple times.
- formatter: Fixed space not being inserted before multiline comment in some situations.
- formatter: Fixed space being incorrectly inserted between comment and `:` in object literal.
- formatter: Fixed `mxml-align-attributes` and `mxml-insert-new-line-attributes` being incorrectly marked as advanced.
- formatter: Fixed failure to decrease `switch` indent if `case`/`default` contains braces, but content after is not a block.
- formatter, linter: Fixed exception caused by null file path.
- formatter, linter: Fixed out of memory error when checking many files caused by unclosed workspace.
- formatter, linter: Fixed some problems not being filtered out when they are not relevant.
- formatter, linter: Fixed incorrect end line and end column on generated whitespace or extra tokens.
- linter: Optimized performance of handling `// @linteroff` and `// @linteron` comments.
- linter: Optimized performance of finding nearest token to location by using binary search.
- royaleunit: Detect ADL and ADT using `AIR_HOME` environment variable, if not found in `ROYALE_HOME`.
- royaleunit: Fixed detection of Adobe AIR version when major version has two digits.

Apache Royale Compiler 0.9.12
=============================

- compiler: Added new `--infer-types` compiler option that allows the compiler to automatically detect an appropriate type for both variables and function signatures that have omitted their declared types. Type inference is based on either the initializer or return values.
- compiler: Abstract classes now support abstract getter and setter methods.
- compiler: Added `--strict-flex-css` compiler option to optionally enable CSS syntax limitations that match Flex.
- compiler: Improved type checking for `&&` and `||` binary operators and `?:` ternary operator.
- compiler: Removed obsolete "AMD" and "Goog" JavaScript backends, and finished some refactoring to make codebase easier to maintain.
- compiler: Now requires Java 11 or newer to run. Previously required Java 8 minimum.
- compiler: Updated various Java dependencies with security updates.
- compiler: Fixed some Flex SDK integration tests that had been failing for a while.
- compiler: Fix parsing of `<arguments>` or `<request>` properties for `RemoteObject`, `WebService`, or `HTTPService` tags in MXML.
- compiler: Fix missing warning for duplicate function parameter names.
- compiler: Fix missing error for multiple root nodes in `<fx:Model>` tag.
- compiler: Fix missing errors for invalid MXML manifest files.
- compiler: Fix null exception when emitting JavaScript if a variable is untyped or function doesn't have a return type
- compiler: Fix exception for multiple intializers in an MXML field tag.
- compiler: Fix column position of string inside metadata.
- compiler: Fix exception when an event attribute in MXML is missing `=` and quotes.
- compiler: Fix unnecessary warning when reading SWF data that contains `EnableTelemetry` tag without a password.
- compiler: Fix backslash as an escape character for `{` and `}` inside MXML data binding.
- compiler: Fix backslash characters being unescaped in property values when emitting JavaScript.
- compiler: Fix overflow exception caused by `uint` values that overflowed an integer in the compiler.
- compiler: Fix incorrect error or warning positions for CSS content inside `<fx:Style>` tag.
- compiler: Fix non-string values in an MXML array sometimes getting incorrectly wrapped in quotes when emitting JavaScript.
- compiler: Fix null exception for `<fx:Style>` tags that contain only comments.
- compiler: Fix exceptions for `<fx:Style>` tags and _defaults.css_ files that contain invalid CSS.
- compiler: Fix silently ignoring errors in some invalid CSS content, if it appears at the end after valid content.
- compiler: Fix crash when attempting to use `--remove-circulars=false` with a release build.
- compiler: Added CSS support for modern syntax without commas in `rgb` and `rgba` functions.
- compiler: Added CSS support for `radial-gradient`, `conic-gradient`, and repeating gradient functions in JS.
- compiler: Added CSS support for several translate, rotate, scale, skew, and matrix transformation functions in JS.
- compiler: Add CSS support for declaring custom properties (CSS variables) and using `var` function in JS.
- compiler: Fix crash when `[Style]` is of type `Object` and value is passed in MXML.
- compiler: Fix null pointer exception when omitting quoted `<fx:Style>` value for `source` attribute.
- compiler: Fix null pointer exception when omitting quoted `<fx:Binding>` value for `source`, `destination`, and `twoWay` attributes.
- compiler: Fix null pointer exception when omitting quoted `<fx:Vector>` value for `fixed`, and `type` attributes.
- compiler: Fix missing problem for invalid `twoWay` value for `<fx:Binding>` tag.
- compiler: Fix exception for unexpected attributes added to `<fx:Array>` and `<fx:Vector>` tags.
- compiler: Changed to better problem message when MXML `implements` attribute is empty.
- compiler: Fix exception when assigning to a property that has a getter, but not a setter.
- compiler: Fix exception when working with certain namespace expressions.
- compiler: Fix configuration problems not being reported when compc options are passed to mxmlc.
- compiler: Fix source maps for fields and methods in the private namespace or custom namespaces.
- compiler: Fixed end line and end column of numeric literal with `+` or `-` sign.
- compiler: Fixed lines and columns of function body.
- compiler: Added support for Friendly Call Frames in source maps.
- compiler: Fixed missing warning for assignment in `else if` conditional.
- compiler: Fixed missing warnings for assignment, return, and comparison of `null`, `undefined`, and `NaN` values with other values that could never possibly be compatible.
- compiler: Fixed missing errors when calling objects as functions that can never possibly be callable.
- compiler: Add `[RoyaleCallableInstances]` metadata to allow instances of a typedef class to be callable as functions. Used by jQuery externs.
- compiler: Fixed incorrect conflicting definition error when a method in a language namespace (like public/protected) has the same name as a method in a custom namespace.
- debugger: Fix exception when evaluating certain expressions at run-time.
- formatter: Added `insert-new-line-else` configuration option.
- formatter: Filtered out unnecessary compiler warnings.
- formatter: Fix stack overflow on some platforms when `<fx:Script>` code is large enough.
- formatter: Fix missing indent after empty object literal when `collapse-empty-blocks` is true (it's not a block).
- formatter: Fix Unicode and ASCII escape sequences getting lost when formatting string literals.
- formatter: Fix formatting for configuration constants that gate definitions (like types or fields) without braces.
- formatter: Fix formatting for object literal inside parentheses.
- formatter: Fix ignored formatting when file contains unresolved configuration constants.
- formatter: Fix formatting for comments on same line as control flow that doesn't have braces.
- formatter: Fix _asformat-config.xml_ file being ignored.
- formatter: Fix ignored implicit semicolon being formatted as `null`.
- formatter: Fix formatting for comment between brace and `else`.
- formatter: Fix formatting of unary operators that (depending on context) may also be arithmetic operators.
- formatter: Fix formatting for switch/case inside for-each or for-in loop.
- linter: Fix implicit semicolon incorrectly detected as an empty statement that should be removed (it can't be removed).
- linter: Fix MXML linter being incorrectly used for _.as_ files and AS3 linter being incorrectly used for _.mxml_ files.
- Building royale-compiler now requires JDK 11 or newer.

Apache Royale Compiler 0.9.10
=============================

- **aslint** is a new command line code linter for ActionScript and MXML, with a programmatic API for use in editors and IDEs.
- compiler: Added `--watch` compiler option that keeps compiler active and incrementally compiles when changes to _.as_ and _.mxml_ files are detected. You may exit with Ctrl+C.
- compiler: Added support for `??` null coalescing operator to ActionScript.
- compiler: Added support for `?.` null conditional operator to ActionScript.
- compiler: Added support for `@""` verbatim strings to ActionScript.
- compiler: Fix JS output for chained `Date` setter initialization, like `date.minutes = date.seconds = 0`.
- compiler: Fix `<![CDATA[]]>` handling in MXML for properties with `[CollapseWhiteSpace]` metadata.
- royaleunit-ant-tasks: The `<royaleunit>` Ant Task can now use Playwright to run headless tests in HTML/JS. Set the `player` to chromium, webkit, or firefox.
- royaleunit-ant-tasks: Added a new `commandArgs` attribute to the `<royaleunit>` Ant Task. It may be used to pass extra command line arguments to the executable specified in the `command` attribute.
- formatter: Can load configuration options with `-load-config+=path/to/file.xml`, similar to compiler.
- formatter: If a file named _asformat-config.xml_ appears in the current working directory, automatically load it with `-load-config`. To disable this behavior, use `-skip-local-config-file=true`.
- formatter: Fix issue where more than one new line might appear at the end of a file.
- formatter: Fix indentation between opening and closing parentheses/brackets on separate lines.
- formatter: Fix detection of `Script` elements in MXML when line endings are CRLF.
- formatter: Fix exception when file starts with UTF BOM character.
- formatter: Fix issue where ternary operator inside `if` condition was formatted incorrectly.
- formatter: Fix missing formatting for `finally` block.
- formatter: If a case or default clause in a `switch` contains only a block, indent is not increased.
- formatter: If formatting is skipped because compiler errors are found, display those compiler errors.
- Building royale-compiler now requires JDK 8 or newer.

Apache Royale Compiler 0.9.9
============================

- **asformat** is a new command line code formatter for ActionScript and MXML, with a programmatic API for use in editors and IDEs.
- Added a boolean new compiler option `compiler.mxml.force-local-id` - (short commandline form `force-local-id`). This is a quick way to avoid propagating id attributes to browser DOM in JSRoyale. 
- (JS) Added new string compiler options `js-getter-prefix` and `js-setter-prefix` to optionally specify different prefixes to use instead of 'get_' and 'set_' for accessors in the generated JavaScript. Useful for integrating with JS libraries/languages that use a different naming convention.
- (JS) Fix for `@royalesuppressexport` feature which had stopped working
- (JS) Fix for `mx.managers.SystemManager` subclass not being generated for applications that were not direct subclasses of the relevant application classes.
- (JS) Improvement in output of locale properties files as ResourceBundles. Now using original Flex/SWF parsing approach for improved parity.
- (JS) First implementation of multiple-catch support in Javascript
- (JS) Added unsafe option for outputting literal javascript (via externally defined jsUnsafeNativeInline function)
- Fixed `<!---->` in MXML being incorrectly detected as an unclosed ASDoc comment, instead of an empty regular comment.
- (JS) Improved the generated JavaScript for `if`, `else if`, and `else` statements that contained only a semicolon and no braces.
- (JS) Generated JavaScript for `parseInt()` passes `0` instead of `undefined` for radix argument to avoid number format exception.
- (JS) Fixed several issues with generated JavaScript for E4X expressions when using `js-dynamic-access-unknown-members` compiler option.
- (JS) Fixed a couple of issues with generated JavaScript when using custom namespaces.
- Fixed missing `[Event]` metadata in playerglobal.swc classes generated from documentation.
- Fixed parse exception when binding to XML.

Apache Royale Compiler 0.9.8
============================

- Fixed issue where problems in .mxml files were sometimes duplicated.
- Fixed issue where unrecognized characters in .mxml files were sometimes ignored, and now an error is reported.
- Fixed some missing syntax checks for bindable variables that should have been the same as non-bindable variables. This may produce some new errors that weren't there before (such as duplicate variable names), but they should have been.
- Fixed issue where a type annotation referencing a class with a private constructor was not correctly resolved.
- Improvements/Fixes in Binding support, added support for binding inheritance, similar to Flex.
- (JS) Source map debugging paths of SDK classes are updated to allow breakpoints in the original .as or .mxml files in the SDK when debugging in a browser or IDE.
- (JS) Added source-map-source-root compiler option to optionally customize the source root of source maps.
- (JS) No longer generates @export annotations for exported symbols in debug builds. Exports are smartly generated when creating a release build, and if they are disabled, they will be omitted from framework classes now too. This can help reduce the size of a release build.
- (JS) Fixed issue where compiling a .swc library with another .swc library on the library-path did not copy the required .js files to the new .swc library. Only when a .swc library is added external-library-path should the .js files not get copied.
- (JS) Improved reproducible builds of .swc library files by ensuring that the paths to .js.map source map files are always referenced with forward slash and never backslash. This matches the existing behavior of references to .js files included with .swc libraries.
- (JS) Static getters/setters are not accessed with ["name"] syntax in generated JavaScript anymore, which required them to always be exported, even if the associated export symbols compiler option were disabled.
- (JS) When internal namespace is used in ActionScript, the generated JavaScript adds the @package annotation.
- (JS) Fixed issue where the Language class was not loaded in the correct order when type coersion is required in a static initializer.
- (JS) (Advanced) Added export-internal-symbols and prevent-rename-internal-symbols compiler options to match the existing options for public and protected namespaces.
- (JS) (Advanced) Added prevent-rename-public-static-methods, prevent-rename-public-instance-methods, prevent-rename-public-static-variables, prevent-rename-public-instance-variables, prevent-rename-public-static-accessors, and prevent-rename-public-instance-accessors compiler options to provide more granular control when prevent-rename-public-symbols is true (same for protected and internal namespaces too).

Apache Royale Compiler 0.9.7
============================

 - Definitions containing [JSModule] with a custom module name are no longer required to use strict camelCase naming scheme.
 - Added Flex emulation RPC WebService partial support
 - Fix add event handler code in renderers and inline Components
 - (SWF/JS) Added support for [RoyaleArrayLike] metadata-driven support for proxying compile-time numeric-typed array index access to get/set method calls and also specific for-in/for-each-in loop support. 
 - (JS) Compiler updates to support more e4X variations/scenarios, including 'use namespace' and 'default xml namespace' directives
 - (JS) Compiler updates to address custom namespace-related output, and reflection support
 - (JS) Compiler updates to output more compact Reflection data
 - Fixed a [compiler memory leak](https://github.com/apache/royale-compiler/issues/117) that was occurring over multiple compilations 
 - Fixes in bindable code generation for accessors, and in bindable function generation for swf
 - Fixed show-binding-warnings=false option to switch off binding warnings
 - Maven: Many fixes, improvements and Updates
 - Support for Maven distribution (Create SDK with Maven)
 - (JS) Prevent renaming of public variables in release builds so that they may be set from MXML.
 - (JS) Fix conflict between methods of the same name in superclass and subclass, where the superclass method is private and the subclass method is public.
 - (JS) Added jsx-factory compiler option to customize the factory method used in code generated from [JSX].
 - (JS) Added inline-constants compiler option that optionally replaces references to contants with their value when the value is a primitive (like numeric, boolean, or string).
 - (SWF/JS) Added warn-this-within-closure compiler option that controls whether the compiler emits warnings when referencing "this" in closures or anonymous functions.
 - (SWF/JS) Added strict-identifier-names compiler option to make the compiler emit errors when using certain keywords as identifiers, to match the old behavior of the Flex SDK compiler.
 - (SWF/JS) Fix metadata attributes being ignored if they did not have a value. Example: async was ignored in [Test(async)].
 - (SWF/JS) Fix path resolving error when specifying a source file with ./ or .\ on the command line.
 - (SWF/JS) Fix [ArrayElementType] being ignored when setting the [DefaultProperty] in MXML.
 - (SWF) Fix mxmlc and compc scripts in the bin folder that didn't work from the command line.
 - (SWF/JS) Fix incorrect resolving of a property with a different namespace than the parent element in MXML. Properties must have the same namespace as the component, just like in Flex.
 - (SWF/JS) Fix missing error for values that cannot be parsed as text in MXML.
 - (JS) Fix stripped end quotes from strings in data binding expressions in MXML.
 - (Maven) Added missing MXML manifest for core language types like Object, Array, Number, String, Boolean, etc.
 - (JS) Fixed null reference error on source map generation with certain folder hierarchies.
 - (JS) Fixed lost UTF-8 encoding when using remove-circulars.
 - (SWF/JS) Fix missing error when setting private/inaccessible property in MXML.
 - (JS) Fix "missing var keyword" warning from Closure compiler for type definitions.
 - (SWF/JS) Fix missing error for calling a getter as a function (similar to attempting to call a variable as a function) when it is the wrong type.
 - (JS) Fix missing . in generated JS when using static getter/setter in a custom namespace.
 

Apache Royale Compiler 0.9.6
============================

 - Added -allow-abstract-classes compiler option to enable abstract keyword for classes and methods.
 - Added -allow-private-constructors compiler option to enable classes with private constructors.
 - Added -allow-import-aliases compiler option to enable import renaming syntax.
 - Added -verbose compiler option to reduce console output by default.
 - Added RoyaleUnit tasks for Apache Ant.
 - Fix incorrect compiler error when unicodeRange value is specified for Embed metadata.
 - Fix missing compiler error when adding type parameters to classes other the Vector.
 - Fix missing compiler error for intantiation of a variable with new that is not typed as Class or Function.
 - Fix missing compiler warning for missing types on function parameter.
 - Fix internal cache that broke IDEs that use compiler to provide code intelligence.
 - Fix automatic type coercion in generated JS so that it better matches SWF behavior.

Apache Royale Compiler 0.9.4
============================
 
 - @royaledebug comment on method will make the method go away in a js release build.

Apache Royale Compiler 0.9.3
============================
 
 - [CSS selectors can start with "::" and compiler adds "."](https://github.com/apache/royale-compiler/issues/40)
 - [CSS file must be minified in js-release](https://github.com/apache/royale-compiler/issues/39)
 - [filter CSS property makes compiler crash bug](https://github.com/apache/royale-compiler/issues/38)
 - [Selector element with "+" crash](https://github.com/apache/royale-compiler/issues/37)
 - [Slow Compiler Build Time enhancement](https://github.com/apache/royale-compiler/issues/36)
 - [Multiple properties in CSS fails in a particular case bug](https://github.com/apache/royale-compiler/issues/35)
 - [content: url fails to compile bug](https://github.com/apache/royale-compiler/issues/34)
 - [selectors without initial dot are removed from final css bug](https://github.com/apache/royale-compiler/issues/32)
 - [selector::pseudo-element is compiled into selector:pseudo-element (removes one ":") bug](https://github.com/apache/royale-compiler/issues/31)
 - [CSS element > element selector is ignored bug](https://github.com/apache/royale-compiler/issues/30)
 - [Compiler Internal Error on Empty Binding bug](https://github.com/apache/royale-compiler/issues/29)
 - [[CSS] using linear-gradient make SWF not compile bug](https://github.com/apache/royale-compiler/issues/26)
 - [[CSS] rgba is not valid in text-shadow bug](https://github.com/apache/royale-compiler/issues/25)


Apache Royale Compiler 0.9.2
============================


Apache Royale Compiler 0.9.1
============================

Fixed Issues:

	#21: Different results for failed compilation between JS and SWF compilation

Updates to the RELEASE_NOTES discovered after this file was packaged into the release artifacts can be found here:

https://github.com/apache/royale-compiler/wiki/Release-Notes-0.9.1


Apache Royale Compiler 0.9.0
============================

Apache Royale Compiler 0.9.0 is the first release of a next-generation
compiler for building Apache Royale applications.

The compiler was previously released by the Apache Flex project.  You can
see RELEASE_NOTES for earlier releases in the Apache Flex releases.

Known Issues
_____________

Adobe Flash Builder Integration

The Apache Royale Compiler can be used in Adobe Flash Builder 4.7, but does
not support incremental compilation or clean non-SWF output.


Unit Test results in compiler.tests 

The jar.tests report "Java Result: 1" which is expected as all these tests do is verify that the jar is executable and has a "main" entry point which in this case reports the compiler usage help and returns 1.

Compilation Warnings

The Java compiler will report warnings for several files in this release.

Please report new issues to our bugbase at:

    https://github.com/apache/royale-compiler/issues

                                          The Apache Royale Project
                                          <http://royale.apache.org/>
