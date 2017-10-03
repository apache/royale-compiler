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
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelPropertyContainerNode;
import org.apache.royale.compiler.tree.mxml.IMXMLModelRootNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLModelNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLModelNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLModelNode getMXMLModelNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLModelNode node = (IMXMLModelNode)findFirstDescendantOfType(fileNode, IMXMLModelNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLModelID));
		assertThat("getName", node.getName(), is("Model"));
		return node;
	}
	
	@Test
	public void MXMLModelNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Model/>"
		};
		IMXMLModelNode node = getMXMLModelNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getRootNode", node.getRootNode(), is((IMXMLModelRootNode)null));
	}
	
	@Test
	public void MXMLModelNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:Model></fx:Model>"
		};
		IMXMLModelNode node = getMXMLModelNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getRootNode", node.getRootNode(), is((IMXMLModelRootNode)null));
	}

	@Test
	public void MXMLModelNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:Model> \t\r\n</fx:Model>"
		};
		IMXMLModelNode node = getMXMLModelNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getRootNode", node.getRootNode(), is((IMXMLModelRootNode)null));
	}
	
	@Test
	public void MXMLModelNode_emptyRoot()
	{
		String[] code = new String[]
		{
			"<fx:Model>",
		    "    <root/>",
			"</fx:Model>"
		};
		IMXMLModelNode node = getMXMLModelNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		IMXMLModelRootNode rootNode = node.getRootNode();
		assertThat("index", rootNode.getIndex(), is(IMXMLModelPropertyContainerNode.NO_INDEX));
		assertThat("property nodes", rootNode.getPropertyNodes().length, is(0));
	}
	
	@Test
	public void MXMLModelNode_oneTag()
	{
		String[] code = new String[]
		{
			"<fx:Model>",
		    "    <root>",
		    "        <a/>",
		    "    </root>",
			"</fx:Model>"
		};
		IMXMLModelNode node = getMXMLModelNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		IMXMLModelRootNode rootNode = node.getRootNode();
		assertThat("index", rootNode.getIndex(), is(IMXMLModelPropertyContainerNode.NO_INDEX));
		assertThat("property nodes", rootNode.getPropertyNodes().length, is(1));
		assertThat("a count", rootNode.getPropertyNodes("a").length, is(1));
	}
	
	@Test
	public void MXMLModelNode_fourTags()
	{
		String[] code = new String[]
		{
			"<fx:Model>",
		    "    <root>",
		    "        <a/>",
		    "        <b/>",
		    "        <a/>",
		    "        <b/>",
		    "    </root>",
			"</fx:Model>"
		};
		IMXMLModelNode node = getMXMLModelNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		IMXMLModelRootNode rootNode = node.getRootNode();
		assertThat("index", rootNode.getIndex(), is(IMXMLModelPropertyContainerNode.NO_INDEX));
		assertThat("property nodes", rootNode.getPropertyNodes().length, is(4));
		assertThat("a count", rootNode.getPropertyNodes("a").length, is(2));
		assertThat("b count", rootNode.getPropertyNodes("a").length, is(2));
	}
}
