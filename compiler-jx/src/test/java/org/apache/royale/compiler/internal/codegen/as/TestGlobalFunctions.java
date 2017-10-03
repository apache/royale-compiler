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
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestGlobalFunctions extends ASTestBase
{
    @Test
    public void testArray()
    {
        IVariableNode node = getVariable("var a:Array = Array(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Array = Array(1)");
    }

    @Test
    public void testBoolean()
    {
        IVariableNode node = getVariable("var a:Boolean = Boolean(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Boolean = Boolean(1)");
    }

    @Test
    public void testDecodeURI()
    {
        IVariableNode node = getVariable("var a:String = decodeURI('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:String = decodeURI('http://whatever.com')");
    }

    @Test
    public void testDecodeURIComponent()
    {
        IVariableNode node = getVariable("var a:String = decodeURIComponent('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:String = decodeURIComponent('http://whatever.com')");
    }

    @Test
    public void testEncodeURI()
    {
        IVariableNode node = getVariable("var a:String = encodeURI('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:String = encodeURI('http://whatever.com')");
    }

    @Test
    public void testEncodeURIComponent()
    {
        IVariableNode node = getVariable("var a:String = encodeURIComponent('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:String = encodeURIComponent('http://whatever.com')");
    }

    @Test
    public void testEscape()
    {
        IVariableNode node = getVariable("var a:String = escape('http://whatever.com');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:String = escape('http://whatever.com')");
    }

    @Test
    public void testInt()
    {
        IVariableNode node = getVariable("var a:int = int(1.8);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:int = int(1.8)");
    }

    @Test
    public void testIsFinite()
    {
        IVariableNode node = getVariable("var a:Boolean = isFinite(1000000.9);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Boolean = isFinite(1000000.9)");
    }

    @Test
    public void testIsNaN()
    {
        IVariableNode node = getVariable("var a:Boolean = isNaN(NaN);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Boolean = isNaN(NaN)");
    }

    @Test
    public void testIsXMLName()
    {
        IVariableNode node = getVariable("var a:Boolean = isXMLName(\"?\");");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Boolean = isXMLName(\"?\")");
    }

    @Test
    public void testNumber()
    {
        IVariableNode node = getVariable("var a:Number = Number(\"1\");");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Number = Number(\"1\")");
    }

    @Test
    public void testObject()
    {
        IVariableNode node = getVariable("var a:Object = Object(\"1\");");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Object = Object(\"1\")");
    }

    @Test
    public void testParseFloat()
    {
        IVariableNode node = getVariable("var a:Number = parseFloat(\"1.8\");");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Number = parseFloat(\"1.8\")");
    }

    @Test
    public void testParseInt()
    {
        IVariableNode node = getVariable("var a:Number = parseInt(\"666\", 10);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Number = parseInt(\"666\", 10)");
    }

    @Test
    public void testString()
    {
        IVariableNode node = getVariable("var a:String = String(100);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:String = String(100)");
    }

    @Test
    public void testTrace()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "trace('Hello World');", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("trace('Hello World')");
    }

    @Test
    public void testUint()
    {
        IVariableNode node = getVariable("var a:uint = uint(-100);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:uint = uint(-100)");
    }

    @Test
    public void testUnescape()
    {
        IVariableNode node = getVariable("var a:String = unescape('%25');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:String = unescape('%25')");
    }

    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var a:Vector.<String> = Vector.<String>(['Hello', 'World'])");
    }

    @Test
    public void testXML()
    {
        IVariableNode node = getVariable("var a:XML = XML('@');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:XML = XML('@')");
    }

    @Test
    public void testXMLList()
    {
        IVariableNode node = getVariable("var a:XMLList = XMLList('<!-- comment -->');");
        asBlockWalker.visitVariable(node);
        assertOut("var a:XMLList = XMLList('<!-- comment -->')");
    }
}
