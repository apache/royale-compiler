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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Comparator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.CountingOutputStream;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCLinker;
import org.apache.royale.compiler.clients.problems.CompilerProblemCategorizer;
import org.apache.royale.compiler.clients.problems.ProblemFormatter;
import org.apache.royale.compiler.clients.problems.ProblemPrinter;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.clients.problems.WorkspaceProblemFormatter;
import org.apache.royale.compiler.common.VersionInfo;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.clients.CLIFactory;
import org.apache.royale.compiler.internal.config.FrameInfo;
import org.apache.royale.compiler.internal.projects.ASCProject;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority.BasePriority;
import org.apache.royale.compiler.internal.targets.AppSWFTarget;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.units.ABCCompilationUnit;
import org.apache.royale.compiler.internal.units.ASCompilationUnit;
import org.apache.royale.compiler.internal.units.ImportedASCompilationUnit;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.FileWriteProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InvalidImportFileProblem;
import org.apache.royale.compiler.problems.MultipleExternallyVisibleDefinitionsProblem;
import org.apache.royale.compiler.problems.UnfoundPropertyProblem;
import org.apache.royale.compiler.targets.ISWFTarget;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;
import org.apache.royale.swc.ISWC;
import org.apache.royale.swc.ISWCLibrary;
import org.apache.royale.swc.ISWCManager;
import org.apache.royale.swc.ISWCScript;
import org.apache.royale.swf.Header;
import org.apache.royale.swf.ISWF;
import org.apache.royale.swf.io.ISWFWriter;
import org.apache.royale.swf.io.SWFWriter;
import org.apache.royale.utils.FilenameNormalization;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * ActionScript Compiler command-line interface.
 * <p>
 * Although {@code ASC} can parse most of the command-line options supported by
 * the old ASC, only a few of them are implemented at the moment.
 * 
 * @see <a href="https://zerowing.corp.adobe.com/display/compiler/ASC+Client">ASC client spec</a>
 */
public class ASC
{

    private static final String DOUBLE_QUOTE = "\"";
    
    /**
     * A target settings class to determine if a compilation unit is external.
     * 
     */
    class ASCTargetSettings implements ITargetSettings
    {
        
        ASCTargetSettings(String rootSourceFile)
        {
            this.rootSourceFile = rootSourceFile;
        }
        
        Set<String> externalLibrariesSet;
        final String rootSourceFile;
        
        @Override
        public boolean isAccessible()
        {
            return false;
        }

        @Override
        public boolean isDebugEnabled()
        {
            return getEmitDebugInfo();
        }

        @Override
        public boolean isTelemetryEnabled()
        {
            return false;
        }

        @Override
        public boolean isOptimized()
        {
            return getOptimize();
        }

        @Override
        public boolean useCompression()
        {
            return false;
        }

        @Override
        public boolean allowSubclassOverrides()
        {
            return false;
        }

        @Override
        public boolean areVerboseStacktracesEnabled()
        {
            return false;
        }

        @Override
        public Collection<String> getASMetadataNames()
        {
            return getEmitMetadata() ? null : Collections.<String>emptyList();
        }

        @Override
        public File getDefaultCSS()
        {
            return null;
        }

        @Override
        public int getDefaultBackgroundColor()
        {
            return 0;
        }

        @Override
        public int getDefaultFrameRate()
        {
            return getFrameRate();
        }

        @Override
        public boolean areDefaultScriptLimitsSet()
        {
            return false;
        }

        @Override
        public int getDefaultScriptTimeLimit()
        {
            return 0;
        }

        @Override
        public int getDefaultScriptRecursionLimit()
        {
            return 0;
        }

        @Override
        public int getDefaultWidth()
        {
            return getWidth();
        }

        @Override
        public int getDefaultHeight()
        {
            return getHeight();
        }

