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

package org.apache.royale.compiler.internal.codegen.js.goog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * This class tests the production of valid 'goog' JS code from an external
 * project.
 * 
 * @author Erik de Bruin
 */
public class TestGoogProject extends ASTestBase
{

    private static String projectDirPath = "goog/projects";

    @Test
    public void test_imports()
    {
        String testDirPath = projectDirPath + "/imports";

        String fileName = "Case";

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/imports"));

        super.addSourcePaths(sourcePaths);
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }

    protected void assertProjectOut(List<String> compiledFileNames,
            String testDirPath)
    {
    	if (compiledFileNames.size() == 0)
    	{
            fail("Expected compiled files");
    	}
        for (String compiledFileName : compiledFileNames)
        {
            String compiledFilePath = tempDir.getAbsolutePath()
                    + File.separator + testDirPath + File.separator
                    + compiledFileName + "_output" + "."
                    + backend.getOutputExtension();
            String compiledResult = readCodeFile(new File(compiledFilePath));

            //System.out.println(compiledResult);

            String expectedFilePath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                    testDirPath +  "/" + compiledFileName + "_result" + "." + backend.getOutputExtension()).getPath();
            String expectedResult = readCodeFile(new File(expectedFilePath));
            /*if (!compiledResult.equals(expectedResult)) {
                System.out.println("expected\n"+expectedResult);
                System.out.println("got\n"+compiledResult);
            }*/
            assertThat(compiledResult, is(expectedResult));
        }
    }

    protected void assertPublishedProjectOut(String projectFolderPath,
            String projectName)
    {
    	String outputFolderName = tempDir + "/" + projectName + "/bin/js-debug";
        IOFileFilter jsFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter("js"));
        IOFileFilter htmlFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter("html"));
        IOFileFilter resultsFilter = FileFilterUtils.or(jsFilter, htmlFilter);
        File outputFolder = new File(outputFolderName);
        File projectFolder = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(), projectFolderPath);
        projectFolderPath = projectFolder.getAbsolutePath();
        Collection<File> files = FileUtils.listFiles(projectFolder, resultsFilter, null);
        for (File resultFile : files)
        {
            String compiledFilePath = resultFile.getAbsolutePath();
            if (compiledFilePath.startsWith(projectFolderPath))
            	compiledFilePath = compiledFilePath.substring(projectFolderPath.length());
            compiledFilePath = compiledFilePath.replace("_result", "");
            File compiledFile = new File(outputFolder, compiledFilePath);
            String compiledResult = readCodeFile(compiledFile);

            String expectedResult = readCodeFile(resultFile);
            /*if (!compiledResult.equals(expectedResult)) {
                System.out.println("expected\n"+expectedResult);
                System.out.println("got\n"+compiledResult);
            }*/
            assertThat(compiledResult, is(expectedResult));
        }
    }
}
