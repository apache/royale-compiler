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

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.compiler.exceptions.DuplicateLabelException;
import org.apache.royale.compiler.exceptions.UnknownControlFlowTargetException;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.internal.tree.as.SwitchNode;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ICatchNode;
import org.apache.royale.compiler.tree.as.ITryNode;

import static org.apache.royale.abc.ABCConstants.*;

/**
 *  The ControlFlowContextManager is the code generator's
 *  keeper of active control-flow contexts and the associated
 *  (implicit in the configuration of contexts) model of the scope stack.
 *
 *  Control-flow contexts come in several varieties:
 *  <ul>
 *  <li>  Ordinary control-flow contexts, established by a 
 *        labeled statement or a statement with break/continue semantics.
 *  <li>  Exception-handling contexts, established by try 
 *        with a catch or finally.
 *  <li>  With statements.
 *  </ul>
 *
 *  These various types of context have few common elements, aside
 *  from participating in the model of control flow and the scope
 *  stack, so this class manages the active configuration of contexts
 *  and embodies the code generator's control-flow and scope stack model.
 */
public class ControlFlowContextManager
{
    /**
     *  The LexicalScope that established this 
     *  ControlFlowContextManager.
     *  Used to allocate temporary storage,
     *  report problems, etc.
     */
    final LexicalScope currentScope;

    /**
     * Base class for all control flow context search criteria
     * classes.
     * All subclass are anonymous classes.
     */
    static abstract class ControlFlowContextSearchCriteria
    {
        /**
         * Determines if this search criteria matches the specified
         * {@link ControlFlowContext}.
         * 
         * @param c {@link ControlFlowContext} to check.
         * @return true if this search criteria matches the specified
         * {@link ControlFlowContext}, false otherwise.
         */
        abstract boolean match(ControlFlowContext c);
        
        /**
         * Gets the {@link Label} in the specified {@link ControlFlowContext}
         * that should be jumped to when this criteria matches the specified
         * {@link ControlFlowContext}.
         * 
         * @param c {@link ControlFlowContext} containing a label that should be
         * jumped to.
         * @return The {@link Label} that should be jumped to.
         */
        abstract Label getLabel(ControlFlowContext c);
        
        /**
         * Determines whether the control flow context is search from the inner
         * most control flow context to the outer most or vice versa, when
         * searching for a control flow context that matches this search
         * criteria.
         * 
         * @return true if the control flow context stack should be search from
         * inner most to outer most, false if the stack should be search outer
         * most to inner most.
         */
        abstract boolean innerToOuter();
    }
    
    /**
     *  Search criterion which finds all active contexts.
     */
    public static final ControlFlowContextSearchCriteria FIND_ALL_CONTEXTS =
        new ControlFlowContextSearchCriteria()
    {

        @Override
        boolean match(ControlFlowContext c)
        {
            return true;
        }

        @Override
        Label getLabel(ControlFlowContext c)
        {
            return null;
        }

        @Override
        boolean innerToOuter()
        {
            return false;
        }
        
    };
    
    /**
     * A {@link ControlFlowContextSearchCriteria} which finds the first enclosing
     * context that can be targetted with a break with no label.
     */
    static ControlFlowContextSearchCriteria breakWithOutLabelCriteria =
        new ControlFlowContextSearchCriteria()
    {
        @Override
        boolean match(ControlFlowContext c)
        {
            return c.hasDefaultBreakLabel();
        }

        @Override
        Label getLabel(ControlFlowContext c)
        {
            assert match(c);
            return c.getBreakLabel();
        }

        @Override
        boolean innerToOuter()
        {
            return true;
        }
        
    };
    
    /**
     * A {@link ControlFlowContextSearchCriteria} which finds the first
     * enclosing context that can be targetted with a continue with no label.
     */
    static ControlFlowContextSearchCriteria continueWithOutLabelCriteria =
        new ControlFlowContextSearchCriteria()
    {
        @Override
        boolean match(ControlFlowContext c)
        {
            return c.hasDefaultContinueLabel();
        }

        @Override
        Label getLabel(ControlFlowContext c)
        {
            assert match(c);
            return c.getContinueLabel();
        }

        @Override
        boolean innerToOuter()
        {
            return true;
        } 
    };
    
