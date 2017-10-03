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
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDesignLayerNode;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLSpecifierNode;
import com.google.common.base.Strings;

/**
 * Implementation of the {@link IMXMLDesignLayerNode} interface.
 */
class MXMLDesignLayerNode extends MXMLInstanceNode implements IMXMLDesignLayerNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLDesignLayerNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLDesignLayerID;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.DESIGN_LAYER;
    }

    @Override
    public int getHoistedChildCount()
    {
        int result = 0;
        for (int i = 0; i < getChildCount(); i++)
        {
            final IASNode child = getChild(i);

            // Do not count specifier nodes.
            if (child instanceof IMXMLSpecifierNode)
                continue;

            if (child instanceof IMXMLDesignLayerNode)
            {
                final IMXMLDesignLayerNode designLayerNode = (IMXMLDesignLayerNode)child;
                result += designLayerNode.getHoistedChildCount();
            }
            else if (child instanceof IMXMLInstanceNode)
            {
                result++;
            }
            else
            {
                throw new IllegalStateException(child.getNodeID() + " is unexpected child of DesignLayer.");
            }
        }
        return result;
    }

    @Override
    public boolean skipCodeGeneration()
    {
        final IMXMLPropertySpecifierNode[] propertySpecifierNodes = getPropertySpecifierNodes();
        final boolean hasNoPropertySpecifiers = propertySpecifierNodes == null || propertySpecifierNodes.length == 0;
        final boolean hasNoID = Strings.isNullOrEmpty(getID());
        return hasNoPropertySpecifiers && hasNoID;
    }
}
