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

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogMethodMembers;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSMethodMembers extends TestGoogMethodMembers
{

    @Override
    @Test
    public void testMethod_withReturnType()
    {
        IFunctionNode node = getMethod("function foo():int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @return {number}\n */\nFalconTest_A.prototype.foo = function() {\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withParameterReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {*} bar\n * @return {number}\n */\nFalconTest_A.prototype.foo = function(bar) {\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @return {number}\n */\nFalconTest_A.prototype.foo = function(bar) {\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withDefaultParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String = \"baz\"):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string=} bar\n * @return {number}\n */\nFalconTest_A.prototype.foo = function(bar) {\n  bar = typeof bar !== 'undefined' ? bar : \"baz\";\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withMultipleDefaultParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String, baz:int = null):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n */\nFalconTest_A.prototype.foo = function(bar, baz) {\n  baz = typeof baz !== 'undefined' ? baz : null;\n  return -1;\n}");
    }

    @Test
    public void testMethod_withDefaultParameterComplexTypeReturnType()
    {
        IFunctionNode node = getMethodWithPackage("static const BAR:String = 'bar'; function foo(bar:String = FalconTest_A.BAR):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string=} bar\n * @return {number}\n */\nfoo.bar.FalconTest_A.prototype.foo = function(bar) {\n  bar = typeof bar !== 'undefined' ? bar : foo.bar.FalconTest_A.BAR;\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withRestParameterTypeReturnType()
    {
        IFunctionNode node = getMethod("function foo(bar:String, ...rest):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @param {...} rest\n * @return {number}\n */\nFalconTest_A.prototype.foo = function(bar, rest) {\n  rest = Array.prototype.slice.call(arguments, 1);\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespaceCustom()
    {
        IFunctionNode node = getMethod("import flash.utils.flash_proxy;flash_proxy function foo(bar:String, baz:int = null):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n */\nFalconTest_A.prototype[\"http://www.adobe.com/2006/actionscript/flash/proxy::foo\"] = function(bar, baz) {\n  baz = typeof baz !== 'undefined' ? baz : null;\n  return -1;\n}");
    }

    //--------------------------------------------------------------------------
    // Doc Specific Tests 
    //--------------------------------------------------------------------------

    @Override
    @Test
    public void testConstructor_withThisInBody()
    {
        IClassNode node = (IClassNode) getNode("public function FalconTest_A(){this.foo();}; private function foo():String{return '';};", IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n  this.foo();\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('FalconTest_A', FalconTest_A);\n\n\n/**\n * @private\n * @return {string}\n */\nFalconTest_A.prototype.foo = function() {\n  return '';\n};");
    }

    @Test
    public void testConstructor_withImplicitThisInBody()
    {
        IClassNode node = (IClassNode) getNode("public function FalconTest_A(){foo();}; private function foo():String{return '';};", IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n  this.foo();\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('FalconTest_A', FalconTest_A);\n\n\n/**\n * @private\n * @return {string}\n */\nFalconTest_A.prototype.foo = function() {\n  return '';\n};");
    }

    @Override
    @Test
    public void testMethod_withThisInBody()
    {
        IClassNode node = (IClassNode) getNode("public function FalconTest_A(){}; private var baz:String; private function foo():String{return this.baz;};", IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
         assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('FalconTest_A', FalconTest_A);\n\n\n/**\n * @private\n * @type {string}\n */\nFalconTest_A.prototype.baz;\n\n\n/**\n * @private\n * @return {string}\n */\nFalconTest_A.prototype.foo = function() {\n  return this.baz;\n};");
    }

    @Test
    public void testMethod_withImplicitThisInBody()
    {
        IClassNode node = (IClassNode) getNode("public function FalconTest_A(){}; private var baz:String; private function foo():String{return baz;};", IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
         assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('FalconTest_A', FalconTest_A);\n\n\n/**\n * @private\n * @type {string}\n */\nFalconTest_A.prototype.baz;\n\n\n/**\n * @private\n * @return {string}\n */\nFalconTest_A.prototype.foo = function() {\n  return this.baz;\n};");
    }

    @Override
    @Test
    public void testMethod_withThisInBodyComplex()
    {
        IClassNode node = (IClassNode) getNode("public function FalconTest_A(){}; private function foo(value:int):String{return value;}; private function bar():String{if(true){while(i){return this.foo(42);}}};", IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('FalconTest_A', FalconTest_A);\n\n\n/**\n * @private\n * @param {number} value\n * @return {string}\n */\nFalconTest_A.prototype.foo = function(value) {\n  return value;\n};\n\n\n/**\n * @private\n * @return {string}\n */\nFalconTest_A.prototype.bar = function() {\n  if (true) {\n    while (i) {\n      return this.foo(42);\n    }\n  }\n};");
    }

    @Test
    public void testMethod_withImplicitThisInBodyComplex()
    {
        IClassNode node = (IClassNode) getNode("public function FalconTest_A(){}; private function foo(value:int):String{return value;}; private function bar():void{if(true){while(i){foo(42);}}};", IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('FalconTest_A', FalconTest_A);\n\n\n/**\n * @private\n * @param {number} value\n * @return {string}\n */\nFalconTest_A.prototype.foo = function(value) {\n  return value;\n};\n\n\n/**\n * @private\n */\nFalconTest_A.prototype.bar = function() {\n  if (true) {\n    while (i) {\n      this.foo(42);\n    }\n  }\n};");
    }

    @Override
    @Test
    public void testMethod_withNamespace()
    {
        IFunctionNode node = getMethod("public function foo(bar:String, baz:int = null):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        // we ignore the 'public' namespace completely
        assertOut("/**\n * @export\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n */\nFalconTest_A.prototype.foo = function(bar, baz) {\n  baz = typeof baz !== 'undefined' ? baz : null;\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespaceModifiers()
    {
        IFunctionNode node = getMethod("public static function foo(bar:String, baz:int = null):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        // (erikdebruin) here we actually DO want to declare the method
        //               directly on the 'class' constructor instead of the
        //               prototype!
        assertOut("/**\n * @export\n * @param {string} bar\n * @param {number=} baz\n * @return {number}\n */\nFalconTest_A.foo = function(bar, baz) {\n  baz = typeof baz !== 'undefined' ? baz : null;\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespaceModifierOverride()
    {
        IFunctionNode node = getMethod("public override function foo(bar:String, baz:int = null):int{  return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n * @override\n */\nFalconTest_A.prototype.foo = function(bar, baz) {\n  baz = typeof baz !== 'undefined' ? baz : null;\n  return -1;\n}");
    }

    @Override
    @Test
    public void testMethod_withNamespaceModifierOverrideBackwards()
    {
        IFunctionNode node = getMethod("override public function foo(bar:String, baz:int = null):int{return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n * @override\n */\nFalconTest_A.prototype.foo = function(bar, baz) {\n  baz = typeof baz !== 'undefined' ? baz : null;\n  return -1;\n}");
    }

    @Test
    public void testMethod_withConstDeclaration()
    {
        IFunctionNode node = getMethod("public function foo():String{const A:String = 'Hello World'; return A;}");
        asBlockWalker.visitFunction(node);
        assertOut("/**\n * @export\n * @return {string}\n */\nFalconTest_A.prototype.foo = function() {\n  \n/**\n * @const\n * @type {string}\n */\nvar A = 'Hello World';\n  return A;\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
    
}
