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
import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.common.XMLName;

/**
 * Represents an open tag, a close tag, or an empty tag in MXML.
 */
public interface IMXMLTagData extends IMXMLUnitData
{
    /**
     * Gets the tag that contains this tag.
     * <p>
     * If the document is not balanced before this tag, this method returns
     * <code>null</code>.
     * 
     * @return An {@code IMXMLTagData} object.
     */
    IMXMLTagData getParentTag();

    /**
     * Determines whether this tag is empty.
     * 
     * @return <code>true</code> if it is.</code>
     */
    boolean isEmptyTag();

    /**
     * Determines whether this tag is a close tag.
     * 
     * @return <code>true</code> if it is.</code>
     */
    boolean isCloseTag();

    /**
     * Determines whether this tag has an actual close tag, and was not closed
     * as a post-process step of MXML repair.
     * 
     * @return If we have an explicit close tag.
     */
    boolean hasExplicitCloseTag();

    /**
     * Gets the name of this tag as written, such as <code>"s:width.over"</code>
     * .
     * 
     * @return The complete tag name, including a possible prefix and state.
     */
    String getName();

    /**
     * Gets the prefix of this tag.
     * <p>
     * If the tag does not have a prefix, this method returns <code>null</code>.
     * 
     * @return The prefix as a String, or <code>null</code>.
     */
    String getPrefix();

    /**
     * Gets the namespace mappings specified on this tag.
     * 
     * @return A prefix map.
     */
    PrefixMap getPrefixMap();

    /**
     * Gets the namespace mapping that apply to this tag, taking ancestor tags
     * into account.
     * 
     * @return A prefix map.
     */
    PrefixMap getCompositePrefixMap();

    /**
     * Gets the URI of this tag.
     * <p>
     * If the tag does not have a prefix, this method returns <code>null</code>.
     * 
     * @return The URI as a String, or <code>null</code>.
     */
    String getURI();

    /**
     * Gets the short name of this tag, not including the prefix or state.
     * 
     * @return The short tag name as a <code>String</code>
     */
    String getShortName();

    /**
     * Gets the name of this tag as an {@code XMLName}.
     * <p>
     * This takes into account the prefix but not the state.
     * 
     * @return The tag name as an {@code XMLName}.
     */
    XMLName getXMLName();

    /**
     * Gets the state name for this tag.
     * 
     * @return The state name as a <code>String</code>
     */
    String getStateName();

    /**
     * Determines whether the specified offset falls inside the attribute list
     * of this tag.
     * 
     * @param offset The offset to test.
     * @return <code>true</code> if the offset falls inside this tag's attribute
     * list.
     */
    boolean isOffsetInAttributeList(int offset);

    /**
     * Gets all of the attributes in this tag.
     * 
     * @return All of the attributes as a array of {@code IMXMLTagAttributeData}
     * objects.
     */
    IMXMLTagAttributeData[] getAttributeDatas();

    /**
     * Gets the attribute in this tag that has the specified name.
     * 
     * @param attributeName The name of the attribute.
     * @return The attribute, or <code>null</code> if no such attribute exists.
     */
    IMXMLTagAttributeData getTagAttributeData(String attributeName);

    /**
     * Gets the attribute value in this tag for the specified attribute name.
     * <p>
     * This value does not include the quotes.
     * 
     * @param attributeName The name of the attribute.
     * @return The value of the attribute, or <code>null</code> if no such
     * attribute exists.
     */
    // TODO Rename to getAttributevalue()
    String getRawAttributeValue(String attributeName);

    /**
     * Gets the source location of this tag's child units.
     * 
     * @return A source location.
     */
    ISourceLocation getLocationOfChildUnits();

    /**
     * Gets the first child unit inside this tag.
     * <p>
     * The child unit may be a tag or text. If there is no child unit, this
     * method returns <code>null</code>.
     * 
     * @return The first child unit inside this tag.
     */
    IMXMLUnitData getFirstChildUnit();

    /**
     * Gets the first child open tag of this tag.
     * <p>
     * "First child" means the first open (or maybe empty) tag found before a
     * close tag. If this is a close tag, this method starts looking after the
     * corresponding start tag.
     * 
     * @return Child tag, or null if none.
     */
    IMXMLTagData getFirstChild(boolean includeEmptyTags);

    /**
     * Gets the next sibling open tag of this tag.
     * <p>
     * "Sibling" is defined as the first open (or maybe empty) tag after this
     * tag's close tag. If this is a close tag, this method starts looking after
     * the corresponding start tag.
     * 
     * @return Sibling, or null if none.
     */
    IMXMLTagData getNextSibling(boolean includeEmptyTags);

    /**
     * Finds the close tag that matches this tag.
     * <p>
     * Returns null if this tag is a close or empty tag.
     * <p>
     * Returns null if a surrounding tag is unbalanced; this is determined by
     * backing up to the innermost parent tag with a different tag.
     * <p>
     * {@code <a> <b> <b> <-- find matching for this one will return null
     * </b> </a> * }
     */
    IMXMLTagData findMatchingEndTag();

    /**
     * Gets this compilable text inside this tag. The compilable text may appear
     * as multiple child text units, and there may also be comments (which are
     * ignored). If any child units are tags rather than text, they are simply
     * ignored.
     * 
     * @return The compilable text inside this tag.
     */
    String getCompilableText();

    /**
     * Determines whether this tag does not actually exist within the MXML
     * document that is its source.
     * 
     * @return <code>true</code> if this tag is implicit.
     */
    boolean isImplicit();
}
