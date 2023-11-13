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

package org.apache.royale.compiler.internal.codegen.js.royale;

import java.io.File;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.test.RoyaleTestBase;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.Test;

/**
 * This class tests the production of valid 'goog' JS code from an external
 * file.
 * 
 * @author Erik de Bruin
 */
public class TestRoyaleFile extends RoyaleTestBase
{
    @Override
    public void setUp()
    {
        super.setUp();
        project.config = new JSGoogConfiguration();
    }

    @Test
    public void testFile_plain()
    {
        IFileNode node = compileAS("input", true, "royale" + File.separator
                + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("output", true, "royale" + File.separator
                + "files"));
    }

    @Test
    public void testFile_getset()
    {
        IFileNode node = compileAS("get-set", true, "royale" + File.separator
                + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("get-set_result", true, "royale"
                + File.separator + "files"));
    }

    @Test
    public void testFile_callsuper()
    {
        IFileNode node = compileAS("call-super", true, "royale" + File.separator
                + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("call-super_result", true, "royale"
                + File.separator + "files"));
    }

    @Test
    public void testFile_qualifynewobject()
    {
        IFileNode node = compileAS("qualify-new-object", true, "royale"
                + File.separator + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("qualify-new-object_result", true, "royale"
                + File.separator + "files"));
    }

    @Test
    public void testFile_poc()
    {
        IFileNode node = compileAS("poc", true, "royale" + File.separator
                + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("poc_result", true, "royale" + File.separator
                + "files"));
    }

    @Test
    public void testLocalFunction()
    {
        String fileName = "LocalFunction";

        IFileNode node = compileAS(fileName, true,
                new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                        "royale/files").getPath(),
                false);
        
        asBlockWalker.visitFile(node);
        
        //writeResultToFile(writer.toString(), fileName);
        
        assertOutWithMetadata(getCodeFromFile(fileName + "_result", true,
                "royale" + File.separator + "files"));
    }

    @Test
    public void testRoyaleMyController()
    {
        String fileName = "controllers/MyController";

        IFileNode node = compileAS(fileName, true,
                new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                        "royale/files").getPath(),
                false);
        
        asBlockWalker.visitFile(node);
        
        //writeResultToFile(writer.toString(), fileName);
        
        assertOutPostProcess(getCodeFromFile(fileName + "_result", true,
                "royale" + File.separator + "files"), true);
    }

    @Test
    public void testRoyaleMyModel()
    {
        String fileName = "models/MyModel";

        IFileNode node = compileAS(fileName, true,
                new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                        "royale/files").getPath(),
                false);

        asBlockWalker.visitFile(node);
        
        //writeResultToFile(writer.toString(), fileName);
        
        assertOutPostProcess(getCodeFromFile(fileName + "_result", true,
                "royale" + File.separator + "files"), true);
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
