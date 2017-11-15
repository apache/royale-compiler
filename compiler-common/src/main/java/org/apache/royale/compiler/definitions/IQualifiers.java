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

package org.apache.royale.compiler.definitions;

import java.util.Set;

/**
 * Represents a set of namespaces to be used for name resolution.
 * <p>
 * This interface makes it easy to pass around one or more qualifiers
 * between the name resolution methods without having to have methods
 * with various signatures.
 */
public interface IQualifiers
{
    /**
     * Gets the number of namespaces in the set. This will often be 1.
     * 
     * @return The number of namespaces.
     */
    int getNamespaceCount();

    /**
     * Gets the set of namespaces.
     * 
     * @return A set of namespace definitions.
     */
    Set<INamespaceDefinition> getNamespaceSet();

    /**
     * Get the first namespace.
     * <p>
     * This is an optimization to avoid the frequent construction
     * of temporary sets, since often an <code>IQualifiers</code>
     * will only have one namespace in it.
     * 
     * @return The first namespace definition in this object,
     * for use when the namespace set contains only one namespace.
     */
    INamespaceDefinition getFirst();
}
