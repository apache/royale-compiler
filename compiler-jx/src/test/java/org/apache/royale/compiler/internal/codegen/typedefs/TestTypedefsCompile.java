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

package org.apache.royale.compiler.internal.codegen.typedefs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.clients.COMPC;
import org.apache.royale.compiler.clients.EXTERNC;
import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.RoyaleProjectConfigurator;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class TestTypedefsCompile
{
    private static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    private static File tempDir = new File(testAdapter.getTempDir());

    private static File app1ASSrcDir = new File(testAdapter.getUnitTestBaseDir(), "typedefs/app1/as_src");

    private static File app1JSSrcDir = new File(testAdapter.getTempDir(), "typedefs/app1/js_src");

    private static File jsSWCFile = new File(testAdapter.getTempDir(), "typedefs/bin/JS.swc");

    protected static Workspace workspace = new Workspace();
    protected RoyaleJSProject project;
    private ArrayList<ICompilerProblem> errors;

    private RoyaleBackend backend;
    //private IJSEmitter emitter;
    //private IASBlockWalker walker;
    //private JSFilterWriter writer;

    private ArrayList<File> sourcePaths;
    private ArrayList<File> libraries;

    private EXTERNC client;

    private ExternCConfiguration config;

    @Before
    public void setUp() throws IOException
    {
        backend = new RoyaleBackend();

        config = new ExternCConfiguration();
        TypedefsTestUtils.init();
        config.setASRoot(TypedefsTestUtils.AS_ROOT_DIR);
        TypedefsTestUtils.addTestExcludesFull(config);
        TypedefsTestUtils.addTestTypedefsFull(config);
        
        assertFalse(config.getConfigurationProblems().size() > 0);

        client = new EXTERNC(config);

        backend = new RoyaleBackend();
        if (project == null)
            project = new RoyaleJSProject(workspace, backend);
        RoyaleProjectConfigurator.configure(project);

        //writer = backend.createWriterBuffer(project);
        //emitter = backend.createEmitter(writer);
        //walker = backend.createWalker(project, errors, emitter);

        sourcePaths = new ArrayList<File>();
        libraries = new ArrayList<File>();

        FileUtils.deleteQuietly(jsSWCFile);
        FileUtils.deleteQuietly(app1JSSrcDir);
    }

    @After
    public void tearDown()
    {
        client = null;
    }

    @Test
    public void test_full_compile() throws IOException
    {
        client.cleanOutput();
        client.compile();
        client.emit();

        compileSWC();
        assertTrue(jsSWCFile.exists());

        compileProject("Main", app1ASSrcDir.getAbsolutePath());

        assertTrue(new File(app1JSSrcDir, "Main_output.js").exists());
    }

    private boolean compileSWC()
    {
        CompilerArguments arguments = new CompilerArguments();
        configureCOMPCCompiler(arguments);

        File destAS3File = new File(config.getAsClassRoot().getAbsolutePath() + File.separator + "AS3.as");
        try {
			FileUtils.copyFile(TypedefsTestUtils.AS3_NAMESPACE_FILE, destAS3File);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        COMPC compc = new COMPC();

        final String[] args = arguments.toArguments().toArray(new String[] {});
        @SuppressWarnings("unused")
        int code = compc.mainNoExit(args);

        @SuppressWarnings("unused")
        List<ICompilerProblem> problems = compc.getProblems().getProblems();
        //getProblemQuery().addAll(problems);
        if (compc.getProblems().hasErrors())
            return false;

        return true;
    }

    protected List<String> compileProject(String inputFileName,
            String inputDirName)
    {
        List<String> compiledFileNames = new ArrayList<String>();

        String mainFileName = inputDirName + File.separator + inputFileName
                + ".as";

        addDependencies();

        ICompilationUnit mainCU = Iterables.getOnlyElement(workspace.getCompilationUnits(
                FilenameNormalization.normalize(mainFileName), project));

        if (project instanceof RoyaleJSProject)
            project.mainCU = mainCU;

        Configurator projectConfigurator = backend.createConfigurator();

        JSTarget target = backend.createTarget(project,
                projectConfigurator.getTargetSettings(null), null);

        target.build(mainCU, new ArrayList<ICompilerProblem>());

        List<ICompilationUnit> reachableCompilationUnits = project.getReachableCompilationUnitsInSWFOrder(ImmutableSet.of(mainCU));
        for (final ICompilationUnit cu : reachableCompilationUnits)
        {
            try
            {
                ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();

                if (cuType == ICompilationUnit.UnitType.AS_UNIT
                        || cuType == ICompilationUnit.UnitType.MXML_UNIT)
                {
                    File outputRootDir = new File(
                            FilenameNormalization.normalize(tempDir
                                    + File.separator + inputDirName));

                    String qname = cu.getQualifiedNames().get(0);

                    compiledFileNames.add(qname.replace(".", "/"));

                    final File outputClassFile = getOutputClassFile(qname
                            + "_output", outputRootDir);

                    System.out.println(outputClassFile);

                    ASFilterWriter writer = backend.createWriterBuffer(project);
                    IASEmitter emitter = backend.createEmitter(writer);
                    IASBlockWalker walker = backend.createWalker(project,
                            errors, emitter);

                    walker.visitCompilationUnit(cu);

                    System.out.println(writer.toString());

                    BufferedOutputStream out = new BufferedOutputStream(
                            new FileOutputStream(outputClassFile));

                    out.write(writer.toString().getBytes());
                    out.flush();
                    out.close();
                }
            }
            catch (Exception e)
            {
                //System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

        return compiledFileNames;
    }

    private void configureCOMPCCompiler(CompilerArguments arguments)
    {
        arguments.setOutput(jsSWCFile.getAbsolutePath());

        File classes = config.getAsClassRoot();
        File interfaces = config.getAsInterfaceRoot();
        File constants = config.getAsConstantRoot();
        File functions = config.getAsFunctionRoot();
        File typedefs = config.getAsTypeDefRoot();

        arguments.addSourcepath(classes.getAbsolutePath());
        arguments.addSourcepath(interfaces.getAbsolutePath());
        arguments.addSourcepath(constants.getAbsolutePath());
        arguments.addSourcepath(functions.getAbsolutePath());
        arguments.addSourcepath(typedefs.getAbsolutePath());

        arguments.addIncludedSources(classes.getAbsolutePath());
        arguments.addIncludedSources(interfaces.getAbsolutePath());
        arguments.addIncludedSources(constants.getAbsolutePath());
        arguments.addIncludedSources(functions.getAbsolutePath());
        arguments.addIncludedSources(typedefs.getAbsolutePath());
    }

    protected File getOutputClassFile(String qname, File outputFolder)
    {
        File baseDir = app1JSSrcDir;

        String[] cname = qname.split("\\.");
        String sdirPath = baseDir.getAbsolutePath() + File.separator;
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

        return new File(sdirPath + qname + "." + backend.getOutputExtension());
    }

    private void addDependencies()
    {
        libraries.add(jsSWCFile);
        sourcePaths.add(app1ASSrcDir);

        project.setSourcePath(sourcePaths);
        project.setLibraries(libraries);
    }
}
