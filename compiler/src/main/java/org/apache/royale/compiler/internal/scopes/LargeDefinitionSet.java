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

import java.util.ArrayList;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.scopes.IDefinitionSet;

/**
 * An implementation of {@link IMutableDefinitionSet}
 * that can hold an arbitrary number of definitions.
 * It simply extends <code>ArrayList&lt;IDefinition&gt;</code>.
 * <p>
 * 95% of definition sets contain only a single definition,
 * which is why {@link DefinitionBase} implements {@link IDefinitionSet}
 * to make a single definition act as a set of size 1.
 * Most of the other 5% of definition sets contain only two
 * definitions, typically for a getter/setter pair;
 * {@link SmallDefinitionSet} is for them.
 * Less than 1% of definitions sets contain more than 2 definitions
 * and must use this class.
 */
@SuppressWarnings("serial")
public class LargeDefinitionSet extends ArrayList<IDefinition> implements IMutableDefinitionSet
{
    /**
     * Constructor
     */
    public LargeDefinitionSet()
    {
        super(3);
    }
    
    /**
     * Copy constructor.
     */
    public LargeDefinitionSet(IDefinitionSet other)
    {
        this();
        
        int n = other.getSize();
        for (int i = 0; i < n; i++)
        {
            addDefinition(other.getDefinition(i));
        }
    }

    @Override
    public int getSize()
    {
        return size();
    }

    @Override
    public int getMaxSize()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public IDefinition getDefinition(int i)
    {
        return get(i);
    }
    
    @Override
    public void addDefinition(IDefinition definition)
    {
        add(definition);
    }

    @Override
    public void removeDefinition(IDefinition definition)
    {
        remove(definition);
    }

    @Override
    public void replaceDefinition(int i, IDefinition newDefinition)
    {
        set(i, newDefinition);
    }
}
