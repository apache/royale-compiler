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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;
import org.apache.flex.tools.FlexToolRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.asdoc.royale.ASDocComment;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnknownTypeProblem;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import antlr.CommonToken;

public class TestCODEGRAPH
{
    private final ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
    private File outputFile;
    private List<ICompilerProblem> problems;

    @Before
    public void setUp()
    {
        outputFile = new File(testAdapter.getTempDir(), "codegraph/InvalidCodeGraph.json");
        FileUtils.deleteQuietly(outputFile);
    }

    @After
    public void tearDown()
    {
        FileUtils.deleteQuietly(outputFile);
    }

    @Test
    public void testRegisteredAsRoyaleFlexTool()
    {
        FlexToolRegistry registry = new FlexToolRegistry();
        FlexToolGroup toolGroup = registry.getToolGroup("Royale");
        FlexTool tool = toolGroup.getFlexTool("codegraph");

        assertTrue(tool instanceof CODEGRAPH);
    }

    @Test
    public void testCompilerErrorsPreventOutputByDefault()
    {
        int exitCode = compile(false);

        assertEquals(2, exitCode);
        assertFalse(outputFile.exists());
    }

    @Test
    public void testCreateTargetWithErrorsAllowsOutput() throws IOException
    {
        int exitCode = compile(true);

        assertEquals(2, exitCode);
        assertTrue(problems.toString(), outputFile.exists());
        assertTrue(problems.toString(), containsProblem(UnknownTypeProblem.class));
        String output = FileUtils.readFileToString(outputFile, "UTF-8");
        assertTrue(output, output.contains("\"qualifiedName\": \"MissingType\""));
        assertTrue(output, output.contains("\"qualifiedName\": \"MissingInterface\""));
        assertTrue(output, output.contains("\"unresolved\": true"));
    }

    @Test
    public void testCompilerBackedGraphIsDeterministic() throws IOException
    {
        File sourceDirectory = testAdapter.getUnitTestBaseDir();
        File sourceFile = new File(sourceDirectory, "codegraph/golden/GraphRoot.as");
        outputFile = new File(testAdapter.getTempDir(), "codegraph/GraphRoot.json");

        int firstExitCode = compile(sourceFile, sourceDirectory, false);
        assertEquals(problems.toString(), 0, firstExitCode);
        String firstOutput = FileUtils.readFileToString(outputFile, "UTF-8");

        int secondExitCode = compile(sourceFile, sourceDirectory, false);
        assertEquals(problems.toString(), 0, secondExitCode);
        String secondOutput = FileUtils.readFileToString(outputFile, "UTF-8");

        assertEquals(firstOutput, secondOutput);
        assertFalse(firstOutput.contains("\r"));
        File goldenFile = new File(sourceDirectory, "codegraph/golden/GraphRoot.json");
        String goldenOutput = normalizeLineEndings(FileUtils.readFileToString(goldenFile, "UTF-8"));
        assertEquals(goldenOutput, firstOutput);
        assertEquals(goldenOutput, normalizeLineEndings(goldenOutput.replace("\n", "\r\n")));
    }

    @Test
    public void testPrivateASDocTagWithWindowsLineEndings()
    {
        ASDocComment comment = new ASDocComment(new CommonToken(0, "/**\r\n * @private\r\n */"));

        comment.compile();

        assertTrue(comment.hasTag("private"));
        assertFalse(comment.hasTag("private\r"));
        assertFalse(comment.commentNoEnd().contains("\r"));
    }

    @Test
    public void testCompilerDefinesSelectTargetMembers() throws IOException
    {
        File sourceDirectory = testAdapter.getUnitTestBaseDir();
        File sourceFile = new File(sourceDirectory, "codegraph/conditional/ConditionalGraph.as");
        outputFile = new File(testAdapter.getTempDir(), "codegraph/ConditionalGraph.json");

        int jsExitCode = compile(sourceFile, sourceDirectory, false, false);
        assertEquals(problems.toString(), 0, jsExitCode);
        String jsOutput = FileUtils.readFileToString(outputFile, "UTF-8");
        assertTrue(jsOutput, jsOutput.contains("\"target\": \"js\""));
        assertTrue(jsOutput, jsOutput.contains("#jsOnly()"));
        assertFalse(jsOutput, jsOutput.contains("#swfOnly()"));

        int swfExitCode = compile(sourceFile, sourceDirectory, false, true);
        assertEquals(problems.toString(), 0, swfExitCode);
        String swfOutput = FileUtils.readFileToString(outputFile, "UTF-8");
        assertTrue(swfOutput, swfOutput.contains("\"target\": \"swf\""));
        assertTrue(swfOutput, swfOutput.contains("#swfOnly()"));
        assertFalse(swfOutput, swfOutput.contains("#jsOnly()"));
    }

