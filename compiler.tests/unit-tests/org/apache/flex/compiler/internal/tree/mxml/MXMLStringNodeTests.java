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
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLStringNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLStringNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLStringNode getMXMLStringNode(String code)
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
		String code = "<fx:String/>";
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is((String)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLStringNode_empty2()
	{
		String code = "<fx:String></fx:String>";
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is((String)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLStringNode_empty3()
	{
		String code = "<fx:String> \t\r\n</fx:String>";
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is((String)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
}
