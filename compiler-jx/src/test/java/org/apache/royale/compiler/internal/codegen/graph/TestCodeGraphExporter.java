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

package org.apache.royale.compiler.internal.codegen.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.junit.Test;

public class TestCodeGraphExporter extends ASTestBase
{
    @Test
    public void testClassAndPublicFieldAreCollectedSemantically()
    {
        IClassNode classNode = getClassNode("public class Widget {"
                + "public var label:String;"
                + "private var hidden:Number;"
                + "}");

        CodeGraphModel model = new CodeGraphExporter(project).export(
                Collections.singleton(classNode.getDefinition()), "js", null);

        assertEquals(1, model.getSymbols().size());
        CodeGraphSymbol classSymbol = model.getSymbols().get(0);
        assertEquals("as3://Widget", classSymbol.getId());
        assertEquals("class", classSymbol.getKind());
        assertNotNull(classSymbol.getBaseType());
        assertEquals("Object", classSymbol.getBaseType().getQualifiedName());
        assertTrue(classSymbol.getBaseType().isExternal());
        assertEquals(1, classSymbol.getMembers().size());

        CodeGraphSymbol fieldSymbol = classSymbol.getMembers().get(0);
        assertEquals("as3://Widget#label", fieldSymbol.getId());
        assertEquals("field", fieldSymbol.getKind());
        assertEquals("Widget", fieldSymbol.getDeclaringType().getQualifiedName());
        assertEquals("String", fieldSymbol.getType().getQualifiedName());
        assertTrue(fieldSymbol.getType().isExternal());
        assertEquals(2, model.getExternalSymbols().size());
        assertTrue(model.getExternalSymbols().get(0).isExternal());
        assertTrue(model.getExternalSymbols().get(1).isExternal());
    }

    @Test
    public void testCallableMembersAreCollectedSemantically()
    {
        IClassNode classNode = getClassNode("public class Widget {"
                + "public function Widget(value:String) {}"
                + "public function get label():String { return null; }"
                + "public function set label(value:String):void {}"
                + "public function work(required:String, optional:Number = 2, ...rest):Boolean { return true; }"
                + "}");

        CodeGraphModel model = new CodeGraphExporter(project).export(
                Collections.singleton(classNode.getDefinition()), "js", null);
        CodeGraphSymbol classSymbol = model.getSymbols().get(0);
        assertEquals(4, classSymbol.getMembers().size());

        CodeGraphSymbol constructor = findMember(classSymbol, "constructor");
        assertEquals("as3://Widget#constructor(String)", constructor.getId());
        assertEquals("String", constructor.getParameters().get(0).getType().getQualifiedName());

        CodeGraphSymbol getter = findMember(classSymbol, "getter");
        CodeGraphSymbol setter = findMember(classSymbol, "setter");
        assertEquals("as3://Widget#label:get", getter.getId());
        assertEquals("as3://Widget#label:set", setter.getId());
        assertEquals("String", getter.getType().getQualifiedName());
        assertEquals("String", setter.getType().getQualifiedName());

        CodeGraphSymbol method = findMember(classSymbol, "method");
        assertEquals("as3://Widget#work(String,Number,Array)", method.getId());
        assertEquals("Boolean", method.getReturnType().getQualifiedName());
        assertEquals(3, method.getParameters().size());
        assertFalse(method.getParameters().get(0).isOptional());
        assertTrue(method.getParameters().get(1).isOptional());
        assertEquals(2, method.getParameters().get(1).getDefaultValue());
        assertTrue(method.getParameters().get(2).isRest());
    }

    private CodeGraphSymbol findMember(CodeGraphSymbol owner, String kind)
    {
        for (CodeGraphSymbol member : owner.getMembers())
        {
            if (kind.equals(member.getKind()))
                return member;
        }
        throw new AssertionError("Expected " + kind + " member in " + owner.getId());
    }
}