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

package org.apache.royale.compiler.internal.mxml;

import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.mxml.IMXMLData;
import org.apache.royale.compiler.mxml.IMXMLTagData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.apache.royale.utils.FastStack;

/**
 * Encapsulation of an MXML unit: an open/close/empty tag, a chunk of text
 * (perhaps whitespace, CDATA, or a comment), or a processing instruction.
 * <p>
 * An {@link MXMLData} object stores a linear list of MXML units, but it is
 * possible to walk them in a hierarchical way.
 */
public abstract class MXMLUnitData extends SourceLocation implements
        IMXMLUnitData
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
     * This position of this unit in the linear list of units owned by the
     * {@link MXMLData}.
     */
    protected int index;

    /**
     * The position of this unit's parent unit in the linear list of units owned
     * by the {@link MXMLData}.
     */
    private int parentIndex;

    @Override
    public int getContentStart()
    {
        return getAbsoluteStart();
    }

    @Override
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

    @Override
    public final IMXMLUnitData getParentUnitData()
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

    @Override
    public final IFileSpecification getSource()
    {
        return new FileSpecification(getParent().getPath().toString());
    }

    @Override
    public final int getIndex()
    {
        return index;
    }

    @Override
    public boolean isText()
    {
        return false;
    }

    @Override
    public boolean isTag()
    {
        return false;
    }

    @Override
    public boolean isOpenTag()
    {
        return false;
    }

    @Override
    public boolean isOpenAndNotEmptyTag()
    {
        return false;
    }

    @Override
    public boolean isCloseTag()
    {
        return false;
    }

    @Override
    public final IMXMLData getParent()
    {
        return parent;
    }

    @Override
    public final IMXMLUnitData getPrevious()
    {
        return parent.getUnit(index - 1);
    }

    @Override
    public final IMXMLUnitData getNext()
    {
        return parent.getUnit(index + 1);
    }

    @Override
    public final IMXMLUnitData getNextSiblingUnit()
    {
        IMXMLUnitData unit = this;

        if (isOpenAndNotEmptyTag())
            unit = ((IMXMLTagData)unit).findMatchingEndTag();

        if (unit != null)
        {
            unit = unit.getNext();

            if (unit != null && (unit.getParentUnitData() != getParentUnitData()))
                unit = null;
        }
        return unit;
    }

    @Override
    public final IMXMLTagData getNextTag()
    {
        IMXMLUnitData nextUnit = getNext();

        while (true)
        {
            if (nextUnit == null)
                return null;

            if (nextUnit.isTag())
                return (IMXMLTagData)nextUnit;

            nextUnit = nextUnit.getNext();
        }
    }

    @Override
    public boolean containsOffset(int offset)
    {
        return MXMLData.contains(getAbsoluteStart(), getAbsoluteEnd(), offset);
    }

    @Override
    public final IMXMLTagData getContainingTag(int offset)
    {
        FastStack<String> tagNames = new FastStack<String>();
        IMXMLUnitData current = getPrevious();
        IMXMLTagData containingTag = null;

        if (containsOffset(offset) && isCloseTag())
        {
            IMXMLTagData tag = (IMXMLTagData)this;
            tagNames.push(tag.getName());
        }

        while (current != null && containingTag == null)
        {
            if (current.isTag())
            {
                IMXMLTagData currentTag = (IMXMLTagData)current;

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

    @Override
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

    public String buildDumpString(boolean skipSrcPath)
    {
        StringBuilder sb = new StringBuilder();

        sb.append('[');
        sb.append(getIndex());
        sb.append(']');

        sb.append('\t');
        if (!skipSrcPath)
        {
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
