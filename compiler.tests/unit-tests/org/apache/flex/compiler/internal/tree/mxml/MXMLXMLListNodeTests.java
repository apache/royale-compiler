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
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLXMLListNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLXMLListNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLXMLListNodeTests extends MXMLInstanceNodeTests
{
	private static String EOL = "\n\t\t";
	
	private IMXMLXMLListNode getMXMLXMLListNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLXMLListNode node = (IMXMLXMLListNode)findFirstDescendantOfType(fileNode, IMXMLXMLListNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLXMLListID));
		assertThat("getName", node.getName(), is("XMLList"));
		return node;
	}
	
	@Test
	public void MXMLXMLListNode_empty1()
	{
		String code = "<fx:XMLList/>";
		IMXMLXMLListNode node = getMXMLXMLListNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLXMLListNode_empty2()
	{
		String code = "<fx:XMLList></fx:XMLList>";
		IMXMLXMLListNode node = getMXMLXMLListNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLXMLListNode_empty3()
	{
		String code = "<fx:XMLList> \t\r\n</fx:XMLList>";
		IMXMLXMLListNode node = getMXMLXMLListNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}

	@Test
	public void MXMLXMLListNode_two_empty_tags()
	{
		String code =
			"<fx:XMLList>" + EOL +
		    "    <a/>" + EOL +
		    "    <b/>" + EOL +
			"</fx:XMLList>";
		IMXMLXMLListNode node = getMXMLXMLListNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getXMLString", node.getXMLString(), is("<a/><b/>"));
	}
	
	@Ignore
	@Test
	public void MXMLXMListLNode_with_databinding()
	{
		String code = "<fx:XMLList>{a.b}</fx:XMLList>";
		IMXMLXMLListNode node = getMXMLXMLListNode(code);
		assertThat("databinding node", node.getChild(0).getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		assertThat("databinding node child count", node.getChild(0).getChildCount(), is(1));
		assertThat("identifier node", node.getChild(0).getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
