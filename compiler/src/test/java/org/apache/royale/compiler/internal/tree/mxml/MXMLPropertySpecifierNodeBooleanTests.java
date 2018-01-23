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
import org.apache.royale.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLPropertyNode} for a property of type <code>Boolean</code>.
 * 
 * @author Gordon Smith
 */
public class MXMLPropertySpecifierNodeBooleanTests extends MXMLPropertySpecifierNodeTests
{     
    @Override
    protected String getPropertyType()
    {
        return "Boolean";
    }
    
	protected IMXMLPropertySpecifierNode testMXMLPropertySpecifierNode(String[] code, boolean value)
	{
		IMXMLPropertySpecifierNode node = getMXMLPropertySpecifierNode(code);
		assertThat("getInstanceNode.getNodeID", node.getInstanceNode().getNodeID(), is(ASTNodeID.MXMLBooleanID));
        assertThat("getInstanceNode.getValue", ((IMXMLBooleanNode)node.getInstanceNode()).getValue(), is(true));
		return node;
	}
    
    @Test
    public void MXMLPropertySpecifierNode_Boolean_attribute_true()
    {
        String[] code = new String[]
        {
            "<MyComp p=' true '/>"
        };
        testMXMLPropertySpecifierNode(code, true);
    }
    
    @Test
    public void MXMLPropertySpecifierNode_Boolean_tag_text_true()
    {
        String[] code = new String[]
        {
            "<MyComp>",
            "    <p> true </p>",
            "</MyComp>"
        };
        testMXMLPropertySpecifierNode(code, true);
    }
    
    @Test
    public void MXMLPropertySpecifierNode_Boolean_tag_tag_true()
    {
        String[] code = new String[]
        {
            "<MyComp>",
            "    <p><fx:Boolean> true </fx:Boolean></p>",
            "</MyComp>"
        };
        testMXMLPropertySpecifierNode(code, true);
    }
}
