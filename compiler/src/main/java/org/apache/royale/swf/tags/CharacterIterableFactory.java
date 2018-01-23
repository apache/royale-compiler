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

package org.apache.royale.swf.tags;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Factory class to create an {@code Iterable} object from potentially several
 * iterable instances
 */
public class CharacterIterableFactory
{
    /**
     * Create an iterable instance by combining two iterable objects. The
     * iteration order is {@code i1} then {@code i2}. Each character will only
     * be visited once. All duplicated characters will be ignored.
     * 
     * @param i1 first iterable object
     * @param i2 second iterable object
     * @return combined iterable object
     */
    public static Iterable<ICharacterTag> collect(
            final Iterable<ICharacterTag> i1,
            final Iterable<ICharacterTag> i2)
    {
        // Possible optimization: hand-build an iterable instead of using a list.
        final Collection<ICharacterTag> merged = new LinkedHashSet<ICharacterTag>();
        for (final ICharacterTag character : i1)
        {
            merged.add(character);
        }

        for (final ICharacterTag character : i2)
        {
            merged.add(character);
        }
        return merged;
    }

    /**
     * Create an iterable instance by combining two iterable objects. The
     * iteration order is {@code i1} then {@code i2}.Each character will only be
     * visited once. All duplicated characters will be ignored.
     * 
     * @param i1 first iterable object
     * @param i2 second iterable object
     * @param i3 third iterable object
     * @return combined iterable object
     */
    public static Iterable<ICharacterTag> collect(
            final Iterable<ICharacterTag> i1,
            final Iterable<ICharacterTag> i2,
            final Iterable<ICharacterTag> i3)
    {
        // Possible optimization: hand-build an iterable instead of using a list.
        final Collection<ICharacterTag> merged = new LinkedHashSet<ICharacterTag>();
        for (final ICharacterTag character : i1)
        {
            merged.add(character);
        }

        for (final ICharacterTag character : i2)
        {
            merged.add(character);
        }

        for (final ICharacterTag character : i3)
        {
            merged.add(character);
        }
        return merged;
    }

    /**
     * Create an iterable object from multiple {@link ICharacterReferrer}
     * objects. The result consists of all the referred characters by them. Each
     * character will only be visited once. All duplicated characters will be
     * ignored.
     * 
     * @param referrers multiple {@link ICharacterReferrer} objects.
     * @return all the referred character tags
     */
    public static Iterable<ICharacterTag> collect(final Iterable<? extends ICharacterReferrer> referrers)
    {
        final Collection<ICharacterTag> merged = new LinkedHashSet<ICharacterTag>();
        for (final Object referrer : referrers)
        {
            for (final ICharacterTag character : ((ICharacterReferrer)referrer).getReferences())
            {
                merged.add(character);
            }
        }
        return merged;
    }

    /**
     * Find all the ICharacterReferrer instances from a generic collection.
     * Then, create an iterable object from these {@link ICharacterReferrer}
     * objects. The result consists of all the referred characters by them. Each
     * character will only be visited once. All duplicated characters will be
     * ignored.
     * 
     * @param iterable an iterable object that contains {@code ICharacterReferrer}
     * members
     * @return all the referred character tags
     */
    public static Iterable<ICharacterTag> filterAndCollect(final Iterable<?> iterable)
    {
        final Collection<ICharacterTag> merged = new LinkedHashSet<ICharacterTag>();
        for (final Object object : iterable)
        {
            if (object instanceof ICharacterReferrer)
            {
                for (final ICharacterTag character : (((ICharacterReferrer)object).getReferences()))
                {
                    assert character != null;
                    merged.add(character);
                }
            }
        }
        return merged;
    }

    /**
     * An empty iterable object.
     */
    private static final Iterable<ICharacterTag> EMPTY = new Iterable<ICharacterTag>()
    {
        @Override
        public Iterator<ICharacterTag> iterator()
        {
            return new Iterator<ICharacterTag>()
            {
                @Override
                public boolean hasNext()
                {
                    return false;
                }

                @Override
                public ICharacterTag next()
                {
                    return null;
                }

                @Override
                public void remove()
                {
                }
            };
        }
    };

    /**
     * Always use this empty iterable instead of creating an empty
     * {@code Collection}.
     * 
     * @return an empty iterable
     */
    public static Iterable<ICharacterTag> empty()
    {
        return EMPTY;
    }

    /**
     * Create an iterable object from one member.
     * 
     * @param character the only member in the iterable
     * @return one-item iterable
     */
    public static Iterable<ICharacterTag> from(final ICharacterTag character)
    {
        return new Iterable<ICharacterTag>()
        {
            @Override
            public Iterator<ICharacterTag> iterator()
            {
                return new Iterator<ICharacterTag>()
                {
                    private boolean visited = false;

                    @Override
                    public boolean hasNext()
                    {
                        if (visited)
                        {
                            return false;
                        }
                        else
                        {
                            return true;
                        }
                    }

                    @Override
                    public ICharacterTag next()
                    {
                        if (visited)
                        {
                            return null;
                        }
                        else
                        {
                            visited = true;
                            return character;
                        }
                    }

                    @Override
                    public void remove()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
