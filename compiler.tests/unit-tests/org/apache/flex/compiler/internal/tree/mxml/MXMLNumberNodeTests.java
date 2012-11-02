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
import org.apache.flex.compiler.tree.mxml.IMXMLNumberNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLNumberNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLNumberNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLNumberNode getMXMLNumberNode(String code)
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
		String code = "<fx:Number/>";
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_empty2()
	{
		String code = "<fx:Number></fx:Number>";
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLNumberNode_empty3()
	{
		String code = "<fx:Number> \t\r\n</fx:Number>";
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
}
