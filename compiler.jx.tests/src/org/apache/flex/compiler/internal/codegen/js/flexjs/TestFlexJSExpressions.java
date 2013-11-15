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

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogExpressions;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.tree.as.ClassNode;
import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSExpressions extends TestGoogExpressions
{

    @Ignore
    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMember()
    {
        // (erikdebruin) this test doesn't make sense in FlexJS context
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "if (a) super.foo;", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("super.foo");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_1()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo();}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  if (a)\n    goog.base(this, 'foo');\n}");
    }

    @Override
    @Test
    public void testVisitLanguageIdentifierNode_SuperMethod_2()
    {
        IFunctionNode node = getMethod("function foo(){if (a) super.foo(a, b, c);}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  if (a)\n    goog.base(this, 'foo', a, b, c);\n}");
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
        assertOut("this.get_c() + 10");
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
    public void testVisitBinaryOperatorNode_setterAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:int):void {}; public function c() { b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentWithThis()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:int):void {}; public function c() { this.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentWithThisMXML()
    {
        // simulate MXML script conditions.
        // get class def
        // disconnect fileNode from parent
        // set thisclass on emitter to class def
        IFileNode node = (IFileNode) getNode(
                "public class B { public function c() { this.b = 1; }; public function set b(value:int):void {}}",
                IFileNode.class, WRAP_LEVEL_PACKAGE, true);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                fnode, IBinaryOperatorNode.class);
        ((NodeBase)fnode).setParent(null);
        IDefinition def = classnode.getDefinition();

        ((JSFlexJSEmitter)asEmitter).thisClass = def;
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("this.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentMXML()
    {
        // simulate MXML script conditions.
        // get class def
        // disconnect fileNode from parent
        // set thisclass on emitter to class def
        IFileNode node = (IFileNode) getNode(
                "public class B { public function c() { b = 1; }; public function set b(value:int):void {}}",
                IFileNode.class, WRAP_LEVEL_PACKAGE, true);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                fnode, IBinaryOperatorNode.class);
        ((NodeBase)fnode).setParent(null);
        IDefinition def = classnode.getDefinition();

        ((JSFlexJSEmitter)asEmitter).thisClass = def;
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("this.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:int):void {}; public function c(other:B) { other.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_nestedSetterAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:int):void {}; public function get d():B {}; public function c(other:B) { d.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.get_d().get_d().set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_nestedSetterAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:int):void {}; public function get d():B {}; public function c(other:B) { other.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.get_d().set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentFromGetter()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:int):void {}; public function c() { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.set_b(this.get_b() + 1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentFromGetterMaskedByLocal()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:int):void {}; public function c() { var b:int; b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_setterAssignmentFromGetterMaskedByParam()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public function set b(value:int):void {}; public function c(b:int) { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; public function c() { b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentWithThis()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; public function c() { this.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; public function c(other:B) { other.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableSetterAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; [Bindable] public var d:B; public function c(other:B) { d.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.get_d().get_d().set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableSetterAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; [Bindable] public var d:B; public function c(other:B) { other.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.get_d().set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentFromGetter()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; public function c() { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.set_b(this.get_b() + 1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentFromGetterMaskedByLocal()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; public function c() { var b:int; b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_bindableAssignmentFromGetterMaskedByParam()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; public function c(b:int) { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:int; public function c() { b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentWithThis()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:int; public function c() { this.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:int; public function c(other:B) { other.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varSetterAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; public var d:B; public function c(other:B) { d.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.d.d.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_varVarAssignment()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:int; public var d:B; public function c(other:B) { d.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.d.d.b = 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varSetterAssignmentOtherInstance()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {[Bindable] public var b:int; public var d:B; public function c(other:B) { other.d.b = 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("other.d.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentFromVar()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:int; public function c() { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("this.b = this.b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentFromVarMaskedByLocal()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:int; public function c() { var b:int; b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_varAssignmentFromVarMaskedByParam()
    {
        IBinaryOperatorNode node = (IBinaryOperatorNode) getNode(
                "public class B {public var b:int; public function c(b:int) { b = b + 1; }}",
                IBinaryOperatorNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitBinaryOperator(node);
        assertOut("b = b + 1");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignment()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { b = 1; }; public static function set b(value:int):void {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentWithPath()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { foo.bar.B.b = 1; }; public static function set b(value:int):void {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B.set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentOtherInstance()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { d.b = 1; }; public function set b(value:int):void {}; public static function get d():B {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B.get_d().set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentOtherInstanceMXML()
    {
        // simulate MXML script conditions.
        // get class def
        // disconnect fileNode from parent
        // set thisclass on emitter to class def
        IFileNode node = (IFileNode) getNode(
                "public class B {public function c() { d.b = 1; }; public function set b(value:int):void {}; public static function get d():B {}}",
                IFileNode.class, WRAP_LEVEL_PACKAGE, true);
        IFunctionNode fnode = (IFunctionNode) findFirstDescendantOfType(
                node, IFunctionNode.class);
        IClassNode classnode = (IClassNode) findFirstDescendantOfType(
                node, IClassNode.class);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                fnode, IBinaryOperatorNode.class);
        ((NodeBase)fnode).setParent(null);
        IDefinition def = classnode.getDefinition();

        ((JSFlexJSEmitter)asEmitter).thisClass = def;
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B.get_d().set_b(1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentFromGetter()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { b = b + 1; }; public static function set b(value:int):void {}; public static function get b():int {}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) findFirstDescendantOfType(
                node, IBinaryOperatorNode.class);
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("foo.bar.B.set_b(foo.bar.B.get_b() + 1)");
    }

    @Test
    public void testVisitBinaryOperatorNode_staticSetterAssignmentFromGetterMaskedByLocal()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function c() { var b:int; b = b + 1; }; public static function set b(value:int):void {}; public static function get b():int {}}",
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
                "public class B {public function c(b:int) { b = b + 1; }; public static function set b(value:int):void {}; public static function get b():int {}}",
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
        IDefinition def = classnode.getDefinition();

        ((JSFlexJSEmitter)asEmitter).thisClass = def;
        asBlockWalker.visitBinaryOperator(bnode);
        assertOut("org.apache.flex.utils.Language.as(this.model, foo.bar.E, true).set_labelText(null)");
    }

    @Test
    public void testNamedFunctionAsArgument()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {}; function d():void {}; c(d); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @expose\n */\nB.prototype.b = function() {\n  function c(f) {\n  };\n  function d() {\n  };\n  goog.bind(c, this)(goog.bind(d, this));\n}");
    }

    @Test
    public void testNamedFunctionAsArgument2()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(s:String, f:Function):void {}; function d():void {}; c('foo', d); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @expose\n */\nB.prototype.b = function() {\n  function c(s, f) {\n  };\n  function d() {\n  };\n  goog.bind(c, this)('foo', goog.bind(d, this));\n}");
    }

    @Test
    public void testNamedFunctionAsArgument3()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() {  c('foo', d); function c(s:String, f:Function):void {}; function d():void {};}}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @expose\n */\nB.prototype.b = function() {\n  goog.bind(c, this)('foo', goog.bind(d, this));\n  function c(s, f) {\n  };\n  function d() {\n  };\n}");
    }

    @Test
    public void testNamedFunctionAsArgument4()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function d():void {}; c('foo', d); } public function c(s:String, f:Function):void {};}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @expose\n */\nB.prototype.b = function() {\n  function d() {\n  };\n  this.c('foo', goog.bind(d, this));\n}");
    }

    @Test
    public void testMethodAsArgument()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b() { function c(f:Function):void {}; c(b); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @expose\n */\nB.prototype.b = function() {\n  function c(f) {\n  };\n  goog.bind(c, this)(goog.bind(this.b, this));\n}");
    }

    @Test
    public void testStaticMethodAsArgument()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {static public function b() { function c(f:Function):void {}; c(b); }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @expose\n */\nfoo.bar.B.b = function() {\n  function c(f) {\n  };\n  goog.bind(c, this)(foo.bar.B.b);\n}");
    }

    @Test
    public void testNativeGetter()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b():int { var s:String; return s.length; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        // String.length is a getter but is a property in JS, so don't generate set_length() call.
        assertOut("/**\n * @expose\n * @return {number}\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {string} */ s;\n  return s.length;\n}");
    }

    @Test
    public void testNativeVectorGetter()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b():int { var a:Vector.<String>; return a.length; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        // String.length is a getter but is a property in JS, so don't generate set_length() call.
        assertOut("/**\n * @expose\n * @return {number}\n */\nfoo.bar.B.prototype.b = function() {\n  var /** @type {Vector.<string>} */ a;\n  return a.length;\n}");
    }

    //----------------------------------
    // Other
    //----------------------------------

    @Test
    public void testClassCast()
    {
        IClassNode node = (IClassNode) getNode("import spark.components.Button; public class B implements Button { public function B() { Button(b).label = ''; } }", ClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n * @implements {spark.components.Button}\n */\nB = function() {\n  org.apache.flex.utils.Language.as(b, spark.components.Button, true).set_label('');\n};");
    }

    @Test
    public void testClassCastOfGetter()
    {
        IFunctionNode node = getMethod("function foo(){var foo:Object = FalconTest_A(bar).bar = '';}; public function get bar():Object { return this; };");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  var /** @type {Object} */ foo = org.apache.flex.utils.Language.as(this.get_bar(), FalconTest_A, true).set_bar('');\n}");
    }

    @Test
    public void testFunctionCall()
    {
        IFunctionNode node = getMethod("function foo(){bar(b).text = '';}");
        asBlockWalker.visitFunction(node);
        assertOut("FalconTest_A.prototype.foo = function() {\n  bar(b).text = '';\n}");
    }

    @Test
    public void testComplexBooleanExpression()
    {
        IFunctionNode node = getMethod("function foo(b:Boolean):Boolean {var c:String; var d:String; if (!(b ? c : d)) { return b;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {boolean} b\n * @return {boolean}\n */\nFalconTest_A.prototype.foo = function(b) {\n  var /** @type {string} */ c;\n  var /** @type {string} */ d;\n  if (!(b ? c : d)) {\n    return b;\n  }\n}");
    }

    @Override
    @Test
    public void testAnonymousFunctionWithParamsReturn()
    {
        IVariableNode node = (IVariableNode) getNode(
                "var a:Object = function(foo:int, bar:String = 'goo'):int{return -1;};",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        assertOut("var /** @type {Object} */ a = function(foo, bar) {\n  bar = typeof bar !== 'undefined' ? bar : 'goo';\n  return -1;\n}");
    }

    @Override
    @Test
    public void testAnonymousFunctionAsArgument()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode(
                "addListener('foo', function(event:Object):void{doit();})",
                IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertOut("addListener('foo', function(event) {\n  doit();\n})");
    }

    @Override
    @Test
    public void testVisitAs()
    {
        IBinaryOperatorNode node = getBinaryNode("a as b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("org.apache.flex.utils.Language.as(a, b)");
    }

    @Test
    public void testVisitAs2()
    {
        IFunctionNode node = (IFunctionNode) getNode(
                "public class B {public function b(o:Object):int { var a:B; a = o as B; }}",
                IFunctionNode.class, WRAP_LEVEL_PACKAGE, true);
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @expose\n * @param {Object} o\n * @return {number}\n */\nfoo.bar.B.prototype.b = function(o) {\n  var /** @type {B} */ a;\n  a = org.apache.flex.utils.Language.as(o, foo.bar.B);\n}");
    }

    @Override
    @Test
    public void testVisitBinaryOperator_Is()
    {
        IBinaryOperatorNode node = getBinaryNode("a is b");
        asBlockWalker.visitBinaryOperator(node);
        assertOut("org.apache.flex.utils.Language.is(a, b)");
    }

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
