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
import org.apache.royale.compiler.tree.mxml.IMXMLPrivateNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLPrivateNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLPrivateNodeTests extends MXMLNodeBaseTests
{
	private IMXMLPrivateNode getMXMLPrivateNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLPrivateNode node = (IMXMLPrivateNode)findFirstDescendantOfType(fileNode, IMXMLPrivateNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLPrivateID));
		assertThat("getName", node.getName(), is("Private"));
		assertThat("getChildCount", node.getChildCount(), is(0));
		return node;
	}
	
	@Test
	public void MXMLPrivateNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Private/>"
		};
		getMXMLPrivateNode(code);
	}
	
	@Test
	public void MXMLPrivateNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:Private></fx:Private>"
		};
		getMXMLPrivateNode(code);
	}
	
	@Test
	public void MXMLPrivateNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:Private> \t\r\n</fx:Private>"
		};
		getMXMLPrivateNode(code);
	}
	
	@Test
	public void MXMLPrivateNode_text()
	{
		String[] code = new String[]
		{
			"<fx:Private>abc</fx:Private>"
		};
		getMXMLPrivateNode(code);
	}
	
	@Test
	public void MXMLPrivateNode_tags()
	{
		String[] code = new String[]
		{
			"<fx:Private>",
			"   <a>",
			"      <b c='1'/>",
			"   </a>" +
			"   <a>" +
			"      <b c='1'/>",
			"   </a>",
			"",
			"</fx:Private>"
		};
		getMXMLPrivateNode(code);
	}
}
