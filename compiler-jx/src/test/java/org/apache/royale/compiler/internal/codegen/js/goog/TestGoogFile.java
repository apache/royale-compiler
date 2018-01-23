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

import java.io.File;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.junit.Test;

/**
 * This class tests the production of valid 'goog' JS code from an external
 * file.
 * 
 * @author Erik de Bruin
 */
public class TestGoogFile extends ASTestBase
{
    @Test
    public void testFile_plain()
    {
        IFileNode node = compileAS("input", true, "goog" + File.separator
                + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("output", true, "goog" + File.separator
                + "files"));
    }

    @Test
    public void testFile_getset()
    {
        IFileNode node = compileAS("get-set", true, "goog" + File.separator
                + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("get-set_result", true, "goog"
                + File.separator + "files"));
    }

    @Test
    public void testFile_callsuper()
    {
        IFileNode node = compileAS("call-super", true, "goog" + File.separator
                + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("call-super_result", true, "goog"
                + File.separator + "files"));
    }

    @Test
    public void testFile_qualifynewobject()
    {
        IFileNode node = compileAS("qualify-new-object", true, "goog"
                + File.separator + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("qualify-new-object_result", true, "goog"
                + File.separator + "files"));
    }

    @Test
    public void testFile_poc()
    {
        IFileNode node = compileAS("poc", true, "goog" + File.separator
                + "files");
        asBlockWalker.visitFile(node);
        assertOut(getCodeFromFile("poc_result", true, "goog" + File.separator
                + "files"));
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
