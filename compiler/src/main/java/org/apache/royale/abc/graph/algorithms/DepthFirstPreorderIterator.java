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

package org.apache.royale.abc.graph.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import org.apache.royale.abc.graph.IBasicBlock;
import org.apache.royale.abc.graph.IFlowgraph;

/**
 * DepthFirstPreorderIterator yields a depth-first preorder traversal of a {@link IFlowgraph}.
 */
public class DepthFirstPreorderIterator implements Iterator<IBasicBlock>
{
    /**
     * @param roots the caller's root(s) of the flowgraph.
     * There should be only one start block, but multiple roots
     * are tolerated to work around fuzzy successor logic to 
     * exception handlers.
     */
    public DepthFirstPreorderIterator(Collection<? extends IBasicBlock> roots)
    {
        this.toDo.addAll(roots);
    }

    /**
     * The to-be-visited stack of blocks.
     */
    Stack<IBasicBlock> toDo = new Stack<IBasicBlock>();

    /**
     *  The set of edges already traversed.
     */
    Set<Edge> visitedEdges = new HashSet<Edge>();


    @Override
    public boolean hasNext()
    {
        return !toDo.isEmpty();
    }

    @Override
    public IBasicBlock next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        IBasicBlock next = toDo.pop();
        pushSuccessors(next);
        return next;
    }

    /**
     * Traverse any previously-untraversed edges
     * by adding the destination block to the to-do stack.
     * @param b the current block.
     */
    private void pushSuccessors(IBasicBlock b)
    {
        for (IBasicBlock succ_block : b.getSuccessors())
            if (visitedEdges.add(new Edge(b, succ_block)))
                toDo.push(succ_block);
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Edge is used to detect edges previously traversed.
     * It implements composite hash and equality operations
     * so it can be used as a key in a hashed collection.
     */
    private static class Edge
    {
        private IBasicBlock from;
        private IBasicBlock to;

        Edge(IBasicBlock from, IBasicBlock to)
        {
            this.from = from;
            this.to = to;
        }

        private static final int PRIME_MULTIPLIER = 7057;

        /**
         * Generate a composite hash code so that an Edge can be
         * used in a hashed container.
         * @return the composite hash code of the from/to vertices.
         */
        @Override
        public int hashCode()
        {
            return (from.hashCode() * PRIME_MULTIPLIER) + to.hashCode();
        }

        /**
         * Use the vertices to determine equality of an Edge so it
         * can be used in a hashed container.
         * @param other the other object to compare.
         * @return true iff other is an Edge, and both Edges' from/to
         * vertices match their corresponding field.
         */
        @Override
        public boolean equals(Object other)
        {
            if (other == this)
            {
                return true;
            }
            else if (other instanceof Edge)
            {
                Edge otherEdge = (Edge)other;
                return from == otherEdge.from && to == otherEdge.to;
            }
            return false;
        }
    }
}
