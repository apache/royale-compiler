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

import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;

import com.google.javascript.jscomp.Result;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestExternJSMissing extends TypedefsTestBase
{
    @Test
    public void test_classes() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        String[] classes = {
                "int",
                "uint",
                "Class" };

        for (String className : classes)
        {
            assertTrue(model.hasClass(className));
        }
    }
    @Test
    public void test_functions() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        String[] functions = {
                "trace" };

        for (String functionName : functions)
        {
            assertTrue(model.hasFunction(functionName));
        }
    }

    @Test
    public void test_Class() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        ClassReference ClassClass = model.getClassReference("Class");
        assertNotNull(ClassClass);
        assertTrue(ClassClass.isDynamic());
    }

    @Test
    public void test_int() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        ClassReference intClass = model.getClassReference("int");
        assertNotNull(intClass);
        assertTrue(intClass.hasStaticField("MIN_VALUE"));
        assertTrue(intClass.hasStaticField("MAX_VALUE"));
    }

    @Test
    public void test_uint() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        ClassReference uintClass = model.getClassReference("uint");
        assertNotNull(uintClass);
        assertTrue(uintClass.hasStaticField("MIN_VALUE"));
        assertTrue(uintClass.hasStaticField("MAX_VALUE"));
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
        TypedefsTestUtils.init();
        config.setASRoot(TypedefsTestUtils.AS_ROOT_DIR);

        String coreRoot = TypedefsTestUtils.TYPEDEFS_JS_DIR.getAbsolutePath();
        System.out.println(coreRoot);
        config.addTypedef(coreRoot + "/es3.js");
        config.addTypedef(TypedefsTestUtils.MISSING_JS_FILE);
    }
}
