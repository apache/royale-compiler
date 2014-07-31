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

package org.apache.flex.compiler.internal.codegen.js.vf2js;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.test.VF2JSTestBase;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class tests the production of valid 'vf2js' JS code from an external
 * project.
 * 
 * @author Erik de Bruin
 */
public class TestVF2JSProject extends VF2JSTestBase
{

    @Override
    public void setUp()
    {
        project = new FlexJSProject(workspace);

        super.setUp();
    }

    @Ignore
    @Test
    public void testSimpleMXMLProject()
    {
        String testDirPath = new File("test-files").getAbsolutePath()
                + "/vf2js/projects/simpleMXML/src";

        String fileName = "SimpleMXML";

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

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

            System.out.println(compiledResult);
            
            String expectedFilePath = testDirPath + File.separator
                    + compiledFileName + "_result" + "."
                    + backend.getOutputExtension();
            String expectedResult = readCodeFile(new File(expectedFilePath));

            assertThat(compiledResult, is(expectedResult));
        }
    }

}
