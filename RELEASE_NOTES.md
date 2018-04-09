Apache Royale Compiler 0.9.3
=================
 
 - [CSS selectors can start with "::" and compiler adds "."](https://github.com/apache/royale-compiler/issues/40)
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
