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

package org.apache.royale.compiler.internal.scopes;

import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.scopes.IFileScopeProvider;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Constructs {@link ASFileScope} objects for byte code in an ABC file.
 */
public final class ASFileScopeProvider implements IFileScopeProvider
{
    private static final ASFileScopeProvider instance = new ASFileScopeProvider();

    /**
     * @return Singleton object of this class.
     */
    public static ASFileScopeProvider getInstance()
    {
        return instance;
    }

    /**
     * Hide constructor for singleton class.
     */
    private ASFileScopeProvider()
    {
    }

    @Override
    public ASFileScope createFileScope(IWorkspace workspace, String filePath)
    {
        final ASFileScope asFileScope = new ASFileScope((Workspace)workspace, filePath);
        return asFileScope;
    }
}
