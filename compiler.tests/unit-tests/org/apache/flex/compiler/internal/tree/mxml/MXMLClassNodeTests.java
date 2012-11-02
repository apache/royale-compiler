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

import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLClassNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLClassNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLClassNode getMXMLClassNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLClassNode node = (IMXMLClassNode)findFirstDescendantOfType(fileNode, IMXMLClassNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLClassID));
		assertThat("getName", node.getName(), is("Class"));
		return node;
	}
	
	@Test
	public void MXMLClassNode_empty1()
	{
		String code = "<fx:Class/>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("getValue", node.getValue(project), is((ITypeDefinition)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLClassNode_empty2()
	{
		String code = "<fx:Class></fx:Class>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("getValue", node.getValue(project), is((ITypeDefinition)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLClassNode_empty3()
	{
		String code = "<fx:Class> \t\r\n</fx:Class>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("getValue", node.getValue(project), is((ITypeDefinition)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLClassNode_flashDisplaySprite()
	{
		String code = "<fx:Class>flash.display.Sprite</fx:Class>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("getValue", node.getValue(project).getQualifiedName(), is("flash.display.Sprite"));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Ignore
	@Test
	public void MXMLClassNode_with_databinding()
	{
		String code = "<fx:Class>{a.b}</fx:Class>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("databinding node", node.getExpressionNode().getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		testExpressionLocation(node, 10, 15);
		assertThat("databinding node child count", node.getExpressionNode().getChildCount(), is(1));
		assertThat("identifier node", node.getExpressionNode().getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
