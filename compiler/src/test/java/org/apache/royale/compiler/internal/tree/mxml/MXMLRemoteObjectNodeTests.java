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
import org.apache.royale.compiler.tree.mxml.IMXMLRemoteObjectMethodNode;
import org.apache.royale.compiler.tree.mxml.IMXMLRemoteObjectNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLRemoteObjectNode} and {@link MXMLRemoteObjectMethodNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLRemoteObjectNodeTests extends MXMLInstanceNodeTests
{
	private IMXMLRemoteObjectNode getMXMLRemoteObjectNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNodeWithFlex(code);
		IMXMLRemoteObjectNode node = (IMXMLRemoteObjectNode)findFirstDescendantOfType(fileNode, IMXMLRemoteObjectNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLRemoteObjectID));
		assertThat("getName", node.getName(), is("mx.rpc.remoting.mxml.RemoteObject"));
		return node;
	}
	
	@Test
	public void MXMLRemoteObjectNode_postRequest()
	{
		String[] code = new String[]
		{
		    "<mx:RemoteObject id='userRequest' destination='Whatever'>",
		    "    <mx:method name='GetQuote'>",
		    "        <mx:arguments>",
		    "            <symbol>{stockSymbol.text}</symbol>",
		    "        </mx:arguments>",
		    "    </mx:method>",
		    "</mx:RemoteObject>"
		};
		IMXMLRemoteObjectNode node = getMXMLRemoteObjectNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		IMXMLPropertySpecifierNode destinationNode = (IMXMLPropertySpecifierNode)node.getChild(0);
		assertThat("", destinationNode.getName(), is("destination"));
		IMXMLRemoteObjectMethodNode methodNode = (IMXMLRemoteObjectMethodNode)node.getChild(1);
		assertThat("", methodNode.getName(), is("mx.rpc.remoting.mxml.Operation"));
		assertThat("", methodNode.getMethodName(), is("GetQuote"));
	}

}
