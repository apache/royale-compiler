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

package org.apache.flex.compiler.internal.as.codegen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;

import org.apache.flex.abc.ABCConstants;
import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.compiler.clients.MXMLJSC;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.metadata.IMetaTag;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.units.SWCCompilationUnit;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.InternalCompilerProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.requests.IABCBytesRequestResult;
import org.apache.flex.swf.ISWF;
import org.apache.flex.swf.SWFFrame;
import org.apache.flex.swf.io.ISWFWriter;
import org.apache.flex.swf.io.OutputBitStream;
import org.apache.flex.swf.tags.DoABCTag;
import org.apache.flex.swf.tags.ITag;
import org.apache.flex.swf.TagType;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.JSSourceFile;

/**
 * The implementation of SWF tag, type encoding logic. The SWF file body are
 * buffered in memory using {@code IOutputBitStream}. ZLIB compression is
 * optional. If enabled, compression is on-the-fly via a filtered output stream.
 * JSWriter is modeled after SWFWriter and emits the resulting JS file collected
 * from JSEmitters. JSEmitter only collects JS from one JSCompilationUnit
 * (usually one class) while JSWriter combines all JS code into one JS file with
 * some extra code for static initializers etc. JSDriver creates and uses
 * JSWriter for writing SWFs, which are JS files in FalconJS's world. This
 * implementation is part of FalconJS. For more details on FalconJS see
 * org.apache.flex.compiler.JSDriver
 */
public class JSWriter implements ISWFWriter
{
    private ICompilerProject m_project;
    List<ICompilerProblem> m_problems;

    // SWF model
    private final ISWF swf;

    private final Collection<ICompilationUnit> compilationUnits;

    // This buffer contains the SWF data after FileLength field. 
    private final OutputBitStream outputBuffer;

    private Boolean hasStage = false;

    /**
     * Create a SWF writer.
     * 
     * @param swf the SWF model to be encoded
     * @param useCompression use ZLIB compression if true
     */
    public JSWriter(ICompilerProject project, List<ICompilerProblem> problems, ISWF swf, boolean useCompression, boolean enableDebug)
    {
        this.m_project = project;
        this.m_problems = problems;
        this.swf = swf;
        this.compilationUnits = null;
        this.outputBuffer = new OutputBitStream(useCompression);
    }

    /**
     * Create a SWF writer.
     * 
     * @param abc the ABC bytes to be encoded
     * @param useCompression use ZLIB compression if true
     */
    public JSWriter(ICompilerProject project, List<ICompilerProblem> problems, Collection<ICompilationUnit> compilationUnits, boolean useCompression)
    {
        this.m_project = project;
        this.m_problems = problems;
        this.swf = null;
        this.compilationUnits = compilationUnits;
        this.outputBuffer = new OutputBitStream(useCompression);
    }

    private Boolean isStageCompilationUnit(ICompilationUnit cu)
    {
        if (cu != null)
        {
            final String name = cu.getName();
            if (name.endsWith("(flash.display:Stage)"))
                return true;
        }
        return false;
    }

    public String getTopLevelClass()
    {
        if (swf != null)
            return swf.getTopLevelClass();
        return null;
    }

    private void writeCode(List<byte[]> abcByteList, Boolean createSharedLib) throws InterruptedException
    {
        // start with the time stamp
        // write time stamp to outputBuffer
        if (JSSharedData.OUTPUT_TIMESTAMPS)
            writeString("/** @preserve " + JSGeneratingReducer.getTimeStampString() + " */\n");

        if (!JSSharedData.OUTPUT_ISOLATED)
        {
            // open function closure: function() { ... } 
            writePrologue(createSharedLib);

            // write out the framework root first.
            writeFrameworkRoot(createSharedLib);

            // write out DOM extensions.
            writeDomExtensions();

            // write out copyright notes
            writeCopyrightNotes();

            // write out package names
            writePackageNames(createSharedLib);

            // writeExternNames();

            // write out namespace names
            writeNamepaceNames();

        }

        // Current frame index. Updated in writeFrames().
        for (byte[] abcBytes : abcByteList)
        {
            /*
             * Extract abc from frame, see JSCompilationUnit::addToFrame():
             * doABC.setAbcData(abc.getABCBytes()); doABC.setName("as3");
             * frame.addTag(doABC);
             */
            {
                // see SWFWriter::writeTag
                {
                    // see SWFWriter::writeDoABC

                    Boolean isBinary = false;
                    /*
                     * see avm2overview.pdf, chapter 4.2 abcFile { u16
                     * minor_version u16 major_version cpool_info constant_pool
                     * u30 method_count method_info method[method_count] u30
                     * metadata_count metadata_info metadata[metadata_count] u30
                     * class_count instance_info instance[class_count]
                     * class_info class[class_count] u30 script_count
                     * script_info script[script_count] u30 method_body_count
                     * method_body_info method_body[method_body_count] }
                     */
                    if (abcBytes.length > 2)
                    {
                        // 16 00 46 00
                        final int minorVersion = 16;
                        final int majorVersion = 46;
                        if (abcBytes[0] == minorVersion &&
                            abcBytes[2] == majorVersion)
                        {
                            isBinary = true;

                            // throw new Error( "Embedded data is not supported yet." );

                            /*
                             * final AbcDump abcDump = new AbcDump();
                             * abcDump.out = JSSharedData.STDOUT; ABCParser
                             * parser = new ABCParser(abcBytes);
                             * parser.parseAbc(abcDump);
                             */
                            /*
                             * try { FileOutputStream out = new
                             * FileOutputStream("/tmp/falcon-js.abc"); try {
                             * out.write(abcBytes); out.close(); } catch(
                             * IOException e ) { } } catch(
                             * FileNotFoundException e ) { }
                             */
                        }
                    }

                    if (!isBinary)
                        outputBuffer.write(abcBytes);
                }
            }
        }

        if (createSharedLib || !JSSharedData.OUTPUT_ISOLATED)
        {
            // write out adobe.finalize();
            writeFinalize(createSharedLib);

            // close closure: function() { ... } 
            writeEpilogue(createSharedLib);
        }
    }

    /*
     * private Boolean hasEncryptedJS( ICompilationUnit cu ) throws
     * InterruptedException { final JSSharedData sharedData =
     * JSSharedData.instance; final List<IDefinition> defs =
     * MXMLJSC.getDefinitions(cu, false); for( IDefinition def: defs ) { final
     * String fullName = def.getQualifiedName(); if(
     * sharedData.hasJavaScript(fullName) && sharedData.hasEncryptedJS(fullName)
     * ) return true; } return false; }
     */

