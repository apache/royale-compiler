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

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.flex.compiler.clients.ExternCConfiguration;
import org.junit.Test;

import com.google.javascript.jscomp.Result;

public class TestExternChrome extends ExternalsTestBase
{
    @Test
    public void test_classes() throws IOException
    {
        client.cleanOutput();
        Result result = compile();
        assertTrue(result.success);
        client.emit();
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
        config.setASRoot(ExternalsTestUtils.AS_ROOT_DIR);

        String coreRoot = ExternalsTestUtils.EXTERNAL_JS_DIR.getAbsolutePath();
        config.addExternal(coreRoot + "/chrome.js");
    }

}