    /**
     * Creates a {@link ControlFlowContextSearchCriteria} which finds the first
     * enclosing context that contains a labeled statement with the specified
     * label and whose
     * {@link ControlFlowContextSearchCriteria#getLabel(ControlFlowContext)}
     * will return the break label for the found context.
     * 
     * @param label Name of the label to find.
     * @return A {@link ControlFlowContextSearchCriteria} which finds the first
     * enclosing context that contains a labeled statement with the specified
     * label and whose
     * {@link ControlFlowContextSearchCriteria#getLabel(ControlFlowContext)}
     * will return the break label for the found context.
     */
    ControlFlowContextSearchCriteria breakWithLabelCriteria(final String label)
    {
        return new ControlFlowContextSearchCriteria()
        {
            @Override
            boolean match(ControlFlowContext c)
            {
                return c.hasBreakLabel(label);
            }

            @Override
            Label getLabel(ControlFlowContext c)
            {
                assert match(c);
                return c.getBreakLabel();
            }

            @Override
            boolean innerToOuter()
            {
                return false;
            } 
        };
    }
    
    /**
     * Creates a {@link ControlFlowContextSearchCriteria} which finds the first
     * enclosing context that contains a labeled loop statement with the
     * specified label and whose
     * {@link ControlFlowContextSearchCriteria#getLabel(ControlFlowContext)}
     * will return the continue label for the found context.
     * 
     * @param label Name of the label to find.
     * @return A {@link ControlFlowContextSearchCriteria} which finds the first
     * enclosing context that contains a labeled loop statement with the
     * specified label and whose
     * {@link ControlFlowContextSearchCriteria#getLabel(ControlFlowContext)}
     * will return the continue label for the found context.
     */
    ControlFlowContextSearchCriteria continueWithLabelCriteria(final String label)
    {
        return new ControlFlowContextSearchCriteria()
        {
            @Override
            boolean match(ControlFlowContext c)
            {
                return c.hasContinueLabel(label);
            }

            @Override
            Label getLabel(ControlFlowContext c)
            {
                assert match(c);
                return c.getContinueLabel();
            }

            @Override
            boolean innerToOuter()
            {
                return false;
            } 
        };
    }
    

    /**
     * Creates a {@link ControlFlowContextSearchCriteria} which finds the first
     * enclosing context that contains a labeled statement with the specified
     * label and whose
     * {@link ControlFlowContextSearchCriteria#getLabel(ControlFlowContext)}
     * will return the goto label for the found context.
     * 
     * @param label Name of the label to find.
     * @return A {@link ControlFlowContextSearchCriteria} which finds the first
     * enclosing context that contains a labeled statement with the specified
     * label and whose
     * {@link ControlFlowContextSearchCriteria#getLabel(ControlFlowContext)}
     * will return the goto label for the found context.
     */
    ControlFlowContextSearchCriteria gotoLabelCriteria(final String label, final boolean allowDuplicates)
    {
        return new ControlFlowContextSearchCriteria()
        {
            @Override
            boolean match(ControlFlowContext c)
            {
                return c.hasGotoLabel(label, allowDuplicates);
            }

            @Override
            Label getLabel(ControlFlowContext c)
            {
                assert match(c);
                return c.getGotoLabel(label);
            }

            @Override
            boolean innerToOuter()
            {
                return true;
            } 
        };
    }
    
    
    /**
     *  @param current_scope - the active LexicalScope
     *    at the time the ControlFlowContextManager was built.
     */
    ControlFlowContextManager(LexicalScope current_scope)
    {
        this.currentScope = current_scope;
        IASNode initialControlFlowRegionNode = currentScope.getInitialControlFlowRegionNode();
        if (initialControlFlowRegionNode != null)
        {
            LabelScopeControlFlowContext rootContext = new LabelScopeControlFlowContext(initialControlFlowRegionNode);
            activeFlowContexts.add(rootContext);
        }
    }

    /**
     *  The stack of currently active control flow contexts.
     */
    Vector<ControlFlowContext> activeFlowContexts = new Vector<ControlFlowContext>();

    /**
     *  @return the index of the topmost control-flow context on the stack.
     */
    private int getTopControlFlowContextIndex()
    {
        int result = activeFlowContexts.size() - 1;
        while ( result >= 0 && !(activeFlowContexts.elementAt(result) instanceof LoopControlFlowContext) )
            result--;
        
        return result;
    }
    
