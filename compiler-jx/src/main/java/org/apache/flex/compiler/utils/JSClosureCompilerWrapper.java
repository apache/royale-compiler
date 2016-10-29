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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.DependencyOptions;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.FlexJSDiagnosticGroups;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.WarningLevel;

public class JSClosureCompilerWrapper
{

    public JSClosureCompilerWrapper(List<String> args)
    {
        Compiler.setLoggingLevel(Level.INFO);

        compiler_ = new Compiler();

        ArrayList<String> splitArgs = new ArrayList<String>();
        for (String s : args)
        {
        	if (s.contains(" "))
        	{
        		String[] parts = s.split(" ");
        		for (String part : parts)
        			splitArgs.add(part);
        	}
        	else
        		splitArgs.add(s);
        }
		String[] stringArgs = new String[splitArgs.size()];
		splitArgs.toArray(stringArgs);
        options_ = new CompilerOptionsParser(stringArgs).getOptions();
        
        jsSourceFiles_ = new ArrayList<SourceFile>();
        jsExternsFiles_ = new ArrayList<SourceFile>();
        
        initOptions(args);
        initExterns();

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
        addJSSourceFile(SourceFile.fromFile(fileName));
    }

    public void addJSSourceFile(SourceFile file)
    {
        jsSourceFiles_.add(file);
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
        
        /*
        for (JSError message : compiler_.getWarnings())
        {
            System.err.println("Warning message: " + message.toString());
        }
     
        for (JSError message : compiler_.getErrors())
        {
            System.err.println("Error message: " + message.toString());
        }
        */
    }
    
    @SuppressWarnings( "deprecation" )
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
    
    private void initOptions(List<String> args)
    {
    	final String JS_FLAG = "--js ";
    	
    	boolean hasCompilationLevel = false;
    	boolean hasWarningLevel = false;
    	for (String s : args)
    	{
    		if (s.startsWith(JS_FLAG))
    			addJSSourceFile(s.substring(JS_FLAG.length()));
    		
    		if (s.startsWith("--compilation_level ") ||
    			s.startsWith("-O "))
    			hasCompilationLevel = true;
    		
    		if (s.startsWith("--warning_level ") ||
    			s.startsWith("-W "))
    			hasWarningLevel = true;
    	}
    	if (!hasCompilationLevel)
    		CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(
                options_);
        
    	if (!hasWarningLevel)
    		WarningLevel.VERBOSE.setOptionsForWarningLevel(options_);
        
        String[] asdocTags = new String[] {"productversion", 
        		"playerversion", "langversion", "copy", 
        		"asparam", "asreturn", "asprivate",
        		"flexjsignoreimport", "flexjsignorecoercion", "flexjsnoimplicitstringconversion"};
        options_.setExtraAnnotationNames(Arrays.asList(asdocTags));
    }
    
