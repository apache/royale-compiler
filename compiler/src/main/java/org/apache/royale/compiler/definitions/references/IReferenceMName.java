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

package org.apache.royale.compiler.definitions.references;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * An <code>IReferenceMName</code> is for IReferences that have MNames
 */
public interface IReferenceMName extends IReference
{
    /**
     * Gets the AET {link Name} that this reference represents in the given
     * project and scope.
     * 
     * @param project The project where the reference is used.
     * @param scope The scope where the reference is used from.
     * @return An AET {@link Name} representing this reference
     */
    Name getMName(ICompilerProject project, IASScope scope);
}
