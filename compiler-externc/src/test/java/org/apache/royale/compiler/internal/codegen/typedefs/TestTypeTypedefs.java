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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.MethodReference;
import org.apache.royale.compiler.internal.codegen.typedefs.utils.JSTypeUtils;

import org.junit.Test;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.jstype.JSType;

public class TestTypeTypedefs extends TypedefsTestBase
{
    @Test
    public void test_constructor() throws IOException
    {
        compile("constructor_members.js");

        ClassReference reference = model.getClassReference("Foo");
        assertTrue(reference.hasInstanceField("bar"));
        assertFalse(reference.hasInstanceField("foo"));
        assertTrue(reference.hasInstanceMethod("method1"));
        assertTrue(reference.hasInstanceMethod("method2"));
        assertTrue(model.hasConstant("bar"));
    }

    @Test
    public void test_types() throws IOException
    {
        compile("types_param.js");

        ClassReference reference = model.getClassReference("Foo");

        JSType jsType1 = getJSType("test1", true, "arg1");
        JSType jsType2 = getJSType("test2", true, "arg1");
        JSType jsType3 = getJSType("test3", true, "arg1");
        JSType jsType4 = getJSType("test4", true, "arg1");
        JSType jsType5 = getJSType("test5", true, "arg1");
        JSType jsType6 = getJSType("test6", true, "arg1");

        assertTrue(jsType1.isString());
        assertTrue(jsType2.isUnionType());
        assertTrue(jsType3.isRecordType());
        assertTrue(jsType4.isUnionType());
        assertTrue(jsType5.isInstanceType());
        assertTrue(jsType6.isFunctionType());

        assertEquals("String",
                JSTypeUtils.toParamTypeString(reference.getStaticMethod("test1"), "arg1"));
        assertEquals("foo.bar.Baz",
                JSTypeUtils.toParamTypeString(reference.getStaticMethod("test2"), "arg1"));
        assertEquals("Object /* {myNum: number, myObject: ?} */",
                JSTypeUtils.toParamTypeString(reference.getStaticMethod("test3"), "arg1"));
        assertEquals("Number",
                JSTypeUtils.toParamTypeString(reference.getStaticMethod("test4"), "arg1"));
        assertEquals("Object",
                JSTypeUtils.toParamTypeString(reference.getStaticMethod("test5"), "arg1"));
        assertEquals("Function /* function(string, boolean): ? */",
                JSTypeUtils.toParamTypeString(reference.getStaticMethod("test6"), "arg1"));
    }

    private JSType getJSType(String methodName, boolean isStatic, String paramName)
    {
    	MethodReference method = null;
    	if(isStatic)
        {
            method = model.getClassReference("Foo").getStaticMethod(methodName);
        }
        else
        {
            method = model.getClassReference("Foo").getInstanceMethod(methodName);
        }
        JSDocInfo comment = method.getComment();
        JSTypeExpression parameterType = comment.getParameterType(paramName);
        JSType jsType = model.evaluate(parameterType);
        return jsType;
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
    }
}
