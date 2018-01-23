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

package org.apache.royale.compiler.internal.graph;

import java.io.OutputStream;
import java.util.Collection;

import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Interface for classes that write to external streams
 */
public interface IReportWriter
{
    /**
     * Writes the current graph state and root compilation nodes to the output stream specified
     * as a link report.
     * <p>
     * This should only be called when all compilation units are finished. There are no guarantees
     * on the effects if called when compiler threads are still running.
     * 
     * @param outStream An {@link OutputStream} that the report will be written to
     * @param problems A {@link Collection} of {@link ICompilerProblem} that this class will add to in case of exceptions or problems
     * @throws InterruptedException can occur if this method is called while compilation is happening
     */
    void writeToStream (OutputStream outStream, Collection<ICompilerProblem> problems)
        throws InterruptedException;
}
