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

package org.apache.royale.compiler.internal.parsing;

/**
 * An {@code ISourceFragment} represents a fragment of source code.
 * <p>
 * These are currently used only in MXML processing, where fragments are created for
 * entities, comments, the beginning and end of CDATA, and the text between these.
 * <p>
 * The "physical" properties of a fragment describe how it actually appears
 * in the source code. For example, the physical text of an entity might be
 * "&lt;" and its physical starting offset, line number, and column number
 * would indicate its position within an MXML document.
 * Note that the physical end is the physical start plus the length
 * of the physical text.
 * <p>
 * The "logical" properties of a fragment describe what it means after MXML
 * pre-processing (entity evaluation, CDATA removal, MXML comment removal).
 */
public interface ISourceFragment
{
    /**
     * Gets the original text for this fragment, as it originally appeared in the file.
     */
    String getPhysicalText();
    
    /**
     * Gets the post-processed text for this fragment.
     */
    String getLogicalText();
    
    /**
     * Gets the starting offset where this fragment originally appeared in the file.
     */
    int getPhysicalStart();
    
    /**
     * Gets the line number where this fragment originally appeared in the file.
     */
    int getPhysicalLine();
    
    /**
     * Gets the column number where this fragment originally appeared in the file.
     */
    int getPhysicalColumn();
}
