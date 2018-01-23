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
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.junit.Test;

/**
 * This class tests the production of valid ActionScript3 code for Interface
 * production.
 * 
 * @author Michael Schmalle
 */
public class TestInterface extends ASTestBase
{
    //--------------------------------------------------------------------------
    // Interface
    //--------------------------------------------------------------------------

    @Test
    public void testSimple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA{}");
        asBlockWalker.visitInterface(node);
        assertOut("public interface IA {\n}");
    }

    @Test
    public void testSimpleExtends()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends IB{}");
        asBlockWalker.visitInterface(node);
        assertOut("public interface IA extends IB {\n}");
    }

    @Test
    public void testSimpleExtendsMultiple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends IB, IC, ID {}");
        asBlockWalker.visitInterface(node);
        assertOut("public interface IA extends IB, IC, ID {\n}");
    }

    @Test
    public void testQualifiedExtendsMultiple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends foo.bar.IB, baz.goo.IC, foo.ID {}");
        asBlockWalker.visitInterface(node);
        assertOut("public interface IA extends foo.bar.IB, baz.goo.IC, foo.ID {\n}");
    }

    @Test
    public void testAccessors()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function get foo1():Object;"
                + "function set foo1(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("public interface IA {\n\tfunction get foo1():Object;\n\t"
                + "function set foo1(value:Object):void;\n}");
    }

    @Test
    public void testMethods()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function foo1():Object;"
                + "function foo1(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("public interface IA {\n\tfunction foo1():Object;\n\t"
                + "function foo1(value:Object):void;\n}");
    }

    @Test
    public void testAccessorsMethods()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function get foo1():Object;"
                + "function set foo1(value:Object):void;"
                + "function baz1():Object;"
                + "function baz2(value:Object):void;}");
        asBlockWalker.visitInterface(node);
        assertOut("public interface IA {\n\tfunction get foo1():Object;"
                + "\n\tfunction set foo1(value:Object):void;\n\tfunction baz1()"
                + ":Object;\n\tfunction baz2(value:Object):void;\n}");
    }
}
