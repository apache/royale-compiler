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

package org.apache.flex.compiler.mxml;

import org.apache.flex.compiler.common.SourceLocation;
import org.apache.flex.compiler.filespecs.FileSpecification;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.mxml.MXMLDialect;
import org.apache.flex.utils.FastStack;

/**
 * Encapsulation of an MXML unit: an open/close/empty tag, a chunk of text
 * (perhaps whitespace, CDATA, or a comment), or a processing instruction.
 * <p>
 * An {@link MXMLData} object stores a linear list of MXML units,
 * but it is possible to walk them in a hierarchical way.
 */
public abstract class MXMLUnitData extends SourceLocation
{
    /**
     * Constructor.
     */
    public MXMLUnitData()
    {
        super();
        
        parent = null;
        index = -1;
    }

    /**
     * Copy constructor.
     */
    public MXMLUnitData(MXMLUnitData other)
    {
        this.parent = other.parent;
        this.index = other.index;
    }

    /**
     * The {@link MXMLData} object that owns this unit.
     */
    private MXMLData parent;
    
    /**
     * This position of this unit in the linear list of units
     * owned by the {@link MXMLData}.
     */
    protected int index;
    
    /**
     * The position of this unit's parent unit in the linear list of units
     * owned by the {@link MXMLData}.
     */
    private int parentIndex;

    /**
     * Returns the first character of the actual content of the unit For most
     * units this is the same as getStart(), but for things like tags which have
     * "junk punctuation" around them, {@link SourceLocation#getAbsoluteStart()}
     * will return the junk punctuation, whereas getContentStart will get the
     * content inside the punctuation.
     */
    public int getContentStart()
    {
        return getAbsoluteStart();
    }

    /**
     * Returns the offset after the last character of actual content. See
     * {@link #getContentStart()} for more.
     */
    public int getContentEnd()
    {
        return getAbsoluteEnd();
    }

    /**
     * Set this unit's position relative to its parent. Used in parsing.
     * 
     * @param parent MXML file containing the unit
     * @param index this unit's position in the list
     */
    public void setLocation(MXMLData parent, int index)
    {
        this.parent = parent;
        this.index = index;
        setSourcePath(parent.getPath());
    }

    /**
     * Sets the index of this tags hierarchical parent in its parents array of
     * MXMLUnitData objects
     * 
     * @param parentIndex The index of the parent tag.
     */
    public void setParentUnitDataIndex(int parentIndex)
    {
        this.parentIndex = parentIndex;
    }

    /**
     * Gets the {@link MXMLUnitData} which is the hierarchical parent of this
     * unit in its parents array of MXMLUnitData objects
     * 
     * @return the parent {@link MXMLUnitData} or null
     */
    public final MXMLUnitData getParentUnitData()
    {
        return parent.getUnit(parentIndex);
    }

    /**
     * Gets the index of this tags hierarchical parent in its parents array of
     * MXMLUnitData objects
     * 
     * @return the index, or -1
     */
    public final int getParentUnitDataIndex()
    {
        return parentIndex;
    }

    /**
     * Set this unit's start and end offsets. Used in parsing.
     * 
     * @param start start offset
     * @param end end offset
     */
    public void setOffsets(int start, int end)
    {
        setStart(start);
        setEnd(end);
    }

    /**
     * Adjust all associated offsets by the adjustment amount
     * 
     * @param offsetAdjustment amount to add to offsets
     */
    public void adjustOffsets(int offsetAdjustment)
    {
        setStart(getAbsoluteStart() + offsetAdjustment);
        setEnd(getAbsoluteEnd() + offsetAdjustment);
    }

    /**
     * Get this unit's source file.
     * 
     * @return An {@code IFileSpecification} representing the source file.
     */
    public final IFileSpecification getSource()
    {
        return new FileSpecification(getParent().getPath().toString());
    }

    /**
     * Get this unit's position in the MXMLData
     * 
     * @return index of this unit
     */
    public final int getIndex()
    {
        return index;
    }

    /**
     * Is this MXML unit a block of text?
     * 
     * @return true if the unit is a block of text
     */
    public boolean isText()
    {
        return false;
    }

    /**
     * Is this MXML unit a tag?
     * 
     * @return true if the unit is a tag
     */
    public boolean isTag()
    {
        return false;
    }

    /**
     * Is this MXML unit an open tag?
     * 
     * @return true if the unit is an open tag
     */
    public boolean isOpenTag()
    {
        return false;
    }

    /**
     * Is this MXML unit an open tag and not an empty tag (i.e. only
     * &lt;foo&gt;, not &ltfoo;/&gt;)?
     * 
     * @return true if the unit is an open tag, and not an empty tag
     */
    public boolean isOpenAndNotEmptyTag()
    {
        return false;
    }

    /**
     * Is this MXML unit an close tag?
     * 
     * @return true if the unit is a close tag
     */
    public boolean isCloseTag()
    {
        return false;
    }

    /**
     * Get the MXML file that contains this unit
     * 
     * @return the MXML file that contains this unit
     */
    public final MXMLData getParent()
    {
        return parent;
    }

