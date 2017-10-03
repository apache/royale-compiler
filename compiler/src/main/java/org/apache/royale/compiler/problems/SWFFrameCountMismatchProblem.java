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
 * Warning reported after reading a SWF if the number of ShowFrame tags in the 
 * SWF does not match the number of frames declared in the header.
 * 
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class SWFFrameCountMismatchProblem extends CompilerProblem
{
    public static final String DESCRIPTION = "The frame count in the SWF header does not match the number of frames found in the SWF. Expected ${expectedFrames}, found ${actualFrames}";

    public static final int warningCode = 5041;
    
    /**
     * @param sourcePath The normalized path of the SWF file.
     */
    public SWFFrameCountMismatchProblem(int expectedFrames, int actualFrames, String sourcePath)
    {
        super(sourcePath);
        this.expectedFrames = expectedFrames;
        this.actualFrames = actualFrames;
    }
    
    public final int expectedFrames;
    public final int actualFrames;
}
