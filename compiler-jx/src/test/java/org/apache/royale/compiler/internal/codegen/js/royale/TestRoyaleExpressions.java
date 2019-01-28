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

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.codegen.js.goog.TestGoogExpressions;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.parsing.as.RoyaleASDocDelegate;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.internal.tree.as.LiteralNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestRoyaleExpressions extends TestGoogExpressions
 {
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
        workspace.setASDocDelegate(new RoyaleASDocDelegate());
    	JSGoogConfiguration config = new JSGoogConfiguration();
    	try {
			config.setKeepASDoc(null, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	((RoyaleJSProject)project).config = config;
        super.setUp();
    }

    @Test
	public void testVisitDynamicAccessString()
    {
        IDynamicAccessNode node = (IDynamicAccessNode) getNode(
                "public class KnownMember { public function KnownMember() { this[\"knownMember\"] = 4; } public var knownMember:Number; }", IDynamicAccessNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitDynamicAccess(node);
        assertOut("this[\"knownMember\"]");
    }
    
    @Test
	public void testVisitDynamicAccessQName()
    {
        IDynamicAccessNode node = (IDynamicAccessNode) getNode(
                "public class KnownMember { public function KnownMember() { var q:QName; this[q] = 4; } public var knownMember:Number; }", IDynamicAccessNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitDynamicAccess(node);
        assertOut("this[q.objectAccessFormat()]");
    }
    
    @Test
	public void testVisitDynamicAccessQName2()
    {
        IDynamicAccessNode node = (IDynamicAccessNode) getNode(
                "public class KnownMember { public function KnownMember() { this[new QName(new Namespace('ns'), 'knownMember')] = 4; } public var knownMember:Number; }", IDynamicAccessNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitDynamicAccess(node);
        assertOut("this[new QName(new Namespace('ns'), 'knownMember').objectAccessFormat()]");
    }
    
    @Ignore
    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMember()
    {
        // (erikdebruin) this test doesn't make sense in Royale context
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) super.foo;", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("super.foo");
    }

    @Test
    public void testVisitLanguageIdentifierNode_SuperGetter()
    {
        IClassNode node = (IClassNode)getNode("public function get defaultPrevented():Boolean " +
        		                       "{ return super.isDefaultPrevented(); }" + 
        		                       "override public function isDefaultPrevented():Boolean" +
                                       "{ return defaultPrevented; }", IClassNode.class);
        // getters and setters don't get output until the class is output so you can't just visit the accessorNode
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\n" + 
        		  "RoyaleTest_A = function() {\n" +
        		  "};\n\n\n" +
        		  "/**\n" +
        		  " * Prevent renaming of class. Needed for reflection.\n" +
        		  " */\n" +
        		  "goog.exportSymbol('RoyaleTest_A', RoyaleTest_A);\n\n\n" +
        		  "RoyaleTest_A.prototype.royaleTest_a = function() {\n" +
        		  "  var self = this;\n" +
        		  "  ;\n" +
        		  "  function isDefaultPrevented() {\n" +
        		  "    return defaultPrevented;\n  };\n  \n" +
        		  "};\n\n\n" +
        		  "RoyaleTest_A.prototype.get__defaultPrevented = function() {\n" +
        		  "  return RoyaleTest_A.superClass_.isDefaultPrevented.apply(this);\n" +
        		  "};\n\n\n" +
        		  "Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n" +
        		  "/**\n  * @export\n  * @type {boolean} */\n" +
        		  "defaultPrevented: {\nget: RoyaleTest_A.prototype.get__defaultPrevented}}\n);");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_1()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  if (a)\n    RoyaleTest_A.superClass_.foo.apply(this);\n}");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_2()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo(a, b, c);}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  if (a)\n    RoyaleTest_A.superClass_.foo.apply(this, [ a, b, c] );\n}");
    }
    
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethodCustomNamespace()
    {
        IFunctionNode node = (IFunctionNode)getNode("import custom.TestProxy;import custom.custom_namespace;use namespace custom_namespace;public class RoyaleTest_A extends TestProxy { custom_namespace function foo(){if (a) super.setProperty(a, b);}}",
        					IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nRoyaleTest_A.prototype.http_$$ns_apache_org$2017$custom$namespace__foo = function() {\n  if (a)\n    RoyaleTest_A.superClass_.http_$$ns_apache_org$2017$custom$namespace__setProperty.apply(this, [ a, b] );\n}");
    }

    @Test
    public void testVisitLanguageIdentifierNode_SuperMethodAsFunctionReference()
    {
        IFileNode node = (IFileNode)getNode("package { public class RoyaleTest_A extends Base { override public function foo() {var f:Function = super.foo;} } }\n" +
        		"class Base { public function foo(){} }", IFileNode.class, 0, false);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IClassDefinition def = classnode.getDefinition();
        ((JSRoyaleEmitter)asEmitter).getModel().setCurrentClass(def);
        asBlockWalker.visitFunction(fnode);
        assertOut("/**\n * @export\n * @override\n */\nRoyaleTest_A.prototype.foo = function() {\n  var /** @type {Function} */ f = org.apache.royale.utils.Language.closure(RoyaleTest_A.superClass_.foo, this, 'foo');\n}");
    }
    
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethodAsVarFunctionReference()
    {
    	IFileNode node = (IFileNode)getNode("package { public class RoyaleTest_A extends Base { override public function foo() {var f:Function; f = super.foo;} } }\n" +
        		"class Base { public function foo(){} }", IFileNode.class, 0, false);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IClassDefinition def = classnode.getDefinition();
        ((JSRoyaleEmitter)asEmitter).getModel().setCurrentClass(def);
        asBlockWalker.visitFunction(fnode);
        assertOut("/**\n * @export\n * @override\n */\nRoyaleTest_A.prototype.foo = function() {\n  var /** @type {Function} */ f;\n  f = org.apache.royale.utils.Language.closure(RoyaleTest_A.superClass_.foo, this, 'foo');\n}");
    }
    
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethodInApply()
    {
    	IFileNode node = (IFileNode)getNode("package { public class RoyaleTest_A extends Base { override public function foo() {super.foo.apply(this, [a, b, c]);} } }\n" +
        		"class Base { public function foo(){} }", IFileNode.class, 0, false);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IClassDefinition def = classnode.getDefinition();
        ((JSRoyaleEmitter)asEmitter).getModel().setCurrentClass(def);
        asBlockWalker.visitFunction(fnode);
        assertOut("/**\n * @export\n * @override\n */\nRoyaleTest_A.prototype.foo = function() {\n  RoyaleTest_A.superClass_.foo.apply(this, [a, b, c]);\n}");
    }
    
    //----------------------------------
    // Primary expression keywords
    //----------------------------------

    //----------------------------------
    // Logical
    //----------------------------------

    @Override
    @Test
    public void testVisitBinaryOperatorNode_LogicalAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &&= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = a && b");
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalAndAssignmentInClass()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode)getNode("public var foo:Boolean;private function test(target:RoyaleTest_A):void { target.foo &&= foo }", IBinaryOperatorNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("target.foo = target.foo && this.foo");
    }

    @Override
    @Test
    public void testVisitBinaryOperatorNode_LogicalOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ||= b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("a = a || b");
    }

    @Test
    public void testVisitBinaryOperatorNode_getterAtEndOfLeftSide()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function b(s:String):Boolean {return this.c + 10; } public function get c():int { return 0; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.c + 10");
    }

    @Test
    public void testVisitBinaryOperatorNode_functionCallOnLeft()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function b(s:String):Boolean {return s.toLowerCase() == 'foo'; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("s.toLowerCase() == 'foo'");
    }

    @Test
    public void testVisitBinaryOperatorNode_functionCallOnLeftContained()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function b(s:String):Boolean {return (s.toLowerCase() == 'foo'); }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("(s.toLowerCase() == 'foo')");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentIntVarToInt()
    {
        IBinaryOperatorNode node = getBinaryNode("var integer1:int;var integer2:int;integer1 = integer2");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("integer1 = integer2");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentNumberVarToInt()
    {
        IBinaryOperatorNode node = getBinaryNode("var integer:int;var number:Number;integer = number");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("integer = (number) >> 0");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentUintVarToInt()
    {
        IBinaryOperatorNode node = getBinaryNode("var integer:int;var unsigned_integer:uint;integer = unsigned_integer");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("integer = (unsigned_integer) >> 0");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentNumberLiteralToInt()
    {
        IBinaryOperatorNode node = getBinaryNode("var numToInt:int;numToInt = 123.4");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("numToInt = (123.4) >> 0");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentIntLiteralToInt()
    {
        IBinaryOperatorNode node = getBinaryNode("var numToInt:int;numToInt = 321");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("numToInt = 321");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentUintVarToUint()
    {
        IBinaryOperatorNode node = getBinaryNode("var unsigned_integer1:uint;var unsigned_integer2:uint;unsigned_integer1 = unsigned_integer2");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("unsigned_integer1 = unsigned_integer2");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentNumberVarToUint()
    {
        IBinaryOperatorNode node = getBinaryNode("var unsigned_integer:uint;var number:Number;unsigned_integer = number");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("unsigned_integer = (number) >>> 0");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentIntVarToUint()
    {
        IBinaryOperatorNode node = getBinaryNode("var unsigned_integer:uint;var integer:int;unsigned_integer = integer");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("unsigned_integer = (integer) >>> 0");
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentNumberLiteralToUint()
    {
        IBinaryOperatorNode node = getBinaryNode("var numToUint:uint;numToUint = 123.4");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("numToUint = (123.4) >>> 0");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:Number):void {}; public function c() { b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentWithThis()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:Number):void {}; public function c() { this.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentPrivate()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function get b():Number { return 0; } private function set b(value:Number):void {}; public function test() { this.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentPrivateWithNamespace()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function get b():Number { return 0; } private function set b(value:Number):void {}; public function test() { this.private::b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentWithThisMXML()
    {
        // simulate MXML script conditions.
        // get class def
        // disconnect fileNode from parent
        // set thisclass on emitter to class def
        IFileNode node = (IFileNode) getNode(
                "public class B { public function c() { this.b = 1; }; public function set b(value:Number):void {}}",
                IFileNode.class, WRAP_LEVEL_PACKAGE, true);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                fnode, IBinaryOperatorNode.class);
        ((NodeBase)fnode).setParent(null);
        IClassDefinition def = classnode.getDefinition();

        ((JSRoyaleEmitter)asEmitter).getModel().setCurrentClass(def);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentMXML()
    {
        // simulate MXML script conditions.
        // get class def
        // disconnect fileNode from parent
        // set thisclass on emitter to class def
        IFileNode node = (IFileNode) getNode(
                "public class B { public function c() { b = 1; }; public function set b(value:Number):void {}}",
                IFileNode.class, WRAP_LEVEL_PACKAGE, true);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                fnode, IBinaryOperatorNode.class);
        ((NodeBase)fnode).setParent(null);
        IClassDefinition def = classnode.getDefinition();

        ((JSRoyaleEmitter)asEmitter).getModel().setCurrentClass(def);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:Number):void {}; public function c(other:B) { other.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_nestedSetterAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:Number):void {}; public function get d():B {}; public function c(other:B) { d.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.d.d.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_nestedSetterAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:Number):void {}; public function get d():B {}; public function c(other:B) { other.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.d.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentFromGetter()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:Number):void {}; public function c() { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = this.b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentFromGetterMaskedByLocal()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:Number):void {}; public function c() { var b:Number; b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentFromGetterMaskedByParam()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:Number):void {}; public function c(b:Number) { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentFromInternalVar()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {var b:Number; public function c() { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = this.b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentFromInternalVar()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {static var b:Number; public function c() { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("B.b = B.b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; public function c() { b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentWithThis()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; public function c() { this.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; public function c(other:B) { other.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableSetterAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; [Bindable] public var d:B; public function c(other:B) { d.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.d.d.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableSetterAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; [Bindable] public var d:B; public function c(other:B) { other.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.d.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentFromGetter()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; public function c() { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = this.b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentFromGetterMaskedByLocal()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; public function c() { var b:Number; b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentFromGetterMaskedByParam()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; public function c(b:Number) { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:Number; public function c() { b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentWithThis()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:Number; public function c() { this.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:Number; public function c(other:B) { other.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varSetterAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; public var d:B; public function c(other:B) { d.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.d.d.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varVarAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:Number; public var d:B; public function c(other:B) { d.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.d.d.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varSetterAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:Number; public var d:B; public function c(other:B) { other.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.d.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentFromVar()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:Number; public function c() { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = this.b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentFromVarMaskedByLocal()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:Number; public function c() { var b:Number; b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentFromVarMaskedByParam()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:Number; public function c(b:Number) { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignment()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { b = 1; }; public static function set b(value:Number):void {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B[\"b\"] = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentWithPath()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { foo.bar.B.b = 1; }; public static function set b(value:Number):void {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B[\"b\"] = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentOtherInstance()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { d.b = 1; }; public function set b(value:Number):void {}; public static function get d():B {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B[\"d\"].b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentOtherInstanceMXML()
    {
        // simulate MXML script conditions.
        // get class def
        // disconnect fileNode from parent
        // set thisclass on emitter to class def
        IFileNode node = (IFileNode) getNode(
                "public class B {public function c() { d.b = 1; }; public function set b(value:Number):void {}; public static function get d():B {}}",
                IFileNode.class, WRAP_LEVEL_PACKAGE, true);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                fnode, IBinaryOperatorNode.class);
        ((NodeBase)fnode).setParent(null);
        IClassDefinition def = classnode.getDefinition();

        ((JSRoyaleEmitter)asEmitter).getModel().setCurrentClass(def);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B[\"d\"].b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentFromGetter()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { b = b + 1; }; public static function set b(value:Number):void {}; public static function get b():Number {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B[\"b\"] = foo.bar.B[\"b\"] + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentFromGetterMaskedByLocal()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { var b:Number; b = b + 1; }; public static function set b(value:Number):void {}; public static function get b():Number {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentFromGetterMaskedByParam()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c(b:Number) { b = b + 1; }; public static function set b(value:Number):void {}; public static function get b():Number {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentFromSuperSuperClass()
    {
        // simulate MXML script conditions.
        // get class def
        // disconnect fileNode from parent
        // set thisclass on emitter to class def
        IFileNode node = (IFileNode) getNode(
                "class B extends C { public function c() { E(model).labelText = null; } } class C extends D {} class D { public var model:Object; } class E { public function set labelText(value:String) {} }",
                IFileNode.class, WRAP_LEVEL_PACKAGE, true);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                fnode, IBinaryOperatorNode.class);
        ((NodeBase)fnode).setParent(null);
        IClassDefinition def = classnode.getDefinition();

        ((JSRoyaleEmitter)asEmitter).getModel().setCurrentClass(def);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("org.apache.royale.utils.Language.as(this.model, foo.bar.E, true).labelText = null");
    }

    @Test
    public void testVisitBinaryOperatorNode_varDynamicSlashAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:Object; public function c() { b[\"\\\\\"] = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b[\"\\\\\"] = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_StringVarAssignmentFromObject()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:String; public var c:Object; public function d() { b = c; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = org.apache.royale.utils.Language.string(this.c)");
    }

    @Test
    public void testVisitBinaryOperatorNode_StringVarAssignmentFromObjectSupressed()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:String; public var c:Object; /**\n * @royalenoimplicitstringconversion\n */\npublic function d() { b = c; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        JSRoyaleDocEmitter docEmitter = (JSRoyaleDocEmitter)(asBlockWalker.getEmitter().getDocEmitter());
        IFunctionNode methodNode = (IFunctionNode)(node.getAncestorOfType(IFunctionNode.class));
        
        // this adds '/**\n * @royalenoimplicitstringconversion\n * @export\n */' to the output but parses
        // the asdoc so the emitter will suppress the output
        docEmitter.emitMethodDoc(methodNode, asBlockWalker.getProject());
        asBlockWalker.visitBinaryOperator(node);
        assertOut("/**\n * @royalenoimplicitstringconversion\n * @export\n */\nthis.b = this.c");
    }

    @Test
    public void testVisitBinaryOperatorNode_StringVarCompareWithObject()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:String; public var c:Object; public function d() { b == c; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b == this.c");
    }

    @Test
    public void testVisitBinaryOperatorNode_StringVarInObject()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:String; public var c:Object; public function d() { if (b in c); }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b in this.c");
    }

    @Test
    public void testVisitBinaryOperatorNode_ObjectPlusNumberLiteral()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function d(obj:Object, prop:String) { var foo:int = obj[prop] + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("obj[prop] + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_StringAssignFromStarToString()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:String; public var c:*; public function d() { b = c.toString(); }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = this.c.toString()");
    }

    @Test
    public void testVisitBinaryOperatorNode_NumberPlusString()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:String; public function d() { b = 10 + 'px'; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 10 + 'px'");
    }

    @Test
    public void testVisitBinaryOperatorNode_ArrayElementType()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function d() { var b:Number; b = c[0]; var c:C;}}\n[ArrayElementType(\"Number\")]\nclass C {}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = c[0]");
    }

    @Test
    public void testVisitBinaryOperatorNode_LotsOfBuiltinFunctions()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function d() { var seed:Number; seed = new Date().time - Math.random() * int.MAX_VALUE; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("seed = new Date().getTime() - Math.random() * 2147483648");
    }

    @Test
    public void testNamedFunctionAsArgument()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {}; function d():void {}; c(d); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(f) {\n  };\n  function d() {\n  };\n  c(d);\n}");
    }

    @Test
    public void testNamedFunctionAsArgument2()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(s:String, f:Function):void {}; function d():void {}; c('foo', d); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(s, f) {\n  };\n  function d() {\n  };\n  c('foo', d);\n}");
    }

    @Test
    public void testNamedFunctionAsArgument3()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() {  c('foo', d); function c(s:String, f:Function):void {}; function d():void {};}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(s, f) {\n  };\n  function d() {\n  };\n  c('foo', d);\n  \n}");
    }

    @Test
    public void testNamedFunctionAsArgument4()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function d():void {}; c('foo', d); } public function c(s:String, f:Function):void {};}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function d() {\n  };\n  this.c('foo', d);\n}");
    }

    @Test
    public void testArgumentAsArgument()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b(ff:Function) { function c(f:Function):void {}; c(ff); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n * @param {Function} ff\n */\nB.prototype.b = function(ff) {\n  var self = this;\n  function c(f) {\n  };\n  c(ff);\n}");
    }

    @Test
    public void testArgumentAsArgumentInStatic()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public static function b(ff:Function) { function c(f:Function):void {}; c(ff); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n * @param {Function} ff\n */\nB.b = function(ff) {\n  function c(f) {\n  };\n  c(ff);\n}");
    }

    @Test
    public void testMethodAsArgument()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {}; c(b); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(f) {\n  };\n  c(org.apache.royale.utils.Language.closure(this.b, this, 'b'));\n}");
    }

    @Test
    public void testStaticMethodAsArgument()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {static public function b() { function c(f:Function):void {}; c(b); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nfoo.bar.B.b = function() {\n  function c(f) {\n  };\n  c(foo.bar.B.b);\n}");
    }

    @Test
    public void testMethodAsVariable()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {}; var f:Function = b; c(f); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(f) {\n  };\n  var /** @type {Function} */ f = org.apache.royale.utils.Language.closure(this.b, this, 'b');\n  c(f);\n}");
    }
    
    @Test
    public void testCustomNamespaceMethodAsVariable()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.custom_namespace; use namespace custom_namespace;public class B {custom_namespace function b() { function c(f:Function):void {}; var f:Function = b; c(f); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nB.prototype.http_$$ns_apache_org$2017$custom$namespace__b = function() {\n  var self = this;\n  function c(f) {\n  };\n  var /** @type {Function} */ f = org.apache.royale.utils.Language.closure(this.http_$$ns_apache_org$2017$custom$namespace__b, this, 'http://ns.apache.org/2017/custom/namespace::b');\n  c(f);\n}");
    }
    
    @Test
    public void testCustomNamespaceMethodAsVariableWithoutUse()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.custom_namespace;;public class B {custom_namespace function b() { function c(f:Function):void {}; var f:Function = this.custom_namespace::b; c(f); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n */\nB.prototype.http_$$ns_apache_org$2017$custom$namespace__b = function() {\n  var self = this;\n  function c(f) {\n  };\n  var /** @type {Function} */ f = org.apache.royale.utils.Language.closure(this[new QName(custom.custom_namespace, 'b').objectAccessFormat()], this, 'http://ns.apache.org/2017/custom/namespace::b');\n  c(f);\n}");
    }
    
    @Test
    public void testCustomNamespaceMethodAsVariableViaMemberAccess()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.custom_namespace; use namespace custom_namespace;public class B {custom_namespace function b():int { return this.b(); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @return {number}\n */\nB.prototype.http_$$ns_apache_org$2017$custom$namespace__b = function() {\n  return this.http_$$ns_apache_org$2017$custom$namespace__b();\n}");
    }
    
    @Test
    public void testStaticMethodAsVariable()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {static public function b() { function c(f:Function):void {}; var f:Function = b; c(f); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nfoo.bar.B.b = function() {\n  function c(f) {\n  };\n  var /** @type {Function} */ f = foo.bar.B.b;\n  c(f);\n}");
    }
    
    @Test
    public void testStaticMethodAsVariableFullyQualified()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {static public function b() { function c(f:Function):void {}; var f:Function = foo.bar.B.b; c(f); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nfoo.bar.B.b = function() {\n  function c(f) {\n  };\n  var /** @type {Function} */ f = foo.bar.B.b;\n  c(f);\n}");
    }
    
    @Test
    public void testMethodAsAssign()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {}; var f:Function; f = b; c(f); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(f) {\n  };\n  var /** @type {Function} */ f;\n  f = org.apache.royale.utils.Language.closure(this.b, this, 'b');\n  c(f);\n}");
    }
    
    @Test
    public void testStaticMethodAsAssign()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {static public function b() { function c(f:Function):void {}; var f:Function; f = b; c(f); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nfoo.bar.B.b = function() {\n  function c(f) {\n  };\n  var /** @type {Function} */ f;\n  f = foo.bar.B.b;\n  c(f);\n}");
    }
    
    @Test
    public void testMethodAsValue()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {}; var f:Array = [b]; c(f[0]); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(f) {\n  };\n  var /** @type {Array} */ f = [org.apache.royale.utils.Language.closure(this.b, this, 'b')];\n  c(f[0]);\n}");
    }
    
    @Test
    public void testStaticMethodAsValue()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {static public function b() { function c(f:Function):void {}; var f:Array = [b]; c(f); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nfoo.bar.B.b = function() {\n  function c(f) {\n  };\n  var /** @type {Array} */ f = [foo.bar.B.b];\n  c(f);\n}");
    }
    
    @Test
    public void testThisMethodAsParam()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {}; c(this.b); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(f) {\n  };\n  c(org.apache.royale.utils.Language.closure(this.b, this, 'b'));\n}");
    }
    
    @Test
    public void testMethodAsParamInLocalFunction()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {c(d); }; } public function d() {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nB.prototype.b = function() {\n  var self = this;\n  function c(f) {\n    c(org.apache.royale.utils.Language.closure(self.d, self, 'd'));\n  };\n  \n}");
    }
    
    @Test
    public void testNativeGetter()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b():Number { var s:String; return s.length; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        // String.length is a getter but is a property in JS, so don't generate set_length() call.
        assertOut("/**\n * @export\n * @return {number}\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {string} */ s;\n  return s.length;\n}");
    }

    @Test
    public void testNativeVectorGetter()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b():Number { var a:Vector.<String>; return a.length; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        // String.length is a getter but is a property in JS, so don't generate set_length() call.
        assertOut("/**\n * @export\n * @return {number}\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {Array} */ a;\n  return a.length;\n}");
    }

    //----------------------------------
    // Other
    //----------------------------------

    @Test
    public void testClassCast()
    {
        IClassNode node = (IClassNode) getNode("import custom.TestOtherInterface; public class B implements TestOtherInterface { public function B() { TestOtherInterface(b).type = ''; } }", ClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {custom.TestOtherInterface}\n */\nB = function() {\n  org.apache.royale.utils.Language.as(b, custom.TestOtherInterface, true).type = '';\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('B', B);");
    }

    @Test
    public void testClassCastOfGetter()
    {
        IFunctionNode node = getMethod("function foo(){var foo:Object = RoyaleTest_A(bar).bar = '';}; public function get bar():Object { return this; };");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  var /** @type {Object} */ foo = org.apache.royale.utils.Language.as(this.bar, RoyaleTest_A, true).bar = '';\n}");
    }

    @Test
    public void testFunctionCall()
    {
        IFunctionNode node = getMethod("function foo(){bar(b).text = '';}");
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.foo = function() {\n  bar(b).text = '';\n}");
    }
    
    @Test
    public void testFunctionCallFullyQualified()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import goog.bind; public class B {public function b() { goog.bind(b, this); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nfoo.bar.B.prototype.b = function() {\n  goog.bind(org.apache.royale.utils.Language.closure(this.b, this, 'b'), this);\n}");
    }

    @Test
    public void testFunctionMemberFullyQualified()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "import custom.TestGlobalFunction; public class B {public function b() { TestGlobalFunction.length; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\nfoo.bar.B.prototype.b = function() {\n  custom.TestGlobalFunction.length;\n}");
    }

    @Test
    public void testComplexBooleanExpression()
    {
        IFunctionNode node = getMethod("function foo(b:Boolean):Boolean {var c:String; var d:String; if (!(b ? c : d)) { return b;}}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {boolean} b\n * @return {boolean}\n */\nRoyaleTest_A.prototype.foo = function(b) {\n  var /** @type {string} */ c;\n  var /** @type {string} */ d;\n  if (!(b ? c : d)) {\n    return b;\n  }\n}");
    }

    @Override
    @Test
    public void testAnonymousFunction()
    {
    	IFunctionNode node = (IFunctionNode) getNode("var a = function(){};",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n" +
        	  "  var self = this;\n" +
        	  "  var /** @type {*} */ a = function() {\n" +
        	  "  };\n" +
        	  "}");
    }

    @Override
    @Test
    public void testAnonymousFunctionWithParamsReturn()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n" +
      		  "  var self = this;\n" +
      		  "  var /** @type {Object} */ a = function(foo, bar) {\n" +
      		  "    bar = typeof bar !== 'undefined' ? bar : 'goo';\n" +
      		  "    return -1;\n" +
      		  "  };\n" +
      		  "}");
    }

    @Override
    @Test
    public void testAnonymousFunctionAsArgument()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "addListener('foo', function(event:Object):void{doit();})",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n" +
      		  "  var self = this;\n" +
      		  "  addListener('foo', function(event) {\n" +
      		  "    doit();\n" +
      		  "  });\n" +
      		  "}");
    }

    @Test
    public void testES5StrictAnonymousFunctions()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "var a:Object = {}; var b:Function = function(foo:Object) { foo.bar = 10 }; var c:Object = b(a);",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n" +
        		  "  var self = this;\n" +
        		  "  var /** @type {Object} */ a = {};\n" +
        		  "  var /** @type {Function} */ b = function(foo) {\n    foo.bar = 10;\n  };\n" +
        		  "  var /** @type {Object} */ c = b(a);\n}");
    }
    
    @Test
    public void testES5StrictNamedLocalFunctions()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "var a:Object = {}; function b(foo:Object) { foo.bar = 10 }; var c:Object = b(a);",
                IFunctionNode.class);
        asBlockWalker.visitFunction(node);
        assertOut("RoyaleTest_A.prototype.royaleTest_a = function() {\n" +
        		  "  var self = this;\n" +
        		  "  function b(foo) {\n    foo.bar = 10;\n  };\n" +
        		  "  var /** @type {Object} */ a = {};\n" +
        		  "  var /** @type {Object} */ c = b(a);\n}");
    }
    
    @Test
    public void testES5StrictNamedLocalFunctionsAsParameter()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public function foo() { var a:Array = []; a.filter(function isEven(element: int, index: int, arr: Array) : Boolean {\n" + 
                "  return element % 2 == 0;\n" + 
                "});}",
                IFunctionNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\n" + 
      		  "RoyaleTest_A.prototype.foo = function() {\n" +
      		  "  var self = this;\n" +
      		  "  function isEven(element, index, arr) {\n    return element % 2 == 0;\n  };\n  var /** @type {Array} */ a = [];\n"
      		  + "  a.filter(isEven);\n}");
    }
    
    @Test
    public void testParametersInInnerFunctions()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public var bar:String = baz; public function foo():void { function localFunction():void { trace(bar); } localFunction() }",
                IFunctionNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\n" + 
        		  "RoyaleTest_A.prototype.foo = function() {\n" +
        		  "  var self = this;\n" +
        		  "  function localFunction() {\n    org.apache.royale.utils.Language.trace(self.bar);\n  };\n" +
        		  "  localFunction();\n}");
    }
    
    @Test
    public void testInternalVarAccess()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "internal var bar:String = baz; public function foo():void { trace(bar); }",
                IFunctionNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n */\n" + 
        		  "RoyaleTest_A.prototype.foo = function() {\n" +
        		  "  org.apache.royale.utils.Language.trace(this.bar);\n}");
    }
    
    @Override
    @Test
    public void testVisitAs()
    {
        IBinaryOperatorNode node = getBinaryNode("a as b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("org.apache.royale.utils.Language.as(a, b)");
    }

    @Test
    public void testVisitAs2()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b(o:Object):int { var a:B; a = o as B; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n * @param {Object} o\n * @return {number}\n */\nfoo.bar.B.prototype.b = function(o) {\n  var /** @type {foo.bar.B} */ a;\n  a = org.apache.royale.utils.Language.as(o, foo.bar.B);\n}");
    }

    @Test
    public void testVisitAsInt()
    {
        IBinaryOperatorNode node = getBinaryNode("a as int");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("org.apache.royale.utils.Language.as(a, Number)");
    }

    @Test
    public void testVisitAsUint()
    {
        IBinaryOperatorNode node = getBinaryNode("a as uint");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("org.apache.royale.utils.Language.as(a, Number)");
    }

    @Test
    public void testVisitAsMemberVariable()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {private var memberVar:Class; public function b(o:Object):int { var a:B; a = o as memberVar; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n * @param {Object} o\n * @return {number}\n */\nfoo.bar.B.prototype.b = function(o) {\n  var /** @type {foo.bar.B} */ a;\n  a = org.apache.royale.utils.Language.as(o, this.memberVar);\n}");
    }

    @Test
    public void testVisitJSDoc()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class LinkableString {public function b(o:Object):int { var a:LinkableString; a = o as LinkableString; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n * @param {Object} o\n * @return {number}\n */\nfoo.bar.LinkableString.prototype.b = function(o) {\n  var /** @type {foo.bar.LinkableString} */ a;\n  a = org.apache.royale.utils.Language.as(o, foo.bar.LinkableString);\n}");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_Is()
    {
        IBinaryOperatorNode node = getBinaryNode("a is b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("org.apache.royale.utils.Language.is(a, b)");
    }

    @Override
    @Test
    public void testParentheses_3()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a = ((a + b) - (c + d)) * e;", IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {*} */ a = ((a + b) - (c + d)) * e");
    }

    @Test
    public void testVisitStringLiteralSingleEscapedBackslash()
    {
    	// a = "\\";
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = \"\\\\\"", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("\"\\\\\"");
    }

    @Test
    public void testVisitStringLiteralSingleEscapedBackslashInParens()
    {
    	// a = a.indexOf("\\");
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = a.indexOf(\"\\\\\")", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("\"\\\\\"");
    }
    
    @Test
    public void testVisitStringLiteralSingleEscapedBackslashSingleQuote()
    {
    	// a = "\\";
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = '\\\\'", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("'\\\\'");
    }

    @Test
    public void testVisitStringLiteralSlashN()
    {
    	// a = "\n";
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = \"\\n\"", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("\"\\n\"");
    }

    @Test
    public void testVisitStringLiteralSlashB()
    {
    	// a = "\n";
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = \"\\b\"", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("\"\\b\"");
    }

    @Test
    public void testVisitStringLiteralEmbeddedDoubleQuote()
    {
    	// a = " ' \" ";
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = \" ' \\\" \"", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("\" ' \\\" \"");
    }

    @Test
    public void testVisitStringLiteralSingleQuote()
    {
    	// a = ' \' " ';
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = ' \\' \" '", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("' \\' \" '");
    }

    @Test
    public void testVisitStringLiteral2029()
    {
    	// a = "\u2029";
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = \"\\u2029\"", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("\"\\u2029\"");
    }
    
    @Test
    public void testVisitStringLiteral2028()
    {
    	// a = "\u2028";
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = \"\\u2028\"", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("\"\\u2028\"");
    }
    
    @Test
    public void testVisitStringLiteralChinese()
    {
    	// a = "你好";
        LiteralNode node = (LiteralNode) getExpressionNode(
                "a = \"你好\"", LiteralNode.class);
        asBlockWalker.visitLiteral(node);
        assertOut("\"你好\"");
    }

    @Test
    public void testVisitCallFunctionReturnedFromFunction()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("function foo(a:String, b:String):Function { return null }; return foo(3, 4)(1, 2);", 
        							IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("foo(3, 4)(1, 2)");
    }
    
    @Test
    public void testVisitNewSimple()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("return new Fn(1, 2);", 
        							IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("new Fn(1, 2)");
    }
    
    @Test
    public void testVisitNewComplex()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("return new Fn(\"a\", \"b\", \"return a + b;\")(1, 2);", 
        							IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("new Fn(\"a\", \"b\", \"return a + b;\")(1, 2)");
    }

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

    
}
