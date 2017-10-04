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

import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;

/**
 * This AST node represents an MXML tag or attribute that specifies a property.
 * <p>
 * An {@link IMXMLPropertySpecifierNode} has exactly one child node: an
 * {@link IMXMLInstanceNode} representing the property value. For a simple
 * scalar property such as one of type <code>String</code>, this will be an
 * {@link IMXMLStringNode}.
 */
public interface IMXMLPropertySpecifierNode extends IMXMLSpecifierNode
{
    /**
     * Gets the value of the property.
     * 
     * @return The value as an {@link IMXMLInstanceNode}.
     */
    IMXMLInstanceNode getInstanceNode();

    /**
     * If the definition for this property has <code>[PercentProxy(...}]</code>
     * metadata, this method returns the definition for the property specified
     * by the metadata.
     * 
     * @param project The {@link RoyaleProject} in which compilation is occuring.
     * @return An {@link IVariableDefinition} object defining the proxy property.
     */
    IVariableDefinition getPercentProxyDefinition(RoyaleProject project);
}
