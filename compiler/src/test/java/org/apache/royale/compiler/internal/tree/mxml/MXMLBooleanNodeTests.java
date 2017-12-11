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

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLBooleanNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLBooleanNodeTests extends MXMLExpressionNodeBaseTests
{	
	private IMXMLBooleanNode getMXMLBooleanNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLBooleanNode node = (IMXMLBooleanNode)findFirstDescendantOfType(fileNode, IMXMLBooleanNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLBooleanID));
		assertThat("getName", node.getName(), is("Boolean"));
		return node;
	}
	
	@Test
	public void MXMLBooleanNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Boolean/>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(false));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLBooleanNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:Boolean></fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(false));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLBooleanNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:Boolean> \t\r\n</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(false));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLBooleanNode_false()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>false</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(false));
		testExpressionLocation(node, 12, 17);
	}

	@Test
	public void MXMLBooleanNode_true()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>true</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		testExpressionLocation(node, 12, 16);
	}

	@Test
	public void MXMLBooleanNode_false_caseinsensitive()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>FaLsE</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(false));
		testExpressionLocation(node, 12, 17);
	}

	@Test
	public void MXMLBooleanNode_true_caseinsensitive()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>TruE</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		testExpressionLocation(node, 12, 16);
	}
	
	@Test
	public void MXMLBooleanNode_false_with_whitespace()
	{
		String[] code = new String []
		{
			"<fx:Boolean> false </fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(false));
		//testExpressionLocation(node, 13, 18);  // location of the MXMLLiteralNode should not include the whitespace 
	}

	@Test
	public void MXMLBooleanNode_true_with_whitespace()
	{
		String[] code = new String[]
		{
			"<fx:Boolean> true </fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		//testExpressionLocation(node, 13, 17); // location of the MXMLLiteralNode should not include the whitespace
	}
	
	@Test
	public void MXMLBooleanNode_with_comments()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>t<!-- comment -->ru<!--- comment -->e</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		testExpressionLocation(node, 12, 49);
	}
	
	@Test
	public void MXMLBooleanNode_with_entities()
	{
		String[] code = new String []
		{
				"<fx:Boolean>t&#114;u&#x65;</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		testExpressionLocation(node, 12, 26);
	}
	
	@Ignore
	@Test
	public void MXMLBooleanNode_with_cdata()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>t<![CDATA[r]]>u<![CDATA[e]]></fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		testExpressionLocation(node, 12, 40);
	}
	
	@Ignore
	@Test
	public void MXMLBooleanNode_abc()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>abc</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(false));
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLBooleanNode_with_xmlns_attribute()
	{
		String[] code = new String[]
		{
			"<fx:Boolean xmlns:foo='bar'>true</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		testExpressionLocation(node, 28, 32);
	}
	
	@Test
	public void MXMLBooleanNode_with_id_attribute()
	{
		String[] code = new String[]
		{
			"<fx:Boolean id='b1'>true</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		testExpressionLocation(node, 20, 24);
	}
	
	@Test
	public void MXMLBooleanNode_with_unrecognized_attribute()
	{
		String code[] = new String[]
		{
			"<fx:Boolean foo='bar'>true</fx:Boolean>"
		};
		errorFilters = new String[1];
		errorFilters[0] = "This attribute is unexpected";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
		testExpressionLocation(node, 22, 26);
		// check problem
	}
	
	@Ignore
	@Test
	public void MXMLBooleanNode_with_child_tag()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>true<foo/></fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("getValue", node.getValue(), is(true));
	}
	
	@Test
	public void MXMLBooleanNode_with_databinding()
	{
		String[] code = new String[]
		{
			"<fx:Boolean>{a.b}</fx:Boolean>"
		};
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat("databinding node", node.getExpressionNode().getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		testExpressionLocation(node, 12, 17);
		assertThat("databinding node child count", node.getExpressionNode().getChildCount(), is(1));
		assertThat("identifier node", node.getExpressionNode().getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
