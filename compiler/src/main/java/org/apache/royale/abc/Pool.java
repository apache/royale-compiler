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

package org.apache.royale.abc;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Abstract representation of an ABC pool.
 * 
 * @param <T> the type of the Pool's elements. T must implement reasonable
 * hashCode and equals methods. Notably, if it uses identity semantics then you
 * will probably end up with duplicate entries in the constant pool.
 */
public final class Pool<T>
{
    /**
     * Construct a new Pool.
     * 
     * @param default_type - one of HasDefaultZero or NoDefaultZero; the Pool
     * will have a default meaning for its 0th entry if HasDefaultZero is passed
     * in.
     */
    public Pool(DefaultType default_type)
    {
        this.hasDefaultZero = default_type == DefaultType.HasDefaultZero;
    }

    /**
     * The pool's elements mapped to their positions for quicker lookup.
     */
    final Map<T, Integer> refs = new HashMap<T, Integer>();

    /**
     * The Pool's elements in entry order.
     */
    final ArrayList<T> values = new ArrayList<T>();

    /**
     * When set, the pool has a default meaning for its 0th element (which is
     * not present in the pool).
     */
    final boolean hasDefaultZero;

    /**
     * A type-safe flag callers pass to the constructor to indicate whether or
     * not the Pool has a default zero entry.
     */
    public enum DefaultType
    {
        HasDefaultZero, NoDefaultZero
    };

    /**
     * Add an element to the pool if it's not already present.
     * 
     * @param e - the element to add.
     * @return the element's position in the pool.
     */
    public int add(T e)
    {
        int result;

        if (null == e)
        {
            if (this.hasDefaultZero)
                return 0;
            else
                throw new NullPointerException();
        }
        else
        {
            Integer cached_value = refs.get(e);

            if (cached_value != null)
            {
                result = cached_value;
            }
            else
            {
                values.add(e);
                result = size();
                refs.put(e, result);
            }
        }

        return result;
    }

    /**
     * @return the pool's elements in entry order.
     */
    public ArrayList<T> getValues()
    {
        return values;
    }

    /**
     * @param e - the element of interest.
     * @return the element's position in the pool.
     * @throws IllegalArgumentException if the element isn't in the pool.
     */
    public int id(T e)
    {
        if (null == e && this.hasDefaultZero)
            return 0;
        
        Integer result = refs.get(e);
        
        if (result == null) {
            String msg = (e != null) ? e.toString() : "-none-";
            throw new IllegalArgumentException("Unknown pool item \"" + msg + "\"");
        }
        
        return result;
    }

    /**
     * @return the size of the pool; this is the size of the elements, plus one
     * if the pool has a default zeroth element.
     */
    public int size()
    {
        return (hasDefaultZero ? 1 : 0) + refs.size();
    }

    /**
     * When the only entry in a pool with a default zero entry is the default
     * zero entry the nominal size of the pool is 0, otherwise the nominal size
     * of the pool is the same as its size.
     * <p>
     * This method is need to compute the pool size to write into the ABC.
     * 
     * @see #size
     * @return The nominal size of the pool.
     */
    public int getNominalSize()
    {
        final int poolSize = size();
        
        if ((hasDefaultZero) && (poolSize == 1))
        {
            assert refs.size() == 0 : "pool collection for pool with default zero entry should be empty when computed pool size is 1";
            return 0;
        }
        
        assert ((!hasDefaultZero) && (poolSize == refs.size())) || ((hasDefaultZero) && (poolSize == (refs.size() + 1))) : "size of pool collection does not match computed size of pool";
        return poolSize;
    }

    @Override
    public String toString()
    {
        return String.valueOf(refs);
    }
}
