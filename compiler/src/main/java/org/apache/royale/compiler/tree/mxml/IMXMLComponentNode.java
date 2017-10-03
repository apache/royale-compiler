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

import org.apache.royale.compiler.definitions.IClassDefinition;

/**
 * This AST node represents an MXML <code>&lt;Component&gt;</code> tag.
 * <p>
 * An {@link IMXMLComponentNode} has one child, which is an
 * {@link IMXMLClassDefinitionNode}.
 */
public interface IMXMLComponentNode extends IMXMLFactoryNode
{
    /**
     * Gets the name of the class being defined.
     * 
     * @return The String specified by the <code>className</code> attribute.
     */
    String getClassName();

    /**
     * Gets the class-defining node which is the sole child of this node.
     * 
     * @return An {@link IMXMLClassDefinitionNode}.
     */
    IMXMLClassDefinitionNode getContainedClassDefinitionNode();

    /**
     * Gets the class definition of the defined class.
     * 
     * @return An {@link IClassDefinition} object representing the class defined
     * by this <code>&lt;Component&gt;</code> tag.
     */
    IClassDefinition getContainedClassDefinition();
}
