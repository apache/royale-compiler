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

package org.apache.royale.compiler.clients;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.ProblemQueryProvider;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.codegen.js.IJSWriter;
import org.apache.royale.compiler.codegen.js.goog.IJSGoogPublisher;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationBuffer;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.config.ICompilerSettingsConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.driver.js.IJSApplication;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.exceptions.ConfigurationException.IOError;
import org.apache.royale.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.royale.compiler.exceptions.ConfigurationException.OnlyOneSource;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.goog.JSGoogDocEmitter;
import org.apache.royale.compiler.internal.config.FlashBuilderConfigurator;
import org.apache.royale.compiler.internal.definitions.AccessorDefinition;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.driver.mxml.royale.MXMLRoyaleBackend;
import org.apache.royale.compiler.internal.parsing.as.RoyaleASDocDelegate;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.internal.scopes.ASProjectScope.DefinitionPromise;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.targets.RoyaleJSTarget;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.internal.units.ResourceBundleCompilationUnit;
import org.apache.royale.compiler.internal.units.ResourceModuleCompilationUnit;
import org.apache.royale.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.problems.UnableToBuildSWFProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.targets.ITarget;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;
import org.apache.royale.compiler.utils.ClosureUtils;
import org.apache.flex.tools.FlexTool;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCFileEntry;
import org.apache.royale.swc.ISWCManager;
import org.apache.royale.utils.ArgumentUtil;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * @author Erik de Bruin
 * @author Michael Schmalle
 */
