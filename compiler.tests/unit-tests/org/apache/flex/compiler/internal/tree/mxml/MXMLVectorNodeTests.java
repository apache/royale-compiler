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
import org.apache.flex.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.apache.flex.compiler.tree.mxml.IMXMLVectorNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLVectorNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLVectorNodeTests extends MXMLInstanceNodeTests
{
	private static String EOL = "\n\t\t";
	
	private IMXMLVectorNode getMXMLVectorNode(String code)
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
		String code = "<fx:Vector/>";
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLVectorNode_empty2()
	{
		String code = "<fx:Vector></fx:Vector>";
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLVectorNode_empty3()
	{
		String code = "<fx:Vector> \t\r\n</fx:Vector>";
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLVectorNode_no_type()
	{
		String code =
			"<fx:Vector>" + EOL +
		    "    <fx:Boolean>true</fx:Boolean>" + EOL +
			"    <fx:int>123</fx:int>" + EOL +
		    "    <fx:String>abc</fx:String>" + EOL +
			"</fx:Vector>";
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
		String code =
			"<fx:Vector type='Object'>" + EOL +
		    "    <fx:Boolean>true</fx:Boolean>" + EOL +
			"    <fx:int>123</fx:int>" + EOL +
		    "    <fx:String>abc</fx:String>" + EOL +
			"</fx:Vector>";
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
		String code =
			"<fx:Vector type='*'>" + EOL +
		    "    <fx:Boolean>true</fx:Boolean>" + EOL +
			"    <fx:int>123</fx:int>" + EOL +
		    "    <fx:String>abc</fx:String>" + EOL +
			"</fx:Vector>";
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
		String code =
			"<fx:Vector type='int'>" + EOL +
		    "    <fx:int>1</fx:int>" + EOL +
			"    <fx:int>2</fx:int>" + EOL +
		    "    <fx:int>3</fx:int>" + EOL +
			"</fx:Vector>";
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
		String code =
			"<fx:Vector type='int'>" + EOL +
		    "    <fx:Vector type='int'>" + EOL +
		    "        <fx:int>1</fx:int>" + EOL +
		    "        <fx:int>2</fx:int>" + EOL +
		    "    </fx:Vector>" + EOL +
		    "    <fx:Vector type='int'>" + EOL +
		    "        <fx:int>3</fx:int>" + EOL +
		    "        <fx:int>4</fx:int>" + EOL +
		    "    </fx:Vector>" + EOL +
			"</fx:Vector>";
		IMXMLVectorNode node = getMXMLVectorNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("[0].getChildCount", ((IMXMLVectorNode)node.getChild(0)).getChildCount(), is(2));
		assertThat("[0][0]", ((IMXMLIntNode)node.getChild(0).getChild(0)).getValue(), is(1));
		assertThat("[0][1]", ((IMXMLIntNode)node.getChild(0).getChild(1)).getValue(), is(2));
		assertThat("[1].getChildCount", ((IMXMLVectorNode)node.getChild(1)).getChildCount(), is(2));
		assertThat("[1][0]", ((IMXMLIntNode)node.getChild(1).getChild(0)).getValue(), is(3));
		assertThat("[1][1]", ((IMXMLIntNode)node.getChild(1).getChild(1)).getValue(), is(4));
	}
}