    public void setOptions(String sourceMapPath, boolean useStrictPublishing, String projectName)
    {
        if (useStrictPublishing)
        {
            // (erikdebruin) set compiler flags to 'strictest' to allow maximum
            //               code optimization

            options_.setDefineToBooleanLiteral("goog.DEBUG", true);
            
            // ToDo (erikdebruin): re-evaluate this option on future GC release
            options_.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
            
            options_.setPreferSingleQuotes(true);
            
            options_.setFoldConstants(true);
            options_.setDeadAssignmentElimination(true);
            options_.setInlineConstantVars(true);
            options_.setInlineFunctions(true);
            options_.setInlineLocalFunctions(true);
            options_.setCrossModuleCodeMotion(true);
            options_.setCoalesceVariableNames(true);
            options_.setCrossModuleMethodMotion(true);
            options_.setInlineGetters(true);
            options_.setInlineVariables(true);
            options_.setSmartNameRemoval(true);
            options_.setRemoveDeadCode(true);
            options_.setCheckMissingReturn(CheckLevel.WARNING);
            options_.setExtractPrototypeMemberDeclarations(true);
            options_.setRemoveUnusedPrototypeProperties(true);
            options_.setRemoveUnusedPrototypePropertiesInExterns(false);
            options_.setRemoveUnusedClassProperties(true);
            options_.setRemoveUnusedVars(true);
            options_.setRemoveUnusedLocalVars(true);
            options_.setCollapseVariableDeclarations(true);
            options_.setCollapseAnonymousFunctions(true);
            options_.setAliasAllStrings(true);
            options_.setConvertToDottedProperties(true);
            options_.setRewriteFunctionExpressions(true);
            options_.setOptimizeParameters(true);
            options_.setOptimizeReturns(true);
            options_.setOptimizeCalls(true);
            options_.setOptimizeArgumentsArray(true);
            options_.setGenerateExports(true);
            options_.setExportLocalPropertyDefinitions(true);
            
            DependencyOptions dopts = new DependencyOptions();
            ArrayList<String> entryPoints = new ArrayList<String>();
            entryPoints.add(projectName);
            dopts.setDependencyPruning(true)
                 .setDependencySorting(true)
                 .setMoocherDropping(true)
                 .setEntryPoints(entryPoints);
            options_.setDependencyOptions(dopts);
            
            // warnings already activated in previous incarnation
            options_.setWarningLevel(DiagnosticGroups.ACCESS_CONTROLS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CONST, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CONSTANT_PROPERTY, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.STRICT_MODULE_DEP_CHECK, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.VISIBILITY, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.DEPRECATED, CheckLevel.OFF); // OFF
            
            // the 'full' set of warnings
            options_.setWarningLevel(DiagnosticGroups.AMBIGUOUS_FUNCTION_DECL, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CHECK_EVENTFUL_OBJECT_DISPOSAL, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_PROVIDE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CHECK_REGEXP, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CHECK_USELESS_CODE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.DEBUGGER_STATEMENT_PRESENT, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.DUPLICATE_MESSAGE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.DUPLICATE_VARS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.ES3, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.EXTERNS_VALIDATION, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.FILEOVERVIEW_JSDOC, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.INTERNET_EXPLORER_CHECKS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.INVALID_CASTS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.LINT_CHECKS, CheckLevel.OFF); // OFF
            options_.setWarningLevel(DiagnosticGroups.MISPLACED_TYPE_ANNOTATION, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_PROVIDE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_REQUIRE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_RETURN, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.NEW_CHECK_TYPES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.NON_STANDARD_JSDOC, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.REPORT_UNKNOWN_TYPES, CheckLevel.OFF); // OFF
            options_.setWarningLevel(DiagnosticGroups.SUSPICIOUS_CODE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.TWEAKS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.TYPE_INVALIDATION, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.UNDEFINED_NAMES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.UNKNOWN_DEFINES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.UNNECESSARY_CASTS, CheckLevel.OFF); // OFF
            options_.setWarningLevel(DiagnosticGroups.USE_OF_GOOG_BASE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.VIOLATED_MODULE_DEP, CheckLevel.WARNING);
            
            // TODO (erikdebruin) Need to figure out how we can replace @expose
            options_.setWarningLevel(DiagnosticGroups.DEPRECATED_ANNOTATIONS, CheckLevel.OFF);

            // create custom DiagnosticGroups to shut off some individual warnings when we
            // still want warnings for others in the group.
            options_.setWarningLevel(FlexJSDiagnosticGroups.FLEXJS_NOT_A_CONSTRUCTOR, CheckLevel.OFF);
            options_.setWarningLevel(FlexJSDiagnosticGroups.FLEXJS_SUPER_CALL_TO_DIFFERENT_NAME, CheckLevel.OFF);
            options_.setWarningLevel(FlexJSDiagnosticGroups.FLEXJS_UNKNOWN_JSDOC_TYPE_NAME, CheckLevel.OFF);
//            options_.setWarningLevel(FlexJSDiagnosticGroups.FLEXJS_REFERENCE_BEFORE_DECLARE, CheckLevel.OFF);
        }
        
        options_.sourceMapFormat = SourceMap.Format.V3;
        options_.sourceMapOutputPath = sourceMapPath + ".map";
    }

    private static class CompilerOptionsParser extends CommandLineRunner
    {
    	public CompilerOptionsParser(String[] args)
    	{
    		super(args);
    	}
    	
    	public CompilerOptions getOptions()
    	{
    		return createOptions();
    	}
    }
}
