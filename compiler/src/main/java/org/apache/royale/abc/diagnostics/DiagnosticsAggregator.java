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

package org.apache.royale.abc.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.ScriptInfo;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;

/**
 *  A DiagnosticsAggregator collects AET diagnostics for analysis.
 */
public class DiagnosticsAggregator implements IDiagnosticsVisitor
{
    private final List<ProblemDescription> errors = new ArrayList<ProblemDescription>();
    private final List<ProblemDescription> warnings = new ArrayList<ProblemDescription>();

    /**
     * Get problem-level diagnostics seen by this visitor.
     * @return problems encountered, as an immutable list.
     */
    public List<ProblemDescription> getErrors()
    {
        return Collections.unmodifiableList(this.errors);
    }

    /**
     * Get warning-level diagnostics seen by this visitor.
     * @return warnings encountered, as an immutable list.
     */
    public List<ProblemDescription> getWarnings()
    {
        return Collections.unmodifiableList(this.warnings);
    }

    @Override
    public void operandStackUnderflow(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block, int instructionIndex)
    {
        errors.add(new ProblemDescription(ProblemDescription.ProblemType.OperandStackUnderflow, cfg, block, instructionIndex));
    }

    @Override
    public void scopeStackUnderflow(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block, int instructionIndex)
    {
        errors.add(new ProblemDescription(ProblemDescription.ProblemType.ScopeStackUnderflow, cfg, block, instructionIndex));
    }

    @Override
    public void unreachableBlock(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block)
    {
        warnings.add(new ProblemDescription(ProblemDescription.ProblemType.UnreachableBlock, cfg, block, block.size() - 1));
    }

    @Override
    public void tooManyDefaultParameters(MethodInfo methodInfo)
    {
        errors.add(new ProblemDescription(ProblemDescription.ProblemType.TooManyDefaultParameters, methodInfo));
    }

    @Override
    public void incorrectNumberOfParameterNames(MethodInfo methodInfo)
    {
        errors.add(new ProblemDescription(ProblemDescription.ProblemType.IncorrectNumberOfParameterNames, methodInfo));
    }

    @Override
    public void nativeMethodWithMethodBody(MethodInfo methodInfo, MethodBodyInfo methodBodyInfo)
    {
        errors.add(new ProblemDescription(ProblemDescription.ProblemType.NativeMethodWithMethodBody, methodInfo));
    }

    @Override
    public void scriptInitWithRequiredArguments(ScriptInfo scriptInfo, MethodInfo methodInfo)
    {
        errors.add(new ProblemDescription(ProblemDescription.ProblemType.ScriptInitWithRequiredArguments, scriptInfo, methodInfo));
    }

    /**
     *  A ProblemDescription holds information about a particular problem occurrence.
     */
    @SuppressWarnings("nls")
    public static class ProblemDescription
    {
        /**
         *  The type of problem observed.
         */
        public enum ProblemType
        {
            OperandStackUnderflow,
            ScopeStackUnderflow,
            UnreachableBlock,
            TooManyDefaultParameters,
            IncorrectNumberOfParameterNames,
            NativeMethodWithMethodBody,
            ScriptInitWithRequiredArguments
        }

        /**
         * Construct a description of a problem that occurred in a method body.
         * @param problemType - the type of problem encountered.
         * @param cfg - the control flow graph.
         * @param b - the block where the problem occurred.
         * @param offset - the offset of the instruction where the problem occurred.
         */
        private ProblemDescription(ProblemType problemType, IFlowgraph cfg, IBasicBlock b, int offset)
        {
            switch ( problemType )
            {
                case OperandStackUnderflow:
                case ScopeStackUnderflow:
                case UnreachableBlock:
                    this.problemType = problemType;
                    this.sourcePath  = cfg.findSourcePath(b, offset);
                    this.lineNumber  = cfg.findLineNumber(b, offset);
                    this.methodInfo  = null;
                    this.scriptInfo  = null;
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid problem type %s", problemType));
            }
        }

        /**
         * Construct a description of a problem that occurred in a method header.
         * @param problemType - the type of problem that occurred.
         * @param methodInfo - the method header where the problem occurred.
         */
        private ProblemDescription(ProblemType problemType, MethodInfo methodInfo)
        {
            switch(problemType)
            {
                case TooManyDefaultParameters:
                case IncorrectNumberOfParameterNames:
                case NativeMethodWithMethodBody:
                    this.problemType = problemType;
                    this.methodInfo  = methodInfo;
        
                    this.scriptInfo  = null;
                    this.sourcePath  = null;
                    this.lineNumber  = -1;
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid problem type %s", problemType));
            }
        }

        /**
         * Construct a description of a problem that occurred due to a mismatch
         * between the script info header and the corresponding method header.
         * @param problemType - the type of problem that occurred.
         * @param scriptInfo - the script info header.
         * @param methodInfo - the method info header.
         */
        private ProblemDescription(ProblemType problemType, ScriptInfo scriptInfo, MethodInfo methodInfo)
        {
            switch(problemType)
            {
                case ScriptInitWithRequiredArguments:
                    this.problemType = problemType;
                    this.methodInfo  = methodInfo;
                    this.scriptInfo  = scriptInfo;
        
                    this.sourcePath  = null;
                    this.lineNumber  = -1;
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid problem type %s", problemType));
            }
        }

        /**
         * The type of problem encountered.
         */
        public final ProblemType problemType;

        /**
         * Source path to the problem's occurrence, where known and applicable.
         */
        public final String sourcePath;

        /**
         * Line number of the problem's occurrence, where known and applicable.
         */
        public final int lineNumber;

        /**
         *  MethodInfo of the problem's occurrence, where known and applicable.
         */
        public final MethodInfo methodInfo;

        /**
         *  ScriptInfo of the problem's occurrence, where known and applicable.
         */
        public final ScriptInfo scriptInfo;
    }
}
