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

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.ScriptInfo;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;

/**
 * An abstract base class implementation of {@link IDiagnosticsVisitor}. Rather
 * than implementing {@link IDiagnosticsVisitor} clients of AET should sub-class
 * this class so that methods can be added to {@link IDiagnosticsVisitor} without
 * having to update all implementations of {@link IDiagnosticsVisitor}.
 */
public abstract class AbstractDiagnosticVisitor implements IDiagnosticsVisitor
{
    @Override
    public void operandStackUnderflow(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block, int instructionIndex)
    {
    }

    @Override
    public void scopeStackUnderflow(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block, int instructionIndex)
    {
    }

    @Override
    public void unreachableBlock(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block)
    {
    }

    @Override
    public void tooManyDefaultParameters(MethodInfo methodInfo)
    {
    }

    @Override
    public void incorrectNumberOfParameterNames(MethodInfo methodInfo)
    {
    }

    @Override
    public void nativeMethodWithMethodBody(MethodInfo methodInfo, MethodBodyInfo methodBodyInfo)
    {
    }

    @Override
    public void scriptInitWithRequiredArguments(ScriptInfo scriptInfo, MethodInfo methodInfo)
    {
    }
}
