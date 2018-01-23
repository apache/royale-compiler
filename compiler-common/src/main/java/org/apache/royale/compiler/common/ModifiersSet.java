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

package org.apache.royale.compiler.common;

import java.util.ArrayList;

import org.apache.royale.compiler.tree.as.IModifierNode;

/**
 * Map for holding a set of modifiers
 */
public class ModifiersSet
{
    public ModifiersSet()
    {
    }

    private short bits = 0;

    private int start = -1;

    /**
     * Add another modifier keyword to the collection (and adjust the span
     * accordingly)
     * 
     * @param modifier token holding modifier keyword
     */
    public void addModifier(ASModifier modifier)
    {
        bits |= modifier.getMaskValue();
    }

    /**
     * Add another modifier keyword to the collection (and adjust the span
     * accordingly)
     * 
     * @param modifier token holding modifier keyword
     */
    public void addModifier(IModifierNode modifier)
    {
        if (modifier.getModifier() != null)
        {
            bits |= modifier.getModifier().getMaskValue();
            if ((start == -1 || modifier.getAbsoluteStart() < start) && modifier.getAbsoluteStart() > 0)
                start = modifier.getAbsoluteStart();
        }

    }

    public int getStart()
    {
        return start;
    }

    public boolean hasModifiers()
    {
        return Integer.bitCount(bits) > 0;
    }

    public ASModifier[] getAllModifiers()
    {
        ArrayList<ASModifier> retVal = new ArrayList<ASModifier>(ASModifier.MODIFIERS.length);
        if (hasModifiers())
        {
            for (int i = 0; i < ASModifier.MODIFIERS.length; i++)
            {
                if ((bits & ASModifier.MODIFIERS[i].getMaskValue()) == ASModifier.MODIFIERS[i].getMaskValue())
                {
                    retVal.add(ASModifier.MODIFIERS[i]);
                }
            }
        }
        return retVal.toArray(new ASModifier[0]);
    }

    /**
     * Is the given modifier in the set?
     * 
     * @return true if the modifier is included in the set
     */
    public boolean hasModifier(ASModifier modifier)
    {
        return (bits & modifier.getMaskValue()) == modifier.getMaskValue();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ModifiersSet))
            return false;

        return bits == ((ModifiersSet)obj).bits && start == ((ModifiersSet)obj).start;
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (ASModifier modifier : getAllModifiers())
        {
            sb.append(modifier.toString());
            sb.append(' ');
        }
        return sb.toString();
    }
}
