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

package org.apache.royale.abc.optimize;

import java.util.Iterator;
import java.util.List;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;
import org.apache.royale.abc.semantics.ExceptionInfo;
import org.apache.royale.abc.semantics.Instruction;
import org.apache.royale.abc.semantics.InstructionFactory;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.visitors.DelegatingMethodBodyVisitor;
import org.apache.royale.abc.visitors.IDiagnosticsVisitor;
import org.apache.royale.abc.visitors.IMethodBodyVisitor;

/**
 * DeadCodeFilter rebuilds the method's result InstructionList by walking the
 * control flow graph at visitEnd() time, and resets its delegate's instructions
 * to the pruned InstructionList.
 */
public class DeadCodeFilter extends DelegatingMethodBodyVisitor
{
    /**
     * Constructor.
     * 
     * @param mbi - the MethodBodyInfo to be analyzed.
     * @param delegate - the next IMethodBodyVisitor in the chain.
     */
    public DeadCodeFilter(MethodBodyInfo mbi, IMethodBodyVisitor delegate, IDiagnosticsVisitor diagnostics)
    {
        super(delegate);
        this.mbi = mbi;
        this.diagnostics = diagnostics;
    }

    /**
     * The MethodBodyInfo under analysis.
     */
    protected final MethodBodyInfo mbi;

    /**
     */
    protected final IDiagnosticsVisitor diagnostics;

    /**
     * Walk the control flow graph and remove unreachable blocks.
     */
    @Override
    public void visitEnd()
    {
        IFlowgraph cfg = this.mbi.getCfg();
        List<IBasicBlock> blocks = cfg.getBlocksInEntryOrder();
        boolean lastBlockWasReachable = true;

        int blockIdx = 0;
        while ( blockIdx < blocks.size() )
        {
            IBasicBlock b = blocks.get(blockIdx);
            boolean isReachable = cfg.isReachable(b);

            // Only advance the block index if the current
            // block is removed.
            int previousBlockCount = blocks.size();

            if ( ! isReachable )
            {
                //  Don't remove unreachable blocks that are the final block in an exception handler,
                //  unless they're also the first block in the exception handler.  The AVM depends on
                //  these blocks under some circumstances.  However, the block's instructions can be
                //  coalesced to a single OP_nop.
                boolean safeToRemove = true;

                for ( ExceptionInfo ex: this.mbi.getExceptions() )
                {
                    IBasicBlock toBlock = this.mbi.getCfg().getBlock(ex.getTo());
                    if ( b.equals(toBlock) )
                    {
                        IBasicBlock fromBlock = this.mbi.getCfg().getBlock(ex.getFrom());

                        int tryFrom = blocks.indexOf(fromBlock);
                        int tryTo   = blocks.indexOf(toBlock);
                        assert tryFrom >= 0 && tryTo >= tryFrom;

                        for ( int j = tryTo - 1; safeToRemove && j >= tryFrom; j-- )
                            safeToRemove = !cfg.isReachable(blocks.get(j));
                        
                        if ( !safeToRemove )
                        {
                            //  Can't remove it, but compact it: remove executable
                            //  instructions, then write a single OP_nop as necessary.

                            Iterator<Instruction> it = b.getInstructions().iterator();

                            while ( it.hasNext() )
                            {
                                Instruction insn = it.next();

                                if ( insn.isExecutable() )
                                    it.remove();
                            }

                            b.getInstructions().add(InstructionFactory.getInstruction(ABCConstants.OP_nop));
                            break;
                        }
                    }
                }

                if ( safeToRemove )
                {
                    //  Only remove the Block if it contains executable and non-NOP instructions.
                    for ( int j = 0; j < b.size(); j++ )
                    {
                        Instruction insn = b.get(j);
                        if ( insn.isExecutable() && insn.getOpcode() != ABCConstants.OP_nop )
                        {
                            //  Only emit a diagnostic if b is the first unreachable block
                            //  encountered in this sequence.
                            if ( lastBlockWasReachable )
                                this.diagnostics.unreachableBlock(this.mbi, this.mbi.getCfg(), b);
                            cfg.removeUnreachableBlock(b);
                            break;
                        }
                    }
                }
            }

            if ( previousBlockCount == blocks.size() )
                blockIdx++;

            //  Remember the state of the last-visited block.
            lastBlockWasReachable = isReachable;
        }

        super.visitEnd();
    }
}
