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
import org.apache.royale.compiler.internal.codegen.js.goog.TestGoogAccessorMembers;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestRoyaleAccessorMembers extends TestGoogAccessorMembers
{
    @Override
    @Test
    public void testGetAccessor()
    {
        IClassNode node = (IClassNode) getNode("function get foo():int{}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n" +
				"RoyaleTest_A.prototype.get__foo = function() {\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @type {number} */\nfoo: {\nget: RoyaleTest_A.prototype.get__foo}}\n);");
    }

    @Override
    @Test
    public void testGetAccessor_withBody()
    {
    	IClassNode node = (IClassNode) getNode("function get foo():int{return -1;}",
    			IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n" +
				"RoyaleTest_A.prototype.get__foo = function() {\n  return -1;\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @type {number} */\nfoo: {\nget: RoyaleTest_A.prototype.get__foo}}\n);");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespace()
    {
    	IClassNode node = (IClassNode) getNode("public function get foo():int{return -1;}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n" +
				"RoyaleTest_A.prototype.get__foo = function() {\n  return -1;\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @export\n  * @type {number} */\nfoo: {\nget: RoyaleTest_A.prototype.get__foo}}\n);");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespaceOverride()
    {
    	IClassNode node = (IClassNode) getNode("public class B extends A { public override function get foo():int{return super.foo;} }; public class A {public function get foo():int {return 0;}} ",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n};\ngoog.inherits(B, A);\n\n\n" +
				"B.prototype.get__foo = function() {\n  return B.superClass_.get__foo.apply(this);\n};\n\n\n" +
        		"Object.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/**\n  * @export\n  * @type {number} */\nfoo: {\nget: B.prototype.get__foo}}\n);");
    }

    @Test
    public void testGetAccessor_withGeneratedSetOverride()
    {
    	IClassNode node = (IClassNode) getNode("public class B extends A { public override function get foo():int{return super.foo;} }; public class A { public function set foo(value:int):void{} public function get foo():int {return 0;}}",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n};\ngoog.inherits(B, A);\n\n\n" +
				"B.prototype.get__foo = function() {\n  return B.superClass_.get__foo.apply(this);\n};\n\n\n" +
        		"Object.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/**\n  * @export\n  * @type {number} */\nfoo: {\nget: B.prototype.get__foo,\nset: A.prototype.set__foo}}\n);");
    }
    
    @Override
    @Test
    public void testGetAccessor_withStatic()
    {
    	IClassNode node = (IClassNode) getNode("public static function get foo():int{return -1;}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n" +
          "/**\n  * @nocollapse\n  * @export\n  * @type {number}\n  */\nRoyaleTest_A.foo;\n\n\n" +
          "RoyaleTest_A.get__foo = function() {\n  return -1;\n};\n\n\n" +
          "Object.defineProperties(RoyaleTest_A, /** @lends {RoyaleTest_A} */ {\n/**\n  * @export\n  * @type {number} */\nfoo: {\nget: RoyaleTest_A.get__foo}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor()
    {
    	IClassNode node = (IClassNode) getNode("function set foo(value:int):void{}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n" +
				"RoyaleTest_A.prototype.set__foo = function(value) {\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @type {number} */\nfoo: {\nset: RoyaleTest_A.prototype.set__foo}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor_withBody()
    {
    	IClassNode node = (IClassNode) getNode("function set foo(value:int):void{fetch('haai');}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n" +
				"RoyaleTest_A.prototype.set__foo = function(value) {\n  fetch('haai');\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @type {number} */\nfoo: {\nset: RoyaleTest_A.prototype.set__foo}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespace()
    {
    	IClassNode node = (IClassNode) getNode("public function set foo(value:int):void{}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n" +
				"RoyaleTest_A.prototype.set__foo = function(value) {\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @export\n  * @type {number} */\nfoo: {\nset: RoyaleTest_A.prototype.set__foo}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespaceOverride()
    {
    	IClassNode node = (IClassNode) getNode("public class B extends A { public override function set foo(value:int):void {super.foo = value;} }; public class A { public function set foo(value:int):void{}}",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n};\ngoog.inherits(B, A);\n\n\n" +
				"B.prototype.set__foo = function(value) {\n  B.superClass_.set__foo.apply(this, [ value] );\n};\n\n\n" +
        		"Object.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/**\n  * @export\n  * @type {number} */\nfoo: {\nset: B.prototype.set__foo}}\n);");
    }

    @Override
    @Test
    public void testSetAccessor_withStatic()
    {
    	IClassNode node = (IClassNode) getNode("public static function set foo(value:int):void{}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n" +
          "/**\n  * @export\n  * @type {number}\n  */\nRoyaleTest_A.foo;\n\n\n" +
          "RoyaleTest_A.set__foo = function(value) {\n};\n\n\n" +
          "Object.defineProperties(RoyaleTest_A, /** @lends {RoyaleTest_A} */ {\n/**\n  * @export\n  * @type {number} */\nfoo: {\nset: RoyaleTest_A.set__foo}}\n);");
    }

    @Test
    public void testSetAccessor_withGeneratedGetOverride()
    {
    	IClassNode node = (IClassNode) getNode("public class B extends A { public override function set foo(value:int):void {super.foo = value;} }; public class A { public function set foo(value:int):void{} public function get foo():int { return 0;}}",
        		IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @extends {A}\n */\nB = function() {\n  B.base(this, 'constructor');\n};\ngoog.inherits(B, A);\n\n\n" +
				"B.prototype.set__foo = function(value) {\n  B.superClass_.set__foo.apply(this, [ value] );\n};\n\n\n" +
        		"Object.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/**\n  * @export\n  * @type {number} */\nfoo: {\nget: A.prototype.get__foo,\nset: B.prototype.set__foo}}\n);");
    }
    
    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
