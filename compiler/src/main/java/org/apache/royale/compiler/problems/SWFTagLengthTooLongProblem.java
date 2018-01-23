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
 * The SWF Tag length is set longer than need to read all of the data in the tag. 
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class SWFTagLengthTooLongProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "A SWF Tag of type ${type} contains ${extraBytes} bytes of unread data at the end of the tag at byte offset ${offset}";

    public static final int warningCode = 5025;
    
    /**
     * The SWF Tag is longer than it should be.
     * 
     * @param type The tag type.
     * @param sourcePath The normalized path of the file
     * in which the problem occurred.
     * @param offset The byte offset of the start of the extra data.
     * @param tagEndOffset The byte offset of the end of the tag.
     */
    public SWFTagLengthTooLongProblem(int type, String sourcePath,
            long offset, long tagEndOffset)
    {
        super(sourcePath);
        this.type = type;
        this.offset = offset;
        this.extraBytes = tagEndOffset - offset;
    }
    
    public final int type;
    public final long offset;
    public final long extraBytes;
    
}
