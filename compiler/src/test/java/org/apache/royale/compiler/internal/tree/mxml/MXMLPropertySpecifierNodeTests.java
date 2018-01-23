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
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.utils.StringUtils;
import org.junit.Ignore;

/**
 * Abstract base class for JUnit tests for {@link MXMLPropertyNode} for properties of various types.
 * 
 * @author Gordon Smith
 */
@Ignore
public abstract class MXMLPropertySpecifierNodeTests extends MXMLSpecifierNodeBaseTests
{	
	@Override
    protected String[] getTemplate()
    {
   	    // Property-node tests use this template, which declares a component
		// with a property of a particular type. The tests then set the
		// property on a <MyComp> tag inside the <Declarations> tag.
        return new String[]
        {
    	    "<fx:Object xmlns:fx='http://ns.adobe.com/mxml/2009'",
    	    "          xmlns:custom='library://ns.apache.org/royale/test'",
    	    "          xmlns='*'>",
    	    "    <fx:Declarations>",
    		"        <fx:Component className='MyComp'>",
    		"            <custom:TestInstance>",
    	    "                <fx:Script>",
    		"                    public var p:%1;",
    	    "                </fx:Script>",
    		"            </custom:TestInstance>",
    		"        </fx:Component>",
    		"        %2",
    	    "    </fx:Declarations>",
    	    "    <fx:Script><![CDATA[",
    	    "        import custom.TestInstance;",
    	    "    ]]></fx:Script>",
    	    "</fx:Object>"
        };
    }
    
	@Override
    protected String getMXML(String[] code)
    {
        String mxml = StringUtils.join(getTemplate(), "\n");
        mxml = mxml.replace("%1", getPropertyType());
        mxml = mxml.replace("%2", StringUtils.join(code, "\n        "));
        return mxml;
    }
	
	abstract protected String getPropertyType();
    
	protected IMXMLPropertySpecifierNode getMXMLPropertySpecifierNode(String[] code)
	{
		String mxml = getMXML(code);
		IMXMLFileNode fileNode = getMXMLFileNode(mxml);
		IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode)findFirstDescendantOfType(fileNode, IMXMLPropertySpecifierNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLPropertySpecifierID));
		assertThat("getName", node.getName(), is("p"));
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getInstanceNode", node.getInstanceNode(), is(node.getChild(0)));
		return node;
	}
}
