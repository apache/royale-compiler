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
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Test;

/**
 * This class tests the production of valid ActionScript3 code for Class Field
 * members.
 * 
 * @author Michael Schmalle
 */
public class TestFieldMembers extends ASTestBase
{
    /*
     * Field, Constant, [Namespace]
     * 
     * var foo;
     * var foo:int;
     * var foo:int = 42;
     * private var foo:int;
     * private var foo:int = 42;
     * protected var foo:int;
     * public var foo:int;
     */

    //--------------------------------------------------------------------------
    // Field
    //--------------------------------------------------------------------------

    @Test
    public void testField()
    {
        IVariableNode node = getField("var foo;");
        asBlockWalker.visitVariable(node);
        assertOut("var foo:*");
    }

    @Test
    public void testField_withType()
    {
        IVariableNode node = getField("var foo:int;");
        asBlockWalker.visitVariable(node);
        assertOut("var foo:int");
    }

    @Test
    public void testField_withTypeValue()
    {
        IVariableNode node = getField("var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("var foo:int = 420");
    }

    @Test
    public void testField_withNamespaceTypeValue()
    {
        IVariableNode node = getField("private var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("private var foo:int = 420");
    }

    @Test
    public void testField_withCustomNamespaceTypeValue()
    {
        IVariableNode node = getField("mx_internal var foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("mx_internal var foo:int = 420");
    }

    @Test
    public void testField_withNamespaceTypeCollection()
    {
        IVariableNode node = getField("protected var foo:Vector.<Foo>;");
        asBlockWalker.visitVariable(node);
        assertOut("protected var foo:Vector.<Foo>");
    }

    @Test
    public void testField_withNamespaceTypeCollectionComplex()
    {
        IVariableNode node = getField("protected var foo:Vector.<Vector.<Vector.<Foo>>>;");
        asBlockWalker.visitVariable(node);
        assertOut("protected var foo:Vector.<Vector.<Vector.<Foo>>>");
    }

    @Test
    public void testField_withNamespaceTypeValueComplex()
    {
        IVariableNode node = getField("protected var foo:Foo = new Foo('bar', 42);");
        asBlockWalker.visitVariable(node);
        assertOut("protected var foo:Foo = new Foo('bar', 42)");
    }

    @Test
    public void testField_withList()
    {
        IVariableNode node = getField("protected var a:int = 4, b:int = 11, c:int = 42;");
        asBlockWalker.visitVariable(node);
        assertOut("protected var a:int = 4, b:int = 11, c:int = 42");
    }

    //--------------------------------------------------------------------------
    // Constant
    //--------------------------------------------------------------------------

    @Test
    public void testConstant()
    {
        IVariableNode node = getField("static const foo;");
        asBlockWalker.visitVariable(node);
        assertOut("static const foo:*");
    }

    @Test
    public void testConstant_withType()
    {
        IVariableNode node = getField("static const foo:int;");
        asBlockWalker.visitVariable(node);
        assertOut("static const foo:int");
    }

    @Test
    public void testConstant_withTypeValue()
    {
        IVariableNode node = getField("static const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("static const foo:int = 420");
    }

    @Test
    public void testConstant_withNamespaceTypeValue()
    {
        IVariableNode node = getField("private static const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("private static const foo:int = 420");
    }

    @Test
    public void testConstant_withCustomNamespaceTypeValue()
    {
        IVariableNode node = getField("mx_internal static const foo:int = 420;");
        asBlockWalker.visitVariable(node);
        assertOut("mx_internal static const foo:int = 420");
    }

    //--------------------------------------------------------------------------
    // Namespace
    //--------------------------------------------------------------------------

    @Test
    public void testNamespace()
    {
        INamespaceNode node = getNamespace("namespace ns = \"http://whatever\";");
        asBlockWalker.visitNamespace(node);
        assertOut("namespace ns = \"http://whatever\"");
    }

    @Test
    public void testNamespace_public()
    {
        INamespaceNode node = getNamespace("public namespace ns = \"http://whatever\";");
        asBlockWalker.visitNamespace(node);
        assertOut("public namespace ns = \"http://whatever\"");
    }

    @Test
    public void testNamespace_protected()
    {
        INamespaceNode node = getNamespace("protected namespace ns = \"http://whatever\";");
        asBlockWalker.visitNamespace(node);
        assertOut("protected namespace ns = \"http://whatever\"");
    }

    protected INamespaceNode getNamespace(String code)
    {
        String source = "package {public class A {" + code + "}}";
        IFileNode node = compileAS(source);
        INamespaceNode child = (INamespaceNode) findFirstDescendantOfType(node,
                INamespaceNode.class);
        return child;
    }
}
