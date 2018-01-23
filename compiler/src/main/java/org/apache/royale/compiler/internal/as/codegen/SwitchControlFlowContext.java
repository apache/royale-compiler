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

package org.apache.royale.compiler.internal.as.codegen;

import org.apache.royale.abc.semantics.Label;
import org.apache.royale.compiler.internal.tree.as.SwitchNode;

/**
 * There is an active SwitchControlFlowContext for the syntactic region of each
 * switch statement. The SwitchControlFlowContext regions are pushed and popped of the
 * activation stack in the {@link ControlFlowContextManager} by the appropriate
 * reduction actions. A context is active while the switch statement's subtrees are reduced;
 * the SwitchControlFlowContext is set up by calling
 * {@link ControlFlowContextManager#startSwitchContext(SwitchNode)} in the
 * reduction's Prologue. The SwitchControlFlowContext is torn down by calling
 * {@code ControlFlowContextManager#finishSwitchControlFlowContext(InstructionList)}
 * in the reduction's "epilogue." ( JBURG does not have a formal epilogue construct
 * so for now the "epilogue" is just open coded in each reduction ).
 * {@code ControlFlowContextManager#finishSwitchControlFlowContext(InstructionList)}
 * is typically one of the last actions in the epilogue, since it may add a
 * pending label to the reduction's result InstructionList, and the pending
 * label needs to be resolved to the next statement after the statement that set
 * up the c-f context.
 * 
 */
class SwitchControlFlowContext extends ControlFlowContext
{
    SwitchControlFlowContext(SwitchNode node)
    {
        super(node);
    }
    
    /**
     *  break statements branching out of the statement 
     *  that established this control flow context target
     *  this label.  
     *  @see {@link #getBreakLabel()}, which creates the label.
     */
    private Label breakLabel = null;

    /**
     *  @return this control-flow context's break Label, synthesized if necessary.
     */
    @Override
    Label getBreakLabel()
    {
        if ( null == this.breakLabel )
            this.breakLabel = new Label("#break#" + Integer.toHexString(this.hashCode()));
        return this.breakLabel;
    }
    
    /**
     *  @return true if the context's break label has been touched.
     */
    boolean hasActiveBreak()
    {
        return breakLabel != null;
    }

    @Override
    boolean hasDefaultBreakLabel()
    {
        return true;
    }
}
