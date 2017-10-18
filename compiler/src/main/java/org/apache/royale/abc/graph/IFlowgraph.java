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

package org.apache.royale.abc.graph;

import java.util.List;

import org.apache.royale.abc.graph.algorithms.DominatorTree;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.MethodBodyInfo;
import org.apache.royale.abc.visitors.IFlowGraphVisitor;

/**
 * IFlowgraph defines the operations required of an object
 * that can serve as an AET flowgraph.  
 * <p>A <i>flowgraph</i>
 * is a directed graph with a set of vertices, its <i>basic blocks,</i>
 * and a set of edges.  One vertex, the <i>start block</i>, is
 * the first vertex on every path through the flowgraph.
 */
public interface IFlowgraph
{

    /**
     *  Get a Label's target Block.
     * 
     *  @param l - the Label of interest.
     *  @return the corresponding Block, or null if not found.
     */
    IBasicBlock getBlock(Label l);

    /**
     *  Get the start block.
     *  @return the start block.
     */
    IBasicBlock getStartBlock();

    /**
     *  Is the given Block a catch target?
     *  @param b - the Block of interest.
     *  @return true if the Block is a catch target.
     *  @note See {@link MethodBodyInfo#isCatchTarget(Label)},
     *    which differs from this routine in that it uses 
     *    positional information, which is not valid if the
     *    ControlFlowGraph has been edited.
     */
    boolean isCatchTarget(IBasicBlock b);

    /**
     * Get an iterator that will iterate over the blocks in the control flow
     * graph in control flow order. This will traverse each edge in the graph
     * once, but may return the same block multiple times if multiple edges lead
     * to it.
     */
    Iterable<IBasicBlock> blocksInControlFlowOrder();

    /**
     *  Get the graph's blocks in their original order.
     * 
     *  @return an immutable List that presents the blocks in entry order.
     */
    List<IBasicBlock> getBlocksInEntryOrder();

    /**
     * Walk a IFlowGraphVisitor over this CFG.
     * 
     *  @param visitor - the visitor.
     */
    void traverseGraph(IFlowGraphVisitor visitor);

    /**
     *  Touch the graph's dominator tree and fetch it.
     *  @return the graph's dominator tree.
     */
    DominatorTree getDominatorTree();

    /**
     *  Remove an unreachable block from the CFG.
     *  @param b - the Block to remove.  
     *  @pre b must be unreachable.
     */
    void removeUnreachableBlock(IBasicBlock b);

    /**
     *  Is the given Block reachable?
     *  @param b - the block of interest.
     *  @return true if a path exists from
     *    any "entry" block to b.
     */
    boolean isReachable(IBasicBlock b);

    /**
     * Find the closest matching line number to the start of a block.
     * @param b the block of interest.
     * @return any initial debugline within the block, or the nearest
     * debugline in the preceeding (entry-order) blocks.
     */
    int findLineNumber(IBasicBlock b);

    /**
     * Find the nearest debugline instruction preceeding the given
     * (Block,offset) position and fetch its line number.
     * @param b - the Block of interest.
     * @param initialOffset - the start offset in the block.
     * @return the closest debugline instruction's line number,
     * or -1 if not found.
     */
    int findLineNumber(IBasicBlock b, int initialOffset);

    /**
     * Find the nearest debugfile instruction to the start of
     * the given block and fetch its source path.
     * @param b - the Block of interest.
     * @return the closest debugfile instruction's source path,
     * or null if not found.
     */
    String findSourcePath(IBasicBlock b);

    /**
     * Find the nearest debugfile instruction preceeding the given
     * (Block,offset) position and fetch its source path.
     * @param b - the Block of interest.
     * @param initialOffset - the start offset in the block.
     * @return the closest debugfile instruction's source path,
     * or null if not found.
     */
    String findSourcePath(IBasicBlock b, int initialOffset);

}
