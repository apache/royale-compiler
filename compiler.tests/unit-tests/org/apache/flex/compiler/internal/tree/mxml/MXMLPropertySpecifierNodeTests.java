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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.flex.utils.StringUtils;
import org.junit.Ignore;

/**
 * Abstract base class for JUnit tests for {@link MXMLPropertyNode} for properties of various types.
 * 
 * @author Gordon Smith
 */
@Ignore
public class MXMLPropertySpecifierNodeTests extends MXMLSpecifierNodeBaseTests
{	
	/**
	 * Property-node tests set properties on a <MyComp> tag which has a property named p of some type.
	 * This method combines various code snippets to make a complete one-file MXML Sprite-based application.
	 */
    protected String getMXML(String propertyType, String[] code)
    {
        String[] template = new String[]
        {
    	    "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009'",
    	    "          xmlns:d='flash.display.*'",
    	    "          xmlns='*'>",
    	    "    <fx:Declarations>",
    		"        <fx:Component className='MyComp'>",
    		"            <d:Sprite>",
    	    "                <fx:Script>",
    		"                    public var p:%1;",
    	    "                </fx:Script>",
    		"            </d:Sprite>",
    		"        </fx:Component>",
    		"        %2",
    	    "    </fx:Declarations>",
    	    "</d:Sprite>"
        };
        String mxml = StringUtils.join(template, "\n");
        mxml = mxml.replace("%1", propertyType);
        mxml = mxml.replace("%2", StringUtils.join(code, "\n        "));
        return mxml;
    }
    
	protected IMXMLPropertySpecifierNode getMXMLPropertySpecifierNode(String[] code)
	{
		String propertyType = getPropertyType();
		String mxml = getMXML(propertyType, code);
		IMXMLFileNode fileNode = getMXMLFileNode(mxml);
		IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode)findFirstDescendantOfType(fileNode, IMXMLPropertySpecifierNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLPropertySpecifierID));
		assertThat("getName", node.getName(), is("p"));
		assertThat("getChildCount", node.getChildCount(), is(1));
		assertThat("getInstanceNode", node.getInstanceNode(), is(node.getChild(0)));
		return node;
	}
	
	protected String getPropertyType()
	{
		return "";
	}
}
