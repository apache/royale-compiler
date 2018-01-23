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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;

import org.apache.royale.abc.semantics.Namespace;

/**
 * Nsset represents an ABC NamespaceSet, i.e., a set of Namespaces.
 */
public class Nsset implements Iterable<Namespace>
{
    /**
     * Manifest constant passed to Collection<T>.toArray(T[]) to cause toArray()
     * to create a new array.
     */
    private static final Namespace[] CREATE_NEW_NSSET_NAMESPACE_ARRAY = new Namespace[0];

    /**
     * A prime number unlikely to be a divisor of the hash table size, used to
     * generate composite hash keys.
     * 
     * @warn if you copy this, pick a new prime to generate distinct hash keys
     * in different classes.
     */
    private static final long PRIME_MULTIPLIER = 9679;

    /**
     * Construct a Nsset from a single Namespace.
     */
    public Nsset(Namespace single_ns)
    {
        namespaces = new Namespace[] {single_ns};
    }

    /**
     * Construct a Nsset from a Collection of Namespaces.
     */
    public Nsset(Collection<Namespace> nss)
    {
        namespaces = nss.toArray(CREATE_NEW_NSSET_NAMESPACE_ARRAY);
    }

    /**
     * The set's constituent Namespaces.
     */
    private Namespace[] namespaces = null;

    /**
     * @return the namespace set's size.
     */
    public int length()
    {
        return namespaces.length;
    }

    /**
     * @return an Iterator over this set's namespaces.
     */
    @Override
    public Iterator<Namespace> iterator()
    {
        return Arrays.asList(namespaces).iterator();
    }

    /**
     * @return the one consitutent Namespace of this set.
     * @throws AssertionError if more or less than one Namespace is present.
     */
    public Namespace getSingleQualifier()
    {
        assert (namespaces.length == 1);
        return namespaces[0];
    }

    @Override
    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append('{');
        for (int i = 0; i < namespaces.length; i++)
        {
            if (i > 0)
                result.append(',');
            result.append(namespaces[i]);
        }
        result.append('}');
        return result.toString();
    }

    /**
     * Cached hash code. Generating a hash code for a Nsset is somewhat
     * expensive, so it's done on demand.
     */
    private Integer cachedHashCode = null;

    /**
     * Generate a composite hash code using the Namespaces' hashes.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        if (cachedHashCode == null)
        {
            int result = 0;

            for (Namespace ns : namespaces)
                result = (int)(PRIME_MULTIPLIER * result + ns.hashCode());

            cachedHashCode = result;
        }

        return cachedHashCode;
    }

    /**
     * Determine equality by checking the Namespaces' corresponding fields.
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
        else if (!(o instanceof Nsset) || this.hashCode() != o.hashCode())
        {
            return false;
        }
        else
        {
            //  Do a namespace-by-namespace comparison.
            Nsset other = (Nsset)o;
            boolean result = this.namespaces.length == other.namespaces.length;

            for (int i = 0; i < this.namespaces.length && result; i++)
                result = this.namespaces[i].equals(other.namespaces[i]);

            return result;
        }
    }
}
