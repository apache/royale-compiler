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

package org.apache.royale.compiler.internal.codegen.js.goog;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.as.TestGlobalClasses;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestGoogGlobalClasses extends TestGlobalClasses
{
    @Override
    @Test
    public void testArgumentError()
    {
        IVariableNode node = getVariable("var a:ArgumentError = new ArgumentError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {ArgumentError} */ a = new ArgumentError()");
    }

    @Override
    @Test
    public void testArguments()
    {
        IFunctionNode node = getMethod("function a():void {\ttrace(arguments);}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.a = function() {\n\tvar self = this;\n\ttrace(arguments);\n}");
    }

    @Override
    @Test
    public void testArray()
    {
        IVariableNode node = getVariable("var a:Array = new Array(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(1)");
    }

    @Override
    @Test
    public void testBoolean()
    {
        IVariableNode node = getVariable("var a:Boolean = new Boolean(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = new Boolean(1)");
    }

    @Override
    @Test
    public void testClass()
    {
        IVariableNode node = getVariable("var a:Class = new Class();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = new Class()");
    }

    @Override
    @Test
    public void testDate()
    {
        IVariableNode node = getVariable("var a:Date = new Date();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Date} */ a = new Date()");
    }

    @Override
    @Test
    public void testDefinitionError()
    {
        IVariableNode node = getVariable("var a:DefinitionError = new DefinitionError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {DefinitionError} */ a = new DefinitionError()");
    }

    @Override
    @Test
    public void testError()
    {
        IVariableNode node = getVariable("var a:Error = new Error();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Error} */ a = new Error()");
    }

    @Override
    @Test
    public void testEvalError()
    {
        IVariableNode node = getVariable("var a:EvalError = new EvalError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {EvalError} */ a = new EvalError()");
    }

    @Override
    @Test
    public void testFunction()
    {
        IVariableNode node = getVariable("var a:Function = new Function();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Function} */ a = new Function()");
    }

    @Override
    @Test
    public void testInt()
    {
        IVariableNode node = getVariable("var a:int = new int(1.8);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = new int(1.8)");
    }

    @Override
    @Test
    public void testJSON()
    {
        IVariableNode node = getVariable("var a:JSON = new JSON();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {JSON} */ a = new JSON()");
    }

    @Override
    @Test
    public void testMath()
    {
        IVariableNode node = getVariable("var a:Number = Math.PI;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = Math.PI");
    }

    @Override
    @Test
    public void testNamespace()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace(\"http://example.com\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Namespace} */ a = new Namespace(\"http://example.com\")");
    }

    @Override
    @Test
    public void testNumber()
    {
        IVariableNode node = getVariable("var a:Number = new Number(\"1\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = new Number(\"1\")");
    }

    @Override
    @Test
    public void testObject()
    {
        IVariableNode node = getVariable("var a:Object = new Object();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = new Object()");
    }

    @Override
    @Test
    public void testQName()
    {
        IVariableNode node = getVariable("var a:QName = new QName();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {QName} */ a = new QName()");
    }

    @Override
    @Test
    public void testRangeError()
    {
        IVariableNode node = getVariable("var a:RangeError = new RangeError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RangeError} */ a = new RangeError()");
    }

    @Override
    @Test
    public void testReferenceError()
    {
        IVariableNode node = getVariable("var a:ReferenceError = new ReferenceError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {ReferenceError} */ a = new ReferenceError()");
    }

    @Override
    @Test
    public void testRegExp_Literal()
    {
        IVariableNode node = getVariable("var a:RegExp = /test-\\d/i;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /test-\\d/i");
    }

    @Override
    @Test
    public void testSecurityError()
    {
        IVariableNode node = getVariable("var a:SecurityError = new SecurityError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {SecurityError} */ a = new SecurityError()");
    }

    @Override
    @Test
    public void testString()
    {
        IVariableNode node = getVariable("var a:String = new String(\"100\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = new String(\"100\")");
    }

    @Override
    @Test
    public void testSyntaxError()
    {
        IVariableNode node = getVariable("var a:SyntaxError = new SyntaxError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {SyntaxError} */ a = new SyntaxError()");
    }

    @Override
    @Test
    public void testTypeError()
    {
        IVariableNode node = getVariable("var a:TypeError = new TypeError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {TypeError} */ a = new TypeError()");
    }

    @Override
    @Test
    public void testUint()
    {
        IVariableNode node = getVariable("var a:uint = new uint(-100);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = new uint(-100)");
    }

    @Override
    @Test
    public void testURIError()
    {
        IVariableNode node = getVariable("var a:URIError = new URIError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {URIError} */ a = new URIError()");
    }

    @Override
    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<string>} */ a = new Array(['Hello', 'World'])");
    }

    @Override
    @Test
    public void testVerifyError()
    {
        IVariableNode node = getVariable("var a:VerifyError = new VerifyError();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {VerifyError} */ a = new VerifyError()");
    }

    @Override
    @Test
    public void testXML()
    {
        IVariableNode node = getVariable("var a:XML = new XML('@');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML('@')");
    }

    @Override
    @Test
    public void testXMLList()
    {
        IVariableNode node = getVariable("var a:XMLList = new XMLList('<!-- comment -->');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ a = new XMLList('<!-- comment -->')");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
