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

import java.util.Vector;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;

import static org.apache.royale.abc.ABCConstants.*;


/**
 *  An ExceptionHandlingContext manages the tree-crossing
 *  state of a try/catch/finally composite statement; its
 *  actual processing state (try, catch, and finally have
 *  slightly different requirements), and most importantly
 *  the return instruction fragments for the finally block's
 *  "callers."
 */
public final class ExceptionHandlingContext extends ControlFlowContext
{

    /**
     *  @param flow_mgr - the flow manager that owns this context.
     */
    ExceptionHandlingContext( ControlFlowContextManager flow_mgr)
    {
        super(null);
        this.flowMgr = flow_mgr;
    }

    /**
     *  The flow manager that owns this context.
     *  Used to find the associated LexicalScope and
     *  allocate temporaries.
     */
    ControlFlowContextManager flowMgr;

    /**
     *  The finally block's label, if there is a finally block.
     */
    Label finallyBlock = null;

    /**
     *  The "error return" from the finally.
     */
    Label finallyDoRethrow = null;

    /**
     *  The "normal return" from the finally.
     */
    Label finallyDoFallthrough = null;

    /**
     *  A local set to a distinct value by each caller;
     *  corresponds with the position of that caller's 
     *  finally return fragment in the returns array.
     */
    Binding finallyReturnStorage = null;

    /**
     *  A local used to store the exception scope; needed to
     *  restore nested exception scopes in an inner catch.
     */
    Binding exceptionStorage = null;

    /**
     *  The possible states of the exception-handling context.
     *  There's an edge from TRY to FINALLY, from FINALLY to 
     *  CATCH, and from TRY to CATCH (if there's no FINALLY).
     *  @see #setFinallyControlState(boolean)
     *  @see #setCatchControlState(boolean)
     */
    enum TryCatchFinallyState
    {
        INITIAL,
        TRY,
        FINALLY,
        CATCH
    };

    /**
     *  This exception-handling context's state.
     */
    TryCatchFinallyState tryCatchFinallyState = TryCatchFinallyState.INITIAL;

    /**
     *  Finally return fragments and their labels.
     *  The labels go into the finally's concluding
     *  lookupswitch a.k.a. computed GOTO instruction.
     */
    public static class FinallyReturn 
    {
        Label finallyLabel;
        InstructionList finallyInsns;

        FinallyReturn(Label label, InstructionList list)
        {
            this.finallyLabel = label;
            this.finallyInsns = list;
        }

        Label getLabel()
        { 
            return finallyLabel;
        }

        InstructionList getInstructions()
        {
            return finallyInsns;
        }
    };

    /**
     *  The marshalled finally returns.
     *  @warn order is significant; the order here must match
     *    the order of the callers' "return index" saved into
     *    the finallyReturnStorage local.
     *  @see finallyReturnStorage
     */
    Vector<FinallyReturn> finallyReturns = null;

    /**
     *  true => this exception handling context has a finally block.
     */
    boolean hasFinally = false;

    @Override
    InstructionList addExitPath(InstructionList exitBranch)
    {
        InstructionList result = exitBranch;
        
        //  Enter any finally blocks
        //  that are not already active.
        if ( hasFinallyBlock() )
        {
            if ( ! isActiveFinally() )
                result = addFinallyReturn(result);
            
        }

        //  Pop the scope of any active catch blocks.
        if ( isActiveCatchBlock() )
        {
            InstructionList catch_fixup = new InstructionList();
            catch_fixup.addInstruction(OP_popscope);
            catch_fixup.addInstruction(getExceptionStorage().kill());

            catch_fixup.addAll(result);
            result = catch_fixup;
        }
        return result;
    }
    
    @Override
    void addExceptionHandlerEntry(InstructionList exceptionHandler)
    {
        if ( isActiveCatchBlock() )
        {
            exceptionHandler.addInstruction(getExceptionStorage().getlocal());
            exceptionHandler.addInstruction(OP_pushscope);
        }
    }



    /**
     *  Add a return fragment to the active finally clause.
     *  @param retblock - the instructions that make up the 
     *    finally return sequence.
     *  @return the substitute return sequence that sets up
     *    the finallyReturnStorage local and jumps to the 
     *    finally block.
     */
    private InstructionList addFinallyReturn(InstructionList retblock)
    {
        assert(this.finallyReturns != null): "Not a finally context.";

        Label retblock_label = new Label();
        retblock.labelFirst(retblock_label);
        finallyReturns.add(new FinallyReturn(retblock_label, retblock));

        InstructionList result = new InstructionList();
        CmcEmitter.pushNumericConstant(finallyReturns.size(), result);
        result.addInstruction(OP_coerce_a);
        result.addInstruction(finallyReturnStorage.setlocal());
        result.addInstruction(OP_jump, finallyBlock);
        return result;
    }

    /**
     *  @return this exception-handling context's exceptionStorage local.
     *  @note generated on demand.
     */
    public Binding getExceptionStorage()
    {
        if ( this.exceptionStorage == null )
            this.exceptionStorage = flowMgr.currentScope.allocateTemp();
        return this.exceptionStorage;
    }

    /**
     * Transition from the initial state into the try processing state. There is
     * no method to end the try control state because the BURM does not have
     * reduction for the try block, instead we implicitly end the try processing
     * state when we start a finally or catch processing state.
     */
    void startTryControlState()
    {
        assert ( this.tryCatchFinallyState == TryCatchFinallyState.INITIAL );
        this.tryCatchFinallyState = TryCatchFinallyState.TRY;
    }

    /**
     *  Transition into the finally processing state.
     */
    void startFinallyControlState()
    {
        assert ( this.tryCatchFinallyState == TryCatchFinallyState.TRY );
        this.tryCatchFinallyState = TryCatchFinallyState.FINALLY;
    }
    
    /**
     * Transition out of the finally processing state into
     * the catch state.
     */
    void endFinallyControlState()
    {
        assert ( this.tryCatchFinallyState == TryCatchFinallyState.FINALLY );
        this.tryCatchFinallyState = TryCatchFinallyState.CATCH;
    }

    /**
     * Enter the catch control state.
     */
    void startCatchControlState()
    {
        //  All of the three processing states
        //  are valid previous states.
        this.tryCatchFinallyState = TryCatchFinallyState.CATCH;
    }
    
    void endCatchControlState()
    {
        assert (TryCatchFinallyState.CATCH == this.tryCatchFinallyState) :
            "leaving catch, but control state is " + this.tryCatchFinallyState;
    }

    /**
     *  @return true if the exception handling context is in
     *    catch processing state.
     */
    private boolean isActiveCatchBlock()
    {
        return TryCatchFinallyState.CATCH == this.tryCatchFinallyState;
    }

    /**
     *  @return true if the exception handling context is in
     *    finally processing state.
     */
    private boolean isActiveFinally()
    {
        return TryCatchFinallyState.FINALLY == this.tryCatchFinallyState;
    }

    /**
     * @return true if the exception handling context has a finally block.
     */
    private boolean hasFinallyBlock()
    {
        return this.hasFinally;
    }

    /**
     * Marks this context as having a finally block.
     * @param has_finally true if this context has a finally block, false otherwise.
     */
    void setHasFinallyBlock(boolean has_finally)
    {
        this.hasFinally = has_finally;
    }
}