        @Override
        public Collection<String> getExterns()
        {
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getIncludes()
        {
            return Collections.emptyList();
        }

        @Override
        public List<FrameInfo> getFrameLabels()
        {
            return Collections.emptyList();
        }

        @Override
        public String getSWFMetadata()
        {
            return null;
        }

        @Override
        public int getSWFVersion()
        {
            return DEFAULT_SWF_VERSION;
        }

        @Override
        public String getPreloaderClassName()
        {
            return null;
        }
        
        @Override
        public String getRootSourceFileName()
        {
            return rootSourceFile;
        }

        @Override
        public String getRootClassName()
        {
            return getSymbolClass();
        }

        @Override
        public boolean keepAllTypeSelectors()
        {
            return false;
        }

        @Override
        public boolean useNetwork()
        {
            return false;
        }

        @Override
        public List<File> getThemes()
        {
            return Collections.emptyList();
        }

        @Override
        public boolean removeUnusedRuntimeSharedLibraryPaths()
        {
            return false;
        }

        @Override
        public boolean verifyDigests()
        {
            return false;
        }

        @Override
        public Collection<File> getExternalLibraryPath()
        {
            if (externalLibraries == null || externalLibraries.size() == 0)
                return Collections.emptyList();
                
            List<File> result = new ArrayList<File>(externalLibraries.size());
            
            for (String path : externalLibraries)
            {
                result.add(new File(path));
            }
            
            return result;
        }

        @Override
        public Collection<File> getIncludeLibraries()
        {
            return Collections.emptyList();
        }

        @Override
        public Collection<File> getIncludeSources()
        {
            return Collections.emptyList();
        }

        @Override
        public List<String> getRuntimeSharedLibraries()
        {
            return Collections.emptyList();
        }

        @Override
        public List<RSLSettings> getRuntimeSharedLibraryPath()
        {
            return Collections.emptyList();
        }

        @Override
        public boolean useResourceBundleMetadata()
        {
            return false;
        }

        @Override
        public File getOutput()
        {
            return null;
        }

        @Override
        public Collection<String> getIncludeClasses()
        {
            return Collections.emptyList();
        }

        @Override
        public Map<String, File> getIncludeFiles()
        {
            return Collections.emptyMap();
        }

        @Override
        public Collection<String> getIncludeNamespaces()
        {
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getIncludeResourceBundles()
        {
            return Collections.emptyList();
        }

        @Override
        public Map<String, File> getIncludeStyleSheets()
        {
            return Collections.emptyMap();
        }

        @Override
        public boolean isIncludeLookupOnlyEnabled()
        {
            return false;
        }

        @Override
        public boolean isLinkageExternal(String path)
        {
            if (externalLibrariesSet == null)
            {
                externalLibrariesSet = new HashSet<String>();
                
                if (externalLibraries != null)
                {
                    for (String externalPath : externalLibraries)
                    {
                        externalLibrariesSet.add(new File(externalPath).getAbsolutePath());
                    }                    
                }
            }
            
            return externalLibrariesSet.contains(path);
        }

        @Override
        public boolean useDirectBlit()
        {
            return false;
        }

        @Override
        public boolean useGPU()
        {
            return false;
        }

        @Override
        public List<String> getDefaultsCSSFiles()
        {
            return ImmutableList.of();
        }
        
        @Override
        public List<String> getExcludeDefaultsCSSFiles()
        {
            return ImmutableList.of();
        }
        
        @Override
        public File getLinkReport()
        {
            return null;
        }    
        
        @Override
        public File getSizeReport()
        {
            return null;
        }

        @Override
        public String getRoyaleMinimumSupportedVersion()
        {
            // Not used because ASC does not create SWCs.
            return null;
        }    

        @Override
        public boolean getMxmlChildrenAsData()
        {
            // Not used because ASC does handle MXML.
            return false;
        }    

        @Override
        public boolean getInfoFlex()
        {
            // Not used because ASC does handle MXML.
            return false;
        }    

        @Override
        public boolean getAllowSubclassOverrides()
        {
            // Not used because ASC is not used in cross-compiling.
            return false;
        }    

        @Override
        public String[] getMxmlImplicitImports()
        {
            // Not used because ASC does not create SWCs.
            return null;
        }    

        @Override
        public boolean getRemoveDeadCode()
        {
            return removeDeadCode;
        }

		@Override
		public String getSWFMetadataDate() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getSWFMetadataDateFormat() {
			// TODO Auto-generated method stub
			return null;
		}
    }
    
    private static final int EXIT_CODE_SUCCESS = 0;
    private static final int EXIT_CODE_ERROR = 1;

    // for SWF generation
    private static final int DEFAULT_SWF_VERSION = 10;

    private static final int TARGET_AVM1 = 0; // Flash 9 VM
    private static final int TARGET_AVM2 = 1; // Flash 10 VM
    private static final int DEFAULT_TARGET_AVM = TARGET_AVM2; // Default to FP10
    private static final int DEFAULT_DIALECT = 9; // Earliest = 7, Latest = 11
    // CLIFactory is not thread-safe, get the options before instances are created.
    private static final Options ASC_OPTIONS = CLIFactory.getOptionsForASC();
    
    private PrintStream out;
    private PrintStream err;
    
    // Save problem info so that testsuite can access directly
    private ProblemQuery problemQuery;
    private ProblemFormatter problemFormatter;
       

    /**
     * Prints the asc executable command line header.
     */
    private void printHeader()
    {
        // This message should not be localized.
        out.println("ActionScript 3.0 Compiler for AVM+");
        out.println(VersionInfo.buildMessage());
    }

    private void printHelp(String commandLine, boolean showUsage)
    {
        PrintWriter pw = new PrintWriter(out);
        pw.println();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(pw, 72, commandLine, "       FILENAME...\noptions:", ASC_OPTIONS, 1, 3, null, showUsage);
        pw.flush();
    }

    private void printUsage(String commandLine)
    {
        PrintWriter pw = new PrintWriter(out);
        pw.println();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printUsage(pw, 72, commandLine, ASC_OPTIONS);
        pw.println("       FILENAME...");
        pw.flush();
    }

    /**
     * Entry point for ASC command-line.
     */
    public static void main(String[] args)
    {
        final ASC asc = new ASC();
        final int exitStatus = asc.mainNoExit(args);
        System.exit(exitStatus);
    }
    
    /**
     * Do not call {@link System#exit(int)} here to allow unit testing.
     * 
     * Uses System.out and System.err as default streams
     * 
     * @param args arguments
     * @return exit code
     */
    public int mainNoExit(String[] args)
    {
        return mainNoExit(args, System.out, System.err);
    }
    
    /**
     * Do not call {@link System#exit(int)} here to allow unit testing.
     * 
     * @param args arguments
     * @param out stdout printstream
     * @param err stderr printstream
     * @return exit code
     */
    public int mainNoExit(String[] args, PrintStream out, PrintStream err)
    {
        // Set the Printstreams
        this.out = out;
        this.err = err;
        
        boolean showHelp = false;
        boolean clientSuccess = false;
        try
        {
            final CommandLineParser cliParser = new PosixParser();
            final CommandLine commandLine = cliParser.parse(ASC_OPTIONS, args);

            // Check if help was requested, otherwise use the command line to
            // instantiate an instance of the ASC client.
            if (commandLine.hasOption("h"))
                showHelp = true;
            else
                clientSuccess = createClient(commandLine);
        }
        catch (ParseException ex)
        {
            err.println("Command-Line Error: " + ex.getMessage());
        }

        int exitStatus = 0;
        if (clientSuccess && ! this.getSourceFilenames().isEmpty() )
        {
            exitStatus = this.main();
        }
        else
        {
            exitStatus = 1;
            if (showHelp)
            {
                printHeader();
                printHelp("asc", true);
            }
            else
            {
                printHeader();
                printUsage("asc");
            }
        }
        return exitStatus;
    }
    
    public String getProblems()
    {
        StringBuffer problems = new StringBuffer();
        if (problemQuery != null && problemQuery.hasFilteredProblems())
        {
            for (final ICompilerProblem problem : problemQuery.getFilteredProblems())
            {
                problems.append(problemFormatter.format(problem));
            }
        }
        return problems.toString().trim();
    }

    /**
     * This is the compilation process.
     * <ol>
     * <li>Create a workspace and AS project.</li>
     * <li>Configure the project according to the command-line options.</li>
     * <li>Compile and generate output files.</li>
     * <li>Print result and error messages if there are problems.</li>
     * </ol>
     * 
     * @return exit status code
     */
    private int main()
    {
        int exitStatus = EXIT_CODE_SUCCESS;

        final Workspace workspace = new Workspace();
        try
        {
            // source AS3 files
            final boolean success = compileSourceFiles(workspace, getSourceFilenames());
            if (!success)
                exitStatus = EXIT_CODE_ERROR;
        }
        catch (Exception e)
        {
            err.println(e.getMessage());
            e.printStackTrace();
            exitStatus = EXIT_CODE_ERROR;
        }
        finally
        {
            workspace.close();
        }

        return exitStatus;
    }

    /**
     * Create SWC compilation units from SWC files.
     * 
     * @param project project
     * @param swcFilePaths swc file paths. The iterator must maintain the order
     * of the swcs.
     * @return a collections of SWC compilation units
     */
    private Collection<ICompilationUnit> getCompilationUnitsForLibraries(
            final CompilerProject project,
            final Collection<String> swcFilePaths)
    {
        final List<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
        final ISWCManager swcManager = project.getWorkspace().getSWCManager();
        int order = 0;
        
        for (final String swcFilePath : swcFilePaths)
        {
            if (!swcFilePath.toLowerCase().endsWith(".swc"))
                throw new RuntimeException("You can only import SWC libraries.");

            final ISWC swc = swcManager.get(new File(swcFilePath));

            for (final ISWCLibrary library : swc.getLibraries())
            {
                for (final ISWCScript script : library.getScripts())
                {
                    // Multiple definition in a script share the same compilation unit 
                    // with the same ABC byte code block.
                    final List<String> qnames = new ArrayList<String>(script.getDefinitions().size());
                    for (final String definitionQName : script.getDefinitions())
                    {
                        qnames.add(definitionQName.replace(":", "."));
                    }

                    final ICompilationUnit cu = new SWCCompilationUnit(
                            project, swc, library, script, qnames, order);
                    result.add(cu);
                }
            }
            
            order++;
        }

        return result;
    }
    
    /**
     * Minimally modifies the directory name read from the command line to make
     * a directory name we can use to write an output file.
     * 
     * @param inputDirectoryName directory name from the command line.
     * @return Directory name that is ends with {@link File#separator}.
     */
    private static String normalizeDirectoryName(String inputDirectoryName)
    {
        final String normalizedSeparators = FilenameUtils.separatorsToSystem(inputDirectoryName);
        if (inputDirectoryName.endsWith(File.separator))
            return normalizedSeparators;
        else
            return normalizedSeparators + File.separator;
    }
    
    /**
     * Compile one source file. Each source file has its own symbol table.
     * 
     * @param workspace workspace
     * @param sourceFilenames source filename
     * @throws InterruptedException compiler thread error
     * @return true compiled without problem
     */
    private boolean compileSourceFiles(final Workspace workspace, final List<String> sourceFilenames) throws InterruptedException
    {
        boolean success = true;
        long startTime = System.nanoTime();
        int problemCount = 0;
        
        //  Set up a problem query object to check the result of the compilation.
        //  Some problems found aren't ever relevant to ASC, and some depend on 
        //  the switches passed on the command line.
        problemQuery = new ProblemQuery();
        problemQuery.setShowProblemByClass(MultipleExternallyVisibleDefinitionsProblem.class, false);
        problemQuery.setShowProblemByClass(UnfoundPropertyProblem.class, false);
        problemQuery.setShowStrictSemantics(useStaticSemantics());    
        problemQuery.setShowWarnings(getShowWarnings());

        // process source AS3 files
        Set<ICompilationUnit> mainUnits = new LinkedHashSet<ICompilationUnit>(getSourceFilenames().size());
        final HashMap<ICompilationUnit, Integer> unitOrdering = new HashMap<ICompilationUnit, Integer>();

        ASCProject applicationProject = createProject(workspace, problemQuery);

        // Add any problems from parsing config vars supplied on the command line
        List<ICompilerProblem> configProblems = new ArrayList<ICompilerProblem>();
        applicationProject.collectProblems(configProblems);
        problemQuery.addAll(configProblems);

        int i = 0;
        for (final String sourceFilename : sourceFilenames)
        {
            // If we are not merging then create a new project
            // and set the compilation units.
            if (i > 0 && !getMergeABCs())
            {
                applicationProject = createProject(workspace, problemQuery);
                mainUnits.clear();
                unitOrdering.clear();
                problemQuery.clear();
            }

            final IFileSpecification sourceFileSpec = new FileSpecification(sourceFilename);
            workspace.fileAdded(sourceFileSpec);
            final ICompilationUnit cu = ASCompilationUnit.createMainCompilationUnitForASC(
                    applicationProject,
                    sourceFileSpec,
                    this);
            mainUnits.add(cu);
            unitOrdering.put(cu,unitOrdering.size());
            
            // add compilation unit to project
            applicationProject.addCompilationUnit(cu);
            applicationProject.updatePublicAndInternalDefinitions(Collections.singletonList(cu));
            
            // The logic that re-parses a garbage collected syntax tree, does not
            // know about the files included with the -in option, so we'll pin
            // the syntax tree here so we know we will never need to re-parse the
            // the synax tree for the root compilation unit.
            rootedSyntaxTrees.add(cu.getSyntaxTreeRequest().get().getAST());
            
            // syntax errors
            for (final ICompilationUnit compilationUnit : applicationProject.getCompilationUnits())
            {
                final ICompilerProblem[] problems = compilationUnit.getSyntaxTreeRequest().get().getProblems();
                problemQuery.addAll(problems);
            }
            
            //  Parse trees
            if ( getShowParseTrees() )
            {
                final String outputSyntaxFilename = FilenameUtils.removeExtension(sourceFilename).concat(".p");
                try
                {
                    PrintWriter syntaxFile = new PrintWriter(outputSyntaxFilename);
                    final IASNode ast = cu.getSyntaxTreeRequest().get().getAST();
                    if(ast instanceof FileNode)
                    {
                        // Parse the full tree and add the new problems found in the
                        // function bodies into the problem collection.
                        final FileNode fileNode = (FileNode)ast;
                        final ImmutableSet<ICompilerProblem> skeletonProblems = 
                                ImmutableSet.copyOf(fileNode.getProblems());
                    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.FILE_NODE) == CompilerDiagnosticsConstants.FILE_NODE)
                    		System.out.println("ASC waiting for lock in populateFunctionNodes");
                        fileNode.populateFunctionNodes();
                    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.FILE_NODE) == CompilerDiagnosticsConstants.FILE_NODE)
                    		System.out.println("ASC done with lock in populateFunctionNodes");
                        final ImmutableSet<ICompilerProblem> allProblems = 
                                ImmutableSet.copyOf(fileNode.getProblems());
                        
                        // Only add newly found problems. Otherwise, there will be
                        // duplicates in "problemQuery".
                        final SetView<ICompilerProblem> difference = Sets.difference(skeletonProblems, allProblems);
                        problemQuery.addAll(difference);
                    }
                    
                    syntaxFile.println(ast);
                    syntaxFile.flush();
                    syntaxFile.close();
                }
                catch (FileNotFoundException e)
                {
                    problemQuery.add(new FileWriteProblem(e));
                }
            }

