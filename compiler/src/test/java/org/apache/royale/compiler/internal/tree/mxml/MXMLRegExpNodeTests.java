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

package org.apache.royale.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRegExpNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLRegExpNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLRegExpNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLRegExpNode getMXMLRegExpNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLRegExpNode node = (IMXMLRegExpNode)findFirstDescendantOfType(fileNode, IMXMLRegExpNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLRegExpID));
		assertThat("getName", node.getName(), is("RegExp"));
		return node;
	}
	
	@Test
	public void MXMLRegExpNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:RegExp/>"
		};
		IMXMLRegExpNode node = getMXMLRegExpNode(code);
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLRegExpNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:RegExp></fx:RegExp>"
		};
		IMXMLRegExpNode node = getMXMLRegExpNode(code);
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLRegExpNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:RegExp> \t\r\n</fx:RegExp>"
		};
		IMXMLRegExpNode node = getMXMLRegExpNode(code);
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Ignore
	@Test
	public void MXMLRegExpNode_with_databinding()
	{
		String[] code = new String[]
		{
			"<fx:RegExp>{a.b}</fx:RegExp>"
		};
		IMXMLRegExpNode node = getMXMLRegExpNode(code);
		assertThat("databinding node", node.getExpressionNode().getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		testExpressionLocation(node, 11, 16);
		assertThat("databinding node child count", node.getExpressionNode().getChildCount(), is(1));
		assertThat("identifier node", node.getExpressionNode().getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
