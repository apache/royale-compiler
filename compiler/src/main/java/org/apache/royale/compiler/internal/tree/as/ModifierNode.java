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

package org.apache.royale.compiler.internal.tree.as;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IModifierNode;

public class ModifierNode extends FixedChildrenNode implements IModifierNode
{
    /**
     * Constructor.
     * 
     * @param keyword The token representing the modifier.
     */
    public ModifierNode(IASToken keyword)
    {
        if (keyword != null)
        {
            modifier = ASModifier.getASModifier(keyword.getText());
            span(keyword);
        }
    }

    /**
     * Constructor.
     * 
     * @param keyword The token representing the modifier.
     */
    public ModifierNode(String keyword)
    {
        modifier = ASModifier.getASModifier(keyword);
        setStart(-1);
        setEnd(-1);
    }

    private ASModifier modifier;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        if (modifier != null)
        {
            if (ASModifier.DYNAMIC == modifier)
                return ASTNodeID.DynamicID;

            else if (ASModifier.FINAL == modifier)
                return ASTNodeID.FinalID;

            else if (ASModifier.NATIVE == modifier)
                return ASTNodeID.NativeID;

            else if (ASModifier.OVERRIDE == modifier)
                return ASTNodeID.OverrideID;

            else if (ASModifier.STATIC == modifier)
                return ASTNodeID.StaticID;
        }

        return ASTNodeID.ModifierID;
    }
    
    @Override
    public void normalize(boolean fillInOffsets)
    {
        //do nothing
    }

    /*
     * For debugging only. Build a string such as <code>"public"</code> from the
     * modifier.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getModifierString());
        sb.append('"');

        return true;
    }

    //
    // IModifierNode implementations
    //

    @Override
    public ASModifier getModifier()
    {
        return modifier;
    }

    @Override
    public String getModifierString()
    {
        return modifier != null ? modifier.toString() : "";
    }
}
