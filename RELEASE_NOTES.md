For additional information on recent issues that have been closed, see [Github Issues List](https://github.com/apache/royale-compiler/issues?q=is%3Aissue+is%3Aclosed)

Apache Royale Compiler 0.9.9

=================

- **asformat** is a new formatter for ActionScript code
- Added a boolean new compiler option 'compiler.mxml.force-local-id' - (short commandline form 'force-local-id'). This is a quick way to avoid propagating id attributes to browser DOM in JSRoyale. 
- (JS) Fix for @royalesuppressexport feature which had stopped working
- (JS) Fix for mx.managers.SystemManager subclass not being generated for applications that were not direct subclasses of the relevant application classes.
- (JS) Improvement in output of locale properties files as ResourceBundles. Now using original Flex/SWF parsing approach for improved parity.
- (JS) First implementation of multiple-catch support in Javascript

Apache Royale Compiler 0.9.8
=================

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
=================

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
=================

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
=================
 
 - @royaledebug comment on method will make the method go away in a js release build.

Apache Royale Compiler 0.9.3
=================
 
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
=================


Apache Royale Compiler 0.9.1
=================

Fixed Issues:

	#21: Different results for failed compilation between JS and SWF compilation

Updates to the RELEASE_NOTES discovered after this file was packaged into the release artifacts can be found here:

https://github.com/apache/royale-compiler/wiki/Release-Notes-0.9.1


Apache Royale Compiler 0.9.0
=================
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
