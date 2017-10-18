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

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLEffectSpecifierNode;

/**
 * This class implements the {@link IMXMLEffectSpecifierNode} interface.
 */
class MXMLEffectSpecifierNode extends MXMLStyleSpecifierNode implements IMXMLEffectSpecifierNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLEffectSpecifierNode(NodeBase parent)
    {
        super(parent);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLEffectSpecifierID;
    }

    @Override
    protected String getPropertyTypeName(MXMLTreeBuilder builder)
    {
        // This makes an attribute like showEffect="Wipe" build
        // MXMLEffectSpecifierNode "showEffect"
        //   MXMLStringNode
        //     LiteralNode "Wipe"
        return IASLanguageConstants.String;
    }
}
