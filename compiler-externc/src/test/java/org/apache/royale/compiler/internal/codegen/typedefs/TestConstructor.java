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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.internal.codegen.typedefs.reference.ClassReference;
import org.junit.Test;

import com.google.javascript.rhino.jstype.JSType.Nullability;

public class TestConstructor extends TypedefsTestBase
{

    @Test
    public void test_const_object_literal() throws IOException
    {
        compile("constructor_params.js");

        assertTrue(model.hasClass("FinalClass"));
        //assertTrue(model.getClassReference("FinalClass").isFinal());
        assertTrue(model.getClassReference("FinalClass").hasStaticMethod("bar"));
        assertTrue(model.getClassReference("FinalClass").getStaticMethod("bar").isStatic());
    }

    @Test
    public void test_constructor_args() throws IOException
    {
        compile("constructor_params.js");

        ClassReference FooNoArgs = model.getClassReference("FooNoArgs");
        ClassReference FooOptArgs = model.getClassReference("FooOptArgs");
        ClassReference FooVarArgs = model.getClassReference("FooVarArgs");
        ClassReference FooOptVarArgs = model.getClassReference("FooOptVarArgs");

        assertNotNull(FooNoArgs.getConstructor());
        assertNotNull(FooOptArgs.getConstructor());
        assertNotNull(FooVarArgs.getConstructor());
        assertNotNull(FooOptVarArgs.getConstructor());

        assertEquals(0, FooNoArgs.getConstructor().getParameterNames().size());
        assertEquals(2, FooOptArgs.getConstructor().getParameterNames().size());
        assertEquals(2, FooVarArgs.getConstructor().getParameterNames().size());
        assertEquals(3,
                FooOptVarArgs.getConstructor().getParameterNames().size());

        assertFalse(FooOptArgs.getConstructor().getComment().getParameterType(
                "arg1").isOptionalArg());
        assertTrue(FooOptArgs.getConstructor().getComment().getParameterType(
                "opt_arg2").isOptionalArg());

        assertFalse(FooVarArgs.getConstructor().getComment().getParameterType(
                "arg1").isVarArgs());
        assertTrue(FooVarArgs.getConstructor().getComment().getParameterType(
                "var_args").isVarArgs());

        assertTrue(FooOptVarArgs.getConstructor().getComment().getParameterType(
                "opt_arg2").isOptionalArg());
        assertTrue(FooOptVarArgs.getConstructor().getComment().getParameterType(
                "var_args").isVarArgs());

        assertEquals(
                "number",
                evaluateParam(FooOptVarArgs.getConstructor(), "arg1").toAnnotationString(Nullability.EXPLICIT));
        assertEquals(
                "*",
                evaluateParam(FooOptVarArgs.getConstructor(), "opt_arg2").toAnnotationString(Nullability.EXPLICIT));
        assertEquals(
                "*",
                evaluateParam(FooOptVarArgs.getConstructor(), "var_args").toAnnotationString(Nullability.EXPLICIT));
    }

    @Test
    public void test_constructor_comment() throws IOException
    {
        compile("constructor_params.js");

        StringBuilder sb = new StringBuilder();

        ClassReference FooOptVarArgs = model.getClassReference("FooOptVarArgs");
        FooOptVarArgs.getConstructor().emit(sb);
        String string = sb.toString();
        assertEquals(
                "    /**\n     * A constructor with arg, opt arg and var args.\n     *\n     "
                        + "* @param arg1 [number] The arg 1.\n     * @param opt_arg2 [*] The arg  "
                        + "that is wrapped by another line in the comment.\n     * @param var_args "
                        + "[*] A var agr param.\n     * @see http://foo.bar.com \n     * @see "
                        + "[constructor_params]\n     * @returns {(FooVarArgs|null)} Another instance.\n"
                        + "     */\n    public function FooOptVarArgs(arg1:Number, opt_arg2:* = null, ...var_args) "
                        + "{\n        super();\n    }\n", string);
    }

    @Override
    protected void configure(ExternCConfiguration config) throws IOException
    {
    }

}
