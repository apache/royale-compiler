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

import java.io.File;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * This class tests the production of valid 'goog' JS code from an external
 * file.
 * 
 * @author Erik de Bruin
 */
public class TestFlexJSFile extends FlexJSTestBase
{
    @Test
    public void testLocalFunction()
    {
        String fileName = "LocalFunction";

        IFileNode node = compileAS(fileName, true,
                "test-files"
                        + File.separator + "flexjs" + File.separator + "files",
                false);
        
        asBlockWalker.visitFile(node);
        
        //writeResultToFile(writer.toString(), fileName);
        
        assertOut(getCodeFromFile(fileName + "_result", true,
                "flexjs" + File.separator + "files"));
    }

    @Test
    public void testFlexJSMyController()
    {
        String fileName = "MyController";

        IFileNode node = compileAS(fileName, true,
                "test-files"
                        + File.separator + "flexjs" + File.separator + "files",
                false);
        
        asBlockWalker.visitFile(node);
        
        writeResultToFile(writer.toString(), fileName);
        
        assertOut(getCodeFromFile(fileName + "_result", true,
                "flexjs" + File.separator + "files"));
    }

    @Test
    public void testFlexJSMyModel()
    {
        String fileName = "MyModel";

        IFileNode node = compileAS(fileName, true, "test-files"
                + File.separator + "flexjs" + File.separator + "files", false);

        asBlockWalker.visitFile(node);
        
        writeResultToFile(writer.toString(), fileName);
        
        assertOut(getCodeFromFile(fileName + "_result", true,
                "flexjs" + File.separator + "files"));
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