public class MXMLJSCRoyale implements JSCompilerEntryPoint, ProblemQueryProvider,
        FlexTool
{
    @Override
    public ProblemQuery getProblemQuery()
    {
        return problems;
    }


    /*
     * Exit code enumerations.
     */
    static enum ExitCode
    {
        SUCCESS(0),
        PRINT_HELP(1),
        FAILED_WITH_PROBLEMS(0),
        FAILED_WITH_ERRORS(3),
        FAILED_WITH_EXCEPTIONS(4),
        FAILED_WITH_CONFIG_PROBLEMS(5);

        ExitCode(int code)
        {
            this.code = code;
        }

        final int code;
    }

    public static MXMLJSC.JSOutputType jsOutputType;

    @Override
    public String getName()
    {
        return FLEX_TOOL_MXMLC;
    }

    @Override
    public int execute(String[] args)
    {
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        return mainNoExit(args, problems, true);
    }

    /**
     * Java program entry point.
     * 
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
        int exitCode = staticMainNoExit(args);
        System.exit(exitCode);
    }

    /**
     * Entry point for the {@code <compc>} Ant task.
     *
     * @param args Command line arguments.
     * @return An exit code.
     */
    public static int staticMainNoExit(final String[] args)
    {
        long startTime = System.nanoTime();

        final MXMLJSCRoyale mxmlc = new MXMLJSCRoyale();
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1e9 + " seconds");

        return exitCode;
    }

    protected Workspace workspace;
    protected RoyaleJSProject project;

    protected ProblemQuery problems;
    protected ISourceFileHandler asFileHandler;
    protected Configuration config;
    protected Configurator projectConfigurator;
    private ConfigurationBuffer configBuffer;
    private ICompilationUnit mainCU;
    protected ITarget target;
    protected ITargetSettings targetSettings;
    protected IJSApplication jsTarget;
    private IJSGoogPublisher jsPublisher;
    
    public MXMLJSCRoyale()
    {
    	this(new MXMLRoyaleBackend());
    }
    
    public MXMLJSCRoyale(IBackend backend)
    {
        DefinitionBase.setPerformanceCachingEnabled(true);
        workspace = new Workspace();
        workspace.setASDocDelegate(new RoyaleASDocDelegate());
        project = new RoyaleJSProject(workspace, backend);
        problems = new ProblemQuery(); // this gets replaced in configure().  Do we need it here?
        asFileHandler = backend.getSourceFileHandlerInstance();
    }

    @Override
    public int mainNoExit(final String[] args, List<ICompilerProblem> problems,
            Boolean printProblems)
    {
        int exitCode = -1;
        try
        {
            exitCode = _mainNoExit(ArgumentUtil.fixArgs(args), problems);
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }
        finally
        {
            if (problems != null && !problems.isEmpty())
            {
                if (printProblems)
                {
                    final WorkspaceProblemFormatter formatter = new WorkspaceProblemFormatter(
                            workspace);
                    final ProblemPrinter printer = new ProblemPrinter(formatter);
                    printer.printProblems(problems);
                }
            }
        }
        return exitCode;
    }

    /**
     * Entry point that doesn't call <code>System.exit()</code>. This is for
     * unit testing.
     * 
     * @param args command line arguments
     * @return exit code
     */
    private int _mainNoExit(final String[] args,
            List<ICompilerProblem> outProblems)
    {
        ExitCode exitCode = ExitCode.SUCCESS;
        try
        {
            final boolean continueCompilation = configure(args);

/*            if (outProblems != null && !config.isVerbose())
                JSSharedData.STDOUT = JSSharedData.STDERR = null;*/

            if (continueCompilation)
            {
                project.setProblems(problems.getProblems());
               	compile();
                if (problems.hasFilteredProblems())
                {
                    if (problems.hasErrors())
                        exitCode = ExitCode.FAILED_WITH_ERRORS;
                    else
                        exitCode = ExitCode.FAILED_WITH_PROBLEMS;
                }
            }
            else if (problems.hasFilteredProblems())
            {
                exitCode = ExitCode.FAILED_WITH_CONFIG_PROBLEMS;
            }
            else
            {
                exitCode = ExitCode.PRINT_HELP;
            }
        }
        catch (Exception e)
        {
            if (outProblems == null) {
                System.err.println(e.getMessage());
            } else
            {
                final ICompilerProblem unexpectedExceptionProblem = new UnexpectedExceptionProblem(
                        e);
                problems.add(unexpectedExceptionProblem);
            }
            exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
        }
        finally
        {
            waitAndClose();

            if (outProblems != null && problems.hasFilteredProblems())
            {
                for (ICompilerProblem problem : problems.getFilteredProblems())
                {
                    outProblems.add(problem);
                }
            }
        }
        return exitCode.code;
    }

    /**
     * Main body of this program. This method is called from the public static
     * method's for this program.
     * 
     * @return true if compiler succeeds
     * @throws IOException
     * @throws InterruptedException
     */
    protected boolean compile()
    {
        JSGoogConfiguration googConfiguration = (JSGoogConfiguration) config;
        boolean compilationSuccess = false;

        try
        {
            project.getSourceCompilationUnitFactory().addHandler(asFileHandler);

            if (!googConfiguration.getSkipTranspile())
            {
	            if (!setupTargetFile()) {
                    return false;
                }

	            buildArtifact();
            }
            if (jsTarget != null || googConfiguration.getSkipTranspile())
            {
                List<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
                List<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();

                if (!config.getCreateTargetWithErrors())
                {
                    problems.getErrorsAndWarnings(errors, warnings);
                    if (errors.size() > 0)
                        return false;
                }

                Set<String> closurePropNamesToKeep = new HashSet<String>();
                //use a LinkedHashSet because the order of the exported names matters -JT
                LinkedHashSet<String> closureSymbolNamesToExport = new LinkedHashSet<String>();
                jsPublisher = (IJSGoogPublisher) project.getBackend().createPublisher(
                        project, errors, config);

                File outputFolder = jsPublisher.getOutputFolder();

                if (!googConfiguration.getSkipTranspile())
                {
	                ArrayList<ICompilationUnit> roots = new ArrayList<ICompilationUnit>();
	                roots.add(mainCU);
	                Set<ICompilationUnit> incs = target.getIncludesCompilationUnits();
	                roots.addAll(incs);
	                project.mixinClassNames = new TreeSet<String>();
	                project.remoteClassAliasMap = new HashMap<String, String>();
	                List<ICompilationUnit> reachableCompilationUnits = project.getReachableCompilationUnitsInSWFOrder(roots);
	                ((RoyaleJSTarget)target).collectMixinMetaData(project.mixinClassNames, reachableCompilationUnits);
	                ((RoyaleJSTarget)target).collectRemoteClassMetaData(project.remoteClassAliasMap, reachableCompilationUnits);
	                // run through looking for resource bundles so we can have the list ready for the mainCU in the second loop
	                for (final ICompilationUnit cu : reachableCompilationUnits)
	                {
	                    ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();
	                    if (cuType == ICompilationUnit.UnitType.RESOURCE_UNIT)
	                    {
	                    	outputResourceBundle((ResourceBundleCompilationUnit)cu, outputFolder);
	                    }
	                }
	                for (final ICompilationUnit cu : reachableCompilationUnits)
	                {
	                    ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();
	
	                    if (cuType == ICompilationUnit.UnitType.AS_UNIT
	                            || cuType == ICompilationUnit.UnitType.MXML_UNIT)
	                    {
	                        final File outputClassFile = getOutputClassFile(
	                                cu.getQualifiedNames().get(0), outputFolder);
    
                            if (config.isVerbose())
                            {
                                System.out.println("Compiling file: " + outputClassFile);
                            }
	
	                        ICompilationUnit unit = cu;
	
	                        IJSWriter writer;
	                        if (cuType == ICompilationUnit.UnitType.AS_UNIT)
	                        {
	                            writer = (IJSWriter) project.getBackend().createWriter(project,
	                                    problems.getProblems(), unit, false);
	                        }
	                        else
	                        {
	                            writer = (IJSWriter) project.getBackend().createMXMLWriter(
	                                    project, problems.getProblems(), unit, false);
	                        }
	
	                        BufferedOutputStream out = new BufferedOutputStream(
	                                new FileOutputStream(outputClassFile));

                            BufferedOutputStream sourceMapOut = null;
	                        File outputSourceMapFile = null;
	                        if (project.config.getSourceMap())
	                        {
	                            outputSourceMapFile = getOutputSourceMapFile(
                                        cu.getQualifiedNames().get(0), outputFolder);
                                sourceMapOut = new BufferedOutputStream(
	                                    new FileOutputStream(outputSourceMapFile));
	                        }
	                        
	                        writer.writeTo(out, sourceMapOut, outputSourceMapFile);
	                        out.flush();
	                        out.close();
                            if (sourceMapOut != null)
                            {
                                sourceMapOut.flush();
                                sourceMapOut.close();
                            }
	                        writer.close();
	                    }
                        ClosureUtils.collectPropertyNamesToKeep(cu, project, closurePropNamesToKeep);
                        ClosureUtils.collectSymbolNamesToExport(cu, project, closureSymbolNamesToExport);
	                }
	                File externsReportFile = googConfiguration.getExternsReport();
	                if (externsReportFile != null)
	                	generateExternsReport(externsReportFile, reachableCompilationUnits, problems);
                }
                
                if (!config.getCreateTargetWithErrors())
                {
                	errors.clear();
                	warnings.clear();
                    problems.getErrorsAndWarnings(errors, warnings);
                    if (errors.size() > 0)
                        return false;
                }
                
                if (jsPublisher != null)
                {
                    jsPublisher.setClosurePropertyNamesToKeep(closurePropNamesToKeep);
                    jsPublisher.setClosureSymbolNamesToExport(closureSymbolNamesToExport);
                    compilationSuccess = jsPublisher.publish(problems);
                }
                else
                {
                    compilationSuccess = true;
                }
            }
        }
        catch (Exception e)
        {
            final ICompilerProblem problem = new InternalCompilerProblem(e);
            problems.add(problem);
        }
        List<ICompilerProblem> errs = new ArrayList<ICompilerProblem>();
        List<ICompilerProblem> warns = new ArrayList<ICompilerProblem>();
        problems.getErrorsAndWarnings(errs, warns);

        return compilationSuccess && (errs.size() == 0);
    }

    private void generateExternsReport(File externsReportFile,
			List<ICompilationUnit> reachableCompilationUnits,
			ProblemQuery problems) {
        
        if (config.isVerbose())
        {
            System.out.println("Generating externs report: " + externsReportFile.getAbsolutePath());
        }
        
    	ArrayList<String> packageNames = new ArrayList<String>();
    	ArrayList<String> partNames = new ArrayList<String>();
    	
    	StringBuilder sb = new StringBuilder();
        sb.append("/**\n");
        sb.append(" * Generated by Apache Royale Compiler\n");
        sb.append(" *\n");
        sb.append(" * @fileoverview\n");
        sb.append(" * @externs\n");
        sb.append(" *\n");
        // need to suppress access controls so access to protected/private from defineProperties
        // doesn't generate warnings.
        sb.append(" * @suppress {checkTypes|accessControls}\n");
        sb.append(" */\n");

    	for (ICompilationUnit cu : reachableCompilationUnits)
    	{
    		if (project.isExternalLinkage(cu)) continue;
    		
            List<IDefinition> dp = cu.getDefinitionPromises();

            if (dp.size() == 0)
                return;

            IDefinition def = dp.get(0);
            IDefinition actualDef = ((DefinitionPromise) def).getActualDefinition();
            if (actualDef.getPackageName().contains("goog")) continue;
            if (actualDef instanceof ClassDefinition)
            {
            	sb.append("\n\n");
            	ClassDefinition cdef = (ClassDefinition)actualDef;
            	String pkgName = cdef.getPackageName();
            	if (pkgName.length() > 0 && !packageNames.contains(pkgName))
            	{
            		packageNames.add(pkgName);
            		String[] parts = pkgName.split("\\.");
            		String current = "";
            		boolean firstOne = true;
            		for (String part : parts)
            		{
            			current += part;
            			if (partNames.contains(current))
            			{
            				firstOne = false;
        	                current += ".";
            				continue;
            			}            			
            			partNames.add(current);
        				sb.append("/**\n * @suppress {duplicate}\n * @const\n */\n");
            			if (firstOne)
            			{
            				sb.append("var ");
            				firstOne = false;
            			}
            			sb.append(current);
            			sb.append(" = {}");
            			sb.append(ASEmitterTokens.SEMICOLON.getToken() + "\n");
    	                current += ".";
            		}
            	}
            	sb.append("\n\n");
            	sb.append("/**\n");
            	sb.append(" * @constructor\n");
            	String baseString = cdef.getBaseClassAsDisplayString();
            	if (baseString.length() > 0)
            		sb.append(" * @extends {" + baseString + "}\n");
            	String[] ifaces = cdef.getImplementedInterfacesAsDisplayStrings();
            	for (String iface : ifaces)
            		sb.append(" * @implements {" + iface + "}\n");
            	sb.append(" */\n");
            	if (pkgName.length() == 0)
                    sb.append("function " + cdef.getQualifiedName() + "() {}\n");
            	else
            		sb.append(cdef.getQualifiedName() + " = function() {}\n");
                
            	ASScope cscope = cdef.getContainedScope();
            	Collection<IDefinitionSet> defSets = cscope.getAllLocalDefinitionSets();
            	IDefinitionSet[] arrayOfDefSets = new IDefinitionSet[defSets.size()];
            	defSets.toArray(arrayOfDefSets);
            	for (IDefinitionSet defSet : arrayOfDefSets)
            	{
            		int n = defSet.getSize();
            		for (int i = 0; i < n; i++)
            		{
            			IDefinition api = defSet.getDefinition(i);
            			String apiName = api.getBaseName();
            			if (apiName.startsWith("#")) continue; // invalid in externs
            			if (!api.isOverride() && (api.isProtected() || api.isPublic()))
            			{
            				if (!(api instanceof FunctionDefinition) ||
            						api instanceof AccessorDefinition)
            				{
                            	sb.append("\n\n");
                            	sb.append("/**\n");
                            	sb.append(" * @type {" + getJSType(api.getTypeAsDisplayString()) + "}\n");
                            	sb.append(" */\n");
                            	sb.append(cdef.getQualifiedName() + ".");
                            	if (!api.isStatic())
                            		sb.append("prototype.");
                            	sb.append(api.getBaseName() + ";\n");            					
            				}
            				else
            				{
            					FunctionDefinition method = (FunctionDefinition)api;
            					ParameterDefinition[] params = method.getParameters();
                            	sb.append("\n\n");
                            	sb.append("/**\n");
                            	for (ParameterDefinition param : params)
                            	{
                            		if (param.getBaseName().isEmpty())
                            			sb.append(" * @param {*=} opt_rest\n");
                            		else
                            			sb.append(" * @param {" + getJSType(param.getTypeAsDisplayString()) + "} " + param.getBaseName() + "\n");
                            	}
                            	String ret = getJSType(method.getReturnTypeAsDisplayString());
                            	if (!ret.equals("void"))
                            		sb.append(" * @returns {" + ret + "}\n");
                            	sb.append(" */\n");
                            	sb.append(cdef.getQualifiedName() + ".");
                            	if (!api.isStatic())
                            		sb.append("prototype.");
                            	sb.append(api.getBaseName());
                            	sb.append(" = function(");
                            	int m = params.length;
                            	for (int j = 0; j < m; j++)
                            	{
                            		if (j > 0)
                            			sb.append(",");
                            		if (params[j].getBaseName().isEmpty())
                            			sb.append("opt_rest");
                            		else
                            			sb.append(params[j].getBaseName());
                            	}
                            	sb.append(") {");
                            	if (!ret.equals("void"))
                            	{
                            		if (ret.equals("number"))
                            			sb.append(" return 0; ");
                            		else if (ret.equals("boolean"))
                            			sb.append(" return false; ");
                            		else
                            			sb.append(" return null; ");
                            	}
                            	sb.append("};\n");
            				}
            			}
            		}
            	}            	
            }
            else if (actualDef instanceof InterfaceDefinition)
            {
            	sb.append("\n\n");
            	InterfaceDefinition cdef = (InterfaceDefinition)actualDef;
            	String pkgName = cdef.getPackageName();
            	if (pkgName.length() > 0 && !packageNames.contains(pkgName))
            	{
            		packageNames.add(pkgName);
            		String[] parts = pkgName.split("\\.");
            		String current = "";
            		boolean firstOne = true;
            		for (String part : parts)
            		{
            			current += part;
            			if (partNames.contains(current))
            			{
            				firstOne = false;
        	                current += ".";
            				continue;
            			}            			
            			partNames.add(current);
        				sb.append("/**\n * @suppress {duplicate}\n * @const\n */\n");
            			if (firstOne)
            			{
            				sb.append("var ");
            				firstOne = false;
            			}
            			sb.append(current);
            			sb.append(" = {}");
            			sb.append(ASEmitterTokens.SEMICOLON.getToken() + "\n");
    	                current += ".";
            		}
            	}
            	sb.append("\n\n");
            	sb.append("/**\n");
            	sb.append(" * @interface\n");
            	String[] ifaces = cdef.getExtendedInterfacesAsDisplayStrings();
            	for (String iface : ifaces)
            		sb.append(" * @extends {" + iface + "}\n");
            	sb.append(" */\n");
                sb.append(cdef.getQualifiedName() + " = function() {}\n");
            }
        }
        if (config.isVerbose())
        {
            System.out.println("Writing externs report: " + externsReportFile.getAbsolutePath());
        }
        FileWriter fw;
		try {
			fw = new FileWriter(externsReportFile, false);
            fw.write(sb.toString());
            fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    private String getJSType(String s)
    {
    	if (s.contains("__AS3__.vec.Vector"))
    		return "Array";
    	return JSGoogDocEmitter.convertASTypeToJSType(s, "");
    }

	private void outputResourceBundle(ResourceBundleCompilationUnit cu, File outputFolder) {
		// TODO Auto-generated method stub
        final ISWCManager swcManager = project.getWorkspace().getSWCManager();
        // find the SWC
        final ISWC swc = swcManager.get(new File(cu.getAbsoluteFilename()));
        if (swc != null)
        {
            if (swc.getSWCFile().getAbsolutePath().endsWith(".swc"))
            {
	        	String bundleName = cu.getBundleNameInColonSyntax();
	        	String propFileName = "locale/" + cu.getLocale() + "/" + bundleName + ".properties";
	        	String bundleClassName = cu.getLocale() + "$" + bundleName + "_properties";
	            Map<String, ISWCFileEntry> files = swc.getFiles();
	            for (String key : files.keySet())
	            {
	                if (key.equals(propFileName))
	                {
	                	if (!project.compiledResourceBundleNames.contains(bundleName))
	                		project.compiledResourceBundleNames.add(bundleName);
	                	project.compiledResourceBundleClasses.add(bundleClassName);
	                    ISWCFileEntry fileEntry = swc.getFile(key);
	                    if (fileEntry != null)
	                    {
							InputStream is;
							try {
								is = fileEntry.createInputStream();
								BufferedReader br = new BufferedReader(new InputStreamReader(is));
			                	writeResourceBundle(br, bundleClassName, outputFolder);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                    }
	                }
	            }
            }
            else
            {
            	// it isn't a bundle from a SWC, it is a bundle in the source path
	        	String bundleName = cu.getBundleNameInColonSyntax();
	        	String bundleClassName = cu.getLocale() + "$" + bundleName + "_properties";
            	if (!project.compiledResourceBundleNames.contains(bundleName))
            		project.compiledResourceBundleNames.add(bundleName);
            	project.compiledResourceBundleClasses.add(bundleClassName);
				InputStream is;
				try {
					is = new FileInputStream(swc.getSWCFile());
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
                	writeResourceBundle(br, bundleClassName, outputFolder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
	}

	private void writeResourceBundle(BufferedReader br, String bundleClassName, File outputFolder)
	{
		StringBuilder sb = new StringBuilder();
		try {
			String line;
			while ((line = br.readLine()) != null)
			{
				if (line.contains("="))
				{
					if (sb.length() == 0)
					{
						sb.append("/**\n");
						sb.append(" * Generated by Apache Royale Compiler from " + bundleClassName + ".properties\n");
						sb.append(" * " + bundleClassName + "\n");
						sb.append(" *\n");
						sb.append(" * @fileoverview\n");
						sb.append(" *\n");
						sb.append(" * @suppress {checkTypes|accessControls}\n");
						sb.append(" */\n\n");
						sb.append("goog.provide('" + bundleClassName + "');\n\n");
						sb.append("goog.require('mx.resources.IResourceBundle');\n");
						sb.append("goog.require('mx.resources.ResourceBundle');\n\n\n");
						sb.append("/**\n");
						sb.append(" * @constructor\n");
						sb.append(" * @extends {mx.resources.ResourceBundle}\n");
						sb.append(" * @implements {mx.resources.IResourceBundle}\n");
						sb.append(" */\n");
						sb.append(bundleClassName + " = function() {\n");
						sb.append("    " + bundleClassName + ".base(this, 'constructor');\n");
						sb.append("};\n");
						sb.append("goog.inherits(" + bundleClassName + ", mx.resources.ResourceBundle);\n\n");
						sb.append("/**\n");
						sb.append(" * Prevent renaming of class. Needed for reflection.\n");
						sb.append(" */\n");
						sb.append("goog.exportSymbol('" + bundleClassName + "', " + bundleClassName + ");\n\n");
						sb.append(bundleClassName + ".prototype.getContent = function() { return {\n");
					}
					int c = line.indexOf("=");
					String propName = line.substring(0, c);
					String value = line.substring(c + 1);
					while (value.endsWith("/"))
					{
						value = value.substring(0, value.length() - 1);
						value += br.readLine();
					}
					sb.append("'" + propName + "' : \"" + value + "\",\n");
				}
			}
			sb.append("__end_of_bundle__: 0\n};};\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final File outputClassFile = getOutputClassFile(
                bundleClassName, outputFolder);
        if (config.isVerbose())
        {
            System.out.println("Generating resource file: " + outputClassFile);
        }
		FileWriter fw;
		try {
			fw = new FileWriter(outputClassFile, false);
			fw.write(sb.toString());
			fw.close();
			long fileDate = 0;
        	String metadataDate = targetSettings.getSWFMetadataDate();
        	if (metadataDate != null)
        	{
        		String metadataFormat = targetSettings.getSWFMetadataDateFormat();
        		try {
        			SimpleDateFormat sdf = new SimpleDateFormat(metadataFormat);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    fileDate = sdf.parse(metadataDate).getTime();
        		} catch (ParseException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (IllegalArgumentException e1) {
    				e1.printStackTrace();
    			}
        		outputClassFile.setLastModified(fileDate);
        	}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
     * Build target artifact.
     * 
     * @throws InterruptedException threading error
     * @throws IOException IO error
     * @throws ConfigurationException
     */
    protected void buildArtifact() throws InterruptedException, IOException,
            ConfigurationException
    {
        jsTarget = buildJSTarget();
    }

    private IJSApplication buildJSTarget() throws InterruptedException,
            FileNotFoundException, ConfigurationException
    {
        final List<ICompilerProblem> problemsBuildingSWF = new ArrayList<ICompilerProblem>();

        project.mainCU = mainCU;
        final IJSApplication app = buildApplication(project,
                config.getMainDefinition(), mainCU, problemsBuildingSWF);
        problems.addAll(problemsBuildingSWF);
        if (app == null)
        {
            ICompilerProblem problem = new UnableToBuildSWFProblem(
                    getOutputFilePath());
            problems.add(problem);
        }

        return app;
    }

    /**
     * Replaces RoyaleApplicationProject::buildSWF()
     * 
     * @param applicationProject
     * @param rootClassName
     * @param problems
     * @return
     * @throws InterruptedException
     */

    private IJSApplication buildApplication(CompilerProject applicationProject,
            String rootClassName, ICompilationUnit mainCU,
            Collection<ICompilerProblem> problems) throws InterruptedException,
            ConfigurationException, FileNotFoundException
    {
        Collection<ICompilerProblem> fatalProblems = applicationProject.getFatalProblems();
        if (!fatalProblems.isEmpty())
        {
            problems.addAll(fatalProblems);
            return null;
        }

        return ((JSTarget) target).build(mainCU, problems);
    }

    /**
     * Get the output file path. If {@code -output} is specified, use its value;
     * otherwise, use the same base name as the target file.
     * 
     * @return output file path
     */
    private String getOutputFilePath()
    {
        if (config.getOutput() == null)
        {
            final String extension = "." + project.getBackend().getOutputExtension();
            return FilenameUtils.removeExtension(config.getTargetFile()).concat(
                    extension);
        }
        else
            return config.getOutput();
    }

    /**
     * @author Erik de Bruin
     * 
     *         Get the output class file. This includes the (sub)directory in
     *         which the original class file lives. If the directory structure
     *         doesn't exist, it is created.
     * 
     * @param qname
     * @param outputFolder
     * @return output class file path
     */
    private File getOutputClassFile(String qname, File outputFolder)
    {
        String[] cname = qname.split("\\.");
        String sdirPath = outputFolder + File.separator;
        if (cname.length > 0)
        {
            for (int i = 0, n = cname.length - 1; i < n; i++)
            {
                sdirPath += cname[i] + File.separator;
            }

            File sdir = new File(sdirPath);
            if (!sdir.exists())
                sdir.mkdirs();

            qname = cname[cname.length - 1];
        }

        return new File(sdirPath + qname + "." + project.getBackend().getOutputExtension());
    }

    /**
     * @param qname
     * @param outputFolder
     * @return output source map file path
     */
    private File getOutputSourceMapFile(String qname, File outputFolder)
    {
        String[] cname = qname.split("\\.");
        String sdirPath = outputFolder + File.separator;
        if (cname.length > 0)
        {
            for (int i = 0, n = cname.length - 1; i < n; i++)
            {
                sdirPath += cname[i] + File.separator;
            }

            File sdir = new File(sdirPath);
            if (!sdir.exists())
                sdir.mkdirs();

            qname = cname[cname.length - 1];
        }

        return new File(sdirPath + qname + "." + project.getBackend().getOutputExtension() + ".map");
    }

    /**
     * Mxmlc uses target file as the main compilation unit and derive the output
     * SWF file name from this file.
     * 
     * @return true if successful, false otherwise.
     * @throws OnlyOneSource
     * @throws InterruptedException
     */
    protected boolean setupTargetFile() throws InterruptedException
    {
        final String mainFileName = config.getTargetFile();

        final String normalizedMainFileName = FilenameNormalization.normalize(mainFileName);

        final SourceCompilationUnitFactory compilationUnitFactory = project.getSourceCompilationUnitFactory();

        File normalizedMainFile = new File(normalizedMainFileName);
        if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
        {
            project.addIncludeSourceFile(normalizedMainFile);

            final List<String> sourcePath = config.getCompilerSourcePath();
            String mainQName = null;
            if (sourcePath != null && !sourcePath.isEmpty())
            {
                for (String path : sourcePath)
                {
                    final String otherPath = new File(path).getAbsolutePath();
                    if (mainFileName.startsWith(otherPath))
                    {
                        mainQName = mainFileName.substring(otherPath.length() + 1);
                        mainQName = mainQName.replaceAll("\\\\", "/");
                        mainQName = mainQName.replaceAll("\\/", ".");
                        if (mainQName.endsWith(".as"))
                            mainQName = mainQName.substring(0,
                                    mainQName.length() - 3);
                        break;
                    }
                }
            }

            if (mainQName == null)
                mainQName = FilenameUtils.getBaseName(mainFileName);

            Collection<ICompilationUnit> mainFileCompilationUnits = workspace.getCompilationUnits(
                    normalizedMainFileName, project);

            mainCU = Iterables.getOnlyElement(mainFileCompilationUnits);

            config.setMainDefinition(mainQName);
        }

        Preconditions.checkNotNull(mainCU,
                "Main compilation unit can't be null");

        ITargetSettings settings = getTargetSettings();
        if (settings != null)
            project.setTargetSettings(settings);

        target = project.getBackend().createTarget(project,
                getTargetSettings(), null);

        return true;
    }

    private ITargetSettings getTargetSettings()
    {
        if (targetSettings == null)
            targetSettings = projectConfigurator.getTargetSettings(null);

        return targetSettings;
    }

    /**
     * Create a new Configurator. This method may be overridden to allow
     * Configurator subclasses to be created that have custom configurations.
     * 
     * @return a new instance or subclass of {@link Configurator}.
     */
    protected Configurator createConfigurator()
    {
        return project.getBackend().createConfigurator();
    }

    /**
     * Load configurations from all the sources.
     * 
     * @param args command line arguments
     * @return True if mxmlc should continue with compilation.
     */
    protected boolean configure(final String[] args)
    {
        project.getSourceCompilationUnitFactory().addHandler(asFileHandler);
        project.configurator = projectConfigurator = createConfigurator();

        try
        {
            if (useFlashBuilderProjectFiles(args))
            {
                projectConfigurator.setConfiguration(
                        FlashBuilderConfigurator.computeFlashBuilderArgs(args,
                                getTargetType().getExtension()),
                        ICompilerSettingsConstants.FILE_SPECS_VAR);
            }
            else
            {
                projectConfigurator.setConfiguration(args,
                        ICompilerSettingsConstants.FILE_SPECS_VAR);
            }

            projectConfigurator.applyToProject(project);
            project.config = (JSGoogConfiguration) projectConfigurator.getConfiguration();

            config = projectConfigurator.getConfiguration();
            configBuffer = projectConfigurator.getConfigurationBuffer();

            problems = new ProblemQuery(projectConfigurator.getCompilerProblemSettings());
            problems.addAll(projectConfigurator.getConfigurationProblems());

            if (configBuffer.getVar("version") != null) //$NON-NLS-1$
                return false;

            if (problems.hasErrors())
                return false;

            validateTargetFile();
            return true;
        }
        catch (ConfigurationException e)
        {
            final ICompilerProblem problem = new ConfigurationProblem(e);
            problems.add(problem);
            return false;
        }
        catch (Exception e)
        {
            final ICompilerProblem problem = new ConfigurationProblem(null, -1,
                    -1, -1, -1, e.getMessage());
            problems.add(problem);
            return false;
        }
        finally
        {
            if (config == null)
            {
                config = new Configuration();
                configBuffer = new ConfigurationBuffer(Configuration.class,
                        Configuration.getAliases());
            }
        }
    }

    private boolean useFlashBuilderProjectFiles(String[] args)
    {
        for (String arg : args)
        {
            if (arg.equals("-fb")
                    || arg.equals("-use-flashbuilder-project-files"))
                return true;
        }
        return false;
    }

    protected TargetType getTargetType()
    {
        return TargetType.SWF;
    }

    /**
     * Validate target file.
     * 
     * @throws MustSpecifyTarget
     * @throws IOError
     */
    protected void validateTargetFile() throws ConfigurationException
    {
        if (mainCU instanceof ResourceModuleCompilationUnit)
            return; //when compiling a Resource Module, no target file is defined.

        final String targetFile = config.getTargetFile();
        if (targetFile == null)
            throw new ConfigurationException.MustSpecifyTarget(null, null, -1);

        final File file = new File(targetFile);
        if (!file.exists())
            throw new ConfigurationException.IOError(targetFile);
    }

    /**
     * Wait till the workspace to finish compilation and close.
     */
    protected void waitAndClose()
    {
        workspace.startIdleState();
        try
        {
            workspace.close();
        }
        finally
        {
            workspace.endIdleState(Collections.<ICompilerProject, Set<ICompilationUnit>> emptyMap());
        }
    }

    /**
     * Force terminate the compilation process.
     */
    protected void close()
    {
        workspace.close();
    }
    
    public List<String> getSourceList()
    {
        ArrayList<String> list = new ArrayList<String>();
        try
        {
            ArrayList<ICompilationUnit> roots = new ArrayList<ICompilationUnit>();
            roots.add(mainCU);
            Set<ICompilationUnit> incs = target.getIncludesCompilationUnits();
            roots.addAll(incs);
            project.mixinClassNames = new TreeSet<String>();
            List<ICompilationUnit> units = project.getReachableCompilationUnitsInSWFOrder(roots);
            for (ICompilationUnit unit : units)
            {
                UnitType ut = unit.getCompilationUnitType();
                if (ut == UnitType.AS_UNIT || ut == UnitType.MXML_UNIT)
                {
                    list.add(unit.getAbsoluteFilename());
                }
            }
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return list;
    }
    
    public String getMainSource()
    {
        return mainCU.getAbsoluteFilename();
    }

}
