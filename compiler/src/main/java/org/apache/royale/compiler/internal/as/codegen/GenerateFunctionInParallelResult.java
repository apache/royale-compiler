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

import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.visitors.IVisitor;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Object that is returned by
 * {@link org.apache.royale.compiler.internal.as.codegen.ICodeGenerator#generateFunctionInParallel(java.util.concurrent.ExecutorService, org.apache.royale.compiler.internal.tree.as.FunctionNode, LexicalScope)}
 * .
 * <p>
 * A wad containing the future for the code generation work happening in
 * parallel and the method info for the function being generated.
 */
public class GenerateFunctionInParallelResult
{
    GenerateFunctionInParallelResult (Future<?> future, MethodInfo methodInfo, List<IVisitor> deferredVisitEndsList)
    {
        assert future != null;
        assert methodInfo != null;
        this.future = future;
        this.methodInfo = methodInfo;
        this.deferredVisitEnds = deferredVisitEndsList;
    }

    private final Future<?> future;
    private final MethodInfo methodInfo;
    private final List<IVisitor> deferredVisitEnds;

    /**
     * Blocks until the code generation work that this object corresponds to
     * is completed.
     */
    public void finish() throws InterruptedException, ExecutionException
    {
        future.get();
        for (IVisitor v : deferredVisitEnds)
        {
            v.visitEnd();
        }
    }

    /**
     * Gets the {@link org.apache.royale.abc.semantics.MethodInfo} for the function for which code is being
     * generated. Thie method may be called immediately after this object is
     * constructed.
     *
     * @return The {@link org.apache.royale.abc.semantics.MethodInfo} for the function for which code is
     * being generated.
     */
    public MethodInfo getMethodInfo()
    {
        return methodInfo;
    }
}
