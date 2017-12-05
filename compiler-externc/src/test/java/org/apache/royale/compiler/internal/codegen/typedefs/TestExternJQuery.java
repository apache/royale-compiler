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

package org.apache.royale.compiler.internal.codegen.typedefs;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.junit.Test;

import com.google.javascript.jscomp.Result;

public class TestExternJQuery extends TypedefsTestBase
{
    @SuppressWarnings("unused")
    @Test
    public void test_classes() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        //        String[] classes = {};
        //
        //        assertEquals(17, model.getClasses().size());
        //        for (String className : classes)
        //        {
        //            assertTrue(model.hasClass(className));
        //        }

        ClassReference jQuery_Promise = model.getInterfaceReference("jQuery.Promise");
        assertNotNull(jQuery_Promise);

        StringBuilder sb = new StringBuilder();
        jQuery_Promise.emit(sb);
        String r = sb.toString();
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
    	TypedefsTestUtils.init();
        config.setASRoot(TypedefsTestUtils.AS_ROOT_DIR);

        String coreRoot = TypedefsTestUtils.EXTERNAL_JQUERY_DIR.getAbsolutePath();
        config.addTypedef(coreRoot + "/jquery-1.9.js");
    }

}
