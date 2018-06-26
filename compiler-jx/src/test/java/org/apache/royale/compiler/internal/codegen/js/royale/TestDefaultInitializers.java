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
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

public class TestDefaultInitializers extends ASTestBase
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
    	JSGoogConfiguration config = new JSGoogConfiguration();
    	try {
			config.setJsDefaultInitializers(null, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	project.config = config;
        super.setUp();
	}

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

    @Test
    public void testVarDeclaration_withNumberType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Number;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = NaN");
    }

    @Test
    public void testVarDeclaration_withBooleanType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Boolean;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {boolean} */ a = false");
    }

    @Test
    public void testVarDeclaration_withIntType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 0");
    }

    @Test
    public void testVarDeclaration_withUintType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:uint;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {number} */ a = 0");
    }

    @Test
    public void testVarDeclaration_withStringType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:String;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {string} */ a = null");
    }

    @Test
    public void testVarDeclaration_withObjectType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Object;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = null");
    }

    @Test
    public void testVarDeclaration_withArrayType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:Array;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Array} */ a = null");
    }

    @Test
    public void testFieldDeclaration_withNumberType()
    {
        IVariableNode node = getField("private var foo:Number;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = NaN");
    }

    @Test
    public void testFieldDeclaration_withBooleanType()
    {
        IVariableNode node = getField("private var foo:Boolean;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {boolean}\n */\nRoyaleTest_A.prototype.foo = false");
    }

    @Test
    public void testFieldDeclaration_withIntType()
    {
        IVariableNode node = getField("private var foo:int;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0");
    }

    @Test
    public void testFieldDeclaration_withUintType()
    {
        IVariableNode node = getField("private var foo:uint;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0");
    }

    @Test
    public void testFieldDeclaration_withStringType()
    {
        IVariableNode node = getField("private var foo:String;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {string}\n */\nRoyaleTest_A.prototype.foo = null");
    }

    @Test
    public void testFieldDeclaration_withObjectType()
    {
        IVariableNode node = getField("private var foo:Object;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {Object}\n */\nRoyaleTest_A.prototype.foo = null");
    }

    @Test
    public void testFieldDeclaration_withArrayType()
    {
        IVariableNode node = getField("private var foo:Array;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {Array}\n */\nRoyaleTest_A.prototype.foo = null");
    }
}