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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.flex.compiler.clients.problems.ProblemPrinter;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.common.VersionInfo;
import org.apache.flex.compiler.config.*;
import org.apache.flex.compiler.config.RSLSettings.RSLAndPolicyFileURLPair;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.exceptions.ConfigurationException.IOError;
import org.apache.flex.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.flex.compiler.exceptions.ConfigurationException.OnlyOneSource;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.as.codegen.*;
import org.apache.flex.compiler.internal.config.localization.LocalizationManager;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.driver.IBackend;
import org.apache.flex.compiler.internal.driver.JSBackend;
import org.apache.flex.compiler.internal.driver.JSTarget;
import org.apache.flex.compiler.internal.graph.GoogDepsWriter;
import org.apache.flex.compiler.internal.graph.GraphMLWriter;
import org.apache.flex.compiler.internal.projects.*;
import org.apache.flex.compiler.internal.projects.DefinitionPriority.BasePriority;
import org.apache.flex.compiler.internal.resourcebundles.ResourceBundleUtils;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.scopes.ASProjectScope.DefinitionPromise;
import org.apache.flex.compiler.internal.targets.LinkageChecker;
import org.apache.flex.compiler.internal.targets.Target;
import org.apache.flex.compiler.internal.tree.mxml.MXMLClassDefinitionNode;
import org.apache.flex.compiler.internal.units.*;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.problems.*;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.targets.ISWFTarget;
import org.apache.flex.compiler.targets.ITarget.TargetType;
import org.apache.flex.compiler.targets.ITargetReport;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.ICompilationUnit.UnitType;
import org.apache.flex.compiler.units.requests.IFileScopeRequestResult;
import org.apache.flex.swc.ISWC;
import org.apache.flex.swf.ISWF;
import org.apache.flex.swf.io.ISWFWriter;
import org.apache.flex.swf.types.Rect;
import org.apache.flex.utils.ArgumentUtil;
import org.apache.flex.utils.FilenameNormalization;

import java.io.*;
import java.util.*;

/**
 * The entry-point class for the FalconJS version of mxmlc.
 */
public class MXMLJSC
{
    private static final String DEFAULT_VAR = "file-specs";
    private static final String L10N_CONFIG_PREFIX = "flex2.configuration";
    private static final int TWIPS_PER_PIXEL = 20;

    /*
     * Exit code enumerations.
     */
    static enum ExitCode
    {
        SUCCESS(0),
        PRINT_HELP(1),
        FAILED_WITH_PROBLEMS(2),
        FAILED_WITH_EXCEPTIONS(3),
        FAILED_WITH_CONFIG_PROBLEMS(4);

        ExitCode(int code)
        {
            this.code = code;
        }

        final int code;
    }

    /**
     * Java program entry point.
     * 
     * @param args command line arguments
     */
    public static void main(final String[] args)
    {
        long startTime = System.nanoTime();

        final IBackend backend = new JSBackend();
        final MXMLJSC mxmlc = new MXMLJSC(backend);
        final Set<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        JSSharedData.instance.stdout((endTime - startTime) / 1e9 + " seconds");

        System.exit(exitCode);
    }

