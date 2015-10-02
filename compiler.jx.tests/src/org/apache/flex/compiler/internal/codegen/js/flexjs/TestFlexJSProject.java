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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogProject;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class tests the production of valid 'flexjs' JS code from an external
 * project.
 * 
 * @author Erik de Bruin
 */
public class TestFlexJSProject extends TestGoogProject
{

    private static String projectDirPath = "flexjs/projects";
    protected String sourcePath;
    protected Collection<String> externs = new ArrayList<String>();

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

        sourcePath = "test-files"
            + File.separator + projectDirPath + "/interfaces";
        
        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_Super()
    {
        String testDirPath = projectDirPath + "/super";

        String fileName = "Base";

        sourcePath = "test-files"
            + File.separator + projectDirPath + "/super";
        
        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_AmbiguousDefinition()
    {
        String testDirPath = projectDirPath + "/package_conflicts_ambiguous_definition";

        String fileName = "AmbiguousDefinition";

        externs.add("Event");
        
        sourcePath = "test-files"
            + File.separator + projectDirPath + "/package_conflicts_ambiguous_definition";
        
        StringBuilder sb = new StringBuilder();
        compileProject(fileName, testDirPath, sb, false);

        externs.clear();

        String out = sb.toString();
        out = out.replace("\\", "/");
        
        assertThat(out, is("test-files/flexjs/projects/package_conflicts_ambiguous_definition/mypackage/TestClass.as(29:20)\nAmbiguous reference to Event\ntest-files/flexjs/projects/package_conflicts_ambiguous_definition/mypackage/TestClass.as(30:41)\nAmbiguous reference to Event\n"));
    }

    @Test
    public void test_PackageConflict_SamePackageAsConflict()
    {
        String testDirPath = projectDirPath + "/package_conflicts_same_package_as_conflict";

        String fileName = "SamePackageAsConflict";

        externs.add("Event");
        
        sourcePath = "test-files"
            + File.separator + projectDirPath + "/package_conflicts_same_package_as_conflict";
        
        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_DifferentPackageAsConflict()
    {
        String testDirPath = projectDirPath + "/package_conflicts_different_package_as_conflict";

        String fileName = "DifferentPackageAsConflict";

        externs.add("Event");
        
        sourcePath = "test-files"
            + File.separator + projectDirPath + "/package_conflicts_different_package_as_conflict";
        
        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_UseWindow()
    {
        String testDirPath = projectDirPath + "/package_conflicts_use_window";

        String fileName = "UseWindow";

        externs.add("Event");
        
        sourcePath = "test-files"
            + File.separator + projectDirPath + "/package_conflicts_use_window";
        
        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_NoConflictNoWindow()
    {
        String testDirPath = projectDirPath + "/package_conflicts_no_conflict_no_window";

        String fileName = "NoConflictNoWindow";

        externs.add("Event");
        
        sourcePath = "test-files"
            + File.separator + projectDirPath + "/package_conflicts_no_conflict_no_window";
        
        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Test
    public void test_PackageConflict_NoConflictUseWindow()
    {
        String testDirPath = projectDirPath + "/package_conflicts_no_conflict_use_window";

        String fileName = "NoConflictUseWindow";

        externs.add("Event");
        
        sourcePath = "test-files"
            + File.separator + projectDirPath + "/package_conflicts_no_conflict_use_window";
        
        List<String> compiledFileNames = compileProject(fileName, testDirPath);

        externs.clear();
        
        assertProjectOut(compiledFileNames, testDirPath);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(FilenameNormalization.normalize(sourcePath)));
        ((FlexJSProject)project).unitTestExterns = externs;
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
