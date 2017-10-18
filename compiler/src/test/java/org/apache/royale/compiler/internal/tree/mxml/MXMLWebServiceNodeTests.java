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
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLWebServiceOperationNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLWebServiceNode} and {@link MXMLWebServiceOperationNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLWebServiceNodeTests extends MXMLInstanceNodeTests
{	
	private IMXMLWebServiceNode getMXMLWebServiceNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNodeWithFlex(code);
		IMXMLWebServiceNode node = (IMXMLWebServiceNode)findFirstDescendantOfType(fileNode, IMXMLWebServiceNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLWebServiceID));
		assertThat("getName", node.getName(), is("mx.rpc.soap.mxml.WebService"));
		return node;
	}
	
	@Test
	public void MXMLWebServiceNode_twoOperations()
	{
		String[] code = new String[]
		{
		    "<mx:WebService id='ws' wsdl='http://whatever'>",
		    "    <mx:operation name='op1'/>",
		    "    <mx:operation name='op2'/>",
		    "</mx:WebService>"
		};
		IMXMLWebServiceNode node = getMXMLWebServiceNode(code);
		assertThat("getChildCount", node.getChildCount(), is(3));
		IMXMLPropertySpecifierNode wsdlNode = (IMXMLPropertySpecifierNode)node.getChild(0);
		assertThat("", wsdlNode.getName(), is("wsdl"));
		IMXMLWebServiceOperationNode op1Node = (IMXMLWebServiceOperationNode)node.getChild(1);
		assertThat("", op1Node.getName(), is("mx.rpc.soap.mxml.Operation"));
		assertThat("", op1Node.getOperationName(), is("op1"));
		IMXMLWebServiceOperationNode op2Node = (IMXMLWebServiceOperationNode)node.getChild(2);
		assertThat("", op2Node.getName(), is("mx.rpc.soap.mxml.Operation"));
		assertThat("", op2Node.getOperationName(), is("op2"));
	}
}
