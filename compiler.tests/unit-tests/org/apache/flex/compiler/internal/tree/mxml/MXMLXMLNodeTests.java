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
import org.apache.flex.compiler.tree.mxml.IMXMLXMLNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLXMLNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLXMLNodeTests extends MXMLInstanceNodeTests
{
	private IMXMLXMLNode getMXMLXMLNode(String code)
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
		String code = "<fx:XML/>";
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLXMLNode_empty2()
	{
		String code = "<fx:XML></fx:XML>";
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLXMLNode_empty3()
	{
		String code = "<fx:XML> \t\r\n</fx:XML>";
		IMXMLXMLNode node = getMXMLXMLNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
}
