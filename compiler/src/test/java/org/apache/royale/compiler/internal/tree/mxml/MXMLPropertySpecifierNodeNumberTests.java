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
import org.apache.royale.compiler.tree.mxml.IMXMLNumberNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Ignore;
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
    
	@Ignore // 1 is causing MXMLIntNode instead of MXMLDoubleNode to get created
	@Test
	public void MXMLPropertySpecifierNode_Number_integer_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' 1 '/>"
		};
		testMXMLPropertySpecifierNode(code, 1);
	}
	
	@Ignore
	@Test
	public void MXMLPropertySpecifierNode_Number_integer_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> 1 </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, 1);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_integer_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> 1 </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, 1);
	}
	
	@Ignore
	@Test
	public void MXMLPropertySpecifierNode_Number_hexInteger_upperCase_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' 0xABCDEF '/>"
		};
		testMXMLPropertySpecifierNode(code, (double)0xABCDEF);
	}
	
	@Ignore
	@Test
	public void MXMLPropertySpecifierNode_Number_hexInteger_upperCase_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> 0xABCDEF </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, (double)0xABCDEF);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_hexInteger_upperCase_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> 0xABCDEF </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, (double)0xABCDEF);
	}
	
	@Ignore
	@Test
	public void MXMLPropertySpecifierNode_Number_hexInteger_lowerCase_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' -0Xabcdef '/>"
		};
		testMXMLPropertySpecifierNode(code, (double)-0xABCDEF);
	}
	
	@Ignore
	@Test
	public void MXMLPropertySpecifierNode_Number_hexInteger_lowerCase_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> -0Xabcdef </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, (double)-0xABCDEF);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_hexInteger_lowerCase_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> -0Xabcdef </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, (double)-0xABCDEF);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_fractional_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' 0.5 '/>"
		};
		testMXMLPropertySpecifierNode(code, 0.5);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_fractional_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> 0.5 </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, 0.5);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_fractional_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> 0.5 </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, 0.5);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_scientific_lowerCase_negExp_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' -1.5e-10 '/>"
		};
		testMXMLPropertySpecifierNode(code, -1.5e-10);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_scientific_lowerCase_negExp_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> -1.5e-10 </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, -1.5e-10);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_scientific_lowerCase_negExp_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> -1.5e-10 </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, -1.5e-10);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_scientific_upperCase_posExp_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' -1.5E+10 '/>"
		};
		testMXMLPropertySpecifierNode(code, -1.5e10);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_scientific_upperCase_posExp_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> -1.5E+10 </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, -1.5e10);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_scientific_upperCase_posExp_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> -1.5E+10 </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, -1.5e10);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_NaN_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' NaN '/>"
		};
		testMXMLPropertySpecifierNode(code, Double.NaN);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_NaN_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> NaN </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, Double.NaN);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_NaN_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> NaN </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, Double.NaN);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_posInfinity_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' Infinity '/>"
		};
		testMXMLPropertySpecifierNode(code, Double.POSITIVE_INFINITY);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_posInfinity_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> Infinity </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, Double.POSITIVE_INFINITY);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_posInfinity_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> Infinity </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, Double.POSITIVE_INFINITY);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_negInfinity_attribute()
	{
		String[] code = new String[]
		{
		    "<MyComp p=' -Infinity '/>"
		};
		testMXMLPropertySpecifierNode(code, Double.NEGATIVE_INFINITY);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_negInfinity_tag_text()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p> -Infinity </p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, Double.NEGATIVE_INFINITY);
	}
	
	@Test
	public void MXMLPropertySpecifierNode_Number_negInfinity_tag_tag()
	{
		String[] code = new String[]
		{
			"<MyComp>",
			"    <p><fx:Number> -Infinity </fx:Number></p>",
			"</MyComp>"
		};
		testMXMLPropertySpecifierNode(code, Double.NEGATIVE_INFINITY);
	}
}
