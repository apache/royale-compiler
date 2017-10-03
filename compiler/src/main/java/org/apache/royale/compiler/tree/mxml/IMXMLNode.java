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

import org.apache.royale.compiler.tree.as.IASNode;

/**
 * This is the base interface for all MXML-specific AST nodes.
 */
public interface IMXMLNode extends IASNode
{
    /**
     * Gets the name of this node.
     * <p>
     * For a node that refers to a class, this is the fully-qualified
     * ActionScript class name. (For example, for <code>&lt;s:Button&gt;</code>
     * it would be <code>"spark.components.Button"</code>.)
     * <p>
     * For a node that specifies a property, style, or event, this is the name
     * of the property, style, or event. (For example, for
     * <code>&lt;s:text&gt;</code> it would be <code>"text"</code>.)
     * <p>
     * For a language node, this is the short name of the tag. (For example, for
     * <code>&lt;fx:Script&gt;</code> it would be <code>"Script"</code>.)
     * 
     * @return The name of this node as a String.
     */
    String getName();

    /**
     * Indicates whether code should be generated for this node.
     * <p>
     * If this method returns <code>false</code>, there is something invalid
     * about this node, or one of its descendants, that prevents code
     * generation.
     */
    boolean isValidForCodeGen();

    /**
     * Gets the closest ancestor node that defines a class.
     * 
     * @return An {@link IMXMLClassDefinitionNode} object.
     */
    IMXMLClassDefinitionNode getClassDefinitionNode();

    /**
     * Gets the closest ancestor node that is the root node of the MXML file.
     * 
     * @return An {@link IMXMLDocumentNode} object.
     */
    IMXMLClassDefinitionNode getDocumentNode();

    /**
     * Gets the root file node.
     * 
     * @return An {@link IOldMXMLFileNode} object.
     */
    IMXMLFileNode getFileNode();
}
