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

package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLBindingAttributeNode;
import org.apache.flex.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLBindingNode} and {@link MXMLBindingAttributeNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLBindingNodeTests extends MXMLNodeBaseTests
{
	private IMXMLBindingNode getMXMLBindingNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLBindingNode node = (IMXMLBindingNode)findFirstDescendantOfType(fileNode, IMXMLBindingNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLBindingID));
		assertThat("getName", node.getName(), is("Binding"));
		return node;
	}
	
	@Test
	public void MXMLBindingNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Binding/>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getDestinationAttributeNode", node.getDestinationAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getTwoWay", node.getTwoWay(), is(false));
	}
	
	@Test
	public void MXMLBindingNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:Binding></fx:Binding>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getDestinationAttributeNode", node.getDestinationAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getTwoWay", node.getTwoWay(), is(false));
	}
	
	@Test
	public void MXMLBindingNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:Binding> \t\r\n</fx:Binding>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getDestinationAttributeNode", node.getDestinationAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getTwoWay", node.getTwoWay(), is(false));
	}
	
	@Test
	public void MXMLBindingNode_source()
	{
		String[] code = new String[]
		{
			"<fx:Binding source=' a.b '/>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)node.getChild(0)));
		assertThat("getSourceAttributeNode.getExpressionNode.getNodeID", node.getSourceAttributeNode().getExpressionNode().getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
		assertThat("getDestinationAttributeNode", node.getDestinationAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getTwoWay", node.getTwoWay(), is(false));
	}
	
	@Test
	public void MXMLBindingNode_destination()
	{
		String[] code = new String[]
		{
			"<fx:Binding destination=' c.d '/>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getDestinationAttributeNode", node.getDestinationAttributeNode(), is((IMXMLBindingAttributeNode)node.getChild(0)));
		assertThat("getDestinationAttributeNode.getExpressionNode.getNodeID", node.getDestinationAttributeNode().getExpressionNode().getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
		assertThat("getTwoWay", node.getTwoWay(), is(false));
	}
	
	@Test
	public void MXMLBindingNode_twoWayFalse()
	{
		String[] code = new String[]
		{
			"<fx:Binding twoWay=' false '/>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getDestinationAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getTwoWay", node.getTwoWay(), is(false));
	}
	
	@Test
	public void MXMLBindingNode_twoWayTrue()
	{
		String[] code = new String[]
		{
			"<fx:Binding twoWay=' true '/>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getDestinationAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)null));
		assertThat("getTwoWay", node.getTwoWay(), is(true));
	}
	
	@Test
	public void MXMLBindingNode_source_destination_twoWayTrue()
	{
		String[] code = new String[]
		{
			"<fx:Binding source=' a.b ' destination=' c.d ' twoWay=' true '/>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)node.getChild(0)));
		assertThat("getSourceAttributeNode.getExpressionNode.getNodeID", node.getSourceAttributeNode().getExpressionNode().getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
		assertThat("getDestinationAttributeNode", node.getDestinationAttributeNode(), is((IMXMLBindingAttributeNode)node.getChild(1)));
		assertThat("getDestinationAttributeNode.getExpressionNode.getNodeID", node.getDestinationAttributeNode().getExpressionNode().getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
		assertThat("getTwoWay", node.getTwoWay(), is(true));
	}
	
	@Test
	public void MXMLBindingNode_destination_twoWayFalse_source()
	{
		String[] code = new String[]
		{
			"<fx:Binding destination=' c.d ' twoWay=' false ' source=' a.b '/>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("getSourceAttributeNode", node.getSourceAttributeNode(), is((IMXMLBindingAttributeNode)node.getChild(1)));
		assertThat("getSourceAttributeNode.getExpressionNode.getNodeID", node.getSourceAttributeNode().getExpressionNode().getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
		assertThat("getDestinationAttributeNode", node.getDestinationAttributeNode(), is((IMXMLBindingAttributeNode)node.getChild(0)));
		assertThat("getDestinationAttributeNode.getExpressionNode.getNodeID", node.getDestinationAttributeNode().getExpressionNode().getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
		assertThat("getTwoWay", node.getTwoWay(), is(false));
	}
	
	@Ignore
	@Test
	public void MXMLBindingNode_text()
	{
		String[] code = new String[]
		{
			"<fx:Binding> a.b </fx:Binding>"
		};
		IMXMLBindingNode node = getMXMLBindingNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
}
