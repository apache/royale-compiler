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
import org.apache.royale.compiler.tree.mxml.IMXMLStringNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLStringNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLStringNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLStringNode getMXMLStringNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLStringNode node = (IMXMLStringNode)findFirstDescendantOfType(fileNode, IMXMLStringNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLStringID));
		assertThat("getName", node.getName(), is("String"));
		return node;
	}
	
	@Test
	public void MXMLStringNode_empty1()
	{
		String[] code = new String[]
		{
		    "<fx:String/>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is("")); // was null in old compiler, but this is inconsistent with an empty String attribute
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLStringNode_empty2()
	{
		String[] code = new String[]
		{
		    "<fx:String></fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is(""));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLStringNode_empty3()
	{
		String[] code = new String[]
		{
		    "<fx:String> \t\r\n</fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is(""));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLStringNode_text()
	{
		String[] code = new String[]
		{
		    "<fx:String>abc</fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is("abc"));
		testExpressionLocation(node, 11, 14);
	}
	
	@Test
	public void MXMLStringNode_text_with_whitespace()
	{
		String[] code = new String[]
		{
		    "<fx:String> a b c </fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is(" a b c "));
		testExpressionLocation(node, 11, 18);
	}
	
	@Test
	public void MXMLStringNode_numeric()
	{
		String[] code = new String[]
		{
		    "<fx:String>123</fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is("123"));
		testExpressionLocation(node, 11, 14);
	}
	
	@Test
	public void MXMLStringNode_true()
	{
		String[] code = new String[]
		{
		    "<fx:String>true</fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is("true"));
		testExpressionLocation(node, 11, 15);
	}
	
	@Test
	public void MXMLStringNode_entities()
	{
		String[] code = new String[]
		{
		    "<fx:String>&#x41;&#x42;&#x43;</fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is("ABC"));
		testExpressionLocation(node, 11, 29);
	}
	
	@Ignore
	@Test
	public void MXMLStringNode_CDATA()
	{
		String[] code = new String[]
		{
		    "<fx:String><![CDATA[a]]><![CDATA[b]]><![CDATA[c]]></fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is("abc"));
		testExpressionLocation(node, 11, 50);
	}
	
	@Test
	public void MXMLStringNode_with_databinding()
	{
		String[] code = new String[]
		{
		    "<fx:String>{a.b}</fx:String>"
		};
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("databinding node", node.getExpressionNode().getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		testExpressionLocation(node, 11, 16);
		assertThat("databinding node child count", node.getExpressionNode().getChildCount(), is(1));
		assertThat("identifier node", node.getExpressionNode().getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
