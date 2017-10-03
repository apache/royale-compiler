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
import org.apache.royale.compiler.tree.mxml.IMXMLNumberNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLNumberNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLNumberNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLNumberNode getMXMLNumberNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLNumberNode node = (IMXMLNumberNode)findFirstDescendantOfType(fileNode, IMXMLNumberNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLNumberID));
		assertThat("getName", node.getName(), is("Number"));
		return node;
	}
	
	@Test
	public void MXMLNumberNode_empty1()
	{
		String[] code = new String[]
		{
		    "<fx:Number/>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_empty2()
	{
		String[] code = new String[]
		{
		    "<fx:Number></fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLNumberNode_empty3()
	{
		String[] code = new String[]
		{
		    "<fx:Number> \t\r\n</fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_integer()
	{
		String[] code = new String[]
		{
		    "<fx:Number> 1 </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(1.0));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_hexInteger_upperCase()
	{
		String[] code = new String[]
		{
		    "<fx:Number> 0xABCDEF </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is((double)0xABCDEF));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_hexInteger_lowerCase()
	{
		String[] code = new String[]
		{
		    "<fx:Number> -0Xabcdef </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is((double)-0xABCDEF));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_fractional()
	{
		String[] code = new String[]
		{
		    "<fx:Number> 0.5 </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(0.5));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_scientific_lowerCase_negExp()
	{
		String[] code = new String[]
		{
		    "<fx:Number> -1.5e-10 </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(-1.5e-10));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_scientific_upperCase_posExp()
	{
		String[] code = new String[]
		{
		    "<fx:Number> -1.5E+10 </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(-1.5e10));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_NaN()
	{
		String[] code = new String[]
		{
		    "<fx:Number> NaN </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_posInfinity()
	{
		String[] code = new String[]
		{
		    "<fx:Number> Infinity </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.POSITIVE_INFINITY));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_negInfinity()
	{
		String[] code = new String[]
		{
		    "<fx:Number> -Infinity </fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NEGATIVE_INFINITY));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_with_databinding()
	{
		String[] code = new String[]
		{
		    "<fx:Number>{a.b}</fx:Number>"
		};
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("databinding node", node.getExpressionNode().getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		testExpressionLocation(node, 11, 16);
		assertThat("databinding node child count", node.getExpressionNode().getChildCount(), is(1));
		assertThat("identifier node", node.getExpressionNode().getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
