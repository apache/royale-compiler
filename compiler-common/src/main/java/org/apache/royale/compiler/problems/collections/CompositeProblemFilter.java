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

import org.apache.royale.compiler.clients.problems.IProblemFilter;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 *  A CompositeProblemFilter combines one more more filters
 *  and a boolean operation to produce a new filter.
 */
public class CompositeProblemFilter implements IProblemFilter
{
    /** Boolean operator to apply to operand filters */
    public enum CompositionOperator {
        And,
        Or,
        Not,
    };

    @Override
    public boolean accept(ICompilerProblem problem)
    {
       switch ( this.operator )
       {
           case And:
               return left.accept(problem) && right.accept(problem);
           case Or:
               return left.accept(problem) || right.accept(problem);
           case Not:
               return ! (left.accept(problem) );
       }

       assert(false): "Uncovered composition case " + this.operator;
       return false;
    }

    /**
     *  Construct a CompositeProblemFilter.  Only this class' factory methods
     *  can construct a CompositeProblemFilter.
     */
    private CompositeProblemFilter(IProblemFilter left, IProblemFilter right, CompositionOperator operator)
    {
        this.left  = left;
        this.right = right;
        this.operator  = operator;

        assert(left != null);

        if ( operator == CompositionOperator.Not )
            assert right == null;
        else
            assert right != null;
    }

    /**  left-hand (or only) filter, tested first. */
    private IProblemFilter left;

    /**  right-hand (or missing) filter, tested last or not at all. */
    private IProblemFilter right;

    /** The operator that determines how this filter works. */
    private CompositionOperator operator;

    /**
     *  Construct a composite filter that ands two filters' decisions to accept.
     */
    public static IProblemFilter and(IProblemFilter left, IProblemFilter right)
    {
        return new CompositeProblemFilter(left, right, CompositionOperator.And);
    }

    /**
     *  Construct a composite filter that ors two filters' decisions to accept.
     */
    public static IProblemFilter or(IProblemFilter left, IProblemFilter right)
    {
        return new CompositeProblemFilter(left, right, CompositionOperator.Or);
    }

    /**
     *  Construct a composite filter that negates a filter's decision to accept.
     */
    public static IProblemFilter not(IProblemFilter filter)
    {
        return new CompositeProblemFilter(filter, null, CompositionOperator.Not);
    }
}
