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

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLDefinitionNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLDefinitionNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLDefinitionNodeTests extends MXMLNodeBaseTests
{	
	private static String EOL = "\n\t\t";

	protected String getPrefix()
	{
		return "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*' xmlns:s='library://ns.adobe.com/flex/spark' xmlns:mx='library://ns.adobe.com/flex/mx'>\n" +
	           "    <fx:Library>\n" +
		       "        ";
	}
			
    protected String getPostfix()
    {
    	return "\n" +
		       "    </fx:Library>\n" +
		       "</d:Sprite>";
    }
    
    @Override
    protected IMXMLFileNode getMXMLFileNode(String code)
    {
    	return super.getMXMLFileNode(getPrefix() + code + getPostfix());
    }

    private IMXMLDefinitionNode getMXMLDefinitionNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLDefinitionNode node = (IMXMLDefinitionNode)findFirstDescendantOfType(fileNode, IMXMLDefinitionNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLDefinitionID));
		assertThat("getName", node.getName(), is("Definition"));
		return node;
	}
	
	// Note: getDefinitionName() always returns null for an IMXMLDefinitionNode.
	// It is non-null in the case of an IFactoryNode that gets created
	// for the value of a property of type IFactory.
	
	@Test
	public void MXMLDefinitionNode_empty1()
	{
		String code = "<fx:Definition/>";
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getName", node.getDefinitionName(), is((String)null));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is((IMXMLClassDefinitionNode)null));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition(), is((IClassDefinition)null));
	}
	
	@Test
	public void MXMLDefinitionNode_empty2()
	{
		String code =
		    "<fx:Definition>" + EOL +
		    "</fx:Definition>";
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getDefinitionName", node.getDefinitionName(), is((String)null));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is((IMXMLClassDefinitionNode)null));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition(), is((IClassDefinition)null));
	}
	
	@Test
	public void MXMLDefinitionNode_Sprite()
	{
		String code =
		    "<fx:Definition>" + EOL +
		    "    <d:Sprite/>" + EOL +
		    "</fx:Definition>";
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getDefinitionName", node.getDefinitionName(), is((String)null));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is(node.getChild(0)));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition().isInstanceOf("flash.display.Sprite", project), is(true));
	}
	
	@Test
	public void MXMLDefinitionNode_name_Sprite()
	{
		String code =
		    "<fx:Definition name='MySprite'>" + EOL +
		    "    <d:Sprite/>" + EOL +
		    "</fx:Definition>";
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getDefinitionName", node.getDefinitionName(), is("MySprite"));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is(node.getChild(0)));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition().isInstanceOf("flash.display.Sprite", project), is(true));
	}
	
	@Test
	public void MXMLDefinitionNode_name_Sprite_width_height()
	{
		String code =
		    "<fx:Definition name='MySprite'>" + EOL +
		    "    <d:Sprite width='100'>" + EOL +
		    "        <d:height>100</d:height>" + EOL +
		    "    </d:Sprite>" + EOL +
		    "</fx:Definition>";
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getDefinitionName", node.getDefinitionName(), is("MySprite"));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is(node.getChild(0)));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition().isInstanceOf("flash.display.Sprite", project), is(true));
		assertThat("getContainedClassDefinitionNode.getChildCount", node.getContainedClassDefinitionNode().getChildCount(), is(2));

	}
}
