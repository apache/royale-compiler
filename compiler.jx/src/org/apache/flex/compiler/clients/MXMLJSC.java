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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.flex.compiler.as.codegen.IASWriter;
import org.apache.flex.compiler.clients.problems.ProblemPrinter;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.flex.compiler.common.VersionInfo;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.ConfigurationBuffer;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.config.ICompilerSettingsConstants;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.exceptions.ConfigurationException.IOError;
import org.apache.flex.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.flex.compiler.exceptions.ConfigurationException.OnlyOneSource;
import org.apache.flex.compiler.internal.js.codegen.JSSharedData;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.internal.js.driver.goog.JSGoogClosureCLR;
import org.apache.flex.compiler.internal.js.driver.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.internal.units.ResourceModuleCompilationUnit;
import org.apache.flex.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.js.IJSApplication;
import org.apache.flex.compiler.problems.ConfigurationProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.InternalCompilerProblem;
import org.apache.flex.compiler.problems.UnableToBuildSWFProblem;
import org.apache.flex.compiler.problems.UnexpectedExceptionProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.targets.ITarget;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.utils.FileUtils;
import org.apache.flex.utils.FilenameNormalization;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.deps.DepsGenerator;
import com.google.javascript.jscomp.deps.DepsGenerator.InclusionStrategy;

/**
 * @author Michael Schmalle
 */
public class MXMLJSC
{
    private static final String GOOG_INTERMEDIATE_DIR_NAME = "js-intermediate";
    private static final String GOOG_RELEASE_DIR_NAME = "js-release";

    /*
     * JS output type enumerations.
     */
    public enum JSOutputType
    {
        FLEXJS("flexjs"), GOOG("goog"), AMD("amd");

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

        //final IBackend backend = new JSBackend();
        final IBackend backend = new GoogBackend();
        final MXMLJSC mxmlc = new MXMLJSC(backend);
        final Set<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        JSSharedData.instance.stdout((endTime - startTime) / 1e9 + " seconds");

        System.exit(exitCode);
    }

    private Workspace workspace;
    private FlexProject project;
    private ProblemQuery problems;
    private ISourceFileHandler asFileHandler;
    private Configuration config;
    private Configurator projectConfigurator;
    private ConfigurationBuffer configBuffer;
    private ICompilationUnit mainCU;
    private ITarget target;
    private ITargetSettings targetSettings;
    private IJSApplication jsTarget;
    private JSOutputType jsOutputType;

    protected MXMLJSC(IBackend backend)
    {
        JSSharedData.backend = backend;
        workspace = new Workspace();
        project = new FlexProject(workspace);
        problems = new ProblemQuery();
        JSSharedData.OUTPUT_EXTENSION = backend.getOutputExtension();
        JSSharedData.workspace = workspace;
        asFileHandler = backend.getSourceFileHandlerInstance();
    }

    public int mainNoExit(final String[] args, Set<ICompilerProblem> problems,
            Boolean printProblems)
    {
        int exitCode = -1;
        try
        {
            exitCode = _mainNoExit(fixArgs(args), problems);
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
            Set<ICompilerProblem> outProblems)
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
        boolean compilationSuccess = false;

        try
        {
            setupJS();
            if (!setupTargetFile())
                return false;

            //if (config.isDumpAst())
            //    dumpAST();

            buildArtifact();

            if (jsTarget != null)
            {
                jsOutputType = JSOutputType
                        .fromString(((JSConfiguration) config)
                                .getJSOutputType());

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

                File outputFolder = getOutputFolder();

                List<ICompilationUnit> reachableCompilationUnits = project
                        .getReachableCompilationUnitsInSWFOrder(ImmutableSet
                                .of(mainCU));
                for (final ICompilationUnit cu : reachableCompilationUnits)
                {
                    if (cu.getCompilationUnitType() == ICompilationUnit.UnitType.AS_UNIT)
                    {
                        final File outputClassFile = getOutputClassFile(cu
                                .getQualifiedNames().get(0), outputFolder);

                        System.out
                                .println("Compiling file: " + outputClassFile);

                        ICompilationUnit unit = cu;
                        IASWriter jswriter = JSSharedData.backend.createWriter(
                                project, (List<ICompilerProblem>) errors, unit,
                                false);

                        // XXX (mschmalle) hack what is CountingOutputStream?
                        BufferedOutputStream out = new BufferedOutputStream(
                                new FileOutputStream(outputClassFile));
                        jswriter.writeTo(out);
                        out.flush();
                        out.close();
                        jswriter.close();
                    }
                }

                if (jsOutputType == JSOutputType.GOOG)
                    optimize(outputFolder.getPath());

                compilationSuccess = true;
            }
        }
        catch (Exception e)
        {
            final ICompilerProblem problem = new InternalCompilerProblem(e);
            problems.add(problem);
        }

        return compilationSuccess;
    }

