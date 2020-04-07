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

package org.apache.royale.compiler.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerMapFetcher;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.DependencyOptions;
import com.google.javascript.jscomp.DependencyOptions.DependencyMode;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.RoyaleClosurePassConfig;
import com.google.javascript.jscomp.RoyaleDiagnosticGroups;
import com.google.javascript.jscomp.ShowByPathWarningsGuard;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.VariableMap;
import com.google.javascript.jscomp.WarningLevel;

public class JSClosureCompilerWrapper
{

    public JSClosureCompilerWrapper(List<String> args) throws IOException
    {
        Compiler.setLoggingLevel(Level.INFO);

        compiler_ = new Compiler();
        jsSourceFiles_ = new ArrayList<SourceFile>();
        jsExternsFiles_ = new ArrayList<SourceFile>();
        
        filterOptions(args);
        
        ArrayList<String> splitArgs = new ArrayList<String>();
        for (String s : args)
        {
        	if (s.contains(" "))
        	{
        		String[] parts = s.split(" ");
                Collections.addAll(splitArgs, parts);
        	}
        	else
        		splitArgs.add(s);
        }
		String[] stringArgs = new String[splitArgs.size()];
		splitArgs.toArray(stringArgs);
        options_ = new CompilerOptionsParser(stringArgs).getOptions();
        
        initOptions(args);
        initExterns();

    }

    private Compiler compiler_;
    private CompilerOptions options_;
    private List<SourceFile> jsExternsFiles_;
    private List<SourceFile> jsSourceFiles_;
    private String variableMapOutputPath;
    private String propertyMapOutputPath;
    private String variableMapInputPath;
    private String propertyMapInputPath;
    private Set<String> propertyNamesToKeep;
    private Set<String> extraSymbolNamesToExport;
    private boolean skipTypeInference;
    private boolean sourceMap = false;
    private boolean verbose = false;
    
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

    public void setSourceMap(boolean enabled)
    {
        sourceMap = enabled;
    }

    public void setVerbose(boolean enabled)
    {
        verbose = enabled;
    }

    public void setPropertyNamesToKeep(Set<String> propertyNames)
    {
        propertyNamesToKeep = propertyNames;
    }

    public void setExtraSymbolNamesToExport(Set<String> names)
    {
        extraSymbolNamesToExport = names;
    }
    
