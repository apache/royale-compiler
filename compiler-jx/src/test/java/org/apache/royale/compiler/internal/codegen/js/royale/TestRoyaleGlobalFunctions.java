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

package org.apache.royale.compiler.internal.codegen.js.royale;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.js.goog.TestGoogGlobalFunctions;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestRoyaleGlobalFunctions extends TestGoogGlobalFunctions
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
        project.config = new JSGoogConfiguration();
    	project.setProxyBaseClass("flash.utils.Proxy");
        super.setUp();
    }
	
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
    
    @Test
    public void testArrayNoArgs()
    {
        IVariableNode node = getVariable("var a:Array = Array();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = Array()");
    }

    @Test
    public void testArrayStringArgs()
    {
        IVariableNode node = getVariable("var a:Array = Array('Hello', 'World');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = Array('Hello', 'World')");
    }

    @Test
    public void testArraySizeArg()
    {
        IVariableNode node = getVariable("var a:Array = Array(30);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = Array(30)");
    }

    @Test
    public void testArrayNumberArgs()
    {
        IVariableNode node = getVariable("var a:Array = Array(30, 40);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = Array(30, 40)");
    }

    @Test
    public void testArrayArrayArg()
    {
        IVariableNode node = getVariable("var a:Array = Array(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = Array(['Hello', 'World'])");
    }
    
    @Override
    @Test
    public void testObject()
    {
        IVariableNode node = getVariable("var a:Object = Object(\"1\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = org.apache.royale.utils.Language.resolveUncertain(Object(\"1\"))");
    }

    @Test
    public void testParseInt()
    {
        IVariableNode node = getVariable("var a:Number = parseInt('1.8');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = parseInt('1.8', undefined)");
    }

    @Test
    public void testParseIntTwoArgs()
    {
        IVariableNode node = getVariable("var a:Number = parseInt('1.8', 16);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = parseInt('1.8', 16)");
    }

    @Override
    @Test
    public void testInt()
    {
        IVariableNode node = getVariable("var a:int = int(1.8);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = org.apache.royale.utils.Language._int(1.8)");
    }

    @Test
    public void testIntWithString()
    {
        IVariableNode node = getVariable("var a:int = int(\"123\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = org.apache.royale.utils.Language._int(\"123\")");
    }

    @Override
    @Test
    public void testTrace()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "trace('Hello World');", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("org.apache.royale.utils.Language.trace('Hello World')");
    }

    @Override
    @Test
    public void testUint()
    {
        IVariableNode node = getVariable("var a:uint = uint(-100);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = org.apache.royale.utils.Language.uint(-100)");
    }
    
    @Override
    @Test
    public void testIsXMLName()
    {
        IVariableNode node = getVariable("var a:Boolean = isXMLName(\"?\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = !!(isXMLName(\"?\"))");
    }

    @Override
    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<string>} */ a = org.apache.royale.utils.Language.synthVector('String')['coerce'](['Hello', 'World'])");
    }
    
    @Test
    public void testCustomVector()
    {
        project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {CustomVector} */ a = new CustomVector(['Hello', 'World'], 'String')");
    }
    
    @Test
    public void testCustomVectorAsArray()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = ['Hello', 'World'].slice()");
    }

    @Test
    public void testVectorNoArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>();");
        asBlockWalker.visitVariable(node);
        assertErrors("Incorrect number of arguments.  Expected 1");
    }

    @Test
    public void testVectorStringArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>('Hello', 'World');");
        asBlockWalker.visitVariable(node);
        assertErrors("Incorrect number of arguments.  Expected no more than 1");
    }

    @Test
    public void testVectorSizeArg()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(30);");
        asBlockWalker.visitVariable(node);
        // MXMLC doesn't report an error either.  Maybe we should. 
        assertOut("var /** @type {Array.<string>} */ a = org.apache.royale.utils.Language.synthVector('String')['coerce'](30)");
    }
    
    @Test
    public void testCustomVectorSizeArg()
    {
        project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(30);");
        asBlockWalker.visitVariable(node);
        // MXMLC doesn't report an error either.  Maybe we should.
        assertOut("var /** @type {CustomVector} */ a = new CustomVector(30, 'String')");
    }
    
    @Test
    public void testCustomVectorAsArraySizeArg()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(30);");
        asBlockWalker.visitVariable(node);
        // MXMLC doesn't report an error either.  Maybe we should.
        assertOut("var /** @type {Array} */ a = 30.slice()");
    }
    

    @Test
    public void testVectorNumberArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(30, 40);");
        asBlockWalker.visitVariable(node);
        assertErrors("Incorrect number of arguments.  Expected no more than 1");
    }

    @Test
    public void testVectorArrayArg()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<string>} */ a = org.apache.royale.utils.Language.synthVector('String')['coerce'](['Hello', 'World'])");
    }

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
        
        assertOut("var /** @type {XML} */ a = XML.conversion('@')");
    }

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
        
        assertOut("var /** @type {XMLList} */ a = XMLList.conversion('<!-- comment -->')");
    }

    @Test
    public void testGlobalFunctionInClass()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function b():String { var s:String; s = encodeURIComponent('foo'); return s;}}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("s = encodeURIComponent('foo')");
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

}