    private File getOutputFolder()
    {
        if (jsOutputType != JSOutputType.GOOG)
        {
            return new File(getOutputFilePath()).getParentFile();
        }
        else
        {
            // (erikdebruin) 'goog' always releases to project directory...

            File outputFolder = new File(config.getTargetFileDirectory())
                    .getParentFile();
            outputFolder = new File(outputFolder, GOOG_INTERMEDIATE_DIR_NAME);

            if (outputFolder.exists())
                org.apache.commons.io.FileUtils.deleteQuietly(outputFolder);

            return outputFolder;
        }

    }

    /**
     * Pass the JS output through the Google Closure Compiler.
     */
    private void optimize(String intermediateDirPath) throws IOException
    {
        final String projectName = FilenameUtils.getBaseName(config
                .getTargetFile());
        final String outputFileName = projectName
                + "." + JSSharedData.OUTPUT_EXTENSION;

        File releaseDir = new File(
                new File(intermediateDirPath).getParentFile(),
                GOOG_RELEASE_DIR_NAME);
        final String releaseDirPath = releaseDir.getPath();
        if (releaseDir.exists())
            org.apache.commons.io.FileUtils.deleteQuietly(releaseDir);
        releaseDir.mkdir();

        final String closureLibDirPath = ((JSGoogConfiguration) config)
                .getClosureLib();
        final String closureGoogSrcLibDirPath = closureLibDirPath
                + "/closure/goog/";
        final String closureGoogTgtLibDirPath = intermediateDirPath
                + "/library/closure/goog";
        final String closureTPSrcLibDirPath = closureLibDirPath
                + "/third_party/closure/goog/";
        final String closureTPTgtLibDirPath = intermediateDirPath
                + "/library/third_party/closure/goog";
        final String vanillaSDKSrcLibDirPath = ((JSGoogConfiguration) config)
                .getVanillaSDKLib();
        final String vanillaSDKTgtLibDirPath = intermediateDirPath
                + "/VanillaSDK";

        final String depsSrcFilePath = intermediateDirPath
                + "/library/closure/goog/deps.js";
        final String depsTgtFilePath = intermediateDirPath + "/deps.js";
        final String projectIntermediateJSFilePath = intermediateDirPath
                + File.separator + outputFileName;
        final String projectReleaseJSFilePath = releaseDirPath
                + File.separator + outputFileName;

        appendExportSymbol(projectIntermediateJSFilePath, projectName);

        copyFile(vanillaSDKSrcLibDirPath, vanillaSDKTgtLibDirPath);

        List<SourceFile> inputs = new ArrayList<SourceFile>();
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                new File(intermediateDirPath),
                new RegexFileFilter("^.*(\\.js)"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            inputs.add(SourceFile.fromFile(file));
        }

        copyFile(closureGoogSrcLibDirPath, closureGoogTgtLibDirPath);
        copyFile(closureTPSrcLibDirPath, closureTPTgtLibDirPath);

        File srcDeps = new File(depsSrcFilePath);

        final List<SourceFile> deps = new ArrayList<SourceFile>();
        deps.add(SourceFile.fromFile(srcDeps));

        ErrorManager errorManager = new JSGoogErrorManager();
        DepsGenerator depsGenerator = new DepsGenerator(deps, inputs,
                InclusionStrategy.ALWAYS, closureGoogTgtLibDirPath,
                errorManager);
        writeFile(depsTgtFilePath, depsGenerator.computeDependencyCalls(),
                false);

        org.apache.commons.io.FileUtils.deleteQuietly(srcDeps);
        org.apache.commons.io.FileUtils.moveFile(new File(depsTgtFilePath),
                srcDeps);

        writeHTML("intermediate", projectName, intermediateDirPath);
        writeHTML("release", projectName, releaseDirPath);

        try
        {
            /* These are the arguments that "just work" with the Closure Builder
            <arg line="--closure_entry_point=${PROJECT_NAME}" />
            <arg line="--js ${selected}" />
            <arg line="--only_closure_dependencies" />
            <arg line="--compilation_level ADVANCED_OPTIMIZATIONS" />
            <arg line="--js_output_file ${FILE_OUTPUT_RELEASE}" />
            <arg line="--output_manifest ${DIR_INTERMEDIATE_APPLICATION}/manifest.txt" />
            <arg line="--create_source_map ${DIR_INTERMEDIATE_APPLICATION}/${PROJECT_NAME}.js.map" />
            <arg line="--source_map_format V3" />
            */

            //*
            ArrayList<String> optionList = new ArrayList<String>();

            files = org.apache.commons.io.FileUtils.listFiles(new File(
                    intermediateDirPath), new RegexFileFilter("^.*(\\.js)"),
                    DirectoryFileFilter.DIRECTORY);
            for (File file : files)
            {
                optionList.add("--js=" + file.getCanonicalPath());
            }

            optionList.add("--closure_entry_point=" + projectName);
            optionList.add("--only_closure_dependencies");
            optionList.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
            optionList.add("--js_output_file=" + projectReleaseJSFilePath);
            optionList.add("--output_manifest="
                    + releaseDirPath + File.separator + "manifest.txt");
            optionList.add("--create_source_map="
                    + projectReleaseJSFilePath + ".map");
            optionList.add("--source_map_format=" + SourceMap.Format.V3);

            String[] options = (String[]) optionList.toArray(new String[0]);

            JSGoogClosureCLR.main(options);
            
            // TODO (erikdebruin/mschmalle) the code never reaches this point...
            //                              the above code runs fine, but
            //                              somehow it 'stops' somewhere 'in'
            //                              the runner; stepping through doesn't
            //                              make it any clearer for me... ideas?

            appendSourceMapLocation(projectReleaseJSFilePath);

            System.out.println("The project '"
                    + projectName
                    + "' has been successfully compiled and optimized.");
            //*/

            /*
            Compiler compiler = new Compiler();

            CompilerOptions options = new CompilerOptions();

            CompilationLevel level = CompilationLevel.ADVANCED_OPTIMIZATIONS;
            level.setOptionsForCompilationLevel(options);

            options.setCodingConvention(new ClosureCodingConvention());
            
            DependencyOptions dependencyOptions = new DependencyOptions();
            dependencyOptions.setDependencySorting(true);
            dependencyOptions.setDependencyPruning(true);
            dependencyOptions.setMoocherDropping(true);
            List<String> entryPoints = new ArrayList<String>();
            entryPoints.add(projectName);
            dependencyOptions.setEntryPoints(entryPoints);
            options.setDependencyOptions(dependencyOptions);

            options.setSourceMapFormat(SourceMap.Format.V3);
            options.setSourceMapOutputPath(projectReleaseJSFilePath + ".map");
            
            List<SourceFile> externs = new ArrayList<SourceFile>();

            inputs = new ArrayList<SourceFile>();
            files = org.apache.commons.io.FileUtils.listFiles(new File(intermediateDirPath),
                    new RegexFileFilter("^.*(\\.js)"),
                    DirectoryFileFilter.DIRECTORY);
            for (File file : files)
            {
                inputs.add(SourceFile.fromFile(file));
            }
            
            compiler.compile(externs, inputs, options);

            if (compiler.getErrorCount() == 0)
            {
                writeFile(projectReleaseJSFilePath, compiler.toSource(), false);
                
                appendSourceMapLocation(projectReleaseJSFilePath);

                writeHTML("release", projectName, releaseDirPath);

                System.out.println("The project '"
                        + projectName
                        + "' has been successfully compiled and optimized.");
            }
            else
            {
                final JSError[] errors = compiler.getErrors();
                System.out.println(errors);
            }
            //*/
        }
        catch (RuntimeException rte)
        {
            final ICompilerProblem problem = new InternalCompilerProblem(rte);
            System.out.println(problem);
        }
    }