            // output
            // For the merged case, wait until the last source file.
            // For the non-merged case, make each source file individually
            if (!getMergeABCs() || 
               (getMergeABCs() && (i == sourceFilenames.size() - 1)))
            {
                
                // Let's start up all the compilation units to try and get more threads generating code
                // at the same time.
                for (final ICompilationUnit compilationUnit : applicationProject.getCompilationUnits())
                {
                    compilationUnit.startBuildAsync(TargetType.SWF);
                }
                
                //  Run the resolveRefs() logic for as long as it's relevant.
                for (final ICompilationUnit compilationUnit : applicationProject.getCompilationUnits())
                {
                    final ICompilerProblem[] problems = compilationUnit.getOutgoingDependenciesRequest().get().getProblems();
                    problemQuery.addAll(problems);
                }

                String outputFileBaseName = FilenameUtils.getBaseName(sourceFilename);
                String outputDirectoryName = FilenameUtils.getFullPath(sourceFilename);
                
                // Apply user specified basename and output directory. The
                // basename is only changed ABCs are merged since each abc
                // needs a unique filename.
                if (getMergeABCs() && getOutputBasename() != null)
                    outputFileBaseName = getOutputBasename();
                
                final String specifiedOutputDirectory = getOutputDirectory();
                if (!Strings.isNullOrEmpty(specifiedOutputDirectory))
                    outputDirectoryName = normalizeDirectoryName(specifiedOutputDirectory);
                

                // Output to either a SWF or ABC file.
                if (isGenerateSWF())
                {
                    final boolean swfBuilt = generateSWF(outputDirectoryName, outputFileBaseName, applicationProject, 
                            mainUnits, sourceFilename, problemQuery, startTime);
                    if (!swfBuilt)
                        success = false;
                }
                else
                {
                    Collection<ICompilationUnit> units = mainUnits;
                    if(getMergeABCs())
                    {
                        // Run the topological sort to figure out which order to output the ABCs in
                        // Resorts to using commandline order rather than a filename based lexical sort in
                        // cases where there are no real dependencies between the scripts
                        units = applicationProject.getDependencyGraph().topologicalSort(mainUnits, 
                            new Comparator<ICompilationUnit>()
                            {
                                @Override
                                public int compare(ICompilationUnit o1, ICompilationUnit o2)
                                {
                                    return (unitOrdering.containsKey(o2) ? unitOrdering.get(o2) : 0) - (unitOrdering.containsKey(o1) ? unitOrdering.get(o1) : 0);
                                }
                            });
                        Collection<ICompilationUnit> sourceUnits = new ArrayList<ICompilationUnit>(mainUnits.size());
                        for(ICompilationUnit unit : units)
                        {
                            // The dependency graph will put all CompilationUnits in the results, but
                            // we only want the CUs for the source files, since the imports should not be merged
                            // into the resulting ABC
                            if(mainUnits.contains(unit))
                            {
                                sourceUnits.add(unit);
                            }
                        }
                        units = sourceUnits;
                    }
                    final boolean abcBuilt = generateABCFile(outputDirectoryName, outputFileBaseName, applicationProject, 
                            units, sourceFilename, problemQuery, startTime);
                    if (!abcBuilt)
                        success = false;
                }

                //*************************************
                // Report problems.
                //
                
                // let's make a categorizer, so we can differentiate errors and warnings
                CompilerProblemCategorizer compilerProblemCategorizer = new CompilerProblemCategorizer();
                problemFormatter = new WorkspaceProblemFormatter(workspace, compilerProblemCategorizer);
                ProblemPrinter printer = new ProblemPrinter(problemFormatter, err);
                problemCount += printer.printProblems(problemQuery.getFilteredProblems());
                
                startTime = System.nanoTime();
            }
            i++;
        }

        // If there were problems, print out the summary
        if (problemCount > 0)
        {
            Collection<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
            Collection<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();
            problemQuery.getErrorsAndWarnings(errors, warnings);
            
            int errorCount = errors.size();
            int warningCount = warnings.size();
          
            if (errorCount == 1)
            {
                err.println();
                err.println("1 error found");
            }
            else if (errorCount > 1)
            {
                err.println();
                err.println(errorCount + " errors found");
            }
           
            if (warningCount == 1)
            {
                err.println();
                err.println("1 warning found");
            }
            else if (warningCount > 1)
            {
                err.println();
                err.println(warningCount + " warnings found");
            }
    
            if (success && (errorCount > 0))
            { 
                success = false;      
            }
        }

        return success;
    }

    private ASCProject createProject(Workspace workspace, ProblemQuery problemQuery) throws InterruptedException
    {
        ASCProject applicationProject = new ASCProject(workspace, this.dialect == 10);
        setupConfigVars(applicationProject);
        applicationProject.setUseParallelCodeGeneration(getParallel());
        final List<ICompilationUnit> compilationUnits = new ArrayList<ICompilationUnit>();

        // import ABC files
        for (final String importFilename : getImportFilenames())
        {
            final ICompilationUnit cu;
            String lcFilename = importFilename.toLowerCase(); 
            if (lcFilename.endsWith(".abc"))
            {
                cu  = new ABCCompilationUnit(applicationProject, importFilename);
                compilationUnits.add(cu);
            }
            else if (lcFilename.endsWith(".as"))
            {
                cu = new ImportedASCompilationUnit(applicationProject,
                                                importFilename,
                                                BasePriority.SOURCE_LIST);
                compilationUnits.add(cu);
            }
            else
            {
                problemQuery.add(new InvalidImportFileProblem(importFilename));
            }
        }

        // get the compilation units for the external and internal libraries
        final List<String> libraries = new ArrayList<String>(externalLibraries.size() + internalLibraries.size());
        libraries.addAll(externalLibraries);
        libraries.addAll(internalLibraries);
        final Collection<ICompilationUnit> allLibraryCUs = getCompilationUnitsForLibraries(applicationProject, libraries);
        compilationUnits.addAll(allLibraryCUs);

        applicationProject.setCompilationUnits(compilationUnits);

        applicationProject.setEnableInlining(isInliningEnabled());

        return applicationProject;
    }

    /**
     * Transfer configuration variables supplied in -config to the project
     * settings.
     * 
     * @param applicationProject
     */
    private void setupConfigVars(ASCProject applicationProject)
    {
        Map<String, String> vars = getConfigVars();
        if (vars != null)
            applicationProject.addConfigVariables(vars);
    }

    /**
     * When {@code -swf} is not set, ASC compiles source file into an ABC file
     * by default.
     * 
     * @param applicationProject AS project
     * @param compilationUnits compilation units all of the source files
     * @param sourceFilename source file name
     * @param startTime time the build was started in nanoseconds
     * @return true if success
     * @throws InterruptedException error from compilation threads
     */
    private boolean generateABCFile(
            final String outputDirectoryName,
            final String outputBaseName,
            final ASCProject applicationProject,
            final Collection<ICompilationUnit> compilationUnits,
            final String sourceFilename,
            ProblemQuery problemQuery,
            long startTime)
            throws InterruptedException
    {
        boolean success = true;
        byte[] abcBytes = null;
        Collection<ICompilerProblem> fatalProblems = applicationProject.getFatalProblems();
        ArrayList<byte[]> abcList = new ArrayList<byte[]>();
        
        if( fatalProblems.isEmpty() )
        {
            for (ICompilationUnit cu : compilationUnits)
            {
                final IABCBytesRequestResult codegenResult = cu.getABCBytesRequest().get();
                abcBytes = codegenResult.getABCBytes();
                abcList.add(abcBytes);
                problemQuery.addAll(codegenResult.getProblems());
            }
        }
        else
        {
            problemQuery.addAll(fatalProblems);
        }

        try
        {

            //  Temporarily disable warnings so we can check for non-warnings.
            //  Note: To implement a "Warnings as errors" facility, change
            //  setShowWarnings()'s argument from literal false to query the
            //  relevant setting.
            problemQuery.setShowWarnings(false);

            if ( ! problemQuery.hasFilteredProblems() )
            {
                assert(abcBytes != null);

                try
                {
                    if( !getEmitDebugInfo() || !getABCFuture() || getMergeABCs() || getOptimize() || isInliningEnabled() )
                    {
                        ABCLinker.ABCLinkerSettings settings = new ABCLinker.ABCLinkerSettings();
                        settings.setStripDebugOpcodes(!getEmitDebugInfo());
                        settings.setOptimize(getOptimize());
                        settings.setEnableInlining(isInliningEnabled());
                        settings.setKeepMetadata(getEmitMetadata() ? null :
                            Collections.<String>emptyList());
                        settings.setStripGotoDefinitionHelp(true);
                        settings.setRemoveDeadCode(getRemoveDeadCode());

                        Collection<ICompilerProblem> linkerProblems = new ArrayList<ICompilerProblem>();
                        settings.setProblemsCollection(linkerProblems);
                        
                        abcBytes = ABCLinker.linkABC(abcList, getMajorABCVersion(), getMinorABCVersion(), settings);
                        problemQuery.addAll(linkerProblems);
                    }
                }
                catch(Exception e)
                {
                }
                
                final String outputABCBaseNameWithExt = outputBaseName + ".abc";
                final File outputABCFile = new File(outputDirectoryName + outputABCBaseNameWithExt);
                try
                {
                    final OutputStream abcFile = new BufferedOutputStream(new FileOutputStream(outputABCFile));
                    abcFile.write(abcBytes);
                    abcFile.flush();
                    abcFile.close();
                    out.format("%s, %d bytes written in %5.3f seconds\n",
                            outputABCFile.toString(), 
                            abcBytes.length,
                            (System.nanoTime() - startTime) / 1e9 );
                }
                catch (IOException e)
                {
                    problemQuery.add(new FileWriteProblem(e));
                    success = false;
                }
            }
            else
            {
                success = false;
            }
        }
        finally
        {
            problemQuery.setShowWarnings(getShowWarnings());
        }

        return success;
    }

    private int getMinorABCVersion()
    {
        return ABCConstants.VERSION_ABC_MINOR_FP10;
    }

    private int getMajorABCVersion()
    {
        return ABCConstants.VERSION_ABC_MAJOR_FP10;
    }

    /**
     * When {@code -swf} option is set, ASC compiles the source files into a SWF
     * file.
     * 
     * @param applicationProject application project
     * @param compilationUnits compilation unit(s) for the source file(s)
     * @param sourceFilename source file name
     * @param startTime time the build was started in nanoseconds
     * @return true if success
     * @throws InterruptedException error from compilation threads
     */
    private boolean generateSWF(
            String outputDirectoryName,
            String outputBaseName,
            final ASCProject applicationProject,
            final Set<ICompilationUnit> compilationUnits,
            final String sourceFilename,
            ProblemQuery problemQuery,
            long startTime)
            throws InterruptedException
    {
        boolean success = true;
        final ArrayList<ICompilerProblem> problemsBuildingSWF = new ArrayList<ICompilerProblem>();
        final ISWFTarget target = new AppSWFTarget(applicationProject, new ASCTargetSettings(sourceFilename), null, 
                compilationUnits);
        final ISWF swf = target.build(problemsBuildingSWF);

        if (swf != null)
        {
            swf.setTopLevelClass(getSymbolClass());

            final ISWFWriter writer = new SWFWriter(swf, Header.Compression.NONE);
            final String outputFileNameWithExt = outputBaseName + ".swf";
            final File outputFile = new File(outputDirectoryName + outputFileNameWithExt);
            try
            {
                CountingOutputStream output = new CountingOutputStream(
                        new BufferedOutputStream(new FileOutputStream(outputFile)));

                writer.writeTo(output);
                output.flush();
                output.close();
                writer.close();

                out.format("%s, %d bytes written in %5.3f seconds\n",
                        outputFile.toString(), 
                        output.getByteCount(),
                        (System.nanoTime() - startTime) / 1e9 );
            }
            catch (IOException e)
            {
                problemQuery.add(new FileWriteProblem(e));
                success = false;
            }
        }
        else
        {
            err.println("Unable to build SWF.");
            success = false;
        }

        problemQuery.addAll(problemsBuildingSWF);

        return success;
    }
    
    /**
     * Map from historical ASC language identifiers
     * to {@link Locale} objects.
     */
    private static final Map<String, Locale> localeMap =
        new ImmutableMap.Builder<String, Locale>()
        .put("EN", Locale.ENGLISH)
        .put("CN", Locale.SIMPLIFIED_CHINESE)
        .put("CS", new Locale("cs"))
        .put("DK", new Locale("da"))
        .put("DE", Locale.GERMAN)
        .put("ES", new Locale("es"))
        .put("FI", new Locale("fi"))
        .put("FR", Locale.FRENCH)
        .put("IT", Locale.ITALIAN)
        .put("JP", Locale.JAPANESE)
        .put("KR", Locale.KOREAN)
        .put("NO", new Locale("nb"))
        .put("NL", new Locale("nl"))
        .put("PL", new Locale("pl"))
        .put("BR", new Locale("pt"))
        .put("RU", new Locale("ru"))
        .put("SE", new Locale("sv"))
        .put("TR", new Locale("tr"))
        .put("TW", Locale.TRADITIONAL_CHINESE)
        .build();
    
    /**
     * Helper method used by command line parsing logic to convert historical
     * ASC language codes into a java {@link Locale} objecst.
     * 
     * @param language ASC language code to convert
     * @return A {@link Locale} object for the specified asc language code.
     * @throws MissingArgumentException Thrown if the specified language code is
     * not a valid asc language code.
     */
    private static Locale getLocaleForLanguage(String language) throws MissingArgumentException
    {
        Locale result = localeMap.get(language);
        if (result != null)
            return result;
        throw new MissingArgumentException("Language option must be one of: " + Joiner.on(", ").join(localeMap.keySet()));
    }
    

    /**
     * Apache Common CLI did the lexer work. This function does the parser work
     * to construct an {@code ASC} job from the command-line options.
     * 
     * @param line - the tokenized command-line
     * @return a new ASC client for the given command-line configuration; null
     * if no arguments were given.
     */
    private Boolean createClient(final CommandLine line) throws ParseException
    {
        // First, process parsed command line options.
        final Option[] options = line.getOptions();

        if (options == null)
            return false;

        for (int i = 0; i < options.length; i++)
        {
            final Option option = options[i];
            final String shortName = option.getOpt();

            if ("import".equals(shortName))
            {
                String[] imports = option.getValues();
                for (int j = 0; j < imports.length; j++)
                {
                    this.addImportFilename(imports[j]);
                }
            }
            else if ("in".equals(shortName))
            {
                String[] includes = option.getValues();
                for (int j = 0; j < includes.length; j++)
                {
                    this.addIncludeFilename(includes[j]);
                }
            }
            else if ("swf".equals(shortName))
            {
                String[] swfValues = option.getValue().split(",");
                if (swfValues.length < 3)
                    throw new MissingArgumentException("The swf option requires three arguments, only " + swfValues.length + " were found.");
                
                for (int j = 0; j < swfValues.length; j++)
                {
                    String value = swfValues[j];
                    if (j == 0)
                        this.setSymbolClass(value);
                    else if (j == 1)
                        this.setWidth(value);
                    else if (j == 2)
                        this.setHeight(value);
                    else if (j == 3)
                        this.setFrameRate(value);
                }
            }
            else if ("use".equals(shortName))
            {
                String[] namespaces = option.getValues();
                for (String namespace : namespaces)
                {
                    this.addNamespace(namespace);
                }
            }
            else if ("config".equals(shortName))
            {
                String[] config = option.getValues();
                if( config.length == 2 )
                {
                    // The config option will have been split around '='
                    // e.g. CONFIG::Foo='hi' will be split into
                    // 2 values - 'CONFIG::Foo' and 'hi'
                    String name = config[0];
                    String value = config[1];
                    value = fixupMissingQuote(value);
                    this.putConfigVar(name, value);
                }
            }
            else if ("strict".equals(shortName) || "!".equals(shortName))
            {
                this.setUseStaticSemantics(true);
            }
            else if ("d".equals(shortName))
            {
                this.setEmitDebugInfo(true);
            }
            else if ("warnings".equals(shortName) || "coach".equals(shortName))
            {
                if ("coach".equals(shortName))
                    err.println("'coach' has been deprecated. Please use 'warnings' instead.");
                this.setShowWarnings(true);
            }
            else if ("log".equals(shortName))
            {
                this.setShowLog(true);
            }
            else if ("md".equals(shortName))
            {
                this.setEmitMetadata(true);
            }
            else if ("merge".equals(shortName))
            {
                this.setMergeABCs(true);
            }
            else if ("language".equals(shortName))
            {
                String value = option.getValue();
                this.setLocale(getLocaleForLanguage(value));
            }
            else if ("doc".equals(shortName))
            {
                this.setEmitDocInfo(true);
            }
            else if ("avmtarget".equals(shortName))
            {
                String value = option.getValue();
                this.setTargetAVM(value);
            }
            else if ("AS3".equals(shortName))
            {
                this.setDialect("AS3");
            }
            else if ("ES".equals(shortName))
            {
                this.setDialect("ES");
            }
            else if ("o".equalsIgnoreCase(shortName) || "optimize".equalsIgnoreCase(shortName))
            {
                this.setOptimize(true);
            }
            else if ("o2".equalsIgnoreCase(shortName))
            {
                this.setOptimize(true);
            }
            else if ("out".equalsIgnoreCase(shortName))
            {
                this.setOutputBasename(option.getValue());
            }
            else if ("outdir".equalsIgnoreCase(shortName))
            {
                this.setOutputDirectory(option.getValue());
            }
            else if ("abcfuture".equals(shortName))
            {
                this.setABCFuture(true);
            }
            else if ("p".equals(shortName))
            {
                this.setShowParseTrees(true);
            }
            else if ("i".equals(shortName))
            {
                this.setShowInstructions(true);
            }
            else if ("m".equals(shortName))
            {
                this.setShowMachineCode(true);
            }
            else if ("f".equals(shortName))
            {
                this.setShowFlowGraph(true);
            }
            else if ("exe".equals(shortName))
            {
                String exe = option.getValue();
                this.setAvmplusFilename(exe);
            }
            else if ("movieclip".equals(shortName))
            {
                this.setMakeMovieClip(true);
            }
            else if ("ES4".equals(shortName))
            {
                this.setDialect("ES4");
            }
            else if ("li".equals(shortName))
            {
                this.internalLibraries.add(option.getValue());
            }
            else if ("le".equals(shortName))
            {
                this.externalLibraries.add(option.getValue());
            }
            else if ("parallel".equals(shortName))
            {
                this.setParallel(true);
            }
            else if ("inline".equals(shortName))
            {
                this.setMergeABCs(true); // inlining requires merging of ABCs
                this.setEnableInlining(true);
            }
            else if ( "removedeadcode".equals(shortName) )
            {
                this.setRemoveDeadCode(true);
            }
            else
            {
                throw new UnrecognizedOptionException("Unrecognized option '" + shortName + "'", shortName);
            }
        }

        // Then any remaining arguments that were not options are interpreted as
        // source files to compile.
        final String[] remainingArgs = line.getArgs();
        if (remainingArgs != null)
        {
            for (int i = 0; i < remainingArgs.length; i++)
            {
                this.addSourceFilename(remainingArgs[i]);
            }
        }
        else
        {
            throw new MissingArgumentException("At least one source file must be specified after the list of options.");
        }

        return true;
    }

    /**
     * The command line parser strips leading and trailing quotes from
     * arguments. For example, -config CONFIG::foo="test" gets parsed into 
     * -config CONFIG::foo="test. So here we will add the missing quote
     * back.
     * 
     * @param value the config value to fix up.
     * @return the fixed up config value.
     */
    private String fixupMissingQuote(String value)
    {
        // Look for unbalanced set of double quotes. If we have an odd number
        // of double quotes then append a double quote to the end of string,
        // assuming that this quote was removed by the parser.
        // If the value ends with a quote, then for sure a quote must have
        // been removed (could have been "");
        if (value.endsWith(DOUBLE_QUOTE))
        {
            value = value.concat(DOUBLE_QUOTE);            
        }
        else
        {
            String[] values = value.split(DOUBLE_QUOTE);
            
            // An even number of values means an odd
            // number of quotes.
            if (values.length % 2 == 0)
                value = value.concat(DOUBLE_QUOTE);            
        }
        
        return value;
    }

    // Inputs
    private List<String> sourceFilenames;
    private List<String> includeFilenames;
    private List<String> importFilenames;
    private Map<String, String> configVars;
    private List<String> useNamespaces;
    private final List<String> internalLibraries = new ArrayList<String>(0);
    private final List<String> externalLibraries = new ArrayList<String>(0);

    private int apiVersion = -1;
    private int dialect = DEFAULT_DIALECT;
    private int targetAVM = DEFAULT_TARGET_AVM;
    private boolean useStaticSemantics = false;
    private boolean showWarnings = false;
    private boolean useSanityMode = false; // TODO: Is this still relevant?
    private Locale locale = Locale.ENGLISH;

    // SWF
    private String symbolClass = null;
    private int width = 550;
    private int height = 400;
    private int frameRate = 12;
    private boolean emitDebugInfo = false;
    private boolean emitDocInfo = false;
    private boolean emitMetadata = false;

    private boolean abcFuture = false;

    private boolean makeMovieClip = false; // TODO: What did this do?
    private String avmplusFilename = null; // TODO: Do we still need to make a projector?

    // Compiler Debugging - TODO: Do we need to still support these? Can we use a different format?
    private boolean showBytes = false;
    private boolean showParseTrees = false;
    private boolean showInstructions = false;
    private boolean showLineNumbers = false;
    private boolean showMachineCode = false;
    private boolean showFlowGraph = false;
    private boolean showLog = false;
    
    // Post Processing
    private boolean optimize = false;
    private List<String> optimizerConfigs = null;
    private boolean mergeABCs = false;
    private String outputDirectory = null;
    private String outputBasename = null;
    
    // Use parallel code generation of method bodies?
    private boolean parallel = false;

    // Use function inlining
    private boolean enableInlining = false;

    // Remove dead code
    private boolean removeDeadCode = false;

    // List of IASNodes that are the root syntax tree nodes
    // of all the main compilation units compiled by an asc instance.
    // This is needed to prevent the GC from collecting our syntax tree.
    // We can't allow the GC to collect the syntax trees because the code that
    // re-parses a syntax tree if it is needed again does not know about
    // the -in options.
    private final ArrayList<IASNode> rootedSyntaxTrees = new ArrayList<IASNode>();

    /**
     * @return the list of source files to compile
     */
    public List<String> getSourceFilenames()
    {
        if (sourceFilenames == null)
            sourceFilenames = new ArrayList<String>();

        return sourceFilenames;
    }

    /**
     * @param source - a root source filename to compile
     */
    public void addSourceFilename(String source)
    {
        getSourceFilenames().add(source);
    }

    /**
     * @return the list of source filenames to include in compilation
     */
    public List<String> getIncludeFilenames()
    {
        if (includeFilenames == null)
            includeFilenames = new ArrayList<String>();

        return includeFilenames;
    }

    /**
     * @param filename - a source filename to include in compilation
     */
    public void addIncludeFilename(String filename)
    {
        final String includeFile = FilenameNormalization.normalize(filename);
        getIncludeFilenames().add(includeFile);
    }

    /**
     * @return the list of ABC filenames to import
     */
    public List<String> getImportFilenames()
    {
        if (importFilenames == null)
            importFilenames = new ArrayList<String>();

        return importFilenames;
    }

    /**
     * @param filename - an ABC filename to import
     */
    public void addImportFilename(String filename)
    {
        getImportFilenames().add(filename);
    }

    public List<String> getInternalLibraries()
    {
        return internalLibraries;
    }

    public List<String> getExternalLibraries()
    {
        return externalLibraries;
    }

    /**
     * @return the map of config namespace variables that will be used to
     * support conditional compilation
     */
    public Map<String, String> getConfigVars()
    {
        if (configVars == null)
            configVars = new LinkedHashMap<String, String>();

        return configVars;
    }

    /**
     * Sets a config var key/value pair used in conditional compilation.
     * 
     * @param key - the config var name
     * @param value - the config var value
     */
    public void putConfigVar(String key, String value)
    {
        getConfigVars().put(key, value);
    }

    /**
     * @return the list of namespaces to automatically use (open) during
     * compilation
     */
    public List<String> getNamespaces()
    {
        if (useNamespaces == null)
            useNamespaces = new ArrayList<String>();

        return useNamespaces;
    }

    /**
     * @param namespace - a namespace to be automatically used (opened) during
     * compilation
     */
    public void addNamespace(String namespace)
    {
        getNamespaces().add(namespace);
    }

    /**
     * @return the native API version of the runtime
     */
    public int getApiVersion()
    {
        return apiVersion;
    }

    /**
     * @param version - the native API version of the runtime
     */
    public void setApiVersion(String version)
    {
        this.apiVersion = Integer.parseInt(version.trim());
    }

    /**
     * @return the ActionScript language dialect to use during compilation
     */
    public int getDialect()
    {
        return dialect;
    }

    /**
     * @param dialect - the ActionScript language dialect to set for compilation
     */
    public void setDialect(String dialect)
    {
        if ("AS3".equalsIgnoreCase(dialect))
            this.dialect = 10;
        else if ("ES".equalsIgnoreCase(dialect))
            this.dialect = 9;
        else if ("ES4".equalsIgnoreCase(dialect))
            this.dialect = 11;
        else
            throw new IllegalArgumentException("Illegal dialect '" + dialect + "'");
    }

    /**
     * @return the version of the ActionScript Virtual Machine that generated
     * byte code should target, either 1 for AVM1 or 2 for AVM2.
     */
    public int getTargetAVM()
    {
        return targetAVM;
    }

    /**
     * @param version - the version of the ActionScript Virtual Machine that
     * generated byte code should target, either 1 for AVM1 or 2 for AVM2.
     */
    public void setTargetAVM(String version)
    {
        int v = Integer.parseInt(version.trim());
        if (v == TARGET_AVM1 || v == TARGET_AVM2)
            this.targetAVM = v;
        else
            throw new IllegalArgumentException("Illegal target AVM version '" + version + "'");
    }

    /**
     * @return whether static semantics should be used?
     */
    public boolean useStaticSemantics()
    {
        return useStaticSemantics;
    }

    /**
     * @param useStaticSemantics - determines whether static semantics should be
     * used? 
     */
    public void setUseStaticSemantics(boolean useStaticSemantics)
    {
        this.useStaticSemantics = useStaticSemantics;
    }
    /**
     * @return whether warnings for common ActionScript mistakes should be shown
     */
    public boolean getShowWarnings()
    {
        return showWarnings;
    }

    /**
     * @param showWarnings - determines whether warnings for common ActionScript
     * mistakes should be shown
     */
    public void setShowWarnings(boolean showWarnings)
    {
        this.showWarnings = showWarnings;
    }

    /**
     * @return whether system-independent error/warning output should be used
     * (appropriate for sanity testing)
     */
    public boolean getUseSanityMode()
    {
        return useSanityMode;
    }

    /**
     * @param useSanityMode - determines whether system-independent
     * error/warning output should be used
     */
    public void setUseSanityMode(boolean useSanityMode)
    {
        this.useSanityMode = useSanityMode;
    }

    /**
     * @return the locale to use for compiler errors and warnings
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * @param locale - the locale to use for compiler errors and warnings
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * @return whether a movieclip should be made? TODO: What did this option
     * do?
     */
    public boolean getMakeMovieClip()
    {
        return makeMovieClip;
    }

    /**
     * @param makeMovieClip - determines whether a movieclip should be made?
     */
    public void setMakeMovieClip(boolean makeMovieClip)
    {
        this.makeMovieClip = makeMovieClip;
    }

    /**
     * @return the name of the top-level SymbolClass to set in a SWF
     */
    public String getSymbolClass()
    {
        return symbolClass;
    }

    /**
     * @param symbolClass - the name of top-level SymbolClass to use in a SWF
     */
    public void setSymbolClass(String symbolClass)
    {
        this.symbolClass = symbolClass;
    }

    /**
     * @return true if {@code -swf} argument is set.
     */
    public boolean isGenerateSWF()
    {
        return this.symbolClass != null;
    }

    /**
     * @return the SWF stage width
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width - the SWF stage width
     */
    public void setWidth(String width)
    {
        this.width = Integer.parseInt(width.trim());
    }

    /**
     * @return the SWF stage height
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param height - the SWF stage height
     */
    public void setHeight(String height)
    {
        this.height = Integer.parseInt(height.trim());
    }

    /**
     * @return the SWF frame rate
     */
    public int getFrameRate()
    {
        return frameRate;
    }

    /**
     * @param frameRate - the SWF frame rate
     */
    public void setFrameRate(String frameRate)
    {
        this.frameRate = Integer.parseInt(frameRate.trim());
    }

    /**
     * @return whether debug info should be emitted into the byte code
     */
    public boolean getEmitDebugInfo()
    {
        return emitDebugInfo;
    }

    /**
     * @return whether we should target newer abc version
     */
    public boolean getABCFuture()
    {
        return abcFuture;
    }

    /**
     * @param b determines if we should emit a 47.14 ABC or not
     */
    public void setABCFuture(boolean b)
    {
        abcFuture = b;
    }
    /**
     * @param emitDebugInfo - determines whether debug info should be emitted
     * into the byte code
     */
    public void setEmitDebugInfo(boolean emitDebugInfo)
    {
        this.emitDebugInfo = emitDebugInfo;
    }

    /**
     * @return whether asdoc information should be emitted into the byte code
     */
    public boolean getEmitDocInfo()
    {
        return emitDocInfo;
    }

    /**
     * @param emitDocInfo - determines whether asdoc info should be emitted into
     * the byte code
     */
    public void setEmitDocInfo(boolean emitDocInfo)
    {
        this.emitDocInfo = emitDocInfo;
    }

    /**
     * @return whether metadata should be emitted into the byte code
     */
    public boolean getEmitMetadata()
    {
        return emitMetadata;
    }

    /**
     * @param emitMetadata - determines whether metadata should be emitted into
     * the byte code
     */
    public void setEmitMetadata(boolean emitMetadata)
    {
        this.emitMetadata = emitMetadata;
    }

    /**
     * @return the filename of the avmplus executable to use to make a projector
     * file
     */
    public String getAvmplusFilename()
    {
        return avmplusFilename;
    }

    /**
     * @param filename - the avmplus executable filename to use to create a
     * projector file
     */
    public void setAvmplusFilename(String filename)
    {
        this.avmplusFilename = filename;
    }

    /**
     * @return whether the byte codes should be shown in a dump of the generated
     * ABC
     */
    public boolean getShowBytes()
    {
        return showBytes;
    }

    /**
     * @param showBytes - determines whether bytes should be shown in a dump of
     * the generated ABC
     */
    public void setShowBytes(boolean showBytes)
    {
        this.showBytes = showBytes;
    }

    /**
     * @return whether the parse tree should be written to a .p file.
     */
    public boolean getShowParseTrees()
    {
        return showParseTrees;
    }

    /**
     * @param showParseTrees - determines whether the parse tree should be
     * written to a .p file
     */
    public void setShowParseTrees(boolean showParseTrees)
    {
        this.showParseTrees = showParseTrees;
    }

    /**
     * @return whether intermediate code should be written to a .il file
     */
    public boolean getShowInstructions()
    {
        return showInstructions;
    }

    /**
     * @param showInstructions - determines whether intermediate code should be
     * written to a .il file
     */
    public void setShowInstructions(boolean showInstructions)
    {
        this.showInstructions = showInstructions;
    }

    /**
     * @return whether line numbers should be shown in the dump of generated ABC
     * bytes
     */
    public boolean getShowLineNumbers()
    {
        return showLineNumbers;
    }

    /**
     * @param showLineNumbers - determines whether line numbers should be shown
     * in the dump of generated ABC bytes
     */
    public void setShowLineNumbers(boolean showLineNumbers)
    {
        this.showLineNumbers = showLineNumbers;
    }

    /**
     * @return whether AVM+ assembly code should be included in the .il file
     */
    public boolean getShowMachineCode()
    {
        return showMachineCode;
    }

    /**
     * @param showMachineCode - determines whether AVM+ assembly code should be
     * included in the .il file
     */
    public void setShowMachineCode(boolean showMachineCode)
    {
        this.showMachineCode = showMachineCode;
    }

    /**
     * @return whether the flow graph should be printed to standard out
     */
    public boolean getShowFlowGraph()
    {
        return showFlowGraph;
    }

    /**
     * @param showFlowGraph - determines whether the flow graph should be
     * printed to standard out
     */
    public void setShowFlowGraph(boolean showFlowGraph)
    {
        this.showFlowGraph = showFlowGraph;
    }

    /**
     * @return whether logging information should be shown
     */
    public boolean getShowLog()
    {
        return showLog;
    }

    /**
     * @param showLog - determines whether logging information should be shown
     */
    public void setShowLog(boolean showLog)
    {
        this.showLog = showLog;
    }

    /**
     * @return whether the ABC file should be optimized
     */
    public boolean getOptimize()
    {
        return optimize;
    }

    /**
     * @param optimize - determines whether the generated ABC should be
     * optimized
     */
    public void setOptimize(boolean optimize)
    {
        this.optimize = optimize;
    }

    /**
     * @return the configuration options for the optimizer
     */
    public List<String> getOptimizerConfigs()
    {
        if (optimizerConfigs == null)
            optimizerConfigs = new ArrayList<String>();

        return optimizerConfigs;
    }

    /**
     * @param value - a configuration option for the optimizer, potentially
     * specified as a 'name=value' String
     */
    public void addOptimizerConfig(String value)
    {
        getOptimizerConfigs().add(value);
    }
    
    /**
     * @return true if the ABC bytes should be merged into a single output 
     * file, false otherwise.
     */
    public boolean getMergeABCs()
    {
        return mergeABCs;
    }

    /**
     * @param mergeABCs true to turn on ABC merging, false to turn off.
     */
    public void setMergeABCs(boolean mergeABCs)
    {
        this.mergeABCs = mergeABCs;
    }

    /**
     * Get the user specified output directory.
     * Set using the "-outdir" option.
     * 
     * @return user specified directory. May be null if no directory was 
     * specified.
     */
    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * Set the output directory from the "-outdir" option.
     * 
     * @param outputDirectory A path string.
     */
    public void setOutputDirectory(String outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * Get the user specified basename. This option only makes sense when
     * merging ABCs. The basename can be specified using the "-out" option.
     * 
     * @return the basename of output files. May be null if no basename was
     * specified.
     */
    public String getOutputBasename()
    {
        return outputBasename;
    }

    /**
     * Set the basename. Typically set to the value of the "-out" option.
     * 
     * @param outputBasename The base name of the output file.
     */
    public void setOutputBasename(String outputBasename)
    {
        this.outputBasename = outputBasename;
    }
    
    /**
     * Gets a boolean that indicates whether or not parallel code generation of
     * method bodies is enabled.
     * 
     * @return true if method bodies should be generated in parallel, false
     * otherwise.
     */
    public boolean getParallel()
    {
        return parallel;
    }

    /**
     * Enables or disables parallel code generation of method bodies.
     * 
     * @param parallel If true, method bodies will be generated in parallel.
     */
    public void setParallel(boolean parallel)
    {
        this.parallel = parallel;
    }

    /**
     * Gets a boolean that indicates whether or not inlining of
     * method bodies is enabled.
     * 
     * @return true if inlining enabled, false otherwise.
     */
    public boolean isInliningEnabled()
    {
        return enableInlining;
    }

    /**
     * Enables or disables parallel code generation of method bodies.
     * 
     * @param enableInlining If true, method bodies will be generated in parallel.
     */
    public void setEnableInlining(boolean enableInlining)
    {
        this.enableInlining = enableInlining;
    }

    /**
     * Enable or disable dead code removal.
     * @param removeDeadCode true if dead code is to be removed.
     */
    public void setRemoveDeadCode(boolean removeDeadCode)
    {
        this.removeDeadCode = removeDeadCode;
    }

    /**
     * @return true if dead code is to be removed.
     */
    public boolean getRemoveDeadCode()
    {
        return this.removeDeadCode;
    }
}
