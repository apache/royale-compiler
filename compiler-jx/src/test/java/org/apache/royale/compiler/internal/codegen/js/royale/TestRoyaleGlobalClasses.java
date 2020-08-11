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

import java.io.File;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.js.goog.TestGoogGlobalClasses;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorAssignmentNode;
import org.apache.royale.compiler.internal.tree.as.VariableNode;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;
import org.junit.Ignore;

/**
 * @author Erik de Bruin
 */
public class TestRoyaleGlobalClasses extends TestGoogGlobalClasses
{
    @Override
    public void setUp()
    {
        backend = createBackend();
    	project = new RoyaleJSProject(workspace, backend);
    	project.config = new JSGoogConfiguration();
    	project.setProxyBaseClass("custom.TestProxy");
        super.setUp();
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

    @Override
    @Test
    public void testArguments()
    {
        IFunctionNode node = getMethod("function a():void {  trace(arguments);}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.a = function() {\n  org.apache.royale.utils.Language.trace(arguments);\n}");
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

    @Test
    public void testArrayConstCaseInsensitive()
    {
        IVariableNode node = getVariable("var a:Number = Array.CASEINSENSITIVE");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 1");
    }

    @Test
    public void testArrayConstNumeric()
    {
        IVariableNode node = getVariable("var a:Number = Array.NUMERIC");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 16");
    }

    @Test
    public void testArrayRemoveAt()
    {
    	File pg = testAdapter.getPlayerglobal();
    	if (arrayHasInsertAtRemoveAt(pg))
    	{
	    	// requires FP19 or newer
	        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array(); a.removeAt(2)");
	        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
	        asBlockWalker.visitFunctionCall(parentNode);
	        assertOut("a.splice(2, 1)");
    	}
    }

    @Test
    public void testArrayInsertAt()
    {
    	File pg = testAdapter.getPlayerglobal();
    	if (arrayHasInsertAtRemoveAt(pg))
    	{
	    	// requires FP19 or newer
	        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array(); a.insertAt(2, 'foo')");
	        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
	        asBlockWalker.visitFunctionCall(parentNode);
	        assertOut("a.splice(2, 0, 'foo')");
    	}
    }

    private boolean arrayHasInsertAtRemoveAt(File pg) {
    	if (pg == null) return true;
    	String path = pg.getAbsolutePath();
    	String[] parts = path.split("/");
    	if (path.contains("\\"))
    		parts = path.split("\\\\");
    	for (String part : parts)
    	{
    		if (part.contains("."))
    		{
    			// see if it is the playerglobal version string
    			String[] versionParts = part.split("\\.");
    			if (versionParts.length != 2) return false;
    			try {
	    			int major = Integer.parseInt(versionParts[0]);
	    			if (major >= 19) return true;
    			} catch (NumberFormatException e)
    			{    				
    			}
    		}
    	}
		return false;
	}

	@Test
    public void testArraySortNoArgs()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array();a.sort()");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.sort()");
    }