    /**
     *  @return the topmost control-flow context in the stack, which the caller
     *  knows to be a {@link LoopControlFlowContext}.
     */
    private LoopControlFlowContext peekActiveLoopControlFlowContext()
    {
        int idx = getTopControlFlowContextIndex();
        assert(idx >= 0): "no non-finally flow context";
        return (LoopControlFlowContext)activeFlowContexts.elementAt(idx);
    }

    /**
     *  Pop the current context off the stack.
     *  @return the popped context.
     */
    private Object popContext()
    {
        assert(! activeFlowContexts.isEmpty()): "no active control-flow context";
        return activeFlowContexts.remove(activeFlowContexts.size()-1);
    }
    

    /**
     *  Pop the topmost control-flow context off the stack,  which the caller
     *  knows to be a {@link SwitchControlFlowContext}.
     *  @return The context popped off the stack.
     */
    private SwitchControlFlowContext popSwitchControlFlowContext()
    {
        return (SwitchControlFlowContext)popContext();
    }
    
    /**
     *  Pop the topmost control-flow context off the stack,  which the caller
     *  knows to be a {@link LabeledStatementControlFlowContext}.
     *  @return The context popped off the stack.
     */
    private LabeledStatementControlFlowContext popLabeledStatementControlFlowContext()
    {
        return (LabeledStatementControlFlowContext)popContext();
    }

    /**
     *  Pop the topmost control-flow context off the stack,  which the caller
     *  knows to be a {@link LoopControlFlowContext}.
     *  @return The context popped off the stack.
     */
    private LoopControlFlowContext popLoopControlFlowContext()
    {
        return (LoopControlFlowContext)popContext();
    }

    /**
     *  Pop the current exception handling context off the stack.
     *  @return the popped context.
     */
    private ExceptionHandlingContext popExceptionHandlingContext()
    {
        // pops the LabelScopeControlFlowObject that is left on the stack
        // by the last catch or finally context.
        popContext(); 
        return (ExceptionHandlingContext)popContext();
    }

    /**
     * Called by a reduction's Prologue section to establish an active
     * {@link LoopControlFlowContext}.
     * 
     * @param loopContents The syntax tree node containing the contents of the
     * body of the loop. This node is used to establish a new scope for labels
     * referenced by goto statements.
     */
    public void startLoopControlFlowContext(IASNode loopContents)
    {
        activeFlowContexts.add(new LoopControlFlowContext(loopContents));
    }
    
    /**
     * Called by a reduction's Prologue section to establish an active
     * {@code SwitchControlFlowContext}.
     * 
     * @param node The syntax tree node for the switch statement.
     */
    public void startSwitchContext(SwitchNode node)
    {
        activeFlowContexts.add(new SwitchControlFlowContext(node));
    }
    
    /**
     * Called by a reduction's Prologue section to establish an active
     * {@link LabeledStatementControlFlowContext}.
     * 
     * @param labeledStatement The syntax tree node for the labeled statement.
     */
    public void startLabeledStatementControlFlowContext(LabeledStatementNode labeledStatement)
        throws DuplicateLabelException
    {
        String labelName = labeledStatement.getLabel();
        //  Scan the active control-flow contexts for a duplicate label.
        boolean is_duplicate = findControlFlowContextNoError(breakWithLabelCriteria(labelName)) != CONTEXT_NOT_FOUND;
        activeFlowContexts.add(new LabeledStatementControlFlowContext(labeledStatement, labelName));

        //  Throw the exception after establishing the (duplicate) context
        //  so that the control-flow context teardown code works for this case.
        if ( is_duplicate )
        {
            throw new DuplicateLabelException(labelName);
        }
    }
    
    /** Manifest constant used by control-flow search routines for "not found" */
    public static final int CONTEXT_NOT_FOUND = -1;
    
