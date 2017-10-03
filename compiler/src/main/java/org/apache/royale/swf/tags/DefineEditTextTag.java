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
import org.apache.royale.swf.types.RGBA;
import org.apache.royale.swf.types.Rect;

/**
 * Represents a <code>DefineEditText</code> tag in a SWF file.
 * <p>
 * The DefineEditText tag defines a dynamic text object, or text field.
 * <p>
 * A text field is associated with an ActionScript variable name where the
 * contents of the text field are stored. The SWF file can read and write the
 * contents of the variable, which is always kept in sync with the text being
 * displayed. If the ReadOnly flag is not set, users may change the value of a
 * text field interactively.
 * <p>
 * Fonts used by DefineEditText must be defined using DefineFont2, not
 * DefineFont.
 */
public class DefineEditTextTag extends CharacterTag implements ICharacterReferrer
{
    /**
     * Constructor.
     */
    public DefineEditTextTag()
    {
        super(TagType.DefineEditText);
    }

    private Rect bounds;
    private boolean hasText;
    private boolean wordWrap;
    private boolean multiline;
    private boolean password;
    private boolean readOnly;
    private boolean hasTextColor;
    private boolean hasMaxLength;
    private boolean hasFont;
    private boolean hasFontclass;
    private boolean autoSize;
    private boolean hasLayout;
    private boolean noSelect;
    private boolean border;
    private boolean wasStatic;
    private boolean html;
    private boolean useOutlines;

    private ICharacterTag fontTag;
    private String fontClass;
    private int fontHeight;
    private RGBA textColor;
    private int maxLength;
    private int align;
    private int leftMargin;
    private int rightMargin;
    private int indent;
    private int leading;
    private String variableName;
    private String initialText;
    private CSMTextSettingsTag CSMTextSettings;

    /**
     * Rectangle that completely encloses the text field.
     * 
     * @return the bounds
     */
    public Rect getBounds()
    {
        return bounds;
    }

    /**
     * @param bounds the bounds to set
     */
    public void setBounds(Rect bounds)
    {
        this.bounds = bounds;
    }

    /**
     * If has default text.
     * 
     * @return the hasText
     */
    public boolean isHasText()
    {
        return hasText;
    }

    /**
     * @param hasText the hasText to set
     */
    public void setHasText(boolean hasText)
    {
        this.hasText = hasText;
    }

    /**
     * If text will not wrap and will scroll sideways.
     * 
     * @return the wordWrap
     */
    public boolean isWordWrap()
    {
        return wordWrap;
    }

    /**
     * @param wordWrap the wordWrap to set
     */
    public void setWordWrap(boolean wordWrap)
    {
        this.wordWrap = wordWrap;
    }

    /**
     * If text field is multi-line and scrollable.
     * 
     * @return the multiline
     */
    public boolean isMultiline()
    {
        return multiline;
    }

    /**
     * @param multiline the multiline to set
     */
    public void setMultiline(boolean multiline)
    {
        this.multiline = multiline;
    }

    /**
     * If all characters are displayed as an asterisk.
     * 
     * @return the password
     */
    public boolean isPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(boolean password)
    {
        this.password = password;
    }

    /**
     * If text editing is enabled
     * 
     * @return the readOnly
     */
    public boolean isReadOnly()
    {
        return readOnly;
    }

    /**
     * If text is read only.
     * 
     * @param readOnly the readOnly to set
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    /**
     * If text has default color.
     * 
     * @return the hasTextColor
     */
    public boolean isHasTextColor()
    {
        return hasTextColor;
    }

    /**
     * @param hasTextColor the hasTextColor to set
     */
    public void setHasTextColor(boolean hasTextColor)
    {
        this.hasTextColor = hasTextColor;
    }

    /**
     * If text has max length.
     * 
     * @return the hasMaxLength
     */
    public boolean isHasMaxLength()
    {
        return hasMaxLength;
    }

    /**
     * @param hasMaxLength the hasMaxLength to set
     */
    public void setHasMaxLength(boolean hasMaxLength)
    {
        this.hasMaxLength = hasMaxLength;
    }

    /**
     * If text has font.
     * 
     * @return the hasFont
     */
    public boolean isHasFont()
    {
        return hasFont;
    }

    /**
     * @param hasFont the hasFont to set
     */
    public void setHasFont(boolean hasFont)
    {
        this.hasFont = hasFont;
    }

    /**
     * @return HasFontClass and HasFont are exclusive.
     */
    public boolean isHasFontClass()
    {
        return hasFontclass & !hasFont;
    }

    /**
     * @param hasFontclass the hasFontclass to set
     */
    public void setHasFontclass(boolean hasFontclass)
    {
        this.hasFontclass = hasFontclass;
    }

    /**
     * @return the autoSize
     */
    public boolean isAutoSize()
    {
        return autoSize;
    }

    /**
     * @param autoSize the autoSize to set
     */
    public void setAutoSize(boolean autoSize)
    {
        this.autoSize = autoSize;
    }

    /**
     * @return the hasLayout
     */
    public boolean isHasLayout()
    {
        return hasLayout;
    }

    /**
     * @param hasLayout the hasLayout to set
     */
    public void setHasLayout(boolean hasLayout)
    {
        this.hasLayout = hasLayout;
    }

