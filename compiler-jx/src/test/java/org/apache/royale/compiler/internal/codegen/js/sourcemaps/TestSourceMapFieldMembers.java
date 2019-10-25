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
package org.apache.royale.compiler.internal.codegen.js.sourcemaps;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.test.SourceMapTestBase;
import org.apache.royale.compiler.tree.as.IVariableNode;

import org.junit.Test;

public class TestSourceMapFieldMembers extends SourceMapTestBase
{
    @Test
    public void testField()
    {
        IVariableNode node = getField("var foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @type {*}\n */\nRoyaleTest_A.prototype.foo
        assertMapping(node, 0, 4, 3, 0, 3, 26);  // foo
    }

    @Test
    public void testField_withStringSetToNull()
    {
        IVariableNode node = getField("var foo:String = null;");
        asBlockWalker.visitVariable(node);
        //**\n * @type {string}\n */\nRoyaleTest_A.prototype.foo = null
        assertMapping(node, 0, 4, 3, 0, 3, 26);  // foo
        assertMapping(node, 0, 14, 3, 26, 3, 29);  // =
        assertMapping(node, 0, 17, 3, 29, 3, 33);  // null
    }

    @Test
    public void testField_withType()
    {
        IVariableNode node = getField("var foo:int;");
        asBlockWalker.visitVariable(node);
        ///**\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0
        assertMapping(node, 0, 4, 3, 0, 3, 26);  // foo
    }

    @Test
    public void testField_withValue()
    {
        IVariableNode node = getField("var foo = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @type {*}\n */\nRoyaleTest_A.prototype.foo = 420
        assertMapping(node, 0, 4, 3, 0, 3, 26);  // foo
        assertMapping(node, 0, 7, 3, 26, 3, 29);  // =
        assertMapping(node, 0, 10, 3, 29, 3, 32);  // 420
    }

    @Test
    public void testField_withTypeValue()
    {
        IVariableNode node = getField("var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 420
        assertMapping(node, 0, 4, 3, 0, 3, 26);  // foo
        assertMapping(node, 0, 11, 3, 26, 3, 29);  // =
        assertMapping(node, 0, 14, 3, 29, 3, 32);  // 420
    }

    @Test
    public void testStaticField()
    {
        IVariableNode node = getField("static var foo;");
        asBlockWalker.visitVariable(node);
        ////**\n * @type {*}\n */\nRoyaleTest_A.foo
        assertMapping(node, 0, 11, 3, 0, 3, 16);  // foo
    }

    @Test
    public void testStaticField_withType()
    {
        IVariableNode node = getField("static var foo:int;");
        asBlockWalker.visitVariable(node);
        ///**\n * @type {number}\n */\nRoyaleTest_A.foo = 0
        assertMapping(node, 0, 11, 3, 0, 3, 16);  // foo
    }

    @Test
    public void testStaticField_withValue()
    {
        IVariableNode node = getField("static var foo = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @type {*}\n */\nRoyaleTest_A.foo = 420
        assertMapping(node, 0, 11, 3, 0, 3, 16);   // foo
        assertMapping(node, 0, 14, 3, 16, 3, 19);  // =
        assertMapping(node, 0, 17, 3, 19, 3, 22);  // 420
    }

    @Test
    public void testStaticField_withTypeValue()
    {
        IVariableNode node = getField("static var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @type {number}\n */\nRoyaleTest_A.foo = 420
        assertMapping(node, 0, 11, 3, 0, 3, 16);  // foo
        assertMapping(node, 0, 18, 3, 16, 3, 19);  // =
        assertMapping(node, 0, 21, 3, 19, 3, 22);  // 420
    }

    @Test
    public void testConstant()
    {
        IVariableNode node = getField("const foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.foo
        assertMapping(node, 0, 6, 4, 0, 4, 26);  // foo
    }

    @Test
    public void testConstant_withType()
    {
        IVariableNode node = getField("const foo:int;");
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0
        assertMapping(node, 0, 6, 4, 0, 4, 26);  // foo
    }

    @Test
    public void testConstant_withValue()
    {
        IVariableNode node = getField("const foo = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.foo = 420
        assertMapping(node, 0, 6, 4, 0, 4, 26);  // foo
        assertMapping(node, 0, 9, 4, 26, 4, 29);  // =
        assertMapping(node, 0, 12, 4, 29, 4, 32);  // 420
    }

    @Test
    public void testConstant_withTypeValue()
    {
        IVariableNode node = getField("const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 420
        assertMapping(node, 0, 6, 4, 0, 4, 26);  // foo
        assertMapping(node, 0, 13, 4, 26, 4, 29);  // =
        assertMapping(node, 0, 16, 4, 29, 4, 32);  // 420
    }

    @Test
    public void testStaticConstant()
    {
        IVariableNode node = getField("static const foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {*}\n */\nRoyaleTest_A.foo
        assertMapping(node, 0, 13, 4, 0, 4, 16);  // foo
    }

    @Test
    public void testStaticConstant_withType()
    {
        IVariableNode node = getField("static const foo:int;");
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {number}\n */\nRoyaleTest_A.foo = 0
        assertMapping(node, 0, 13, 4, 0, 4, 16);  // foo
    }

    @Test
    public void testStaticConstant_withValue()
    {
        IVariableNode node = getField("static const foo = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {*}\n */\nRoyaleTest_A.foo = 420
        assertMapping(node, 0, 13, 4, 0, 4, 16);  // foo
        assertMapping(node, 0, 16, 4, 16, 4, 19);  // =
        assertMapping(node, 0, 19, 4, 19, 4, 22);  // 420
    }

    @Test
    public void testStaticConstant_withTypeValue()
    {
        IVariableNode node = getField("static const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @export\n * @const\n * @type {number}\n */\nRoyaleTest_A.foo = 420
        assertMapping(node, 0, 13, 4, 0, 4, 16);  // foo
        assertMapping(node, 0, 20, 4, 16, 4, 19);  // =
        assertMapping(node, 0, 23, 4, 19, 4, 22);  // 420
    }

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
