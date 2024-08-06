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
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;

import org.junit.Test;

public class TestSourceMapFieldMembers extends SourceMapTestBase
{
    @Override
    public void setUp()
    {
        super.setUp();

        project.setAllowPrivateNameConflicts(true);
    }

    @Test
    public void testField()
    {
        IVariableNode node = getField("var foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @type {*}\n */\nRoyaleTest_A.prototype.foo
        assertMapping(node, 0, 4, 4, 0, 4, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 4, 4, 23, 4, 26);  // foo
    }

    @Test
    public void testField_withStringSetToNull()
    {
        IVariableNode node = getField("var foo:String = null;");
        asBlockWalker.visitVariable(node);
        //**\n * @package\n * @type {string}\n */\nRoyaleTest_A.prototype.foo = null
        assertMapping(node, 0, 4, 4, 0, 4, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 4, 4, 23, 4, 26);  // foo
        assertMapping(node, 0, 14, 4, 26, 4, 29);  // =
        assertMapping(node, 0, 17, 4, 29, 4, 33);  // null
    }

    @Test
    public void testField_withType()
    {
        IVariableNode node = getField("var foo:int;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0
        assertMapping(node, 0, 4, 4, 0, 4, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 4, 4, 23, 4, 26);  // foo
    }

    @Test
    public void testField_withValue()
    {
        IVariableNode node = getField("var foo = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @type {*}\n */\nRoyaleTest_A.prototype.foo = 420
        assertMapping(node, 0, 4, 4, 0, 4, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 4, 4, 23, 4, 26);  // foo
        assertMapping(node, 0, 7, 4, 26, 4, 29);  // =
        assertMapping(node, 0, 10, 4, 29, 4, 32);  // 420
    }

    @Test
    public void testField_withTypeValue()
    {
        IVariableNode node = getField("var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 420
        assertMapping(node, 0, 4, 4, 0, 4, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 4, 4, 23, 4, 26);  // foo
        assertMapping(node, 0, 11, 4, 26, 4, 29);  // =
        assertMapping(node, 0, 14, 4, 29, 4, 32);  // 420
    }

    @Test
    public void testField_withPrivate()
    {
        IVariableNode node = getField("private var foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @private\n * @type {*}\n */\nRoyaleTest_A.prototype.RoyaleTest_A_foo
        assertMapping(node, 0, 12, 4, 0, 4, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 12, 4, 23, 4, 39, "foo");  // RoyaleTest_A_foo
    }

    @Test
    public void testField_withCustomNamespace()
    {
        IVariableNode node = (IVariableNode) getNode(
                "import custom.custom_namespace;use namespace custom_namespace;public class RoyaleTest_A {custom_namespace var foo;}",
                IVariableNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitVariable(node);
        ///**\n * @type {*}\n */\nRoyaleTest_A.prototype.http_$$ns_apache_org$2017$custom$namespace__foo
        assertMapping(node, 0, 21, 3, 0, 3, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 21, 3, 23, 3, 70, "foo");  // http_$$ns_apache_org$2017$custom$namespace__foo
    }

    @Test
    public void testStaticField()
    {
        IVariableNode node = getField("static var foo;");
        asBlockWalker.visitVariable(node);
        ////**\n * @package\n * @type {*}\n */\nRoyaleTest_A.foo
        assertMapping(node, 0, 11, 4, 0, 4, 13);    // RoyaleTest_A.
        assertMapping(node, 0, 11, 4, 13, 4, 16);  // foo
    }

    @Test
    public void testStaticField_withType()
    {
        IVariableNode node = getField("static var foo:int;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @type {number}\n */\nRoyaleTest_A.foo = 0
        assertMapping(node, 0, 11, 4, 0, 4, 13);    // RoyaleTest_A.
        assertMapping(node, 0, 11, 4, 13, 4, 16);  // foo
    }

    @Test
    public void testStaticField_withValue()
    {
        IVariableNode node = getField("static var foo = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @type {*}\n */\nRoyaleTest_A.foo = 420
        assertMapping(node, 0, 11, 4, 0, 4, 13);    // RoyaleTest_A.
        assertMapping(node, 0, 11, 4, 13, 4, 16);  // foo
        assertMapping(node, 0, 14, 4, 16, 4, 19);  // =
        assertMapping(node, 0, 17, 4, 19, 4, 22);  // 420
    }

    @Test
    public void testStaticField_withTypeValue()
    {
        IVariableNode node = getField("static var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @type {number}\n */\nRoyaleTest_A.foo = 420
        assertMapping(node, 0, 11, 4, 0, 4, 13);    // RoyaleTest_A.
        assertMapping(node, 0, 11, 4, 13, 4, 16);  // foo
        assertMapping(node, 0, 18, 4, 16, 4, 19);  // =
        assertMapping(node, 0, 21, 4, 19, 4, 22);  // 420
    }

    @Test
    public void testStaticField_withPrivate()
    {
        IVariableNode node = getField("private static var foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @private\n * @type {*}\n */\nRoyaleTest_A.foo
        assertMapping(node, 0, 19, 4, 0, 4, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 19, 4, 13, 4, 16);  // foo
    }

    @Test
    public void testStaticField_withCustomNamespace()
    {
        IVariableNode node = (IVariableNode) getNode(
                "import custom.custom_namespace;use namespace custom_namespace;public class RoyaleTest_A {custom_namespace static var foo;}",
                IVariableNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitVariable(node);
        ///**\n * @type {*}\n */\nRoyaleTest_A.http_$$ns_apache_org$2017$custom$namespace__foo
        assertMapping(node, 0, 28, 3, 0, 3, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 28, 3, 13, 3, 60, "foo");  // http_$$ns_apache_org$2017$custom$namespace__foo
    }

    @Test
    public void testConstant()
    {
        IVariableNode node = getField("const foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.foo
        assertMapping(node, 0, 6, 5, 0, 5, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 6, 5, 23, 5, 26);  // foo
    }

    @Test
    public void testConstant_withType()
    {
        IVariableNode node = getField("const foo:int;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @const\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 0
        assertMapping(node, 0, 6, 5, 0, 5, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 6, 5, 23, 5, 26);  // foo
    }

    @Test
    public void testConstant_withValue()
    {
        IVariableNode node = getField("const foo = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.foo = 420
        assertMapping(node, 0, 6, 5, 0, 5, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 6, 5, 23, 5, 26);  // foo
        assertMapping(node, 0, 9, 5, 26, 5, 29);  // =
        assertMapping(node, 0, 12, 5, 29, 5, 32);  // 420
    }

    @Test
    public void testConstant_withTypeValue()
    {
        IVariableNode node = getField("const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @const\n * @type {number}\n */\nRoyaleTest_A.prototype.foo = 420
        assertMapping(node, 0, 6, 5, 0, 5, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 6, 5, 23, 5, 26);  // foo
        assertMapping(node, 0, 13, 5, 26, 5, 29);  // =
        assertMapping(node, 0, 16, 5, 29, 5, 32);  // 420
    }

    @Test
    public void testConstant_withPrivate()
    {
        IVariableNode node = getField("private const foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @private\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.RoyaleTest_A_foo
        assertMapping(node, 0, 14, 5, 0, 5, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 14, 5, 23, 5, 39, "foo");  // RoyaleTest_A_foo
    }

    @Test
    public void testConstant_withCustomNamespace()
    {
        IVariableNode node = (IVariableNode) getNode(
                "import custom.custom_namespace;use namespace custom_namespace;public class RoyaleTest_A {custom_namespace const foo;}",
                IVariableNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.http_$$ns_apache_org$2017$custom$namespace__foo
        assertMapping(node, 0, 23, 4, 0, 4, 23);  // RoyaleTest_A.prototype.
        assertMapping(node, 0, 23, 4, 23, 4, 70, "foo");  // http_$$ns_apache_org$2017$custom$namespace__foo
    }

    @Test
    public void testStaticConstant()
    {
        IVariableNode node = getField("static const foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @const\n * @type {*}\n */\nRoyaleTest_A.foo
        assertMapping(node, 0, 13, 5, 0, 5, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 13, 5, 13, 5, 16);  // foo
    }

    @Test
    public void testStaticConstant_withType()
    {
        IVariableNode node = getField("static const foo:int;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @const\n * @type {number}\n */\nRoyaleTest_A.foo = 0
        assertMapping(node, 0, 13, 5, 0, 5, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 13, 5, 13, 5, 16);  // foo
    }

    @Test
    public void testStaticConstant_withValue()
    {
        IVariableNode node = getField("static const foo = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @const\n * @type {*}\n */\nRoyaleTest_A.foo = 420
        assertMapping(node, 0, 13, 5, 0, 5, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 13, 5, 13, 5, 16);  // foo
        assertMapping(node, 0, 16, 5, 16, 5, 19);  // =
        assertMapping(node, 0, 19, 5, 19, 5, 22);  // 420
    }

    @Test
    public void testStaticConstant_withTypeValue()
    {
        IVariableNode node = getField("static const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        ///**\n * @package\n * @const\n * @type {number}\n */\nRoyaleTest_A.foo = 420
        assertMapping(node, 0, 13, 5, 0, 5, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 13, 5, 13, 5, 16);  // foo
        assertMapping(node, 0, 20, 5, 16, 5, 19);  // =
        assertMapping(node, 0, 23, 5, 19, 5, 22);  // 420
    }

    @Test
    public void testStaticConstant_withPrivate()
    {
        IVariableNode node = getField("private static const foo;");
        asBlockWalker.visitVariable(node);
        ///**\n * @private\n * @const\n * @type {*}\n */\nRoyaleTest_A.foo
        assertMapping(node, 0, 21, 5, 0, 5, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 21, 5, 13, 5, 16);  // foo
    }

    public void testStaticConstant_withCustomNamespace()
    {
        IVariableNode node = (IVariableNode) getNode(
                "import custom.custom_namespace;use namespace custom_namespace;public class RoyaleTest_A {custom_namespace static const foo;}",
                IVariableNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitVariable(node);
        ///**\n * @const\n * @type {*}\n */\nRoyaleTest_A.http_$$ns_apache_org$2017$custom$namespace__foo
        assertMapping(node, 0, 30, 4, 0, 5, 13);  // RoyaleTest_A.
        assertMapping(node, 0, 30, 4, 13, 5, 60);  // http_$$ns_apache_org$2017$custom$namespace__foo
    }

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
