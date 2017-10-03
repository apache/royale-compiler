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
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLIntNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLIntNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLIntNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLIntNode getMXMLIntNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLIntNode node = (IMXMLIntNode)findFirstDescendantOfType(fileNode, IMXMLIntNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLIntID));
		assertThat("getName", node.getName(), is("int"));
		return node;
	}
	
	@Test
	public void MXMLIntNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:int/>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(0));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLIntNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:int></fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(0));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLIntNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:int> \t\r\n</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(0));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLIntNode_zero()
	{
		String[] code = new String[]
		{
			"<fx:int>0</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(0));
		testExpressionLocation(node, 8, 9);
	}

	@Test
	public void MXMLIntNode_minusZero()
	{
		String code[] = new String[]
		{
			"<fx:int>-0</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(0));
		testExpressionLocation(node, 8, 10);
	}

	@Test
	public void MXMLIntNode_one()
	{
		String[] code = new String[]
		{
			"<fx:int>1</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(1));
		testExpressionLocation(node, 8, 9);
	}

	@Test
	public void MXMLIntNode_minusOne()
	{
		String[] code = new String[]
		{
			"<fx:int>-1</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(-1));
		testExpressionLocation(node, 8, 10);
	}

	@Test
	public void MXMLIntNode_maxInt()
	{
		String[] code = new String[]
		{
			"<fx:int>2147483647</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(2147483647));
		testExpressionLocation(node, 8, 18);
	}

	@Test
	public void MXMLIntNode_minInt()
	{
		String[] code = new String[]
		{
			"<fx:int>-2147483648</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(-2147483648));
		testExpressionLocation(node, 8, 19);
	}
	
	@Test
	public void MXMLIntNode_hex_short_zero()
	{
		String[] code = new String[]
		{
			"<fx:int>0x0</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(0));
		testExpressionLocation(node, 8, 11);		
	}
	
	@Test
	public void MXMLIntNode_hex_max()
	{
		String[] code = new String[]
		{
			"<fx:int>0x7FFFffff</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(2147483647));
		testExpressionLocation(node, 8, 18);		
	}
	
	@Test
	public void MXMLIntNode_hex_min()
	{
		String[] code = new String[]
		{
			"<fx:int>-0X80000000</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(-2147483648));
		testExpressionLocation(node, 8, 19);
	}
	
	@Test
	public void MXMLIntNode_hash_short_zero()
	{
		String[] code = new String[]
		{
			"<fx:int>#0</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(0));
		testExpressionLocation(node, 8, 10);		
	}
	
	@Test
	public void MXMLIntNode_hash_max()
	{
		String[] code = new String[]
		{
			"<fx:int>#7FFFFFFF</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(2147483647));
		testExpressionLocation(node, 8, 17);		
	}
	
	@Test
	public void MXMLIntNode_hash_min()
	{
		String[] code = new String[]
		{
			"<fx:int>-#80000000</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(-2147483648));
		testExpressionLocation(node, 8, 18);
	}
	
	@Test
	public void MXMLIntNode_withWhitespace()
	{
		String[] code = new String[]
		{
			"<fx:int> -123 </fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(-123));
		//testExpressionLocation(node, 9, 13); // location of the MXMLLiteralNode should not include the whitespace
	}
	
	@Ignore
	@Test
	public void MXMLIntNode_nonnumeric()
	{
		String[] code = new String[]
		{
			"<fx:int> abc </fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("getValue", node.getValue(), is(0));
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
		
	@Test
	public void MXMLIntNode_with_databinding()
	{
		String code[] = new String[]
		{
			"<fx:int>{a.b}</fx:int>"
		};
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat("databinding node", node.getExpressionNode().getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		testExpressionLocation(node, 8, 13);
		assertThat("databinding node child count", node.getExpressionNode().getChildCount(), is(1));
		assertThat("identifier node", node.getExpressionNode().getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
