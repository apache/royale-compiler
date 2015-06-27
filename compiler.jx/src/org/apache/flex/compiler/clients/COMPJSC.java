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

import org.apache.commons.io.FilenameUtils;
import org.apache.flex.compiler.codegen.as.IASWriter;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.driver.js.IJSApplication;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.exceptions.ConfigurationException.IOError;
import org.apache.flex.compiler.exceptions.ConfigurationException.MustSpecifyTarget;
import org.apache.flex.compiler.internal.codegen.js.JSSharedData;
import org.apache.flex.compiler.internal.driver.as.ASBackend;
import org.apache.flex.compiler.internal.driver.js.amd.AMDBackend;
import org.apache.flex.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSSWCBackend;
import org.apache.flex.compiler.internal.driver.mxml.jsc.MXMLJSCJSBackend;
import org.apache.flex.compiler.internal.driver.mxml.jsc.MXMLJSCJSSWCBackend;
import org.apache.flex.compiler.internal.driver.mxml.vf2js.MXMLVF2JSSWCBackend;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.InternalCompilerProblem;
import org.apache.flex.compiler.problems.UnableToBuildSWFProblem;
import org.apache.flex.compiler.targets.ITarget.TargetType;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.units.ICompilationUnit;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.*;


/**
 * @author Erik de Bruin
 * @author Michael Schmalle
 */
public class COMPJSC extends MXMLJSC
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
    public String getName() {
        return FLEX_TOOL_COMPC;
    }

    @Override
    public int execute(String[] args) {
        return staticMainNoExit(args);
    }

    /**
     * Java program entry point.
     * 
     * @param args command line arguments
     */
    public static void main(final String[] args) {
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
            if (s.contains("-js-output-type"))
            {
                jsOutputType = JSOutputType.fromString(s.split("=")[1]);

                switch (jsOutputType)
                {
                case AMD:
                    backend = new AMDBackend();
                    break;
                    
                case JSC:
                    backend = new MXMLJSCJSSWCBackend();
                    break;
                
                case FLEXJS:
                case FLEXJS_DUAL:
                    backend = new MXMLFlexJSSWCBackend();
                    break;
                
                case GOOG:
                    backend = new GoogBackend();
                    break;

                case VF2JS:
                    backend = new MXMLVF2JSSWCBackend();
                    break;
                    
                default :
                    throw new NotImplementedException();
                }
            }
        }

        final COMPJSC mxmlc = new COMPJSC(backend);
        final Set<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
        final int exitCode = mxmlc.mainNoExit(args, problems, true);

        long endTime = System.nanoTime();
        JSSharedData.instance.stdout((endTime - startTime) / 1e9 + " seconds");

        return exitCode;
    }

    public COMPJSC(IBackend backend)
    {
        super(backend);
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
                
                File outputFolder = new File(getOutputFilePath());
                
                Collection<ICompilationUnit> reachableCompilationUnits = project
                        .getCompilationUnits();
                for (final ICompilationUnit cu : reachableCompilationUnits)
                {
                    ICompilationUnit.UnitType cuType = cu
                            .getCompilationUnitType();

                    if (cuType == ICompilationUnit.UnitType.AS_UNIT
                            || cuType == ICompilationUnit.UnitType.MXML_UNIT)
                    {
                        final File outputClassFile = getOutputClassFile(cu
                                .getQualifiedNames().get(0), outputFolder);

                        System.out
                                .println("Compiling file: " + outputClassFile);

                        ICompilationUnit unit = cu;

                        IASWriter writer;
                        if (cuType == ICompilationUnit.UnitType.AS_UNIT)
                        {
                            writer = JSSharedData.backend.createWriter(project,
                                    (List<ICompilerProblem>) errors, unit,
                                    false);
                        }
                        else
                        {
                            writer = JSSharedData.backend.createMXMLWriter(
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
        {
            String outputFolderName = config.getOutput();
            if (outputFolderName.endsWith(".swc"))
            {
                File outputFolder = new File(outputFolderName);
                outputFolderName = outputFolder.getParent();
            }
            return outputFolderName;
        }
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
        
        target = JSSharedData.backend.createTarget(project,
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
