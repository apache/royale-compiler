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
import org.apache.royale.compiler.tree.mxml.IMXMLXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLXMLNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLXMLNodeTests extends MXMLInstanceNodeTests
{	
	private IMXMLXMLNode getMXMLXMLNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLXMLNode node = (IMXMLXMLNode)findFirstDescendantOfType(fileNode, IMXMLXMLNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLXMLID));
		assertThat("getName", node.getName(), is("XML"));
		return node;
	}
	
	@Test
	public void MXMLXMLNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:XML/>"
		};
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getXMLType", node.getXMLType(), is(IMXMLXMLNode.XML_TYPE.E4X));
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getXMLString", node.getXMLString(), is((String)null));
	}
	
	@Test
	public void MXMLXMLNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:XML></fx:XML>"
		};
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getXMLType", node.getXMLType(), is(IMXMLXMLNode.XML_TYPE.E4X));
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getXMLString", node.getXMLString(), is((String)null));
	}
	
	@Test
	public void MXMLXMLNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:XML> \t\r\n</fx:XML>"
		};
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getXMLType", node.getXMLType(), is(IMXMLXMLNode.XML_TYPE.E4X));
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getXMLString", node.getXMLString(), is((String)null));
	}
	
	@Test
	public void MXMLXMLNode_empty_root()
	{
		String[] code = new String[]
		{
			"<fx:XML>",
		    "    <root/>",
			"</fx:XML>"
		};
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getXMLType", node.getXMLType(), is(IMXMLXMLNode.XML_TYPE.E4X));
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getXMLString", node.getXMLString(), is("<root/>"));
	}
	
	@Test
	public void MXMLXMLNode_root_with_one_child_with_text()
	{
		String[] code = new String[]
		{
			"<fx:XML>",
		    "    <root>",
		    "        <a>xxx</a>",
		    "    </root>",
			"</fx:XML>"
		};
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getXMLType", node.getXMLType(), is(IMXMLXMLNode.XML_TYPE.E4X));
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getXMLString", node.getXMLString(), is("<root><a>xxx</a></root>"));
	}
	
	@Test
	public void MXMLXMLNode_root_with_one_child_with_attribute()
	{
		String[] code = new String[]
		{
			"<fx:XML>",
		    "    <root>",
		    "        <a b='xxx' />",
		    "    </root>",
			"</fx:XML>"
		};
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getXMLType", node.getXMLType(), is(IMXMLXMLNode.XML_TYPE.E4X));
		// childCount is 0 because we are counting MXML children, not XML children
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getXMLString", node.getXMLString(), is("<root><a b=\"xxx\"/></root>")); // should single quote come back as double quote?
	}
	
	@Ignore
	@Test
	public void MXMLXMLNode_with_databinding()
	{
		String[] code = new String[]
		{
			"<fx:XML>{a.b}</fx:XML>"
		};
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("databinding node", node.getChild(0).getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		assertThat("databinding node child count", node.getChild(0).getChildCount(), is(1));
		assertThat("identifier node", node.getChild(0).getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
