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

/**
 * There are four types of shape records:
 * <ul>
 * <li>End shape record</li>
 * <li>Style change record</li>
 * <li>Straight edge record</li>
 * <li>Curved edge record</li>
 * </ul>
 * <p>
 * Each individual shape record is byte-aligned within an array of shape
 * records. One shape record is padded to a byte boundary before the next shape
 * record begins.
 * <p>
 * Each shape record begins with a TypeFlag. If the TypeFlag is zero, the shape
 * record is a non-edge record, and a further five bits of flag information
 * follow.
 */
public abstract class ShapeRecord implements IDataType
{
    private final ShapeRecordType type;

    /**
     * This is a type marker for subclasses of {@code ShapeRecord}. Always query
     * this tag when checking the type of a {@code ShapeRecord}, instead of
     * using {@code instanceof}.
     */
    public static enum ShapeRecordType
    {
        END_SHAPE(false),
        STYLE_CHANGE(false),
        STRAIGHT_EDGE(false),
        CURVED_EDGE(false);

        final boolean isEdge;

        ShapeRecordType(boolean isEdge)
        {
            this.isEdge = isEdge;
        }
    }

    /**
     * Create a {@code ShapeRecord} object from one of the four types.
     * 
     * @param type ShapeRecord type
     */
    protected ShapeRecord(ShapeRecordType type)
    {
        this.type = type;
    }

    public boolean getTypeFlag()
    {
        return type.isEdge;
    }

    public ShapeRecordType getShapeRecordType()
    {
        return type;
    }
}
