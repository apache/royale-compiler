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

package org.apache.flex.compiler.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.WarningLevel;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class JSClosureCompilerWrapper
{

    public JSClosureCompilerWrapper()
    {
        Compiler.setLoggingLevel(Level.INFO);

        compiler_ = new Compiler();

        options_ = new CompilerOptions();
        initOptions();
        
        jsExternsFiles_ = new ArrayList<SourceFile>();
        initExterns();

        jsSourceFiles_ = new ArrayList<SourceFile>();
    }

    private Compiler compiler_;
    private CompilerOptions options_;
    private List<SourceFile> jsExternsFiles_;
    private List<SourceFile> jsSourceFiles_;
    
    public String targetFilePath;
    
    public void addJSExternsFile(String fileName)
    {
        addJSExternsFile(SourceFile.fromFile(fileName));
    }
    
    public void addJSExternsFile(SourceFile file)
    {
        jsExternsFiles_.add(file);
    }
    
    public void addJSSourceFile(String fileName)
    {
        jsSourceFiles_.add(SourceFile.fromFile(fileName));
    }
    
    public void compile()
    {
        compiler_.compile(jsExternsFiles_, jsSourceFiles_, options_);

        try
        {
            FileWriter targetFile = new FileWriter(targetFilePath);
            targetFile.write(compiler_.toSource());
            targetFile.close();
            
            FileWriter sourceMapFile = new FileWriter(options_.sourceMapOutputPath);
            compiler_.getSourceMap().appendTo(sourceMapFile, "");
            sourceMapFile.close();
        }
        catch (IOException error)
        {
            System.out.println(error);
        }
        
        for (JSError message : compiler_.getWarnings())
        {
            System.err.println("Warning message: " + message.toString());
        }
     
        for (JSError message : compiler_.getErrors())
        {
            System.err.println("Error message: " + message.toString());
        }
    }
    
    private void initExterns()
    {
        try
        {
            List<SourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
            for (SourceFile defaultExtern : defaultExterns)
            {
                this.addJSExternsFile(defaultExtern);
            }
        }
        catch (IOException error)
        {
            System.out.println(error);
        }
    }
    
    private void initOptions()
    {
        CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(
                options_);
        
        WarningLevel.VERBOSE.setOptionsForWarningLevel(options_);
    }
    
    public void setOptions(String sourceMapPath, boolean useStrictPublishing)
    {
        if (useStrictPublishing)
        {
            // (erikdebruin) set compiler flags to 'strictest' to allow maximum
            //               code optimization

            options_.getDefineReplacements().put(
                    "goog.DEBUG", new Node(Token.TRUE));
            
            options_.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
            
            options_.setWarningLevel(DiagnosticGroups.ACCESS_CONTROLS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CONST, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CONSTANT_PROPERTY, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.STRICT_MODULE_DEP_CHECK, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.VISIBILITY, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.DEPRECATED, CheckLevel.OFF);
        }
        
        options_.sourceMapFormat = SourceMap.Format.V3;
        options_.sourceMapOutputPath = sourceMapPath + ".map";
    }

}
