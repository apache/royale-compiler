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
 * A Fill Style record has an invalid fill style type. 
 *
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class SWFUnknownFillStyleProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "${recordName} record contains a unknown fill style type of ${fillStyleType} at byte offset ${offset}";

    public static final int warningCode = 5027;
    /**
     * @param fillStyleType The fillStyleType field in the FillStyle record.
     * @param sourcePath The normalized path of the file
     * in which the problem occurred.
     * @param offset The byte offset where the problem occurred.
     */
    public SWFUnknownFillStyleProblem(int fillStyleType, boolean isMorphStyle, String sourcePath,
            long offset)
    {
        super(sourcePath);
        this.fillStyleType = fillStyleType;
        this.offset = offset;
        this.recordName = isMorphStyle ? "MorphFillStyle" : "FillStyle";
    }
    
    public final int fillStyleType;
    public final long offset;
    public final String recordName;
}
