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

package org.apache.royale.compiler.internal.scopes;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.scopes.IDefinitionSet;

/**
 * An implementation of {@link IMutableDefinitionSet} set that can hold
 * only a small and fixed number -- currently 2 -- of definitions.
 * <p>
 * Note that this class simply stores its two definitions
 * in two fields and does not require a separate array.
 * <p>
 * 95% of definition sets contain only a single definition,
 * which is why {@link DefinitionBase} implements {@link IDefinitionSet}
 * to make a single definition act as a set of size 1.
 * Most of the other 5% of definition sets contain only two
 * definitions, typically for a getter/setter pair,
 * and can use {@link SmallDefinitionSet}; this class is for them.
 * Less than 1% of definitions sets contain more than 2 definitions
 * and must use {@link LargeDefinitionSet}.
 */
public class SmallDefinitionSet implements IMutableDefinitionSet
{
    private static final int MAX_SIZE = 2;
    
    /**
     * Constructor.
     */
    public SmallDefinitionSet(IDefinition definition0, IDefinition definition1)
    {
        assert definition0 != null && definition1 != null;
        this.definition0 = definition0;
        this.definition1 = definition1;
    }
    
    // Storage for two definitions.
    private IDefinition definition0;
    private IDefinition definition1;

    @Override
    public boolean isEmpty()
    {
        return definition0 == null && definition1 == null;
    }

    @Override
    public int getSize()
    {
        int size = 0;
        if (definition0 != null)
            size++;
        if (definition1 != null)
            size++;
        return size;
    }

    @Override
    public int getMaxSize()
    {
        return MAX_SIZE;
    }

    @Override
    public IDefinition getDefinition(int i)
    {
        if (i == 0)
            return definition0;
        else if (i == 1)
            return definition1;
        
        return null;
    }

    @Override
    public void addDefinition(IDefinition definition)
    {
        assert definition != null : "Can't add a null definition to a definition set";

        if (definition0 == null)
            definition0 = definition;
        else if (definition1 == null)
            definition1 = definition;
        else
            assert false : "SmallDefinitionSet is full";
    }

    @Override
    public void removeDefinition(IDefinition definition)
    {
        assert definition != null : "Can't remove a null definition from a definition set";

        if (definition1 == definition)
            definition1 = null;
        
        if (definition0 == definition)
        {
            // We're removing definition #0, so move definition #1 down to be definition #0.
            definition0 = definition1;
            definition1 = null;
        }
    }

    @Override
    public void replaceDefinition(int i, IDefinition newDefinition)
    {
        assert newDefinition != null : "Can't replace a definition in a definition set with null";
        
        if (i == 0)
            definition0 = newDefinition;
        else if (i == 1)
            definition1 = newDefinition;
        else
            assert false : "Invalid index " + i + " in SmallDefinitionSet";
    }

    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        if (definition0 != null)
            sb.append(definition0.toString());
        sb.append('\n');
        if (definition1 != null)
            sb.append(definition1.toString());
        return sb.toString();
    }
}