    private List<byte[]> getAbcData(ISWF swf, Boolean createSharedLib) throws InterruptedException
    {
        final JSSharedData sharedData = JSSharedData.instance;
        final List<byte[]> abcByteList = new ArrayList<byte[]>();
        if (swf == null)
        {
            for (ICompilationUnit cu : compilationUnits)
            {
                // only add topLevelClass to Stage if the Stage gets emitted.
                if (!hasStage)
                    hasStage = isStageCompilationUnit(cu);

                final Boolean isSWC = cu instanceof SWCCompilationUnit;
                if (createSharedLib == isSWC)
                {
                    final List<IDefinition> defs = MXMLJSC.getDefinitions(cu, false);
                    for (IDefinition def : defs)
                    {
                        final String fullName = def.getQualifiedName();
                        if (sharedData.hasJavaScript(fullName))
                        {
                            final String jsCode = sharedData.getJavaScript(fullName);
                            abcByteList.add(jsCode.getBytes());
                        }
                    }

                    final IABCBytesRequestResult codegenResult = cu.getABCBytesRequest().get();
                    final byte[] abcBytes = codegenResult.getABCBytes();
                    abcByteList.add(abcBytes);
                }
            }
            return abcByteList;
        }

        // Current frame index. Updated in writeFrames().
        for (int currentFrameIndex = 0; currentFrameIndex < swf.getFrameCount(); currentFrameIndex++)
        {
            final SWFFrame frame = swf.getFrameAt(currentFrameIndex);

            // If the SWF has a top level class name, the first frame must contain a SymbolClass tag.
            if (0 == currentFrameIndex && null != getTopLevelClass())
            {
                SWFFrame.forceSymbolClassTag(frame);
            }

            /*
             * Extract abc from frame, see JSCompilationUnit::addToFrame():
             * doABC.setAbcData(abc.getABCBytes()); doABC.setName("as3");
             * frame.addTag(doABC);
             */
            for (final ITag tag : frame)
            {
                // see SWFWriter::writeTag
                if (tag.getTagType() == TagType.DoABC)
                {
                    // see SWFWriter::writeDoABC
                    final DoABCTag abcTag = ((DoABCTag)tag);
                    byte[] abcBytes = abcTag.getABCData();

                    Boolean isBinary = false;
                    /*
                     * see avm2overview.pdf, chapter 4.2 abcFile { u16
                     * minor_version u16 major_version cpool_info constant_pool
                     * u30 method_count method_info method[method_count] u30
                     * metadata_count metadata_info metadata[metadata_count] u30
                     * class_count instance_info instance[class_count]
                     * class_info class[class_count] u30 script_count
                     * script_info script[script_count] u30 method_body_count
                     * method_body_info method_body[method_body_count] }
                     */
                    if (abcBytes.length > 2)
                    {
                        // 16 00 46 00
                        final int minorVersion = 16;
                        final int majorVersion = 46;
                        if (abcBytes[0] == minorVersion &&
                            abcBytes[2] == majorVersion)
                        {
                            isBinary = true;

                            // throw new Error( "Embedded data is not supported yet." );

                            if (abcTag.getName().startsWith("#"))
                                sharedData.verboseMessage("ABC: " + abcTag.getName());

                            /*
                             * final AbcDump abcDump = new AbcDump();
                             * abcDump.out = JSSharedData.STDOUT; ABCParser
                             * parser = new ABCParser(abcBytes);
                             * parser.parseAbc(abcDump);
                             */
                            /*
                             * try { FileOutputStream out = new
                             * FileOutputStream("/tmp/falcon-js.abc"); try {
                             * out.write(abcBytes); out.close(); } catch(
                             * IOException e ) { } } catch(
                             * FileNotFoundException e ) { }
                             */
                        }
                    }

                    if (!isBinary)
                    {
                        final ICompilationUnit cu = sharedData.getCompilationUnit(frame);
                        final Boolean isSWC = cu instanceof SWCCompilationUnit;

                        // only add topLevelClass to Stage if the Stage gets emitted.
                        if (!hasStage)
                            hasStage = isStageCompilationUnit(cu);

                        if (createSharedLib == isSWC)
                        {
                            if (cu != null && sharedData.isVerbose())
                            {
                                String str = cu.toString() + ": ";
                                if (cu instanceof SWCCompilationUnit)
                                {
                                    Boolean comma = false;
                                    final List<IDefinition> defs = MXMLJSC.getDefinitions(cu, true);
                                    for (IDefinition def : defs)
                                    {
                                        if (comma)
                                            str += ", ";
                                        else
                                            comma = true;
                                        str += JSGeneratingReducer.createFullNameFromDefinition(cu.getProject(), def);
                                    }
                                }
                                str += " " + abcBytes.length + " bytes.";
                                sharedData.verboseMessage(str);
                            }

                            abcByteList.add(abcBytes);
                        }
                    }
                }
            }
        }

        return abcByteList;
    }

    public void writeTo(OutputStream output)
    {
        writeTo(output, false);
    }

    public void writeLibTo(OutputStream output)
    {
        writeTo(output, true);
    }

