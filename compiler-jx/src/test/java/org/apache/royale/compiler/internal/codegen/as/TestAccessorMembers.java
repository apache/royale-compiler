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

package org.apache.royale.compiler.internal.codegen.as;

import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.junit.Test;

/**
 * This class tests the production of valid ActionScript3 code for Class
 * Accessor members.
 * 
 * @author Michael Schmalle
 */
public class TestAccessorMembers extends ASTestBase
{
    //--------------------------------------------------------------------------
    // Accessor
    //--------------------------------------------------------------------------

    @Test
    public void testGetAccessor()
    {
        IAccessorNode node = getAccessor("function get foo():int{return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("function get foo():int {\n\treturn -1;\n}");
    }

    @Test
    public void testGetAccessor_withNamespace()
    {
        IAccessorNode node = getAccessor("public function get foo():int{return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("public function get foo():int {\n\treturn -1;\n}");
    }

    @Test
    public void testGetAccessor_withNamespaceOverride()
    {
        IAccessorNode node = getAccessor("public override function get foo():int{return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("public override function get foo():int {\n\treturn -1;\n}");
    }

    @Test
    public void testGetAccessor_withStatic()
    {
        IAccessorNode node = getAccessor("public static function get foo():int{return -1;}");
        asBlockWalker.visitFunction(node);
        assertOut("public static function get foo():int {\n\treturn -1;\n}");
    }

    @Test
    public void testSetAccessor()
    {
        IAccessorNode node = getAccessor("function set foo(value:int):void{}");
        asBlockWalker.visitFunction(node);
        assertOut("function set foo(value:int):void {\n}");
    }

    @Test
    public void testSetAccessor_withNamespace()
    {
        IAccessorNode node = getAccessor("public function set foo(value:int):void{}");
        asBlockWalker.visitFunction(node);
        assertOut("public function set foo(value:int):void {\n}");
    }

    @Test
    public void testSetAccessor_withNamespaceOverride()
    {
        IAccessorNode node = getAccessor("public override function set foo(value:int):void{}");
        asBlockWalker.visitFunction(node);
        assertOut("public override function set foo(value:int):void {\n}");
    }

    @Test
    public void testSetAccessor_withStatic()
    {
        IAccessorNode node = getAccessor("public static function set foo(value:int):void{}");
        asBlockWalker.visitFunction(node);
        assertOut("public static function set foo(value:int):void {\n}");
    }
}
