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

import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLClassNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFunctionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLPropertyNode} for a property of type <code>Function</code>.
 */
public class MXMLPropertySpecifierNodeFunctionTests extends MXMLPropertySpecifierNodeTests
{	  
	@Override
	protected String getPropertyType()
	{
		return "Function";
	}
	
	protected IMXMLPropertySpecifierNode testMXMLPropertySpecifierNode(String[] code, String functionBaseName, String parentQname)
	{
		IMXMLPropertySpecifierNode node = getMXMLPropertySpecifierNode(code);
		assertThat("getInstanceNode.getNodeID", node.getInstanceNode().getNodeID(), is(ASTNodeID.MXMLFunctionID));
		assertThat("getInstanceNode.getValue.getBaseName", ((IMXMLFunctionNode)node.getInstanceNode()).getValue(project).getBaseName(), is(functionBaseName));
		assertThat("getInstanceNode.getValue.getParent.getQualifiedName", ((IMXMLFunctionNode)node.getInstanceNode()).getValue(project).getParent().getQualifiedName(), is(parentQname));
		return node;
	}
    
	@Test
	public void MXMLPropertySpecifierNode_Function_shortName_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' TestInstance.anotherFunction '/>"
		};
		testMXMLPropertySpecifierNode(code, "anotherFunction", "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Function_shortName_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> TestInstance.anotherFunction </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, "anotherFunction", "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Function_shortName_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Function> TestInstance.anotherFunction </fx:Function></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, "anotherFunction", "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Function_qualifiedName_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' custom.TestInstance.anotherFunction '/>"
		};
		testMXMLPropertySpecifierNode(code, "anotherFunction", "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Function_qualifiedName_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> custom.TestInstance.anotherFunction </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, "anotherFunction", "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Function_qualifiedName_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Function> custom.TestInstance.anotherFunction </fx:Function></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, "anotherFunction", "custom.TestInstance");
	}
}
