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

import org.apache.royale.swf.tags.ICharacterReferrer;
import org.apache.royale.swf.tags.ICharacterTag;
import static org.apache.royale.swf.io.SWFWriter.requireUBCount;

/**
 * {@code Styles} is a composition of {@link LineStyleArray} and
 * {@link FillStyleArray}. This is not a type in SWF specification. It is a
 * wrapper class shared by {@link ShapeWithStyle} and {@link StyleChangeRecord}.
 */
public class Styles implements IDataType, ICharacterReferrer
{
    public Styles(final FillStyleArray fillStyles, final LineStyleArray lineStyles)
    {
        this.fillStyles = fillStyles;
        this.lineStyles = lineStyles;
    }

    /**
     * Get all the characters referred by the fill styles.
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        return fillStyles.getReferences();
    }

    private final LineStyleArray lineStyles;
    private final FillStyleArray fillStyles;

    /**
     * Calculate number of bits needed for line style array length.
     * 
     * @return number of bits needed for line style array length
     */
    public static int calculateMinLineUBits(Styles styles)
    {
        return requireUBCount(styles.lineStyles.size());
    }

    /**
     * Calculate number of bits needed for fill style array length.
     * 
     * @return number of bits needed for fill style array length
     */
    public static int calculateMinFillUBits(Styles styles)
    {
        return requireUBCount(styles.fillStyles.size());
    }

    /**
     * @return the lineStyles
     */
    public LineStyleArray getLineStyles()
    {
        return lineStyles;
    }

    /**
     * @return the fillStyles
     */
    public FillStyleArray getFillStyles()
    {
        return fillStyles;
    }
}
