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

package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.codegen.TestWalkerBase;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class tests the production of valid 'goog' JS code from an external file.
 * 
 * @author Erik de Bruin
 */
public class TestGoogFile extends TestWalkerBase
{
    @Test
    public void testFile_plain()
    {
        IFileNode node = getFileNode("input", true);
        visitor.visitFile(node);
        assertOut(getCodeFromFile("output", true));
    }

    @Test
    public void testFile_getset()
    {
        IFileNode node = getFileNode("get-set", true);
        visitor.visitFile(node);
        assertOut(getCodeFromFile("get-set_result", true));
    }

    @Ignore
    @Test
    public void testFile_callsuper()
    {
    	// TODO (erikdebruin) handle various constructor super call edge cases first
        IFileNode node = getFileNode("call-super", true);
        visitor.visitFile(node);
        assertOut(getCodeFromFile("call-super_result", true));
    }

    @Test
    public void testFile_qualifynewobject()
    {
        IFileNode node = getFileNode("qualify-new-object", true);
        visitor.visitFile(node);
        assertOut(getCodeFromFile("qualify-new-object_result", true));
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
