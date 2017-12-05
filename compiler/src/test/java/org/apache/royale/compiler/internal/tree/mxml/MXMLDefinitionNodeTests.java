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

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLClassDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDefinitionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.utils.StringUtils;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLDefinitionNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLDefinitionNodeTests extends MXMLNodeBaseTests
{	
	@Override
	protected String[] getTemplate()
	{
	    // Tests of MXMLDefinitionNodes are done by parsing <Definition> tags
		// inside this document template.
		return new String[]
		{
		    "<fx:Object xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'>",
			"    <fx:Library>",
			"        %1",
			"    </fx:Library>",
		    "</fx:Object>"
	    };
	}
	
	@Override
	protected String getMXML(String[] code)
    {
        String mxml = StringUtils.join(getTemplate(), "\n");
        mxml = mxml.replace("%1", StringUtils.join(code, "\n        "));
        return mxml;
    }
	
    private IMXMLDefinitionNode getMXMLDefinitionNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLDefinitionNode node = (IMXMLDefinitionNode)findFirstDescendantOfType(fileNode, IMXMLDefinitionNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLDefinitionID));
		assertThat("getName", node.getName(), is("Definition"));
		return node;
	}
	
	@Test
	public void MXMLDefinitionNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Definition/>"
		};
		errorFilters = new String[1];
		errorFilters[0] = "The <Definition> tag requires a 'name' attribute";
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getName", node.getDefinitionName(), is((String)null));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is((IMXMLClassDefinitionNode)null));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition(), is((IClassDefinition)null));
	}
	
	@Test
	public void MXMLDefinitionNode_empty2()
	{
		String[] code = new String[]
		{
		    "<fx:Definition>",
		    "</fx:Definition>"
		};
		errorFilters = new String[1];
		errorFilters[0] = "The <Definition> tag requires a 'name' attribute";
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
		assertThat("getDefinitionName", node.getDefinitionName(), is((String)null));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is((IMXMLClassDefinitionNode)null));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition(), is((IClassDefinition)null));
	}
	
	@Test
	public void MXMLDefinitionNode_TestInstance()
	{
		String[] code = new String[]
		{
		    "<fx:Definition>",
		    "    <custom:TestInstance/>",
		    "</fx:Definition>"
		};
		errorFilters = new String[1];
		errorFilters[0] = "The <Definition> tag requires a 'name' attribute";
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getDefinitionName", node.getDefinitionName(), is((String)null));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is(node.getChild(0)));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition().isInstanceOf("custom.TestInstance", project), is(true));
	}
	
	@Test
	public void MXMLDefinitionNode_name_TestInstance()
	{
		String[] code = new String[]
		{
		    "<fx:Definition name='MyTestInstance'>",
		    "    <custom:TestInstance/>",
		    "</fx:Definition>"
		};
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getDefinitionName", node.getDefinitionName(), is("MyTestInstance"));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is(node.getChild(0)));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition().isInstanceOf("custom.TestInstance", project), is(true));
	}
	
	@Test
	public void MXMLDefinitionNode_name_TestInstance_NestedProperty()
	{
		String[] code = new String[]
		{
		    "<fx:Definition name='MyTestInstance'>",
		    "    <custom:TestInstance name='foo'>",
		    "        <custom:value>100</custom:value>",
		    "    </custom:TestInstance>",
		    "</fx:Definition>"
		};
		IMXMLDefinitionNode node = getMXMLDefinitionNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getDefinitionName", node.getDefinitionName(), is("MyTestInstance"));
		assertThat("getContainedClassDefinitionNode", node.getContainedClassDefinitionNode(), is(node.getChild(0)));
		assertThat("getContainedClassDefinition", node.getContainedClassDefinition().isInstanceOf("custom.TestInstance", project), is(true));
		assertThat("getContainedClassDefinitionNode.getChildCount", node.getContainedClassDefinitionNode().getChildCount(), is(2));
	}
}
