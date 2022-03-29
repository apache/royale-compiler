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

import java.io.File;
import java.io.OutputStream;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.ProblemQueryProvider;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.config.CommandLineConfigurator;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationBuffer;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.config.ICompilerProblemSettings;
import org.apache.royale.compiler.config.ICompilerSettingsConstants;
import org.apache.royale.compiler.driver.js.IJSApplication;
import org.apache.royale.compiler.exceptions.ConfigurationException.OnlyOneSource;
import org.apache.royale.compiler.internal.config.FlashBuilderConfigurator;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.parsing.as.RoyaleASDocDelegate;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.RoyaleProjectConfigurator;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITarget;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.SWF;
import org.apache.royale.swf.types.RGB;
import org.apache.royale.swf.types.Rect;
import org.apache.flex.tools.FlexTool;
import org.apache.royale.utils.ArgumentUtil;
import org.apache.royale.utils.FilenameNormalization;

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
    
    static final String NEWLINE = System.getProperty("line.separator");
    private static final String DEFAULT_VAR = "file-specs";
    private static final String L10N_CONFIG_PREFIX = "org.apache.royale.compiler.internal.config.configuration";

    /*
     * JS output type enumerations.
     */
    public enum JSOutputType
    {
        AMD("amd"),
        ROYALE("royale"),
        GOOG("goog"),
        ROYALE_DUAL("royale_dual"),
        ROYALE_DITA("royale_dita"),
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
        JS_ROYALE("JSRoyale"),
        JS_ROYALE_CORDOVA("JSRoyaleCordova"),
        //JS without the Royale framework
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
            return null;
        }
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

    /**
     * Get my program name.
     * 
     * @return always "mxmlc".
     */
    protected String getProgramName()
    {
        return "mxmljsc";
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
    protected RoyaleJSProject project;

    protected ProblemQuery problems;
    protected ISourceFileHandler asFileHandler;
    public JSConfiguration config;
    protected Configurator projectConfigurator;
    private ConfigurationBuffer configBuffer;
    private ICompilationUnit mainCU;
    protected ITarget target;
    protected ITargetSettings targetSettings;
    protected IJSApplication jsTarget;
    protected MXMLC mxmlc;
    protected JSCompilerEntryPoint lastCompiler;
    public boolean noLink;
    public OutputStream err;
	public Class<? extends Configuration> configurationClass = JSGoogConfiguration.class;
    
    public MXMLJSC()
    {
        DefinitionBase.setPerformanceCachingEnabled(true);
        workspace = new Workspace();
        workspace.setASDocDelegate(new RoyaleASDocDelegate());
        project = new RoyaleJSProject(workspace, null);
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
                            workspace, createProblemCategorizer());
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
        	CompilerDiagnosticsConstants.diagnostics = config.getDiagnosticsLevel();

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
	                case JS_ROYALE:
	                	MXMLJSCRoyale royale = new MXMLJSCRoyale();
	                	lastCompiler = royale;
	                    result = royale.mainNoExit(removeASArgs(args), problems.getProblems(), false);
	                    if (result != 0 && result != 2)
	                    {
	                    	break targetloop;
	                    }
	                    break;
	                case JS_ROYALE_CORDOVA:
	                	MXMLJSCRoyaleCordova royaleCordova = new MXMLJSCRoyaleCordova();
	                	lastCompiler = royaleCordova;
	                    result = royaleCordova.mainNoExit(removeASArgs(args), problems.getProblems(), false);
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
    			  arg.startsWith("-js-vector-emulation-class") ||
    			  arg.startsWith("-externs-report") ||
    			  arg.startsWith("-js-load-config") ||
    			  arg.startsWith("-warn-public-vars") ||
    			  arg.startsWith("-export-protected-symbols") ||
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
    	projectConfigurator = new RoyaleProjectConfigurator(configurationClass);
    	
        try
        {
            if (args.length == 0)
            {
                final String usage = CommandLineConfigurator.brief(
                        getProgramName(),
                        DEFAULT_VAR,
                        LocalizationManager.get(),
                        L10N_CONFIG_PREFIX);
                println(getStartMessage());
                if (usage != null)
                    println(usage);
            }
            
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
            
            // Print help if "-help" is present.
            final List<ConfigurationValue> helpVar = configBuffer.getVar("help");
            if (helpVar != null)
            {
                processHelp(helpVar);
                return false;
            }

            for(String target : config.getCompilerTargets())
            {
                JSTargetType jsTargetType = JSTargetType.fromString(target);
                if (jsTargetType == null)
                {
                    String message = "configuration variable 'targets' must be one of the following: ";
                    for (JSTargetType type : JSTargetType.values())
                    {
                        message += "'" + type.text + "', ";
                    }
                    message += "got '" + target + "'";
                    final ICompilerProblem problem = new ConfigurationProblem(null, -1,
                            -1, -1, -1, message);
                    problems.add(problem);
                }
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

    /**
     * Print a message.
     * 
     * @param msg Message text.
     */
    public void println(final String msg)
    {
        System.out.println(msg);
    }
    
    /**
     * Get the start up message that contains the program name 
     * with the copyright notice.
     * 
     * @return The startup message.
     */
    protected String getStartMessage()
    {
        // This message should not be localized.
        String message = "Apache Royale MXML and ActionScript Compiler (mxmlc)" + NEWLINE +
            VersionInfo.buildMessage() + NEWLINE;
        return message;
    }

    /**
     * Print detailed help information if -help is provided.
     */
    private void processHelp(final List<ConfigurationValue> helpVar)
    {
        final Set<String> keywords = new LinkedHashSet<String>();
        for (final ConfigurationValue val : helpVar)
        {
            for (final Object element : val.getArgs())
            {
                String keyword = (String)element;
                while (keyword.startsWith("-"))
                    keyword = keyword.substring(1);
                keywords.add(keyword);
            }
        }

        if (keywords.size() == 0)
            keywords.add("help");

        final String usages = CommandLineConfigurator.usage(
                    getProgramName(),
                    DEFAULT_VAR,
                    configBuffer,
                    keywords,
                    LocalizationManager.get(),
                    L10N_CONFIG_PREFIX);
        println(getStartMessage());
        println(usages);
    }
    
    protected TargetType getTargetType()
    {
        return TargetType.SWF;
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
        // but that would complicate RoyaleTask.
        return code == ExitCode.FAILED_WITH_ERRORS.getCode() ||
               code == ExitCode.FAILED_WITH_EXCEPTIONS.getCode() ||
               code == ExitCode.FAILED_WITH_CONFIG_PROBLEMS.getCode();
    }

    /**
     * Set up any user defines customization of the problem severities.
     * 
     */
    private CompilerProblemCategorizer createProblemCategorizer()
    {
        ICompilerProblemSettings problemSettings = null;
        try
        {
            problemSettings = projectConfigurator.getCompilerProblemSettings();
        }
        catch (Exception e)
        {
            // Create a categorizer that will only use default settings.
        }

        return new CompilerProblemCategorizer(problemSettings);
    }

}
