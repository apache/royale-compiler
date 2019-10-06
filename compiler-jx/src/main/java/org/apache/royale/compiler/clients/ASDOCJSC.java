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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.apache.royale.compiler.clients.problems.ProblemQuery;
import org.apache.royale.compiler.codegen.as.IASWriter;
import org.apache.royale.compiler.codegen.js.royale.IJSRoyaleASDocEmitter;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.driver.js.IJSApplication;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.exceptions.ConfigurationException.IOError;
import org.apache.royale.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.royale.compiler.internal.driver.as.ASBackend;
import org.apache.royale.compiler.internal.driver.js.amd.AMDBackend;
import org.apache.royale.compiler.internal.driver.js.goog.ASDocConfiguration;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.internal.driver.mxml.royale.MXMLRoyaleASDocBackend;
import org.apache.royale.compiler.internal.driver.mxml.royale.MXMLRoyaleASDocDITABackend;
import org.apache.royale.compiler.internal.driver.mxml.royale.MXMLRoyaleBackend;
import org.apache.royale.compiler.internal.driver.mxml.jsc.MXMLJSCJSSWCBackend;
import org.apache.royale.compiler.internal.parsing.as.RoyaleASDocDelegate;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.RoyaleASDocProject;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.targets.RoyaleSWCTarget;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.InternalCompilerProblem;
import org.apache.royale.compiler.problems.UnableToBuildSWFProblem;
import org.apache.royale.compiler.targets.ITarget.TargetType;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * @author Erik de Bruin
 * @author Michael Schmalle
 */
public class ASDOCJSC extends MXMLJSCRoyale
{
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

    @Override
    public String getName()
    {
        return FLEX_TOOL_ASDOC;
    }

    @Override
    public int execute(String[] args)
    {
        return staticMainNoExit(args);
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

        IBackend backend = new ASBackend();
        for (String s : args)
        {
            if (s.contains("js-output-type"))
            {
                jsOutputType = MXMLJSC.JSOutputType.fromString(s.split("=")[1]);

                switch (jsOutputType)
                {
                case AMD:
                    backend = new AMDBackend();
                    break;

                case JSC:
                    backend = new MXMLJSCJSSWCBackend();
                    break;

                case ROYALE:
                case ROYALE_DUAL:
                    backend = new MXMLRoyaleASDocBackend();
                    break;

                case ROYALE_DITA:
                    backend = new MXMLRoyaleASDocDITABackend();
                    break;
                    
                case GOOG:
                    backend = new GoogBackend();
                    break;

                default:
                    throw new UnsupportedOperationException();
                }
            }
        }

        final ASDOCJSC mxmlc = new ASDOCJSC(backend);
        final List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        System.out.println((endTime - startTime) / 1e9 + " seconds");

        return exitCode;
    }

    public ASDOCJSC(IBackend backend)
    {
    	super(backend);
        project = new RoyaleASDocProject(workspace, backend);
    }
    
    protected void init()
    {
        IBackend backend = new MXMLRoyaleBackend();
        workspace = new Workspace();
        workspace.setASDocDelegate(new RoyaleASDocDelegate());
        project = new RoyaleJSProject(workspace, backend);
        problems = new ProblemQuery(); // this gets replaced in configure().  Do we need it here?
        asFileHandler = backend.getSourceFileHandlerInstance();    	
    }


