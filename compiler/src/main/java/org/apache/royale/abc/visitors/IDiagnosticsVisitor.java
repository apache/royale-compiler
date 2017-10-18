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

package org.apache.royale.abc.visitors;

import org.apache.royale.abc.ABCEmitter;
import org.apache.royale.abc.diagnostics.AbstractDiagnosticVisitor;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.ScriptInfo;

/**
 * IVisitor that is called by the {@link ABCEmitter} to notify clients of issues
 * it encountered that will most likely cause the abc created by the
 * {@link ABCEmitter} to fail verification in the VM.
 * <p>
 * Clients should not directly implement this method unless absolutely
 * necessary, instead the should sub-class {@link AbstractDiagnosticVisitor}.
 * Methods may be added to this interface in future versions of the AET.
 */
public interface IDiagnosticsVisitor
{
    /**
     * An underflow of operand stack was detected in a basic block in a method.
     * 
     * @param methodBodyInfo {@link MethodBodyInfo} for the method body with the
     * stack underflow.
     * @param cfg {@link IFlowgraph} for the method body with the stack
     * underflow.
     * @param block {@link IBasicBlock} in the {@link IFlowgraph} that contains
     * the stack underflow.
     * @param instructionIndex The index of the instruction in the {@link IBasicBlock}
     * that contains the stack underflow.
     */
    void operandStackUnderflow(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block, int instructionIndex);

    /**
     * An underflow of scope stack was detected in a basic block in a method.
     * 
     * @param methodBodyInfo {@link MethodBodyInfo} for the method body with the
     * stack underflow.
     * @param cfg {@link IFlowgraph} for the method body with the stack
     * underflow.
     * @param block {@link IBasicBlock} in the {@link IFlowgraph} that contains
     * the stack underflow.
     * @param instructionIndex The index of the instruction in the {@link IBasicBlock}
     * that contains the stack underflow.
     */
    void scopeStackUnderflow(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block, int instructionIndex);

    /**
     *  A basic block in a method has been found to be unreachable.
     * @param methodBodyInfo {@link MethodBodyInfo} for the method body
     * with the unreachable block.
     * @param cfg {@link IFlowgraph} for the method body with the 
     * unreachable block.
     * @param block {@link IBasicBlock} in the {@link IFlowgraph} 
     * that contains the unreachable block.
     */
    void unreachableBlock(MethodBodyInfo methodBodyInfo, IFlowgraph cfg, IBasicBlock block);

    /**
     * A {@link MethodInfo} has too many default parameters.
     * 
     * @param methodInfo The {@link MethodInfo} that has too many default
     * parameters.
     */
    void tooManyDefaultParameters(MethodInfo methodInfo);

    /**
     * A {@link MethodInfo} has a different number of parameter names than
     * parameter types.
     * 
     * @param methodInfo The {@link MethodInfo} that has the incorrect number of
     * parameter names.
     */
    void incorrectNumberOfParameterNames(MethodInfo methodInfo);

    /**
     * A {@link MethodInfo} for a native method also has an associated
     * {@link MethodBodyInfo}.
     * 
     * @param methodInfo The {@link MethodInfo} that is marked as a native
     * method.
     * @param methodBodyInfo The {@link MethodBodyInfo} associated with the
     * native method.
     */
    void nativeMethodWithMethodBody(MethodInfo methodInfo, MethodBodyInfo methodBodyInfo);

    /**
     * A {@link MethodInfo} for the init method of a {@link ScriptInfo} has
     * required arguments.
     * 
     * @param scriptInfo The {@link ScriptInfo} whose init method has required
     * arguments.
     * @param methodInfo The {@link MethodInfo} for the init method of the
     * specified {@link ScriptInfo}.
     */
    void scriptInitWithRequiredArguments(ScriptInfo scriptInfo, MethodInfo methodInfo);
}