    /**
     * Uses the specified {@link ControlFlowContextSearchCriteria} to find the
     * index of the first matching control flow context on the control flow
     * context stack.
     * 
     * @param criterion
     * @return the index of the first matching control flow context on the
     * control flow context stack.
     */
    private final int findControlFlowContextNoError(ControlFlowContextSearchCriteria criterion)
    {
        final int nContexts = activeFlowContexts.size();
        
        if (nContexts == 0)
            return CONTEXT_NOT_FOUND;
        
        if (criterion == FIND_ALL_CONTEXTS)
            return 0;
        
        // The criterion object can specify whether or not the
        // stack of control flow contexts should be searched from
        // the inner most context to the outer most context or vice
        // versa.  tharwood does not remember why we don't just
        // always search from inner to outer, so it would be worth
        // doing an experiment at some point.
        if (criterion.innerToOuter())
        {
            for ( int i = nContexts - 1; i >= 0; i--)
            {
                ControlFlowContext context = activeFlowContexts.elementAt(i);
                if (criterion.match(context))
                    return i;
            }
            return CONTEXT_NOT_FOUND;
        }
        else
        {
            for ( int i = 0; i < nContexts; i++)
            {
                ControlFlowContext context = activeFlowContexts.elementAt(i);
                if (criterion.match(context))
                    return i;
            }
            return CONTEXT_NOT_FOUND;
        }
    }
    
    /**
     * Uses the specified {@link ControlFlowContextSearchCriteria} to find the
     * index of the first matching control flow context on the control flow
     * context stack.
     * 
     * @param criterion
     * @return the index of the first matching control flow context on the
     * control flow context stack.
     * @throws UnknownControlFlowTargetException When no matching context could
     * be found.
     */
    private int findControlFlowContext(ControlFlowContextSearchCriteria criterion)
    throws UnknownControlFlowTargetException
    {
        int result = findControlFlowContextNoError(criterion);

        if ( result == CONTEXT_NOT_FOUND )
        {
            //  Callers catch this exception and report
            //  the error in context.
            throw new UnknownControlFlowTargetException(criterion);
        }

        return result;
    }

    /**
     *  Called by a reduction's Prologue section to establish
     *  an active exception handling context.
     */
    void startExceptionContext(ITryNode tryNode)
    {
        ExceptionHandlingContext finally_context = new ExceptionHandlingContext(this);

        finally_context.finallyReturns = new Vector<ExceptionHandlingContext.FinallyReturn>();
        finally_context.finallyBlock = new Label();
        finally_context.finallyDoRethrow = new Label();
        finally_context.finallyDoFallthrough = new Label("finallyDoFallthrough");
        finally_context.finallyReturnStorage = currentScope.allocateTemp();

        activeFlowContexts.add(finally_context);
        
        finally_context.startTryControlState();
        // Add a new label scope for labels that are referenced by
        // goto statements in the try block.
        activeFlowContexts.add(new LabelScopeControlFlowContext(tryNode.getStatementContentsNode()));
        
    }
    
    /**
     * @param finallyStatements Sub-tree containing all the statements in the
     * finally block. This node is used to establish as scope for labels that
     * can be referenced by goto statements.
     */
    void startFinallyContext(IASNode finallyStatements)
    {
        getFinallyContext().startFinallyControlState();

        popContext();  // Pop off the LabelScopeControlFlowContext from the try block.
        activeFlowContexts.add(new LabelScopeControlFlowContext(finallyStatements));
    }
    
    void endFinallyContext()
    {
        getFinallyContext().endFinallyControlState();
    }
    
    void startCatchContext(ICatchNode catchNode)
    {
        getFinallyContext().startCatchControlState();
        popContext(); // Pop off the LabelScopeControlFlowContext from the try block or
                      // the finally block.
        activeFlowContexts.add(new LabelScopeControlFlowContext(catchNode.getStatementContentsNode()));
    }
    
    void endCatchContext()
    {
        getFinallyContext().endCatchControlState();
    }

    /**
     *  @return the computed GOTO that implements the "return" from a finally block.
     */
    public InstructionList getFinallySwitch()
    {
        InstructionList result = new InstructionList();

        ExceptionHandlingContext finally_context = getFinallyContext();
        int n_alternatives = getFinallyAlternativesSize();

        Label[] finally_labels;
        if ( 0 == n_alternatives )
        {
            finally_labels = new Label[] { finally_context.finallyDoFallthrough, finally_context.finallyDoRethrow };
        }
        else
        {
            finally_labels = new Label[n_alternatives + 2];
            finally_labels[0] = finally_context.finallyDoFallthrough;

            int i = 1;
            for ( ExceptionHandlingContext.FinallyReturn ret: getFinallyContext().finallyReturns )
                finally_labels[i++] = ret.getLabel();

            finally_labels[i] = finally_context.finallyDoRethrow;
        }
        result.addInstruction(OP_lookupswitch, finally_labels);
        return result;
    }

