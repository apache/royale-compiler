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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogInterface;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.junit.Test;

/**
 * This class tests the production of valid 'goog' JS code for Interface
 * production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestFlexJSInterface extends TestGoogInterface
{

    @Override
    @Test
    public void testAccessors()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function get foo1():Object;"
                + "function set foo1(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n */\nIA = function() {\n};\n\n\n/**\n * @return {Object}\n */\nIA.prototype.get_foo1 = function() {};\n\n\n/**\n * @param {Object} value\n */\nIA.prototype.set_foo1 = function(value) {};");
    }

    @Override
    @Test
    public void testMethods()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function baz1():Object;"
                + "function baz2(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n */\nIA = function() {\n};\n\n\n/**\n * @return {Object}\n */\nIA.prototype.baz1 = function() {};\n\n\n/**\n * @param {Object} value\n */\nIA.prototype.baz2 = function(value) {};");
    }

    @Override
    @Test
    public void testAccessorsMethods()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function get foo1():Object;"
                + "function set foo1(value:Object):void;"
                + "function baz1():Object;"
                + "function baz2(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("/**\n * @interface\n */\nIA = function() {\n};\n\n\n/**\n * @return {Object}\n */\nIA.prototype.get_foo1 = function() {};\n\n\n/**\n * @param {Object} value\n */\nIA.prototype.set_foo1 = function(value) {};\n\n\n/**\n * @return {Object}\n */\nIA.prototype.baz1 = function() {};\n\n\n/**\n * @param {Object} value\n */\nIA.prototype.baz2 = function(value) {};");
    }

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }

}
