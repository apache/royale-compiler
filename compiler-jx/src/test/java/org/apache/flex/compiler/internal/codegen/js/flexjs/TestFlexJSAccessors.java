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
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.test.ASTestBase;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSAccessors extends ASTestBase
{
    
    @Test
    public void testGetAndSetAccessor()
    {
        IClassNode node = (IClassNode) getNode(
                "public function doStuff():void {label = 'hello, bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value}; ",
                IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * @export\n */\nFalconTest_A.prototype.doStuff = function() {\n  this.label = 'hello, bye';\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nFalconTest_A.prototype._label;\n\n\nObject.defineProperties(FalconTest_A.prototype, /** @lends {FalconTest_A.prototype} */ {\n/** @export */\nlabel: {\nget: /** @this {FalconTest_A} */ function() {\n  return this._label;\n},\nset: /** @this {FalconTest_A} */ function(value) {\n  this._label = value;\n}}}\n);";
        assertOut(expected);
    }

    @Test
    public void testSetAccessorWithMemberAccessOnLeftSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public class B { public function B() {}; public function doStuff():void {this.label = label + 'bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value};}",
                IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nB = function() {\n};\n\n\n/**\n * @export\n */\nB.prototype.doStuff = function() {\n  this.label = this.label + 'bye';\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nB.prototype._label;\n\n\nObject.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/** @export */\nlabel: {\nget: /** @this {B} */ function() {\n  return this._label;\n},\nset: /** @this {B} */ function(value) {\n  this._label = value;\n}}}\n);"; 
        assertOut(expected);
    }

    @Test
    public void testSetAccessorWithCompoundRightSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public function doStuff():void {label = label + 'bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value}; ",
                IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nFalconTest_A = function() {\n};\n\n\n/**\n * @export\n */\nFalconTest_A.prototype.doStuff = function() {\n  this.label = this.label + 'bye';\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nFalconTest_A.prototype._label;\n\n\nObject.defineProperties(FalconTest_A.prototype, /** @lends {FalconTest_A.prototype} */ {\n/** @export */\nlabel: {\nget: /** @this {FalconTest_A} */ function() {\n  return this._label;\n},\nset: /** @this {FalconTest_A} */ function(value) {\n  this._label = value;\n}}}\n);";
        assertOut(expected);
    }
    
    @Test
    public void testSetAccessorWithMemberAccessOnRightSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public class B { public function B() {}; public function doStuff():void {label = this.label; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value};}",
                IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nB = function() {\n};\n\n\n/**\n * @export\n */\nB.prototype.doStuff = function() {\n  this.label = this.label;\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nB.prototype._label;\n\n\nObject.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/** @export */\nlabel: {\nget: /** @this {B} */ function() {\n  return this._label;\n},\nset: /** @this {B} */ function(value) {\n  this._label = value;\n}}}\n);";
        assertOut(expected);
    }

    @Test
    public void testGetSetCustomNamespaceAccessor()
    {
        IClassNode node = (IClassNode) getNode(
                "import flash.utils.flash_proxy;use namespace flash_proxy;public class B { public function B() {}; public function doStuff():void {var theLabel:String = label; label = theLabel;}; private var _label:String; flash_proxy function get label():String {return _label}; flash_proxy function set label(value:String):void {_label = value};}",
                IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nB = function() {\n};\n\n\n/**\n * @export\n */\nB.prototype.doStuff = function() {\n  var /** @type {string} */ theLabel = this[\"http://www.adobe.com/2006/actionscript/flash/proxy::label\"];\n  this[\"http://www.adobe.com/2006/actionscript/flash/proxy::label\"] = theLabel;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nB.prototype._label;\n\n\nObject.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/** @export */\n\"http://www.adobe.com/2006/actionscript/flash/proxy::label\": {\nget: /** @this {B} */ function() {\n  return this._label;\n},\nset: /** @this {B} */ function(value) {\n  this._label = value;\n}}}\n);";
        assertOut(expected);
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
