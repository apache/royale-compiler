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
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

public class TestDefaultInitializers extends ASTestBase
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
        super.setUp();
    }
    
    protected void createConfig(boolean defaultInitializers)
    {
    	JSGoogConfiguration config = new JSGoogConfiguration();
    	try {
			config.setJsDefaultInitializers(null, defaultInitializers);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	project.config = config;
    }

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withNumberType()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:Number;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ a = NaN;\n  //var /** @type {number} */ a = NaN;\n}");
    }

    @Test
    public void testVarDeclaration_noDefaultInitializers_withNumberType()
    {
        createConfig(false);
        IVariableNode node = (IVariableNode) getNode("var a:Number;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withBooleanType()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:Boolean;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {boolean} */ a = false;\n  //var /** @type {boolean} */ a = false;\n}");
    }

    @Test
    public void testVarDeclaration_noDefaultInitializers_withBooleanType()
    {
        createConfig(false);
        IVariableNode node = (IVariableNode) getNode("var a:Boolean;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withIntType()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:int;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ a = 0;\n  //var /** @type {number} */ a = 0;\n}");
    }

    @Test
    public void testVarDeclaration_noDefaultInitializers_withIntType()
    {
        createConfig(false);
        IFunctionNode node = (IFunctionNode) getNode("var a:int;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        //an exception that always has an initializer
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ a = 0;\n  //var /** @type {number} */ a = 0;\n}");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withUintType()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:uint;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ a = 0;\n  //var /** @type {number} */ a = 0;\n}");
    }

    @Test
    public void testVarDeclaration_noDefaultInitializers_withUintType()
    {
        createConfig(false);
        IFunctionNode node = (IFunctionNode) getNode("var a:uint;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        //an exception that always has an initializer
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ a = 0;\n  //var /** @type {number} */ a = 0;\n}");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withStringType()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:String;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {string} */ a = null;\n  //var /** @type {string} */ a = null;\n}");
    }

    @Test
    public void testVarDeclaration_noDefaultInitializers_withStringType()
    {
        createConfig(false);
        IVariableNode node = (IVariableNode) getNode("var a:String;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withObjectType()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:Object;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {Object} */ a = null;\n  //var /** @type {Object} */ a = null;\n}");
    }

    @Test
    public void testVarDeclaration_noDefaultInitializers_withObjectType()
    {
        createConfig(false);
        IVariableNode node = (IVariableNode) getNode("var a:Object;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withArrayType()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:Array;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {Array} */ a = null;\n  //var /** @type {Array} */ a = null;\n}");
    }

    @Test
    public void testVarDeclaration_noDefaultInitializers_withArrayType()
    {
        createConfig(false);
        IVariableNode node = (IVariableNode) getNode("var a:Array;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withChainedAndAllInitialized()
    {
        createConfig(true);
        IVariableNode node = (IVariableNode) getNode("var a:Number = 0, b:Number = 0, c:Number = 0;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 0, /** @type {number} */ b = 0, /** @type {number} */ c = 0");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withChainedAndNoneInitialized()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:Number, b:Number, c:Number;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ a = NaN;\n  var /** @type {number} */ b = NaN;\n  var /** @type {number} */ c = NaN;\n  //var /** @type {number} */ a = NaN;\n  //var /** @type {number} */ b = NaN;\n  //var /** @type {number} */ c = NaN;\n}");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withChainedAndFirstInitialized()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:Number = 1, b:Number, c:Number;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ b = NaN;\n  var /** @type {number} */ c = NaN;\n  var /** @type {number} */ a = 1;\n  //var /** @type {number} */ b = NaN;\n  //var /** @type {number} */ c = NaN;\n}");
    }

    @Test
    public void testVarDeclaration_defaultInitializers_withChainedAndLastInitialized()
    {
        createConfig(true);
        IFunctionNode node = (IFunctionNode) getNode("var a:Number, b:Number, c:Number = 1;",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n  var /** @type {number} */ a = NaN;\n  var /** @type {number} */ b = NaN;\n  //var /** @type {number} */ a = NaN;\n  //var /** @type {number} */ b = NaN;\n  var /** @type {number} */ c = 1;\n}");
    }

    @Test
    public void testFieldDeclaration_defaultInitializers_withNumberType()
    {
        createConfig(true);
        IVariableNode node = getField("private var foo:Number;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = NaN");
    }

    @Test
    public void testFieldDeclaration_noDefaultInitializers_withNumberType()
    {
        createConfig(false);
        IVariableNode node = getField("private var foo:Number;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo");
    }

    @Test
    public void testFieldDeclaration_defaultInitializers_withBooleanType()
    {
        createConfig(true);
        IVariableNode node = getField("private var foo:Boolean;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {boolean}\n */\nRoyaleTest_A.prototype.foo = false");
    }

    @Test
    public void testFieldDeclaration_noDefaultInitializers_withBooleanType()
    {
        createConfig(false);
        IVariableNode node = getField("private var foo:Boolean;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {boolean}\n */\nRoyaleTest_A.prototype.foo");
    }

    @Test
    public void testFieldDeclaration_defaultInitializers_withIntType()
    {
        createConfig(true);
        IVariableNode node = getField("private var foo:int;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0");
    }

    @Test
    public void testFieldDeclaration_noDefaultInitializers_withIntType()
    {
        createConfig(false);
        IVariableNode node = getField("private var foo:int;");
        asBlockWalker.visitVariable(node);
        //an exception that always has an initializer
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0");
    }

    @Test
    public void testFieldDeclaration_defaultInitializers_withUintType()
    {
        createConfig(true);
        IVariableNode node = getField("private var foo:uint;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0");
    }

    @Test
    public void testFieldDeclaration_noDefaultInitializers_withUintType()
    {
        createConfig(false);
        IVariableNode node = getField("private var foo:uint;");
        asBlockWalker.visitVariable(node);
        //an exception that always has an initializer
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0");
    }

    @Test
    public void testFieldDeclaration_defaultInitializers_withStringType()
    {
        createConfig(true);
        IVariableNode node = getField("private var foo:String;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {string}\n */\nRoyaleTest_A.prototype.foo = null");
    }

    @Test
    public void testFieldDeclaration_noDefaultInitializers_withStringType()
    {
        createConfig(false);
        IVariableNode node = getField("private var foo:String;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {string}\n */\nRoyaleTest_A.prototype.foo");
    }

    @Test
    public void testFieldDeclaration_defaultInitializers_withObjectType()
    {
        createConfig(true);
        IVariableNode node = getField("private var foo:Object;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {Object}\n */\nRoyaleTest_A.prototype.foo = null");
    }

    @Test
    public void testFieldDeclaration_noDefaultInitializers_withObjectType()
    {
        createConfig(false);
        IVariableNode node = getField("private var foo:Object;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {Object}\n */\nRoyaleTest_A.prototype.foo");
    }

    @Test
    public void testFieldDeclaration_defaultInitializers_withArrayType()
    {
        createConfig(true);
        IVariableNode node = getField("private var foo:Array;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {Array}\n */\nRoyaleTest_A.prototype.foo = null");
    }

    @Test
    public void testFieldDeclaration_noDefaultInitializers_withArrayType()
    {
        createConfig(false);
        IVariableNode node = getField("private var foo:Array;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {Array}\n */\nRoyaleTest_A.prototype.foo");
    }
}