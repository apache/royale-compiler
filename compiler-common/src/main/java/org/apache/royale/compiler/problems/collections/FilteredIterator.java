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

package org.apache.royale.compiler.problems.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.royale.compiler.clients.problems.IProblemFilter;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 *  A FilteredIterator applies a IProblemFilter to an
 *  Iterator&gt;ICompilerProblem&lt; and generates a
 *  subsequence of the orginal Iterator's results, 
 *  namely those elements that satisfy the filter.
 */
public class FilteredIterator implements Iterator<ICompilerProblem>
{
    /**
     *  Construct a FilteredIterator.
     *  @param problems - the underlying problem sequence.
     *  @param filter - the filter to apply to the problems.
     */
    public FilteredIterator(Iterator<ICompilerProblem> problems, IProblemFilter filter)
    {
        this.filter             = filter;
        this.underlyingProblems = problems;
        this.currentProblem     = null;
    }

    /** The caller's problem filter.*/
    IProblemFilter filter;

    /** The underlying sequence of problems. */
    Iterator<ICompilerProblem> underlyingProblems;

    /**
     *  Holding area for a matching problem that was
     *  found by hasNext() but has not yet been 
     *  consumed by a call to next().
     */
    ICompilerProblem currentProblem = null;

    /**
     *  Find the next element of the underlying sequence
     *  that the filter accepts.
     *  @return true if a matching element was found.
     *  @post currentProblem is not null if hasNext() returns true.
     */
    @Override
    public boolean hasNext()
    {
        return findNext();
    }

    /**
     *  Find and fetch the next element of the underlying sequence 
     *  that the filter accepts.
     *  @return the next filtered element.
     *  @throws NoSuchElementException if no more elements found.
     *  @post currentProblem is null.
     */
    @Override
    public ICompilerProblem next()
    {
        if ( ! findNext() )
            throw new NoSuchElementException();

        //  Return the current problem, which uses it up.
        ICompilerProblem result = currentProblem;
        currentProblem = null;
        return result;
    }

    /**
     *  This iterator cannot remove elements.
     *  @throws UnsupportedOperationException in all cases.
     */
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     *  Position to the next problem that matches the filter.
     *  @post currentProblem is not null if findNext() returns true.
     */
    private boolean findNext()
    {
        while (this.currentProblem == null && this.underlyingProblems.hasNext() )
        {
            ICompilerProblem candidate = this.underlyingProblems.next();

            if ( filter.accept(candidate) )
                this.currentProblem = candidate;
        }

        return this.currentProblem != null;
    }

    /**
     *  Convenience method extends the FilteredIterator concept to an Iterable.
     *  @param problems - an Iterable (collection) of problems.
     *  @param filter - the filter to apply to the problems.
     *  @return an Iterable wrapper around a FilteredIterator.
     */
    public static Iterable<ICompilerProblem> getFilteredIterable(final Collection<ICompilerProblem> problems, final IProblemFilter filter)
    {
        return new Iterable<ICompilerProblem>()
        {
            @Override
            public Iterator<ICompilerProblem> iterator()
            {
                return new FilteredIterator(problems.iterator(), filter);
            }
        };
    }
}
