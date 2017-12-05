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

package org.apache.royale.compiler.workspaces;

import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Interface that clients of Royale can implement to record profile data about
 * what operation are executed on what compilation units and how long those
 * operations took.
 * <p>
 * All methods of this interface may be called from any thread at any time, so
 * implementations of this interface must be thread-safe.
 */
public interface IWorkspaceProfilingDelegate
{
    /**
     * Called when a ICompilationUnit operation starts executing. Can be called
     * from any thread. This method is logically part of the CompilationUnit
     * operation and thus should not do anything that an ICompilationUnit
     * operation is not allowed to do.
     * 
     * @param cu The ICompilationUnit on which the operation is starting.
     * @param operation Enumeration value indicating which operation is
     * starting.
     */
    void operationStarted(ICompilationUnit cu, ICompilationUnit.Operation operation);

    /**
     * Called when a ICompilationUnit operation completes executing. Can be
     * called from any thread. This method is logically part of the
     * CompilationUnit operation and thus should not do anything that an
     * ICompilationUnit operation is not allowed to do.
     * 
     * @param cu The ICompilationUnit on which the operation is completing.
     * @param operation Enumeration value indicating which operation is
     * completing.
     */
    void operationCompleted(ICompilationUnit cu, ICompilationUnit.Operation operation);
}
