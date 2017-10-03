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

package org.apache.royale.compiler.tree.mxml;

import org.apache.royale.compiler.definitions.IDefinition;

/**
 * This AST node represents an MXML tag or attribute that specifies a property,
 * style, or event.
 */
public interface IMXMLSpecifierNode extends IMXMLNode
{
    /**
     * Gets the name of the property, style, or event.
     * 
     * @return The name as a {@code String}.
     */
    @Override
    String getName();

    /**
     * Gets the suffix, which is the name of a state or state group.
     * 
     * @return The suffix as a {@code String}.
     */
    String getSuffix();

    /**
     * Resolves the name of the property, style, or event to its definition.
     * 
     * @return The definition as an {@code IDefinition}.
     */
    IDefinition getDefinition();
}
