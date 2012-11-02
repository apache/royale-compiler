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
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLArrayNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLArrayNodeTests extends MXMLInstanceNodeTests
{
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
}
