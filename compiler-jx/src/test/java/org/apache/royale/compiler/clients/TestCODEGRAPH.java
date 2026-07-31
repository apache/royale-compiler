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

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void testCompilerErrorsPreventOutputByDefault()
    {
        int exitCode = compile(false);

        assertEquals(2, exitCode);
        assertFalse(outputFile.exists());
    }

    @Test
    public void testCreateTargetWithErrorsAllowsOutput()
    {
        int exitCode = compile(true);

        assertEquals(2, exitCode);
        assertTrue(problems.toString(), outputFile.exists());
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
        File goldenFile = new File(sourceDirectory, "codegraph/golden/GraphRoot.json");
        String goldenOutput = FileUtils.readFileToString(goldenFile, "UTF-8");
        assertEquals(goldenOutput, firstOutput);
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

    private int compile(boolean createTargetWithErrors)
    {
        File sourceFile = new File(testAdapter.getUnitTestBaseDir(), "codegraph/InvalidCodeGraph.as");
        return compile(sourceFile, null, createTargetWithErrors);
    }

    private int compile(File sourceFile, File sourceDirectory, boolean createTargetWithErrors)
    {
        return compile(sourceFile, sourceDirectory, createTargetWithErrors, null);
    }

    private int compile(File sourceFile, File sourceDirectory, boolean createTargetWithErrors, Boolean swf)
    {
        File jsSWC = new File("../compiler-externc/target/js.swc");
        List<String> arguments = new ArrayList<String>();
        arguments.add("-external-library-path=" + jsSWC.getPath());
        arguments.add("-output=" + outputFile.getPath());
        arguments.add("-include-sources=" + sourceFile.getPath());
        if (sourceDirectory != null)
            arguments.add("-source-path=" + sourceDirectory.getPath());
        if (createTargetWithErrors)
            arguments.add("-create-target-with-errors=true");
        if (swf != null)
        {
            arguments.add("-define=COMPILE::JS," + !swf);
            arguments.add("-define+=COMPILE::SWF," + swf);
        }
        arguments.add(sourceFile.getPath());

        problems = new ArrayList<ICompilerProblem>();
        return new CODEGRAPH().mainNoExit(arguments.toArray(new String[arguments.size()]), problems, false);
    }
}