    @Test
    public void testArraySortOptArgOnly()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array();a.sort(Array.NUMERIC)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("org.apache.royale.utils.Language.sort(a, 16)");
    }

    @Test
    public void testArraySortCompareFunctionArgOnly()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array();var f:Function = function():void {};a.sort(f)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.sort(f)");
    }

    @Test
    public void testArraySortCompareFunctionWithOptArg()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array();var f:Function = function():void {};a.sort(f, Array.UNIQUESORT)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("org.apache.royale.utils.Language.sort(a, f, 4)");
    }

    @Test
    public void testArraySortCompareFunctionWithOptNumberArg()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array();var f:Function = function():void {};a.sort(Array.UNIQUESORT)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("org.apache.royale.utils.Language.sort(a, 4)");
    }

    @Test
    public void testArraySortCompareFunctionWithOptFunctionArg()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array();var f:Function = function():void {};a.sort(f)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.sort(f)");
    }

    @Test
    public void testArraySortOn()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array(); a.sortOn('foo')");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("org.apache.royale.utils.Language.sortOn(a, 'foo')");
    }

    @Test
    public void testArraySortOnTwoArgs()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Array = new Array(); a.sortOn('foo', 10)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("org.apache.royale.utils.Language.sortOn(a, 'foo', 10)");
    }

    @Test
    public void testInt()
    {
        IVariableNode node = getVariable("var a:int = new int(\"123\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = org.apache.royale.utils.Language._int(\"123\")");
    }

    @Override
    @Test
    public void testUint()
    {
        IVariableNode node = getVariable("var a:uint = new uint(-100);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = org.apache.royale.utils.Language.uint(-100)");
    }

    @Test
    public void testIntConstMaxValue()
    {
        IVariableNode node = getVariable("var a:Number = int.MAX_VALUE");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 2147483648");
    }

    @Test
    public void testIntConstMinValue()
    {
        IVariableNode node = getVariable("var a:Number = int.MIN_VALUE");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = -2147483648");
    }

    @Test
    public void testUintConstMaxValue()
    {
        IVariableNode node = getVariable("var a:Number = uint.MAX_VALUE");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 4294967295");
    }

    @Test
    public void testUintConstMinValue()
    {
        IVariableNode node = getVariable("var a:Number = uint.MIN_VALUE");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 0");
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
    public void testClass()
    {
        IVariableNode node = getVariable("var a:Class = String; var b:* = new a('test')");
        node = (IVariableNode)(node.getParent().getChild(1));
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {*} */ b = org.apache.royale.utils.Language.resolveUncertain(new a('test'))");
    }

    @Test
    public void testDateSetSeconds()
    {
    	IBinaryOperatorNode node = getBinaryNode("var a:Date = new Date(); a.seconds = 10");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setSeconds(10)");
    }

    @Test
    public void testDateSetDate()
    {
    	IBinaryOperatorNode node = getBinaryNode("var a:Date = new Date(); a.date = 10");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setDate(10)");
    }

    @Test
    public void testDateGetTimeInMilliseconds()
    {
        IVariableNode node = getVariable("var a:Date = new Date(); var b:Number = a.time");
        node = (IVariableNode)(node.getParent().getChild(1));
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ b = a.getTime()");
    }

    @Test
    public void testDateSetTimeInMilliseconds()
    {
    	IBinaryOperatorNode node = getBinaryNode("var a:Date = new Date(); a.time = 10");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setTime(10)");
    }

    @Test
    public void testDateIncreaseTimeInMilliseconds()
    {
    	IBinaryOperatorNode node = getBinaryNode("var a:Date = new Date(); a.time += 10");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setTime(a.getTime() + 10)");
    }

    @Test
    public void testDateGetMinutes()
    {
        IVariableNode node = getVariable("var a:Date = new Date(); var b:Number = a.minutes");
        node = (IVariableNode)(node.getParent().getChild(1));
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ b = a.getMinutes()");
    }

    @Test
    public void testDateSetMinutes()
    {
    	IBinaryOperatorNode node = getBinaryNode("var a:Date = new Date(); a.minutes = 10");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setMinutes(10)");
    }

    @Test
    public void testDateIncreaseMinutes()
    {
    	IBinaryOperatorNode node = getBinaryNode("var a:Date = new Date(); a.minutes += 10");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setMinutes(a.getMinutes() + 10)");
    }

    @Test
    public void testDateGetMinutesMethod()
    {
        IVariableNode node = getVariable("var a:Date = new Date(); var b:Number = a.getMinutes()");
        node = (IVariableNode)(node.getParent().getChild(1));
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ b = a.getMinutes()");
    }
    
    @Test
    public void testDateSetMinutesMethod()
    {
    	IBinaryOperatorNode node = getBinaryNode("var a:Date = new Date(); a.setMinutes(10, 0, 0)");
    	IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.setMinutes(10, 0, 0)");
    }

    @Override
    @Test
    public void testVector()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {Array.<string>} */ a = new (org.apache.royale.utils.Language.synthVector('String'))(['Hello', 'World'])");
    }

    @Test
    public void testVectorLiteral_1()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new <String>[];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<string>} */ a = org.apache.royale.utils.Language.synthVector('String')['coerce']([])");
    }

    @Test
    public void testVectorLiteral_2()
    {
        IVariableNode node = getVariable("var a:Vector.<int> = new <int>[0, 1, 2, 3];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<number>} */ a = org.apache.royale.utils.Language.synthVector('int')['coerce']([0, 1, 2, 3])");
    }

    @Test
    public void testVectorLiteral_3()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new <String>[\"one\", \"two\", \"three\";");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<string>} */ a = org.apache.royale.utils.Language.synthVector('String')['coerce']([\"one\", \"two\", \"three\"])");
    }
    
    @Test
    public void testVectorNoArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<string>} */ a = new (org.apache.royale.utils.Language.synthVector('String'))()");
    }

    @Test
    public void testVectorStringArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>('Hello', 'World');");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {Array.<string>} */ a = new (org.apache.royale.utils.Language.synthVector('String'))('Hello', 'World')");
    }

    @Test
    public void testVectorStringArgs3()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>('Hello', 'World', 'Three');");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {Array.<string>} */ a = new (org.apache.royale.utils.Language.synthVector('String'))('Hello', 'World', 'Three')");
    }

    @Test
    public void testVectorSizeArg()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array.<string>} */ a = new (org.apache.royale.utils.Language.synthVector('String'))(30)");
    }

    @Test
    public void testVectorNumberArgs()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30, 40);");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {Array.<string>} */ a = new (org.apache.royale.utils.Language.synthVector('String'))(30, 40)");
    }

    @Test
    public void testVectorArrayArg()
    {
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {Array.<string>} */ a = new (org.apache.royale.utils.Language.synthVector('String'))(['Hello', 'World'])");
    }
    
    @Test
    public void testVectorSetLength()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<String> = new Vector.<String>(); a.length = 20)");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a[org.apache.royale.utils.Language.SYNTH_TAG_FIELD].length = 20");
    }
    
    @Test
    public void testCustomVectorSetLength()
    {
        project.config.setJsVectorEmulationClass(null, "Anything");
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<String> = new Vector.<String>(); a.length = 20)");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.length = 20");
    }
    
    @Test
    public void testVectorRemoveAt()
    {
    	File pg = testAdapter.getPlayerglobal();
    	if (arrayHasInsertAtRemoveAt(pg))
    	{
	    	// requires FP19 or newer
	        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<String> = new Vector.<String>(); a.removeAt(2)");
	        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
	        asBlockWalker.visitFunctionCall(parentNode);
	        assertOut("a['removeAt'](2)");
    	}
    }
    
    @Test
    public void testCustomVectorRemoveAt()
    {
        project.config.setJsVectorEmulationClass(null, "CustomVector");
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<String> = new Vector.<String>(); a.removeAt(2)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.removeAt(2)");
    }
    
    @Test
    public void testCustomVectorAsArrayRemoveAt()
    {
        
        project.config.setJsVectorEmulationClass(null, "Array");
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<String> = new Vector.<String>(); a.removeAt(2)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.splice(2, 1)[0]");
    }

    @Test
    public void testVectorInsertAt()
    {
    	File pg = testAdapter.getPlayerglobal();
    	if (arrayHasInsertAtRemoveAt(pg))
    	{
	    	// requires FP19 or newer
	        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<String> = new Vector.<String>(); a.insertAt(2, 'foo')");
	        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
	        asBlockWalker.visitFunctionCall(parentNode);
	        assertOut("a['insertAt'](2, 'foo')");
    	}
    }
    
    @Test
    public void testCustomVectorInsertAt()
    {
        project.config.setJsVectorEmulationClass(null, "CustomVector");
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<String> = new Vector.<String>(); a.insertAt(2, 'foo')");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.insertAt(2, 'foo')");
    }
    
    @Test
    public void testCustomVectorAsArrayInsertAt()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<String> = new Vector.<String>(); a.insertAt(2, 'foo')");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.splice(2, 0, 'foo')");
    }

    @Test
    public void testCustomVector()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {CustomVector} */ a = new CustomVector(['Hello', 'World'], 'String')");
    }

    @Test
    public void testCustomVectorLiteral_1()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new <String>[];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {CustomVector} */ a = new CustomVector([], 'String')");
    }
    
    @Test
    public void testCustomVectorLiteral_1a()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = new <String>[];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = []");
    }

    @Test
    public void testCustomVectorLiteral_2()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<int> = new <int>[0, 1, 2, 3];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {CustomVector} */ a = new CustomVector([0, 1, 2, 3], 'int')");
    }
    
    @Test
    public void testCustomVectorLiteral_2a()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<int> = new <int>[0, 1, 2, 3];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = [0, 1, 2, 3]");
    }

    @Test
    public void testCustomVectorLiteral_3()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new <String>[\"one\", \"two\", \"three\"];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {CustomVector} */ a = new CustomVector([\"one\", \"two\", \"three\"], 'String')");
    }
    
    @Test
    public void testCustomVectorLiteral_3a()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = new <String>[\"one\", \"two\", \"three\"];");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = [\"one\", \"two\", \"three\"]");
    }
    
    @Test
    public void testCustomVectorNoArgs()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {CustomVector} */ a = new CustomVector([], 'String')");
    }
    
    @Test
    public void testCustomVectorNoArgs2()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = []");
    }

    @Test
    public void testCustomVectorStringArgs()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>('Hello', 'World');");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {CustomVector} */ a = new CustomVector('Hello', 'String')");
    }

    @Test
    public void testCustomVectorStringArgs3()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>('Hello', 'World', 'Three');");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {CustomVector} */ a = new CustomVector('Hello', 'String')");
    }

    @Test
    public void testCustomVectorSizeArg()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {CustomVector} */ a = new CustomVector(30, 'String')");
    }
    
    @Test
    public void testCustomVectorSizeArg2()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = org.apache.royale.utils.Language.arrayAsVector(30, 'String')");
    }
    
    @Test
    public void testCustomVectorSizeAndFixedArgs()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30, true);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {CustomVector} */ a = new CustomVector(30, 'String', true)");
    }
    
    @Test
    public void testCustomVectorSizeAndFixedArgs2()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30, true);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = org.apache.royale.utils.Language.arrayAsVector(30, 'String')");
    }

    @Test
    public void testCustomVectorNumberArgs()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30, 40);");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {CustomVector} */ a = new CustomVector(30, 'String')");
    }
    
    @Test
    public void testCustomVectorNumberArgs2()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(30, 40);");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {Array} */ a = org.apache.royale.utils.Language.arrayAsVector(30, 'String')");
    }

    @Test
    public void testCustomVectorArrayArg()
    {
    	project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {CustomVector} */ a = new CustomVector(['Hello', 'World'], 'String')");
    }
    
    @Test
    public void testCustomVectorArrayArg2()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Vector.<String> = new Vector.<String>(['Hello', 'World']);");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {Array} */ a = org.apache.royale.utils.Language.arrayAsVector(['Hello', 'World'], 'String')");
    }
    
    @Test
    public void testDefaultVectorClassRepresentation()
    {
        IVariableNode node = getVariable("var a:Class = Vector.<String>;");
        asBlockWalker.visitVariable(node);
        //MXMLC does not report an error.  Should we?
        assertOut("var /** @type {Object} */ a = org.apache.royale.utils.Language.synthVector('String')");
    }
    
    @Test
    public void testCustomVectorClassRepresentation()
    {
        project.config.setJsVectorEmulationClass(null, "CustomVector");
        IVariableNode node = getVariable("var a:Class = Vector.<String>;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = CustomVector");
    }
    
    @Test
    public void testCustomVectorClassRepresentation2()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IVariableNode node = getVariable("var a:Class = Vector.<String>;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = Array");
    }
    
    @Test
    public void testDefaultVectorSortNumericArg()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<Number> = new Vector.<Number>();a.sort(Array.NUMERIC)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("org.apache.royale.utils.Language.sort(a, 16)");
    }
    
    @Test
    public void testCustomVectorRepresentationSortNumericArg()
    {
        project.config.setJsVectorEmulationClass(null, "CustomVector");
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<Number> = new Vector.<Number>();a.sort(Array.NUMERIC)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("a.sort(16)");
    }
    
    @Test
    public void testCustomVectorAsArrayRepresentationSortNumericArg()
    {
        project.config.setJsVectorEmulationClass(null, "Array");
        IBinaryOperatorNode node = getBinaryNode("var a:Vector.<Number> = new Vector.<Number>();a.sort(Array.NUMERIC)");
        IFunctionCallNode parentNode = (IFunctionCallNode)(node.getParent());
        asBlockWalker.visitFunctionCall(parentNode);
        assertOut("org.apache.royale.utils.Language.sort(a, 16)");
    }
    
    @Override
    @Test
    public void testBoolean()
    {
        IVariableNode node = getVariable("var a:Boolean = new Boolean(1);");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = Boolean(1)");
    }
    
    @Override
    @Test
    public void testNumber()
    {
        IVariableNode node = getVariable("var a:Number = new Number(\"1\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = Number(\"1\")");
    }
    
    @Override
    @Test
    public void testString()
    {
        IVariableNode node = getVariable("var a:String = new String(\"100\");");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = String(\"100\")");
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
        assertOut("var /** @type {XML} */ a = new XML( \"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\")");
    }

    @Test
    public void testXMLLiteralWithQuote()
    {
        IVariableNode node = getVariable("var a:XML = <root title=\"That's Entertainment\"/>");
        asBlockWalker.visitVariable(node);
        String s = "var /** @type {XML} */ a = new XML( \"<root title=\\\"That's Entertainment\\\"/>\")";
        assertOut(s);
    }
    
    @Test
    public void testXMLLiteralMultiline()
    {
        IVariableNode node = getVariable("var a:XML = <top attr1='cat'>\n<child attr2='dog'>\n<grandchild attr3='fish'>text</grandchild>\n</child>\n</top>");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML( \"<top attr1='cat'>\\n<child attr2='dog'>\\n<grandchild attr3='fish'>text</grandchild>\\n</child>\\n</top>\")");
    }
    
    @Test
    public void testXMLLiteralMultilineNoSemicolon()
    {
        IVariableNode node = getVariable("var a:XML = <top />\nvar foo;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML( '<top />')");
    }
    
    @Test
    public void testXMLLiteralMultilineNoSemicolonWithAttribute()
    {
        IVariableNode node = getVariable("var a:XML = <top attr1='cat'/>\nvar foo;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML( \"<top attr1='cat'/>\")");
    }
    
    @Test
    public void testXMLLiteralWithTemplate()
    {
        VariableNode node = (VariableNode)getNode("private function get tagname():String { return 'name'; };\n" +
        							 "private function get attributename():String { return 'id'; };\n" +
        							 "private function get attributevalue():Number { return 5; };\n" +
        							 "private function get content():String { return 'Fred'; };\n" +
        							 "private function test() { var a:XML = <{tagname} {attributename}={attributevalue}>{content}</{tagname}>;}",
        							 VariableNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML( '<' + this.tagname + ' ' + this.attributename + '=' + '\"' + this.attributevalue + '\"' + '>' + this.content + '</' + this.tagname + '>')");
    }
    
    @Test
    public void testXMLLiteralWithTemplateAndParams()
    {
        VariableNode node = (VariableNode)getNode("private function test(attributevalue:String) { var a:XML = <name attributeName={attributevalue} />;}",
        							 VariableNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML( '<name attributeName=' + '\"' + attributevalue + '\"' + ' />')");
    }
    
    @Test
    public void testXMLLiteralWithTemplateExpression()
    {
        VariableNode node = (VariableNode)getNode("private function get tagname():String { return 'name'; };\n" +
        							 "private function get attributename():String { return 'id'; };\n" +
        							 "private function get attributevalue():Number { return 5; };\n" +
        							 "private function get content():String { return 'Fred'; };\n" +
        							 "private function test() { var a:XML = <{tagname} {attributename}={attributevalue + \" \" + attributevalue}>{content}</{tagname}>;}",
        							 VariableNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML( '<' + this.tagname + ' ' + this.attributename + '=' + '\"' + this.attributevalue + \" \" + this.attributevalue + '\"' + '>' + this.content + '</' + this.tagname + '>')");
    }
    
    @Test
    public void testXMLLiteralWithTemplateAndStaticVar()
    {
        VariableNode node = (VariableNode)getNode("private static function get txtStr():String { return 'foo'; }; private function test() { var a:XML = <text><content>{txtStr}</content></text>;}",
        							 VariableNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = new XML( '<text><content>' + RoyaleTest_A.txtStr + '</content></text>')");
    }
    
    @Test
    public void testXMLLiteralAsParam()
    {
        IFunctionCallNode node = (IFunctionCallNode)getNode("var a:XML; a.appendChild(<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>)",
        		IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("a.appendChild(new XML( \"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\"))");
    }
    
    @Test
    public void testXMLLiteralInReassign()
    {
    	IBinaryOperatorNode node = getBinaryNode("var a:XML = <foo />; a = <top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>)");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = new XML( \"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\")");
    }
    
    @Test
    public void testXMLSingleDot()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a.child;");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.child('child')");
    }

    @Test
    public void testXMLSingleDotBracket()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a[\"child\"];");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.child(\"child\")");
    }

    @Test
    public void testXMLSingleDotBracketQName()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var q:QName; var b:XMLList = a[q];");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(2);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.child(q)");
    }
    
    @Test
    public void testXMLSingleDotChain()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a.child.grandchild;");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.child('child').child('grandchild')");
    }
    
    @Test
    public void testXMLDelete()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");delete a.child;");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("a.removeChild('child')");
    }
    
    @Test
    public void testXMLDeleteAttribute()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");delete a.@attr1;");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("a.removeChild(a.attribute('attr1'))");
    }
    
    @Test
    public void testXMLDeleteChain()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");delete a.child.grandchild;");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("a.child('child').removeChild('grandchild')");
    }

    @Test
    public void testXMLDeleteObjChain()
    {
        IUnaryOperatorNode node = getUnaryNode("public var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");private function foo() { delete this.a.child.grandchild;}",
        		WRAP_LEVEL_CLASS);
        asBlockWalker.visitUnaryOperator(node);
        assertOut("this.a.child('child').removeChild('grandchild')");
    }
    
    @Test
    public void testXMLDeleteCastChain()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:Object = { xml: a};delete XML(b.xml).child.grandchild;");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("XML.conversion(b.xml).child('child').removeChild('grandchild')");
    }
    
    @Test
    public void testXMLDeleteCastAsChain()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:Object = { xml: a};delete (b.xml as XML).child.grandchild;");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("org.apache.royale.utils.Language.as(b.xml, XML).child('child').removeChild('grandchild')");
    }

    @Test
    public void testXMLListDelete()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");delete a.child[0];");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("a.child('child').removeChildAt(0)");
    }
    
    @Test
    public void testXMLListChain()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");delete a.child.grandchild[0];");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("a.child('child').child('grandchild').removeChildAt(0)");
    }
    
    @Test
    public void testXMLListObjChain()
    {
        IUnaryOperatorNode node = getUnaryNode("public var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");private function foo() { delete this.a.child.grandchild[0];}",
        		WRAP_LEVEL_CLASS);
        asBlockWalker.visitUnaryOperator(node);
        assertOut("this.a.child('child').child('grandchild').removeChildAt(0)");
    }
    
    @Test
    public void testXMLListCastChain()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:Object = { xml: a};delete XML(b.xml).child.grandchild[0];");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("XML.conversion(b.xml).child('child').child('grandchild').removeChildAt(0)");
    }
    
    @Test
    public void testXMLListAsCastChain()
    {
        IUnaryOperatorNode node = getUnaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:Object = { xml: a};delete (b.xml as XML).child.grandchild[0];");
        asBlockWalker.visitUnaryOperator(node);
        assertOut("org.apache.royale.utils.Language.as(b.xml, XML).child('child').child('grandchild').removeChildAt(0)");
    }
    
    @Test
    public void testXMLListAccess()
    {
    		IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var child:XML = a.child[0]");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
    		asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ child = a.child('child')[0]");
    }
    
    @Test
    public void testXMLListAccessComplex()
    {
    		IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var child:XML = a.child[a.child.length()-1]");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
    		asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ child = a.child('child')[a.child('child').length() - 1]");
    }
    
    @Test
    public void testXMLListAssignment()
    {
    		IBinaryOperatorNode node = (IBinaryOperatorNode)getNode("var a:XMLList = new XMLList();a[a.length()] = <foo/>;a[a.length()] = <baz/>;", IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setChild(a.length(), new XML( '<foo/>'))");
    }

    @Test
    public void testXMLNameFunction()
    {
    	IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:String = a.name();");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ b = org.apache.royale.utils.Language.string(a.name())");
    }
    
    @Test
    public void testXMLListLengthFunction()
    {
    	IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:int = a.child.length();");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ b = (a.child('child').length()) >> 0");
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
    public void testXMLDoubleDotTwice()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a..child..grandchild;");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.descendants('child').descendants('grandchild')");
    }
    
    @Ignore
    public void testXMLDoubleDotLiteral()
    {
        IVariableNode node = getVariable("var xml:XML; var a:XML = xml..('http://ns.adobe.com/mxml/2009')::catalog_item[0];");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XML} */ a = xml.descendants('http://ns.adobe.com/mxml/2009::catalog_item')[0];");
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
    public void testXMLAttributeBracket()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a.@[\"attr1\"];");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.attribute(\"attr1\")");
    }
    
    @Test
    public void testXMLAttributeBracket2()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a[\"@attr1\"];");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.attribute(\"attr1\")");
    }
    
    @Test
    public void testXMLAttributeToString()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:String = a.@attr1;");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ b = org.apache.royale.utils.Language.string(a.attribute('attr1'))");
    }
    
    @Test
    public void testXMLAttributeToStringAsExpression()
    {
    	IBinaryOperatorNode node = (IBinaryOperatorNode)getNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:String; b = a.@attr1;", IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = org.apache.royale.utils.Language.string(a.attribute('attr1'))");
    }
    
    @Test
    public void testXMLAttributeToNumber()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:Number = a.@attr1;");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ b = Number(a.attribute('attr1'))");
    }
    
    @Test
    public void testXMLAttributeToNumberAsExpression()
    {
    	IBinaryOperatorNode node = (IBinaryOperatorNode)getNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:Number; b = a.@attr1;", IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = Number(a.attribute('attr1'))");
    }
    
    @Test
    public void testStringSetToNull()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:String = null;");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ b = null");
    }
    
    @Test
    public void testXMLFilter()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a..grandchild.(@attr2 == 'fish');");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.descendants('grandchild').filter(function(/** @type {XML} */ node){return (node.attribute('attr2') == 'fish')})");
    }
    
    @Test
    public void testXMLFilterForChild()
    {
        IVariableNode node = getVariable("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");var b:XMLList = a..grandchild.(year == '2016');");
        IASNode parentNode = node.getParent();
        node = (IVariableNode) parentNode.getChild(1);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {XMLList} */ b = a.descendants('grandchild').filter(function(/** @type {XML} */ node){return (node.child('year') == '2016')})");
    }
    
    @Test
    public void testXMLFilterWithAttribute()
    {
    	IBinaryOperatorNode node = (IBinaryOperatorNode)getNode("private var attribute:Function; private function test() {var a:XMLList; a = a.(attribute('name').length())};", IBinaryOperatorNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = a.filter(function(/** @type {XML} */ node){return (node.attribute('name').length())})");
    }
    
    @Test
    public void testXMLSetAttribute()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.@bar = 'foo'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setAttribute('bar', 'foo')");
    }
    
    @Test
    public void testXMLSetAttributeBracket()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.@[\"bar\"] = 'foo'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setAttribute(\"bar\", 'foo')");
    }
    
    @Test
    public void testXMLSetAttributeBracketProp()
    {
        IBinaryOperatorNode node = getBinaryNode("var z:String = 'prop';var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.@[z] = 'foo'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setAttribute(z, 'foo')");
    }
    
    @Test
    public void testXMLSetAttributeBracketPropObject()
    {
        IBinaryOperatorNode node = getBinaryNode("var z:Object = 'prop';var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.@[z] = 'foo'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setAttribute(z, 'foo')");
    }
    
    @Test
    public void testXMLSetAttributeBracketPropStar()
    {
        IBinaryOperatorNode node = getBinaryNode("var z:* = 'prop';var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.@[z] = 'foo'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setAttribute(z, 'foo')");
    }
    
    @Test
    public void testXMLSetChildAttributeBracketProp()
    {
        IBinaryOperatorNode node = getBinaryNode("var z:String = 'prop';var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.child.@['attr3'] = 'foo'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.child('child').setAttribute('attr3', 'foo')");
    }
    
    @Test
    public void testXMLListSetAttribute()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XMLList;a[1].@bar = 'foo'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a[1].setAttribute('bar', 'foo')");
    }
    
    @Test
    public void testXMLListSetAttributeIndex()
    {
        IBinaryOperatorNode node = getBinaryNode("var n:int = 1;var a:XMLList;a[n].@bar = 'foo'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a[n].setAttribute('bar', 'foo')");
    }
    
    @Test
    public void testXMLListSetAttributeComplex()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XMLList;a.(@id==3).@height = '100px'");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.filter(function(/** @type {XML} */ node){return (node.attribute('id') == 3)}).setAttribute('height', '100px')");
    }
    
    @Test
    public void testXMLSetChild()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.foo = a.child");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setChild('foo', a.child('child'))");
    }
    
    @Test
    public void testXMLSetChildBracket()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a[\"foo\"] = a.child");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setChild('foo', a.child('child'))");
    }
    
    @Test
    public void testXMLSetChildToObjectMember()
    {
    	BinaryOperatorAssignmentNode node = (BinaryOperatorAssignmentNode) getNode(
                "public class B {public var someProp:XML; public function B() { var a:XML; var b:B; b.someProp = a.child; }}",
                BinaryOperatorAssignmentNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b.someProp = a.child('child')");
    }
    
    @Test
    public void testXMLListConcat()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.foo += a.child");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setChild('foo', a.child('foo').plus(a.child('child')))");
    }
    
    @Test
    public void testXMLListConcat2()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XMLList; var b:XMLList; a += b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = a.plus(b)");
    }
    
    @Test
    public void testXMLListAddAndAssign()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");a.foo = a.child + a..grandchild");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a.setChild('foo', a.child('child').plus(a.descendants('grandchild')))");
    }
    
    @Test
    public void testXMLForLoop()
    {
        IForLoopNode node = getForLoopNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");for (var p:* in a) delete a[p];");
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {*} */ p in a.elementNames())\n  a.removeChild(p);");
    }
    
    @Test
    public void testXMLForEachLoop()
    {
    	IForLoopNode node = getForLoopNode("var a:XML = new XML(\"<top attr1='cat'><child attr2='dog'><grandchild attr3='fish'>text</grandchild></child></top>\");for each (var p:XMLList in a) var i:int = p.length();");
        asBlockWalker.visitForLoop(node);
        assertOut("var foreachiter0_target = a;\nfor (var foreachiter0 in foreachiter0_target.elementNames()) \n{\nvar p = foreachiter0_target[foreachiter0];\n\n  var /** @type {number} */ i = (p.length()) >> 0;}\n");
    }
    
    @Test
    public void testXMLForEachLoopAs()
    {
    	IForLoopNode node = getForLoopNode("var a:*;for each (var p:XML in (a as XMLList)) var i:int = p.length();");
        asBlockWalker.visitForLoop(node);
        assertOut("var foreachiter0_target = org.apache.royale.utils.Language.as(a, XMLList);\nfor (var foreachiter0 in foreachiter0_target.elementNames()) \n{\nvar p = foreachiter0_target[foreachiter0];\n\n  var /** @type {number} */ i = (p.length()) >> 0;}\n");
    }
    
    @Test
    public void testXMLForEachLoopCast()
    {
    	IForLoopNode node = getForLoopNode("var a:*;for each (var p:XML in XMLList(a)) var i:int = p.length();");
        asBlockWalker.visitForLoop(node);
        assertOut("var foreachiter0_target = XMLList.conversion(a);\nfor (var foreachiter0 in foreachiter0_target.elementNames()) \n{\nvar p = foreachiter0_target[foreachiter0];\n\n  var /** @type {number} */ i = (p.length()) >> 0;}\n");
    }
    
    @Test
    public void testNamespaceNoArg()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Namespace} */ a = new Namespace()");
    }
    
    @Test
    public void testNamespaceOneArg()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace('foo');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Namespace} */ a = new Namespace('foo')");
    }
    
    @Test
    public void testNamespaceTwoArg()
    {
        IVariableNode node = getVariable("var a:Namespace = new Namespace('foo', 'bar');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Namespace} */ a = new Namespace('foo', 'bar')");
    }
    
    @Test
    public void testQNameNoArg()
    {
        IVariableNode node = getVariable("var a:QName = new QName();");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {QName} */ a = new QName()");
    }
    
    @Test
    public void testQNameTwoArg()
    {
        IVariableNode node = getVariable("var a:QName = new QName(new Namespace('foo'), 'bar');");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {QName} */ a = new QName(new Namespace('foo'), 'bar')");
    }

    @Test
    public void testQNameOneArg()
    {
        IVariableNode node = getVariable("var a:QName = new QName(new QName(new Namespace('foo'), 'bar'));");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {QName} */ a = new QName(new QName(new Namespace('foo'), 'bar'))");
    }
    
    
    @Test
    public void testProxy()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();a.foo = 'bar'; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  a.setProperty('foo', 'bar');\n}");
    }
    
    @Test
    public void testProxySetBrackets()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();a['foo'] = 'bar'; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  a.setProperty('foo', 'bar');\n}");
    }
    
    @Test
    public void testProxySetBracketsStringVar()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();var foo:String;a[foo] = 'bar'; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {string} */ foo = null;\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  //var /** @type {string} */ foo = null;\n  a.setProperty(foo, 'bar');\n}");
    }
    
    @Test
    public void testProxySetBracketsUintVar()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();var foo:uint = 0;a[foo] = 'bar'; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  var /** @type {number} */ foo = 0;\n  a.setProperty(foo, 'bar');\n}");
    }
    
    @Test
    public void testProxyGet()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();var bar:* = a.foo; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  var /** @type {*} */ bar = a.getProperty('foo');\n}");
    }
    
    @Test
    public void testProxyGetArrayIndexAndCompare()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy(); if (a[0] != null) return; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  if (a.getProperty(0) != null)\n    return;\n}");
    }
    
    @Test
    public void testProxyFunctionCall()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();var bar:* = a.foo(10,\"ten\"); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  var /** @type {*} */ bar = a.callProperty('foo', 10, \"ten\");\n}");
    }
    
    @Test
    public void testProxyConcat()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();var baz:String = a.foo + 'bar'; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  var /** @type {string} */ baz = a.getProperty('foo') + 'bar';\n}");
    }
    
    @Test
    public void testProxyAddAndAssign()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();a.foo += 'bar'; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {custom.TestProxy} */ a = new custom.TestProxy();\n  a.setProperty('foo', a.getProperty('foo') + 'bar');\n}");
    }
    
    @Test
    public void testProxyForLoop()
    {
    	IForLoopNode node = (IForLoopNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();for (var p:* in a) delete a[p];; }}",
                IForLoopNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitForLoop(node);
        assertOut("for (var /** @type {*} */ p in a.propertyNames())\n  a.deleteProperty(p);");
    }
    
    @Test
    public void testProxyForEachLoop()
    {
    	IForLoopNode node = (IForLoopNode) getNode(
                "import custom.TestProxy; public class B {public function b() { var a:TestProxy = new TestProxy();for each (var p:String in a) var i:int = p.length; }}",
                IForLoopNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitForLoop(node);
        assertOut("var foreachiter0_target = a;\nfor (var foreachiter0 in foreachiter0_target.propertyNames()) \n{\nvar p = foreachiter0_target.getProperty(foreachiter0);\n\n  var /** @type {number} */ i = (p.length) >> 0;}\n");
    }
    
    @Test
    public void testRegExp_LiteralUnicode()
    {
        IVariableNode node = getVariable("var a:RegExp = /[\\u0065\\u0066\\u0067]/g;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /[efg]/g");
    }

    @Test
    public void testRegExp_LiteralComplex1()
    {
        IVariableNode node = getVariable("var a:RegExp = /[\\u0009\\u000a\\u000d]/g;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /[\\u0009\\u000a\\u000d]/g");
    }

    @Test
    public void testRegExp_LiteralComplex2()
    {
        IVariableNode node = getVariable("var a:RegExp = /\\u2028/;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /\\u2028/");
    }

    @Test
    public void testRegExp_LiteralComplex3()
    {
        IVariableNode node = getVariable("var a:RegExp = /\\u000A|\\u000D\\u000A?/g;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /\\u000a|\\u000d\\u000a?/g");
    }

    @Test
    public void testRegExp_LiteralComplex4()
    {
        IVariableNode node = getVariable("var a:RegExp = /[^\\u0009\\u000a\\u000d\\u0020]/g;");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /[^\\u0009\\u000a\\u000d\\u0020]/g");
    }

    @Test
    public void testRegExp_LiteralComplex5()
    {
        IVariableNode node = getVariable("var a:RegExp = /[^\\s+(\\w+)(?:\\s*=\\s*(\".*?\"|'.*?'|[\\w\\.]+))?/sg");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /[^\\s+(\\w+)(?:\\s*=\\s*(\".*?\"|'.*?'|[\\w\\.]+))?/sg");
    }
    
    @Test
    public void testRegExp_LiteralComplex6()
    {
        IVariableNode node = getVariable("var a:RegExp = /\\/$/g");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /\\/$/g");
    }
    
    @Test
    public void testRegExp_LiteralComplex7()
    {
        IVariableNode node = getVariable("var a:RegExp = /.+\\//g");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /.+\\//g");
    }
    
    @Test
    public void testRegExp_LiteralComplex8()
    {
        IVariableNode node = getVariable("var a:RegExp = /.+\\\\/g");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /.+\\\\/g");
    }
    
    @Test
    public void testRegExp_LiteralComplex9()
    {
        IVariableNode node = getVariable("var a:RegExp = /^\\s*counter\\s*\\(\\s*ordered\\s*,\\s*/g");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /^\\s*counter\\s*\\(\\s*ordered\\s*,\\s*/g");
    }

    @Test
    public void testRegExp_LiteralComplex10()
    {
        IVariableNode node = getVariable("var a:RegExp = /^(?:(?:https?|ftp):\\/\\/)?(?:[-\\w]+\\.)(?:[a-zA-Z\\.]{2,6})(?:[?\\/\\w\\.&=-]*)\\/?$/i");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /^(?:(?:https?|ftp):\\/\\/)?(?:[-\\w]+\\.)(?:[a-zA-Z\\.]{2,6})(?:[?\\/\\w\\.&=-]*)\\/?$/i");
    }

    @Test
    public void testRegExp_LiteralComplex11()
    {
        IVariableNode node = getVariable("var a:RegExp = /\\\\/g");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /\\\\/g");
    }
    
    @Test
    public void testRegExp_LiteralComplex12()
    {
        IVariableNode node = getVariable("var a:RegExp = /\\\\\\\\/g");
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {RegExp} */ a = /\\\\\\\\/g");
    }
    

}
