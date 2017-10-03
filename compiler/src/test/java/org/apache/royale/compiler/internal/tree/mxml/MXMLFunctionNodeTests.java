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

import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFunctionNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLFunctionNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLFunctionNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLFunctionNode getMXMLFunctionNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLFunctionNode node = (IMXMLFunctionNode)findFirstDescendantOfType(fileNode, IMXMLFunctionNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLFunctionID));
		assertThat("getName", node.getName(), is("Function"));
		return node;
	}
	
	@Test
	public void MXMLFunctionNode_empty1()
	{
		String[] code = new String[]
        {
    	    "<fx:Function/>"
        };
		IMXMLFunctionNode node = getMXMLFunctionNode(code);
		assertThat("getValue", node.getValue(project), is((IFunctionDefinition)null));
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLFunctionNode_empty2()
	{
		String[] code = new String[]
		{
		    "<fx:Function></fx:Function>"
		};
		IMXMLFunctionNode node = getMXMLFunctionNode(code);
		assertThat("getValue", node.getValue(project), is((IFunctionDefinition)null));
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLFunctionNode_empty3()
	{
		String[] code = new String[]
		{
		    "<fx:Function> \t\r\n</fx:Function>"
		};
		IMXMLFunctionNode node = getMXMLFunctionNode(code);
		assertThat("getValue", node.getValue(project), is((IFunctionDefinition)null));
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLFunctionNode_trace()
	{
		String[] code = new String[]
	    {
		    "<fx:Function>trace</fx:Function>"
		};
		IMXMLFunctionNode node = getMXMLFunctionNode(code);
		assertThat("getValue", node.getValue(project).getQualifiedName(), is("trace"));
		testExpressionLocation(node, 13, 18);
	}

	@Ignore
	@Test
	public void MXMLFunctionNode_flash_utils_getQualifiedClassName()
	{
		String[] code = new String[]
		{
		    "<fx:Function>flash.utils.getQualifiedClassName</fx:Function>"
		};
		IMXMLFunctionNode node = getMXMLFunctionNode(code);
		assertThat("getValue", node.getValue(project).getQualifiedName(), is("flash.utils.getQualifiedClassName"));
		testExpressionLocation(node, 13, 41);
	}
	
	@Ignore
	@Test
	public void MXMLFunctionNode_with_databinding()
	{
		String[] code = new String[]
		{
		    "<fx:Function>{a.b}</fx:Function>"
		};
		IMXMLFunctionNode node = getMXMLFunctionNode(code);
		assertThat("databinding node", node.getExpressionNode().getNodeID(), is(ASTNodeID.MXMLDataBindingID));
		testExpressionLocation(node, 13, 18);
		assertThat("databinding node child count", node.getExpressionNode().getChildCount(), is(1));
		assertThat("identifier node", node.getExpressionNode().getChild(0).getNodeID(), is(ASTNodeID.MemberAccessExpressionID));
	}
}
