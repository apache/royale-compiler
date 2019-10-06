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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.CountingOutputStream;

import org.apache.flex.tools.FlexTool;

import org.apache.royale.compiler.Messages;
import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.config.CommandLineConfigurator;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationBuffer;
import org.apache.royale.compiler.config.ConfigurationPathResolver;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.config.ICompilerProblemSettings;
import org.apache.royale.compiler.config.ICompilerSettingsConstants;
import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.config.RSLSettings.RSLAndPolicyFileURLPair;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.common.Counter;
import org.apache.royale.compiler.internal.config.FlashBuilderConfigurator;
import org.apache.royale.compiler.internal.config.localization.LocalizationManager;
import org.apache.royale.compiler.internal.graph.GraphMLWriter;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority.BasePriority;
import org.apache.royale.compiler.internal.projects.RoyaleProjectConfigurator;
import org.apache.royale.compiler.internal.targets.LinkageChecker;
import org.apache.royale.compiler.internal.targets.SWFTarget;
import org.apache.royale.compiler.internal.targets.Target;
import org.apache.royale.compiler.internal.units.ResourceModuleCompilationUnit;
import org.apache.royale.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.royale.compiler.internal.units.StyleModuleCompilationUnit;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ConfigurationProblem;
import org.apache.royale.compiler.problems.FileIOProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.problems.UnableToBuildSWFProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ISWFTarget;
import org.apache.royale.compiler.targets.ITargetReport;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;
import org.apache.royale.swf.io.ISWFWriterFactory;
import org.apache.royale.swf.Header;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.io.ISWFWriter;
import org.apache.royale.swf.io.SizeReportWritingSWFWriter;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * The entry-point class for mxmlc.
 */
public class MXMLC implements FlexTool
{
    static final String NEWLINE = System.getProperty("line.separator");
    private static final String SWF_EXT = ".swf";
    private static final String DEFAULT_VAR = "file-specs";
    private static final String L10N_CONFIG_PREFIX = "org.apache.royale.compiler.internal.config.configuration";

    /**
     * Exit code enumerations.
     */
    public static enum ExitCode
    {
        // NOTE: Negative error codes do not work on OSX.
        // Therefore the following enum values must be non-negative.
        SUCCESS(0),
        PRINT_HELP(1),
        FAILED_WITH_PROBLEMS(0),
        FAILED_WITH_ERRORS(3),
        FAILED_WITH_EXCEPTIONS(4),
        FAILED_WITH_CONFIG_ERRORS(5);

        ExitCode(int code)
        {
            assert code >= 0 : "Exit code must be non-negative";
            this.code = code;
        }

        final int code;
        
        public int getCode()
        {
            return code;
        }
    }

    /**
     * Entry point for the <code>mxmlc</code> tool.
     * 
     * @param args Command line arguments.
     */
    public static void main(final String[] args)
    {
        final int exitCode = staticMainNoExit(args);
        System.exit(exitCode);
    }
    
    /**
     * Entry point for the {@code <mxmlc>} Ant task.
     * 
     * @param args Command line arguments.
     * @return An exit code.
     */
    public static int staticMainNoExit(final String[] args)
    {
        final MXMLC mxmlc = new MXMLC();
        return mxmlc.mainNoExit(args);
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
               code == ExitCode.FAILED_WITH_CONFIG_ERRORS.getCode();
    }

    @Override
    public String getName() {
        return FLEX_TOOL_MXMLC;
    }

    @Override
    public int execute(String[] args) {
        return mainNoExit(args);
    }


    /**
     * Entry point for when you already have an MXMLC instance.
     * This is for unit testing.
     * 
     * @param args Command line arguments.
     * @return An exit code.
     */
    public int mainNoExit(final String[] args)
    {
        return mainNoExit(args, System.err);
    }