    /**
     *  Find all active scopes enclosing the currently active scope,
     *  and synthesize an instruction fragment to re-initialize
     *  the scope stack.
     *  @return the instruction fragment that re-initializes the
     *    enclosing scopes on the scope stack.
     */
    public InstructionList getScopeStackReinit()
    {
        InstructionList result = new InstructionList();
        assert activeFlowContexts.size() >= 2;
        assert activeFlowContexts.lastElement() instanceof LabelScopeControlFlowContext;
        
        for ( int i = 0; i < activeFlowContexts.size() - 2; i++ )
        {
            ControlFlowContext context = activeFlowContexts.elementAt(i);
            context.addExceptionHandlerEntry(result);
        }
        return result;
    }

    /**
     *  Find all active exception handling blocks or scopes,
     *  and set up finally return sequences and/or popscopes.
     */
    InstructionList getNonLocalControlFlow(InstructionList original, ControlFlowContextSearchCriteria criterion)
        throws UnknownControlFlowTargetException
    {
        int criterion_index = findControlFlowContext(criterion);

        //  Synthesize an instruction sequence that re-balances the
        //  stack to its condition on entry to this control flow region.
        InstructionList result = original;

        for (int i = criterion_index; i < activeFlowContexts.size(); i++ )
        {
            ControlFlowContext context = activeFlowContexts.elementAt(i);
            result = context.addExitPath(result);
        }

        return result;
    }

    /**
     *  @return the ExceptionHandlingContext on top of the stack.
     */
    public ExceptionHandlingContext getFinallyContext()
    {
        ExceptionHandlingContext current_context;
        assert activeFlowContexts.size() >= 2;
        assert activeFlowContexts.lastElement() instanceof LabelScopeControlFlowContext;
        current_context = (ExceptionHandlingContext)activeFlowContexts.get(activeFlowContexts.size() - 2);
        return current_context;
    }

    /**
     *  @return the number of callers to a finally block.
     */
    public int getFinallyAlternativesSize()
    {
        return getFinallyContext().finallyReturns.size();
    }

    /**
     *  @return a code fragment that sets up the "fail" finally return.
     */
    public InstructionList getFinallyFailSignal()
    {
        InstructionList result = new InstructionList();
        //  Allow for the "success" alternative.
        CmcEmitter.pushNumericConstant(getFinallyAlternativesSize() + 1, result);
        return result;
    }

    /**
     *  Pop the active exception handling context off the stack.
     */
    void finishExceptionContext()
    {
        popExceptionHandlingContext();
    }

    /**
     * Gets the {@link Label} for a labeled statement with the specified name.
     * This code is used to assign the target of the returned {@link Label} when
     * reducing the labeled statement node. The label may be created before we
     * reduce the label statement node if there is a forward reference to the
     * label.
     * <p>
     * This method will return null if a label specified specified name could
     * not be found or if more than one label with the specified label was
     * found.
     * 
     * @param label Name of the label to return.
     * @return {@link Label} for a labeled statement with the specified name, or
     * null if no visible label with the specified name could be found.
     */
    Label getGotoLabel(String label)
    {
        ControlFlowContextSearchCriteria criterion = gotoLabelCriteria(label, false);
        int context_idx = findControlFlowContextNoError(criterion);
        if (context_idx == CONTEXT_NOT_FOUND)
            return null;
        ControlFlowContext ctx = activeFlowContexts.elementAt(context_idx);
        assert ctx.hasGotoLabel(label, false);
        return ctx.getGotoLabel(label);
    }
    
    /**
     * Generates a jump instruction to the appropriate label in the context
     * matched by the specified {@link ControlFlowContextSearchCriteria}.
     * @param criterion
     * @return {@link InstructionList} containing a jump.
     * @throws UnknownControlFlowTargetException
     */
    InstructionList getBranchTarget(ControlFlowContextSearchCriteria criterion)
        throws UnknownControlFlowTargetException
    {

        InstructionList result = new InstructionList();

        int context_idx = findControlFlowContext(criterion);
        ControlFlowContext ctx = activeFlowContexts.elementAt(context_idx);

        result.addInstruction(OP_jump, criterion.getLabel(ctx));
        return result;
    }
    
