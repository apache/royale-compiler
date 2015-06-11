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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.flex.compiler.clients.ExternCConfiguration;
import org.apache.flex.compiler.internal.codegen.externals.reference.ClassReference;
import org.junit.Test;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.NamedType;

public class TestTypeExternals extends ExternalsTestBase
{
    @Test
    public void test_constructor() throws IOException
    {
        compile("constructor_members.js");

        ClassReference reference = model.getClassReference("Foo");
        assertTrue(reference.hasField("bar"));
        assertFalse(reference.hasField("foo"));
        assertTrue(reference.hasMethod("method1"));
        assertTrue(reference.hasMethod("method2"));
        assertTrue(model.hasConstant("bar"));
    }

    @SuppressWarnings("unused")
    @Test
    public void test_types() throws IOException
    {
        compile("types_param.js");

        ClassReference reference = model.getClassReference("Foo");

        JSType jsType1 = getJSType("test1", "arg1");
        JSType jsType2 = getJSType("test2", "arg1");
        JSType jsType3 = getJSType("test3", "arg1");
        JSType jsType4 = getJSType("test4", "arg1");
        JSType jsType5 = getJSType("test5", "arg1");
        JSType jsType6 = getJSType("test6", "arg1");

        assertTrue(jsType1.isString());
        assertTrue(jsType2.isUnionType());
        assertTrue(jsType3.isRecordType());
        assertTrue(jsType4.isUnionType());
        assertTrue(jsType5.isInstanceType());
        assertTrue(jsType6.isFunctionType());

        assertEquals("String", toParamTypeString(jsType1));
        assertEquals("foo.bar.Baz", toParamTypeString(jsType2));
        assertEquals("Object /* {myNum: number, myObject: ?} */",
                toParamTypeString(jsType3));
        assertEquals("Number", toParamTypeString(jsType4));
        assertEquals("Object", toParamTypeString(jsType5));
        assertEquals("Function /* function (string, boolean): ? */",
                toParamTypeString(jsType6));
    }

    public String toParamTypeString(JSType jsType)
    {
        String result = "";
        if (jsType instanceof NamedType)
        {
            NamedType nt = (NamedType) jsType;
            return nt.toAnnotationString();
        }
        else if (jsType.isString())
        {
            return "String";
        }
        else if (jsType.isBooleanObjectType())
        {
            return "Boolean";
        }
        else if (jsType.isNumber())
        {
            return "Number";
        }
        else if (jsType.isUnionType())
        {
            JSType collapseUnion = jsType.restrictByNotNullOrUndefined();
            return toParamTypeString(collapseUnion);
        }
        else if (jsType.isRecordType())
        {
            return "Object /* " + jsType.toAnnotationString() + " */";
        }
        else if (jsType.isInstanceType())
        {
            return jsType.toAnnotationString();
        }
        else if (jsType.isFunctionType())
        {
            return "Function /* " + jsType.toAnnotationString() + " */";
        }

        return result;
    }

    private JSType getJSType(String methodName, String paramName)
    {
        JSDocInfo comment = model.getClassReference("Foo").getMethod(methodName).getComment();
        JSTypeExpression parameterType = comment.getParameterType("arg1");
        JSType jsType = parameterType.evaluate(null,
                model.getCompiler().getTypeRegistry());
        return jsType;
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
    }
}
