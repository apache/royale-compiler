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
import org.apache.royale.swf.tags.CharacterIterableFactory;

/**
 * The style change record is also a non-edge record. It can be used to do the
 * following:
 * <ol>
 * <li>Select a fill or line style for drawing.</li>
 * <li>Move the current drawing position (without drawing).</li>
 * <li>Replace the current fill and line style arrays with a new set of styles.</li>
 * </ol>
 * <p>
 * Because fill and line styles often change at the start of a new path, it is
 * useful to perform more than one action in a single record. For example, say a
 * {@code DefineShape} tag defines a red circle and a blue square. After the
 * circle is closed, it is necessary to move the drawing position, and replace
 * the red fill with the blue fill. The style change record can achieve this
 * with a single shape record.
 * </p>
 */
public class StyleChangeRecord extends ShapeRecord implements ICharacterReferrer
{
    public StyleChangeRecord()
    {
        super(ShapeRecordType.STYLE_CHANGE);
        this.stateNewStyles = false;
        this.stateLineStyle = false;
        this.stateFillStyle0 = false;
        this.stateFillStyle1 = false;
        this.stateMoveTo = false;
    }

    private boolean stateNewStyles;
    private boolean stateLineStyle;
    private boolean stateFillStyle1;
    private boolean stateFillStyle0;
    private boolean stateMoveTo;

    private int moveDeltaX;
    private int moveDeltaY;

    private IFillStyle fillStyle0;
    private IFillStyle fillStyle1;
    private ILineStyle lineStyle;

    private int numFillBits;
    private int numLineBits;

    private Styles styles;

    /**
     * Move the current drawing position (without drawing).
     * 
     * @param x delta x
     * @param y delta y
     */
    public void setMove(int x, int y)
    {
        stateMoveTo = true;
        moveDeltaX = x;
        moveDeltaY = y;
    }

    /**
     * Select styles from parent {@code ShapeWithStyle} record. The
     * {@code FillStyle} and {@code LineStyle} objects provided must be defined
     * in the parent {@code ShapeWithStyle} record. Otherwise, exception will be
     * thrown.
     * 
     * @param fillStyle0 fill0 style
     * @param fillStyle1 fill1 style
     * @param lineStyle line style
     * @throws IllegalArgumentException using styles not defined in the parent
     * shape
     */
    public void setDefinedStyles(
            final IFillStyle fillStyle0,
            final IFillStyle fillStyle1,
            final ILineStyle lineStyle,
            final Styles styleContext)
    {
        if (fillStyle0 != null && !styleContext.getFillStyles().contains(fillStyle0))
        {
            throw new IllegalArgumentException("FillStyle0 is not defined in parent shape.");
        }

        if (fillStyle1 != null && !styleContext.getFillStyles().contains(fillStyle1))
        {
            throw new IllegalArgumentException("FillStyle1 is not defined in parent shape.");
        }

        if (lineStyle != null && !styleContext.getLineStyles().contains(lineStyle))
        {
            throw new IllegalArgumentException("LineStyle is not defined in parent shape.");
        }

        this.fillStyle0 = fillStyle0;
        this.fillStyle1 = fillStyle1;
        this.lineStyle = lineStyle;
        this.stateFillStyle0 = fillStyle0 != null;
        this.stateFillStyle1 = fillStyle1 != null;
        this.stateLineStyle = lineStyle != null;
    }

    /**
     * Select styles from parent {@code ShapeWithStyle} record. The
     * {@code FillStyle} and {@code LineStyle} objects provided must be defined
     * in the parent {@code ShapeWithStyle} record. Otherwise, exception will be
     * thrown.
     * 
     * @param fillStyle0 fill0 style
     * @param fillStyle1 fill1 style
     * @param lineStyle line style
     * @throws IllegalArgumentException using styles not defined in the parent
     * shape
     */
    public void setDefinedStyles(
            final IFillStyle fillStyle0,
            final IFillStyle fillStyle1,
            final ILineStyle lineStyle,
            final boolean stateFillStyle0,
            final boolean stateFillStyle1,
            final boolean stateLineStyle,
            final Styles styleContext)
    {
        this.fillStyle0 = fillStyle0;
        this.fillStyle1 = fillStyle1;
        this.lineStyle = lineStyle;
        this.stateFillStyle0 = stateFillStyle0;
        this.stateFillStyle1 = stateFillStyle1;
        this.stateLineStyle = stateLineStyle;
    }

