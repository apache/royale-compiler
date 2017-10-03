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

import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.royale.compiler.tree.mxml.IMXMLIntNode;
import org.apache.royale.compiler.tree.mxml.IMXMLStringNode;
import org.apache.royale.compiler.tree.mxml.IMXMLVectorNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLVectorNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLVectorNodeTests extends MXMLInstanceNodeTests
{
	private IMXMLVectorNode getMXMLVectorNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLVectorNode node = (IMXMLVectorNode)findFirstDescendantOfType(fileNode, IMXMLVectorNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLVectorID));
		assertThat("getName", node.getName(), is("Vector"));
		return node;
	}
	
	@Test
	public void MXMLVectorNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Vector/>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLVectorNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:Vector></fx:Vector>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLVectorNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:Vector> \t\r\n</fx:Vector>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLVectorNode_no_type()
	{
		String[] code = new String[]
		{
			"<fx:Vector>",
		    "    <fx:Boolean>true</fx:Boolean>",
			"    <fx:int>123</fx:int>",
		    "    <fx:String>abc</fx:String>",
			"</fx:Vector>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getType", node.getType(), is((ITypeDefinition)null));
		assertThat("getFixed", node.getFixed(), is(false));
		assertThat("getChildCount", node.getChildCount(), is(3));
		assertThat("[0]", ((IMXMLBooleanNode)node.getChild(0)).getValue(), is(true));
		assertThat("[1]", ((IMXMLIntNode)node.getChild(1)).getValue(), is(123));
		assertThat("[2]", ((IMXMLStringNode)node.getChild(2)).getValue(), is("abc"));
	}
	
	@Test
	public void MXMLVectorNode_Object_type()
	{
		String[] code = new String[]
		{
			"<fx:Vector type='Object'>",
		    "    <fx:Boolean>true</fx:Boolean>",
			"    <fx:int>123</fx:int>",
		    "    <fx:String>abc</fx:String>",
			"</fx:Vector>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getType", node.getType().getQualifiedName(), is("Object"));
		assertThat("getFixed", node.getFixed(), is(false));
		assertThat("getChildCount", node.getChildCount(), is(3));
		assertThat("[0]", ((IMXMLBooleanNode)node.getChild(0)).getValue(), is(true));
		assertThat("[1]", ((IMXMLIntNode)node.getChild(1)).getValue(), is(123));
		assertThat("[2]", ((IMXMLStringNode)node.getChild(2)).getValue(), is("abc"));
	}
	
	@Test
	public void MXMLVectorNode_star_type()
	{
		String[] code = new String[]
		{
			"<fx:Vector type='*'>",
		    "    <fx:Boolean>true</fx:Boolean>",
			"    <fx:int>123</fx:int>",
		    "    <fx:String>abc</fx:String>",
			"</fx:Vector>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getType", node.getType().getQualifiedName(), is("*"));
		assertThat("getFixed", node.getFixed(), is(false));
		assertThat("getChildCount", node.getChildCount(), is(3));
		assertThat("[0]", ((IMXMLBooleanNode)node.getChild(0)).getValue(), is(true));
		assertThat("[1]", ((IMXMLIntNode)node.getChild(1)).getValue(), is(123));
		assertThat("[2]", ((IMXMLStringNode)node.getChild(2)).getValue(), is("abc"));
	}
	
	@Test
	public void MXMLVectorNode_int_type()
	{
		String[] code = new String[]
		{
			"<fx:Vector type='int'>",
		    "    <fx:int>1</fx:int>",
			"    <fx:int>2</fx:int>",
		    "    <fx:int>3</fx:int>",
			"</fx:Vector>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getType", node.getType().getQualifiedName(), is("int"));
		assertThat("getFixed", node.getFixed(), is(false));
		assertThat("getChildCount", node.getChildCount(), is(3));
		assertThat("[0]", ((IMXMLIntNode)node.getChild(0)).getValue(), is(1));
		assertThat("[1]", ((IMXMLIntNode)node.getChild(1)).getValue(), is(2));
		assertThat("[2]", ((IMXMLIntNode)node.getChild(2)).getValue(), is(3));
	}
	
	@Test
	public void MXMLVectorNode_nested_int_vectors()
	{
		String[] code = new String[]
		{
			"<fx:Vector type='int'>",
		    "    <fx:Vector type='int'>",
		    "        <fx:int>1</fx:int>",
		    "        <fx:int>2</fx:int>",
		    "    </fx:Vector>",
		    "    <fx:Vector type='int'>",
		    "        <fx:int>3</fx:int>",
		    "        <fx:int>4</fx:int>",
		    "    </fx:Vector>",
			"</fx:Vector>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("[0].getChildCount", ((IMXMLVectorNode)node.getChild(0)).getChildCount(), is(2));
		assertThat("[0][0]", ((IMXMLIntNode)node.getChild(0).getChild(0)).getValue(), is(1));
		assertThat("[0][1]", ((IMXMLIntNode)node.getChild(0).getChild(1)).getValue(), is(2));
		assertThat("[1].getChildCount", ((IMXMLVectorNode)node.getChild(1)).getChildCount(), is(2));
		assertThat("[1][0]", ((IMXMLIntNode)node.getChild(1).getChild(0)).getValue(), is(3));
		assertThat("[1][1]", ((IMXMLIntNode)node.getChild(1).getChild(1)).getValue(), is(4));
	}
	
	@Ignore
	@Test
	public void MXMLVectorNode_with_databinding()
	{
		String[] code = new String[]
		{
			"<fx:Vector>{a.b}</fx:Vector>"
		};
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("databinding node", node.getChild(0).getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		assertThat("databinding node child count", node.getChild(0).getChildCount(), is(1));
		assertThat("identifier node", node.getChild(0).getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
