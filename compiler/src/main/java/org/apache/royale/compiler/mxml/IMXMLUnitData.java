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

package org.apache.royale.compiler.mxml;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;

/**
 * Represents one unit of MXML.
 */
public interface IMXMLUnitData extends ISourceLocation
{
    /**
     * Gets the source file that contains this unit.
     * 
     * @return An {@code IFileSpecification} representing the source file.
     */
    IFileSpecification getSource();

    /**
     * Gets the {@code IMXMLData} representing the MXML document that contains
     * this unit.
     * 
     * @return The {@code IMXMLData} that contains this unit.
     */
    IMXMLData getParent();

    /**
     * Returns the MXML dialect used in the MXML document that contains this
     * unit.
     * 
     * @return An {@link MXMLDialect} object.
     */
    MXMLDialect getMXMLDialect();

    /**
     * Get this unit's position in the {@code IMXMLData}.
     * 
     * @return The index of this unit.
     */
    int getIndex();

    /**
     * Does this unit contain the given offset (excluding start and including
     * end)?
     * 
     * @return true if the offset falls within this unit's bounds.
     */
    boolean containsOffset(int offset);

    /**
     * Gets the {@link IMXMLUnitData} which is the hierarchical parent of this
     * unit in its parents array of IMXMLUnitData objects
     * 
     * @return the parent {@link IMXMLUnitData} or <code>null</code>.
     */
    IMXMLUnitData getParentUnitData();

    /**
     * Get the nearest containing tag. Moving backwards through the list of
     * tokens for this MXML file, this is the first open tag that you find for
     * which you haven't found a corresponding close.
     * 
     * @return nearest containing open tag (or null, if no such open tag exists)
     */
    IMXMLTagData getContainingTag(int offset);

    /**
     * Is this MXML unit a block of text?
     * 
     * @return true if the unit is a block of text
     */
    boolean isText();

    /**
     * Is this MXML unit a tag?
     * 
     * @return true if the unit is a tag
     */
    boolean isTag();

    /**
     * Is this MXML unit an open tag?
     * 
     * @return true if the unit is an open tag
     */
    boolean isOpenTag();

    /**
     * Is this MXML unit an open tag and not an empty tag (i.e. only
     * &lt;foo&gt;, not &ltfoo;/&gt;)?
     * 
     * @return true if the unit is an open tag, and not an empty tag
     */
    boolean isOpenAndNotEmptyTag();

    /**
     * Is this MXML unit an close tag?
     * 
     * @return true if the unit is a close tag
     */
    boolean isCloseTag();

    /**
     * Returns the first character of the actual content of the unit For most
     * units this is the same as getStart(), but for things like tags which have
     * "junk punctuation" around them, {@link SourceLocation#getAbsoluteStart()}
     * will return the junk punctuation, whereas getContentStart will get the
     * content inside the punctuation.
     */
    int getContentStart();

    /**
     * Returns the offset after the last character of actual content. See
     * {@link #getContentStart()} for more.
     */
    int getContentEnd();

    /**
     * Gets the next MXML unit.
     * 
     * @return The next MXML unit.
     */
    IMXMLUnitData getNext();

    /**
     * Gets the next sibling unit after this unit. The next sibling unit may be
     * a tag or text. If there is no sibling unit after this one, this method
     * returns <code>null</code>.
     * 
     * @return The next sibling unit.
     */
    IMXMLUnitData getNextSiblingUnit();

    /**
     * Gets the next tag.
     * 
     * @return The next tag, or null if none.
     */
    IMXMLTagData getNextTag();

    /**
     * Gets the previous MXML unit.
     * 
     * @return the previous MXML unit.
     */
    IMXMLUnitData getPrevious();
}
