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

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.common.SourceLocation;
import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

/**
 * Base class for internal compiler problems that are created
 * when AET determined the code generator produced obviously
 * invalid byte code.
 */
@DefaultSeverity(CompilerProblemSeverity.ERROR)
public abstract class InvalidByteCodeGeneratedProblem extends CompilerProblem
{
    protected InvalidByteCodeGeneratedProblem(ISourceLocation location)
    {
        super(location);
    }
    
    /**
     * Helper method that will use debug op code information to deduce a source
     * location from an instruction offset in a Block in a
     * {@link IFlowgraph}.
     * 
     * @param cfg {@link IFlowgraph} containing the specified
     * Block.
     * @param b Block containing the instruction to deduce a
     * {@link ISourceLocation} for.
     * @param instructionIndex Offset in the specified Block of the
     * instruction to deduce a {@link ISourceLocation} for.
     * @return The deduced {@link ISourceLocation}.
     */
    public static ISourceLocation computeSourceLocationForBlockAndInstruction(IFlowgraph cfg, IBasicBlock b, int instructionIndex)
    {
        String currentFileName = cfg.findSourcePath(b, instructionIndex);
        int currentLine = cfg.findLineNumber(b, instructionIndex);

        if ( currentFileName != null && currentLine != -1 )
            return new SourceLocation(currentFileName, ISourceLocation.UNKNOWN, ISourceLocation.UNKNOWN, currentLine, ISourceLocation.UNKNOWN);
        else
            return new SourceLocation();
    }
}
