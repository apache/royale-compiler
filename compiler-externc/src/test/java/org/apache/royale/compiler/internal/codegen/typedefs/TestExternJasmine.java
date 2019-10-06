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

import com.google.javascript.jscomp.Result;
import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class TestExternJasmine extends TypedefsTestBase
{
    @Test
    public void test_classes() throws IOException
    {
        client.cleanOutput();
        Result result = compile();
        assertTrue(result.success);

        String[] classes = {
                "jasmine",
                "jasmine.Clock"};

        assertEquals(10, model.getClasses().size());
        for (String className : classes)
        {
            assertTrue(model.hasClass(className));
        }

        client.emit();
    }

    @Test
    public void test_members() throws IOException
    {
        client.cleanOutput();
        Result result = compile();
        assertTrue(result.success);

        // jasmine
        ClassReference jasmine = model.getClassReference("jasmine");
        assertNotNull(jasmine);

        assertTrue(jasmine.hasStaticMethod("clock"));
        assertEquals("jasmine.Clock", jasmine.getStaticMethod("clock").toReturnTypeAnnotationString());

        assertTrue(jasmine.hasImport("jasmine.Clock"));

        //Clock
        ClassReference Clock = model.getClassReference("jasmine.Clock");
        assertNotNull(Clock);
    }

    @Override
    protected void configure(ExternCConfiguration install) throws IOException
    {
    	TypedefsTestUtils.init();
        config.setASRoot(TypedefsTestUtils.AS_ROOT_DIR);

        String coreRoot = TypedefsTestUtils.EXTERNAL_JASMINE_DIR.getAbsolutePath();
        config.addTypedef(coreRoot + "/jasmine-2.0.js");
    }

}
