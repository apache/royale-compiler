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

package org.apache.flex.compiler.internal.tree.mxml;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLPropertyNode} for a property of type <code>Number</code>.
 * 
 * @author Gordon Smith
 */
public class MXMLPropertySpecifierNodeNumberTests extends MXMLPropertySpecifierNodeTests
{	  
	@Override
	protected String getPropertyType()
	{
		return "Number";
	}
	
	protected IMXMLPropertySpecifierNode testMXMLPropertySpecifierNode(String[] code, double value)
	{
		IMXMLPropertySpecifierNode node = getMXMLPropertySpecifierNode(code);
		assertThat("getInstanceNode.getNodeID", node.getInstanceNode().getNodeID(), is(ASTNodeID.MXMLNumberID));
		assertThat("getInstanceNode.getValue", ((IMXMLNumberNode)node.getInstanceNode()).getValue(), is(value));
		return node;
	}
    
	@Test
	public void MXMLPropertySpecifierNode_Number_attribute_fractional()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' -1.5 '>"
		};
		testMXMLPropertySpecifierNode(code, -1.5);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_tag_text_fractional()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> -1.5 </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, -1.5);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_tag_tag_fractional()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> -1.5 </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, -1.5);
	}
}