    public boolean compile()
    {
        if (verbose)
        {
            System.out.println("list of source files");
            for (SourceFile file : jsSourceFiles_)
                System.out.println(file.getName());
            System.out.println("end of list of source files");
        }
    	File outputFolder = new File(targetFilePath).getParentFile();
        if (variableMapInputPath != null)
        {
        	File inputFile = new File(outputFolder, variableMapInputPath);
        	try {
            	VariableMap map = VariableMap.load(inputFile.getAbsolutePath());
				CompilerMapFetcher.setVariableMap(options_, map);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        if (propertyMapInputPath != null)
        {
        	File inputFile = new File(outputFolder, propertyMapInputPath);
        	try {
            	VariableMap map = VariableMap.load(inputFile.getAbsolutePath());
				CompilerMapFetcher.setPropertyMap(options_, map);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        compiler_.setPassConfig(new RoyaleClosurePassConfig(options_, 
        		jsSourceFiles_.get(jsSourceFiles_.size() - 1).getName(), 
        		variableMapInputPath == null ? null : new File(outputFolder, variableMapInputPath), propertyNamesToKeep, extraSymbolNamesToExport));
        Result result = compiler_.compile(jsExternsFiles_, jsSourceFiles_, options_);
        
        try
        {
            FileWriter targetFile = new FileWriter(targetFilePath);
            targetFile.write(compiler_.toSource());
            targetFile.close();

            if (sourceMap)
            {
                FileWriter sourceMapFile = new FileWriter(options_.sourceMapOutputPath);
                compiler_.getSourceMap().appendTo(sourceMapFile, "");
                sourceMapFile.close();
            }
        }
        catch (IOException error)
        {
            System.out.println(error);
        }
        
        if (variableMapOutputPath != null)
        {
        	File outputFile = new File(outputFolder, variableMapOutputPath);
        	VariableMap map = CompilerMapFetcher.getVariableMap(compiler_);
        	if (map != null)
        	{
	        	try {
					map.save(outputFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
        if (propertyMapOutputPath != null)
        {
        	File outputFile = new File(outputFolder, propertyMapOutputPath);
        	VariableMap map = CompilerMapFetcher.getPropertyMap(compiler_);
        	if (map != null)
        	{
	        	try {
					map.save(outputFile.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
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
        return result.success;
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
    
    private void filterOptions(List<String> args)
    {
		final String SKIP_TYPE_INFERENCE = "--skip_type_inference";
		final String PROPERTY_MAP = "--property_map_output_file ";
		final String VARIABLE_MAP = "--variable_map_output_file ";
		final String PROPERTY_INPUT_MAP = "--property_map_input_file ";
		final String VARIABLE_INPUT_MAP = "--variable_map_input_file ";
		final String EXTERNS = "--externs ";
		String propEntry = null;
		String varEntry = null;
		String skipEntry = null;
		String propInputEntry = null;
		String varInputEntry = null;
		ArrayList<String> removeArgs = new ArrayList<String>();

    	for (String s : args)
    	{
    		if (s.startsWith(PROPERTY_MAP))
    		{
    			propEntry = s;
    			propertyMapOutputPath = s.substring(PROPERTY_MAP.length());
    		}
    		
    		if (s.startsWith(PROPERTY_INPUT_MAP))
    		{
    			propInputEntry = s;
    			propertyMapInputPath = s.substring(PROPERTY_INPUT_MAP.length());
    		}
    		
    		if (s.startsWith(VARIABLE_MAP))
    		{
    			varEntry = s;
    			variableMapOutputPath = s.substring(VARIABLE_MAP.length());
    		}
    			
    		if (s.startsWith(VARIABLE_INPUT_MAP))
    		{
    			varInputEntry = s;
    			variableMapInputPath = s.substring(VARIABLE_INPUT_MAP.length());
    		}
    		
    		if (s.equals(SKIP_TYPE_INFERENCE))
    		{
    			skipEntry = s;
    			skipTypeInference = true;
    		}
    		
    		if (s.startsWith(EXTERNS))
    		{
    			String fileName = s.substring(EXTERNS.length());
    			addJSExternsFile(fileName);
    			removeArgs.add(s);
    		}    			
    	}
    	if (varEntry != null)
    		args.remove(varEntry);
    	if (propEntry != null)
    		args.remove(propEntry);
    	if (varInputEntry != null)
    		args.remove(varInputEntry);
    	if (propInputEntry != null)
    		args.remove(propInputEntry);
    	if (skipEntry != null)
    		args.remove(skipEntry);
    	for (String s : removeArgs)
    	{
    		args.remove(s);
    	}
    		
    }
    
    private void initOptions(List<String> args)
    {
    	final String JS_FLAG = "--js ";
		final String PROPERTY_MAP = "--property_map_output_file ";
		final String VARIABLE_MAP = "--variable_map_output_file ";
		String propEntry = null;
		String varEntry = null;

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
    		
    		if (s.startsWith(PROPERTY_MAP))
    		{
    			propEntry = s;
    			propertyMapOutputPath = s.substring(PROPERTY_MAP.length());
    		}
    		
    		if (s.startsWith(VARIABLE_MAP))
    		{
    			varEntry = s;
    			variableMapOutputPath = s.substring(VARIABLE_MAP.length());
    		}
    			
    	}
    	if (varEntry != null)
    		args.remove(varEntry);
    	if (propEntry != null)
    		args.remove(propEntry);
    		
    	if (!hasCompilationLevel)
    		CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(
                options_);
        
    	if (!hasWarningLevel)
    		WarningLevel.VERBOSE.setOptionsForWarningLevel(options_);
        
        String[] asdocTags = new String[] {"productversion", 
        		"playerversion", "langversion", "copy", "span", "para", "throw", "tiptext",
        		"asparam", "asreturn", "asreturns", "asprivate",
        		"royaleignoreimport", "royaleignorecoercion", "royaleemitcoercion",
                "royalesuppresscompleximplicitcoercion","royalesuppressresolveuncertain",
                "royalesuppressvectorindexcheck","royalesuppressexport", "royalesuppressclosure",
                "royalenoimplicitstringconversion","royaledebug"};
        options_.setExtraAnnotationNames(Arrays.asList(asdocTags));
    }
    
    @SuppressWarnings("deprecation")
	public void setOptions(String sourceMapPath, boolean useStrictPublishing, boolean manageDependencies, String projectName)
    {
        if (useStrictPublishing)
        {
            // (erikdebruin) set compiler flags to 'strictest' to allow maximum
            //               code optimization

            options_.setDefineToBooleanLiteral("goog.DEBUG", false);
            
            // ToDo (erikdebruin): re-evaluate this option on future GC release
            options_.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
            
            options_.setPreferSingleQuotes(true);
            
            options_.setFoldConstants(true);
            options_.setDeadAssignmentElimination(true);
            options_.setInlineConstantVars(true);
            options_.setInlineFunctions(true);
            options_.setInlineLocalVariables(true);
            options_.setCrossChunkCodeMotion(true);
            options_.setCoalesceVariableNames(true);
            options_.setCrossChunkMethodMotion(true);
            options_.setInlineProperties(true);
            options_.setInlineVariables(true);
            options_.setSmartNameRemoval(true);
            options_.setRemoveDeadCode(true);
            options_.setExtractPrototypeMemberDeclarations(true);
            options_.setRemoveUnusedPrototypeProperties(true);
            options_.setRemoveUnusedPrototypePropertiesInExterns(false);
            options_.setRemoveUnusedClassProperties(true);
            options_.setRemoveUnusedVariables(CompilerOptions.Reach.ALL);
            options_.setCollapseVariableDeclarations(true);
            options_.setCollapseAnonymousFunctions(true);
            options_.setAliasAllStrings(true);
            options_.setConvertToDottedProperties(true);
            options_.setRewriteFunctionExpressions(true);
            options_.setOptimizeCalls(true);
            options_.setOptimizeArgumentsArray(true);
            options_.setGenerateExports(true);
            options_.setExportLocalPropertyDefinitions(true);
            options_.addWarningsGuard(new ShowByPathWarningsGuard(
                    new String[] { "goog/", "externs/svg.js" },
                    ShowByPathWarningsGuard.ShowType.EXCLUDE));
            
            ArrayList<String> entryPoints = new ArrayList<String>();
            if (manageDependencies)
            	entryPoints.add(projectName);
            options_.setDependencyOptions(DependencyOptions.fromFlags(manageDependencies ? DependencyMode.PRUNE_LEGACY : DependencyMode.NONE, 
            				entryPoints, new ArrayList<String>(), null, manageDependencies, false));
            
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
            options_.setWarningLevel(DiagnosticGroups.CHECK_TYPES, skipTypeInference ? CheckLevel.OFF : CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CHECK_USELESS_CODE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.DEBUGGER_STATEMENT_PRESENT, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.DUPLICATE_MESSAGE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.DUPLICATE_VARS, skipTypeInference ? CheckLevel.OFF : CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.ES3, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.EXTERNS_VALIDATION, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.FILEOVERVIEW_JSDOC, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.INTERNET_EXPLORER_CHECKS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.INVALID_CASTS, skipTypeInference ? CheckLevel.OFF : CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.LINT_CHECKS, CheckLevel.OFF); // OFF
            options_.setWarningLevel(DiagnosticGroups.MISPLACED_TYPE_ANNOTATION, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, skipTypeInference ? CheckLevel.OFF : CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_PROVIDE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_REQUIRE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.MISSING_RETURN, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.NEW_CHECK_TYPES, skipTypeInference ? CheckLevel.OFF : CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.NON_STANDARD_JSDOC, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.REPORT_UNKNOWN_TYPES, CheckLevel.OFF); // OFF
            options_.setWarningLevel(DiagnosticGroups.SUSPICIOUS_CODE, skipTypeInference ? CheckLevel.OFF : CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.TWEAKS, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.TYPE_INVALIDATION, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.UNDEFINED_NAMES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.UNDEFINED_VARIABLES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.UNKNOWN_DEFINES, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.USE_OF_GOOG_BASE, CheckLevel.WARNING);
            options_.setWarningLevel(DiagnosticGroups.VIOLATED_MODULE_DEP, CheckLevel.WARNING);
            
            // TODO (erikdebruin) Need to figure out how we can replace @expose
            options_.setWarningLevel(DiagnosticGroups.DEPRECATED_ANNOTATIONS, CheckLevel.OFF);

            // create custom DiagnosticGroups to shut off some individual warnings when we
            // still want warnings for others in the group.
            options_.setWarningLevel(RoyaleDiagnosticGroups.ROYALE_NOT_A_CONSTRUCTOR, CheckLevel.OFF);
            options_.setWarningLevel(RoyaleDiagnosticGroups.ROYALE_SUPER_CALL_TO_DIFFERENT_NAME, CheckLevel.OFF);
            options_.setWarningLevel(RoyaleDiagnosticGroups.ROYALE_UNKNOWN_JSDOC_TYPE_NAME, CheckLevel.OFF);
//            options_.setWarningLevel(RoyaleDiagnosticGroups.ROYALE_REFERENCE_BEFORE_DECLARE, CheckLevel.OFF);
        }
        
        options_.sourceMapFormat = SourceMap.Format.V3;
        options_.sourceMapOutputPath = sourceMapPath + ".map";
        if (skipTypeInference)
        {
        	options_.setCheckTypes(false);
        	options_.setInferTypes(false);
        	options_.setNewTypeInference(false);
        }
        
    }

    private static class CompilerOptionsParser extends CommandLineRunner
    {
        public CompilerOptionsParser(String[] args)
        {
            super(args);
        }
        
        public CompilerOptions getOptions() throws IOException
        {
            CompilerOptions options = createOptions();
            setRunOptions(options);
            return options;
        }
    }
}
