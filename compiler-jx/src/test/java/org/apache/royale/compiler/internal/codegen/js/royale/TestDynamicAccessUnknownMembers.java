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

package org.apache.royale.compiler.internal.codegen.js.royale;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.test.ASTestBase;
import org.apache.royale.compiler.tree.as.ILiteralContainerNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.junit.Test;

public class TestDynamicAccessUnknownMembers extends ASTestBase
{
    @Override
    public void setUp()
    {
        backend = createBackend();
        project = new RoyaleJSProject(workspace, backend);
    	JSGoogConfiguration config = new JSGoogConfiguration();
    	try {
			config.setJsDynamicAccessUnknownMembers(null, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	project.config = config;
        super.setUp();
	}

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }

    @Test
	public void testVisitKnownMember()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "public class KnownMember { public function KnownMember() { this.knownMember = 4; } public var knownMember:Number; }", IMemberAccessExpressionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("this.knownMember");
    }

    @Test
	public void testVisitUnknownMember()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getNode(
                "public dynamic class KnownMember { public function UnknownMember() { this.unknownMember = 4; } public var knownMember:Number; }", IMemberAccessExpressionNode.class, WRAP_LEVEL_PACKAGE);
        asBlockWalker.visitMemberAccessExpression(node);
        assertOut("this[\"unknownMember\"]");
    }

    @Test
	public void testVisitObjectLiteral()
    {
        ILiteralContainerNode node = (ILiteralContainerNode) getNode(
                "var obj:Object = { one: 1, \"two\": 2 }", ILiteralContainerNode.class, WRAP_LEVEL_MEMBER);
        asBlockWalker.visitLiteral(node);
        assertOut("{\"one\":1, \"two\":2}");
    }
}