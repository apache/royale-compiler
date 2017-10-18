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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Stores the set of dependency types for an edge in the dependency graph.
 * <p>
 * The APIs of this class are similar to that of <code>EnumSet&lt;DependencyType&gt;</code>.
 * Instead of using an <code>EnumSet</code>, we use this smaller
 * class which stores the dependencies as bitflags in a <code>int</code> field.
 * <p>
 * This class was originally designed to handle the fact that we shouldn't need
 * to keep track of each type of dependency on each edge: if an edge has a more
 * important dependency type we should be able to ignore the fact that it also has
 * a less important dependency type. The idea was that this would reduce the size
 * of the SWC catalog.
 * <p>
 * The <code>add()</code> was originally a bottleneck which added a dependency type
 * to the set, and then cleaned up the set by removing lesser dependency types that
 * are trumped by greater ones. (<code>SIGNATURE</code> trumped <code>EXPRESSION</code>;
 * <code>INHERITANCE</code> trumped all others.) But this meant that APIs like
 * <code>allOf()</code> didn't actually create a set that contained all the types,
 * Although everything seemed to work, it seemed too dangerous given the way that
 * sets are used for filtering of edges.
 * <p>Therefore that "trumping" logic has moved to the SWC catalog writer and this
 * class is merely a optimization.
 */
public final class DependencyTypeSet implements Iterable<DependencyType>
{
    private static DependencyType[] DEPENDENCY_TYPES = DependencyType.values();
    
    /**
     * Constructs a new empty set.
     * 
     * @return The new {@link DependencyTypeSet}.
     */
    public static DependencyTypeSet noneOf()
    {
        return new DependencyTypeSet();
    }
    
    /**
     * Constructs a new set with each type of dependency.
     * 
     * @return The new {@link DependencyTypeSet}.
     */
    public static DependencyTypeSet allOf()
    {
        DependencyTypeSet typeSet = new DependencyTypeSet();
        for (DependencyType type : DEPENDENCY_TYPES)
        {
            typeSet.add(type);
        }
        return typeSet;
    }
    
    /**
     * Constructs a new set with the specified dependency types in it.
     * 
     * @param types Zero or more parameters, each of which is a {@link DependencyType}.
     * @return The new {@link DependencyTypeSet}.
     */
    public static DependencyTypeSet of(DependencyType ... types)
    {
        DependencyTypeSet typeSet = new DependencyTypeSet();
        for (DependencyType type : types)
        {
            typeSet.add(type);
        }  
        return typeSet;
    }
    
    /**
     * Constructs a new set with the specified collection of dependency
     * types in it.
     * 
     * @param dependencyTypeCollection A collection of {@link DependencyType}s.
     * @return The new {@link DependencyTypeSet}.
     */
    public static DependencyTypeSet copyOf(Collection<DependencyType> dependencyTypeCollection)
    {
        DependencyTypeSet typeSet = new DependencyTypeSet();
        for (DependencyType type : dependencyTypeCollection)
        {
            typeSet.add(type);
        }
        return typeSet;
    }
    
    /**
     * Constructs a copy of another {@link DependencyTypeSet}.
     * 
     * @param otherTypeSet The {@link DependencyTypeSet} to copy.
     * @return The new {@link DependencyTypeSet}.
     */
    public static DependencyTypeSet copyOf(DependencyTypeSet otherTypeSet)
    {
        DependencyTypeSet typeSet = new DependencyTypeSet();
        typeSet.flags = otherTypeSet.flags;
        return typeSet;
    }
    
    /**
     * Private constructor.
     * <p>
     * Use one of the static method to construct an instance.
     */
    private DependencyTypeSet()
    {
    }
            
    // The lowest four bits of this field are flags for INHERITANCE (0),
    // SIGNATURE (1), NAMESPACE (2), and EXPRESSION (4).
    // The bit position is the ordinal of the enum.
    private int flags;
    
    @Override
    public Iterator<DependencyType> iterator()
    {
        return new Iterator<DependencyType>()
        {
            // Bit position of the DependencyType flag that next()
            // last returned.
            private int i = -1;
            
            @Override
            public boolean hasNext()
            {
                // Increment i until it points at a bit which is set
                // or until we're past all the flag bits.
                int bit = getNextBit(i + 1);
                return bit < DEPENDENCY_TYPES.length;
            }

            @Override
            public DependencyType next()
            {
                i = getNextBit(i + 1);
                if (i < DEPENDENCY_TYPES.length)
                    return DEPENDENCY_TYPES[i];
                
                throw new NoSuchElementException();
            }
               
            private int getNextBit(int startingBit)
            {
                int nextBit = startingBit;
                while (nextBit < DEPENDENCY_TYPES.length)
                {
                    int mask = 1 << nextBit;
                    if ((flags & mask) != 0)
                        break;
                    nextBit++;
                }

                return nextBit;
            }
            
            @Override
            public void remove()
            {
                assert false: "This should never get called";
            }
        };
    }
    
    /**
     * Determines if the set is empty.
     * 
     * @return <code>true</code> if the set is empty.
     */
    public boolean isEmpty()
    {
        return flags == 0;
    }
    
    /**
     * Adds a dependency type to the set.

     * @param dependencyType The {@link DependencyType} to be
     * added to the set.
     */
    public void add(DependencyType dependencyType)
    {
        int mask = 1 << dependencyType.ordinal();
        flags |= mask;
    }
    
    /**
     * Adds the dependencies in another set to this set.
     * 
     * @param otherSet The other {@link DependencyTypeSet}.
     */
    public void addAll(DependencyTypeSet otherSet)
    {
        for (DependencyType type : otherSet)
        {
            add(type);
        }
    }        
    
    /**
     * Determines whether this set contains the specified
     * dependency type.

     * @param dependencyType A {@link DependencyType}.
     * @return <code>true</code> if this set contains
     * that dependency type.
     */
    public boolean contains(DependencyType dependencyType)
    {
        int mask = 1 << dependencyType.ordinal();
        return (flags & mask) != 0;
    }
    
    /**
     * For debugging only.
     * <p>
     * @return A String like <code>"[ NAMESPACE EXPRESSION ]"</code>
     * representing the dependency types in this set.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(' ');
        for (DependencyType type : DependencyType.values())
        {
            if (contains(type))
            {
                sb.append(type.name());
                sb.append(' ');
            }
        }
        sb.append(']');
        return sb.toString();
    }
}