    private void appendExportSymbol(String path, String projectName)
            throws IOException
    {
        StringBuilder appendString = new StringBuilder();
        appendString
                .append("\n\n// Ensures the symbol will be visible after compiler renaming.\n");
        appendString.append("goog.exportSymbol('");
        appendString.append(projectName);
        appendString.append("', ");
        appendString.append(projectName);
        appendString.append(");\n");
        writeFile(path, appendString.toString(), true);
    }

    private void appendSourceMapLocation(String path) throws IOException
    {
        StringBuilder appendString = new StringBuilder();
        appendString.append("\n//@ sourceMappingURL=./Example.js.map\n");
        writeFile(path, appendString.toString(), true);
    }

    private void copyFile(String srcPath, String tgtPath) throws IOException
    {
        File srcFile = new File(srcPath);
        if (srcFile.isDirectory())
            org.apache.commons.io.FileUtils.copyDirectory(srcFile, new File(
                    tgtPath));
        else
            org.apache.commons.io.FileUtils
                    .copyFile(srcFile, new File(tgtPath));
    }

    private void writeHTML(String type, String projectName, String dirPath)
            throws IOException
    {
        StringBuilder htmlFile = new StringBuilder();
        htmlFile.append("<!DOCTYPE html>\n");
        htmlFile.append("<html>\n");
        htmlFile.append("<head>\n");
        htmlFile.append("\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n");
        htmlFile.append("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");

        if (type == "intermediate")
        {
            htmlFile.append("\t<script type=\"text/javascript\" src=\"./library/closure/goog/base.js\"></script>\n");
            htmlFile.append("\t<script type=\"text/javascript\">\n");
            htmlFile.append("\t\tgoog.require(\"");
            htmlFile.append(projectName);
            htmlFile.append("\");\n");
            htmlFile.append("\t</script>\n");
        }
        else
        {
            htmlFile.append("\t<script type=\"text/javascript\" src=\"./");
            htmlFile.append(projectName);
            htmlFile.append(".js\"></script>\n");
        }

        htmlFile.append("</head>\n");
        htmlFile.append("<body>\n");
        htmlFile.append("\t<script type=\"text/javascript\">\n");
        htmlFile.append("\t\tnew ");
        htmlFile.append(projectName);
        htmlFile.append("();\n");
        htmlFile.append("\t</script>\n");
        htmlFile.append("</body>\n");
        htmlFile.append("</html>");

        writeFile(dirPath + File.separator + "index.html", htmlFile.toString(),
                false);
    }

