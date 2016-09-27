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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogFieldMembers;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSFieldMembers extends TestGoogFieldMembers
{

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

    @Override
    @Test
    public void testField()
    {
        IVariableNode node = getField("var foo;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @export\n * @type {*}\n */\nFalconTest_A.prototype.foo");
    }

    @Test
    public void testField_withStringSetToNull()
    {
        IVariableNode node = getField("var foo:String = null;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @export\n * @type {string}\n */\nFalconTest_A.prototype.foo = null");
    }

    @Override
    @Test
    public void testField_withType()
    {
        IVariableNode node = getField("var foo:int;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @export\n * @type {number}\n */\nFalconTest_A.prototype.foo = 0");
    }

    @Override
    @Test
    public void testField_withTypeValue()
    {
        IVariableNode node = getField("var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @export\n * @type {number}\n */\nFalconTest_A.prototype.foo = 420");
    }

    @Test
    public void testField_withTypeValue_Negative()
    {
        IVariableNode node = getField("var foo:int = -420;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @export\n * @type {number}\n */\nFalconTest_A.prototype.foo = -420");
    }

    @Override
    @Test
    public void testField_withNamespaceTypeValue()
    {
        IVariableNode node = getField("private var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @type {number}\n */\nFalconTest_A.prototype.foo = 420");
    }

    @Override
    @Test
    public void testField_withCustomNamespaceTypeValue()
    {
        IVariableNode node = getField("mx_internal var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        // (erikdebruin) we ignore custom namespaces completely (are there side effects I'm missing?)
        assertOut("/**\n * @export\n * @type {number}\n */\nFalconTest_A.prototype.foo = 420");
    }

    @Override
    @Test
    public void testField_withNamespaceTypeCollection()
    {
        IVariableNode node = getField("protected var foo:Vector.<Foo>;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @protected\n * @type {Array}\n */\nFalconTest_A.prototype.foo");
    }

    @Override
    @Test
    public void testField_withNamespaceTypeCollectionComplex()
    {
        IVariableNode node = getField("protected var foo:Vector.<Vector.<Vector.<Foo>>>;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @protected\n * @type {Array}\n */\nFalconTest_A.prototype.foo");
    }

    @Override
    @Test
    public void testField_withNamespaceTypeValueComplex()
    {
    	IClassNode node = (IClassNode) getNode("protected var foo:Foo = new Foo('bar', 42);",
    			IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n\nthis.foo = new Foo('bar', 42);\n};\n\n\n/**\n * @protected\n * @type {Foo}\n */\nFalconTest_A.prototype.foo;");
    }

    @Test
    public void testStaticField()
    {
        IVariableNode node = getField("static var foo;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @expose\n * @type {*}\n */\nFalconTest_A.foo");
    }

    @Test
    public void testStaticField_withType()
    {
        IVariableNode node = getField("static var foo:int;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @expose\n * @type {number}\n */\nFalconTest_A.foo = 0");
    }

    @Test
    public void testStaticField_withTypeValue()
    {
        IVariableNode node = getField("static var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @expose\n * @type {number}\n */\nFalconTest_A.foo = 420");
    }

    @Test
    public void testField_withTypeValueArrayLiteral()
    {
    	IClassNode node = (IClassNode) getNode("protected var foo:Array = [ 'foo' ]",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n\nthis.foo = ['foo'];\n};\n\n\n/**\n * @protected\n * @type {Array}\n */\nFalconTest_A.prototype.foo;");
    }
    
    @Test
    public void testField_withTypeValueObjectLiteral()
    {
    	IClassNode node = (IClassNode) getNode("protected var foo:Object = { 'foo': 'bar' }",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n\nthis.foo = {'foo':'bar'};\n};\n\n\n/**\n * @protected\n * @type {Object}\n */\nFalconTest_A.prototype.foo;");
    }
    
    @Test
    public void testField_withTypeValueCustomNamespaceStaticMethodCall()
    {
    	IClassNode node = (IClassNode) getNode("import flash.utils.flash_proxy;use namespace flash_proxy;public static var foo:Object = initFoo(); flash_proxy static function initFoo():Object { return null; }",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * @expose\n * @type {Object}\n */\nFalconTest_A.foo = FalconTest_A[\"http://www.adobe.com/2006/actionscript/flash/proxy::initFoo\"]();\n\n\n/**\n * @expose\n * @return {Object}\n */\nFalconTest_A[\"http://www.adobe.com/2006/actionscript/flash/proxy::initFoo\"] = function() {\n  return null;\n};");
    }
    
    @Test
    public void testStaticField_withTypeValueObjectLiteral()
    {
    	IClassNode node = (IClassNode) getNode("static public var foo:Object = { 'foo': 'bar' }",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * @expose\n * @type {Object}\n */\nFalconTest_A.foo = {'foo':'bar'};");
    }
    
    @Test
    public void testField_withTypeValueFunctionCall()
    {
    	IClassNode node = (IClassNode) getNode("protected var foo:Number = parseFloat('1E2')",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n\nthis.foo = parseFloat('1E2');\n};\n\n\n/**\n * @protected\n * @type {number}\n */\nFalconTest_A.prototype.foo;");
    }
    
    @Test
    public void testStaticField_withFunctionInitializer()
    {
    	IClassNode node = (IClassNode) getNode("private static var empty:Function = function():void {}",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * @private\n * @type {Function}\n */\nFalconTest_A.empty = function() {\n};");
    }
    
    @Override
    @Test
    public void testField_withList()
    {
        IVariableNode node = getField("protected var a:int = 4, b:int = 11, c:int = 42;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @protected\n * @type {number}\n */\nFalconTest_A.prototype.a = 4;\n\n/**\n * @protected\n * @type {number}\n */\nFalconTest_A.prototype.b = 11;\n\n/**\n * @protected\n * @type {number}\n */\nFalconTest_A.prototype.c = 42");
    }

    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    @Override
    @Test
    public void testConstant()
    {
        IVariableNode node = getField("static const foo;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @expose\n * @const\n * @type {*}\n */\nFalconTest_A.foo");
    }

    @Test
    public void testConstant_nonStatic()
    {
        IVariableNode node = getField("const foo;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @export\n * @const\n * @type {*}\n */\nFalconTest_A.prototype.foo");
    }

    @Override
    @Test
    public void testConstant_withType()
    {
        IVariableNode node = getField("static const foo:int;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @expose\n * @const\n * @type {number}\n */\nFalconTest_A.foo = 0");
    }

    @Test
    public void testConstant_withType_nonStatic()
    {
        IVariableNode node = getField("const foo:int;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @export\n * @const\n * @type {number}\n */\nFalconTest_A.prototype.foo = 0");
    }

    @Override
    @Test
    public void testConstant_withTypeValue()
    {
        IVariableNode node = getField("static const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @expose\n * @const\n * @type {number}\n */\nFalconTest_A.foo = 420");
    }

    @Test
    public void testConstant_withComplexTypeValue()
    {
        IVariableNode node = getField("static const foo:Number = parseFloat('1E2');");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @expose\n * @const\n * @type {number}\n */\nFalconTest_A.foo = parseFloat('1E2')");
    }

    @Test
    public void testConstant_withTypeValue_nonStatic()
    {
        IVariableNode node = getField("const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @export\n * @const\n * @type {number}\n */\nFalconTest_A.prototype.foo = 420");
    }

    @Test
    public void testConstant_withComplexTypeValue_nonStatic()
    {
    	IClassNode node = (IClassNode) getNode("protected const foo:Number = parseFloat('1E2');",
    			IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n\nthis.foo = parseFloat('1E2');\n};\n\n\n/**\n * @protected\n * @const\n * @type {number}\n */\nFalconTest_A.prototype.foo;");
    }
    
    @Test
    public void testConstant_withTypeValueArrayLiteral()
    {
    	IClassNode node = (IClassNode) getNode("protected const foo:Array = [ 'foo' ]",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n\nthis.foo = ['foo'];\n};\n\n\n/**\n * @protected\n * @const\n * @type {Array}\n */\nFalconTest_A.prototype.foo;");
    }
    
    @Test
    public void testConstant_withTypeValueObjectLiteral()
    {
    	IClassNode node = (IClassNode) getNode("protected const foo:Object = { 'foo': 'bar' }",
        		IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        assertOut("/**\n * @constructor\n */\nFalconTest_A = function() {\n\nthis.foo = {'foo':'bar'};\n};\n\n\n/**\n * @protected\n * @const\n * @type {Object}\n */\nFalconTest_A.prototype.foo;");
    }

    @Override
    @Test
    public void testConstant_withNamespaceTypeValue()
    {
        IVariableNode node = getField("private static const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @const\n * @type {number}\n */\nFalconTest_A.foo = 420");
    }

    @Test
    public void testConstant_withNamespaceTypeValue_nonStatic()
    {
        IVariableNode node = getField("private const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("/**\n * @private\n * @const\n * @type {number}\n */\nFalconTest_A.prototype.foo = 420");
    }

    @Override
    @Test
    public void testConstant_withCustomNamespaceTypeValue()
    {
        IVariableNode node = getField("mx_internal static const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        // (erikdebruin) we ignore custom namespaces completely (are there side effects I'm missing?)
        assertOut("/**\n * @expose\n * @const\n * @type {number}\n */\nFalconTest_A.foo = 420");
    }

    @Test
    public void testConstant_withCustomNamespaceTypeValue_nonStatic()
    {
        IVariableNode node = getField("mx_internal const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        // (erikdebruin) we ignore custom namespaces completely (are there side effects I'm missing?)
        assertOut("/**\n * @export\n * @const\n * @type {number}\n */\nFalconTest_A.prototype.foo = 420");
    }
}