    @Test
    public void testIncludeSourcesDirectoryExportsAllSources() throws IOException
    {
        File sourceDirectory = testAdapter.getUnitTestBaseDir();
        File includeDirectory = new File(sourceDirectory, "codegraph/conditional");
        File sourceFile = new File(includeDirectory, "ConditionalGraph.as");
        outputFile = new File(testAdapter.getTempDir(), "codegraph/IncludedDirectory.json");

        int exitCode = compile(sourceFile, sourceDirectory, includeDirectory, false, false);
        assertEquals(problems.toString(), 0, exitCode);
        String output = FileUtils.readFileToString(outputFile, "UTF-8");
        assertTrue(output, output.contains("as3://codegraph/conditional/ConditionalGraph"));
        assertTrue(output, output.contains("as3://codegraph/conditional/IncludedOnly"));
    }

    @Test
    public void testIncludeSourcesDirectoryDoesNotRequireTargetFile() throws IOException
    {
        File sourceDirectory = testAdapter.getUnitTestBaseDir();
        File includeDirectory = new File(sourceDirectory, "codegraph/conditional");
        outputFile = new File(testAdapter.getTempDir(), "codegraph/IncludedDirectory.json");

        int exitCode = compile(null, sourceDirectory, includeDirectory, false, false);
        assertEquals(problems.toString(), 0, exitCode);
        String output = FileUtils.readFileToString(outputFile, "UTF-8");
        assertTrue(output, output.contains("\"module\": \"IncludedDirectory\""));
        assertTrue(output, output.contains("as3://codegraph/conditional/ConditionalGraph"));
        assertTrue(output, output.contains("as3://codegraph/conditional/IncludedOnly"));
    }

    @Test
    public void testIncludeSourcesDirectoryDoesNotRequireSourcePath() throws IOException
    {
        File includeDirectory = new File(testAdapter.getUnitTestBaseDir(), "codegraph/conditional");
        outputFile = new File(testAdapter.getTempDir(), "codegraph/IncludedDirectory.json");

        int exitCode = compile(null, null, includeDirectory, false, false);
        assertEquals(problems.toString(), 0, exitCode);
        String output = FileUtils.readFileToString(outputFile, "UTF-8");
        assertTrue(output, output.contains("as3://codegraph/conditional/ConditionalGraph"));
        assertTrue(output, output.contains("as3://codegraph/conditional/IncludedOnly"));
    }

    private int compile(boolean createTargetWithErrors)
    {
        File sourceFile = new File(testAdapter.getUnitTestBaseDir(), "codegraph/InvalidCodeGraph.as");
        return compile(sourceFile, null, createTargetWithErrors);
    }

    private boolean containsProblem(Class<? extends ICompilerProblem> problemType)
    {
        for (ICompilerProblem problem : problems)
        {
            if (problemType.isInstance(problem))
                return true;
        }
        return false;
    }

    private String normalizeLineEndings(String value)
    {
        return value.replace("\r\n", "\n").replace('\r', '\n');
    }

    private int compile(File sourceFile, File sourceDirectory, boolean createTargetWithErrors)
    {
        return compile(sourceFile, sourceDirectory, createTargetWithErrors, null);
    }

    private int compile(File sourceFile, File sourceDirectory, boolean createTargetWithErrors, Boolean swf)
    {
        return compile(sourceFile, sourceDirectory, sourceFile, createTargetWithErrors, swf);
    }

    private int compile(File sourceFile, File sourceDirectory, File includeSource,
            boolean createTargetWithErrors, Boolean swf)
    {
        File jsSWC = new File("../compiler-externc/target/js.swc");
        List<String> arguments = new ArrayList<String>();
        arguments.add("-external-library-path=" + jsSWC.getPath());
        arguments.add("-output=" + outputFile.getPath());
        arguments.add("-include-sources=" + includeSource.getPath());
        if (sourceDirectory != null)
            arguments.add("-source-path=" + sourceDirectory.getPath());
        if (createTargetWithErrors)
            arguments.add("-create-target-with-errors=true");
        if (swf != null)
        {
            arguments.add("-define=COMPILE::JS," + !swf);
            arguments.add("-define+=COMPILE::SWF," + swf);
        }
        if (sourceFile != null)
            arguments.add(sourceFile.getPath());

        problems = new ArrayList<ICompilerProblem>();
        return new CODEGRAPH().mainNoExit(arguments.toArray(new String[arguments.size()]), problems, false);
    }
}