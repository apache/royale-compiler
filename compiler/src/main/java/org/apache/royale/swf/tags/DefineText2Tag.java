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
 * Represents a <code>DefineText2</code> tag in a SWF file.
 * <p>
 * The DefineText2 tag is almost identical to the DefineText tag. The only
 * difference is that Type 1 text records contained within a DefineText2 tag use
 * an RGBA value (rather than an RGB value) to define TextColor. This allows
 * partially or completely transparent characters.
 * <p>
 * Text defined with DefineText2 is always rendered with glyphs. Device text can
 * never include transparency.
 */
public class DefineText2Tag extends DefineTextTag
{
    /**
     * Constructor.
     */
    public DefineText2Tag()
    {
        super(TagType.DefineText2);
    }
}
