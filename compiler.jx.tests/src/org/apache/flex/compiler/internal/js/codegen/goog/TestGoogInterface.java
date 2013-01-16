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
import org.apache.flex.compiler.internal.as.codegen.TestInterface;
import org.apache.flex.compiler.internal.js.driver.goog.GoogBackend;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This class tests the production of valid 'goog' JS code for Interface production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class TestGoogInterface extends TestInterface
{
    // TODO (erikdebruin/mschmalle) handle interfaces and accessors first ;-)
	
	@Ignore
	@Override
    @Test
    public void testSimple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA{}");
        visitor.visitInterface(node);
        assertOut("");
    }

	@Ignore
	@Override
    @Test
    public void testSimpleExtends()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends IB{}");
        visitor.visitInterface(node);
        assertOut("");
    }

	@Ignore
	@Override
    @Test
    public void testSimpleExtendsMultiple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends IB, IC, ID {}");
        visitor.visitInterface(node);
        assertOut("");
    }

	@Ignore
	@Override
    @Test
    public void testQualifiedExtendsMultiple()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA extends foo.bar.IB, baz.goo.IC, foo.ID {}");
        visitor.visitInterface(node);
        assertOut("");
    }

	@Ignore
	@Override
    @Test
    public void testAccessors()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function get foo1():Object;"
                + "function set foo1(value:Object):void;}");
        visitor.visitInterface(node);
        assertOut("");
    }

	@Ignore
	@Override
    @Test
    public void testMethods()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function foo1():Object;"
                + "function foo1(value:Object):void;}");
        visitor.visitInterface(node);
        assertOut("");
    }

	@Ignore
	@Override
    @Test
    public void testAccessorsMethods()
    {
        IInterfaceNode node = getInterfaceNode("public interface IA {"
                + "function get foo1():Object;"
                + "function set foo1(value:Object):void;"
                + "function baz1():Object;"
                + "function baz2(value:Object):void;}");
        visitor.visitInterface(node);
        assertOut("");
    }

    protected IBackend createBackend()
    {
        return new GoogBackend();
    }

}
