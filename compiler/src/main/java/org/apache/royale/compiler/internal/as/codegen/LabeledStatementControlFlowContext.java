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

/**
 * {@link ControlFlowContext} for labeled statement nodes.  This type of context
 * can be matched by break statements with a label.
 * <p>
 * continue statements with a label match {@link LoopControlFlowContext}'s.
 * <p>
 * goto statements match {@code LabelScopeControlFlowContext}'s.
 */
public final class LabeledStatementControlFlowContext extends ControlFlowContext
{
    LabeledStatementControlFlowContext(LabeledStatementNode node, String labelName)
    {
        super(node);
        this.labelName = labelName;
    }
    
    private final String labelName;
    private Label breakLabel;
    
    @Override
    boolean hasBreakLabel(String label)
    {
        if (labelName == null)
            return false;
        return labelName.equals(label);
    }

    @Override
    Label getBreakLabel()
    {
        assert labelName != null;
        if ( null == this.breakLabel )
            this.breakLabel = new Label("#break#" + labelName);
        return this.breakLabel;
    }
    
    /**
     * @return true if this context's break label was ever referenced, false
     * otherwise.
     */
    boolean hasActiveBreak()
    {
        return breakLabel != null;
    }
    
}