    /**
     * Finds all the labeled statements current in scope in the control flow
     * context stack that a goto with a specified label might refer to. This is
     * used to generate compiler problems for ambiguous goto statements.
     * 
     * @param label Name of the labeled statements to return.
     * @return all the labeled statements current in scope in the control flow
     * context stack that a goto with a specified label might refer to.
     */
    Collection<LabeledStatementNode> getGotoLabels(String label)
    {
        ControlFlowContextSearchCriteria criterion = gotoLabelCriteria(label, true);
        int context_idx = findControlFlowContextNoError(criterion);
        if (context_idx == CONTEXT_NOT_FOUND)
            return Collections.emptyList();
        LabelScopeControlFlowContext context =
            (LabelScopeControlFlowContext)activeFlowContexts.elementAt(context_idx);
        return context.getLabelNodes(label);
    }
    
    /**
     * Finish the current context which is known by the caller of this method
     * to be a labeled statement control flow context.
     * @param insns The instruction stream of the statement that established
     * the control flow context.
     */
    void finishLabeledStatementControlFlowContext(InstructionList insns)
    {
        LabeledStatementControlFlowContext context = popLabeledStatementControlFlowContext();
        if (context.hasActiveBreak())
            insns.labelNext(context.getBreakLabel());
    }
    
    /**
     * Finish the current context which is known by the caller of this method
     * to be a labeled statement control flow context.
     * @param insns The instruction stream of the statement that established
     * the control flow context.
     */
    void finishSwitchControlFlowContext(InstructionList insns)
    {
        SwitchControlFlowContext context = popSwitchControlFlowContext();
        if (context.hasActiveBreak())
            insns.labelNext(context.getBreakLabel());
    }

    /**
     *  Finish the current control flow context; if a break statement 
     *  targeted this context, ensure an appropriate instruction gets 
     *  labeled as the break target.
     *  @param insns - the instruction stream of the statement
     *    that established the control flow context
     */
    void finishLoopControlFlowContext(InstructionList insns)
    {
        LoopControlFlowContext current_context = popLoopControlFlowContext();

        if ( current_context.hasActiveBreak() )
            insns.labelNext(current_context.getBreakLabel());
    }

    /**
     *  If a continue statement referenced the active CF context,
     *  attach the continue target label to the target InstructionList.
     *  @param continue_target - the InstructionList to continue to.
     *  @pre The InstructionList must still be valid, i.e., it cannot
     *    have been added to another InstructionList.  This forces 
     *    resolveContinueLabel() calls further up the reduction logic
     *    than finishControlFlowContext() calls, which are almost invariably
     *    the last operation in a reduction.  resolveContinueLabel() 
     *    calls are usually among the first operations in the reduction.
     */
    void resolveContinueLabel(InstructionList continue_target)
    {
        LoopControlFlowContext context = peekActiveLoopControlFlowContext();
        if ( context.hasActiveContinue() )
            continue_target.labelFirst(context.getContinueLabel());
    }

    /**
     *  Called by a reduction's Prologue section 
     *  to establish an active with scope.
     */
    void startWithContext(IASNode withContents)
    {
        WithContext with_context = new WithContext(withContents, this);

        activeFlowContexts.add(with_context);
    }

    /**
     *  Finish a with context; propagate the lifecycle event
     *  to the with context, then pop it off the stack.
     */
    void finishWithContext(InstructionList result)
    {
        WithContext with_context = (WithContext) activeFlowContexts.lastElement();
        with_context.finish(result);
        popContext();
    }

    /**
     *  @return the current with scope's temp storage.
     *  @post The with scope will allocate a temp
     *    if one was not previously allocated.
     *  @see #hasWithStorage()
     */
    Binding getWithStorage()
    {
        WithContext with_context = (WithContext) activeFlowContexts.lastElement();
        return with_context.getWithStorage();
    }

    /**
     *  @return true if the current with scope
     *    has allocated temporary storage for
     *    its with scope.
     */
    boolean hasWithStorage()
    {
        WithContext with_context = (WithContext) activeFlowContexts.lastElement();
        return with_context.hasWithStorage();
    }

    /**
     *  @return true if the active control-flow contexts
     *    contain any context that requires the caller to
     *    cache a return value (i.e., exception handling 
     *    contexts or with statement contexts).
     */
    boolean hasNontrivialFlowCharacteristics()
    {
        for ( ControlFlowContext ctx : activeFlowContexts )
            if ( ctx instanceof WithContext || ctx instanceof ExceptionHandlingContext )
                return true;

        return false;
    }
}
