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
import org.apache.flex.compiler.tree.mxml.IMXMLArrayNode;
import org.apache.flex.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLArrayNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLArrayNodeTests extends MXMLInstanceNodeTests
{
	private static String EOL = "\n\t\t";
	
	private IMXMLArrayNode getMXMLArrayNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLArrayNode node = (IMXMLArrayNode)findFirstDescendantOfType(fileNode, IMXMLArrayNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLArrayID));
		assertThat("getName", node.getName(), is("Array"));
		return node;
	}
	
	@Test
	public void MXMLArrayNode_empty1()
	{
		String code = "<fx:Array/>";
		IMXMLArrayNode node = getMXMLArrayNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLArrayNode_empty2()
	{
		String code = "<fx:Array></fx:Array>";
		IMXMLArrayNode node = getMXMLArrayNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLArrayNode_empty3()
	{
		String code = "<fx:Array> \t\r\n</fx:Array>";
		IMXMLArrayNode node = getMXMLArrayNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLArrayNode_three_inhomogenous_elements()
	{
		String code =
			"<fx:Array>" + EOL +
		    "    <fx:Boolean>true</fx:Boolean>" + EOL +
			"    <fx:int>123</fx:int>" + EOL +
		    "    <fx:String>abc</fx:String>" + EOL +
			"</fx:Array>";
		IMXMLArrayNode node = getMXMLArrayNode(code);
		assertThat("getChildCount", node.getChildCount(), is(3));
		assertThat("[0]", ((IMXMLBooleanNode)node.getChild(0)).getValue(), is(true));
		assertThat("[1]", ((IMXMLIntNode)node.getChild(1)).getValue(), is(123));
		assertThat("[2]", ((IMXMLStringNode)node.getChild(2)).getValue(), is("abc"));
	}
	
	@Test
	public void MXMLArrayNode_nested_arrays()
	{
		String code =
			"<fx:Array>" + EOL +
		    "    <fx:Array>" + EOL +
		    "        <fx:int>1</fx:int>" + EOL +
		    "        <fx:int>2</fx:int>" + EOL +
		    "    </fx:Array>" + EOL +
		    "    <fx:Array>" + EOL +
		    "        <fx:int>3</fx:int>" + EOL +
		    "        <fx:int>4</fx:int>" + EOL +
		    "    </fx:Array>" + EOL +
			"</fx:Array>";
		IMXMLArrayNode node = getMXMLArrayNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("[0].getChildCount", ((IMXMLArrayNode)node.getChild(0)).getChildCount(), is(2));
		assertThat("[0][0]", ((IMXMLIntNode)node.getChild(0).getChild(0)).getValue(), is(1));
		assertThat("[0][1]", ((IMXMLIntNode)node.getChild(0).getChild(1)).getValue(), is(2));
		assertThat("[1].getChildCount", ((IMXMLArrayNode)node.getChild(1)).getChildCount(), is(2));
		assertThat("[1][0]", ((IMXMLIntNode)node.getChild(1).getChild(0)).getValue(), is(3));
		assertThat("[1][1]", ((IMXMLIntNode)node.getChild(1).getChild(1)).getValue(), is(4));
	}
}
