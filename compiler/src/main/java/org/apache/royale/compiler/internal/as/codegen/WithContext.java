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

import static org.apache.royale.abc.ABCConstants.OP_popscope;
import static org.apache.royale.abc.ABCConstants.OP_pushwith;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  The WithContext tracks the usage of a with scope
 *  in the body of the with statement; if a temp is
 *  needed to reinitialize the scope stack in a catch
 *  or finally block, it will be recorded here.
 */
class WithContext extends LabelScopeControlFlowContext
{
    /**
     *  The scope that allocated this with.
     *  Used to allocate and release the temp.
     */
    LexicalScope currentScope;

    /**
     *  The temp allocated to store the with scope.
     */
    Binding withStorage = null;

    /**
     *  Construct a with scope.
     *  @param mgr - the defining ControlFlowContextManager.
     */
    WithContext(IASNode withContents, ControlFlowContextManager mgr)
    {
        super(withContents);
        this.currentScope = mgr.currentScope;
    }

    /**
     *  Get the temp used to store the with scope,
     *  allocating it as necessary.
     *  @return the temp's Binding.
     */
    Binding getWithStorage()
    {
        if ( !hasWithStorage() )
        {
            this.withStorage = currentScope.allocateTemp();
        }
        return this.withStorage;
    }

    /**
     *  @return true if a temp has been allocated.
     */
    boolean hasWithStorage()
    {
        return this.withStorage != null;
    }

    /**
     *  Finish the lifecycle of this with context;
     *  release the temp as necessary.
     */
    void finish(InstructionList result)
    {
        if ( hasWithStorage() )
            currentScope.releaseTemp(this.withStorage);
    }

    @Override
    InstructionList addExitPath(InstructionList exitBranch)
    {
        InstructionList with_fixup = new InstructionList();
        with_fixup.addInstruction(OP_popscope);
        with_fixup.addInstruction(getWithStorage().kill());

        with_fixup.addAll(exitBranch);
        return with_fixup;
    }

    @Override
    void addExceptionHandlerEntry(InstructionList exceptionHandler)
    {
        //  This causes the with context to allocate a temp (if not already allocated),
        //  which in turn causes the withStmt reduction in the CG to populate that temp.
        exceptionHandler.addInstruction(getWithStorage().getlocal());
        exceptionHandler.addInstruction(OP_pushwith);
    }
}
