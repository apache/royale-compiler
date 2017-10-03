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

import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLApplicationNode;

import static org.apache.royale.compiler.mxml.IMXMLLanguageConstants.*;

/**
 * {@code MXMLDocumentNode} represents the root MXML tag in an MXML document.
 */
class MXMLApplicationNode extends MXMLDocumentNode implements IMXMLApplicationNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLApplicationNode(NodeBase parent)
    {
        super(parent);
    }

    private int frameRate;

    private String pageTitle;

    private int scriptRecursionLimit;

    private int scriptTimeLimit;

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, IMXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        String value = attribute.getRawValue();

        if (attribute.isSpecialAttribute(ATTRIBUTE_FRAME_RATE))
        {
            frameRate = Integer.parseInt(value);
            // TODO Report problem if it couldn't be parsed as an integer
        }
        else if (attribute.isSpecialAttribute(ATTRIBUTE_PAGE_TITLE))
        {
            pageTitle = value;
        }
        else if (attribute.isSpecialAttribute(ATTRIBUTE_SCRIPT_RECURSION_LIMIT))
        {
            scriptRecursionLimit = Integer.parseInt(value);
            // TODO Report problem if it couldn't be parsed as an integer
        }
        else if (attribute.isSpecialAttribute(ATTRIBUTE_SCRIPT_TIME_LIMIT))
        {
            scriptRecursionLimit = Integer.parseInt(value);
            // TODO Report problem if it couldn't be parsed as an integer
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLApplicationID;
    }

    @Override
    public int getFrameRate()
    {
        return frameRate;
    }

    @Override
    public String getPageTitle()
    {
        return pageTitle;
    }

    @Override
    public int getScriptRecursionLimit()
    {
        return scriptRecursionLimit;
    }

    @Override
    public int getScriptTimeLimit()
    {
        return scriptTimeLimit;
    }
}
