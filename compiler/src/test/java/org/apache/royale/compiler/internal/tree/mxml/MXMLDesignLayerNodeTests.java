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
import org.apache.royale.compiler.tree.mxml.IMXMLDesignLayerNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLDesignLayerNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLDesignLayerNodeTests extends MXMLInstanceNodeTests
{
	private IMXMLDesignLayerNode getMXMLDesignLayerNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNodeWithFlex(code);
		IMXMLDesignLayerNode node = (IMXMLDesignLayerNode)findFirstDescendantOfType(fileNode, IMXMLDesignLayerNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLDesignLayerID));
		assertThat("getName", node.getName(), is("DesignLayer"));
		return node;
	}
	
	@Test
	public void MXMLDesignLayerNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:DesignLayer/>"
		};
		IMXMLDesignLayerNode node = getMXMLDesignLayerNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getHoistedChildCount", node.getHoistedChildCount(), is(0));
		assertThat("skipCodeGeneration", node.skipCodeGeneration(), is(true));
	}
	
	@Test
	public void MXMLDesignLayerNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:DesignLayer></fx:DesignLayer>"
		};
		IMXMLDesignLayerNode node = getMXMLDesignLayerNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getHoistedChildCount", node.getHoistedChildCount(), is(0));
		assertThat("skipCodeGeneration", node.skipCodeGeneration(), is(true));
	}
	
	@Test
	public void MXMLDesignLayerNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:DesignLayer> \t\r\n</fx:DesignLayer>"
		};
		IMXMLDesignLayerNode node = getMXMLDesignLayerNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getHoistedChildCount", node.getHoistedChildCount(), is(0));
		assertThat("skipCodeGeneration", node.skipCodeGeneration(), is(true));
	}
	
	@Test
	public void MXMLDesignLayerNode_id_visible_alpha()
	{
		String[] code = new String[]
		{
			"<fx:DesignLayer id=' dl1 ' visible=' false ' alpha=' 0.5 '/>"
		};
		IMXMLDesignLayerNode node = getMXMLDesignLayerNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2)); // visible and alpha property nodes
		assertThat("getHoistedChildCount", node.getHoistedChildCount(), is(0));
		assertThat("skipCodeGeneration", node.skipCodeGeneration(), is(false));
		assertThat("getID", node.getID(), is("dl1"));
		assertThat("getChild(0).getName()", ((IMXMLPropertySpecifierNode)node.getChild(0)).getName(), is("visible"));
		assertThat("getChild(0).getName()", ((IMXMLPropertySpecifierNode)node.getChild(1)).getName(), is("alpha"));
	}
	
	@Test
	public void MXMLDesignLayerNode_two_children()
	{
		String[] code = new String[]
		{
			"<fx:DesignLayer>",
			"    <s:Group/>",
			"    <s:Group/>",
			"</fx:DesignLayer>"
		};
		IMXMLDesignLayerNode node = getMXMLDesignLayerNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("getHoistedChildCount", node.getHoistedChildCount(), is(2));
		assertThat("skipCodeGeneration", node.skipCodeGeneration(), is(true));
	}
}
