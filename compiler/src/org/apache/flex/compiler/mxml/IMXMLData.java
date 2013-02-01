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

import java.util.Collection;

import org.apache.flex.compiler.internal.mxml.MXMLDialect;
import org.apache.flex.compiler.problems.ICompilerProblem;

/**
 * Encapsulation of an MXML file, with individual units for each open tag, close tag,
 * and block of text.
 */
public interface IMXMLData
{
    /**
     * Gets the  path to the file on disk that created this {@link MXMLData}.
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
     * @return The specified MXML unit, or <code>null</code> if the index is out of range.
     */
    MXMLUnitData getUnit(int i);
    
    /**
     * Gets the compiler problems found during the creation of this {@code MXMLData}.
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
    MXMLTagData getRootTag();
}
