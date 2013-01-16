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

package org.apache.flex.compiler.internal.js.codegen.goog;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.codegen.TestAccessorMembers;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.junit.Ignore;
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
    // TODO (erikdebruin)
    //  1) do we have to compile with '--language_in=ECMASCRIPT5'?

    @Override
    @Test
    public void testGetAccessor()
    {
        /*
        Object.defineProperty(
            A.prototype, 
            'foo', 
            {get:function() {
                return -1;
            }, configurable:true}
        )
         */
    	// TODO (erikdebruin) add 'goog' type declaration
        IGetterNode node = (IGetterNode) getAccessor("function get foo():int{return -1;}");
        visitor.visitGetter(node);
        assertOut("Object.defineProperty(\n\tA.prototype, \n\t'foo', "
                + "\n\t{get:function() {\n\t\treturn -1;\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testGetAccessor_withNamespace()
    {
        IGetterNode node = (IGetterNode) getAccessor("public function get foo():int{return -1;}");
        visitor.visitGetter(node);
        assertOut("Object.defineProperty(\n\tA.prototype, \n\t'foo', "
                + "\n\t{get:function() {\n\t\treturn -1;\n\t}, configurable:true}\n)");
    }

    @Ignore
    @Override
    @Test
    public void testGetAccessor_withNamespaceOverride()
    {
        // TODO (erikdebruin) public override get
        IAccessorNode node = getAccessor("public override function get foo():int{return -1;}");
        visitor.visitFunction(node);
        assertOut("");
    }

    @Ignore
    @Override
    @Test
    public void testGetAccessor_withStatic()
    {
        // TODO (erikdebruin) public static get
        IAccessorNode node = getAccessor("public static function get foo():int{return -1;}");
        visitor.visitFunction(node);
        assertOut("");
    }

    @Override
    @Test
    public void testSetAccessor()
    {
        /*
        Object.defineProperty(
            A.prototype, 
            'foo', 
            {set:function(value) {
            }, configurable:true}
        )
         */
        ISetterNode node = (ISetterNode) getAccessor("function set foo(value:int):void{}");
        visitor.visitSetter(node);
        assertOut("Object.defineProperty(\n\tA.prototype, \n\t'foo', \n\t{set:function(value)"
                + " {\n\t}, configurable:true}\n)");
    }

    @Override
    @Test
    public void testSetAccessor_withNamespace()
    {
        ISetterNode node = (ISetterNode) getAccessor("public function set foo(value:int):void{}");
        visitor.visitSetter(node);
        assertOut("Object.defineProperty(\n\tA.prototype, \n\t'foo', \n\t{set:function(value)"
                + " {\n\t}, configurable:true}\n)");
    }

    @Ignore
    @Override
    @Test
    public void testSetAccessor_withNamespaceOverride()
    {
        // TODO (erikdebruin) public override set
        IAccessorNode node = getAccessor("public override function set foo(value:int):void{}");
        visitor.visitFunction(node);
        assertOut("");
    }

    @Ignore
    @Override
    @Test
    public void testSetAccessor_withStatic()
    {
        // TODO (erikdebruin) public static set
        IAccessorNode node = getAccessor("public static function set foo(value:int):void{}");
        visitor.visitFunction(node);
        assertOut("");
    }

    @Override
    protected IBackend createBackend()
    {
        return new GoogBackend();
    }
}
