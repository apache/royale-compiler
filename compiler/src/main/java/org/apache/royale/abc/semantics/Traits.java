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

package org.apache.royale.abc.semantics;

import java.util.Iterator;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

/**
 * A representation of an ABC entity's <a href="http://learn.adobe.com/wiki/display/AVM2/4.8+Trait">traits</a>.
 */
public class Traits implements Iterable<Trait>
{
    /**
     * The trait's elements.
     */
    private Vector<Trait> elements = new Vector<Trait>();

    /**
     * Map to store the various TRAIT_Kinds that a Name in this Trait structure
     * holds
     */
    private Map<Name, Integer> nameToKinds = new HashMap<Name, Integer>();

    /**
     * @return the number of Trait objects in this Traits.
     */
    public int getTraitCount()
    {
        return elements.size();
    }

    /**
     * @return an Iterator over the traits.
     */
    public Iterator<Trait> iterator()
    {
        return elements.iterator();
    }

    /**
     * Add a trait.
     * 
     * @param t - the trait to add.
     */
    public boolean add(Trait t)
    {
        Integer i = nameToKinds.get(t.getName());
        int flags = i != null ? i : 0;
        flags |= getBitMaskForKind(t.getKind());
        nameToKinds.put(t.getName(), flags);
        return elements.add(t);
    }

    /**
     * Does this Traits collection contain the specified element?
     * 
     * @param kind - the low nibble of a kind byte.
     * @param name - the desired Name.
     * @return true if this collection has a trait with the specified kind (low
     * nibble of kind byte) and name.
     */
    public boolean containsTrait(int kind, Name name)
    {
        Integer i = nameToKinds.get(name);
        if (i != null)
        {
            int flags = i;
            if ((flags & getBitMaskForKind(kind)) != 0)
                return true;
        }
        return false;

    }

    /**
     * Get an in that can be used as a bitmask to represent one of the
     * TRAIT_Kinds The TRAIT_Kinds go from 0 to 6, so we can just left shift by
     * the kind to get bitmasks that won't collide with each other
     * 
     * @param kind the Trait kind
     * @return an int that can be used as a bitmask to store the flag in a
     * packed int.
     */
    private final int getBitMaskForKind(int kind)
    {
        return (1 << kind);
    }
}