    /**
     * Get the previous MXML unit
     * 
     * @return the previous MXML unit
     */
    public final MXMLUnitData getPrevious()
    {
        return parent.getUnit(index - 1);
    }

    /**
     * Get the next MXML unit
     * 
     * @return the next MXML unit
     */
    public final MXMLUnitData getNext()
    {
        return parent.getUnit(index + 1);
    }

    /**
     * Gets the next sibling unit after this unit. The next sibling unit may be
     * a tag or text. If there is no sibling unit after this one, this method
     * returns <code>null</code>.
     * 
     * @return The next sibling unit.
     */
    public final MXMLUnitData getNextSiblingUnit()
    {
        MXMLUnitData unit = this;

        if (isOpenAndNotEmptyTag())
            unit = ((MXMLTagData)unit).findMatchingEndTag();
        
        if (unit != null)
        {
            unit = unit.getNext();

            if (unit != null && (unit.getParentUnitData() != getParentUnitData()))
                unit = null;
        }
        return unit;
    }

    /**
     * Get the next tag.
     * 
     * @return the next tag, or null if none.
     */
    public final MXMLTagData getNextTag()
    {
        MXMLUnitData nextUnit = getNext();
        
        while (true)
        {
            if (nextUnit == null)
                return null;
            
            if (nextUnit.isTag())
                return (MXMLTagData)nextUnit;
            
            nextUnit = nextUnit.getNext();
        }
    }

    /**
     * Does this unit contain the given offset (excluding start and including
     * end)
     * 
     * @return true if the offset falls within this unit's bounds
     */
    public boolean containsOffset(int offset)
    {
        return MXMLData.contains(getAbsoluteStart(), getAbsoluteEnd(), offset);
    }

    /**
     * Get the nearest containing tag. Moving backwards through the list of
     * tokens for this MXML file, this is the first open tag that you find for
     * which you haven't found a corresponding close.
     * 
     * @return nearest containing open tag (or null, if no such open tag exists)
     */
    public final MXMLTagData getContainingTag(int offset)
    {
        FastStack<String> tagNames = new FastStack<String>();
        MXMLUnitData current = getPrevious();
        MXMLTagData containingTag = null;
        
        if (containsOffset(offset) && isCloseTag())
        {
            MXMLTagData tag = (MXMLTagData)this;
            tagNames.push(tag.getName());
        }
        
        while (current != null && containingTag == null)
        {
            if (current.isTag())
            {
                MXMLTagData currentTag = (MXMLTagData)current;
                
                if (currentTag.isCloseTag())
                {
                    tagNames.push(currentTag.getName());
                }
                else if (currentTag.isOpenTag() && !currentTag.isEmptyTag())
                {
                    String stackName = "";
                    while (stackName.compareTo(currentTag.getName()) != 0 && !tagNames.isEmpty())
                    {
                        stackName = tagNames.pop();
                    }
                    if (stackName.compareTo(currentTag.getName()) != 0)
                        containingTag = currentTag;
                }
            }
            
            current = current.getPrevious();
        }
        
        return containingTag;
    }

    /**
     * Returns an object representing the MXML diaclect used in the document
     * containing this unit.
     * 
     * @return An {@link MXMLDialect} object.
     */
    public MXMLDialect getMXMLDialect()
    {
        return getParent().getMXMLDialect();
    }

    protected String getTypeString()
    {
        return getClass().getSimpleName();
    }

    /**
     * For debugging only. This format is nice in a text file.
     */
    public String toDumpString()
    {
       return buildDumpString(false);
    }
    
    public String buildDumpString(boolean skipSrcPath) {
        StringBuilder sb = new StringBuilder();

        sb.append('[');
        sb.append(getIndex());
        sb.append(']');

        sb.append('\t');
        if(!skipSrcPath) {
            sb.append(getLine() + 1);
            sb.append('\t');
            sb.append(getColumn() + 1);
            sb.append('\t');
        }
        sb.append(getAbsoluteStart());
        sb.append('\t');
        sb.append(getAbsoluteEnd());
        sb.append('\t');

        String type = getTypeString();
        sb.append(type);
        int n = 32 - type.length();
        for (int i = 0; i < n; i++)
            sb.append(' ');
        sb.append('\t');

        sb.append('^');
        sb.append('[');
        sb.append(parentIndex);
        sb.append(']');

        return sb.toString();
    }

    /**
     * Verifies that this unit has its source location information set.
     * <p>
     * This is used only in asserts.
     */
    public boolean verify()
    {
        // Verify the source location.
        assert getSourcePath() != null : "MXMLUnitData has null source path: " + toString();
        assert getStart() != UNKNOWN : "MXMLUnitData has unknown start: " + toString();
        assert getEnd() != UNKNOWN : "MXMLUnitData has unknown end: " + toString();
        assert getLine() != UNKNOWN : "MXMLUnitData has unknown line: " + toString();
        assert getColumn() != UNKNOWN : "MXMLUnitData has unknown column: " + toString();

        return true;
    }
}
