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
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * There is an active LoopControlFlowContext for the syntactic region of each
 * loop. The LoopControlFlowContext regions are pushed and popped of the
 * activation stack in the {@link ControlFlowContextManager} by the appropriate
 * reduction actions. A context is active while the loop's subtrees are reduced;
 * the LoopControlFlowContext is set up by calling
 * {@link ControlFlowContextManager#startLoopControlFlowContext(IASNode)} in the
 * reduction's Prologue. The LoopControlFlowContext is torn down by calling
 * {@code ControlFlowContextManager#finishLoopControlFlowContext(InstructionList)}
 * in the reduction's "epilogue." ( JBURG does not have a formal epilogue construct
 * so for now the "epilogue" is just open coded in each reduction ).
 * {@code ControlFlowContextManager#finishLoopControlFlowContext(InstructionList)}
 * is typically one of the last actions in the epilogue, since it may add a
 * pending label to the reduction's result InstructionList, and the pending
 * label needs to be resolved to the next statement after the statement that set
 * up the c-f context.
 */
public class LoopControlFlowContext extends LabelScopeControlFlowContext
{
    /**
     *  Construct a ControlFlowContext.
     *  @param label_text - the control flow
     *    region's AS3 label string.  May be null.
     */
    LoopControlFlowContext(IASNode loopContents)
    {
        super(loopContents);
    }

    /**
     *  continue statements branching out of the statement 
     *  that established this control flow context target
     *  this label.  
     *  @see {@link #getContinueLabel()}, which creates the label.
     */
    private Label continueLabel = null;
    
    /**
     *  break statements branching out of the statement 
     *  that established this control flow context target
     *  this label.  
     *  @see {@link #getBreakLabel()}, which creates the label.
     */
    private Label breakLabel = null;

    /**
     *  @return this control-flow context's continue Label, synthesized if necessary.
     */
    @Override
    Label getContinueLabel()
    {
        if ( null == this.continueLabel )
            this.continueLabel = new Label("#continue#" + Integer.toHexString(this.hashCode()));
        return this.continueLabel;
    }

    /**
     *  @return true if the context's continue label has been touched.
     */
    boolean hasActiveContinue()
    {
        return null != this.continueLabel;
    }
    
    /**
     *  @return true if the context's break label has been touched.
     */
    boolean hasActiveBreak()
    {
        return breakLabel != null;
    }

    /**
     *  @return this control-flow context's break Label, synthesized if necessary.
     */
    @Override
    public Label getBreakLabel()
    {
        if ( null == this.breakLabel )
            this.breakLabel = new Label("#break#" + Integer.toHexString(this.hashCode()));
        return this.breakLabel;
    }

    @Override
    boolean hasDefaultBreakLabel()
    {
        return true;
    }

    @Override
    boolean hasDefaultContinueLabel()
    {
        return true;
    }

    @Override
    boolean hasContinueLabel(String label)
    {
        IASNode loopNode = controlFlowTreeNode.getParent();
        assert loopNode != null;
        IASNode loopNodeParent = loopNode.getParent();
        if (loopNodeParent == null)
            return false;
        IASNode loopNodeGrandParent = loopNodeParent.getParent();
        if (!(loopNodeGrandParent instanceof LabeledStatementNode))
            return false;
        
        LabeledStatementNode labeledStatementNode = (LabeledStatementNode)loopNodeGrandParent;
        String labeledStatementNodeLabel = labeledStatementNode.getLabel();
        if (labeledStatementNodeLabel == null)
            return false;
        return labeledStatementNodeLabel.equals(label);
    }
}