    public static int mainNoExit(final String[] args, List<ICompilerProblem> problemList)
    {
        final IBackend backend = new JSBackend();
        final MXMLJSC mxmlc = new MXMLJSC(backend);
        final Set<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, problemList == null);
        if (problemList != null)
            problemList.addAll(problems);
        return exitCode;
    }

    public int mainNoExit(final String[] args, Set<ICompilerProblem> problems, Boolean printProblems)
    {
        int exitCode = -1;
        try
        {
            exitCode = _mainNoExit(ArgumentUtil.fixArgs(args), problems);
        }
        catch (Exception e)
        {
            JSSharedData.instance.stderr(e.toString());
        }
        finally
        {
            if (problems != null && !problems.isEmpty())
            {
                if (printProblems)
                {
                    final WorkspaceProblemFormatter formatter = new WorkspaceProblemFormatter(workspace);
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
    private int _mainNoExit(final String[] args, Set<ICompilerProblem> outProblems)
    {
        ExitCode exitCode = ExitCode.SUCCESS;
        try
        {
            final boolean continueCompilation = configure(args);

            if (outProblems != null && !config.isVerbose())
                JSSharedData.STDOUT = JSSharedData.STDERR = null;

            if (continueCompilation)
            {
                compile();
                if (problems.hasFilteredProblems())
                    exitCode = ExitCode.FAILED_WITH_PROBLEMS;
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
            if (outProblems == null)
                JSSharedData.instance.stderr(e.getMessage());
            else
            {
                final ICompilerProblem unexpectedExceptionProblem = new UnexpectedExceptionProblem(e);
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

    protected MXMLJSC(IBackend backend)
    {
        JSSharedData.backend = backend;
        workspace = new Workspace();
        project = new FlexJSProject(workspace);
        MXMLClassDefinitionNode.GENERATED_ID_BASE = "$ID";
        problems = new ProblemQuery();
        JSSharedData.OUTPUT_EXTENSION = backend.getOutputExtension();
        JSSharedData.workspace = workspace;
        asFileHandler = backend.getSourceFileHandlerInstance();
    }

    protected Workspace workspace;
    protected FlexProject project;
    protected Configuration config;
    protected ProblemQuery problems;
    private ConfigurationBuffer configBuffer;

    protected Configurator projectConfigurator;

    protected ICompilationUnit mainCU;
    protected JSTarget target;
    private ITargetSettings targetSettings;
    private ISWF swfTarget;

    private Collection<ICompilationUnit> includedResourceBundleCompUnits;
    protected ISourceFileHandler asFileHandler;

    /**
     * Print a message.
     * 
     * @param msg Message text.
     */
    public void println(final String msg)
    {
        JSSharedData.instance.stdout(msg);
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

    /**
     * Create a new Configurator. This method may be overridden to allow
     * Configurator subclasses to be created that have custom configurations.
     * 
     * @return a new instance or subclass of {@link Configurator}.
     */
    protected Configurator createConfigurator()
    {
        return JSSharedData.backend.createConfigurator();
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
        CodeGeneratorManager.setFactory(JSGenerator.getABCGeneratorFactory());
        projectConfigurator = createConfigurator();

        try
        {
            // Print brief usage if no arguments provided.
            if (args.length == 0)
            {
                final String usage = CommandLineConfigurator.brief(
                        getProgramName(), DEFAULT_VAR, LocalizationManager.get(), L10N_CONFIG_PREFIX);
                if (usage != null)
                    println(usage);
                return false;
            }

            projectConfigurator.setConfiguration(args, ICompilerSettingsConstants.FILE_SPECS_VAR);
            projectConfigurator.applyToProject(project);
            problems = new ProblemQuery(projectConfigurator.getCompilerProblemSettings());

            // Get the configuration and configBuffer which are now initialized.
            config = projectConfigurator.getConfiguration();
            configBuffer = projectConfigurator.getConfigurationBuffer();
            problems.addAll(projectConfigurator.getConfigurationProblems());

            // Print version if "-version" is present.
            if (configBuffer.getVar("version") != null) //$NON-NLS-1$
            {
                println(VersionInfo.buildMessage() + " (" + JSSharedData.COMPILER_VERSION + ")");
                return false;
            }

            // Print help if "-help" is present.
            final List<ConfigurationValue> helpVar = configBuffer.getVar("help"); //$NON-NLS-1$
            if (helpVar != null)
            {
                processHelp(helpVar);
                return false;
            }

            for (String fileName : projectConfigurator.getLoadedConfigurationFiles())
            {
                JSSharedData.instance.stdout("Loading configuration: " + fileName);
            }

            if (config.isVerbose())
            {
                for (final IFileSpecification themeFile : project.getThemeFiles())
                {
                    JSSharedData.instance.stdout(String.format("Found theme file %s", themeFile.getPath()));
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
        finally
        {
            // If we couldn't create a configuration, then create a default one
            // so we can exit without throwing an exception.
            if (config == null)
            {
                config = new Configuration();
                configBuffer = new ConfigurationBuffer(Configuration.class, Configuration.getAliases());
            }
        }
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
     * Main body of this program. This method is called from the public static
     * method's for this program.
     * 
     * @return true if compiler succeeds
     * @throws IOException
     * @throws InterruptedException
     */
    protected boolean compile()
    {
    	ArrayList<String> otherFiles = new ArrayList<String>();
    	
        boolean compilationSuccess = false;
        try
        {
            setupJS();
            if (!setupTargetFile())
                return false;

            if (config.isDumpAst())
                dumpAST();

            buildArtifact();

            final File outputFile = new File(getOutputFilePath());
            final File outputFolder = outputFile.getParentFile();
            if (!outputFolder.exists())
            {
            	outputFolder.mkdirs();
            }
            
            if (swfTarget != null)
            {
                Collection<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
                Collection<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();
                
                // Don't create a swf if there are errors unless a 
                // developer requested otherwise.
                if (!config.getCreateTargetWithErrors())
                {
                    problems.getErrorsAndWarnings(errors, warnings);
                    if (errors.size() > 0)
                        return false;
                }

                final int swfSize = writeSWF(swfTarget, outputFile);

                println(String.format("%d bytes written to %s", swfSize, outputFile.getCanonicalPath()));
                
                if (JSSharedData.OUTPUT_ISOLATED)
                {
                    List<ICompilationUnit> reachableCompilationUnits = project.getReachableCompilationUnitsInSWFOrder(ImmutableSet.of(mainCU));
                    for (final ICompilationUnit cu : reachableCompilationUnits)
                    {
                    	if ((cu.getCompilationUnitType() == UnitType.AS_UNIT || 
                    			cu.getCompilationUnitType() == UnitType.MXML_UNIT) && cu != mainCU)
                    	{
		                	final File outputClassFile = new File(outputFolder.getAbsolutePath() + File.separator + cu.getShortNames().get(0) + ".js");
		                	System.out.println(outputClassFile.getAbsolutePath());
		                	otherFiles.add(outputClassFile.getAbsolutePath());
		                    final ISWFWriter swfWriter = JSSharedData.backend.createJSWriter(project, (List<ICompilerProblem>) errors, ImmutableSet.of(cu), false);
		
		                    if (swfWriter instanceof JSWriter)
		                    {
		                        final JSWriter writer = (JSWriter)swfWriter;
		
		                        final CountingOutputStream output =
		                                new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(outputClassFile)));
		                        writer.writeTo(output);
		                        output.flush();
		                        output.close();
		                        writer.close();
		                    }
	                    }
                    }
                }
            }

            dumpDependencyGraphIfNeeded();

            generateGoogDepsIfNeeded(outputFile.getParentFile());
            
            compilationSuccess = true;
            
            if (JSSharedData.OPTIMIZE)
            {
            	compilationSuccess = closureCompile(outputFile, problems);
            	if (compilationSuccess)
            	{
            		for (String fn : otherFiles)
            		{
            			File f = new File(fn);
            			f.delete();
            		}
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
     * Setup theme files.
     */
    protected void setupThemeFiles()
    {
        project.setThemeFiles(toFileSpecifications(config.getCompilerThemeFiles(), workspace));

        if (config.isVerbose())
        {
            for (final IFileSpecification themeFile : project.getThemeFiles())
            {
                verboseMessage(String.format("Found theme file %s", themeFile.getPath()));
            }
        }
    }

    /**
     * Setup {@code -compatibility-version} level. Falcon only support Flex 3+.
     */
    protected void setupCompatibilityVersion()
    {
        final int compatibilityVersion = config.getCompilerMxmlCompatibilityVersion();
        if (compatibilityVersion < Configuration.MXML_VERSION_3_0)
            throw new UnsupportedOperationException("Unsupported compatibility version: " + config.getCompilerCompatibilityVersionString());
        this.project.setCompatibilityVersion(
                config.getCompilerMxmlMajorCompatibilityVersion(),
                config.getCompilerMxmlMinorCompatibilityVersion(),
                config.getCompilerMxmlRevisionCompatibilityVersion());
    }

    /**
     * Parse all source files and dumpAST
     * 
     * @throws InterruptedException
     */
    private void dumpAST() throws InterruptedException
    {
        final List<String> astDump = new ArrayList<String>();
        final ImmutableList<ICompilationUnit> compilationUnits = getReachableCompilationUnits();
        for (final ICompilationUnit compilationUnit : compilationUnits)
        {
            final IASNode ast = compilationUnit.getSyntaxTreeRequest().get().getAST();
            astDump.add(ast.toString());
        }

        println(Joiner.on("\n\n").join(astDump)); //$NON-NLS-1$
    }

    /**
     * Build target artifact.
     * 
     * @throws InterruptedException threading error
     * @throws IOException IO error
     * @throws ConfigurationException
     */
    protected void buildArtifact() throws InterruptedException, IOException, ConfigurationException
    {
        swfTarget = buildSWFModel();
    }

    /**
     * Build SWF model object and collect problems building SWF in
     * {@link #problems}.
     * 
     * @return SWF model or null if SWF can't be built.
     * @throws InterruptedException concurrency problem
     * @throws ConfigurationException
     * @throws FileNotFoundException
     */
    private ISWF buildSWFModel() throws InterruptedException, FileNotFoundException, ConfigurationException
    {
        final List<ICompilerProblem> problemsBuildingSWF =
                new ArrayList<ICompilerProblem>();

        final ISWF swf = buildSWF(project, config.getMainDefinition(), mainCU, problemsBuildingSWF);
        problems.addAll(problemsBuildingSWF);
        if (swf == null)
        {
            ICompilerProblem problem = new UnableToBuildSWFProblem(getOutputFilePath());
            problems.add(problem);
        }
        else
        {
            swf.setFrameRate(config.getDefaultFrameRate());
            final int swfWidth = config.getDefaultWidth() * TWIPS_PER_PIXEL;
            final int swfHeight = config.getDefaultHeight() * TWIPS_PER_PIXEL;
            swf.setFrameSize(new Rect(swfWidth, swfHeight));
            swf.setVersion(config.getSwfVersion());
            swf.setTopLevelClass(config.getMainDefinition());
            swf.setUseAS3(true);
        }

        reportRequiredRSLs(target);

        return swf;
    }

    private void reportRequiredRSLs(ISWFTarget target) throws FileNotFoundException, InterruptedException, ConfigurationException
    {
        // Report the required RSLs:
        if (hasRSLs())
        {
            ITargetReport report = target.getTargetReport();

            if (report == null)
                return; // target must not have been built.

            // TODO (dloverin): localize messages
            JSSharedData.instance.stdout("Required RSLs:");

            // loop thru the RSLs and print out the required RSLs.
            for (RSLSettings rslSettings : report.getRequiredRSLs())
            {
                List<RSLAndPolicyFileURLPair> rslUrls = rslSettings.getRSLURLs();

                switch (rslUrls.size())
                {
                    case 0:
                        assert false; // One RSL URL is required.
                        break;
                    case 1:
                        JSSharedData.instance.stdout("    " + rslUrls.get(0).getRSLURL());
                        //ThreadLocalToolkit.log(new RequiredRSLUrl(rslUrls.get(0)));                    
                        break;
                    case 2:
                        JSSharedData.instance.stdout("    " + rslUrls.get(0).getRSLURL() + " with 1 failover.");
                        //ThreadLocalToolkit.log(new RequiredRSLUrlWithFailover(rslUrls.get(0)));
                        break;
                    default:
                        JSSharedData.instance.stdout("    " + rslUrls.get(0).getRSLURL() + " with " + (rslUrls.size() - 1) + " failovers.");
                        //                            ThreadLocalToolkit.log(new RequiredRSLUrlWithMultipleFailovers(
                        //                                    rslUrls.get(0),
                        //                                    rslUrls.size() - 1));
                        break;
                }

            }

            // All -runtime-shared-libraries are required
            for (String rslUrl : targetSettings.getRuntimeSharedLibraries())
            {
                JSSharedData.instance.stdout("    " + rslUrl);
                //ThreadLocalToolkit.log(new RequiredRSLUrl(rslUrls.get(0)));                    
            }
        }
    }

    private ITargetSettings getTargetSettings()
    {
        if (targetSettings == null)
            targetSettings = projectConfigurator.getTargetSettings(TargetType.SWF);

        return targetSettings;
    }

    private boolean hasRSLs() throws FileNotFoundException, InterruptedException, ConfigurationException
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
     * @throws FileNotFoundException error
     * @throws IOException error
     */
    private int writeSWF(final ISWF swf, final File outputFile) throws FileNotFoundException, IOException
    {
        int swfSize = 0;
        final List<ICompilerProblem> problemList = new ArrayList<ICompilerProblem>();
        final ISWFWriter swfWriter = JSSharedData.backend.createSWFWriter(project, problemList, swf, false, config.debug());

        if (swfWriter instanceof JSWriter)
        {
            final JSWriter writer = (JSWriter)swfWriter;

            final CountingOutputStream output =
                    new CountingOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
            writer.writeTo(output);
            output.flush();
            output.close();
            writer.close();
            swfSize = output.getCount();
        }

        /*
         * W#3047880 falconjs_cs6: internal compiler error generated with
         * optimize enabled compiling as3_enumerate.fla and fails to release the
         * JS file http://watsonexp.corp.adobe.com/#bug=3047880 This is part #3
         * of the fix: The closure compiler throws RTEs on internal compiler
         * errors, that don't get caught until they bubble up to MXMLJSC's
         * scope. On their way out files remain unclosed and cause problems,
         * because Flame cannot delete open files. We now get
         * InternalCompilerProblems, which we need to transfer to our problem
         * list.
         */
        problems.addAll(problemList);

        return swfSize;
    }

    /**
     * Computes the set of compilation units that root the dependency walk. The
     * returned set of compilation units and their dependencies will be
     * compiled.
     * <p>
     * This method can be overriden by sub-classes.
     * 
     * @return The set of rooted {@link ICompilationUnit}'s.
     */
    protected ImmutableSet<ICompilationUnit> getRootedCompilationUnits()
    {
        return ImmutableSet.of(mainCU);
    }

    /**
     * @return All the reachable compilation units in this job.
     */
    protected ImmutableList<ICompilationUnit> getReachableCompilationUnits()
    {
        final Set<ICompilationUnit> root = getRootedCompilationUnits();
        final List<ICompilationUnit> reachableCompilationUnitsInSWFOrder =
                project.getReachableCompilationUnitsInSWFOrder(root);
        final ImmutableList<ICompilationUnit> compilationUnits = ImmutableList.<ICompilationUnit> copyOf(reachableCompilationUnitsInSWFOrder);
        return compilationUnits;
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

        if (mainFileName != null)
        {
            final String normalizedMainFileName = FilenameNormalization.normalize(mainFileName);

            // Can not add a SourceHandler for *.css file because we don't want
            // to create compilation units for CSS files on the source path.
            if (mainFileName.toLowerCase().endsWith(".css")) //$NON-NLS-1$
            {
                mainCU = new StyleModuleCompilationUnit(
                        project,
                        workspace.getFileSpecification(normalizedMainFileName),
                        BasePriority.SOURCE_LIST);
                // TODO: Use CSS file name once CSS module runtime code is finalized. (scai)
                config.setMainDefinition("CSSModule2Main"); //$NON-NLS-1$
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
                    project.addIncludeSourceFile(normalizedMainFile);

                    // just using the basename is obviously wrong:
                    // final String mainQName = FilenameUtils.getBaseName(normalizedMainFile);

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
                                    mainQName = mainQName.substring(0, mainQName.length() - 3);
                                break;
                            }
                        }
                    }

                    if (mainQName == null)
                        mainQName = FilenameUtils.getBaseName(mainFileName);

                    Collection<ICompilationUnit> mainFileCompilationUnits =
                            workspace.getCompilationUnits(normalizedMainFileName, project);

                    assert mainFileCompilationUnits.size() == 1;
                    mainCU = Iterables.getOnlyElement(mainFileCompilationUnits);

                    assert ((DefinitionPriority)mainCU.getDefinitionPriority()).getBasePriority() == DefinitionPriority.BasePriority.SOURCE_LIST;

                    // Use main source file name as the root class name.
                    config.setMainDefinition(mainQName);
                }
            }
        }
        else
        {
            final List<ICompilerProblem> resourceBundleProblems = new ArrayList<ICompilerProblem>();
            Collection<ICompilationUnit> includedResourceBundles = target.getIncludedResourceBundlesCompilationUnits(resourceBundleProblems);
            problems.addAll(resourceBundleProblems);

            if (includedResourceBundles.size() > 0)
            {
                //This means that a Resource Module is requested to be built.
                mainCU = new ResourceModuleCompilationUnit(project, "GeneratedResourceModule", //$NON-NLS-1$
                        includedResourceBundles,
                        BasePriority.SOURCE_LIST);
                config.setMainDefinition("GeneratedResourceModule"); //$NON-NLS-1$
                project.addCompilationUnitsAndUpdateDefinitions(
                        Collections.singleton(mainCU));
            }
        }

        Preconditions.checkNotNull(mainCU, "Main compilation unit can't be null"); //$NON-NLS-1$

        /*
         * final String mainFileName = new
         * File(config.getTargetFile()).getAbsolutePath(); final
         * SourceCompilationUnitFactory compilationUnitFactory =
         * project.getSourceCompilationUnitFactory(); final File mainFile = new
         * File(mainFileName); // just using the basename is obviously wrong: //
         * final String mainQName = FilenameUtils.getBaseName(mainFileName);
         * final List<String> sourcePath = config.getCompilerSourcePath();
         * String mainQName = null; if( sourcePath != null &&
         * !sourcePath.isEmpty() ) { for( String path : sourcePath ) { final
         * String otherPath = new File(path).getAbsolutePath(); if(
         * mainFileName.startsWith(otherPath) ) { mainQName =
         * mainFileName.substring(otherPath.length() + 1); mainQName =
         * mainQName.replaceAll("\\\\", "/"); mainQName =
         * mainQName.replaceAll("\\/", "."); if( mainQName.endsWith(".as") )
         * mainQName = mainQName.substring(0, mainQName.length() - 3); break; }
         * } } if( mainQName == null ) mainQName =
         * FilenameUtils.getBaseName(mainFileName); mainCU =
         * compilationUnitFactory.createCompilationUnit( mainFile,
         * DefinitionPriority.BasePriority.SOURCE_LIST, mainQName, null);
         * Preconditions.checkNotNull(mainCU,
         * "Main compilation unit can't be null");
         * project.addCompilationUnitsAndUpdateDefinitions(
         * Collections.singleton(mainCU)); // Use main source file name as the
         * root class name. config.setMainDefinition(mainQName);
         */

        // target = (FlexSWFTarget)project.createSWFTarget(getTargetSettings(), null);
        if (getTargetSettings() == null)
            return false;

        project.setTargetSettings(getTargetSettings());
        target = (JSTarget)JSSharedData.backend.createSWFTarget(project, getTargetSettings(), null);

        return true;
    }

    /**
     * @return a list of resource bundle compilation units that are included
     * into the build process by -include-resource-bundles compiler argument.
     */
    protected Collection<ICompilationUnit> getIncludedResourceBundlesCompUnits() throws InterruptedException
    {
        Collection<ICompilerProblem> bundleProblems = new ArrayList<ICompilerProblem>();
        if (includedResourceBundleCompUnits == null)
        {
            includedResourceBundleCompUnits = new HashSet<ICompilationUnit>();

            for (String bundleName : config.getIncludeResourceBundles())
            {
                includedResourceBundleCompUnits.addAll(ResourceBundleUtils.findCompilationUnits(bundleName, project, bundleProblems));
                problems.addAll(bundleProblems);
            }
        }

        return includedResourceBundleCompUnits;
    }

    /**
     * Setup the source paths.
     * 
     * @throws InterruptedException
     */
    /**
     * Setup the source paths.
     * 
     * @throws InterruptedException
     */
    protected void setupSourcePath() throws InterruptedException
    {
        project.setSourcePath(toFiles(config.getCompilerSourcePath()));
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
        {
            final String extension = "." + JSSharedData.OUTPUT_EXTENSION;
            return FilenameUtils.removeExtension(config.getTargetFile()).concat(extension);
        }
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
     * @param fileSpecs file specifications
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
                return workspace.getFileSpecification(path);
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
        return "mxmljsc"; //$NON-NLS-1$
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
                while (keyword.startsWith("-")) //$NON-NLS-1$
                    keyword = keyword.substring(1);
                keywords.add(keyword);
            }
        }

        if (keywords.size() == 0)
            keywords.add("help"); //$NON-NLS-1$

        final String usages = CommandLineConfigurator.usage(
                getProgramName(),
                DEFAULT_VAR,
                configBuffer,
                keywords,
                LocalizationManager.get(),
                L10N_CONFIG_PREFIX);
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

    private void dumpDependencyGraphIfNeeded() throws IOException, InterruptedException, ConfigurationException
    {
        File dependencyGraphOutput = config.getDependencyGraphOutput();
        if (dependencyGraphOutput != null)
        {
            LinkedList<ICompilerProblem> problemList = new LinkedList<ICompilerProblem>();
            LinkageChecker linkageChecker = new LinkageChecker(project, getTargetSettings());
            final Target.RootedCompilationUnits rootedCompilationUnits = target.getRootedCompilationUnits();
            problems.addAll(rootedCompilationUnits.getProblems());
            GraphMLWriter dependencyGraphWriter =
                    new GraphMLWriter(project.getDependencyGraph(),
                            rootedCompilationUnits.getUnits(), true,
                            linkageChecker);
            BufferedOutputStream graphStream = new BufferedOutputStream(new FileOutputStream(dependencyGraphOutput));
            dependencyGraphWriter.writeToStream(graphStream, problemList);
            problems.addAll(problemList);
        }
    }

    // see http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html
    private boolean closureCompile(File outputFile, ProblemQuery problems) throws IOException
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
        Compiler compiler = new Compiler();

        CompilerOptions options = new CompilerOptions();

        if (JSSharedData.CLOSURE_compilation_level.equals("ADVANCED_OPTIMIZATIONS"))
            CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
        else if (JSSharedData.CLOSURE_compilation_level.equals("WHITESPACE_ONLY"))
            CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
        else
            CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

        final List<JSSourceFile> extern = CommandLineRunner.getDefaultExterns();

        final List<JSSourceFile> input = new ArrayList<JSSourceFile>();
		String googHome = System.getenv("GOOG_HOME");
		if (googHome == null || googHome.length() == 0)
			System.out.println("GOOG_HOME not defined.  Should point to goog folder containing base.js.");

        input.add(JSSourceFile.fromFile(googHome + "/base.js"));

        GoogDepsWriter dependencyGraphWriter =
            new GoogDepsWriter(mainCU, outputFile.getParentFile());

        ArrayList<String> files;
		try {
			files = dependencyGraphWriter.getListOfFiles();
	        for (String fileName : files)
	        {
	            input.add(JSSourceFile.fromFile(fileName));
	        }
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
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

        try
        {
            // compile() returns a Result, but it is not needed here.
            compiler.compile(extern, input, options);

            if (compiler.getErrorCount() == 0)
            {
                // The compiler is responsible for generating the compiled code; it is not
                // accessible via the Result.
                final String optimizedCode = compiler.toSource();
                BufferedOutputStream outputbuffer;
				try {
					String mainName = mainCU.getShortNames().get(0);
					File outputFolder = outputFile.getParentFile();
					outputbuffer = new BufferedOutputStream(new FileOutputStream(outputFile));
					outputbuffer.write(optimizedCode.getBytes());
					outputbuffer.flush();
					outputbuffer.close();
					File htmlFile = new File(outputFolder.getAbsolutePath() + File.separator + mainName + ".example.html");
					outputbuffer = new BufferedOutputStream(new FileOutputStream(htmlFile));
					String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
					html += "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
					html += "<head>\n";
					html += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n";
					html += "<script type=\"text/javascript\" src=\"" + mainName + ".js" + "\" ></script>\n";
					html += "<script type=\"text/javascript\">\n";
					html += "    var app = new " + mainName + "();\n";
					html += "</script>\n";
					html += "<title>" + mainName + "</title>\n";
					html += "</head>\n";
					html += "<body onload=\"app.start()\">\n";
					html += "</body>\n";
					html += "</html>\n";
					outputbuffer.write(html.getBytes());
					outputbuffer.flush();
					outputbuffer.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            else if (problems != null)
            {
                final JSError[] errors = compiler.getErrors();
                for (JSError err : errors)
                    problems.add(new ClosureProblem(err));
                
                return false;
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
            if (problems != null)
            {
                final ICompilerProblem problem = new InternalCompilerProblem(rte);
                problems.add(problem);
                return false;
            }
        }
        return true;
    }
    
    private void generateGoogDepsIfNeeded(File outputFolder) throws IOException, InterruptedException, ConfigurationException
    {
    	final File depsOutput = new File(outputFolder.getAbsolutePath() + File.separator + mainCU.getShortNames().get(0) + "Deps.js");
        if (!JSSharedData.OPTIMIZE)
        {
            GoogDepsWriter dependencyGraphWriter =
                    new GoogDepsWriter(mainCU, outputFolder);
            BufferedOutputStream graphStream = new BufferedOutputStream(new FileOutputStream(depsOutput));
            dependencyGraphWriter.writeToStream(graphStream);
            graphStream.flush();
            graphStream.close();
        }
    }
    
    /**
     * Get the current project.
     * 
     * @return project
     */
    protected FlexProject getProject()
    {
        return this.project;
    }

    /**
     * sets up JavaScript specific options
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    protected void setupJS() throws IOException, InterruptedException
    {
        JSGeneratingReducer.validate();

        JSSharedData.instance.reset();
        project.getSourceCompilationUnitFactory().addHandler(asFileHandler);

        if (isCompc())
            JSSharedData.COMPILER_NAME = "COMPJSC";
        else
            JSSharedData.COMPILER_NAME = "MXMLJSC";

        JSSharedData.instance.setVerbose(config.isVerbose());

        JSSharedData.DEBUG = config.debug();
        JSSharedData.OPTIMIZE = !config.debug() && config.optimize();

        // workaround for Falcon bug: getCompilerLibraryPath() is not supported yet.
        /*
         * if( config.getCompilerLibraryPath() != null ) { final List<String>
         * libs = config.getCompilerLibraryPath(); final File libPaths[] = new
         * File[libs.size()]; int nthPath = 0; for( String lib: libs ) { if( lib
         * == null ) throw JSSharedData.backend.createException(
         * "Invalid swc path in -compiler.library-path"); final String pathname
         * = lib; final File libPath = new File(pathname); libPaths[nthPath++] =
         * libPath; if( JSSharedData.SDK_PATH == null &&
         * libPath.getName().equals("browserglobal.swc")) { //
         * ../sdk/frameworks/libs/browser/browserglobal.swc File sdkFolder =
         * libPath.getParentFile(); if( sdkFolder != null ) { sdkFolder =
         * sdkFolder.getParentFile(); if( sdkFolder != null ) { sdkFolder =
         * sdkFolder.getParentFile(); if( sdkFolder != null ) { sdkFolder =
         * sdkFolder.getParentFile(); if( sdkFolder != null )
         * JSSharedData.SDK_PATH = sdkFolder.getAbsolutePath(); } } } } } //
         * Setting the library path into the project // causes an ISWC to be
         * built for each SWC on the library path. // It also causes an
         * MXMLManifestManager to be built for the project // from the manifest
         * info in the project's SWCs. project.setInternalLibraryPath(libPaths);
         * }
         */

        final Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();

        // workaround for Falcon bug: getCompilerIncludeLibraries() is not supported yet.
        /*
         * if( config.getCompilerIncludeLibraries() != null ) { // see
         * LibraryPathManager.computeUnitsToAdd() for( String swcSpec:
         * config.getCompilerIncludeLibraries() ) { final ISWCManager swcManager
         * = project.getWorkspace().getSWCManager(); final String swcFilePath =
         * swcSpec; final ISWC swc = swcManager.get(new File(swcFilePath));
         * final boolean isExternal = true; for (final ISWCLibrary library :
         * swc.getLibraries()) { for (final ISWCScript script :
         * library.getScripts()) { // Multiple definition in a script share the
         * same compilation unit // with the same ABC byte code block. final
         * List<String> qnames = new
         * ArrayList<String>(script.getDefinitions().size()); for (final String
         * definitionQName : script.getDefinitions()) { final String defName =
         * definitionQName.replace(":", "."); qnames.add(defName); //$NON-NLS-1$
         * //$NON-NLS-2$ } final ICompilationUnit cu = new SWCCompilationUnit(
         * project, swc, library, script, qnames, isExternal);
         * compilationUnits.add(cu); } } } }
         */

        // add builtins
        final File builtin = new File(JSSharedData.BUILT_IN);
        if (builtin.canRead())
        {
            if (config.isVerbose())
                JSSharedData.instance.stdout("[abc] found: " + builtin);
            ABCCompilationUnit cu = new ABCCompilationUnit(project, builtin.getPath());
            compilationUnits.add(cu);
        }

        if (!compilationUnits.isEmpty())
        {
            final List<ICompilationUnit> units = new LinkedList<ICompilationUnit>();
            units.addAll(compilationUnits);
            project.addCompilationUnitsAndUpdateDefinitions(units);

            if (config.isVerbose())
            {
                for (final ISWC swc : project.getLibraries())
                {
                    JSSharedData.instance.stdout(String.format("[lib] found library %s", swc.getSWCFile().getPath()));
                }
            }
        }

        registerSWCs(project);
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

    private ISWF buildSWF(CompilerProject applicationProject, String rootClassName, ICompilationUnit mainCU, Collection<ICompilerProblem> problems) throws
            InterruptedException, ConfigurationException, FileNotFoundException
    {
        Collection<ICompilerProblem> fatalProblems = applicationProject.getFatalProblems();
        if (!fatalProblems.isEmpty())
        {
            problems.addAll(fatalProblems);
            return null;
        }

        return target.build(mainCU, problems);
    }

    protected void verboseMessage(PrintStream strm, String s)
    {
        if (strm != null && config.isVerbose())
            strm.println(s);
    }

    /**
     * Scans JavaScript code for @requires tags and registers class
     * dependencies.
     * 
     * @param cu current CompilationUnit
     * @param classDef ClassDefinition of the JavaScript code
     * @param jsCode JavaScript code of the ClassDefinition. private static void
     * registerDependencies( ICompilationUnit cu, ClassDefinition classDef,
     * String jsCode ) { final JSSharedData sharedData = JSSharedData.instance;
     * final String requiresTag = "@requires"; // extract @requires class names
     * and register dependencies. if( jsCode.contains(requiresTag) ) { final
     * String line = jsCode.substring( jsCode.indexOf(requiresTag) +
     * requiresTag.length() ); for( String part : line.split(requiresTag) ) {
     * final String[] names = part.split("\\s+"); if( names.length > 1 ) { final
     * String depClassName = names[1]; ASProjectScope projectScope =
     * (ASProjectScope)cu.getProject().getScope(); IDefinition depClassDef =
     * projectScope.findDefinitionByName(depClassName); if(depClassDef != null)
     * { sharedData.addDependency(classDef, depClassDef); } } } } }
     */

    public static Boolean addDependency(ICompilationUnit cu, String className, DependencyType dt)
    {
        if (JSGeneratingReducer.isReservedDataType(className))
            return false;

        final ICompilationUnit fromCU = cu;
        final CompilerProject compilerProject = (CompilerProject)cu.getProject();
        final ASProjectScope projectScope = compilerProject.getScope();

        final IDefinition classDef = projectScope.findDefinitionByName(className);
        if (classDef == null)
            return false;

        final ICompilationUnit toCU = projectScope.getCompilationUnitForDefinition(classDef);
        if (fromCU == toCU)
            return false;

        // sharedData.verboseMessage( "Adding dependency: " + className );
        compilerProject.addDependency(fromCU, toCU, dt);
        JSSharedData.instance.registerDefinition(classDef);
        return true;
    }

    public static List<IDefinition> getDefinitions(ICompilationUnit cu, Boolean onlyClasses) throws InterruptedException
    {
        final List<IDefinition> classDefs = new ArrayList<IDefinition>();
        // populate the IDefinition to ClassDefinition map
        final List<IDefinition> defs = cu.getDefinitionPromises();
        for (IDefinition def : defs)
        {
            if (def instanceof DefinitionPromise)
            {
                // see DefinitionPromise::getActualDefinition 
                final String qname = def.getQualifiedName();
                final IFileScopeRequestResult fileScopeRequestResult = cu.getFileScopeRequest().get();
                def = fileScopeRequestResult.getMainDefinition(qname);
            }

            if (def != null && !onlyClasses || (def instanceof ClassDefinition))
            {
                classDefs.add(def);
            }
        }
        return classDefs;
    }

    public static void registerSWCs(CompilerProject project) throws InterruptedException
    {
        final JSSharedData sharedData = JSSharedData.instance;

        // collect all SWCCompilationUnit in swcUnits
        final List<ICompilationUnit> swcUnits = new ArrayList<ICompilationUnit>();
        for (ICompilationUnit cu : project.getCompilationUnits())
        {
            if (cu instanceof SWCCompilationUnit)
                swcUnits.add(cu);

            final List<IDefinition> defs = getDefinitions(cu, false);
            for (IDefinition def : defs)
            {
                sharedData.registerDefinition(def);
            }
        }

    }

    protected String getFlexHomePath()
    {
        final String loadConfig = config.getLoadConfig();
        if (loadConfig == null || loadConfig.isEmpty())
            return null;
        // throw new Error("Cannot find load configuration file: " + loadConfig.getPath() );

        final File loadConfigFile = new File(loadConfig);
        if (!loadConfigFile.isFile())
            return null;
        // throw new Error("Cannot find load configuration file: " + loadConfigFile.getAbsolutePath() );

        final File frameworksFolder = new File(loadConfigFile.getParent());
        if (!frameworksFolder.isDirectory())
            return null;
        // throw new Error("Cannot find framework folder: " + frameworksFolder.getAbsolutePath() );

        final String flexHome = frameworksFolder.getParent();
        if (flexHome == null || flexHome.isEmpty())
            return null;
        // throw new Error("Cannot find FLEX_HOME environment variable.");

        return flexHome;
    }

    protected JSCommandLineConfiguration getConfiguration()
    {
        if (config instanceof JSCommandLineConfiguration)
            return (JSCommandLineConfiguration)config;
        return null;
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