    /**
     * Select styles from parent {@code ShapeWithStyle} record. The
     * {@code FillStyle} and {@code LineStyle} objects provided must be defined
     * in the parent {@code ShapeWithStyle} record. Otherwise, exception will be
     * thrown.
     * 
     * @param fillStyle0Index fill0 style
     * @param fillStyle1Index fill1 style
     * @param lineStyleIndex line style
     * @throws IllegalArgumentException using styles not defined in the parent
     * shape
     */
    public void setDefinedStyles(
            final int fillStyle0Index,
            final int fillStyle1Index,
            final int lineStyleIndex,
            final Styles styleContext)
    {
        if (fillStyle0Index >= 0)
        {
            if (fillStyle0Index > 0)
            {
                this.fillStyle0 = styleContext.getFillStyles().get(fillStyle0Index - 1);
            }
            else
            {
                this.fillStyle0 = null;
            }

            stateFillStyle0 = true;
        }
        if (fillStyle1Index >= 0)
        {
            if (fillStyle1Index > 0)
            {
                this.fillStyle1 = styleContext.getFillStyles().get(fillStyle1Index - 1);
            }
            else
            {
                this.fillStyle1 = null;
            }

            stateFillStyle1 = true;
        }
        if (lineStyleIndex >= 0)
        {
            if (lineStyleIndex > 0)
            {
                this.lineStyle = styleContext.getLineStyles().get(lineStyleIndex - 1);
            }
            else
            {
                this.lineStyle = null;
            }

            stateLineStyle = true;
        }
    }

    /**
     * Select styles from parent {@code Shape} record for a font. The
     * {@code FillStyle} and {@code LineStyle} objects provided must be defined
     * in the parent {@code ShapeWithStyle} record. Otherwise, exception will be
     * thrown.
     * 
     * @param fillStyle0Index fill0 style
     * @param fillStyle1Index fill1 style
     * @param lineStyleIndex line style
     * @throws IllegalArgumentException using styles not defined in the parent
     * shape
     */
    public void setDefinedFontStyles(
            final int fillStyle0Index,
            final int fillStyle1Index,
            final int lineStyleIndex,
            final Styles styleContext)
    {
        // there shouldn't be any styles on a shape for fonts, as the
        // tag is a Shape, not ShapeWithStyle, but the fillStyle0 can be 1 because
        // of the following from the SWF spec:
        // "The first STYLECHANGERECORD of each SHAPE in the GlyphShapeTable does not use
        // the LineStyle and LineStyles fields. In addition, the first STYLECHANGERECORD of each
        // shape must have both fields StateFillStyle0 and FillStyle0 set to 1."
        if (fillStyle0Index >= 0)
        {
            fillStyle0 = null;
            stateFillStyle0 = true;
        }
        if (fillStyle1Index >= 0)
        {
            fillStyle1 = null;
            stateFillStyle1 = true;
        }
        if (lineStyleIndex >= 0)
        {
            lineStyle = null;
            stateLineStyle = true;
        }
    }

    /**
     * clears the styles of this style change record if the respective boolean is set
     * 
     * @param fill0 <code>true</code> to clear fill style 0
     * @param fill1 <code>true</code> to clear file style 1
     * @param line <code>true</code> to clear line style
     */
    public void defaultStyles(boolean fill0, boolean fill1, boolean line)
    {
        if (fill0)
        {
            this.fillStyle0 = null;
            stateFillStyle0 = true;
        }
        if (fill1)
        {
            this.fillStyle1 = null;
            stateFillStyle1 = true;
        }
        if (line)
        {
            this.lineStyle = null;
            stateLineStyle = true;
        }
    }

