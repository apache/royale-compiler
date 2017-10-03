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
 * Represents a <code>DefineFontInfo2</code> tag in a SWF file.
 * <p>
 * DefineFontInfo2 is identical to DefineFontInfo, except that it adds a field
 * for a language code. If you use the older DefineFontInfo, the language code
 * will be assumed to be zero, which results in behavior that is dependent on
 * the locale in which Flash Player is running.
 */
public class DefineFontInfo2Tag extends DefineFontInfoTag
{
    /**
     * Constructor.
     */
    public DefineFontInfo2Tag()
    {
        super(TagType.DefineFontInfo2);
    }

    // UI8 LanguageCode
    private int languageCode;

    /**
     * @return the languageCode
     */
    public int getLanguageCode()
    {
        return languageCode;
    }

    /**
     * @param languageCode the languageCode to set
     */
    public void setLanguageCode(int languageCode)
    {
        this.languageCode = languageCode;
    }
}
