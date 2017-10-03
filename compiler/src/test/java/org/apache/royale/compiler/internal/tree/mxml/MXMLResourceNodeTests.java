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

import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLResourceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLResourceNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLResourceNodeTests extends MXMLInstanceNodeTests
{
	private IMXMLResourceNode getMXMLResourceNode(String[] code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLResourceNode node = (IMXMLResourceNode)findFirstDescendantOfType(fileNode, IMXMLResourceNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLResourceID));
		assertThat("getName", node.getName(), is("Resource"));
		assertThat("getChildCount", node.getChildCount(), is(0));
		return node;
	}
	
	@Test
	public void MXMLResourceNode_Boolean()
	{
		String[] code = new String[]
		{
			"<fx:Boolean> @Resource(bundle='b1', key='k1') </fx:Boolean>"
		};
		IMXMLResourceNode node = getMXMLResourceNode(code);
		assertThat("getBundleName", node.getBundleName(), is("b1"));
		assertThat("getKey", node.getKey(), is("k1"));
		assertThat("getType", node.getType(), is(project.getBuiltinType(BuiltinType.BOOLEAN)));
	}
	
	@Test
	public void MXMLResourceNode_int()
	{
		String[] code = new String[]
		{
			"<fx:int> @Resource(bundle='b1', key='k1') </fx:int>"
		};
		IMXMLResourceNode node = getMXMLResourceNode(code);
		assertThat("getBundleName", node.getBundleName(), is("b1"));
		assertThat("getKey", node.getKey(), is("k1"));
		assertThat("getType", node.getType(), is(project.getBuiltinType(BuiltinType.INT)));
	}
	
	@Test
	public void MXMLResourceNode_uint()
	{
		String[] code = new String[]
		{
			"<fx:uint> @Resource(bundle='b1', key='k1') </fx:uint>"
		};
		IMXMLResourceNode node = getMXMLResourceNode(code);
		assertThat("getBundleName", node.getBundleName(), is("b1"));
		assertThat("getKey", node.getKey(), is("k1"));
		assertThat("getType", node.getType(), is(project.getBuiltinType(BuiltinType.UINT)));
	}
	
	@Test
	public void MXMLResourceNode_Number()
	{
		String[] code = new String[]
		{
			"<fx:Number> @Resource(bundle='b1', key='k1') </fx:Number>"
		};
		IMXMLResourceNode node = getMXMLResourceNode(code);
		assertThat("getBundleName", node.getBundleName(), is("b1"));
		assertThat("getKey", node.getKey(), is("k1"));
		assertThat("getType", node.getType(), is(project.getBuiltinType(BuiltinType.NUMBER)));
	}
	
	@Test
	public void MXMLResourceNode_String()
	{
		String[] code = new String[]
		{
			"<fx:String> @Resource(bundle='b1', key='k1') </fx:String>"
		};
		IMXMLResourceNode node = getMXMLResourceNode(code);
		assertThat("getBundleName", node.getBundleName(), is("b1"));
		assertThat("getKey", node.getKey(), is("k1"));
		assertThat("getType", node.getType(), is(project.getBuiltinType(BuiltinType.STRING)));
	}
	
	@Ignore
	@Test
	public void MXMLResourceNode_Class()
	{
		String[] code = new String[]
		{
			"<fx:Class> @Resource(bundle='b1', key='k1') </fx:Class>"
		};
		IMXMLResourceNode node = getMXMLResourceNode(code);
		assertThat("getBundleName", node.getBundleName(), is("b1"));
		assertThat("getKey", node.getKey(), is("k1"));
		assertThat("getType", node.getType(), is(project.getBuiltinType(BuiltinType.CLASS)));
	}
}
