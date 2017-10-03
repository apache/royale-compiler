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

import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.CharacterIterableFactory;

/**
 * The {@code ShapeWithStyle} structure extends the {@link Shape} structure by
 * including fill style and line style information. It is used by the
 * {@code DefineShape} tag.
 */
public class ShapeWithStyle extends Shape
{

    public ShapeWithStyle(Styles styles)
    {
        super();
        this.styles = styles;
    }

    private final Styles styles;

    /**
     * Get fill styles.
     * 
     * @return fill styles
     */
    public FillStyleArray getFillStyles()
    {
        return styles.getFillStyles();
    }

    /**
     * Get line styles.
     * 
     * @return line styles
     */
    public LineStyleArray getLineStyles()
    {
        return styles.getLineStyles();
    }

    /**
     * Get all the character tags referred by this ShapeWithStyle type. A
     * ShapeWithStyle type can refer to a character in two ways:
     * <ol>
     * <li>ShapeWithStyle > FillStyles::Bitmap</li>
     * <li>ShapeWithStyle > ShapeRecord(StyleChangeRecord) > FillStyle0/1 >
     * Bitmap</li>
     * </ol>
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        return CharacterIterableFactory.collect(
                styles.getReferences(),
                CharacterIterableFactory.filterAndCollect(shapeRecords));
    }
}
