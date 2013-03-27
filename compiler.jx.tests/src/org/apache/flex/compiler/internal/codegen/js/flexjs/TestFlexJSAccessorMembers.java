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
import org.apache.flex.compiler.internal.codegen.js.goog.TestGoogAccessorMembers;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.junit.Test;

/**
 * @author Erik de Bruin
 */
public class TestFlexJSAccessorMembers extends TestGoogAccessorMembers
{
    @Override
    @Test
    public void testGetAccessor()
    {
        IGetterNode node = (IGetterNode) getAccessor("function get foo():int{}");
        asBlockWalker.visitGetter(node);
        assertOut("A.prototype.get_foo = function() {\n}");
    }

    @Override
    @Test
    public void testGetAccessor_withBody()
    {
        IGetterNode node = (IGetterNode) getAccessor("function get foo():int{return -1;}");
        asBlockWalker.visitGetter(node);
        assertOut("A.prototype.get_foo = function() {\n\tvar self = this;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespace()
    {
        IGetterNode node = (IGetterNode) getAccessor("public function get foo():int{return -1;}");
        asBlockWalker.visitGetter(node);
        assertOut("A.prototype.get_foo = function() {\n\tvar self = this;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespaceOverride()
    {
        IGetterNode node = (IGetterNode) getAccessor("public override function get foo():int{super.foo(); return -1;}");
        asBlockWalker.visitGetter(node);
        assertOut("A.prototype.get_foo = function() {\n\tvar self = this;\n\tgoog.base(this, 'get_foo');\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testGetAccessor_withStatic()
    {
        IGetterNode node = (IGetterNode) getAccessor("public static function get foo():int{return -1;}");
        asBlockWalker.visitGetter(node);
        assertOut("A.get_foo = function() {\n\tvar self = this;\n\treturn -1;\n}");
    }

    @Override
    @Test
    public void testSetAccessor()
    {
        ISetterNode node = (ISetterNode) getAccessor("function set foo(value:int):void{}");
        asBlockWalker.visitSetter(node);
        assertOut("A.prototype.set_foo = function(value) {\n}");
    }

    @Override
    @Test
    public void testSetAccessor_withBody()
    {
        ISetterNode node = (ISetterNode) getAccessor("function set foo(value:int):void{fetch('haai');}");
        asBlockWalker.visitSetter(node);
        assertOut("A.prototype.set_foo = function(value) {\n\tvar self = this;\n\tfetch('haai');\n}");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespace()
    {
        ISetterNode node = (ISetterNode) getAccessor("public function set foo(value:int):void{}");
        asBlockWalker.visitSetter(node);
        assertOut("A.prototype.set_foo = function(value) {\n}");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespaceOverride()
    {
        ISetterNode node = (ISetterNode) getAccessor("public override function set foo(value:int):void{super.foo();}");
        asBlockWalker.visitSetter(node);
        assertOut("A.prototype.set_foo = function(value) {\n\tvar self = this;\n\tgoog.base(this, 'set_foo');\n}");
    }

    @Override
    @Test
    public void testSetAccessor_withStatic()
    {
        ISetterNode node = (ISetterNode) getAccessor("public static function set foo(value:int):void{}");
        asBlockWalker.visitSetter(node);
        assertOut("A.set_foo = function(value) {\n}");
    }

    @Override
    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