    private void writeFile(String path, String content, boolean append)
            throws IOException
    {
        File tgtFile = new File(path);

        if (!tgtFile.exists())
            tgtFile.createNewFile();

        FileWriter fw = new FileWriter(tgtFile, append);
        fw.write(content);
        fw.close();
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

        final IJSApplication app = buildApplication(project,
                config.getMainDefinition(), mainCU, problemsBuildingSWF);
        problems.addAll(problemsBuildingSWF);
        if (app == null)
        {
            ICompilerProblem problem = new UnableToBuildSWFProblem(
                    getOutputFilePath());
            problems.add(problem);
        }

        //reportRequiredRSLs(target);

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
        Collection<ICompilerProblem> fatalProblems = applicationProject
                .getFatalProblems();
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
            final String extension = "." + JSSharedData.OUTPUT_EXTENSION;
            return FilenameUtils.removeExtension(config.getTargetFile())
                    .concat(extension);
        }
        else
            return config.getOutput();
    }

    /**
     * @author Erik de Bruin
     * 
     * Get the output class file. This includes the (sub)directory in which the
     * original class file lives. If the directory structure doesn't exist, it
     * is created.
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

        return new File(sdirPath + qname + "." + JSSharedData.OUTPUT_EXTENSION);
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

        final String normalizedMainFileName = FilenameNormalization
                .normalize(mainFileName);

        final SourceCompilationUnitFactory compilationUnitFactory = project
                .getSourceCompilationUnitFactory();

        File normalizedMainFile = new File(normalizedMainFileName);
        if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
        {
            // adds the source path to the sourceListManager
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
                        mainQName = mainFileName
                                .substring(otherPath.length() + 1);
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

            Collection<ICompilationUnit> mainFileCompilationUnits = workspace
                    .getCompilationUnits(normalizedMainFileName, project);

            //assert mainFileCompilationUnits.size() == 1;
            mainCU = Iterables.getOnlyElement(mainFileCompilationUnits);

            //assert ((DefinitionPriority)mainCU.getDefinitionPriority()).getBasePriority() == DefinitionPriority.BasePriority.SOURCE_LIST;

            // Use main source file name as the root class name.
            config.setMainDefinition(mainQName);
        }

        Preconditions.checkNotNull(mainCU,
                "Main compilation unit can't be null");

        // if (getTargetSettings() == null)
        //     return false;

        target = JSSharedData.backend.createTarget(project,
                getTargetSettings(), null);

        return true;
    }

    private ITargetSettings getTargetSettings()
    {
        if (targetSettings == null)
            targetSettings = projectConfigurator.getTargetSettings(null);

        return targetSettings;
    }

    private void setupJS() throws IOException, InterruptedException
    {
        // JSSharedData.instance.reset();
        project.getSourceCompilationUnitFactory().addHandler(asFileHandler);

        // JSSharedData.instance.setVerbose(config.isVerbose());

        //JSSharedData.DEBUG = config.debug();
        //JSSharedData.OPTIMIZE = !config.debug() && config.optimize();

        //--- final Set<ICompilationUnit> compilationUnits = new HashSet<ICompilationUnit>();

        // XXX // add builtins?

        registerSWCs(project); // XXX is this needed?
    }

    public static void registerSWCs(CompilerProject project)
            throws InterruptedException
    {
        //        final JSSharedData sharedData = JSSharedData.instance;
        //
        //        // collect all SWCCompilationUnit in swcUnits
        //        final List<ICompilationUnit> swcUnits = new ArrayList<ICompilationUnit>();
        //        for (ICompilationUnit cu : project.getCompilationUnits())
        //        {
        //            //            if (cu instanceof SWCCompilationUnit)
        //            //                swcUnits.add(cu);
        //            //
        //            //            final List<IDefinition> defs = getDefinitions(cu, false);
        //            //            for (IDefinition def : defs)
        //            //            {
        //            //                sharedData.registerDefinition(def);
        //            //            }
        //        }

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
        projectConfigurator = createConfigurator();

        try
        {
            //            // Print brief usage if no arguments provided.
            //            if (args.length == 0)
            //            {
            //                final String usage = CommandLineConfigurator.brief(
            //                        getProgramName(), DEFAULT_VAR,
            //                        LocalizationManager.get(), L10N_CONFIG_PREFIX);
            //                if (usage != null)
            //                    println(usage);
            //                return false;
            //            }
            //
            projectConfigurator.setConfiguration(args,
                    ICompilerSettingsConstants.FILE_SPECS_VAR);
            projectConfigurator.applyToProject(project);
            problems = new ProblemQuery(
                    projectConfigurator.getCompilerProblemSettings());

            // Get the configuration and configBuffer which are now initialized.
            config = projectConfigurator.getConfiguration();
            configBuffer = projectConfigurator.getConfigurationBuffer();
            problems.addAll(projectConfigurator.getConfigurationProblems());

            // Print version if "-version" is present.
            if (configBuffer.getVar("version") != null) //$NON-NLS-1$
            {
                println(VersionInfo.buildMessage()
                        + " (" + JSSharedData.COMPILER_VERSION + ")");
                return false;
            }
            //
            //            // Print help if "-help" is present.
            //            final List<ConfigurationValue> helpVar = configBuffer
            //                    .getVar("help"); //$NON-NLS-1$
            //            if (helpVar != null)
            //            {
            //                processHelp(helpVar);
            //                return false;
            //            }
            //
            //            for (String fileName : projectConfigurator
            //                    .getLoadedConfigurationFiles())
            //            {
            //                JSSharedData.instance.stdout("Loading configuration: "
            //                        + fileName);
            //            }
            //
            //            if (config.isVerbose())
            //            {
            //                for (final IFileSpecification themeFile : project
            //                        .getThemeFiles())
            //                {
            //                    JSSharedData.instance.stdout(String.format(
            //                            "Found theme file %s", themeFile.getPath()));
            //                }
            //            }
            //
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
            final ICompilerProblem problem = new ConfigurationProblem(null, -1,
                    -1, -1, -1, e.getMessage());
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
                configBuffer = new ConfigurationBuffer(Configuration.class,
                        Configuration.getAliases());
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

    private void println(String string)
    {
        // TODO Auto-generated method stub

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
            workspace.endIdleState(Collections
                    .<ICompilerProject, Set<ICompilationUnit>> emptyMap());
        }
    }

    /**
     * Force terminate the compilation process.
     */
    protected void close()
    {
        workspace.close();
    }

    // workaround for Falcon bug.
    // Input files with relative paths confuse the algorithm that extracts the root class name.

    protected static String[] fixArgs(final String[] args)
    {
        String[] newArgs = args;
        if (args.length > 1)
        {
            String targetPath = args[args.length - 1];
            if (targetPath.startsWith("."))
            {
                targetPath = FileUtils
                        .getTheRealPathBecauseCanonicalizeDoesNotFixCase(new File(
                                targetPath));
                newArgs = new String[args.length];
                for (int i = 0; i < args.length - 1; ++i)
                    newArgs[i] = args[i];
                newArgs[args.length - 1] = targetPath;
            }
        }
        return newArgs;
    }

    public class JSGoogErrorManager implements ErrorManager
    {

        @Override
        public void setTypedPercent(double arg0)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public void report(CheckLevel arg0, JSError arg1)
        {
            // TODO Auto-generated method stub

        }

        @Override
        public JSError[] getWarnings()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getWarningCount()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getTypedPercent()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public JSError[] getErrors()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getErrorCount()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void generateReport()
        {
            // TODO Auto-generated method stub

        }
    }
}
