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
import org.apache.royale.compiler.tree.mxml.IMXMLHTTPServiceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLHTTPServiceRequestPropertyNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLHTTPServiceNode} and {@link MXMLHTTPServiceRequestPropertyNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLHTTPServiceNodeTests extends MXMLInstanceNodeTests
{
	private IMXMLHTTPServiceNode getMXMLHTTPServiceNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNodeWithFlex(code);
		IMXMLHTTPServiceNode node = (IMXMLHTTPServiceNode)findFirstDescendantOfType(fileNode, IMXMLHTTPServiceNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLHTTPServiceID));
		assertThat("getName", node.getName(), is("mx.rpc.http.mxml.HTTPService"));
		return node;
	}
	
	@Test
	public void MXMLHTTPServiceNode_postRequest()
	{
		String[] code = new String[]
		{
		    "<mx:HTTPService id='userRequest' url='http://whatever' method='POST'>",
		    "    <mx:request xmlns=''>",
		    "        <username>{username.text}</username>",
            "        <emailaddress>{emailaddress.text}</emailaddress>",
		    "    </mx:request>",
		    "</mx:HTTPService>"
		};
		IMXMLHTTPServiceNode node = getMXMLHTTPServiceNode(code);
		assertThat("getChildCount", node.getChildCount(), is(3));
		IMXMLPropertySpecifierNode urlNode = (IMXMLPropertySpecifierNode)node.getChild(0);
		assertThat("", urlNode.getName(), is("url"));
		IMXMLPropertySpecifierNode methodNode = (IMXMLPropertySpecifierNode)node.getChild(1);
		assertThat("", methodNode.getName(), is("method"));
		IMXMLHTTPServiceRequestPropertyNode requestNode = (MXMLHTTPServiceRequestPropertyNode)node.getChild(2);
		assertThat("", requestNode.getName(), is("request"));
	}
}
