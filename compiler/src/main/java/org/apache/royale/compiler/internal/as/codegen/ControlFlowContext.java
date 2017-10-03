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

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * There is an active ControlFlowContext for each syntactic region that can
 * introduce new labels or requires clean up code in the presence of non-linear
 * code.:
 * <dl>
 * <dt>Loops</dt>
 * <dd>Loops can contain break or continue statements. Loops also scope label
 * definitions.</dd>
 * <dt>try/catch/finally</dt>
 * <dd>Jumps ( using break, continue, or goto ) out of a try block require the
 * finally block to be executed.
 * <p>
 * Jumps out of an exception handler block require code to adjust the scope
 * chain.</dd>
 * <dt>with</dt>
 * <dd>Jumps ( using break, continue, or goto ) out of a with block require code
 * to adjust the scope chain.</dd>
 * </dl>
 * <p>
 * Helper methods on the {@link ControlFlowContextManager} push and pop sub-classes of this class
 * on and off an activation stack in {@link ControlFlowContextManager}.
 */
public abstract class ControlFlowContext
{
    protected ControlFlowContext(IASNode controlFlowTreeNode)
    {
        this.controlFlowTreeNode = controlFlowTreeNode;
    }
    
    final IASNode controlFlowTreeNode;
    
    /**
     * Virtual method called by the criteria objects returned from
     * {@link ControlFlowContextManager#breakWithLabelCriteria(String)}.
     * Sub-classes of this class that contain a target which can be referenced by
     * a break statement with a label should return true.
     * @param label Name of the label referenced by a break statement.
     * @return true if the context contains a label with the specified name that can
     * be referenced by a break statement with a label, false otherwise.
     */
    boolean hasBreakLabel(String label)
    {
        return false;
    }
    
    /**
     * Virtual method called by the criteria object returned from
     * {@link ControlFlowContextManager#breakWithOutLabelCriteria}. Sub-classes
     * of this class that contain a target which can be referenced by a break
     * statement without a label should return true.
     * 
     * @return true if this context contains the target of a break statement,
     * false otherwise.
     */
    boolean hasDefaultBreakLabel()
    {
        return false;
    }
    
    
    /**
     * @return The {@link Label} that break statements that target this control
     * flow context should jump to.
     */
    Label getBreakLabel()
    {
        return null;
    }
    
    /**
     * Virtual method called by the criteria objects returned from
     * {@link ControlFlowContextManager#continueWithLabelCriteria(String)}
     * Sub-classes of this class that contain a target which can be referenced by
     * a continue statement with a label should return true.
     * @param label Name of the label referenced by a continue statement.
     * @return true if the context contains a label with the specified name that can
     * be referenced by a continue statement with a label, false otherwise.
     */
    boolean hasContinueLabel(String label)
    {
        return false;
    }
    

    
    /**
     * Virtual method called by the criteria object returned from
     * {@link ControlFlowContextManager#continueWithOutLabelCriteria}.
     * Sub-classes of this class that contain a target which can be referenced
     * by a continue statement without a label should return true.
     * 
     * @return true if this context contains the target of a continue statement,
     * false otherwise.
     */
    boolean hasDefaultContinueLabel()
    {
        return false;
    }
    
    /**
     * @return The {@link Label} that continue statements that target this control
     * flow context should jump to.
     */
    Label getContinueLabel()
    {
        return null;
    }
    
    /**
     * Virtual method called by the criteria objects returned from
     * {@link ControlFlowContextManager#gotoLabelCriteria(String, boolean)}.
     * Sub-classes of this class that contain a target which can be referenced
     * by a goto statement should return true.
     * <p>
     * If there is more than one label in this context with the specified label,
     * then this should should return false, unless the allowDuplicates option
     * is true.
     * 
     * @param label Name of the labeled referenced by a goto statement.
     * @param allowDuplicates If false, this method will return false if there
     * is more than one label with the specified name in this context. If true,
     * this method will return true if there is at least one label in this
     * context with the specified name.
     * @return <ul>
     * <li>true if there is exactly one label in this context with the specified
     * name</li> <li>true if allowDuplicates is true <b>and</b> there is at
     * least one label in this context with the specified name.</li> <li>false
     * otherwise</li>
     * </ul>
     */
    boolean hasGotoLabel(String label, boolean allowDuplicates)
    {
        return false;
    }
    
    /**
     * @return The {@link Label} that goto statements that target this control
     * flow context should jump to.
     */
    Label getGotoLabel(String label)
    {
        return null;
    }
    
    /**
     * Create a new instruction list that starts with code to exit this control
     * flow context and ends the specified instructions.
     * <p>
     * Sub-classes should override this method if a jump out of this control
     * flow context requires code other than the jump to run. Eg: When jumping
     * out of a try or exception block, the finally block must be run first.
     * 
     * @param exitBranch Code to execute after leaving this control flow
     * context.
     * @return A new instruction list that contains code the exit this control
     * flow context and all the instructions from the specified instruciton
     * list.
     */
    InstructionList addExitPath(InstructionList exitBranch)
    {
        return exitBranch;
    }
    
    /**
     * Add instructions to the specified instruction list that will re-establish
     * this control flow context ( currently this code only ever push a value on
     * the scope stack ).
     * 
     * @param exceptionHandler {@link InstructionList} to add to.
     */
    void addExceptionHandlerEntry(InstructionList exceptionHandler)
    {
        
    }
}