    /**
     * Define new styles and use the new styles.
     */
    public void setNewStyles(final Styles value)
    {
        this.stateNewStyles = true;
        this.styles = value;
    }

    /**
     * If true, this {@code StyleChangeRecord} defines new styles, and its
     * FillStyle0, FillStyle1, LineStyle points to the new styles.
     * 
     * @return true if the StyleChangeRecord defines new styles.
     */
    public boolean isStateNewStyles()
    {
        return stateNewStyles;
    }

    /**
     * If true, this {@code StyleChangeReocrd} selects a line style.
     * 
     * @return true if the StyleChangeRecord selects a line style.
     */
    public boolean isStateLineStyle()
    {
        return stateLineStyle;
    }

    /**
     * If true, this {@code StyleChangeReocrd} selects a fill style for
     * {@code FillStyle1}.
     * 
     * @return true if the StyleChangeRecord selects a fill style for
     * {@code FillStyle1}.
     */
    public boolean isStateFillStyle1()
    {
        return stateFillStyle1;
    }

    /**
     * If true, this {@code StyleChangeReocrd} selects a fill style for
     * {@code FillStyle0}.
     * 
     * @return true if the StyleChangeRecord selects a fill style for
     * {@code FillStyle0}.
     */
    public boolean isStateFillStyle0()
    {
        return stateFillStyle0;
    }

    /**
     * If true, this {@code StyleChangeReocrd} moves the draw position.
     * 
     * @return true if the StyleChangeRecord moves the draw position.
     */
    public boolean isStateMoveTo()
    {
        return stateMoveTo;
    }

    /**
     * Get move delta on X-axis.
     * 
     * @return move delta X.
     */
    public int getMoveDeltaX()
    {
        return moveDeltaX;
    }

    /**
     * Get move delta on Y-axis.
     * 
     * @return move delta Y.
     */
    public int getMoveDeltaY()
    {
        return moveDeltaY;
    }

    /**
     * Get FillStyle0.
     * 
     * @return fill0 style
     */
    public IFillStyle getFillstyle0()
    {
        return fillStyle0;
    }

    /**
     * Get FillStyle1
     * 
     * @return fill1 style
     */
    public IFillStyle getFillstyle1()
    {
        return fillStyle1;
    }

    /**
     * Get LineStyle
     * 
     * @return line style
     */
    public ILineStyle getLinestyle()
    {
        return lineStyle;
    }

    public Styles getStyles()
    {
        return styles;
    }

    /**
     * StyleChangeRecord refers to fill styles either from its parent
     * ShapeWithStyle's fill styles or from its own private fill styles (when
     * stateNewStyle is true).
     */
    @Override
    public Iterable<ICharacterTag> getReferences()
    {
        return CharacterIterableFactory.collect(
                (stateFillStyle0 && fillStyle0 != null) ? fillStyle0.getReferences() : CharacterIterableFactory.empty(),
                (stateFillStyle1 && fillStyle1 != null) ? fillStyle1.getReferences() : CharacterIterableFactory.empty());
    }

    /**
     * Get the numFillBits field read from SWF.
     * 
     * @return the numFillBits
     */
    public int getNumFillBits()
    {
        return numFillBits;
    }

    /**
     * Only SWFReader can set this field.
     * 
     * @param value the numFillBits to set
     */
    public void setNumFillBits(int value)
    {
        this.numFillBits = value;
    }

    /**
     * Get the numLineBits field read from SWF.
     * 
     * @return the numLineBits
     */
    public int getNumLineBits()
    {
        return numLineBits;
    }

    /**
     * Only SWFReader can set this field.
     * 
     * @param value the numLineBits to set
     */
    public void setNumLineBits(int value)
    {
        this.numLineBits = value;
    }
}
