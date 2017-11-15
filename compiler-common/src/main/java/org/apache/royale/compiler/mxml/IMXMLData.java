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

import org.apache.royale.compiler.common.PrefixMap;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.mxml.MXMLDialect;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Represents the syntax, but not the semantics, of an MXML file.
 * <p>
 * The file is represented as a sequence of "units", one for each open tag,
 * close tag, and block of text.
 * <p>
 * No meaning is assigned to any tag, attribute, or text.
 */
public interface IMXMLData
{
    /**
     * Gets the file on disk that created this MXMLData.
     * 
     * @return The file as an {@code IFileSpecification}.
     */
    IFileSpecification getFileSpecification();

    /**
     * Gets the path to the file on disk that created this MXMLData.
     * 
     * @return The path as a {@code String}.
     */
    String getPath();

    /**
     * Gets the MXML dialect being used.
     * 
     * @return An {@code MXMLDialect}.
     */
    MXMLDialect getMXMLDialect();

    /**
     * Get the MXML units found in this {@link IMXMLData}.
     * 
     * @return An array of the {@link IMXMLUnitData}.
     */
    IMXMLUnitData[] getUnits();

    /**
     * Gets the number of MXML "units".
     * <p>
     * Each unit represents an open tag, a close tag, a block of text, etc.
     * 
     * @return The number of {@code MXMLUnitData} objects you can retrieve with
     * {@code getUnit()}.
     */
    int getNumUnits();

    /**
     * Gets an MXML unit by index.
     * 
     * @param i The index into the list of MXML units.
     * @return The specified MXML unit, or <code>null</code> if the index is out
     * of range.
     */
    IMXMLUnitData getUnit(int i);

    /**
     * Gets the compiler problems found during the creation of this
     * MXMLData
     * 
     * @return A collection of {@code ICompilerProblem} objects.
     */
    Collection<ICompilerProblem> getProblems();

    /**
     * Gets the ending offset of the last unit.
     */
    int getEnd();

    /**
     * Gets the root tag.
     * 
     * @return An {@code MXMLTagData} for the root tag.
     */
    IMXMLTagData getRootTag();

    /**
     * Similar to findTagContainingOffset, but if the unit inside offset is a
     * text node, will return the surrounding tag instead.
     * 
     * @param offset offset
     * @return the containing/surrounding tag, or null if one can not be found
     */
    IMXMLTagData findTagOrSurroundingTagContainingOffset(int offset);

    /**
     * Returns the PrefixMap for the given {@link IMXMLTagData}. This will not
     * walk up the chain of prefix maps if this tag does not physically have
     * uri->namespace mappings.
     * 
     * @param data the {@link IMXMLTagData} to find the {@link PrefixMap} for
     * @return a {@link PrefixMap} or null
     */
    PrefixMap getPrefixMapForData(IMXMLTagData data);
}
