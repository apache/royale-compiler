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
import org.apache.royale.compiler.tree.mxml.IMXMLDeclarationsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLDeclarationsNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLDeclarationsNodeTests extends MXMLNodeBaseTests
{	
	private IMXMLDeclarationsNode getMXMLDeclarationsNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLDeclarationsNode node = (IMXMLDeclarationsNode)findFirstDescendantOfType(fileNode, IMXMLDeclarationsNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLDeclarationsID));
		assertThat("getName", node.getName(), is("Declarations"));
		return node;
	}
	
	@Test
	public void MXMLDeclarationsNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Declarations/>"
		};
		IMXMLDeclarationsNode node = getMXMLDeclarationsNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getDeclarationInstanceNodes", node.getDeclarationInstanceNodes().length, is(0));
	}
	
	@Test
	public void MXMLDeclarationsNode_empty2()
	{
		String[] code = new String[]
		{
		    "<fx:Declarations>",
		    "</fx:Declarations>"
	    };
		IMXMLDeclarationsNode node = getMXMLDeclarationsNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getDeclarationInstanceNodes", node.getDeclarationInstanceNodes().length, is(0));
	}
	
	@Test
	public void MXMLDeclarationsNode_one()
	{
		String[] code = new String[]
		{
		    "<fx:Declarations>",
		    "    <fx:int/>",
		    "</fx:Declarations>"
		};
		IMXMLDeclarationsNode node = getMXMLDeclarationsNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getDeclarationInstanceNodes", node.getDeclarationInstanceNodes().length, is(1));
		assertThat("getDeclarationInstanceNodes[0]", node.getDeclarationInstanceNodes()[0].getName(), is("int"));
	}
	
	@Test
	public void MXMLDeclarationsNode_two()
	{
		String[] code = new String[]
		{
		    "<fx:Declarations className='MySprite'>",
		    "    <fx:int/>",
		    "    <fx:uint/>",
		    "</fx:Declarations>"
		};
		errorFilters = new String[1];
		errorFilters[0] = "This attribute is unexpected";
		IMXMLDeclarationsNode node = getMXMLDeclarationsNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("getDeclarationInstanceNodes", node.getDeclarationInstanceNodes().length, is(2));
		assertThat("getDeclarationInstanceNodes[0]", node.getDeclarationInstanceNodes()[0].getName(), is("int"));
		assertThat("getDeclarationInstanceNodes[1]", node.getDeclarationInstanceNodes()[1].getName(), is("uint"));
	}
}
