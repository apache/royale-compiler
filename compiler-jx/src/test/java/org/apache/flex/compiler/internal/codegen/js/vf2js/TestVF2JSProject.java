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

import java.io.File;
import java.util.List;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogProject;
import org.apache.flex.compiler.internal.driver.js.vf2js.VF2JSBackend;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.utils.FilenameNormalization;
import org.apache.flex.utils.TestAdapterFactory;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class tests the production of valid 'vf2js' JS code from an external
 * project.
 * 
 * @author Erik de Bruin
 */
public class TestVF2JSProject extends TestGoogProject
{

    private static String projectDirPath = "vf2js/projects";

    @Override
    public void setUp()
    {
        project = new FlexJSProject(workspace);

        super.setUp();
    }
    
    @Ignore
    @Test
    public void test_imports()
    {
        // crude bypass to allow for successful inheritance
    }

    @Test
    public void test_Test()
    {
        String testDirPath = projectDirPath + "/interfaces";

        String fileName = "Test";

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    @Ignore
    public void test_SDKTricks()
    {
        String testDirPath = projectDirPath + "/sdk";

        String fileName = "SomeSDKClass";

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_Super()
    {
        String testDirPath = projectDirPath + "/super";

        String fileName = "Base";

        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/interfaces"));

        sourcePaths.add(new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/sdk"));

        sourcePaths.add(new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                projectDirPath + "/super"));

        super.addSourcePaths(sourcePaths);
    }

    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/" + env.FPVER + "/playerglobal.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/framework.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/spark.swc")));

        super.addLibraries(libraries);
    }

    @Override
    protected IBackend createBackend()
    {
        return new VF2JSBackend();
    }

}