    /**
     * Entry point for when you already have an MXML instance and want
     * to redirect <code>System.err</code>. This is for unit testing.
     * 
     * @param args Command line arguments.
     * @param err An {@link OutputStream} to use instead of <code>System.err</code>.
     * @return An exit code.
     */
    @SuppressWarnings("unused")
    public int mainNoExit(final String[] args, OutputStream err)
    {
        startTime = System.nanoTime();
        
        ExitCode exitCode = ExitCode.SUCCESS;
        try
        {
            final boolean continueCompilation = configure(args);
            boolean legacyOutput = config.useLegacyMessageFormat();
            CompilerProblemCategorizer categorizer = null;
            
            if (legacyOutput)
                categorizer = createProblemCategorizer();
            
            ProblemFormatter formatter = new WorkspaceProblemFormatter(workspace, categorizer); 
            
            ProblemPrinter printer = new ProblemPrinter(formatter, err);
            
            if (continueCompilation)
            {
                if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
                	System.out.println("Configuration is ok");
                project.setProblems(problems.getProblems());
                compile();
                exitCode = printProblems(printer, legacyOutput);
                reportTargetCompletion();
            }
            else if (problems.hasFilteredProblems())
            {
                if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
                	System.out.println("Failed with config errors");
                printer.printProblems(problems.getFilteredProblems());
                exitCode = ExitCode.FAILED_WITH_CONFIG_ERRORS;
            }
            else
            {
                exitCode = ExitCode.PRINT_HELP;
            }
        }
        catch (Exception e)
        {
            if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
            	System.out.println("Failed with exceptions");
            (new PrintStream(err)).println(e.getMessage());
            exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
        }
        finally
        {
            waitAndClose();
            
            if (Counter.COUNT_TOKENS || Counter.COUNT_NODES ||
                Counter.COUNT_DEFINITIONS || Counter.COUNT_SCOPES)
            {
                Counter.getInstance().dumpCounts();
            }
        }
        return exitCode.code;
    }
    
    /**
     * Entry point for when you already have an MXML instance and just want to
     * compile and not link. This is for FB integration, but other IDEs could
     * use this too.
     * 
     * @param args Command line arguments.
     * @param err An {@link OutputStream} to use instead of <code>System.err</code>.
     * @return An exit code.
     */
    @SuppressWarnings("unused")
    public int mainCompileOnly(final String[] args, OutputStream err)
    {
        if (err == null)
            err = System.err;
        
        startTime = System.nanoTime();
        
        ExitCode exitCode = ExitCode.SUCCESS;
        try
        {
            final boolean continueCompilation = configure(args);
            boolean legacyOutput = config.useLegacyMessageFormat();
            CompilerProblemCategorizer categorizer = null;
            
            if (legacyOutput)
                categorizer = createProblemCategorizer();
            
            ProblemFormatter formatter = new WorkspaceProblemFormatter(workspace, categorizer); 
            
            ProblemPrinter printer = new ProblemPrinter(formatter, err);

            if (continueCompilation)
            {
                compile(true); // skip linking
                exitCode = printProblems(printer, legacyOutput);
                reportTargetCompletion();
            }
            else if (problems.hasFilteredProblems())
            {
                printer.printProblems(problems.getFilteredProblems());
                exitCode = ExitCode.FAILED_WITH_CONFIG_ERRORS;
            }
            else
            {
                exitCode = ExitCode.PRINT_HELP;
            }
        }
        catch (Exception e)
        {
            (new PrintStream(err)).println(e.getMessage());
            exitCode = ExitCode.FAILED_WITH_EXCEPTIONS;
        }
        finally
        {
            waitAndClose();
            
            if (Counter.COUNT_TOKENS || Counter.COUNT_NODES ||
                Counter.COUNT_DEFINITIONS || Counter.COUNT_SCOPES)
            {
                Counter.getInstance().dumpCounts();
            }
        }
        return exitCode.code;
    }

    /** 
     * Print the problems in either the legacy format or the new format.
     * 
     * @param printer
     * @param legacyOutput
     * @return ExitCode
     */
    private ExitCode printProblems(ProblemPrinter printer, boolean legacyOutput)
    {
        ExitCode exitCode = ExitCode.SUCCESS;
        
        if (legacyOutput)
        {
            if (printer.printProblems(problems.getFilteredProblems()) > 0)
            {
                if (problems.hasErrors())
                    exitCode = ExitCode.FAILED_WITH_ERRORS;       
            }
        }
        else
        {
            Collection<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
            Collection<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();
            
            problems.getErrorsAndWarnings(errors, warnings);
            
            int errorCount = errors.size();
            int warningCount = warnings.size();
            if (warningCount > 0)
            {
                System.err.println(Messages.getString("MXMLC.WarningsHeader"));
                printer.printProblems(warnings);                    

            }
            
            if (errorCount > 0)
            {
                System.err.println(Messages.getString("MXMLC.ErrorsHeader"));
                printer.printProblems(errors);
            }
            
            // Output summary of errors and warnings
            if (errorCount == 1)
                System.err.println(Messages.getString("MXMLC.1_error"));
            else if (errorCount > 0)
                System.err.println(Messages.getString("MXMLC.multiple_errors_format", 
                        Collections.<String,Object>singletonMap("errorCount", errors.size())));

            if (warningCount == 1)
                System.err.println(Messages.getString("MXMLC.1_warning"));
            else if (warningCount > 0)
                System.err.println(Messages.getString("MXMLC.multiple_warnings_format", 
                        Collections.<String,Object>singletonMap("warningCount", warnings.size())));
            
            if (errorCount > 0)
                exitCode = ExitCode.FAILED_WITH_ERRORS;
        }
        
        return exitCode;
    }

