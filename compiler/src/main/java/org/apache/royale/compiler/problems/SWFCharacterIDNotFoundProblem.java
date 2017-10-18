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
 * A Character ID was not found in the SWF's dictionary. 
 *
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class SWFCharacterIDNotFoundProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "Character ID ${characterID} in tag type ${tagType} at byte offset ${offset} was not found in the SWF's dictionary";

    public static final int warningCode = 5023;
    /**
     * @param characterID the missing character ID.
     * @param sourcePath The normalized path of the file
     * in which the problem occurred.
     */
    public SWFCharacterIDNotFoundProblem(int characterID, int tagType, String sourcePath,
            long offset)
    {
        super(sourcePath);
        this.characterID = characterID;
        this.tagType = tagType;
        this.offset = offset;
    }
    
    public final int characterID;
    public final int tagType;
    public final long offset;

}