    /**
     * Main body of this program. This method is called from the public static
     * method's for this program.
     * 
     * @return true if compiler succeeds
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected boolean compile()
    {
        boolean compilationSuccess = false;

        try
        {
            project.getSourceCompilationUnitFactory().addHandler(asFileHandler);

            if (setupTargetFile())
                buildArtifact();

            if (jsTarget != null)
            {
                Collection<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
                Collection<ICompilerProblem> warnings = new ArrayList<ICompilerProblem>();

                if (!config.getCreateTargetWithErrors())
                {
                    problems.getErrorsAndWarnings(errors, warnings);
                    if (errors.size() > 0)
                        return false;
                }
                
            	Map<String, String> defs = config.getCompilerDefine();
            	String swf = defs.get("COMPILE::SWF");
            	if (swf != null)
            	{
		        	if (swf.equals("true"))
		        		middle = ".swf";
		        	else
		        		middle = ".js";
            	}
                String outputFolderName = getOutputFilePath();

                File outputFolder = null;
            	outputFolder = new File(outputFolderName);

                Set<String> externs = config.getExterns();
                List<String> excludeClasses = ((ASDocConfiguration)config).getExcludeClasses();
                List<String> excludeSources = ((ASDocConfiguration)config).getExcludeSources();
                Collection<ICompilationUnit> roots = ((RoyaleSWCTarget)target).getReachableCompilationUnits(errors);
                Collection<ICompilationUnit> reachableCompilationUnits = project.getReachableCompilationUnitsInSWFOrder(roots);
                for (final ICompilationUnit cu : reachableCompilationUnits)
                {
                    ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();

                    if (cuType == ICompilationUnit.UnitType.AS_UNIT
                            || cuType == ICompilationUnit.UnitType.MXML_UNIT)
                    {
                    	String symbol = cu.getQualifiedNames().get(0);
                    	if (externs.contains(symbol)) continue;
                    	if (excludeClasses.contains(symbol)) continue;
                    	String sourceFile = cu.getAbsoluteFilename();
                    	sourceFile = sourceFile.replace("\\", "/");
                    	if (excludeSources.contains(sourceFile)) continue;                    		
                    	
                        final File outputClassFile = getOutputClassFile(
                                cu.getQualifiedNames().get(0), outputFolder);

                        if (config.isVerbose())
                        {
                            System.out.println("Compiling file: " + outputClassFile);
                        }

                        ICompilationUnit unit = cu;

                        IASWriter writer;
                        if (cuType == ICompilationUnit.UnitType.AS_UNIT)
                        {
                            writer = project.getBackend().createWriter(project,
                                    (List<ICompilerProblem>) errors, unit,
                                    false);
                        }
                        else
                        {
                            writer = project.getBackend().createMXMLWriter(
                                    project, (List<ICompilerProblem>) errors,
                                    unit, false);
                        }
                        problems.addAll(errors);
                        BufferedOutputStream out = new BufferedOutputStream(
                                new FileOutputStream(outputClassFile));
                        writer.writeTo(out);
                        out.flush();
                        out.close();
                        writer.close();
                    }
                }
                compilationSuccess = true;
                IJSRoyaleASDocEmitter emitter = (IJSRoyaleASDocEmitter) project.getBackend().createEmitter(null);
                emitter.outputIndex(outputFolder, (RoyaleASDocProject)project);
                emitter.outputClasses(outputFolder, (RoyaleASDocProject)project);
                emitter.outputTags(outputFolder, (RoyaleASDocProject)project);
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
    @Override
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
                config.getMainDefinition(), null, problemsBuildingSWF);
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
        {
            String outputFolderName = config.getOutput();
            return outputFolderName;
        }
    }

    String middle = "";
    
    /**
     * Get the output class file. This includes the (sub)directory in which the
     * original class file lives. If the directory structure doesn't exist, it
     * is created.
     * 
     * @author Erik de Bruin
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

        return new File(sdirPath + qname + middle + "." + project.getBackend().getOutputExtension());
    }

    /**
     * Mxmlc uses target file as the main compilation unit and derive the output
     * SWF file name from this file.
     * 
     * @return true if successful, false otherwise.
     * @throws InterruptedException
     */
    @Override
    protected boolean setupTargetFile() throws InterruptedException
    {
        config.getTargetFile();

        ITargetSettings settings = getTargetSettings();
        if (settings != null)
            project.setTargetSettings(settings);
        else
            return false;

        target = project.getBackend().createTarget(project,
                getTargetSettings(), null);

        return true;
    }

    private ITargetSettings getTargetSettings()
    {
        if (targetSettings == null)
            targetSettings = projectConfigurator.getTargetSettings(getTargetType());

        if (targetSettings == null)
            problems.addAll(projectConfigurator.getConfigurationProblems());

        return targetSettings;
    }

    /**
     * Validate target file.
     * 
     * @throws MustSpecifyTarget
     * @throws IOError
     */
    @Override
    protected void validateTargetFile() throws ConfigurationException
    {

    }

    protected String getProgramName()
    {
        return "compc";
    }

    protected boolean isCompc()
    {
        return true;
    }

    @Override
    protected TargetType getTargetType()
    {
        return TargetType.SWC;
    }
}
