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
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

public class TestRoyaleInferTypes extends ASTestBase
{
	@Override
	public void setUp()
	{
		backend = createBackend();
		project = new RoyaleJSProject(workspace, backend);
		project.setInferTypes(true);
		project.config = new JSGoogConfiguration();
		super.setUp();
	}

	// ----- local variables

	@Test
	public void testInferLocalVariableWithStringDefaultValue()
	{
		IVariableNode node = getVariable("var s = \"hello\"");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {string} */ s = \"hello\"");
	}

	@Test
	public void testInferLocalVariableWithBooleanDefaultValue()
	{
		IVariableNode node = getVariable("var b = true");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {boolean} */ b = true");
	}

	@Test
	public void testInferLocalVariableWithNumberDefaultValue()
	{
		IVariableNode node = getVariable("var n = 123.4");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {number} */ n = 123.4");
	}

	@Test
	public void testInferLocalVariableWithArrayDefaultValue()
	{
		IVariableNode node = getVariable("var a = []");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {Array} */ a = []");
	}

	@Test
	public void testInferLocalVariableWithObjectDefaultValue()
	{
		IVariableNode node = getVariable("var o = {}");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {Object} */ o = {}");
	}

	@Test
	public void testInferLocalVariableWithDateDefaultValue()
	{
		IVariableNode node = getVariable("var d = new Date();");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {Date} */ d = new Date()");
	}

	@Test
	public void testInferLocalVariableWithNullDefaultValue()
	{
		IVariableNode node = getVariable("var x = null;");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {*} */ x = null");
	}

	@Test
	public void testInferLocalVariableWithUndefinedDefaultValue()
	{
		IVariableNode node = getVariable("var x = undefined;");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {*} */ x = undefined");
	}

	@Test
	public void testInferLocalVariableWithNoDefaultValue()
	{
		IVariableNode node = getVariable("var x;");
		asBlockWalker.visitVariable(node);
		assertOut("var /** @type {*} */ x");
	}

	// ----- parameters

	@Test
	public void testInferParameterWithStringDefaultValue()
	{
		IFunctionNode node = getMethod("function f(s = \"hello\"):void {}");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @param {string=} s\n */\nRoyaleTest_A.prototype.f = function(s) {\n  s = typeof s !== 'undefined' ? s : \"hello\";\n}");
	}

	@Test
	public void testInferParameterWithNullDefaultValue()
	{
		IFunctionNode node = getMethod("function f(x = null):void {}");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @param {*=} x\n */\nRoyaleTest_A.prototype.f = function(x) {\n  x = typeof x !== 'undefined' ? x : null;\n}");
	}

	@Test
	public void testInferParameterWithUndefinedDefaultValue()
	{
		IFunctionNode node = getMethod("function f(x = undefined):void {}");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @param {*=} x\n */\nRoyaleTest_A.prototype.f = function(x) {\n  x = typeof x !== 'undefined' ? x : undefined;\n}");
	}

	@Test
	public void testInferParameterWithNoDefaultValue()
	{
		IFunctionNode node = getMethod("function f(x):void {}");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @param {*} x\n */\nRoyaleTest_A.prototype.f = function(x) {\n}");
	}

	// ----- returns

	@Test
	public void testInferReturnWithStringDefaultValue()
	{
		IFunctionNode node = getMethod("function f() { return \"hello\" }");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @return {string}\n */\nRoyaleTest_A.prototype.f = function() {\n  return \"hello\";\n}");
	}

	@Test
	public void testInferReturnWithNoReturnStatements()
	{
		IFunctionNode node = getMethod("function f() {}");
		asBlockWalker.visitFunction(node);
		// inferred as void, so no comment is required
		assertOut("RoyaleTest_A.prototype.f = function() {\n}");
	}

	@Test
	public void testInferReturnWithEmptyReturnStatement()
	{
		IFunctionNode node = getMethod("function f() { return; }");
		asBlockWalker.visitFunction(node);
		// inferred as void, so no comment is required
		assertOut("RoyaleTest_A.prototype.f = function() {\n  return;\n}");
	}

	@Test
	public void testInferReturnWithNullDefaultValue()
	{
		IFunctionNode node = getMethod("function f() { return null; }");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @return {*}\n */\nRoyaleTest_A.prototype.f = function() {\n  return null;\n}");
	}

	@Test
	public void testInferReturnWithUndefinedDefaultValue()
	{
		IFunctionNode node = getMethod("function f() { return undefined; }");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @return {*}\n */\nRoyaleTest_A.prototype.f = function() {\n  return undefined;\n}");
	}

	@Test
	public void testInferReturnWithOverrideAndSuperInferredReturn()
	{
		IFileNode node = compileAS(
			"package { public class A {} }\n" + 
			"class C { public function s() { return \"hello\"; } }\n" +
			"class D extends C { override public function s() { return null; } }");
		IClassNode d = null;
		for(int i = 0; i < node.getChildCount(); i++)
		{
			IASNode child = node.getChild(i);
			if (child instanceof IClassNode)
			{
				IClassNode childClass = (IClassNode) child;
				if ("D".equals(childClass.getName()))
				{
					d = childClass;
					break;
				}
			}
		}
		asBlockWalker.visitClass(d);
		// overrides don't need to specify type
		assertOut("/**\n * @constructor\n * @extends {C}\n */\nD = function() {\n  D.base(this, 'constructor');\n};\ngoog.inherits(D, C);\n\n\n/**\n * @override\n */\nD.prototype.s = function() {\n  return null;\n};");
	}

	@Test
	public void testInferReturnWithOverrideAndSuperDeclaredReturn()
	{
		IFileNode node = compileAS(
			"package { public class A {} }\n" + 
			"class C { public function s():String { return null; } }\n" +
			"class D extends C { override public function s() { return null; } }");
		IClassNode d = null;
		for(int i = 0; i < node.getChildCount(); i++)
		{
			IASNode child = node.getChild(i);
			if (child instanceof IClassNode)
			{
				IClassNode childClass = (IClassNode) child;
				if ("D".equals(childClass.getName()))
				{
					d = childClass;
					break;
				}
			}
		}
		asBlockWalker.visitClass(d);
		// overrides don't need to specify type
		assertOut("/**\n * @constructor\n * @extends {C}\n */\nD = function() {\n  D.base(this, 'constructor');\n};\ngoog.inherits(D, C);\n\n\n/**\n * @override\n */\nD.prototype.s = function() {\n  return null;\n};");
	}

	// ----- member variables

	@Test
	public void testInferMemberVariableWithStringDefaultValue()
	{
		IVariableNode node = getField("public var s = \"hello\"");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @type {string}\n */\nRoyaleTest_A.prototype.s = \"hello\"");
	}

	@Test
	public void testInferMemberVariableWithNullDefaultValue()
	{
		IVariableNode node = getField("public var s = null");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @type {*}\n */\nRoyaleTest_A.prototype.s = null");
	}

	@Test
	public void testInferMemberVariableWithUndefinedDefaultValue()
	{
		IVariableNode node = getField("public var s = undefined");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @type {*}\n */\nRoyaleTest_A.prototype.s = undefined");
	}

	@Test
	public void testInferMemberVariableWithNoDefaultValue()
	{
		IVariableNode node = getField("public var s");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @type {*}\n */\nRoyaleTest_A.prototype.s = undefined");
	}

	// ----- static variables

	@Test
	public void testInferStaticVariableWithStringDefaultValue()
	{
		IVariableNode node = getField("public static var s = \"hello\"");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @nocollapse\n * @type {string}\n */\nRoyaleTest_A.s = \"hello\"");
	}

	@Test
	public void testInferStaticVariableWithNullDefaultValue()
	{
		IVariableNode node = getField("public static var s = null");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @nocollapse\n * @type {*}\n */\nRoyaleTest_A.s = null");
	}

	@Test
	public void testInferStaticVariableWithUndefinedDefaultValue()
	{
		IVariableNode node = getField("public static var s = undefined");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @nocollapse\n * @type {*}\n */\nRoyaleTest_A.s = undefined");
	}

	@Test
	public void testInferStaticVariableWithNoDefaultValue()
	{
		IVariableNode node = getField("public static var s");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @nocollapse\n * @type {*}\n */\nRoyaleTest_A.s = undefined");
	}

	// ----- static constants

	@Test
	public void testInferStaticConstantWithStringDefaultValue()
	{
		IVariableNode node = getField("public static const s = \"hello\"");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @nocollapse\n * @const\n * @type {string}\n */\nRoyaleTest_A.s = \"hello\"");
	}

	@Test
	public void testInferStaticConstantWithNullDefaultValue()
	{
		IVariableNode node = getField("public static const s = null");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @nocollapse\n * @const\n * @type {*}\n */\nRoyaleTest_A.s = null");
	}

	@Test
	public void testInferStaticConstantWithUndefinedDefaultValue()
	{
		IVariableNode node = getField("public static const s = undefined");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @nocollapse\n * @const\n * @type {*}\n */\nRoyaleTest_A.s = undefined");
	}

	@Test
	public void testInferStaticConstantWithNoDefaultValue()
	{
		IVariableNode node = getField("public static const s");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @nocollapse\n * @const\n * @type {*}\n */\nRoyaleTest_A.s = undefined");
	}

	// ----- member constants

	@Test
	public void testInferMemberConstantWithStringDefaultValue()
	{
		IVariableNode node = getField("public const s = \"hello\"");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @const\n * @type {string}\n */\nRoyaleTest_A.prototype.s = \"hello\"");
	}

	@Test
	public void testInferMemberConstantWithNullDefaultValue()
	{
		IVariableNode node = getField("public const s = null");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.s = null");
	}

	@Test
	public void testInferMemberConstantWithUndefinedDefaultValue()
	{
		IVariableNode node = getField("public const s = undefined");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.s = undefined");
	}

	@Test
	public void testInferMemberConstantWithNoDefaultValue()
	{
		IVariableNode node = getField("public const s");
		asBlockWalker.visitVariable(node);
		assertOut("/**\n * @const\n * @type {*}\n */\nRoyaleTest_A.prototype.s = undefined");
	}

	// ----- local constants

	@Test
	public void testInferLocalConstantWithStringDefaultValue()
	{
		IVariableNode node = getVariable("const s = \"hello\"");
		asBlockWalker.visitVariable(node);
		assertOut("\n/**\n * @const\n * @type {string}\n */\nvar s = \"hello\"");
	}

	@Test
	public void testInferLocalConstantWithNullDefaultValue()
	{
		IVariableNode node = getVariable("const s = null");
		asBlockWalker.visitVariable(node);
		assertOut("\n/**\n * @const\n * @type {*}\n */\nvar s = null");
	}

	@Test
	public void testInferLocalConstantWithUndefinedDefaultValue()
	{
		IVariableNode node = getVariable("const s = undefined");
		asBlockWalker.visitVariable(node);
		assertOut("\n/**\n * @const\n * @type {*}\n */\nvar s = undefined");
	}

	@Test
	public void testInferLocalConstantWithNoDefaultValue()
	{
		IVariableNode node = getVariable("const s");
		asBlockWalker.visitVariable(node);
		assertOut("\n/**\n * @const\n * @type {*}\n */\nvar s");
	}

	// ----- getters and setters

	@Test
	public void testInferGetterWithStringDefaultValue()
	{
		IGetterNode node = (IGetterNode) getAccessor("public function get s() { return \"hello\"; }");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @return {string}\n */\nRoyaleTest_A.prototype.s = function() {\n  return \"hello\";\n}");
	}

	@Test
	public void testInferGetterWithNullDefaultValue()
	{
		IGetterNode node = (IGetterNode) getAccessor("public function get s() { return null; }");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @return {*}\n */\nRoyaleTest_A.prototype.s = function() {\n  return null;\n}");
	}

	@Test
	public void testInferGetterWithUndefinedDefaultValue()
	{
		IGetterNode node = (IGetterNode) getAccessor("public function get s() { return undefined; }");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @return {*}\n */\nRoyaleTest_A.prototype.s = function() {\n  return undefined;\n}");
	}

	@Test
	public void testInferGetterWithSetterDeclaredType()
	{
		IClassNode node = getClassNode("class C { public function get s() { return null; } public function set s(value:String):void {} }");
		asBlockWalker.visitClass(node);
		assertOut("/**\n * @constructor\n */\nC = function() {\n};\n\n\n/**\n * @nocollapse\n * @export\n * @type {string}\n */\nC.prototype.s;\n\n\nC.prototype.get__s = function() {\n  return null;\n};\n\n\nC.prototype.set__s = function(value) {\n};\n\n\nObject.defineProperties(C.prototype, /** @lends {C.prototype} */ {\n/**\n * @type {string}\n */\ns: {\nget: C.prototype.get__s,\nset: C.prototype.set__s}}\n);");
	}

	@Test
	public void testInferSetterWithGetterDeclaredType()
	{
		IClassNode node = getClassNode("class C { public function get s():String { return null; } public function set s(value):void {} }");
		asBlockWalker.visitClass(node);
		assertOut("/**\n * @constructor\n */\nC = function() {\n};\n\n\n/**\n * @nocollapse\n * @export\n * @type {string}\n */\nC.prototype.s;\n\n\nC.prototype.get__s = function() {\n  return null;\n};\n\n\nC.prototype.set__s = function(value) {\n};\n\n\nObject.defineProperties(C.prototype, /** @lends {C.prototype} */ {\n/**\n * @type {string}\n */\ns: {\nget: C.prototype.get__s,\nset: C.prototype.set__s}}\n);");
	}

	@Test
	public void testInferSetterWithGetterStringDefaultValue()
	{
		IClassNode node = getClassNode("class C { public function get s() { return \"hello\"; } public function set s(value):void {} }");
		asBlockWalker.visitClass(node);
		assertOut("/**\n * @constructor\n */\nC = function() {\n};\n\n\n/**\n * @nocollapse\n * @export\n * @type {string}\n */\nC.prototype.s;\n\n\nC.prototype.get__s = function() {\n  return \"hello\";\n};\n\n\nC.prototype.set__s = function(value) {\n};\n\n\nObject.defineProperties(C.prototype, /** @lends {C.prototype} */ {\n/**\n * @type {string}\n */\ns: {\nget: C.prototype.get__s,\nset: C.prototype.set__s}}\n);");
	}

	@Test
	public void testInferSetterWithNoDefaultValue()
	{
		ISetterNode node = (ISetterNode) getAccessor("public function set s(value) {}");
		asBlockWalker.visitFunction(node);
		assertOut("/**\n * @param {*} value\n */\nRoyaleTest_A.prototype.s = function(value) {\n}");
	}

	@Test
	public void testInferGetterWithOverrideAndSuperInferredReturn()
	{
		IFileNode node = compileAS(
			"package { public class A {} }\n" + 
			"class C { public function get s() { return \"hello\"; } }\n" +
			"class D extends C { override public function get s() { return null; } }");
		IClassNode d = null;
		for(int i = 0; i < node.getChildCount(); i++)
		{
			IASNode child = node.getChild(i);
			if (child instanceof IClassNode)
			{
				IClassNode childClass = (IClassNode) child;
				if ("D".equals(childClass.getName()))
				{
					d = childClass;
					break;
				}
			}
		}
		asBlockWalker.visitClass(d);
		assertOut("/**\n * @constructor\n * @extends {C}\n */\nD = function() {\n  D.base(this, 'constructor');\n};\ngoog.inherits(D, C);\n\n\nD.prototype.get__s = function() {\n  return null;\n};\n\n\nObject.defineProperties(D.prototype, /** @lends {D.prototype} */ {\n/**\n * @type {string}\n */\ns: {\nget: D.prototype.get__s}}\n);");
	}

	@Test
	public void testInferGetterWithOverrideAndSuperDeclaredReturn()
	{
		IFileNode node = compileAS(
			"package { public class A {} }\n" + 
			"class C { public function get s():String { return null; } }\n" +
			"class D extends C { override public function get s() { return null; } }");
		IClassNode d = null;
		for(int i = 0; i < node.getChildCount(); i++)
		{
			IASNode child = node.getChild(i);
			if (child instanceof IClassNode)
			{
				IClassNode childClass = (IClassNode) child;
				if ("D".equals(childClass.getName()))
				{
					d = childClass;
					break;
				}
			}
		}
		asBlockWalker.visitClass(d);
		assertOut("/**\n * @constructor\n * @extends {C}\n */\nD = function() {\n  D.base(this, 'constructor');\n};\ngoog.inherits(D, C);\n\n\nD.prototype.get__s = function() {\n  return null;\n};\n\n\nObject.defineProperties(D.prototype, /** @lends {D.prototype} */ {\n/**\n * @type {string}\n */\ns: {\nget: D.prototype.get__s}}\n);");
	}

	@Test
	public void testInferSetterWithOverrideAndSuperInferredType()
	{
		IFileNode node = compileAS(
			"package { public class A {} }\n" + 
			"class C { public function get s():String {} public function set s(p) {} }\n" +
			"class D extends C { override public function set s(p) {} }");
		IClassNode d = null;
		for(int i = 0; i < node.getChildCount(); i++)
		{
			IASNode child = node.getChild(i);
			if (child instanceof IClassNode)
			{
				IClassNode childClass = (IClassNode) child;
				if ("D".equals(childClass.getName()))
				{
					d = childClass;
					break;
				}
			}
		}
		asBlockWalker.visitClass(d);
		assertOut("/**\n * @constructor\n * @extends {C}\n */\nD = function() {\n  D.base(this, 'constructor');\n};\ngoog.inherits(D, C);\n\n\nD.prototype.set__s = function(p) {\n};\n\n\nObject.defineProperties(D.prototype, /** @lends {D.prototype} */ {\n/**\n * @type {string}\n */\ns: {\nget: C.prototype.get__s,\nset: D.prototype.set__s}}\n);");
	}

	@Test
	public void testInferSetterWithOverrideAndSuperDeclaredType()
	{
		IFileNode node = compileAS(
			"package { public class A {} }\n" + 
			"class C { public function set s(p:String) {} }\n" +
			"class D extends C { override public function set s(p) {} }");
		IClassNode d = null;
		for(int i = 0; i < node.getChildCount(); i++)
		{
			IASNode child = node.getChild(i);
			if (child instanceof IClassNode)
			{
				IClassNode childClass = (IClassNode) child;
				if ("D".equals(childClass.getName()))
				{
					d = childClass;
					break;
				}
			}
		}
		asBlockWalker.visitClass(d);
		assertOut("/**\n * @constructor\n * @extends {C}\n */\nD = function() {\n  D.base(this, 'constructor');\n};\ngoog.inherits(D, C);\n\n\nD.prototype.set__s = function(p) {\n};\n\n\nObject.defineProperties(D.prototype, /** @lends {D.prototype} */ {\n/**\n * @type {string}\n */\ns: {\nset: D.prototype.set__s}}\n);");
	}

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}