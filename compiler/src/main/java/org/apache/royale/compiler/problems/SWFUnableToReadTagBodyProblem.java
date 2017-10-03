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
 * Problem reading the a tag's body. The most likely problem is reading the tag body
 * exceeded the length of the tag.
 *
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class SWFUnableToReadTagBodyProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "Unable to read the tag body of tag with type = ${tagType} and length = ${tagLength} at byte offset ${offset}";

    public static final int warningCode = 5026;
    /**
     * @param tagType The tagType from the tag's header.
     * @param tagLength The length of the tag from the tag's header.
     * @param sourcePath The normalized path of the file
     * in which the problem occurred.
     * @param offset The byte offset where the problem occurred.
     */
    public SWFUnableToReadTagBodyProblem(int tagType, int tagLength, String sourcePath,
            long offset)
    {
        super(sourcePath);
        this.tagType = tagType;
        this.tagLength = tagLength;
        this.offset = offset;
    }
    
    public final int tagType;
    public final int tagLength;
    public final long offset;
}
