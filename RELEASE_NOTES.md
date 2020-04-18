
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
 
 For additional information on recent issues that have been closed, see [Github Issues List](https://github.com/apache/royale-compiler/issues?q=is%3Aissue+is%3Aclosed)

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
