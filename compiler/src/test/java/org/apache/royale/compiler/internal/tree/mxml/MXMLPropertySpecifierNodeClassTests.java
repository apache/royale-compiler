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
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLPropertyNode} for a property of type <code>Class</code>.
 * 
 * @author Gordon Smith
 */
public class MXMLPropertySpecifierNodeClassTests extends MXMLPropertySpecifierNodeTests
{	  
	@Override
	protected String getPropertyType()
	{
		return "Class";
	}
	
	protected IMXMLPropertySpecifierNode testMXMLPropertySpecifierNode(String[] code, String qname)
	{
		IMXMLPropertySpecifierNode node = getMXMLPropertySpecifierNode(code);
		assertThat("getInstanceNode.getNodeID", node.getInstanceNode().getNodeID(), is(ASTNodeID.MXMLClassID));
		assertThat("getInstanceNode.getValue.getQualifiedName", ((IMXMLClassNode)node.getInstanceNode()).getValue(project).getQualifiedName(), is(qname));
		return node;
	}
    
	@Test
	public void MXMLPropertySpecifierNode_Class_shortName_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' TestInstance '/>"
		};
		testMXMLPropertySpecifierNode(code, "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Class_shortName_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> TestInstance </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Class_shortName_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Class> TestInstance </fx:Class></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Class_qualifiedName_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' custom.TestInstance '/>"
		};
		testMXMLPropertySpecifierNode(code, "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Class_qualifiedName_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> custom.TestInstance </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, "custom.TestInstance");
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Class_qualifiedName_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Class> custom.TestInstance </fx:Class></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, "custom.TestInstance");
	}
}
