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

package org.apache.royale.compiler.css;

/**
 * CSS DOM for an <code>@font-face</code> statement.
 */
public interface ICSSFontFace extends ICSSNode
{
    /**
     * Get the font source type. A font can be loaded either by its filename (by
     * using src:url) or by its system font name (by using src:local).
     * 
     * @return Font source type. Either {@code url()} or {@code local()}.
     */
    FontFaceSourceType getSourceType();

    /**
     * @return If the source type is {@link FontFaceSourceType#URL}, the
     * return value is the URL of the font file path; If the source type is
     * {@link FontFaceSourceType#LOCAL}, the return value is the system font
     * name.
     */
    String getSourceValue();

    /**
     * The fontFamily property sets the alias for the font that you use to apply
     * the font in style sheets. This property is required. If you embed a font
     * with a family name that matches the family name of a system font, the
     * Flex compiler gives you a warning. You can disable this warning by
     * setting the show-shadows-system-font-warnings compiler option to false.
     * 
     * @return Font family name of this font-face statement. The font family
     * name can be used in the later CSS rulesets' {@code font-family}
     * properties.
     */
    String getFontFamily();

    /**
     * Get the "style" type face value of the font. Possible values are
     * "normal", "italic" and "oblique". If the value is not set in the CSS
     * document, this method returns the default font style "normal".
     * 
     * @return Font style.
     */
    String getFontStyle();

    /**
     * Get the "weight" type face value of the font. Possible values are
     * "normal", "bold" and "heavy". If the value is not set in the CSS
     * document, this method returns the default font style "normal".
     * 
     * @return Font style.
     */
    String getFontWeight();

    /**
     * The advancedAntiAliasing property determines whether to include the
     * advanced anti-aliasing information when embedding the font. This property
     * is optional. The default value is true.
     * 
     * @return True if this font face uses advanced anti-aliasing.
     */
    boolean getAdvancedAntiAliasing();

}
