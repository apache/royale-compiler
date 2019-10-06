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

package org.apache.royale.compiler.internal.codegen.js.goog;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.as.TestAccessorMembers;
import org.apache.royale.compiler.internal.driver.js.goog.GoogBackend;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.junit.Test;

/**
 * This class tests the production of valid 'goog' JS code for Class Accessor
 * members.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestGoogAccessorMembers extends TestAccessorMembers
{
    @Override
    @Test
    public void testGetAccessor()
    {
        IGetterNode node = (IGetterNode) getAccessor("function get foo():int{}");
        asBlockWalker.visitGetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A.prototype, \n\t'foo', "
                + "\n\t{get:function() {\n\t}, configurable:true}\n)");
    }

    @Test
    public void testGetAccessor_withBody()
    {
        IGetterNode node = (IGetterNode) getAccessor("function get foo():int{return -1;}");
        asBlockWalker.visitGetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A.prototype, \n\t'foo', "
                + "\n\t{get:function() {\n\t\tvar self = this;\n\t\treturn -1;\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespace()
    {
        IGetterNode node = (IGetterNode) getAccessor("public function get foo():int{return -1;}");
        asBlockWalker.visitGetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A.prototype, \n\t'foo', "
                + "\n\t{get:function() {\n\t\tvar self = this;\n\t\treturn -1;\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespaceOverride()
    {
        // TODO (erikdebruin) need to figure out how to handle calls to 
        //                    'super' since the JS getter is actually an 
        //                    anonymous function... goog.bind or goog.partial?
        IGetterNode node = (IGetterNode) getAccessor("public override function get foo():int{super.foo(); return -1;}");
        asBlockWalker.visitGetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A.prototype, \n\t'foo', \n\t{get:function() {\n\t\tvar self = this;\n\t\tRoyaleTest_A.base(this, 'foo');\n\t\treturn -1;\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testGetAccessor_withStatic()
    {
        IGetterNode node = (IGetterNode) getAccessor("public static function get foo():int{return -1;}");
        asBlockWalker.visitGetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A, \n\t'foo', \n\t{get:function() {\n\t\treturn -1;\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testSetAccessor()
    {
        ISetterNode node = (ISetterNode) getAccessor("function set foo(value:int):void{}");
        asBlockWalker.visitSetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A.prototype, \n\t'foo', \n\t{set:function(value)"
                + " {\n\t}, configurable:true}\n)");
    }

    @Test
    public void testSetAccessor_withBody()
    {
        ISetterNode node = (ISetterNode) getAccessor("function set foo(value:int):void{'haai';}");
        asBlockWalker.visitSetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A.prototype, \n\t'foo', "
                + "\n\t{set:function(value) {\n\t\tvar self = this;\n\t\t'haai';\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespace()
    {
        ISetterNode node = (ISetterNode) getAccessor("public function set foo(value:int):void{}");
        asBlockWalker.visitSetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A.prototype, \n\t'foo', \n\t{set:function(value)"
                + " {\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespaceOverride()
    {
        // TODO (erikdebruin) see: testGetAccessor_withNamespaceOverride
        ISetterNode node = (ISetterNode) getAccessor("public override function set foo(value:int):void{super.foo();}");
        asBlockWalker.visitSetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A.prototype, \n\t'foo', \n\t{set:function(value) {\n\t\tvar self = this;\n\t\tRoyaleTest_A.base(this, 'foo');\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testSetAccessor_withStatic()
    {
        ISetterNode node = (ISetterNode) getAccessor("public static function set foo(value:int):void{}");
        asBlockWalker.visitSetter(node);
        assertOut("Object.defineProperty(\n\tRoyaleTest_A, \n\t'foo', \n\t{set:function(value) {\n\t}, configurable:true}\n)");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
