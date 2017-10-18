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

package org.apache.royale.compiler.internal.css.codegen;

import java.util.Map;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.MethodInfo;

/**
 * The CSS code generation produces two sets of ABC instructions - one for the
 * array data, the other for the closure functions. This is a two-tuple used by
 * {@link CSSReducer}. At different tree nodes, the reduction result types for
 * array and closure can be different. This generic class allows variant types
 * to be used when reducing different tree nodes.
 * 
 * @param <L> Type of the array reduction result.
 * @param <R> Type of the closure reduction result.
 */
abstract class Pair<L, R>
{
    /**
     * Create a two-tuple object.
     * 
     * @param arrayReduction The array reduction result.
     * @param closureReduction The closure reduction result.
     */
    protected Pair(L arrayReduction, R closureReduction)
    {
        this.arrayReduction = arrayReduction;
        this.closureReduction = closureReduction;
    }

    /** Array reduction result. */
    public final L arrayReduction;

    /** Closure reduction result. */
    public final R closureReduction;

    /**
     * Print debugging message for this {@code Pair} object.
     */
    @Override
    public String toString()
    {
        return String.format("[array]\n%s\n\n[closure]\n%s\n", arrayReduction, closureReduction);
    }

    /**
     * Both the array reduction and the closure reduction results are
     * {@link InstructionList}.
     */
    public static final class PairOfInstructionLists extends Pair<InstructionList, InstructionList>
    {
        public PairOfInstructionLists(InstructionList arrayReduction, InstructionList closureReduction)
        {
            super(arrayReduction, closureReduction);
        }
    }

    /**
     * The array reduction result is {@link InstructionList}. The closure result
     * is {@code String}.
     */
    public static final class InstructionListAndString extends Pair<InstructionList, String>
    {
        public InstructionListAndString(InstructionList arrayReduction, String closureReduction)
        {
            super(arrayReduction, closureReduction);
        }
    }

    /**
     * The array reduction result is {@link InstructionList}. The closure result
     * is a map of closure names to closure method bodies.
     */
    public static final class InstructionListAndClosure extends Pair<InstructionList, Map<String, MethodInfo>>
    {
        public InstructionListAndClosure(InstructionList arrayReduction, Map<String, MethodInfo> closureReduction)
        {
            super(arrayReduction, closureReduction);
        }
    }
}
