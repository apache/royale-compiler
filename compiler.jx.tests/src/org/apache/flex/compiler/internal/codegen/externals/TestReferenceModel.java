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

package org.apache.flex.compiler.internal.codegen.externals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class TestReferenceModel extends ExternalsTestBase
{
    @Test
    public void test_full_compile() throws IOException
    {
        config.setASRoot(ExternalsTestUtils.AS_ROOT_DIR);

        ExternalsTestUtils.addTestExcludesFull(config);
        ExternalsTestUtils.addTestExternalsFull(config);

        client.cleanOutput();

        // TODO (mschmalle) this root needs to create 'classes' in the root and move 
        // constants and functions up into it aside classes
        assertFalse(ExternalsTestUtils.AS_ROOT_DIR.exists());

        // TODO (mschmalle) get warnings and errors from the closure compiler
        client.compile();

        client.emit();

        File root = ExternalsTestUtils.AS_ROOT_DIR;
        assertTrue(new File(root, "classes").exists());
        assertTrue(new File(root, "interfaces").exists());
        assertTrue(new File(root, "constants").exists());
        assertTrue(new File(root, "functions").exists());
        assertTrue(new File(root, "typedefs").exists());
    }
}
