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
                "public function A() {}; public function doStuff():void {label = 'hello, bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value}; ",
                IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nA = function() {\n};\n\nA.prototype.doStuff = function() {\n\tvar self = this;\n\tset_label('hello, bye');\n\tvar /** @type {string} */ theLabel = get_label();\n};\n\n/**\n * @private\n * @type {string}\n */\nA.prototype._label;\n\nA.prototype.get_label = function() {\n\tvar self = this;\n\treturn _label;\n};\n\nA.prototype.set_label = function(value) {\n\tvar self = this;\n\t_label = value;\n};";
        assertOut(expected);
    }

    @Test
    public void testSetAccessorWithMemberAccessOnLeftSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public class B { public function B() {}; public function doStuff():void {this.label = label + 'bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value};}",
                IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nB = function() {\n};\n\n/**\n * @this {B}\n */\nB.prototype.doStuff = function() {\n\tvar self = this;\n\tself.set_label(self.get_label() + 'bye');\n\tvar /** @type {string} */ theLabel = self.get_label();\n};\n\n/**\n * @private\n * @type {string}\n */\nB.prototype._label;\n\nB.prototype.get_label = function() {\n\tvar self = this;\n\treturn self._label;\n};\n\nB.prototype.set_label = function(value) {\n\tvar self = this;\n\tself._label = value;\n};"; 
        assertOut(expected);
    }

    @Test
    public void testSetAccessorWithCompoundRightSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public function A() {}; public function doStuff():void {label = label + 'bye'; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value}; ",
                IClassNode.class, WRAP_LEVEL_CLASS);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nA = function() {\n};\n\nA.prototype.doStuff = function() {\n\tvar self = this;\n\tset_label(get_label() + 'bye');\n\tvar /** @type {string} */ theLabel = get_label();\n};\n\n/**\n * @private\n * @type {string}\n */\nA.prototype._label;\n\nA.prototype.get_label = function() {\n\tvar self = this;\n\treturn _label;\n};\n\nA.prototype.set_label = function(value) {\n\tvar self = this;\n\t_label = value;\n};";
        assertOut(expected);
    }
    
    @Test
    public void testSetAccessorWithMemberAccessOnRightSide()
    {
        IClassNode node = (IClassNode) getNode(
                "public class B { public function B() {}; public function doStuff():void {label = this.label; var theLabel:String = label;}; private var _label:String; public function get label():String {return _label}; public function set label(value:String):void {_label = value};}",
                IClassNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitClass(node);
        String expected = "/**\n * @constructor\n */\nB = function() {\n};\n\n/**\n * @this {B}\n */\nB.prototype.doStuff = function() {\n\tvar self = this;\n\tself.set_label(self.get_label());\n\tvar /** @type {string} */ theLabel = self.get_label();\n};\n\n/**\n * @private\n * @type {string}\n */\nB.prototype._label;\n\nB.prototype.get_label = function() {\n\tvar self = this;\n\treturn self._label;\n};\n\nB.prototype.set_label = function(value) {\n\tvar self = this;\n\tself._label = value;\n};";
        assertOut(expected);
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
