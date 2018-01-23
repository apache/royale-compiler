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
import org.apache.royale.compiler.tree.mxml.IMXMLLibraryNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLLibraryNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLLibraryNodeTests extends MXMLNodeBaseTests
{	
	private IMXMLLibraryNode getMXMLLibraryNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLLibraryNode node = (IMXMLLibraryNode)findFirstDescendantOfType(fileNode, IMXMLLibraryNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLLibraryID));
		assertThat("getName", node.getName(), is("Library"));
		return node;
	}
	
	@Test
	public void MXMLLibraryNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Library/>"
		};
		IMXMLLibraryNode node = getMXMLLibraryNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getDefinitionNodes", node.getDefinitionNodes().length, is(0));
	}
	
	@Test
	public void MXMLLibraryNode_empty2()
	{
		String[] code = new String[]
		{
		    "<fx:Library>",
		    "</fx:Library>"
		};
		IMXMLLibraryNode node = getMXMLLibraryNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getDefinitionNodes", node.getDefinitionNodes().length, is(0));
	}
	
	@Test
	public void MXMLLibraryNode_one()
	{
		String[] code = new String[]
		{
		    "<fx:Library>",
		    "    <fx:Definition name='MyTestInstance'>",
		    "        <custom:TestInstance/>",
		    "    </fx:Definition>",
		    "</fx:Library>"
		};
		IMXMLLibraryNode node = getMXMLLibraryNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getDefinitionNodes", node.getDefinitionNodes().length, is(1));
		assertThat("getDefinitionNodes[0]", node.getDefinitionNodes()[0].getDefinitionName(), is("MyTestInstance"));
	}
	
	@Test
	public void MXMLLibraryNode_two()
	{
		String[] code = new String[]
		{
		    "<fx:Library className='MyTestInstances'>",
		    "    <fx:Definition name='MyTestInstance1'>",
		    "        <custom:TestInstance/>",
		    "    </fx:Definition>",
		    "    <fx:Definition name='MyTestInstance2'>",
		    "        <custom:TestInstance/>",
		    "    </fx:Definition>",
		    "</fx:Library>"
		};
		errorFilters = new String[1];
		errorFilters[0] = "This attribute is unexpected";
		IMXMLLibraryNode node = getMXMLLibraryNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("getDeclarationInstanceNodes", node.getDefinitionNodes().length, is(2));
		assertThat("getDefinitionNodes[0]", node.getDefinitionNodes()[0].getDefinitionName(), is("MyTestInstance1"));
		assertThat("getDefinitionNodes[1]", node.getDefinitionNodes()[1].getDefinitionName(), is("MyTestInstance2"));
	}
}
