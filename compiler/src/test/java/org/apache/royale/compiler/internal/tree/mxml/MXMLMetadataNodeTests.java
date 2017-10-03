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
import org.apache.royale.compiler.tree.metadata.IEventTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLMetadataNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLMetadataNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLMetadataNodeTests extends MXMLNodeBaseTests
{
	private IMXMLMetadataNode getMXMLMetadataNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLMetadataNode node = (IMXMLMetadataNode)findFirstDescendantOfType(fileNode, IMXMLMetadataNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLMetadataID));
		assertThat("getName", node.getName(), is("Metadata"));
		return node;
	}
	
	@Test
	public void MXMLMetadataNode_empty1()
	{
		String[] code = new String[]
		{
			"<fx:Metadata/>"
		};
		IMXMLMetadataNode node = getMXMLMetadataNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLMetadataNode_empty2()
	{
		String[] code = new String[]
		{
			"<fx:Metadata></fx:Metadata>"
		};
		IMXMLMetadataNode node = getMXMLMetadataNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLMetadataNode_empty3()
	{
		String[] code = new String[]
		{
			"<fx:Metadata/> \t\r\n<fx:Metadata/>"
		};
		IMXMLMetadataNode node = getMXMLMetadataNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLMetadataNode_two_events()
	{
		String[] code = new String[]
		{
			"<fx:Metadata>",
			"    [Event(name='mouseDown', type='mx.events.MouseEvent')]",
			"    [Event(name='mouseUp', type='mx.events.MouseEvent')]",
			"</fx:Metadata>"
		};
		IMXMLMetadataNode node = getMXMLMetadataNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		IMetaTagNode[] metaTagNodes = node.getMetaTagNodes();
		assertThat("event 0", ((IEventTagNode)metaTagNodes[0]).getName(), is("mouseDown"));
		assertThat("event 1", ((IEventTagNode)metaTagNodes[1]).getName(), is("mouseUp"));
	}
}
