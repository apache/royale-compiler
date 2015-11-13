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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogGlobalClasses;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.junit.Test;
import org.junit.Ignore;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSGlobalClasses extends TestGoogGlobalClasses
{

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

    @Override
    @Test
    public void testArguments()
    {
        IFunctionNode node = getMethod("function a():void {  trace(arguments);}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.a = function() {\n  org.apache.flex.utils.Language.trace(arguments);\n}");
    }

    @Test
    public void testArrayNoArgs()
    {
        IVariableNode node = getVariable("var a:Array = new Array();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array()");
    }

    @Test
    public void testArrayStringArgs()
    {
        IVariableNode node = getVariable("var a:Array = new Array('Hello', 'World');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array('Hello', 'World')");
    }

    @Test
    public void testArraySizeArg()
    {
        IVariableNode node = getVariable("var a:Array = new Array(30);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(30)");
    }

    @Test
    public void testArrayNumberArgs()
    {
        IVariableNode node = getVariable("var a:Array = new Array(30, 40);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(30, 40)");
    }

    @Test
    public void testArrayArrayArg()
    {
        IVariableNode node = getVariable("var a:Array = new Array(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }

    @Override
    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }

    @Test
    public void testVectorLiteral_1()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new <String>[];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = []");
    }

    @Test
    public void testVectorLiteral_2()
    {
        IVariableNode node = getVariable("var a:Vector.<int> = new <int>[0, 1, 2, 3];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = [0, 1, 2, 3]");
    }

    @Test
    public void testVectorLiteral_3()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new <String>[\"one\", \"two\", \"three\";");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = [\"one\", \"two\", \"three\"]");
    }
    
    @Test
    public void testVectorNoArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array()");
    }

    @Test
    public void testVectorStringArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>('Hello', 'World');");
        asBlockWalker.visitVariable(node);
        // expected error?
        assertOut("var /** @type {Array} */ a = new Array('Hello', 'World')");
    }

    @Test
    public void testVectorStringArgs3()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>('Hello', 'World', 'Three');");
        asBlockWalker.visitVariable(node);
        // expected error?
        assertOut("var /** @type {Array} */ a = new Array('Hello', 'World', 'Three')");
    }

    @Test
    public void testVectorSizeArg()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(30)");
    }

    @Test
    public void testVectorNumberArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30, 40);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(30, 40)");
    }

    @Test
    public void testVectorArrayArg()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }

    @Test
    public void testXML()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\")");
    }
    
    @Test
    public void testXMLLiteral()
    {
        IVariableNode node = getVariable("var a:XML = <top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML( \"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\") ");
    }
    
    @Test
    public void testXMLDelete()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");delete a.child;");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("delete a.child");
    }
    
    @Test
    public void testXMLListDelete()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");delete a.child[0];");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("a.child._as3_removeChildAt(0)");
    }
    
    @Test
    public void testXMLNameFunction()
    {
    	IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:String = a.name();");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ b = a._as3_name()");
    }
    
    @Test
    public void testXMLDoubleDot()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a..child;");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.descendants('child')");
    }
    
    @Test
    public void testXMLAttribute()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a.@attr1;");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.attribute('attr1')");
    }
    
    @Test
    public void testXMLFilter()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a..grandchild.(@attr2 == 'fish');");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.descendants('grandchild').filter(function(node){return (node.attribute('attr2') == 'fish')})");
    }
    
    @Ignore
    public void testNamespaceNoArg()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }
    
    @Ignore
    public void testNamespaceOneArg()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace('foo');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }
    
    @Ignore
    public void testNamespaceTwoArg()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace('foo', 'bar');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }
    
    @Ignore
    public void testNamespaceThreeArg()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace('foo', 'bar', 'baz');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }
    
    @Ignore
    public void testQNameNoArg()
    {
        IVariableNode node = getVariable("var a:QName = new QName();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }
    
    @Ignore
    public void testQNameTwoArg()
    {
        IVariableNode node = getVariable("var a:QName = new QName(new Namespace('foo'), 'bar');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }

    @Ignore
    public void testQNameOneArg()
    {
        IVariableNode node = getVariable("var a:QName = new QName(new QName(new Namespace('foo'), 'bar'));");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = new Array(['Hello', 'World'])");
    }
}
