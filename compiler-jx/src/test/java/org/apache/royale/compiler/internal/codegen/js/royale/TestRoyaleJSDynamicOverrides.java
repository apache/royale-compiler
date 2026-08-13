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
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.junit.Test;

public class TestRoyaleJSDynamicOverrides extends ASTestBase
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
        project.config = new JSGoogConfiguration();
        super.setUp();
    }
    
    @Test
    public void testDynamicOverride_getMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(getMethod=\"get\")] class A { public function A() { var a = this[0]; } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  var /** @type {*} */ a = this.get(0);\n};");
    }
    
    @Test
    public void testDynamicOverride_getMethod_parentheses()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(getMethod=\"get\")] class A { public function A() { var a = (this[0]); } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  var /** @type {*} */ a = (this.get(0));\n};");
    }
    
    @Test
    public void testDynamicOverride_getMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { var a = this[0]; } }; [JSDynamicOverride(getMethod=\"get\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  var /** @type {*} */ a = this.get(0);\n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testDynamicOverride_getMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { var a = this[0]; } }; [JSDynamicOverride(getMethod=\"get\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  var /** @type {*} */ a = this.get(0);\n};");
    }
    
    @Test
    public void testDynamicOverride_setMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(setMethod=\"set\")] class A { public function A() { this[0] = 123.4; } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  this.set(0, 123.4);\n};");
    }
    
    @Test
    public void testDynamicOverride_setMethod_parentheses()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(setMethod=\"set\")] class A { public function A() { (this[0] = 123.4); } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  (this.set(0, 123.4));\n};");
    }
    
    @Test
    public void testDynamicOverride_setMethod_reversedTrue()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(setMethod=\"set\",setMethodReversed=\"true\")] class A { public function A() { this[0] = 123.4; } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  this.set(123.4, 0);\n};");
    }
    
    @Test
    public void testDynamicOverride_setMethod_reversedFalse()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(setMethod=\"set\",setMethodReversed=\"false\")] class A { public function A() { this[0] = 123.4; } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  this.set(0, 123.4);\n};");
    }
    
    @Test
    public void testDynamicOverride_setMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { this[0] = 123.4; } }; [JSDynamicOverride(setMethod=\"set\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  this.set(0, 123.4);\n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testDynamicOverride_setMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { this[0] = 123.4; } }; [JSDynamicOverride(setMethod=\"set\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  this.set(0, 123.4);\n};");
    }
    
    @Test
    public void testDynamicOverride_deleteMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(deleteMethod=\"del\")] class A { public function A() { delete this[0]; } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  this.del(0);\n};");
    }
    
    @Test
    public void testDynamicOverride_deleteMethod_parentheses()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(deleteMethod=\"del\")] class A { public function A() { (delete this[0]); } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  (this.del(0));\n};");
    }
    
    @Test
    public void testDynamicOverride_deleteMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { delete this[0]; } }; [JSDynamicOverride(deleteMethod=\"del\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  this.del(0);\n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testDynamicOverride_deleteMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { delete this[0]; } }; [JSDynamicOverride(deleteMethod=\"del\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  this.del(0);\n};");
    }
    
    @Test
    public void testDynamicOverride_inMethod()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(inMethod=\"exists\")] class A { public function A() { 0 in this; } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  this.exists(0);\n};");
    }
    
    @Test
    public void testDynamicOverride_inMethod_parentheses()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(inMethod=\"exists\")] class A { public function A() { (0 in this); } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  (this.exists(0));\n};");
    }
    
    @Test
    public void testDynamicOverride_inMethod_subclass()
    {
        IClassNode node = (IClassNode) getNode("public class B extends A { public function B() { 0 in this; } }; [JSDynamicOverride(inMethod=\"exists\")] public class A { public function A() {} }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n  this.exists(0);\n};\ngoog.inherits(B, A);");
    }
    
    @Test
    public void testDynamicOverride_inMethod_implementsInterface()
    {
        IClassNode node = (IClassNode) getNode("public class B implements A { public function B() { 0 in this; } }; [JSDynamicOverride(inMethod=\"exists\")] public interface A { }",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {A}\n */\nB = function() {\n  this.exists(0);\n};");
    }
    
    @Test
    public void testDynamicOverride_getMethod_setMethod_postIncrement()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(getMethod=\"get\",setMethod=\"set\")] class A { public function A() { this[0]++; } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  this.set(0, this.get(0) + 1);\n};");
    }
    
    @Test
    public void testDynamicOverride_getMethod_setMethod_postIncrement_parentheses()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(getMethod=\"get\",setMethod=\"set\")] class A { public function A() { (this[0]++); } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  (this.set(0, this.get(0) + 1));\n};");
    }
    
    @Test
    public void testDynamicOverride_getMethod_setMethod_postDecrement()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(getMethod=\"get\",setMethod=\"set\")] class A { public function A() { this[0]--; } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  this.set(0, this.get(0) - 1);\n};");
    }
    
    @Test
    public void testDynamicOverride_getMethod_setMethod_postDecrement_parentheses()
    {
        IClassNode node = (IClassNode) getNode("[JSDynamicOverride(getMethod=\"get\",setMethod=\"set\")] class A { public function A() { (this[0]--); } }",
        		IClassNode.class, WRAP_LEVEL_NONE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nA = function() {\n  (this.set(0, this.get(0) - 1));\n};");
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

}
