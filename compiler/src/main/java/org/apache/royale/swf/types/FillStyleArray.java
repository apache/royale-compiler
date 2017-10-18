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

package org.apache.royale.swf.types;

import java.util.ArrayList;

import org.apache.royale.swf.tags.ICharacterReferrer;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.CharacterIterableFactory;

/**
 * A fill style array enumerates a number of fill styles.
 */
public class FillStyleArray extends ArrayList<IFillStyle> implements IDataType, ICharacterReferrer
{
    /**
     * Create a FillStyleArray with specified initial capacity.
     * 
     * @param initialCapacity initial capacity
     */
    public FillStyleArray(int initialCapacity)
    {
        super(initialCapacity);
    }

    public FillStyleArray()
    {
        super();
    }

    private static final long serialVersionUID = -8366602180614487940L;

    /**
     * Get all the referred bitmap characters by its children fill styles.
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        return CharacterIterableFactory.collect(this);
    }
}
