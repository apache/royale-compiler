/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.clients;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Factory class for command-line interpreters of various compilation tasks.
 */
public class CLIFactory
{

    // TODO: Remove this hard-coded info from ASC
    private static final int MIN_VERSION = 660;
    private static final int MAX_VERSION = 670;
    
    /**
     * Hide constructor.
     */
    private CLIFactory()
    {
    }

    // Singleton of ASC options.
    private static Options ascOptions = null;

    /**
     * Create CLI options for ASC command-line.
     * 
     * @return Apache Common CLI options
     */
    @SuppressWarnings({ "static-access" })
    public static Options getOptionsForASC()
    {
        if (ascOptions != null)
            return ascOptions;

        ascOptions = new Options();

        //-------------------------
        // Options as Boolean Flags
        //-------------------------
        ascOptions.addOption("AS3", false, "use the AS3 class based object model for greater performance and better error reporting");
        ascOptions.addOption("abcfuture", false, "future abc");
        ascOptions.addOption("b", false, "show bytes");
        ascOptions.addOption("coach", false, "warn on common actionscript mistakes (deprecated)");
        ascOptions.addOption("d", false, "emit debug info into the bytecode");
        ascOptions.addOption("doc", false, "emit asdoc info");
        ascOptions.addOption("ES", false, "use the ECMAScript edition 3 prototype based object model to allow dynamic overriding of prototype properties");
        ascOptions.addOption("ES4", false, "use ECMAScript 4 dialect");
        ascOptions.addOption("f", false, "print the flow graph to standard out");
        ascOptions.addOption("h", "help", false, "print this help message");
        ascOptions.addOption("i", false, "write intermediate code to the .il file");
        ascOptions.addOption("l", false, "show line numbers");
        ascOptions.addOption("log", false, "redirect all error output to a logfile");
        ascOptions.addOption("m", false, "write the avm+ assembly code to the .il file");
        ascOptions.addOption("merge", false, "merge the compiled source into a single output file");
        ascOptions.addOption("movieclip", false, "make movieclip");
        ascOptions.addOption("md", false, "emit metadata information into the bytecode");
        ascOptions.addOption("o", "O", false, "produce an optimized abc file");
        ascOptions.addOption("optimize", false, "produce an optimized abc file");
        ascOptions.addOption("p", false, "write parse tree to the .p file");
        ascOptions.addOption("sanity", false, "system-independent error/warning output -- appropriate for sanity testing");
        ascOptions.addOption("static", false, "use static semantics");
        ascOptions.addOption("strict", "!", false, "treat undeclared variable and method access as errors");
        ascOptions.addOption("warnings", false, "warn on common actionscript mistakes");
        ascOptions.addOption("parallel", false, "turn on 'paralle generation of method bodies' feature for Alchemy");
        ascOptions.addOption("inline", false, "turn on the inlining of functions");
        ascOptions.addOption("removedeadcode", false, "remove dead code when -optimize is set");

        //-----------------------
        // Options with Arguments
        //-----------------------

        // -api <version>
        Option apiOpt = OptionBuilder.withArgName("version")
                                     .hasArg()
                                     .withDescription("compile program as a specfic version between " + MIN_VERSION + " and " + MAX_VERSION)
                                     .create("api");
        ascOptions.addOption(apiOpt);

        // -avmtarget <version>
        Option avmOpt = OptionBuilder.withArgName("vm version number")
                                     .hasArg()
                                     .withDescription("emit bytecode for a target virtual machine version, 1 is AVM1, 2 is AVM2")
                                     .create("avmtarget");
        ascOptions.addOption(avmOpt);

        // -config <ns::name=value>
        Option configOpt = OptionBuilder.withArgName("ns::name=value")
                                        .hasArgs(2)
                                        .withValueSeparator('=')
                                        .withDescription("define a configuration value in the namespace ns")
                                        .create("config");
        ascOptions.addOption(configOpt);

        // -exe <avmplus path>
        Option exeOpt = OptionBuilder.withArgName("avmplus path")
                                     .hasArg()
                                     .withDescription("emit an exe file (projector)")
                                     .create("exe");
        ascOptions.addOption(exeOpt);

        // -in <filename>
        Option includeOpt = OptionBuilder.withArgName("filename")
                                         .hasArg()
                                         .withDescription("include the specified source file")
                                         .create("in");
        ascOptions.addOption(includeOpt);

        // -import <filename>
        Option importOpt = OptionBuilder.withArgName("filename")
                                        .hasArg()
                                        .withDescription("make the packages in the abc file available for import")
                                        .create("import");
        ascOptions.addOption(importOpt);

        // -language <lang>
        Option langOpt = OptionBuilder.withArgName("lang")
                                      .hasArg()
                                      .withDescription("set the language for output strings {EN|FR|DE|IT|ES|JP|KR|CN|TW}")
                                      .create("language");
        ascOptions.addOption(langOpt);

        // --library <filename>
        ascOptions.addOption(OptionBuilder.withArgName("swc file")
                                          .hasArg()
                                          .withDescription("import a swc library")
                                          .withLongOpt("library")
                                          .create("li"));
        
        // --libraryext <filename>
        ascOptions.addOption(OptionBuilder.withArgName("swc file")
                                          .hasArg()
                                          .withDescription("import a swc as external library")
                                          .withLongOpt("libraryext")
                                          .create("le"));

        // -o2 <name=value>
        Option o2Opt = OptionBuilder.withArgName("name=value")
                                    .hasArgs()
                                    .withDescription("optimizer configuration")
                                    .withLongOpt("O2")
                                    .create("o2");
        ascOptions.addOption(o2Opt);

        // -outdir <output directory name>
        Option outdir = OptionBuilder.withArgName("output directory name")
                                    .hasArg()
                                    .withDescription("Change the directory of the output files")
                                    .create("outdir");
        ascOptions.addOption(outdir);

        // -out <out basename>
        Option out = OptionBuilder.withArgName("basename")
                                    .hasArg()
                                    .withDescription("Change the basename of the output file")
                                    .create("out");
        ascOptions.addOption(out);

        // -swf <classname,width,height,fps>
        // An Option can't have both required arguments and optional arguments 
        // so tell the Option we have just one argument and later parse the 
        // value into multiple arguments.
        Option swfOpt = OptionBuilder.withArgName("classname,width,height[,fps]")
                                     .hasArg()
                                     .withValueSeparator(',')
                                     .withDescription("emit a SWF file")
                                     .create("swf");
        ascOptions.addOption(swfOpt);

        // -use <namespace>
        Option useOpt = OptionBuilder.withArgName("namespace")
                                     .hasArg()
                                     .withDescription("automatically use a namespace when compiling this code")
                                     .create("use");
        ascOptions.addOption(useOpt);

        return ascOptions;
    }

    /**
     * Create CLI options for MXMLC command-line.
     * 
     * @return Apache Common CLI options
     */
    protected Options getOptionsForMXMLC()
    {
        return null;
    }

    /**
     * Create CLI options for COMPC command-line.
     * 
     * @return Apache Common CLI options
     */
    protected Options getOptionsForCOMPC()
    {
        return null;
    }
}
