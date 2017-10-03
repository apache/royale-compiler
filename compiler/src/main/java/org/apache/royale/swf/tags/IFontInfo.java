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

public interface IFontInfo
{
    /**
     * @return the fontTag
     */
    ICharacterTag getFontTag();

    /**
     * @param fontTag the fontTag to set
     */
    void setFontTag(ICharacterTag fontTag);

    /**
     * @return the fontName
     */
    String getFontName();

    /**
     * @param fontName the fontName to set
     */
    void setFontName(String fontName);

    /**
     * @return the fontFlagsReserved
     */
    int getFontFlagsReserved();

    /**
     * @param fontFlagsReserved the fontFlagsReserved to set
     */
    void setFontFlagsReserved(int fontFlagsReserved);

    /**
     * @return the fontFlagsSmallText
     */
    boolean isFontFlagsSmallText();

    /**
     * @param fontFlagsSmallText the fontFlagsSmallText to set
     */
    void setFontFlagsSmallText(boolean fontFlagsSmallText);

    /**
     * @return the fontFlagsShiftJIS
     */
    boolean isFontFlagsShiftJIS();

    /**
     * @param fontFlagsShiftJIS the fontFlagsShiftJIS to set
     */
    void setFontFlagsShiftJIS(boolean fontFlagsShiftJIS);

    /**
     * @return the fontFlagsANSI
     */
    boolean isFontFlagsANSI();

    /**
     * @param fontFlagsANSI the fontFlagsANSI to set
     */
    void setFontFlagsANSI(boolean fontFlagsANSI);

    /**
     * @return the fontFlagsItalic
     */
    boolean isFontFlagsItalic();

    /**
     * @param fontFlagsItalic the fontFlagsItalic to set
     */
    void setFontFlagsItalic(boolean fontFlagsItalic);

    /**
     * @return the fontFlagsBold
     */
    boolean isFontFlagsBold();

    /**
     * @param fontFlagsBold the fontFlagsBold to set
     */
    void setFontFlagsBold(boolean fontFlagsBold);

    /**
     * @return the fontFlagsWideCodes
     */
    boolean isFontFlagsWideCodes();

    /**
     * @param fontFlagsWideCodes the fontFlagsWideCodes to set
     */
    void setFontFlagsWideCodes(boolean fontFlagsWideCodes);

    /**
     * @return the codeTable
     */
    int[] getCodeTable();

    /**
     * @param codeTable the codeTable to set
     */
    void setCodeTable(int[] codeTable);
}
