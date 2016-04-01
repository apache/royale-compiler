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
package org.apache.flex.compiler.internal.codegen.mxml.vf2js;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.apache.flex.compiler.internal.test.VF2JSMXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.utils.FilenameNormalization;
import org.apache.flex.utils.ITestAdapter;
import org.apache.flex.utils.TestAdapterFactory;
import org.junit.Test;

public class TestVF2JSMXMLApplication extends VF2JSMXMLTestBase
{
    private static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(testAdapter.getUnitTestBaseDir(), "vf2js/files"));
        sourcePaths.add(new File(testAdapter.getUnitTestBaseDir(), "vf2js/projects/simpleMXML/src"));

        super.addSourcePaths(sourcePaths);
    }

    @Test
    public void testSimple()
    {
        String fileName = "SimpleMXML";

        IMXMLFileNode node = compileMXML(fileName, true,
                new File(testAdapter.getUnitTestBaseDir(), "vf2js/files").getPath(), false);

        mxmlBlockWalker.visitFile(node);
        
        //writeResultToFile(writer.toxString(), fileName);

        assertOutWithMetadata(getCodeFromFile(fileName + "_result", true, "vf2js/files"));
    }


    @Test
    public void testSimpleMXMLProject()
    {
        String testDirPath = "vf2js/projects/simpleMXML/src";

        String fileName = "SimpleMXML_Project";

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        // ToDo (erikdebruin): MXML property initialized with a FunctionCall
        //                     are not included in the output (the assignment 
        //                     should be handled in the constructor, like in AS
        assertProjectOut(compiledFileNames, testDirPath);
    }

    protected void assertProjectOut(List<String> compiledFileNames,
            String testDirPath)
    {
        for (String compiledFileName : compiledFileNames)
        {
            String compiledFilePath = tempDir.getAbsolutePath()
                    + File.separator + testDirPath + File.separator
                    + compiledFileName + "_output" + "."
                    + backend.getOutputExtension();
            String compiledResult = readCodeFile(new File(compiledFilePath));

            //System.out.println(compiledResult);

            String expectedFilePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                    testDirPath + "/" + compiledFileName + "_result" + "." + backend.getOutputExtension()).getPath();
            String expectedResult = readCodeFile(new File(expectedFilePath));

            assertThat(compiledResult, is(expectedResult));
        }
    }
}
