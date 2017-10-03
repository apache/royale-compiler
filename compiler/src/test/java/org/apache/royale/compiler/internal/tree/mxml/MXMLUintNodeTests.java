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
import org.apache.royale.compiler.tree.mxml.IMXMLUintNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLUintNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLUintNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLUintNode getMXMLUintNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLUintNode node = (IMXMLUintNode)findFirstDescendantOfType(fileNode, IMXMLUintNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLUintID));
		assertThat("getName", node.getName(), is("uint"));
		return node;
	}
	
	@Test
	public void MXMLUintNode_empty1()
	{
		String[] code = new String[]
		{
		    "<fx:uint/>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLUintNode_empty2()
	{
		String[] code = new String[]
		{
	        "<fx:uint></fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLUintNode_empty3()
	{
		String[] code = new String[]
		{
		     "<fx:uint> \t\r\n</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLUintNode_zero()
	{
		String[] code = new String[]
		{
		    "<fx:uint>0</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		testExpressionLocation(node, 9, 10);
	}

	@Test
	public void MXMLUintNode_one()
	{
		String[] code = new String[]
		{
		    "<fx:uint>1</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(1L));
		testExpressionLocation(node, 9, 10);
	}

	@Test
	public void MXMLUintNode_maxUint()
	{
		String[] code = new String[]
		{
		    "<fx:uint>4294967295</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(4294967295L));
		testExpressionLocation(node, 9, 19);
	}
	
	@Test
	public void MXMLUintNode_hex_short_zero()
	{
		String[] code = new String[]
		{
		    "<fx:uint>0x0</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		testExpressionLocation(node, 9, 12);		
	}
	
	@Test
	public void MXMLUintNode_hex_max()
	{
		String[] code = new String[]
		{
		    "<fx:uint>0x7FFFffff</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(2147483647L));
		testExpressionLocation(node, 9, 19);		
	}
	
	@Test
	public void MXMLUintNode_hash_short_zero()
	{
		String[] code = new String[]
		{
		    "<fx:uint>#0</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		testExpressionLocation(node, 9, 11);		
	}
	
	@Test
	public void MXMLUintNode_hash_max()
	{
		String[] code = new String[]
		{
		    "<fx:uint>#7FFFFFFF</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(2147483647L));
		testExpressionLocation(node, 9, 18);		
	}
	
	@Test
	public void MXMLUintNode_withWhitespace()
	{
		String[] code = new String[]
		{
		    "<fx:uint> 123 </fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(123L));
		//testExpressionLocation(node, 9, 13); // location of the MXMLLiteralNode should not include the whitespace
	}
	
	@Ignore
	@Test
	public void MXMLUintNode_nonnumeric()
	{
		String[] code = new String[]
		{
		    "<fx:uint> abc </fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
		
	@Test
	public void MXMLUintNode_with_databinding()
	{
		String[] code = new String[]
		{
		    "<fx:uint>{a.b}</fx:uint>"
		};
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("databinding node", node.getExpressionNode().getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		testExpressionLocation(node, 9, 14);
		assertThat("databinding node child count", node.getExpressionNode().getChildCount(), is(1));
		assertThat("identifier node", node.getExpressionNode().getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
