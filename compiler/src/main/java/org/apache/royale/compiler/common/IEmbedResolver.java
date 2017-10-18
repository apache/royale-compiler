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

package org.apache.royale.compiler.common;

import java.util.Collection;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Any Node related to embedding (EmbedNode, MXMLEmbedNode) needs to implement
 * this interface so as to be able resolve the compilation unit related to that
 * embedded asset.
 */
public interface IEmbedResolver
{
    /**
     * Resolve the ICompilationUnit which is related to the node which
     * implements this interface
     * @param project Current project
     * @param problems Any problems resolving the compilation unit
     * @return An ICompilationUnit for the embedded asset, or null if error
     * @throws InterruptedException
     */
    ICompilationUnit resolveCompilationUnit(ICompilerProject project, Collection<ICompilerProblem> problems) throws InterruptedException;
    
    /**
     * A variant of <code>resolveCompilationUnit()</code> that ignores problems.
      */
    ICompilationUnit resolveCompilationUnit(ICompilerProject project) throws InterruptedException;
}
