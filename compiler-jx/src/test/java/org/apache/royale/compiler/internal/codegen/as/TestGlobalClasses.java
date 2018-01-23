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
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestGlobalClasses extends ASTestBase
{
    @Test
    public void testArgumentError()
    {
        IVariableNode node = getVariable("var a:ArgumentError = new ArgumentError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:ArgumentError = new ArgumentError()");
    }

    @Test
    public void testArguments()
    {
        IFunctionNode node = getMethod("function a():void {\ttrace(arguments);}");
        asBlockWalker.visitFunction(node);
        assertOut("function a():void {\n\ttrace(arguments);\n}");
    }

    @Test
    public void testArray()
    {
        IVariableNode node = getVariable("var a:Array = new Array(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Array = new Array(1)");
    }

    @Test
    public void testBoolean()
    {
        IVariableNode node = getVariable("var a:Boolean = new Boolean(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Boolean = new Boolean(1)");
    }

    @Test
    public void testClass()
    {
        IVariableNode node = getVariable("var a:Class = Class(FooBar)");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Class = Class(FooBar)");
    }

    @Test
    public void testDate()
    {
        IVariableNode node = getVariable("var a:Date = new Date();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Date = new Date()");
    }

    @Test
    public void testDefinitionError()
    {
        IVariableNode node = getVariable("var a:DefinitionError = new DefinitionError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:DefinitionError = new DefinitionError()");
    }

    @Test
    public void testError()
    {
        IVariableNode node = getVariable("var a:Error = new Error();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Error = new Error()");
    }

    @Test
    public void testEvalError()
    {
        IVariableNode node = getVariable("var a:EvalError = new EvalError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:EvalError = new EvalError()");
    }

    @Test
    public void testFunction()
    {
        IVariableNode node = getVariable("var a:Function = new Function();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Function = new Function()");
    }

    @Test
    public void testInt()
    {
        IVariableNode node = getVariable("var a:int = new int(1.8);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:int = new int(1.8)");
    }

    @Test
    public void testJSON()
    {
        IVariableNode node = getVariable("var a:JSON = new JSON();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:JSON = new JSON()");
    }

    @Test
    public void testMath()
    {
        IVariableNode node = getVariable("var a:Number = Math.PI;");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Number = Math.PI");
    }

    @Test
    public void testNamespace()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace(\"http://example.com\");");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Namespace = new Namespace(\"http://example.com\")");
    }

    @Test
    public void testNumber()
    {
        IVariableNode node = getVariable("var a:Number = new Number(\"1\");");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Number = new Number(\"1\")");
    }

    @Test
    public void testObject()
    {
        IVariableNode node = getVariable("var a:Object = new Object();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Object = new Object()");
    }

    @Test
    public void testQName()
    {
        IVariableNode node = getVariable("var a:QName = new QName();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:QName = new QName()");
    }

    @Test
    public void testRangeError()
    {
        IVariableNode node = getVariable("var a:RangeError = new RangeError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:RangeError = new RangeError()");
    }

    @Test
    public void testReferenceError()
    {
        IVariableNode node = getVariable("var a:ReferenceError = new ReferenceError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:ReferenceError = new ReferenceError()");
    }

    // TODO (mschmalle) the backslashes in don't match the backslashes out...
    @Test
    public void testRegExp()
    {
//        IVariableNode node = getVariable("var a:RegExp = new RegExp('test-\\d', 'i');");
//        asBlockWalker.visitVariable(node);
//        assertOut("var a:RegExp = new RegExp('test-\\\\d', 'i')");
    }

    @Test
    public void testRegExp_Literal()
    {
        IVariableNode node = getVariable("var a:RegExp = /test-\\d/i;");
        asBlockWalker.visitVariable(node);
        assertOut("var a:RegExp = /test-\\d/i");
    }

    @Test
    public void testSecurityError()
    {
        IVariableNode node = getVariable("var a:SecurityError = new SecurityError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:SecurityError = new SecurityError()");
    }

    @Test
    public void testString()
    {
        IVariableNode node = getVariable("var a:String = new String(\"100\");");
        asBlockWalker.visitVariable(node);
        assertOut("var a:String = new String(\"100\")");
    }

    @Test
    public void testSyntaxError()
    {
        IVariableNode node = getVariable("var a:SyntaxError = new SyntaxError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:SyntaxError = new SyntaxError()");
    }

    @Test
    public void testTypeError()
    {
        IVariableNode node = getVariable("var a:TypeError = new TypeError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:TypeError = new TypeError()");
    }

    @Test
    public void testUint()
    {
        IVariableNode node = getVariable("var a:uint = new uint(-100);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:uint = new uint(-100)");
    }

    @Test
    public void testURIError()
    {
        IVariableNode node = getVariable("var a:URIError = new URIError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:URIError = new URIError()");
    }

    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Vector.<String> = new Vector.<String>(['Hello', 'World'])");
    }

    @Test
    public void testVerifyError()
    {
        IVariableNode node = getVariable("var a:VerifyError = new VerifyError();");
        asBlockWalker.visitVariable(node);
        assertOut("var a:VerifyError = new VerifyError()");
    }

    @Test
    public void testXML()
    {
        IVariableNode node = getVariable("var a:XML = new XML('@');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:XML = new XML('@')");
    }

    @Test
    public void testXMLList()
    {
        IVariableNode node = getVariable("var a:XMLList = new XMLList('<!-- comment -->');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:XMLList = new XMLList('<!-- comment -->')");
    }
}
