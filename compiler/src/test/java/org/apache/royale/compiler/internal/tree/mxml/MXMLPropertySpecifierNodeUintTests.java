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
import org.apache.royale.compiler.tree.mxml.IMXMLUintNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLPropertyNode} for a property of type <code>uint</code>.
 * 
 * @author Gordon Smith
 */
public class MXMLPropertySpecifierNodeUintTests extends MXMLPropertySpecifierNodeTests
{     
    @Override
    protected String getPropertyType()
    {
        return "uint";
    }
    
    protected IMXMLPropertySpecifierNode testMXMLPropertySpecifierNode(String[] code, long value)
    {
        IMXMLPropertySpecifierNode node = getMXMLPropertySpecifierNode(code);
        assertThat("getInstanceNode.getNodeID", node.getInstanceNode().getNodeID(), is(ASTNodeID.MXMLUintID));
        assertThat("getInstanceNode.getValue", ((IMXMLUintNode)node.getInstanceNode()).getValue(), is(value));
        return node;
    }
    
    @Test
    public void MXMLPropertySpecifierNode_uint_attribute_max()
    {
        String[] code = new String[]
        {
            "<MyComp p=' 4294967295 '/>"
        };
        testMXMLPropertySpecifierNode(code, 4294967295L);
    }
    
    @Test
    public void MXMLPropertySpecifierNode_uint_tag_text_max()
    {
        String[] code = new String[]
        {
            "<MyComp>",
            "    <p> 4294967295 </p>",
            "</MyComp>"
        };
        testMXMLPropertySpecifierNode(code, 4294967295L);
    }
    
    @Test
    public void MXMLPropertySpecifierNode_uint_tag_tag_max()
    {
        String[] code = new String[]
        {
            "<MyComp>",
            "    <p><fx:uint> 4294967295 </fx:uint></p>",
            "</MyComp>"
        };
        testMXMLPropertySpecifierNode(code, 4294967295L);
    }
}
