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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.MethodReference;
import org.junit.Test;

import com.google.javascript.jscomp.Result;

public class TestExternES3 extends TypedefsTestBase
{
    @Test
    public void test_classes() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        String[] classes = {
                "Arguments",
                "Object",
                "Function",
                "Array",
                "Boolean",
                "Number",
                "Date",
                "String",
                "RegExp",
                "Error",
                "EvalError",
                "RangeError",
                "ReferenceError",
                "SyntaxError",
                "TypeError",
                "URIError",
                "Math" };

        // IObject and IArrayLike are two extras
        assertEquals(25, model.getClasses().size());
        for (String className : classes)
        {
            assertTrue(model.hasClass(className));
        }
    }

    @Test
    public void test_Object() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        ClassReference Object = model.getClassReference("Object");
        assertNotNull(Object);
        assertTrue(Object.isDynamic());
    }

    @Test
    public void test_Array() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        ClassReference Array = model.getClassReference("Array");
        assertNotNull(Array);

        MethodReference constructor = Array.getConstructor();
        StringBuilder sb = new StringBuilder();
        constructor.emitCode(sb);
        String emit = sb.toString();
        assertEquals("    public function Array(...var_args):Array {  return null; }\n", emit);
    }

    @Test
    public void test_Array_indexOf() throws IOException
    {
        Result result = compile();
        assertTrue(result.success);

        ClassReference Array = model.getClassReference("Array");
        assertNotNull(Array);

        MethodReference indexOf = Array.getInstanceMethod("indexOf");
        StringBuilder sb = new StringBuilder();
        indexOf.emitCode(sb);
        String emit = sb.toString();
        assertEquals("    public function indexOf(obj:Object, opt_fromIndex:Number = 0):Number { return 0; }\n", emit);
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
    	TypedefsTestUtils.init();
        config.setASRoot(TypedefsTestUtils.AS_ROOT_DIR);

        String coreRoot = TypedefsTestUtils.TYPEDEFS_JS_DIR.getAbsolutePath();
        config.addTypedef(coreRoot + "/es3.js");
    }

}
