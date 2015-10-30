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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogGlobalFunctions;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSGlobalFunctions extends TestGoogGlobalFunctions
{
    @Override
    @Test
    public void testArray()
    {
        IVariableNode node = getVariable("var a:Array = Array(1);");
        asBlockWalker.visitVariable(node);
        // (erikdebruin) The Actionscript and JavaScript implementations of
        //               Array are identical in this regard, Array() can be
        //               called as a function (without new) and if the argument
        //               is a single integer, an Array with that length is 
        //               returned:
        //
        //               https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array
        //
        assertOut("var /** @type {Array} */ a = Array(1)");
    }

    @Override
    @Test
    public void testInt()
    {
        IVariableNode node = getVariable("var a:int = int(1.8);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = org.apache.flex.utils.Language._int(1.8)");
    }

    @Override
    @Test
    public void testTrace()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "trace('Hello World');", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("org.apache.flex.utils.Language.trace('Hello World')");
    }

    @Override
    @Test
    public void testUint()
    {
        IVariableNode node = getVariable("var a:uint = uint(-100);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = org.apache.flex.utils.Language.uint(-100)");
    }

    @Override
    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = Array(['Hello', 'World'])");
    }

    @Ignore
    @Override
    @Test
    public void testXML()
    {
        IVariableNode node = getVariable("var a:XML = XML('@');");
        asBlockWalker.visitVariable(node);
        // TODO (aharui) claims this is not valid and someday needs to result in:
        //     <@/>  or something like that?
        // I cannot find any reference to creating an XML object via a
        // global function
        
        // (erikdebruin) E4X in Javascript is obsolete.
        //               Ref.: https://developer.mozilla.org/en-US/docs/E4X
        
        assertOut("var /** @type {XML} */ a = XML('@')");
    }

    @Ignore
    @Override
    @Test
    public void testXMLList()
    {
        IVariableNode node = getVariable("var a:XMLList = XMLList('<!-- comment -->');");
        asBlockWalker.visitVariable(node);
        // TODO (aharui) claims this is not valid and someday needs to result in:
        //     <@/>  or something like that?
        // I cannot find any reference to creating an XML object via a
        // global function

        // (erikdebruin) E4X in Javascript is obsolete.
        //               Ref.: https://developer.mozilla.org/en-US/docs/E4X
        
        assertOut("var /** @type {XMLList} */ a = XMLList('<!-- comment -->')");
    }

    @Test
    public void testGlobalFunctionInClass()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function b():String { var s:String; s = encodeURIComponent('foo'); return s;}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("s = encodeURIComponent('foo')");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
