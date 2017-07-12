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

package org.apache.flex.compiler.clients;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.apache.flex.compiler.clients.problems.ProblemPrinter;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.clients.problems.ProblemQueryProvider;
import org.apache.flex.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.flex.compiler.codegen.js.IJSPublisher;
import org.apache.flex.compiler.codegen.js.IJSWriter;
import org.apache.flex.compiler.common.VersionInfo;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.ConfigurationBuffer;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.config.ICompilerSettingsConstants;
import org.apache.flex.compiler.driver.js.IJSApplication;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.exceptions.ConfigurationException.IOError;
import org.apache.flex.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.flex.compiler.exceptions.ConfigurationException.OnlyOneSource;
import org.apache.flex.compiler.internal.config.FlashBuilderConfigurator;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.parsing.as.FlexJSASDocDelegate;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.targets.FlexJSTarget;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.internal.units.ResourceModuleCompilationUnit;
import org.apache.flex.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.problems.ConfigurationProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.InternalCompilerProblem;
import org.apache.flex.compiler.problems.UnableToBuildSWFProblem;
import org.apache.flex.compiler.problems.UnexpectedExceptionProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.targets.ITarget;
import org.apache.flex.compiler.targets.ITarget.TargetType;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.swf.ISWF;
import org.apache.flex.swf.SWF;
import org.apache.flex.swf.types.RGB;
import org.apache.flex.swf.types.Rect;
import org.apache.flex.tools.FlexTool;
import org.apache.flex.utils.ArgumentUtil;
import org.apache.flex.utils.FilenameNormalization;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * @author Erik de Bruin
 * @author Michael Schmalle
 */
