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

import java.util.Collection;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents an attribute in MXML.
 */
public interface IMXMLTagAttributeData extends ISourceLocation
{
    /**
     * Gets the MXML dialect used in the document containing this attribute.
     * 
     * @return An {@link MXMLDialect} object.
     */
    MXMLDialect getMXMLDialect();

    /**
     * Gets the tag to which this attribute belongs.
     */
    IMXMLTagData getParent();

    /**
     * Gets the name of this attribute as written, such as
     * <code>"s:width.over"</code>.
     * 
     * @return The complete attribute name, including a possible prefix and
     * state.
     */
    String getName();

    /**
     * Gets the prefix of this attribute.
     * <p>
     * If the attribute does not have a prefix, this method returns
     * <code>null</code>.
     * 
     * @return The prefix as a String, or <code>null</code>.
     */
    String getPrefix();

    /**
     * Gets the URI of this attribute.
     * <p>
     * If the attribute does not have a prefix, this method returns
     * <code>null</code>.
     * 
     * @return The URI as a String, or <code>null</code>.
     */
    String getURI();

    /**
     * Gets the short name of this attribute, not including the prefix or state.
     * 
     * @return The short attribute name as a <code>String</code>
     */
    String getShortName();

    /**
     * Gets the name of this attribute as an {@code XMLName}.
     * <p>
     * This takes into account the prefix but not the state.
     * 
     * @return The attribute name as an {@code XMLName}.
     */
    XMLName getXMLName();

    /**
     * Gets the state name for this attribute.
     * 
     * @return The state name as a <code>String</code>
     */
    String getStateName();

    /**
     * Returns <code>true</code> if this attribute has the specified short name
     * and it either has no prefix or has a prefix that maps to the language
     * URI.
     * 
     * @return <code>true</code> or <code>false</code>.
     */
    boolean isSpecialAttribute(String name);

    /**
     * Returns <code>true</code> if this attribute has a value.
     * 
     * @return <code>true</code> or <code>false</code>.
     */
    boolean hasValue();

    /**
     * Gets the value of this attribute as a String.
     * <p>
     * The delimiting quotes are not included.
     * 
     * @return The attribute value.
     */
    // TODO Rename this to getValue()
    String getRawValue();

    /**
     * Gets the galue of this attribute as an array of source fragments.
     * 
     * @param problems The collection of compiler problems to which problems are
     * to be added.
     * @return An array of source fragments.
     */
    ISourceFragment[] getValueFragments(Collection<ICompilerProblem> problems);

    /**
     * Gets the starting offset of the value of this attribute.
     * 
     * @return The starting offset.
     */
    int getValueStart();

    /**
     * Gets the ending offset of the value of this attribute.
     * 
     * @return The ending offset.
     */
    int getValueEnd();

    /**
     * Gets the line number of the start of the value of this attribute.
     * 
     * @return The ending offset.
     */
    int getValueLine();

    /**
     * Gets the column number of the start of the value of this attribute.
     * 
     * @return The ending offset.
     */
    int getValueColumn();

    /**
     * Gets the source location of the start of the value of this attribute.
     * 
     * @return The sourced location.
     */
    ISourceLocation getValueLocation();
}
