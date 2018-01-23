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
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLScriptNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLScriptNodeTests extends MXMLNodeBaseTests
{
	@Override
	protected String[] getTemplate()
	{
		return new String[]
		{
   			"<custom:TestInstance xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'>",
   			"    %1",
   			"</custom:TestInstance>"
	    };
	}

	private IMXMLScriptNode getMXMLScriptNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLScriptNode node = (IMXMLScriptNode)findFirstDescendantOfType(fileNode, IMXMLScriptNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLScriptID));
		assertThat("getName", node.getName(), is("Script"));
		return node;
	}
	
	@Test
	public void MXMLScriptNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Script/>"
		};
		IMXMLScriptNode node = getMXMLScriptNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLScriptNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:Script></fx:Script>"
		};
		IMXMLScriptNode node = getMXMLScriptNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLScriptNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:Script> \t\r\n</fx:Script>"
		};
		IMXMLScriptNode node = getMXMLScriptNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLScriptNode_var_and_function()
	{
		String[] code = new String[]
		{
			"<fx:Script>",
			"    private var i:int = 1;",
			"    private function f():void { };",
			"</fx:Script>"
		};
		IMXMLScriptNode node = getMXMLScriptNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("child 0", node.getChild(0).getNodeID(), is(ASTNodeID.VariableID));
		assertThat("child 1", node.getChild(1).getNodeID(), is(ASTNodeID.FunctionID));
	}
}