    /**
     * @return the noSelect
     */
    public boolean isNoSelect()
    {
        return noSelect;
    }

    /**
     * @param noSelect the noSelect to set
     */
    public void setNoSelect(boolean noSelect)
    {
        this.noSelect = noSelect;
    }

    /**
     * @return the border
     */
    public boolean isBorder()
    {
        return border;
    }

    /**
     * @param border the border to set
     */
    public void setBorder(boolean border)
    {
        this.border = border;
    }

    /**
     * @return the wasStatic
     */
    public boolean isWasStatic()
    {
        return wasStatic;
    }

    /**
     * @param wasStatic the wasStatic to set
     */
    public void setWasStatic(boolean wasStatic)
    {
        this.wasStatic = wasStatic;
    }

    /**
     * @return the html
     */
    public boolean isHtml()
    {
        return html;
    }

    /**
     * @param html the html to set
     */
    public void setHtml(boolean html)
    {
        this.html = html;
    }

    /**
     * @return the useOutlines
     */
    public boolean isUseOutlines()
    {
        return useOutlines;
    }

    /**
     * @param useOutlines the useOutlines to set
     */
    public void setUseOutlines(boolean useOutlines)
    {
        this.useOutlines = useOutlines;
    }

    /**
     * Get the font to use.
     * 
     * @return the fontTag
     */
    public ICharacterTag getFontTag()
    {
        return fontTag;
    }

    /**
     * @param fontTag the fontTag to set
     */
    public void setFontTag(ICharacterTag fontTag)
    {
        this.fontTag = fontTag;
    }

    /**
     * Class name of font to be loaded from another SWF and used for this text.
     * 
     * @return the fontClass
     */
    public String getFontClass()
    {
        return fontClass;
    }

    /**
     * @param fontClass the fontClass to set
     */
    public void setFontClass(String fontClass)
    {
        setHasFontclass(fontClass != null);
        this.fontClass = fontClass;
    }

    /**
     * @return the fontHeight
     */
    public int getFontHeight()
    {
        return fontHeight;
    }

    /**
     * @param fontHeight the fontHeight to set
     */
    public void setFontHeight(int fontHeight)
    {
        this.fontHeight = fontHeight;
    }

    /**
     * Color of text.
     * 
     * @return the textColor
     */
    public RGBA getTextColor()
    {
        return textColor;
    }

    /**
     * @param textColor the textColor to set
     */
    public void setTextColor(RGBA textColor)
    {
        this.textColor = textColor;
    }

    /**
     * @return the maxLength
     */
    public int getMaxLength()
    {
        return maxLength;
    }

    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }

    /**
     * 0 = Left 1 = Right 2 = Center 3 = Justify
     * 
     * @return the align
     */
    public int getAlign()
    {
        return align;
    }

    /**
     * @param align the align to set
     */
    public void setAlign(int align)
    {
        this.align = align;
    }

    /**
     * Left margin in twips.
     * 
     * @return the leftMargin
     */
    public int getLeftMargin()
    {
        return leftMargin;
    }

    /**
     * @param leftMargin the leftMargin to set
     */
    public void setLeftMargin(int leftMargin)
    {
        this.leftMargin = leftMargin;
    }

    /**
     * Right margin in twips.
     * 
     * @return the rightMargin
     */
    public int getRightMargin()
    {
        return rightMargin;
    }

    /**
     * @param rightMargin the rightMargin to set
     */
    public void setRightMargin(int rightMargin)
    {
        this.rightMargin = rightMargin;
    }

    /**
     * Indent in twips.
     * 
     * @return the indent
     */
    public int getIndent()
    {
        return indent;
    }

    /**
     * @param indent the indent to set
     */
    public void setIndent(int indent)
    {
        this.indent = indent;
    }

    /**
     * Leading in twips (vertical distance between bottom of descender of one
     * line and top of ascender of the next).
     * 
     * @return the leading
     */
    public int getLeading()
    {
        return leading;
    }

    /**
     * @param leading the leading to set
     */
    public void setLeading(int leading)
    {
        this.leading = leading;
    }

    /**
     * Name of the variable where the contents of the text field are stored. May
     * be qualified with dot syntax or slash syntax for non-global variables.
     * 
     * @return the variableName
     */
    public String getVariableName()
    {
        return variableName;
    }

    /**
     * @param variableName the variableName to set
     */
    public void setVariableName(String variableName)
    {
        this.variableName = variableName;
    }

    /**
     * Text that is initially displayed.
     * 
     * @return the initialText
     */
    public String getInitialText()
    {
        return initialText;
    }

    /**
     * @param initialText the initialText to set
     */
    public void setInitialText(String initialText)
    {
        this.initialText = initialText;
    }

    /**
     * @return the csmTextSettings
     */
    public CSMTextSettingsTag getCSMTextSettings()
    {
        return CSMTextSettings;
    }

    /**
     * @param csmTextSettings the csmTextSettings to set
     */
    public void setCSMTextSettings(CSMTextSettingsTag csmTextSettings)
    {
        this.CSMTextSettings = csmTextSettings;
    }

    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        if (!isHasFont())
            return CharacterIterableFactory.empty();

        assert fontTag != null;
        return CharacterIterableFactory.from(fontTag);
    }
}