    public MXMLC()
    {
        workspace = new Workspace();
        project = new RoyaleProject(workspace);
        problems = new ProblemQuery();
    }

    protected Workspace workspace;
    protected RoyaleProject project;
    public Configuration config;
    public ProblemQuery problems;
    public ConfigurationBuffer configBuffer;

	public Class<? extends Configuration> configurationClass = Configuration.class;
    protected Configurator projectConfigurator;

    protected ICompilationUnit mainCU;
    protected SWFTarget target;
    protected long startTime;     // start time of execution in nanoseconds
    protected ITargetSettings targetSettings;
    private ISWF swfTarget;
    private String swfOutputMessage;
    
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
            workspace.endIdleState(Collections.<ICompilerProject, Set<ICompilationUnit>>emptyMap());
        }
    }

    /**
     * Force terminate the compilation process.
     */
    protected void close()
    {
        workspace.close();
    }

    /**
     * Create a new Configurator. This method may be overridden to allow
     * Configurator subclasses to be created that have custom configurations.
     * 
     * @return a new instance or subclass of {@link Configurator}. 
     * 
     */
	protected Configurator createConfigurator()
    {
        return new RoyaleProjectConfigurator(configurationClass);
    }
    
    /**
     * Load configurations from all the sources.
     * 
     * @param args command line arguments
     * @return True if mxmlc should continue with compilation.
     */
    public boolean configure(final String[] args)
    {
        projectConfigurator = createConfigurator();
        
        try
        {
            // Print brief usage if no arguments provided.
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

                // Create a default configuration so we can exit gracefully.
                config = new Configuration();
                configBuffer = new ConfigurationBuffer(
                        Configuration.class, Configuration.getAliases());
                return false;
            }

            ConfigurationPathResolver resolver = new ConfigurationPathResolver(System.getProperty("user.dir")); 
            projectConfigurator.setConfigurationPathResolver(resolver);
            projectConfigurator.setWarnOnRoyaleOnlyOptionUsage(false);
            if (useFlashBuilderProjectFiles(args))
                projectConfigurator.setConfiguration(FlashBuilderConfigurator.computeFlashBuilderArgs(args, getTargetType().getExtension()), 
                                                        getConfigurationDefaultVariable());
            else
                projectConfigurator.setConfiguration(args, getConfigurationDefaultVariable());
            projectConfigurator.applyToProject(project);
            getTargetSettings();    // get targetSettings here to flush out any configuration problems.
            problems = new ProblemQuery(projectConfigurator.getCompilerProblemSettings());
            
            // Get the configuration and configBuffer which are now initialized.
            config = projectConfigurator.getConfiguration();
            Messages.setLocale(config.getToolsLocale());
            project.apiReportFile = config.getApiReport();
            configBuffer = projectConfigurator.getConfigurationBuffer();
            problems.addAll(projectConfigurator.getConfigurationProblems());

            // Print version if "-version" is present.
            if (configBuffer.getVar("version") != null)
            {
                println(VersionInfo.buildMessage());
                return false;
            }

            // Print help if "-help" is present.
            final List<ConfigurationValue> helpVar = configBuffer.getVar("help");
            if (helpVar != null)
            {
                processHelp(helpVar);
                return false;
            }
            
            for (String fileName : projectConfigurator.getLoadedConfigurationFiles())
            {
                println(Messages.getString("MXMLC.Loading_configuration_format", 
                        Collections.<String,Object>singletonMap("configurationName", fileName)));                
            }
            
            // Add a blank line between the configuration list and the rest of 
            // the output to make the start of the output easier to detect.
            println(""); 
            
            if (config.isVerbose())
            {
                for (final IFileSpecification themeFile : project.getThemeFiles())
                {
                    println(Messages.getString("MXMLC.Found_theme_file_format", 
                            Collections.<String, Object>singletonMap("themePath", 
                                    themeFile.getPath())));
                }
            }

            // If we have configuration errors then exit before trying to 
            // validate the target.
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
            final ICompilerProblem problem = new ConfigurationProblem(null, -1, -1, -1, -1, e.getMessage());
            problems.add(problem);
            return false;
        }
    }

    /**
     * Get the default variable for this configuration. MXMLC has a default 
     * variable of "file-spec" and COMPC has default variable of 
     * "include-classes".
     * 
     * @return the default variable for the configuration.
     */
    protected String getConfigurationDefaultVariable()
    {
        return ICompilerSettingsConstants.FILE_SPECS_VAR;
    }
    
    private boolean useFlashBuilderProjectFiles(String[] args)
    {
        for (String arg : args)
        {
            if (arg.equals("-fb") || arg.equals("-use-flashbuilder-project-files"))
                return true;
        }
        return false;
    }
    
    /**
     * Validate target file.
     * 
     * @throws ConfigurationException
     */
    protected void validateTargetFile() throws ConfigurationException
    {
        if(mainCU instanceof ResourceModuleCompilationUnit)
            return; //when compiling a Resource Module, no target file is defined.
        
        final String targetFile = config.getTargetFile();
        if (targetFile == null)
            throw new ConfigurationException.MustSpecifyTarget(null, null, -1);

        final File file = new File(targetFile);
        if (!file.exists())
            throw new ConfigurationException.IOError(targetFile);
    }

    /**
     * Main body of this program. This method is called from the public static
     * method's for this program.
     * 
     * @return true if compiler succeeds
     */
    protected boolean compile()
    {
        return compile(false);
    }
    
    private boolean compile(boolean skipLinking)
    {
        boolean compilationSuccess = false;
        try
        {
            if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
            	System.out.println("Setting up target file");
            if (!setupTargetFile())
            {
                if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
                	System.out.println("Could not set up target file");
                return false;
            }
            
            if (config.isDumpAst())
                dumpAST();

            buildArtifact();
            project.generateAPIReport();

            if (swfTarget == null)
            {
                if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
                	System.out.println("No swftarget");
                return false;
            }
            
            // Don't create a swf if there are errors unless a 
            // developer requested otherwise.
            if (!config.getCreateTargetWithErrors() && problems.hasErrors())
            {
                if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
                	System.out.println("got errors creating target");
                return false;
            }

            if (skipLinking)
                return true;
            if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
            	System.out.println("attempting to write output");
            final File outputFile = new File(getOutputFilePath());
            final int swfSize = writeSWF(swfTarget, outputFile);
            long endTime = System.nanoTime();
            String seconds = String.format("%5.3f", (endTime - startTime) / 1e9);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("byteCount", swfSize);
            params.put("path", outputFile.getCanonicalPath());
            params.put("seconds", seconds);
            swfOutputMessage = Messages.getString("MXMLC.bytes_written_to_file_in_seconds_format",
                    params);
            dumpDependencyGraphIfNeeded();
            compilationSuccess = true;
        }
        catch (IOException e)
        {
            if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
            	System.out.println("got IOException in compile()");
            final FileIOProblem problem = new FileIOProblem(e);
            problems.add(problem);
        }
        catch (Exception e)
        {
            if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COMPC_PHASES) == CompilerDiagnosticsConstants.COMPC_PHASES)
            	System.out.println("got Exception in compile()");
            final ICompilerProblem problem = new InternalCompilerProblem(e);
            problems.add(problem);
        }

        return compilationSuccess;
    }

    /**
     * Setup theme files.
     */
    protected void setupThemeFiles()
    {
        project.setThemeFiles(toFileSpecifications(config.getCompilerThemeFiles(), workspace));

        if (config.isVerbose())
        {
            for (final IFileSpecification themeFile : project.getThemeFiles())
            {
                verboseMessage(Messages.getString("MXMLC.Found_theme_file_format", 
                        Collections.<String,Object>singletonMap("themePath", themeFile.getPath())));
            }
        }
    }

    /**
     * Reports the size and location of the target that was created.
     * @throws InterruptedException 
     * 
     */
    protected void reportTargetCompletion() throws InterruptedException
    {
        if (swfOutputMessage != null)
        {
            reportRequiredRSLs(target);
            println(swfOutputMessage);
        }
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

    /**
     * Parse all source files and dumpAST
     * 
     * @throws InterruptedException
     */
    private void dumpAST() throws InterruptedException
    {
        final List<String> astDump = new ArrayList<String>();
        final Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        final ImmutableList<ICompilationUnit> compilationUnits = target.getReachableCompilationUnits(problems);
        for (final ICompilationUnit compilationUnit : compilationUnits)
        {
            final IASNode ast = compilationUnit.getSyntaxTreeRequest().get().getAST();
            if (ast != null)
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.FILE_NODE) == CompilerDiagnosticsConstants.FILE_NODE)
            		System.out.println("MXMLC waiting for lock in populateFunctionNodes");
                ((IFileNode)ast).populateFunctionNodes();
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.FILE_NODE) == CompilerDiagnosticsConstants.FILE_NODE)
            		System.out.println("MXMLC done with lock in populateFunctionNodes");
                astDump.add(ast.toString());
            }
        }

        println(Joiner.on("\n\n").join(astDump));
    }

    /**
     * Build target artifact.
     * 
     * @throws InterruptedException threading error
     * @throws IOException IO error
     */
    protected void buildArtifact() throws InterruptedException, IOException
    {
        swfTarget = buildSWFModel();
    }

    /**
     * Build SWF model object and collect problems building SWF in
     * {@link #problems}.
     * 
     * @return SWF model or null if SWF can't be built.
     * @throws InterruptedException concurrency problem
     */
    private ISWF buildSWFModel() throws InterruptedException
    {
        final List<ICompilerProblem> problemsBuildingSWF =
                new ArrayList<ICompilerProblem>();
        final ISWF swf = target.build(problemsBuildingSWF);
        problems.addAll(problemsBuildingSWF);
        if (swf == null)
        {
            ICompilerProblem problem = new UnableToBuildSWFProblem(getOutputFilePath());
            problems.add(problem);
        }
        
        return swf;
    }

    private void reportRequiredRSLs(ISWFTarget target) throws InterruptedException
    {
        // Report the required RSLs:
        if (hasRSLs())
        {
            ITargetReport report = target.getTargetReport();
            
            if (report == null)
                return;     // target must not have been built.
            
            List<RSLSettings> requiredRSLs = report.getRequiredRSLs();
            List<String> legacyRSLs = targetSettings.getRuntimeSharedLibraries();
            
            if (requiredRSLs.isEmpty() && legacyRSLs.isEmpty())
                return;
            
            println(Messages.getString("MXMLC.Required_RSLs"));                
            
            // loop thru the RSLs and print out the required RSLs.
            for (RSLSettings rslSettings : requiredRSLs)
            {
                List<RSLAndPolicyFileURLPair>rslURLs = rslSettings.getRSLURLs();
                Map<String,Object> params = new HashMap<String,Object>();
                params.put("rslPath",rslURLs.get(0).getRSLURL());
                
                switch (rslURLs.size())
                {
                    case 0:
                        assert false; // One RSL URL is required.
                        break;
                    case 1:
                        println(Messages.getString("MXMLC.required_rsl_url_format", 
                                params));
                        break;
                    case 2:
                        println(Messages.getString("MXMLC.required_rsl_url_with_1_failover_format",
                                params));
                        break;
                    default:
                        params.put("failoverCount", rslURLs.size() - 1);
                        println(Messages.getString("MXMLC.required_rsl_url_with_multiple_failovers_format", 
                                params));
                        break;
                }

            }
            
            // All -runtime-shared-libraries are required
            for (String rslURL : legacyRSLs)
                println(Messages.getString("MXMLC.required_rsl_url_format", 
                        Collections.<String,Object>singletonMap("rslPath", rslURL)));
        }
    }

    /**
     * Virtual method that returns the type of target we are building.
     * Subclasses will override this method to return different target types.
     * 
     * @return The {@link TargetType} of the target we are building.
     */
    protected TargetType getTargetType()
    {
        return TargetType.SWF;
    }
    
    private ITargetSettings getTargetSettings()
    {
        if (targetSettings == null)
            targetSettings = projectConfigurator.getTargetSettings(getTargetType());
        
        return targetSettings;
    }

    private boolean hasRSLs()
    {
        return (getTargetSettings().getRuntimeSharedLibraryPath().size() > 0) ||
               (getTargetSettings().getRuntimeSharedLibraryPath().size() > 0);
    }

    /**
     * Write out SWF file and return file size in bytes.
     * 
     * @param swf SWF model
     * @param outputFile output SWF file handle
     * @return SWF file size in bytes
     * @throws IOException error
     */
    private int writeSWF(final ISWF swf, final File outputFile) throws IOException
    {
  
        final Header.Compression compression = Header.decideCompression(
                targetSettings.useCompression(), 
                targetSettings.getSWFVersion(),
                targetSettings.isDebugEnabled());
        final ISWFWriterFactory writerFactory = SizeReportWritingSWFWriter.getSWFWriterFactory(
                targetSettings.getSizeReport()); 
        final ISWFWriter writer = writerFactory.createSWFWriter(swf, compression,
                targetSettings.isDebugEnabled(), targetSettings.isTelemetryEnabled());
        
        return writer.writeTo(outputFile);
    }

    /**
     * MXMLC uses target file as the main compilation unit and derive the output
     * SWF file name from this file.
     *
     * @return true if successful, false otherwise.
     * 
     * @throws InterruptedException
     */
    protected boolean setupTargetFile() throws InterruptedException
    {
        final String mainFileName = config.getTargetFile();

        if(mainFileName != null)
        {
            final String normalizedMainFileName = FilenameNormalization.normalize(mainFileName);
            
            // Can not add a SourceHandler for *.css file because we don't want
            // to create compilation units for CSS files on the source path.
            if (mainFileName.toLowerCase().endsWith(".css"))
            {
                mainCU = new StyleModuleCompilationUnit(
                        project, 
                        workspace.getFileSpecification(normalizedMainFileName), 
                        BasePriority.SOURCE_LIST);
                // TODO: Use CSS file name once CSS module runtime code is finalized.
                config.setMainDefinition("CSSModule2Main");
                project.addCompilationUnitsAndUpdateDefinitions(
                        Collections.singleton(mainCU));
            }
            else
            {
                final SourceCompilationUnitFactory compilationUnitFactory =
                    project.getSourceCompilationUnitFactory();
                File normalizedMainFile = new File(normalizedMainFileName);
                if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
                {
                    // Remove the main file from the source path and put it on the 
                    // source list. The only reason this needs to be done is to
                    // prevent the compilation unit of the main file from shadowing
                    // another compilation unit with the same qname. This can 
                    // happen in the odd case where you have test.mxml (main file)
                    // and test.as in the same directory and test.mxml's compilation
                    // unit end up shadowing test.as's cu.
                    project.removeSourceFile(normalizedMainFile);
                    project.addIncludeSourceFile(normalizedMainFile, true);
                    
                    Collection<ICompilationUnit> mainFileCompilationUnits =
                        workspace.getCompilationUnits(normalizedMainFileName, project);
                    
                    assert mainFileCompilationUnits.size() == 1;
                    mainCU = Iterables.getOnlyElement(mainFileCompilationUnits);
                }
            }
        }
        else 
        {
            final List<ICompilerProblem> resourceBundleProblems = new ArrayList<ICompilerProblem>();
            Collection<ICompilationUnit> includedResourceBundles = target.getIncludedResourceBundlesCompilationUnits(resourceBundleProblems);
            problems.addAll(resourceBundleProblems);

            if(includedResourceBundles.size() > 0)
            {
                //This means that a Resource Module is requested to be built.
                mainCU = new ResourceModuleCompilationUnit(project, 
                        "GeneratedResourceModule", 
                        includedResourceBundles, 
                        BasePriority.SOURCE_LIST);
                config.setMainDefinition("GeneratedResourceModule");
                project.addCompilationUnitsAndUpdateDefinitions(
                        Collections.singleton(mainCU));
            }
        }
                
        Preconditions.checkNotNull(mainCU, "Main compilation unit can't be null");

        if (getTargetSettings() == null)
            return false;            
        
        target = (SWFTarget)project.createSWFTarget(getTargetSettings(), null);
        
        return true;
    }

    /**
     * Setups the locale related settings.
     */
    protected void setupLocaleSettings()
    {
        project.setLocales(config.getCompilerLocales());
        project.setLocaleDependentResources(config.getLocaleDependentSources());
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
            return FilenameUtils.removeExtension(config.getTargetFile()).concat(SWF_EXT);
        else
            return config.getOutput();
    }

    private void verboseMessage(String s)
    {
        if (config.isVerbose())
            println(s);
    }

    /**
     * Convert file path strings to {@code File} objects. Null values are
     * discarded.
     * 
     * @param paths list of paths.
     * @return List of File objects. No null values will be returned.
     */
    public static List<File> toFiles(final List<String> paths)
    {
        final List<File> result = new ArrayList<File>();
        for (final String path : paths)
        {
            if (path != null)
                result.add(new File(path));
        }
        return result;
    }

    /**
     * Resolve a list of normalized paths to {@link IFileSpecification} objects
     * from the given {@code workspace}.
     * 
     * @param paths A list of normalized paths.
     * @param workspace Workspace.
     * @return A list of file specifications.
     */
    public static List<IFileSpecification> toFileSpecifications(
            final List<String> paths,
            final Workspace workspace)
    {
        return Lists.transform(paths, new Function<String, IFileSpecification>()
        {
            @Override
            public IFileSpecification apply(final String path)
            {
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
            		System.out.println("MXMLC waiting for lock in toFileSpecifications");
                IFileSpecification ret = workspace.getFileSpecification(path);
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.WORKSPACE) == CompilerDiagnosticsConstants.WORKSPACE)
            		System.out.println("MXMLC waiting for lock in toFileSpecifications");
            	return ret;
            }
        });
    }

    /**
     * Get my program name.
     * 
     * @return always "mxmlc".
     */
    protected String getProgramName()
    {
        return "mxmlc";
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

    /**
     * "compc" subclass will override this method.
     * 
     * @return False if the client is not "compc".
     */
    protected boolean isCompc()
    {
        return false;
    }
    
    private void dumpDependencyGraphIfNeeded() throws IOException, InterruptedException
    {
        File dependencyGraphOutput = config.getDependencyGraphOutput();
        if (dependencyGraphOutput != null)
        {
            LinkageChecker linkageChecker = new LinkageChecker(project, getTargetSettings());
            Target.RootedCompilationUnits rootedCompilationUnits = target.getRootedCompilationUnits();
            problems.addAll(rootedCompilationUnits.getProblems());
            GraphMLWriter dependencyGraphWriter = 
                new GraphMLWriter(project.getDependencyGraph(), 
                        rootedCompilationUnits.getUnits(), true,
                        linkageChecker);
            BufferedOutputStream graphStream = new BufferedOutputStream(new FileOutputStream(dependencyGraphOutput));
            LinkedList<ICompilerProblem> problemList = new LinkedList<ICompilerProblem>();
            Iterables.addAll(problemList, rootedCompilationUnits.getProblems());
            dependencyGraphWriter.writeToStream(graphStream, problemList);
            problems.addAll(problemList);
        }
    }
    
    public ProblemQuery getProblems()
    {
        return problems;
    }

    public List<String> getSourceList()
    {
        ArrayList<String> list = new ArrayList<String>();
        LinkedList<ICompilerProblem> problemList = new LinkedList<ICompilerProblem>();
        try
        {
            ImmutableList<ICompilationUnit> units = target.getReachableCompilationUnits(problemList);
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
    	if (mainCU == null) return "";
        return mainCU.getAbsoluteFilename();
    }
    
    public ISWF getSWFTarget()
    {
        return swfTarget;
    }
    
    public int writeSWF(OutputStream outputStream)
    {
        
        final Header.Compression compression = Header.decideCompression(
                targetSettings.useCompression(), 
                targetSettings.getSWFVersion(),
                targetSettings.isDebugEnabled());
        final ISWFWriterFactory writerFactory = SizeReportWritingSWFWriter.getSWFWriterFactory(
                targetSettings.getSizeReport()); 
        final ISWFWriter writer = writerFactory.createSWFWriter(swfTarget, compression,
                targetSettings.isDebugEnabled(), targetSettings.isTelemetryEnabled());
        
        // Write out the SWF, counting how many bytes were written.
        final CountingOutputStream output =
                new CountingOutputStream(outputStream);

        writer.writeTo(output);
        final int swfSize = output.getCount();
        return swfSize;
    }
}
