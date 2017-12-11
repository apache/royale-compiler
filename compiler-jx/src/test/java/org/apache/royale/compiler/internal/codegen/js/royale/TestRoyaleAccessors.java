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
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestRoyaleAccessors extends ASTestBase
{
    @Override
    public void setUp()
    {
        super.setUp();
    	((RoyaleJSProject)royaleJSProject).config = new JSGoogConfiguration();
    }
    
    @Test
    public void testGetAndSetAccessor()
    {
        IClassNode node = (IClassNode) getNode(
                "public function doStuff():void {label = 'hello, bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value}; ",
                IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('RoyaleTest_A', RoyaleTest_A);\n\n\n/**\n * @export\n */\nRoyaleTest_A.prototype.doStuff = function() {\n  this.label = 'hello, bye';\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nRoyaleTest_A.prototype._label;\n\n\n" +
        		"RoyaleTest_A.prototype.get__label = function() {\n  return this._label;\n};\n\n\n" +
        		"RoyaleTest_A.prototype.set__label = function(value) {\n  this._label = value;\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @export\n  * @type {string} */\n" +
        		"label: {\nget: RoyaleTest_A.prototype.get__label,\nset: RoyaleTest_A.prototype.set__label}}\n);";
        assertOut(expected);
    }

    @Test
    public void testSetAccessorWithMemberAccessOnLeftSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public class B { public function B() {}; public function doStuff():void {this.label = label + 'bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value};}",
                IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nB = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('B', B);\n\n\n/**\n * @export\n */\nB.prototype.doStuff = function() {\n  this.label = this.label + 'bye';\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nB.prototype._label;\n\n\n" +
				"B.prototype.get__label = function() {\n  return this._label;\n};\n\n\n" +
				"B.prototype.set__label = function(value) {\n  this._label = value;\n};\n\n\n" +
        		"Object.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/**\n  * @export\n  * @type {string} */\nlabel: {\n" +
        		"get: B.prototype.get__label,\nset: B.prototype.set__label}}\n);"; 
        assertOut(expected);
    }

    @Test
    public void testSetAccessorWithCompoundRightSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public function doStuff():void {label = label + 'bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value}; ",
                IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('RoyaleTest_A', RoyaleTest_A);\n\n\n/**\n * @export\n */\nRoyaleTest_A.prototype.doStuff = function() {\n  this.label = this.label + 'bye';\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nRoyaleTest_A.prototype._label;\n\n\n" +
				"RoyaleTest_A.prototype.get__label = function() {\n  return this._label;\n};\n\n\n" +
				"RoyaleTest_A.prototype.set__label = function(value) {\n  this._label = value;\n};\n\n\n" +
				"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @export\n  * @type {string} */\nlabel: {\n" +
				"get: RoyaleTest_A.prototype.get__label,\nset: RoyaleTest_A.prototype.set__label}}\n);"; 
        assertOut(expected);
    }
    
    @Test
    public void testSetAccessorWithMemberAccessOnRightSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public class B { public function B() {}; public function doStuff():void {label = this.label; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value};}",
                IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nB = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('B', B);\n\n\n/**\n * @export\n */\nB.prototype.doStuff = function() {\n  this.label = this.label;\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nB.prototype._label;\n\n\n" +
				"B.prototype.get__label = function() {\n  return this._label;\n};\n\n\n" +
				"B.prototype.set__label = function(value) {\n  this._label = value;\n};\n\n\n" +
				"Object.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/**\n  * @export\n  * @type {string} */\nlabel: {\n" +
				"get: B.prototype.get__label,\nset: B.prototype.set__label}}\n);"; 
        assertOut(expected);
    }

    @Test
    public void testGetSetCustomNamespaceAccessor()
    {
        IClassNode node = (IClassNode) getNode(
                "import custom.custom_namespace;use namespace custom_namespace;public class B { public function B() {}; public function doStuff():void {var theLabel:String = label; label = theLabel;}; private var _label:String; custom_namespace function get label():String {return _label}; custom_namespace function set label(value:String):void {_label = value};}",
                IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nB = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('B', B);\n\n\n/**\n * @export\n */\nB.prototype.doStuff = function() {\n  var /** @type {string} */ theLabel = this[\"http://ns.apache.org/2017/custom/namespace::label\"];\n  this[\"http://ns.apache.org/2017/custom/namespace::label\"] = theLabel;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nB.prototype._label;\n\n\n" +
				"B.prototype[\"http://ns.apache.org/2017/custom/namespace::get__label\"] = function() {\n  return this._label;\n};\n\n\n" +
				"B.prototype[\"http://ns.apache.org/2017/custom/namespace::set__label\"] = function(value) {\n  this._label = value;\n};\n\n\n" +
        		"Object.defineProperties(B.prototype, /** @lends {B.prototype} */ {\n/**\n  * @export\n  * @type {string} */\n\"http://ns.apache.org/2017/custom/namespace::label\": {\nget: B.prototype[\"http://ns.apache.org/2017/custom/namespace::get__label\"],\nset: B.prototype[\"http://ns.apache.org/2017/custom/namespace::set__label\"]}}\n);";
        assertOut(expected);
    }

    @Test
    public void testBindableGetAndSetAccessor()
    {
        IClassNode node = (IClassNode) getNode(
                "public function doStuff():void {label = 'hello, bye'; var theLabel:String = label;}; private var _label:String; [Bindable] public function get label():String {return _label}; public function set label(value:String):void {_label = value}; ",
                IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('RoyaleTest_A', RoyaleTest_A);\n\n\n/**\n * @export\n */\nRoyaleTest_A.prototype.doStuff = function() {\n  this.label = 'hello, bye';\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nRoyaleTest_A.prototype._label;\n\n\n" +
        		"RoyaleTest_A.prototype.get__label = function() {\n  return this._label;\n};\n\n\n" +
				"RoyaleTest_A.prototype.bindable__set__label = function(value) {\n  this._label = value;\n};\n\n\n" +
        		"RoyaleTest_A.prototype.set__label = function(value) {\nvar oldValue = this.get__label();\nif (oldValue != value) {\nthis.bindable__set__label(value);\n" +
        		"    this.dispatchEvent(org.apache.royale.events.ValueChangeEvent.createUpdateEvent(\n" +
        		"         this, \"label\", oldValue, value));\n}\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @export\n  * @type {string} */\n" +
        		"label: {\nget: RoyaleTest_A.prototype.get__label,\nset: RoyaleTest_A.prototype.set__label}}\n);";
        assertOut(expected);
    }

    @Test
    public void testBindableWithEventGetAndSetAccessor()
    {
        IClassNode node = (IClassNode) getNode(
                "public function doStuff():void {label = 'hello, bye'; var theLabel:String = label;}; private var _label:String; [Bindable(\"change\")] public function get label():String {return _label}; public function set label(value:String):void {_label = value}; ",
                IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nRoyaleTest_A = function() {\n};\n\n\n/**\n * Prevent renaming of class. Needed for reflection.\n */\ngoog.exportSymbol('RoyaleTest_A', RoyaleTest_A);\n\n\n/**\n * @export\n */\nRoyaleTest_A.prototype.doStuff = function() {\n  this.label = 'hello, bye';\n  var /** @type {string} */ theLabel = this.label;\n};\n\n\n/**\n * @private\n * @type {string}\n */\nRoyaleTest_A.prototype._label;\n\n\n" +
				"RoyaleTest_A.prototype.get__label = function() {\n  return this._label;\n};\n\n\n" +
				"RoyaleTest_A.prototype.set__label = function(value) {\n  this._label = value;\n};\n\n\n" +
        		"Object.defineProperties(RoyaleTest_A.prototype, /** @lends {RoyaleTest_A.prototype} */ {\n/**\n  * @export\n  * @type {string} */\n" +
        		"label: {\nget: RoyaleTest_A.prototype.get__label,\nset: RoyaleTest_A.prototype.set__label}}\n);";
        assertOut(expected);
    }

    @Override
    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

}
