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

package org.apache.royale.compiler.internal.codegen.as;

import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * This class tests the production of valid ActionScript3 code for Class Method
 * members.
 * 
 * @author Michael Schmalle
 */
public class TestMethodMembers extends ASTestBase
{
    /*
     * Method
     * 
     * function foo(){}
     * function foo():int{}
     * function foo(bar):int{}
     * function foo(bar:String):int{}
     * function foo(bar:String = "baz"):int{}
     * function foo(bar:String, baz:int = null):int{}
     * function foo(bar:String, ...rest):int{}
     * public function foo(bar:String, baz:int = null):int{}
     * public static function foo(bar:String, baz:int = null):int{}
     */

    //--------------------------------------------------------------------------
    // Method
    //--------------------------------------------------------------------------

    @Test
    public void testMethod()
    {
        IFunctionNode node = getMethod("function foo(){}");
        asBlockWalker.visitFunction(node);
        assertOut("function foo() {\n}");
    }

    @Test
    public void testMethod_withReturnType()
    {
        IFunctionNode node = getMethod("function foo():int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("function foo():int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withParameterReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("function foo(bar:*):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("function foo(bar:String):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withDefaultParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String = \"baz\"):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("function foo(bar:String = \"baz\"):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withMultipleDefaultParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("function foo(bar:String, baz:int = null):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withRestParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String, ...rest):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("function foo(bar:String, ...rest):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withNamespace()
    {
        IFunctionNode node = getMethod("public function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("public function foo(bar:String, baz:int = null):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withNamespaceCustom()
    {
        IFunctionNode node = getMethod("mx_internal function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("mx_internal function foo(bar:String, baz:int = null):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withNamespaceModifiers()
    {
        IFunctionNode node = getMethod("public static function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("public static function foo(bar:String, baz:int = null):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withNamespaceModifierOverride()
    {
        IFunctionNode node = getMethod("public override function foo(bar:String, baz:int = null):int{\treturn -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("public override function foo(bar:String, baz:int = null):int {\n\treturn -1;\n}");
    }

    @Test
    public void testMethod_withNamespaceModifierOverrideBackwards()
    {
        IFunctionNode node = getMethod("override public function foo(bar:String, baz:int = null):int{return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("public override function foo(bar:String, baz:int = null):int {\n\treturn -1;\n}");
    }
}