    private void writeTo(OutputStream output, Boolean createSharedLib)
    {
        assert output != null;

        try
        {
            outputBuffer.reset();

            List<byte[]> abcByteList = getAbcData(swf, createSharedLib);
            if (!abcByteList.isEmpty())
            {
                writeCode(abcByteList, createSharedLib);

                // if we are pulling in encrypted JS through a SWC that JS should probably 
                // go into the shared lib and obfuscated with ADVANCED_OPTIMIZATIONS. Otherwise the whole point
                // of encrypting JS would be defeated.
                // At this point protecting intellectual property of JS and -js-generate-shared-library 
                // have a lower priority than other features.
                // DISABLED for now:
                // if( JSSharedData.OPTIMIZE || (createSharedLib && JSSharedData.instance.hasAnyEncryptedJS()) )
                // 		optimize( outputBuffer, createSharedLib );

                // support for optimize=true
                // if (JSSharedData.OPTIMIZE)
                    // optimize(outputBuffer, createSharedLib);

                output.write(outputBuffer.getBytes(), 0, outputBuffer.size());
                output.flush();
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the internal output buffer that stores the encoded SWF tags and
     * part of the SWF header.
     */
    public void close() throws IOException
    {
        outputBuffer.close();
    }

    private void writeString(String s)
    {
        outputBuffer.write(s.getBytes());
    }

    protected void writeCopyrightNotes()
    {
        if (!JSSharedData.m_useClosureLib)
        {
            // Source: http://ejohn.org/blog/simple-javascript-inheritance/
            final String jresigNote =
                    "/* Simple JavaScript Inheritance\n" +
                            " * By John Resig http://ejohn.org/\n" +
                            " * MIT Licensed.\n" +
                            " *\n" +
                            " * Adobe Systems Inc. 2010.  Modifications:\n" +
                            " * - added support for getter and setter.\n" +
                            " * - all objects become jQuery objects if jQuery is present.\n" +
                            " * - support for calling constructor functions directly, i.e. var s = Sprite();\n" +
                            " * - ported to ActionScript so this code can be cross-compiled.\n" +
                            " */\n" +
                            "// Inspired by base2 and Prototype";

            // write out the copyright note for John Resig's simple class inheritance in JS:
            // http://ejohn.org/blog/simple-javascript-inheritance/
            writeString(jresigNote + "\n\n");
        }
    }

    protected void writePackageNames(Boolean createSharedLib)
    {
        writeString("\n");
        writeString("/**\n");
        writeString(" * Packages \n");
        writeString(" */\n");

        final Set<String> visited = new HashSet<String>();
        final Set<String> packages = JSSharedData.instance.getPackages();
        for (String name : packages)
        {
            String packageName = JSSharedData.ROOT_NAME;
            String[] parts = name.split("\\.");
            for (String part : parts)
            {
                if (!part.isEmpty())
                {
                    if (!packageName.isEmpty())
                        packageName += ".";

                    packageName += part;
                    if (!visited.contains(packageName))
                    {
                        visited.add(packageName);

                        writeString("/** @typedef { Object } */\n");
                        if (!packageName.contains("."))
                            writeString("var ");

                        writeString(packageName + " = { _FULLNAME: \"" + packageName + "\"};\n");
                    }
                }
            }
        }
        writeString("\n");
    }

    /*
     * protected void writeExternNames() { final Set<String> externs =
     * JSSharedData.instance.getUsedExterns(); if( !externs.isEmpty() ) {
     * writeString( "\n" ); writeString( "/ **\n" ); writeString(
     * " * Externs \n" ); writeString( " * /\n" ); for( String defName: externs
     * ) { if( JSSharedData.instance.isReferencedDefinition(defName) ) { final
     * IDefinition def = JSSharedData.instance.getDefinition(defName); String
     * name = null; final IMetaTag extern =
     * def.getMetaTagByName(JSSharedData.EXTERN_METATAG); if( extern != null )
     * name = extern.getAttributeValue("name"); if( name == null ) name =
     * def.getShortName(); // TODO: get rid of the hard-coded checks. String
     * fullName = def.getPackageName(); if( !fullName.isEmpty() ) fullName +=
     * "."; fullName += def.getName(); if( !fullName.equals(name) ) writeString(
     * fullName + " = " + name + ";\n" ); } } writeString( "\n" ); } }
     */

    protected void writeNamepaceNames()
    {
        final Map<String, INamespaceDefinition> namespaces = JSSharedData.instance.getNamespaces();

        if (!namespaces.isEmpty())
        {
            writeString("\n");
            writeString("/**\n");
            writeString(" * Namespace identifiers \n");
            writeString(" */\n");

            // see JSGeneratingReducer.reduce_namespaceDeclarationConstantInitializer() for where these get registered

            for (Map.Entry<String, INamespaceDefinition> entry : namespaces.entrySet())
            {
                String uri = entry.getKey();
                INamespaceDefinition nsDef = entry.getValue();

                // TODO: should there be a package qualifier prefix?
                String name = nsDef.getBaseName();

                // TODO: value should really be an object of type Namespace, not a String; see FJS-65
                writeString("var " + name + " = \"" + uri + "\";\n");
            }
            writeString("\n");
        }

    }

    protected void writeFrameworkRoot(Boolean createSharedLib)
    {
        writeString("// Declaring missing types\n");
        writeString("var IntClass = function(){};\n");
        writeString("var UIntClass = function(){};\n");
        writeString("\n");

        // write out framework root object
        if (JSSharedData.m_useClosureLib)
        {
            writeString("/**\n");
            writeString(" * Framework: " + JSSharedData.JS_FRAMEWORK_NAME + "\n");
            writeString(" */\n");
            // writeString( "goog.provide(\"" + JSSharedData.JS_FRAMEWORK_NAME + "\");\n\n" ); 

            if (createSharedLib)
                writeString("var " + JSSharedData.JS_FRAMEWORK_NAME + " = __adobe || {};\n\n");
            else
                writeString("var " + JSSharedData.JS_FRAMEWORK_NAME + " = {};\n\n");

            if (!JSSharedData.ROOT_NAME.isEmpty())
            {
                String rootName = JSSharedData.ROOT_NAME;
                // remove trailing dot
                if (rootName.endsWith("."))
                    rootName = rootName.substring(0, rootName.length() - 1);

                writeString("/**\n");
                writeString(" * Root: " + rootName + "\n");
                writeString(" */\n");
                writeString("goog.provide(\"" + rootName + "\");\n");
            }
        }
        else
        {
            writeString("/**\n");
            writeString(" * Framework: " + JSSharedData.JS_FRAMEWORK_NAME + "\n");
            writeString(" * @type {object}\n");
            writeString(" */\n");
            writeString("var " + JSSharedData.JS_FRAMEWORK_NAME + " = {};\n");

        }

        writeString(JSSharedData.JS_SYMBOLS + "= {};\n");

        writeString("\n");
    }

    protected void writeDomExtensions()
    {
        if (JSSharedData.EXTEND_DOM)
        {
            final String indent = "\t";

            // see also:
            // http://hg.mozilla.org/tamarin-redux/file/758198237dc1/test/acceptance/ecma3
            // http://qfox.nl/ecma/275
            // http://code.google.com/p/sputniktests/
            // http://es5.github.com/

            writeString("\n");

            writeString("// Additions to DOM classes necessary for passin Tamarin's acceptance tests.\n");
            writeString("// Please use -js-extend-dom=false if you want to prevent DOM core objects changes.\n");
            writeString("if( typeof(__global[\"Math\"].NaN) != \"number\" )\n");
            writeString("{\n");

            writeString(indent + "/**\n");
            writeString(indent + " * @const\n");
            writeString(indent + " * @type {function()}\n");
            writeString(indent + " */\n");
            writeString(indent + "var fnToString = function() { return \"function Function() {}\"; };\n");

            writeString(indent + "/** @type {object} */\n");
            writeString(indent + "var proto = {};\n");

            // see e15_8_1.as
            writeString("\n");
            writeString(indent + "// Additions to Math\n");
            writeString(indent + "__global[\"Math\"].NaN = NaN;\n");

            // see e15_8_1.as, ecma3/Array/e15_4_2_1_3.
            writeString("\n");
            writeString(indent + "// Additions to Array (see e15_4_1_1, e15_4_2_1_1, e15_4_2_1_3, e15_4_2_2_1)\n");
            writeString(indent + "proto = __global[\"Array\"].prototype;\n");
            writeString(indent + "proto.toString.toString = fnToString;\n");
            writeString(indent + "proto.join.toString = fnToString;\n");
            writeString(indent + "proto.sort.toString = fnToString;\n");
            writeString(indent + "proto.reverse.toString = fnToString;\n");

            // In browsers, Array.prototype.unshift.length = 1, because
            // the this value is coerced to an object.

            // We need to hide unshift after setting it.
            // Otherwise we'll get "unshift" for:
            // var arr = new Array(); for( n in arr ) { console.info(n); }
            writeString(indent + "if( Object.defineProperty )\n");
            writeString(indent + "{\n");
            writeString(indent + indent + "proto.unshift = (function ()\n");
            writeString(indent + indent + "{\n");
            writeString(indent + indent + indent + "/** @type {function()} */\n");
            writeString(indent + indent + indent + "var f = proto.unshift;\n");
            writeString(indent + indent + indent + "return function() { return f.apply(this,arguments); };\n");
            writeString(indent + indent + "})();\n");
            writeString(indent + indent + "Object.defineProperty( proto, \"unshift\", {enumerable: false} );\n");
            writeString(indent + "}\n");

            // see ecma3/ErrorObject/e15_11_2_1.as
            writeString("\n");
            writeString(indent + "// Additions to Error (see e15_11_2_1)\n");
            writeString(indent + "proto = __global[\"Error\"].prototype;\n");
            writeString(indent + "proto.getStackTrace = proto.getStackTrace || function() { return null; };\n");
            writeString(indent + "proto.toString = (function ()\n");
            writeString(indent + "{\n");
            writeString(indent + indent + "/** @type {function()} */\n");
            writeString(indent + indent + "var f = proto.toString;\n");
            writeString(indent + indent + "return function () { return (this.name == this.message || this.message == \"\") ? this.name : f.call(this); };\n");
            writeString(indent + "})();\n");

            // see ecma3/ErrorObject/e15_11_2_1.as
            writeString("\n");
            writeString(indent + "proto = __global[\"Object\"].prototype;\n");
            writeString(indent + "// Additions to Object (see e15_11_2_1)\n");
            writeString(indent + "proto.toString = (function ()\n");
            writeString(indent + "{\n");
            writeString(indent + indent + "/** @type {function()} */\n");
            writeString(indent + indent + "var f = proto.toString;\n");
            writeString(indent + indent + "return function () { return (this instanceof Error) ? (\"[object \" + this.name + \"]\") : f.call(this); };\n");
            writeString(indent + "})();\n");

            writeString("}\n");
        }

    }

    private void emitSettings()
    {
        final String topLevelClass = getTopLevelClass();
        if (topLevelClass != null)
        {
            // support for no export 
            if (!JSSharedData.NO_EXPORTS)
            {
                // We are now using createMain() isntead of m_topLevelClass
                // writeString( "\tadobe.m_topLevelClass = " + JSSharedData.ROOT_NAME + getTopLevelClass() + ";\n" );
                writeString("\tadobe.m_topLevelClass = null;\n");
                writeString("\tgoog.exportSymbol(\"" + JSSharedData.JS_FRAMEWORK_NAME + ".m_topLevelClass\", " + JSSharedData.JS_FRAMEWORK_NAME + ".m_topLevelClass);\n");
            }

            // emit createMain
            String create = "new " + JSSharedData.ROOT_NAME + topLevelClass + "()";
            final String symbolName = JSSharedData.instance.getSymbol(topLevelClass);
            if (symbolName != null && !symbolName.isEmpty())
                create = symbolName + "." + JSSharedData.SYMBOL_INSTANCE + "." + JSSharedData.SYMBOL_INIT + "(" + create + ")";
            writeString("\tadobe.createMain = function() { return " + create + "; };\n");

        }

        // shell.as is using flash.system.Capabilities.playerType, which must be set to "AVMPlus"
        // var playerType /* : String */ = flash.system.Capabilities.playerType;
        // flash.system.Capabilities.__static_init() must be called before we set playerType, see http://watsonexp.corp.adobe.com/#bug=3021707
        if (JSSharedData.GENERATE_TEST_CASE)
        {
            writeString("\n");
            writeString("\t// Setting playerType to AVMPlus for running Tamarin's acceptance tests.\n");
            writeString("\tif( flash && flash.system && flash.system.Capabilities )\n");
            writeString("\t{\n");
            writeString("\t\tflash.system.Capabilities.__static_init();\n");
            writeString("\t\tflash.system.Capabilities.playerType = \"AVMPlus\";\n");
            writeString("\t}\n");
        }
    }

    /*
     * private void emitClassInfo() { writeString( "\n" ); writeString(
     * "\t// Class Information \n" ); writeString( "\t" +
     * JSSharedData.JS_FRAMEWORK_NAME + ".info = {};\n" ); Map<String,String>map
     * = JSSharedData.instance.getClassInfo(); // sorted output final
     * TreeSet<String> keys = new TreeSet<String>(map.keySet()); for(String key
     * : keys) { final String value = map.get(key); writeString( "\t" +
     * JSSharedData.JS_FRAMEWORK_NAME + ".info[\"" + key + "\"] = \"" + value +
     * "\";\n" ); } }
     */
    /*
     * private void emitExports() { writeString( "\n" ); writeString(
     * "\t// Exported Symbols\n" ); writeString( "\t" +
     * JSSharedData.JS_FRAMEWORK_NAME + ".root = {};\n" ); final Set<String>
     * visited = new HashSet<String>(); final Set<Name> classes =
     * JSSharedData.instance.getClasses(); List<String> sorted = new
     * ArrayList<String>(); for(Name name : classes ) { if( name != null &&
     * JSSharedData.instance.hasClassBeenEmitted(name) ) { String fullName = "";
     * final String packageName =
     * JSGeneratingReducer.getPackageNameFromName(name, false); if( packageName
     * != null && !packageName.isEmpty() ) fullName += packageName + ".";
     * fullName += name.getBaseName(); String[] parts = fullName.split("\\.");
     * fullName = JSSharedData.ROOT_NAME; for( String part: parts ) { if(
     * !part.isEmpty() ) { if( !fullName.isEmpty() ) fullName += "."; fullName
     * += part; if(!visited.contains(fullName)) { visited.add(fullName); //
     * collect symbols for later sorted.add("\tgoog.exportSymbol(\"" + fullName
     * + "\", " + fullName + ", " + JSSharedData.JS_FRAMEWORK_NAME +
     * ".root);\n"); } } } } } java.util.Collections.sort(sorted); for( String
     * s: sorted ) { writeString( s ); } writeString( "\n" ); }
     */

    private void emitInterfacesInfo()
    {
        writeString("\n");
        writeString("\t// Interfaces Information \n");
        writeString("\t" + JSSharedData.JS_FRAMEWORK_NAME + ".interfaces = {};\n");

        Map<Name, Set<Name>> map = JSSharedData.instance.getAllInterfaces();
        Iterator<Map.Entry<Name, Set<Name>>> it = map.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<Name, Set<Name>> pair = it.next();
            final IDefinition def = JSSharedData.instance.getDefinition(pair.getKey());
            final String className = def.getQualifiedName();
            writeString("\t" + JSSharedData.JS_FRAMEWORK_NAME + ".interfaces[\"" + className + "\"] = ");
            writeString("[");

            Boolean emitComma = false;
            Set<Name> interfaces = pair.getValue();
            for (Name name : interfaces)
            {
                if (emitComma)
                    writeString(", ");
                else
                    emitComma = true;
                // writeString( JSGeneratingReducer.getFullNameFromName(name, true) );

                String fullName = "";
                if (name.getQualifiers().length() > 1)
                {
                    if (!JSSharedData.ROOT_NAME.isEmpty())
                        fullName += JSSharedData.ROOT_NAME;

                    // Picking a package qualifier at random (if there's more than one). We skip other things like
                    // namespace qualifiers since their names might be URLs (not syntactically valid).
                    Namespace packageNS = findAnyPackageQualifier(name);
                    assert packageNS != null : "Unable to find a usable qualifier for " + name;

                    fullName += packageNS.getName();
                    fullName += "." + name.getBaseName();
                }
                else
                {
                    final IDefinition idef = JSSharedData.instance.getDefinition(name);
                    fullName = idef.getQualifiedName();
                }
                writeString(fullName);
            }

            writeString("];\n");
        }
    }

    private static Namespace findAnyPackageQualifier(Name name)
    {
        for (Namespace ns : name.getQualifiers())
        {
            if (ns.getKind() == ABCConstants.CONSTANT_PackageNs || ns.getKind() == ABCConstants.CONSTANT_PackageInternalNs)
                return ns;
        }
        return null;
    }

    /*
     * private void emitStaticInits() { writeString( "\n" ); writeString(
     * "\t// Static inits \n" ); // writeString(
     * JSSharedData.instance.getStaticInits("\t", "\n") ); // auto-register
     * static inits. writeString( "\t" + JSSharedData.JS_RUN_STATIC_INITS + "("
     * + JSSharedData.JS_STATIC_INITS + ");\n" ); writeString( "\tdelete(" +
     * JSSharedData.JS_STATIC_INITS + ");\n" ); }
     */

    private void emitScriptInfos()
    {
        final Set<String> scriptInfos = JSSharedData.instance.getScriptInfos();
        if (!scriptInfos.isEmpty())
        {
            writeString("\n");
            for (String si : scriptInfos)
            {
                writeString(si);
            }
        }
    }

    private void emitSymbolInstances()
    {
        if (JSSharedData.instance.hasSymbols())
        {
            writeString("\n");
            writeString("\n");
            writeString("\t// Symbols \n");

            final List<String> list = JSSharedData.instance.getSymbols();
            for (String symbol : list)
            {
                writeString("\t" + JSSharedData.ROOT_NAME + symbol + "." + JSSharedData.SYMBOL_INSTANCE + " = new " + JSSharedData.ROOT_NAME + symbol + "();\n");
            }
        }
    }

    // support for generating test cases for JsTestDriver.
    // see http://code.google.com/p/js-test-driver/wiki/TestCase
    /*
     * private void emitTestCase(String topLevelClass) { if(
     * JSSharedData.GENERATE_TEST_CASE && topLevelClass != null ) { // support
     * for [TestCase(name="...")] String testCaseName = topLevelClass;
     * ASProjectScope projectScope = (ASProjectScope)m_project.getScope();
     * IDefinition rootClassDef =
     * projectScope.findDefinitionByName(topLevelClass); if (rootClassDef !=
     * null) { final IMetaTag testCaseTag =
     * rootClassDef.getMetaTagByName(JSSharedData.TESTCASE_METATAG );
     * if(testCaseTag!= null) { final String testCaseAttr =
     * testCaseTag.getAttributeValue("name"); if( testCaseAttr != null &&
     * !testCaseAttr.isEmpty() ) testCaseName = testCaseAttr; } } writeString(
     * "\n" ); writeString( "\t// Create and export " +
     * JSSharedData.JS_FRAMEWORK_NAME + ".mainTestCase\n" ); writeString(
     * "\tif(typeof(TestCase) != \"undefined\")\n" ); writeString( "\t{\n" );
     * writeString( "\t\tadobe.mainTestCase = TestCase(\"" + testCaseName +
     * "\");\n" ); writeString(
     * "\t\tadobe.mainTestCase.prototype.testMain = function()\n" );
     * writeString( "\t\t{\n" ); if( JSSharedData.MAIN != null ) { writeString(
     * "\t\t\tconsole.info(\"Running " + JSSharedData.MAIN + "() ...\")\n" );
     * writeString( "\t\t\t" + JSSharedData.MAIN + "();\n" ); } else {
     * writeString( "\t\t\tconsole.info(\"Running " + testCaseName + " ...\")\n"
     * ); writeString( "\t\t\tnew " + topLevelClass + "();\n" ); } writeString(
     * "\t\t}\n" ); writeString( "\t\tgoog.exportSymbol( \"" +
     * JSSharedData.JS_FRAMEWORK_NAME + ".mainTestCase\", " +
     * JSSharedData.JS_FRAMEWORK_NAME + ".mainTestCase );\n" ); writeString(
     * "\t\tgoog.exportSymbol( \"" + JSSharedData.JS_FRAMEWORK_NAME +
     * ".mainTestCase.prototype.testMain\", " + JSSharedData.JS_FRAMEWORK_NAME +
     * ".mainTestCase.prototype.testMain );\n" ); writeString( "\t}\n" ); } }
     */
    protected void writePrologue(Boolean createSharedLib)
    {
        if (createSharedLib)
        {
            writeString("/**\n");
            writeString(" * @const\n");
            writeString(" * @type {function()}\n");
            writeString(" */\n");
            writeString("var __libInit = function(__global, __adobe) {\n");
        }
        else if (JSSharedData.GENERATE_TEST_CASE)
        {
            // ensure there is always a TestCase.
            String testCaseName;
            String topLevelClass = getTopLevelClass();
            if (topLevelClass == null)
            {
                final Date now = new Date();
                testCaseName = topLevelClass = "Test_" + now.getTime();
            }
            else
            {
                testCaseName = topLevelClass;
                final ASProjectScope projectScope = (ASProjectScope)m_project.getScope();
                final IDefinition rootClassDef = projectScope.findDefinitionByName(topLevelClass);
                if (rootClassDef != null)
                {
                    final IMetaTag testCaseTag = rootClassDef.getMetaTagByName(JSSharedData.TESTCASE_METATAG);
                    if (testCaseTag != null)
                    {
                        final String testCaseAttr = testCaseTag.getAttributeValue("name");
                        if (testCaseAttr != null && !testCaseAttr.isEmpty())
                            testCaseName = testCaseAttr;
                    }
                }
            }

            // TODO: emit type annotations for Google's closure compiler.
            writeString("var __global = this;\n");

            writeString("/**\n");
            writeString(" * @const\n");
            writeString(" * @typedef {string}\n");
            writeString(" */\n");
            writeString("var testCaseName = \"" + testCaseName + "\";\n");
            writeString("\n");

            writeString("/**\n");
            writeString(" * @const\n");
            writeString(" * @typedef {boolean}\n");
            writeString(" */\n");
            writeString("var hasTestCase = typeof(TestCase) != \"undefined\";\n");
            writeString("\n");

            writeString("/**\n");
            writeString(" * @const\n");
            writeString(" * @type {array}\n");
            writeString(" */\n");
            writeString("var __testCases = [];\n");

            writeString("/**\n");
            writeString(" * @const\n");
            writeString(" * @type {function()}\n");
            writeString(" */\n");
            writeString("var __runTestCases = function()\n");
            writeString("{\n");
            writeString("\tif(hasTestCase)\n");
            writeString("\t\treturn;\n\n");
            writeString("\twhile( __testCases.length > 0 )\n");
            writeString("\t{\n");
            writeString("\t\tvar tc = __testCases.shift();\n");
            writeString("\t\tfor( var method in tc )\n");
            writeString("\t\t{\n");
            writeString("\t\t\tif( method.substring(0,4) == \"test\" )\n");
            writeString("\t\t\t\ttc[method]();\n");
            writeString("\t\t}\n");
            writeString("\t}\n");
            writeString("}\n");

            writeString("/**\n");
            writeString(" * @const\n");
            writeString(" * @type {function(...)}\n");
            writeString(" */\n");
            writeString("var TestCase = hasTestCase ? TestCase : (function(name,testcase)\n");
            writeString("\t{\n");
            writeString("\t\tvar tc = testcase;\n");
            writeString("\t\tif( tc )\n");
            writeString("\t\t{\n");
            writeString("\t\t\t__testCases.push(tc);\n");
            writeString("\t\t}\n");
            writeString("\t\telse\n");
            writeString("\t\t{\n");
            writeString("\t\t\ttc = function(){};\n");
            writeString("\t\t\t __testCases.push(new tc());\n");
            writeString("\t\t}\n");
            writeString("\t\treturn tc;\n");
            writeString("\t});\n");

            writeString("\n");
            writeString("/**\n");
            writeString(" * @const\n");
            writeString(" * @type {function(...)}\n");
            writeString(" */\n");
            writeString("var AsyncTestCase = hasTestCase ? AsyncTestCase : TestCase;\n");

            writeString("\n");
            writeString("/**\n");
            writeString(" * @const\n");
            writeString(" * @type {function(...)}\n");
            writeString(" */\n");
            writeString("var MainTestCase = TestCase(testCaseName);\n");
            writeString("MainTestCase.prototype.testMain = function() {\n");
            writeString("console.info(\"START TEST: \" + testCaseName );\n");
            writeString("(function(__global) {\n");
        }
        else
        {
            writeString("(function(__global) {\n");
        }
    }

    protected void writeEpilogue(Boolean createSharedLib)
    {
        if (createSharedLib)
        {
            writeString("}; // __libInit() \n\n");
            writeString("goog.exportSymbol( \"__libInit\", __libInit);\n");
        }
        else if (JSSharedData.GENERATE_TEST_CASE)
        {
            writeString("})(__global);\n");
            writeString("console.info(\"END TEST: \" + testCaseName );\n");
            writeString("} // MainTestCase.prototype.testMain \n\n");

            writeString("goog.exportSymbol( \"MainTestCase\", MainTestCase);\n");
            writeString("goog.exportSymbol( \"MainTestCase.prototype.testMain\", MainTestCase.prototype.testMain);\n");

            writeString("__runTestCases();\n");
        }
        else
        {
            writeString("})(this);\n");
        }
    }

    protected void writeFinalize(Boolean createSharedLib)
    {
        writeString("\n");

        // ASProjectScope projectScope = applicationProject.getScope();
        // IDefinition rootClassDef = projectScope.findDefinitionByName(getTopLevelClass());

        // export toplevel class

        // support for no export 
        final String topLevelClass = getTopLevelClass();
        if (topLevelClass != null && !JSSharedData.NO_EXPORTS)
        {
            writeString("// Top Level Class\n");
            writeString("goog.exportSymbol(\"" + JSSharedData.ROOT_NAME + topLevelClass + "\", "
                                                + JSSharedData.ROOT_NAME + topLevelClass + ");\n");
            writeString("// Export " + JSSharedData.JS_FRAMEWORK_NAME + ".createStage\n");
            writeString("goog.exportSymbol( \"" + JSSharedData.JS_FRAMEWORK_NAME + ".createStage\", " + JSSharedData.JS_FRAMEWORK_NAME + ".createStage );\n\n");
        }

        final String finalize = "staticInits";

        // generated finalize() function 
        writeString("/** \n");
        writeString(" * Generated finalizer.\n");
        writeString(" * @param {DOMWindow} global Global object\n");
        writeString(" * @param {" + JSSharedData.ROOT_NAME + "browser.IFramework} framework Framework to use\n");
        writeString(" */\n");
        writeString(JSSharedData.JS_FRAMEWORK_NAME + "." + finalize + " = function(global,framework)\n");
        writeString("{\n");
        writeString("\tadobe.globals = global;\n");
        writeString("\tadobe.m_framework = framework;\n");

        writeString("\tadobe.uintClass = UIntClass;\n");
        writeString("\tadobe.intClass = IntClass;\n");

        emitSettings();
        // emitExports();
        // emitClassInfo();
        emitInterfacesInfo();
        // emitStaticInits();
        emitSymbolInstances();
        // emitTestCase(topLevelClass);
        writeString("}\n\n");

        // select framework 
        writeString("/** \n");
        writeString(" * Selected framework, i.e. JQueryFramework, or GoogleFramework\n");
        writeString(" * @typedef {" + JSSharedData.ROOT_NAME + "browser.IFramework}\n");
        writeString(" */\n");
        writeString("var framework = new " + JSSharedData.ROOT_NAME + JSSharedData.FRAMEWORK_CLASS + "();\n\n");

        // call finalize() 
        writeString("// Call static initialization chain\n");
        writeString(JSSharedData.JS_FRAMEWORK_NAME + "." + finalize + "(goog.global,framework);\n\n");

        emitScriptInfos();

        // support for no export
        if (!createSharedLib)
        {
            if (JSSharedData.MAIN != null)
            {
                writeString("// Main entry point: " + JSSharedData.MAIN + "\n");
                writeString(JSSharedData.MAIN + "();\n\n");
            }
            else if (topLevelClass != null)
            {
                if (hasStage)
                {
                    writeString("// Main entry point: wrapper for adding " + topLevelClass + " to the stage.\n");
                    writeString("function main()\n");
                    writeString("{\n");
                    writeString("\t/** @type {flash.display.Stage} */\n");
                    writeString("\tvar stage /* : Stage */ = flash.display.Stage.instance();\n");
                    writeString("\t/** @type {flash.display.DisplayObject} */\n");
                    writeString("\tvar sprite /* : DisplayObject */ = (new " + JSSharedData.ROOT_NAME + topLevelClass + "());\n");
                    writeString("\tstage.addChild(sprite);\n");
                    writeString("}\n\n");

                    writeString("// Delay creation of " + topLevelClass + "\n");
                    writeString("document.addEventListener( \"DOMContentLoaded\", main, false );\n\n");
                }
                else
                {
                    writeString("// Main entry point: wrapper for creating " + topLevelClass + "\n");
                    writeString("\t/** @type {flash.display.DisplayObject} */\n");
                    writeString("\tvar sprite /* : DisplayObject */ = (new " + JSSharedData.ROOT_NAME + topLevelClass + "());\n");
                }
            }
        }
    }

    // see http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html
    private void optimize(OutputBitStream outputbuffer, Boolean createSharedLib) throws IOException
    {
        /*
         * <arg value="--compilation_level=ADVANCED_OPTIMIZATIONS"/> <arg value=
         * "--externs=${falcon-sdk}/lib/google/closure-compiler/contrib/externs/jquery-1.5.js"
         * /> <arg value=
         * "--externs=${falcon-sdk}/lib/google/closure-compiler/contrib/externs/svg.js"
         * /> <arg value=
         * "--externs=${falcon-sdk}/lib/google/closure-compiler/contrib/externs/jsTestDriver.js"
         * /> <arg value="--formatting=PRETTY_PRINT"/> <arg
         * value="--js=${falcon-sdk}/frameworks/javascript/goog/base.js"/> <arg
         * value="--js=${build.target.js}"/> <arg
         * value="--js_output_file=${build.target.compiled.js}"/> <arg
         * value="--create_source_map=${build.target.compiled.map}"/>
         */
        String code = new String(outputbuffer.getBytes());
        code = code.substring(0, outputBuffer.size());
        Compiler compiler = new Compiler();

        CompilerOptions options = new CompilerOptions();

        if (JSSharedData.CLOSURE_compilation_level.equals("ADVANCED_OPTIMIZATIONS"))
            CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        else if (createSharedLib && JSSharedData.instance.hasAnyEncryptedJS())
            CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        else if (JSSharedData.CLOSURE_compilation_level.equals("WHITESPACE_ONLY"))
            CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
        else
            CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

        String sdkPath = null;
        if (JSSharedData.CLOSURE_externs == null || JSSharedData.CLOSURE_js == null)
        {
            sdkPath = JSSharedData.SDK_PATH;
        }

        // To get the complete set of externs, the logic in
        // CompilerRunner.getDefaultExterns() should be used here.
        final List<JSSourceFile> extern = CommandLineRunner.getDefaultExterns();
        if (JSSharedData.CLOSURE_externs != null)
        {
            for (String externsFile : JSSharedData.CLOSURE_externs)
                extern.add(JSSourceFile.fromFile(externsFile));
        }
        else if (sdkPath != null)
        {
            /*
             * <arg value=
             * "--externs=${falcon-sdk}/lib/google/closure-compiler/contrib/externs/jquery-1.5.js"
             * /> <arg value=
             * "--externs=${falcon-sdk}/lib/google/closure-compiler/contrib/externs/svg.js"
             * /> <arg value=
             * "--externs=${falcon-sdk}/lib/google/closure-compiler/contrib/externs/jsTestDriver.js"
             * />
             */
            extern.add(JSSourceFile.fromFile(sdkPath + "/lib/google/closure-compiler/contrib/externs/svg.js"));
            if (JSSharedData.FRAMEWORK_CLASS.equals("browser.JQueryFramework"))
                extern.add(JSSourceFile.fromFile(sdkPath + "/lib/google/closure-compiler/contrib/externs/jquery-1.5.js"));
            if (JSSharedData.GENERATE_TEST_CASE)
                extern.add(JSSourceFile.fromFile(sdkPath + "/lib/google/closure-compiler/contrib/externs/jsTestDriver.js"));
        }

        final List<JSSourceFile> input = new ArrayList<JSSourceFile>();
        if (JSSharedData.CLOSURE_js != null)
        {
            for (String inputFile : JSSharedData.CLOSURE_js)
                input.add(JSSourceFile.fromFile(inputFile));
        }
        else if (sdkPath != null)
        {
            // <arg value="--js=${falcon-sdk}/frameworks/javascript/goog/base.js"/>
            input.add(JSSourceFile.fromFile(sdkPath + "/frameworks/javascript/goog/base.js"));
        }

        // The dummy input name "input.js" is used here so that any warnings or
        // errors will cite line numbers in terms of input.js.
        input.add(JSSourceFile.fromCode("input.js", code.toString()));
        // input.add( JSSourceFile.fromCode("input.js", "/** @preserve CROSS-COMPILED BY FALCONJS (320588.13) ON 2011-09-09 22:18:10 */\nvar __global = this;\n") );

        if (JSSharedData.CLOSURE_create_source_map != null)
            options.sourceMapOutputPath = JSSharedData.CLOSURE_create_source_map;

        if (JSSharedData.CLOSURE_formatting != null)
        {
            if (JSSharedData.CLOSURE_formatting.equals("PRETTY_PRINT"))
                options.prettyPrint = true;
            else if (JSSharedData.CLOSURE_formatting.equals("PRINT_INPUT_DELIMITER"))
                options.prettyPrint = true;
            else
                throw new RuntimeException("Unknown formatting option: " + JSSharedData.CLOSURE_formatting);
        }
        else if (JSSharedData.DEBUG)
        {
            options.prettyPrint = true;
        }

        // turn off closure's logger depending on verbose mode.
        if (!JSSharedData.instance.isVerbose())
        {
            final Logger logger = Logger.getLogger("com.google.javascript.jscomp");
            logger.setLevel(Level.OFF);
        }

        try
        {
            // compile() returns a Result, but it is not needed here.
            compiler.compile(extern, input, options);

            if (compiler.getErrorCount() == 0)
            {
                // The compiler is responsible for generating the compiled code; it is not
                // accessible via the Result.
                final String optimizedCode = compiler.toSource();
                outputbuffer.reset();
                outputbuffer.write(optimizedCode.getBytes());
            }
            else if (m_problems != null)
            {
                final JSError[] errors = compiler.getErrors();
                for (JSError err : errors)
                    m_problems.add(new ClosureProblem(err));
            }
        }

        /*
         * internal compiler error generated with optimize enabled compiling
         * as3_enumerate.fla and fails to release the JS file
         * http://watsonexp.corp.adobe.com/#bug=3047880 This is part #3 of the
         * fix: The closure compiler throws RTEs on internal compiler errors,
         * that don't get caught until they bubble up to MXMLJSC's scope. On
         * their way out files remain unclosed and cause problems, because Flame
         * cannot delete open files. The change below addresses this problem.
         */
        catch (RuntimeException rte)
        {
            if (m_problems != null)
            {
                final ICompilerProblem problem = new InternalCompilerProblem(rte);
                m_problems.add(problem);
            }
        }
    }

    /**
     * {@inherit}
     */
    @Override
    public int writeTo(File outputFile) throws FileNotFoundException, IOException
    {
        return 0;
    }

    public class ClosureProblem implements ICompilerProblem
    {
        private JSError m_error;

        public ClosureProblem(JSError error)
        {
            m_error = error;
        }

        /**
         * Returns a unique identifier for this type of problem.
         * <p>
         * Clients can use this identifier to look up, in a .properties file, a
         * localized template string describing the problem. The template string
         * can have named placeholders such as ${name} to be filled in, based on
         * correspondingly-named fields in the problem instance.
         * <p>
         * Clients can also use this identifier to decide whether the problem is
         * an error, a warning, or something else; for example, they might keep
         * a list of error ids and a list of warning ids.
         * <p>
         * The unique identifier happens to be the fully-qualified classname of
         * the problem class.
         * 
         * @return A unique identifier for the type of problem.
         */
        public String getID()
        {
            // Return the fully-qualified classname of the CompilerProblem subclass
            // as a String to identify the type of problem that occurred.
            return getClass().getName();
        }

        /**
         * Gets the path of the file in which the problem occurred.
         * 
         * @return The path of the source file, or null if unknown.
         */
        public String getFilePath()
        {
            return m_error.sourceName;
        }

        /**
         * Gets the offset within the source buffer at which the problem starts.
         * 
         * @return The starting offset, or -1 if unknown.
         */
        public int getStart()
        {
            return m_error.getCharno();
        }

        /**
         * Gets the offset within the source buffer at which the problem ends.
         * 
         * @return The ending offset, or -1 if unknown.
         */
        public int getEnd()
        {
            return -1;
        }

        /**
         * Gets the line number within the source buffer at which the problem
         * starts. Line numbers start at 0, not 1.
         * 
         * @return The line number, or -1 if unknown.
         */
        public int getLine()
        {
            return m_error.lineNumber;
        }

        /**
         * Gets the column number within the source buffer at which the problem
         * starts. Column numbers start at 0, not 1.
         * 
         * @return The column number, of -1 if unknown.
         */
        public int getColumn()
        {
            return -1;
        }

        /**
         * Returns a readable description of the problem, by substituting field
         * values for named placeholders such as ${name} in the localized
         * template.
         * 
         * @param template A localized template string describing the problem,
         * determined by the client from the problem ID. If this parameter is
         * null, an English template string, stored as the DESCRIPTION of the
         * problem class, will be used.
         * @return A readable description of the problem.
         */
        public String getDescription(String template)
        {
            return m_error.description;
        }

        /**
         * Compares this problem to another problem by path, line, and column so
         * that problems can be sorted.
         */
        final public int compareTo(final ICompilerProblem other)
        {
            if (getFilePath() != null && other.getSourcePath() != null)
            {
                final int pathCompare = getFilePath().compareTo(other.getSourcePath());
                if (pathCompare != 0)
                    return pathCompare;
            }
            else if (getFilePath() != null && other.getSourcePath() == null)
            {
                return 1;
            }
            else if (getFilePath() == null && other.getSourcePath() != null)
            {
                return -1;
            }

            if (getLine() < other.getLine())
                return -1;
            else if (getLine() > other.getLine())
                return 1;

            if (getColumn() < other.getColumn())
                return -1;
            else if (getColumn() > other.getColumn())
                return 1;

            return 0;
        }

        public int getAbsoluteEnd()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getAbsoluteStart()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        public String getSourcePath()
        {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
