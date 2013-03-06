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

package org.apache.flex.compiler.internal.test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.codegen.as.IASEmitter;
import org.apache.flex.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.projects.FlexProjectConfigurator;
import org.apache.flex.compiler.internal.projects.ISourceFileHandler;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.EnvProperties;
import org.apache.flex.compiler.visitor.as.IASBlockWalker;
import org.apache.flex.compiler.visitor.mxml.IMXMLBlockWalker;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

@Ignore
public class TestBase implements ITestBase
{
    protected List<ICompilerProblem> errors;

    protected static EnvProperties env = EnvProperties.initiate();

    protected static Workspace workspace = new Workspace();
    protected FlexProject project;

    protected IBackend backend;
    protected ASFilterWriter writer;

    protected IASEmitter asEmitter;
    protected IMXMLEmitter mxmlEmitter;

    protected IASBlockWalker asBlockWalker;
    protected IMXMLBlockWalker mxmlBlockWalker;

    protected String inputFileExtension;

    protected String mCode;

    protected File tempDir;

    private List<File> sourcePaths = new ArrayList<File>();
    private List<File> libraries = new ArrayList<File>();
    private List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();

    @Before
    public void setUp()
    {
        assertNotNull("Environment variable FLEX_HOME is not set", env.SDK);
        assertNotNull("Environment variable PLAYERGLOBAL_HOME is not set",
                env.FPSDK);

        errors = new ArrayList<ICompilerProblem>();

        project = new FlexProject(workspace);
        FlexProjectConfigurator.configure(project);

        backend = createBackend();
        writer = backend.createWriterBuffer(project);

        try
        {
            ISourceFileHandler sfh = backend.getSourceFileHandlerInstance();
            inputFileExtension = "." + sfh.getExtensions()[0];
        }
        catch (Exception e)
        {
            inputFileExtension = ".as";
        }

        sourcePaths = new ArrayList<File>();
        libraries = new ArrayList<File>();
        namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();

        tempDir = new File(FilenameNormalization.normalize("temp")); // ensure this exists
    }

    @After
    public void tearDown()
    {
        backend = null;
        writer = null;
        asEmitter = null;
        asBlockWalker = null;
        mxmlBlockWalker = null;
    }

    protected IBackend createBackend()
    {
        return null;
    }

    protected void assertOut(String code)
    {
        mCode = writer.toString();
        //System.out.println(mCode);
        assertThat(mCode, is(code));
    }

    @Override
    public String toString()
    {
        return writer.toString();
    }

    protected IFileNode compileAS(String input)
    {
        return compileAS(input, false, "");
    }

    protected IFileNode compileAS(String input, boolean isFileName,
            String inputDir)
    {
        return compileAS(input, isFileName, inputDir, true);
    }

    protected IFileNode compileAS(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        return (IFileNode) compile(input, isFileName, inputDir, useTempFile);
    }

    protected IASNode compile(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        File tempFile = (useTempFile) ? writeCodeToTempFile(input, isFileName,
                inputDir) : new File(FilenameNormalization.normalize(inputDir
                + File.separator + input + inputFileExtension));

        addDependencies();

        String normalizedMainFileName = FilenameNormalization
                .normalize(tempFile.getAbsolutePath());

        Collection<ICompilationUnit> mainFileCompilationUnits = workspace
                .getCompilationUnits(normalizedMainFileName, project);

        ICompilationUnit cu = null;
        for (ICompilationUnit cu2 : mainFileCompilationUnits)
        {
            if (cu2 != null)
                cu = cu2;
        }

        IASNode fileNode = null;
        try
        {
            fileNode = cu.getSyntaxTreeRequest().get().getAST();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return fileNode;
    }

    protected IMXMLFileNode compileMXML(String input)
    {
        return compileMXML(input, false, "");
    }

    protected IMXMLFileNode compileMXML(String input, boolean isFileName,
            String inputDir)
    {
        return compileMXML(input, isFileName, inputDir, true);
    }

    protected IMXMLFileNode compileMXML(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        return (IMXMLFileNode) compile(input, isFileName, inputDir, useTempFile);
    }

    protected File writeCodeToTempFile(String input, boolean isFileName,
            String inputDir)
    {
        File tempASFile = null;
        try
        {
            String tempFileName = (isFileName) ? input : getClass()
                    .getSimpleName();

            tempASFile = File.createTempFile(tempFileName, inputFileExtension,
                    tempDir);
            tempASFile.deleteOnExit();

            String code = "";
            if (!isFileName)
            {
                code = input;
            }
            else
            {
                code = getCodeFromFile(input, false, inputDir);
            }

            BufferedWriter out = new BufferedWriter(new FileWriter(tempASFile));
            out.write(code);
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        return tempASFile;
    }

    /**
     * Overridable setup of dependencies, default adds source, libraries and
     * namepsaces.
     * <p>
     * The test will then set the dependencies on the current
     * {@link ICompilerProject}.
     */
    protected void addDependencies()
    {
        addSourcePaths(sourcePaths);
        addLibraries(libraries);
        addNamespaceMappings(namespaceMappings);

        project.setSourcePath(sourcePaths);
        project.setLibraries(libraries);
        project.setNamespaceMappings(namespaceMappings);
    }

    protected void addLibraries(List<File> libraries)
    {
    }

    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(tempDir);
    }

    protected void addNamespaceMappings(
            List<IMXMLNamespaceMapping> namespaceMappings)
    {
    }

    protected String getCodeFromFile(String fileName, boolean isJS,
            String sourceDir)
    {
        String testFileDir = FilenameNormalization.normalize("test-files");

        File testFile = new File(testFileDir + File.separator + sourceDir
                + File.separator + fileName
                + (isJS ? ".js" : inputFileExtension));

        String code = "";
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(testFile), "UTF8"));

            String line = in.readLine();

            while (line != null)
            {
                code += line + "\n";
                line = in.readLine();
            }
            code = code.substring(0, code.length() - 1);

            in.close();
        }
        catch (Exception e)
        {
        }

        return code;
    }

    protected IASNode findFirstDescendantOfType(IASNode node,
            Class<? extends IASNode> nodeType)
    {
        int n = node.getChildCount();
        for (int i = 0; i < n; i++)
        {
            IASNode child = node.getChild(i);
            if (child instanceof FunctionNode)
            {
                ((FunctionNode) child).parseFunctionBody(errors);
            }
            if (nodeType.isInstance(child))
                return child;

            IASNode found = findFirstDescendantOfType(child, nodeType);
            if (found != null)
                return found;
        }

        return null;
    }

}
