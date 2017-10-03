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

import java.util.Collection;

import org.apache.royale.compiler.definitions.IDefinition;

/**
 * Interface implemented by all file scope, which is the root scope for any
 * source file or SWC script.
 */
public interface IFileScope extends IASScope
{
    String getContainingPath();
    
    /**
     * @return true if this scope is from a SWC, false otherwise.
     */
    boolean isSWC();
    
    /**
     * Finds the definitions in this scope which could be visible to other
     * compilation units and adds them to a specified collection of definitions.
     * <p>
     * The externally-visible definitions are the ones in a file scope or in a
     * package scope.
     * 
     * @param definitions The collection of {@link IDefinition}'s to which the
     * externally-visible definitions in this scope are to be added.
     * @param includePrivateDefinitions If true, definitions with private
     * namespace qualifiers are added to the specified collection.
     */
    void collectExternallyVisibleDefinitions(Collection<IDefinition> definitions, boolean includePrivateDefinitions);
}
