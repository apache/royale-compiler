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
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLImplementsNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.utils.StringUtils;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLImplementsNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLImplementsNodeTests extends MXMLNodeBaseTests
{
	@Override
 	protected String[] getTemplate()
	{
 		// Tests of nodes for class-definition-level tags like <Declarations>,
 		// <Library>,  <Metadata>, <Script>, and <Style> use this document template.
 		// Tests for nodes produced by tags that appear at other locations
 		// override getTemplate() and getMXML().
		return new String[] 
		{
			"<fx:Object xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test'",
		    "          %1 >",
			"</fx:Object>"
		};
    };
	
	protected String getMXML(String[] code)
    {
        String mxml = StringUtils.join(getTemplate(), "\n");
        mxml = mxml.replace("%1", StringUtils.join(code, ", "));
        return mxml;
    }
	    
	private IMXMLImplementsNode getMXMLImplementsNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLImplementsNode node = (IMXMLImplementsNode)findFirstDescendantOfType(fileNode, IMXMLImplementsNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLImplementsID));
		assertThat("getName", node.getName(), is("implements"));
		return node;
	}
	
	@Test
	public void MXMLImplementsNode_oneInterfaceWithSimpleName()
	{
		String[] code = new String[]
		{
			"implements=' I1 '"
		};
		IMXMLImplementsNode node = getMXMLImplementsNode(code);
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getInterfaceNodes.length", node.getInterfaceNodes().length, is(1));
		IIdentifierNode interfaceNode0 = node.getInterfaceNodes()[0];
		assertThat("interfaceNode0", interfaceNode0, is(node.getChild(0)));
		assertThat("interfaceNode0.getNodeID", interfaceNode0.getNodeID(), is(ASTNodeID.IdentifierID));
		assertThat("interfaceNode0.getName", interfaceNode0.getName(), is("I1"));
	}
	
	@Test
	public void MXMLImplementsNode_twoInterfacesWithQNames()
	{
		String[] code = new String[]
		{
			"implements=' a.b.I1 , c.d.I2 '"
		};
		IMXMLImplementsNode node = getMXMLImplementsNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("getInterfaceNodes.length", node.getInterfaceNodes().length, is(2));
		IIdentifierNode interfaceNode0 = node.getInterfaceNodes()[0];
		assertThat("interfaceNode0", interfaceNode0, is(node.getChild(0)));
		assertThat("interfaceNode0.getNodeID", interfaceNode0.getNodeID(), is(ASTNodeID.FullNameID));
		assertThat("interfaceNode0.getName", interfaceNode0.getName(), is("a.b.I1"));
		IIdentifierNode interfaceNode1 = node.getInterfaceNodes()[1];
		assertThat("interfaceNode1", interfaceNode1, is(node.getChild(1)));
		assertThat("interfaceNode1.getNodeID", interfaceNode1.getNodeID(), is(ASTNodeID.FullNameID));
		assertThat("interfaceNode1.getName", interfaceNode1.getName(), is("c.d.I2"));
	}
}
