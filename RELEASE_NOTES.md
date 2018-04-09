Apache Royale Compiler 0.9.3
=================
 
	#38 filter CSS property makes compiler crash bug
	#36 Slow Compiler Build Time enhancement
	#34 content: url fails to compile bug
	#35 Multiple properties in CSS fails in a particular case bug
	#32 selectors without initial dot are removed from final css bug
	#30 CSS element > element selector is ignored bug
	#31 selector::pseudo-element is compiled into selector:pseudo-element (removes one ":") bug
	#29 Compiler Internal Error on Empty Binding bug
	#25 [CSS] rgba is not valid in text-shadow bug
	#26 [CSS] using linear-gradient make SWF not compile bug


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
