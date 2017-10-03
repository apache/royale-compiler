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

package org.apache.royale.compiler.problems;

import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

/**
 * A DefineFontAlignZones record was found in an unexpected tag.
 *
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class SWFDefineFontAlignZonesLinkToIncorrectFontProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "DefineFontAlignZones cannot be used by font with id ${fontID}. The Problem was found at byte offset ${offset}";

    public static final int warningCode = 5024;
    /**
     * A DefineFontAlignZones record was found in an unexpected tag.
     * 
     * @param fontID The tag type that contains the DefineFontAlignZones record.
     * @param sourcePath The normalized path of the file
     * in which the problem occurred.
     * @param offset The byte offset where the problem occurred.
     */
    public SWFDefineFontAlignZonesLinkToIncorrectFontProblem(int fontID, 
            String sourcePath, long offset)
    {
        super(sourcePath);
        this.fontID = fontID;
        this.offset = offset;
    }
    
    public final int fontID;
    public final long offset;
}
