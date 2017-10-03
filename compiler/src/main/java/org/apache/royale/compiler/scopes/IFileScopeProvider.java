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

package org.apache.royale.compiler.scopes;

import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.units.ABCCompilationUnit;
import org.apache.royale.compiler.internal.units.SWCCompilationUnit;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Callback for constructing a {@code ASFileScope} object.
 * {@link ABCCompilationUnit} constructs {@link ASFileScope};
 * {@link SWCCompilationUnit} constructs {@code SWCFileScope}.
 */
public interface IFileScopeProvider
{
    /**
     * Create an {@code ASFileScope} object.
     * 
     * @param workspace workspace
     * @param filePath path of the file that contains this file scope
     * @return file scope
     */
    ASFileScope createFileScope(final IWorkspace workspace, final String filePath);
}
