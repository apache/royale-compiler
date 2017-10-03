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

import org.apache.royale.swf.TagType;

/**
 * Represents a <code>DefineBinaryData</code> tag in a SWF file.
 * <p>
 * The DefineBinaryData tag permits arbitrary binary data to be embedded in a
 * SWF file. DefineBinaryData is a definition tag. It associates a blob of
 * binary data with a standard SWF 16-bit character ID. The character ID is
 * entered into the SWF file's character dictionary.
 * </p>
 * </p> DefineBinaryData is intended to be used in conjunction with the
 * {@link SymbolClassTag} tag. The {@link SymbolClassTag} tag can be used to
 * associate a {@code DefineBinaryData} tag with an AS3 class definition. </p>
 * <p>
 * The AS3 class must be a subclass of {@code ByteArray}. When the class is
 * instantiated, it will be populated automatically with the contents of the
 * binary data resource.
 * </p>
 */
public class DefineBinaryDataTag extends CharacterTag
{
    /**
     * Constructor.
     * 
     * @param data binary data as bytes
     */
    public DefineBinaryDataTag(byte[] data)
    {
        super(TagType.DefineBinaryData);
        this.data = data;
    }
    
    private final byte[] data;

    /**
     * A blob of binary data, up to the end of the tag.
     * 
     * @return binary data as bytes
     */
    public byte[] getData()
    {
        return data;
    }

    @Override
    protected String description()
    {
        return String.format("#%d, including %.2f kb data", getCharacterID(), data.length / 1024f);
    }

}
