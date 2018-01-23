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

import org.apache.royale.compiler.definitions.IEventDefinition;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLEventSpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.utils.StringUtils;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLEventSpecifierNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLEventSpecifierNodeTests extends MXMLSpecifierNodeBaseTests
{
	@Override
	protected String[] getTemplate()
	{
		return new String[]
		{
	    };
	}
	
	@Override
	protected String getMXML(String[] code)
    {
        return StringUtils.join(code, "\n");
    }
	
	private IMXMLEventSpecifierNode getMXMLEventSpecifierNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLEventSpecifierNode node = (IMXMLEventSpecifierNode)findFirstDescendantOfType(fileNode, IMXMLEventSpecifierNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLEventSpecifierID));
		assertThat("children", node.getChildCount() == node.getASNodes().length, is(true));
		for (int i = 0; i < node.getChildCount(); i++)
		{
			assertThat("children", node.getChild(i) == node.getASNodes()[i], is(true));
		}
		return node;
	}
	
	@Test
	public void MXMLEventSpecifierNode_emptyEventAttribute1()
	{
		String[] code = new String[]
		{
			"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'",
			"    complete='' >",
			"</custom:TestInstance>"
		};
		errorFilters = new String[1];
		errorFilters[0] = "Event handler is empty";
		IMXMLEventSpecifierNode node = getMXMLEventSpecifierNode(code);
		assertThat("getName", node.getName(), is("complete"));
		assertThat("getSuffix", node.getSuffix(), is(""));
		assertThat("getDefinition", ((IEventDefinition)node.getDefinition()).getBaseName(), is("complete"));
		IASNode[] asNodes = node.getASNodes();
		assertThat("getASNodes", asNodes.length, is(0));
	}
	
	@Test
	public void MXMLEventSpecifierNode_emptyEventAttribute2()
	{
		String[] code = new String[]
		{
				"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'",
				"    complete=' ' >",
				"</custom:TestInstance>"
		};
		IMXMLEventSpecifierNode node = getMXMLEventSpecifierNode(code);
		assertThat("getName", node.getName(), is("complete"));
		assertThat("getSuffix", node.getSuffix(), is(""));
		assertThat("getDefinition", ((IEventDefinition)node.getDefinition()).getBaseName(), is("complete"));
		IASNode[] asNodes = node.getASNodes();
		assertThat("getASNodes", asNodes.length, is(0));
	}
	
	@Test
	public void MXMLEventSpecifierNode_emptyEventAttribute3()
	{
		String[] code = new String[]
		{
				"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'",
				"    complete=' \t\r\n' >",
				"</custom:TestInstance>"
		};
		IMXMLEventSpecifierNode node = getMXMLEventSpecifierNode(code);
		assertThat("getName", node.getName(), is("complete"));
		assertThat("getSuffix", node.getSuffix(), is(""));
		assertThat("getDefinition", ((IEventDefinition)node.getDefinition()).getBaseName(), is("complete"));
		IASNode[] asNodes = node.getASNodes();
		assertThat("getASNodes", asNodes.length, is(0));
	}
	
	@Test
	public void MXMLEventSpecifierNode_eventAttribute_twoFunctionCalls()
	{
		String[] code = new String[]
		{
			"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'",
			"    complete='someFunction();someFunction()' >",
			"</custom:TestInstance>"
		};
		IMXMLEventSpecifierNode node = getMXMLEventSpecifierNode(code);
		assertThat("getName", node.getName(), is("complete"));
		assertThat("getSuffix", node.getSuffix(), is(""));
		assertThat("getDefinition", ((IEventDefinition)node.getDefinition()).getBaseName(), is("complete"));
		IASNode[] asNodes = node.getASNodes();
		assertThat("getASNodes", asNodes.length, is(2));
		assertThat("child 0", asNodes[0].getNodeID(), is(ASTNodeID.FunctionCallID));
		assertThat("child 0", asNodes[1].getNodeID(), is(ASTNodeID.FunctionCallID));
	}
	
	@Test
	public void MXMLEventSpecifierNode_emptyEventTag1()
	{
		String[] code = new String[]
		{
			"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'>",
			"    <custom:complete/>",
			"</custom:TestInstance>"
		};
		IMXMLEventSpecifierNode node = getMXMLEventSpecifierNode(code);
		assertThat("getName", node.getName(), is("complete"));
		assertThat("getSuffix", node.getSuffix(), is(""));
		assertThat("getDefinition", ((IEventDefinition)node.getDefinition()).getBaseName(), is("complete"));
		IASNode[] asNodes = node.getASNodes();
		assertThat("getASNodes", asNodes.length, is(0));
	}
	
	@Test
	public void MXMLEventSpecifierNode_emptyEventTag2()
	{
		String[] code = new String[]
		{
			"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'>",
			"    <custom:complete></custom:complete>",
			"</custom:TestInstance>"
		};
		IMXMLEventSpecifierNode node = getMXMLEventSpecifierNode(code);
		assertThat("getName", node.getName(), is("complete"));
		assertThat("getSuffix", node.getSuffix(), is(""));
		assertThat("getDefinition", ((IEventDefinition)node.getDefinition()).getBaseName(), is("complete"));
		IASNode[] asNodes = node.getASNodes();
		assertThat("getASNodes", asNodes.length, is(0));
	}
	
	@Test
	public void MXMLEventSpecifierNode_emptyEventTag3()
	{
		String[] code = new String[]
   		{
			"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'>",
   			"    <custom:complete> \t\r\n</custom:complete>",
			"</custom:TestInstance>"
   		};
		IMXMLEventSpecifierNode node = getMXMLEventSpecifierNode(code);
		assertThat("getName", node.getName(), is("complete"));
		assertThat("getSuffix", node.getSuffix(), is(""));
		assertThat("getDefinition", ((IEventDefinition)node.getDefinition()).getBaseName(), is("complete"));
		IASNode[] asNodes = node.getASNodes();
		assertThat("getASNodes", asNodes.length, is(0));
	}
	
	@Test
	public void MXMLEventSpecifierNode_eventTag_twoFunctionCalls()
	{
		String[] code = new String[]
   		{
			"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'>",
   			"    <custom:complete>",
			"          someFunction();",
			"          someFunction();",
   			"    </custom:complete>",
			"</custom:TestInstance>"
   		};
		IMXMLEventSpecifierNode node = getMXMLEventSpecifierNode(code);
		assertThat("getName", node.getName(), is("complete"));
		assertThat("getSuffix", node.getSuffix(), is(""));
		assertThat("getDefinition", ((IEventDefinition)node.getDefinition()).getBaseName(), is("complete"));
		IASNode[] asNodes = node.getASNodes();
		assertThat("getASNodes", asNodes.length, is(2));
		assertThat("child 0", asNodes[0].getNodeID(), is(ASTNodeID.FunctionCallID));
		assertThat("child 0", asNodes[1].getNodeID(), is(ASTNodeID.FunctionCallID));
	}
}
