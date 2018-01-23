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

import org.apache.royale.compiler.common.SourceLocation;

/**
 * Represents an FXG problem. This abstract class is the super class of all FXG related problems.
 */
public abstract class FXGProblem extends CompilerProblem
{
    /**
     * Constructor.
     * 
     * @param filePath The normalized path of the file in which the problem occurred.
     * @param line The line number within the source buffer at which the problem starts.
     * @param column The column number within the source buffer at which the problem starts.
     */
    protected FXGProblem(String filePath, int line, int column)
    {
        super(filePath, SourceLocation.UNKNOWN, SourceLocation.UNKNOWN, line, column);
    }
    
    /**
     * Constructor.
     * 
     * @param filePath The normalized path of the file in which the problem occurred.
     */
    protected FXGProblem(String filePath)
    {
        super(filePath);
    }
}
