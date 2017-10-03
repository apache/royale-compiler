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

import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.ABCParser;

/**
 * The {@code Metadata} entry provides a mean of embedding arbitrary key/value
 * pairs into the ABC file. The AVM2 will ignore all such entries.
 * <p>
 * Note that, "empty key" and "empty value" are implemented as {@code null} in
 * the key/value arrays by {@link ABCParser} and {@link ABCEmitter}.
 */
public class Metadata
{
    /**
     * A prime number unlikely to be a divisor of the hash table size, used to
     * generate composite hash keys.
     * 
     * @warn if you copy this, pick a new prime to generate distinct hash keys
     * in different classes.
     */
    private static final long PRIME_MULTIPLIER = 5857;

    /**
     * Test two arrays of Comparable objects for equality.
     * 
     * @param array1 array 1
     * @param array2 array 2
     * @return true if the arrays are equal.
     */
    public static <T extends Object> boolean arrayEquals(final T[] array1, final T[] array2)
    {
        boolean result = array1.length == array2.length;
        for (int i = 0; result && i < array1.length; i++)
        {
            if (array1[i] == null && array2[i] == null)
                continue;
            else if (array1[i] != null && array2[i] != null)
                result = array1[i].equals(array2[i]);
            else
                result = false;
        }

        return result;
    }

    /**
     * Constructor.
     */
    public Metadata(final String name, final String[] keys, final String[] values)
    {
        if (name == null)
            throw new IllegalArgumentException("Null name not allowed.");
        if (keys == null)
            throw new IllegalArgumentException("Null key array not allowed.");
        if (values == null)
            throw new IllegalArgumentException("Null value array not allowed.");
        if (keys.length != values.length)
            throw new IllegalArgumentException("Number of keys and values must be same.");

        this.name = name;
        this.keys = keys;
        this.values = values;
    }

    private final String name;
    private final String[] keys;
    private final String[] values;

    /**
     * Cache the hash code since it's fairly expensive to compute.
     */
    private Integer cachedHashCode = null;
    
    public String getName()
    {
        return name;
    }

    public String[] getKeys()
    {
        return keys;
    }

    public String[] getValues()
    {
        return values;
    }

    /**
     * Generate a composite hash code from this item's name and key/value set.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        if (cachedHashCode == null)
        {
            int result = name.hashCode();

            for (String key : keys)
                result = (int)(PRIME_MULTIPLIER * result + (key != null ? key.hashCode() : 0));

            for (String value : values)
                result = (int)(PRIME_MULTIPLIER * result + (value != null ? value.hashCode() : 0));

            cachedHashCode = result;
        }

        return cachedHashCode;
    }

    /**
     * Determine equality by checking the Metadata objects' corresponding
     * fields.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        else if (!(o instanceof Metadata))
        {
            return false;
        }
        else
        {
            Metadata other = (Metadata)o;

            return this.name.equals(other.name) &&
                   arrayEquals(this.keys, other.keys) &&
                   arrayEquals(this.values, other.values);
        }
    }
}
