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
import java.util.List;

import org.apache.royale.swf.tags.ICharacterReferrer;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.CharacterIterableFactory;

/**
 * Shape is used by the {@code DefineFont} tag, to define character glyphs.
 */
public class Shape implements IDataType, ICharacterReferrer
{
    protected List<ShapeRecord> shapeRecords;
    protected int numFillBits;
    protected int numLineBits;

    public Shape()
    {
        shapeRecords = new ArrayList<ShapeRecord>();
    }

    /**
     * Get shape records.
     * 
     * @return shape records
     */
    public List<ShapeRecord> getShapeRecords()
    {
        return shapeRecords;
    }

    /**
     * Add new shape records.
     * 
     * @param value new shape records.
     */
    public void addShapeRecords(List<ShapeRecord> value)
    {
        shapeRecords.addAll(value);
    }

    /**
     * Add a new shape record.
     * 
     * @param value new shape record.
     */
    public void addShapeRecord(ShapeRecord value)
    {
        shapeRecords.add(value);
    }

    /**
     * @return the numFillBits
     */
    public int getNumFillBits()
    {
        return numFillBits;
    }

    /**
     * @param value the numFillBits to set
     */
    public void setNumFillBits(int value)
    {
        this.numFillBits = value;
    }

    /**
     * @return the numLineBits
     */
    public int getNumLineBits()
    {
        return numLineBits;
    }

    /**
     * @param value the numLineBits to set
     */
    public void setNumLineBits(int value)
    {
        this.numLineBits = value;
    }

    /**
     * If one the shape records is a StyleChangeRecord, it may contain
     * references to other character tags.
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        return CharacterIterableFactory.filterAndCollect(shapeRecords);
    }

}
