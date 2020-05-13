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
import org.apache.royale.compiler.internal.codegen.as.TestGlobalFunctions;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestGoogGlobalFunctions extends TestGlobalFunctions
{
    @Override
    @Test
    public void testArray()
    {
        IVariableNode node = getVariable("var a:Array = Array(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = Array(1)");
    }

    @Override
    @Test
    public void testBoolean()
    {
        IVariableNode node = getVariable("var a:Boolean = Boolean(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = Boolean(1)");
    }

    @Override
    @Test
    public void testDecodeURI()
    {
        IVariableNode node = getVariable("var a:String = decodeURI('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = decodeURI('http://whatever.com')");
    }

    @Override
    @Test
    public void testDecodeURIComponent()
    {
        IVariableNode node = getVariable("var a:String = decodeURIComponent('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = decodeURIComponent('http://whatever.com')");
    }

    @Override
    @Test
    public void testEncodeURI()
    {
        IVariableNode node = getVariable("var a:String = encodeURI('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = encodeURI('http://whatever.com')");
    }

    @Override
    @Test
    public void testEncodeURIComponent()
    {
        IVariableNode node = getVariable("var a:String = encodeURIComponent('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = encodeURIComponent('http://whatever.com')");
    }

    @Override
    @Test
    public void testEscape()
    {
        IVariableNode node = getVariable("var a:String = escape('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = escape('http://whatever.com')");
    }

    @Override
    @Test
    public void testInt()
    {
        IVariableNode node = getVariable("var a:int = int(1.8);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = int(1.8)");
    }

    @Override
    @Test
    public void testIsFinite()
    {
        IVariableNode node = getVariable("var a:Boolean = isFinite(1000000.9);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = isFinite(1000000.9)");
    }

    @Override
    @Test
    public void testIsNaN()
    {
        IVariableNode node = getVariable("var a:Boolean = isNaN(NaN);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = isNaN(NaN)");
    }

    //isXMLName is in E4X, which is not supported by JavaScript
/*    @Ignore*/
    @Override
    @Test
    public void testIsXMLName()
    {
        IVariableNode node = getVariable("var a:Boolean = isXMLName(\"?\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = isXMLName(\"?\")");
    }

    @Override
    @Test
    public void testNumber()
    {
        IVariableNode node = getVariable("var a:Number = Number(\"1\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = Number(\"1\")");
    }

    @Override
    @Test
    public void testObject()
    {
        IVariableNode node = getVariable("var a:Object = Object(\"1\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = Object(\"1\")");
    }

    @Override
    @Test
    public void testParseFloat()
    {
        IVariableNode node = getVariable("var a:Number = parseFloat(\"1.8\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = parseFloat(\"1.8\")");
    }

    @Override
    @Test
    public void testParseInt()
    {
        IVariableNode node = getVariable("var a:Number = parseInt(\"666\", 10);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = parseInt(\"666\", 10)");
    }

    @Override
    @Test
    public void testString()
    {
        IVariableNode node = getVariable("var a:String = String(100);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = String(100)");
    }

    @Override
    @Test
    public void testTrace()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "trace('Hello World');", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("trace('Hello World')");
    }

    @Override
    @Test
    public void testUint()
    {
        IVariableNode node = getVariable("var a:uint = uint(-100);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = uint(-100)");
    }

    @Override
    @Test
    public void testUnescape()
    {
        IVariableNode node = getVariable("var a:String = unescape('%25');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = unescape('%25')");
    }

    @Override
    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<string>} */ a = Array(['Hello', 'World'])");
    }

    @Override
    @Test
    public void testXML()
    {
        IVariableNode node = getVariable("var a:XML = XML('@');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = XML('@')");
    }

    @Override
    @Test
    public void testXMLList()
    {
        IVariableNode node = getVariable("var a:XMLList = XMLList('<!-- comment -->');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ a = XMLList('<!-- comment -->')");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