public class MXMLJSC implements JSCompilerEntryPoint, ProblemQueryProvider,
        FlexTool
{
    @Override
    public ProblemQuery getProblemQuery()
    {
        return problems;
    }

    /*
     * JS output type enumerations.
     */
    public enum JSOutputType
    {
        AMD("amd"),
        FLEXJS("flexjs"),
        GOOG("goog"),
        FLEXJS_DUAL("flexjs_dual"),
        FLEXJS_DITA("flexjs_dita"),
        JSC("jsc"),
        NODE("node");

        private String text;

        JSOutputType(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return this.text;
        }

        public static JSOutputType fromString(String text)
        {
            for (JSOutputType jsOutputType : JSOutputType.values())
            {
                if (text.equalsIgnoreCase(jsOutputType.text))
                    return jsOutputType;
            }
            return GOOG;
        }
    }

    /*
     * JS output type enumerations.
     */
    public enum JSTargetType
    {
        SWF("SWF"),
        JS_FLEX("JSFlex"),
        JS_FLEX_CORDOVA("JSFlexCordova"),
        //JS without the FlexJS framework
        JS_NATIVE("JS"),
        //Node.js application
        JS_NODE("JSNode"),
        //Node.js module
        JS_NODE_MODULE("JSNodeModule");

        private String text;

        JSTargetType(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return this.text;
        }

        public static JSTargetType fromString(String text)
        {
            for (JSTargetType jsTargetType : JSTargetType.values())
            {
                if (text.equalsIgnoreCase(jsTargetType.text))
                    return jsTargetType;
            }
            return JS_FLEX;
        }
    }

    /*
     * Exit code enumerations.
     */
    static enum ExitCode
    {
        SUCCESS(0),
        PRINT_HELP(1),
        FAILED_WITH_PROBLEMS(2),
        FAILED_WITH_ERRORS(3),
        FAILED_WITH_EXCEPTIONS(4),
        FAILED_WITH_CONFIG_PROBLEMS(5);

        ExitCode(int code)
        {
            this.code = code;
        }

        final int code;
        
        int getCode()
        {
        	return code;
        }
    }

    public static JSOutputType jsOutputType;

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
        System.out.println("MXMLJSC");
        for (String arg : args)
        	System.out.println(arg);
        final MXMLJSC mxmlc = new MXMLJSC();
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1e9 + " seconds");

        return exitCode;
    }

    protected Workspace workspace;
    protected FlexJSProject project;

    protected ProblemQuery problems;
    protected ISourceFileHandler asFileHandler;
    public JSConfiguration config;
    protected Configurator projectConfigurator;
    private ConfigurationBuffer configBuffer;
    private ICompilationUnit mainCU;
    protected ITarget target;
    protected ITargetSettings targetSettings;
    protected IJSApplication jsTarget;
    private IJSPublisher jsPublisher;
    protected MXMLC mxmlc;
    protected JSCompilerEntryPoint lastCompiler;
    public boolean noLink;
    public OutputStream err;
	public Class<? extends Configuration> configurationClass = JSGoogConfiguration.class;
    
    public MXMLJSC()
    {
        workspace = new Workspace();
        workspace.setASDocDelegate(new FlexJSASDocDelegate());
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

            	targetloop:
            	for (String target : config.getCompilerTargets())
            	{
            		int result = 0;
            		switch (JSTargetType.fromString(target))
	                {
	                case SWF:
	                    mxmlc = new MXMLC();
	                    mxmlc.configurationClass = configurationClass;
	                    if (noLink)
	                    	result = mxmlc.mainCompileOnly(removeJSArgs(args), err);
	                    else
	                    	result = mxmlc.mainNoExit(removeJSArgs(args));
	                    if (result != 0 && result != 2)
	                    {
	                    	problems.addAll(mxmlc.problems.getProblems());
	                    	break targetloop;
	                    }
	                    break;
	                case JS_FLEX:
	                	MXMLJSCFlex flex = new MXMLJSCFlex();
	                	lastCompiler = flex;
	                    result = flex.mainNoExit(removeASArgs(args), problems.getProblems(), false);
	                    if (result != 0 && result != 2)
	                    {
	                    	break targetloop;
	                    }
	                    break;
	                case JS_FLEX_CORDOVA:
	                	MXMLJSCFlexCordova flexCordova = new MXMLJSCFlexCordova();
	                	lastCompiler = flexCordova;
	                    result = flexCordova.mainNoExit(removeASArgs(args), problems.getProblems(), false);
	                    if (result != 0 && result != 2)
	                    {
	                    	break targetloop;
	                    }
	                    break;
	                case JS_NODE:
                        MXMLJSCNode node = new MXMLJSCNode();
                        lastCompiler = node;
                        result = node.mainNoExit(removeASArgs(args), problems.getProblems(), false);
                        if (result != 0 && result != 2)
                        {
                            break targetloop;
                        }
                        break;
                    case JS_NODE_MODULE:
                        MXMLJSCNodeModule nodeModule = new MXMLJSCNodeModule();
                        lastCompiler = nodeModule;
                        result = nodeModule.mainNoExit(removeASArgs(args), problems.getProblems(), false);
                        if (result != 0 && result != 2)
                        {
                            break targetloop;
                        }
                        break;
	                case JS_NATIVE:
	                	MXMLJSCNative jsc = new MXMLJSCNative();
	                	lastCompiler = jsc;
	                    result = jsc.mainNoExit(removeASArgs(args), problems.getProblems(), false);
	                    if (result != 0 && result != 2)
	                    {
	                    	break targetloop;
	                    }
	                    break;
	                // if you add a new js-output-type here, don't forget to also add it
	                // to flex2.tools.MxmlJSC in flex-compiler-oem for IDE support
	                }
            	}
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
    
    protected String[] removeJSArgs(String[] args)
    {
    	ArrayList<String> list = new ArrayList<String>();
    	for (String arg : args)
    	{
    		if (!(arg.startsWith("-compiler.targets") ||
    			  arg.startsWith("-closure-lib") ||
    			  arg.startsWith("-remove-circulars") ||
    			  arg.startsWith("-compiler.js-external-library-path") ||
    			  arg.startsWith("-compiler.js-library-path") ||
    			  arg.startsWith("-compiler.js-define") ||
    			  arg.startsWith("-js-output") ||
    			  arg.startsWith("-js-load-config") ||
    			  arg.startsWith("-source-map")))
    			list.add(arg);						
    	}
    	return list.toArray(new String[0]);
    }

    protected String[] removeASArgs(String[] args)
    {
    	ArrayList<String> list = new ArrayList<String>();
    	boolean hasJSLoadConfig = false;
    	for (String arg : args)
    	{
    		if (arg.startsWith("-js-load-config"))
    			hasJSLoadConfig = true;
    	}
    	if (!hasJSLoadConfig)
    		return args;
    	for (String arg : args)
    	{
    		if (!arg.startsWith("-load-config"))
    		{
    			if (arg.startsWith("-js-load-config"))
    				arg = arg.substring(3);
    			list.add(arg);	
    		}
    	}
    	return list.toArray(new String[0]);
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

                jsPublisher = (IJSPublisher) project.getBackend().createPublisher(
                        project, errors, config);

                File outputFolder = jsPublisher.getOutputFolder();

                if (!googConfiguration.getSkipTranspile())
                {
	                ArrayList<ICompilationUnit> roots = new ArrayList<ICompilationUnit>();
	                roots.add(mainCU);
	                Set<ICompilationUnit> incs = target.getIncludesCompilationUnits();
	                roots.addAll(incs);
	                project.mixinClassNames = new TreeSet<String>();
	                List<ICompilationUnit> reachableCompilationUnits = project.getReachableCompilationUnitsInSWFOrder(roots);
	                ((FlexJSTarget)target).collectMixinMetaData(project.mixinClassNames, reachableCompilationUnits);
	                for (final ICompilationUnit cu : reachableCompilationUnits)
	                {
	                    ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();
	
	                    if (cuType == ICompilationUnit.UnitType.AS_UNIT
	                            || cuType == ICompilationUnit.UnitType.MXML_UNIT)
	                    {
	                        final File outputClassFile = getOutputClassFile(
	                                cu.getQualifiedNames().get(0), outputFolder);
	
	                        System.out.println("Compiling file: " + outputClassFile);
	
	                        ICompilationUnit unit = cu;
	
	                        IJSWriter writer;
	                        if (cuType == ICompilationUnit.UnitType.AS_UNIT)
	                        {
	                            writer = (IJSWriter) project.getBackend().createWriter(project,
	                                    errors, unit, false);
	                        }
	                        else
	                        {
	                            writer = (IJSWriter) project.getBackend().createMXMLWriter(
	                                    project, errors, unit, false);
	                        }
	
	                        BufferedOutputStream out = new BufferedOutputStream(
	                                new FileOutputStream(outputClassFile));
	
	                        File outputSourceMapFile = null;
	                        if (project.config.getSourceMap())
	                        {
	                            outputSourceMapFile = getOutputSourceMapFile(
	                                    cu.getQualifiedNames().get(0), outputFolder);
	                        }
	                        
	                        writer.writeTo(out, outputSourceMapFile);
	                        out.flush();
	                        out.close();
	                        writer.close();
	                    }
	                }
                }
                
                if (jsPublisher != null)
                {
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

        return compilationSuccess;
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
     * Replaces FlexApplicationProject::buildSWF()
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
    public boolean configure(final String[] args)
    {
    	projectConfigurator = new Configurator(configurationClass);
    	
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

            // getCompilerProblemSettings initializes the configuration
            problems = new ProblemQuery(projectConfigurator.getCompilerProblemSettings());
            problems.addAll(projectConfigurator.getConfigurationProblems());
            config = (JSConfiguration) projectConfigurator.getConfiguration();
            configBuffer = projectConfigurator.getConfigurationBuffer();

            if (configBuffer.getVar("version") != null) //$NON-NLS-1$
            {
                System.out.println(VersionInfo.buildMessage());
                return false;
            }

            if (problems.hasErrors())
                return false;
            
            return true;
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
                config = new JSConfiguration();
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
        if (lastCompiler != null)
        	return lastCompiler.getSourceList();
        if (mxmlc != null)
        	return mxmlc.getSourceList();
        return null;
    }
    
    public String getMainSource()
    {
        if (lastCompiler != null)
        	return lastCompiler.getMainSource();
        if (mxmlc != null)
        	return mxmlc.getMainSource();
        return null;
    }
    

    /**
     * return a data structure for FB integration
     * @return
     */
    public ISWF getSWFTarget()
    {
    	SWF swf = new SWF();
    	Rect rect = new Rect(getTargetSettings().getDefaultWidth(),
    						getTargetSettings().getDefaultHeight());
    	swf.setFrameSize(rect);
    	// we might need to report actual color some day
    	swf.setBackgroundColor(new RGB(255, 255, 255));
    	swf.setTopLevelClass(config.getTargetFile());
    	return swf;
    }
    
    public long writeSWF(OutputStream output)
    {
    	if (mxmlc != null)
    		return mxmlc.writeSWF(output);
    	return 0;
    }
    
    /**
     * Determines whether an exit code should be considered
     * a fatal failure, such as for an Ant task.
     * 
     * @param code A numeric exit code.
     * @return <code>true</code> if the Ant task failed.
     */
    public static boolean isFatalFailure(final int code)
    {
        // This method really belongs in ExitCode
        // but that would complicate FlexTask.
        return code == ExitCode.FAILED_WITH_ERRORS.getCode() ||
               code == ExitCode.FAILED_WITH_EXCEPTIONS.getCode() ||
               code == ExitCode.FAILED_WITH_CONFIG_PROBLEMS.getCode();
    }

}
