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

/**
 * This type of AST node represents tags inside an MXML
 * <code>&lt;Model&gt;</code> tag that contain other tags.
 * <p>
 * It is the superinterface for {@link IMXMLModelRootNode} and
 * {@link IMXMLModelPropertyNode}.
 */
public interface IMXMLModelPropertyContainerNode extends IMXMLNode
{
    static final int NO_INDEX = -1;

    /**
     * The property-node children of this node.
     * 
     * @return An array of {@link IMXMLModelPropertyNode} objects.
     */
    IMXMLModelPropertyNode[] getPropertyNodes();

    /**
     * The names of the properties to be set on this node.
     * 
     * @return The names of the properties as an array of Strings. If this node
     * has more than one child {@link IMXMLModelPropertyNode} with the same
     * name, the property name appears only once in this array.
     */
    String[] getPropertyNames();

    /**
     * The child property nodes with a specified property name.
     * 
     * @param propertyName A property name.
     * @return An array of {@link IMXMLModelPropertyNode} objects. This will
     * often be a single-element array, but there will be multiple elements if
     * this node has more than one child {@link IMXMLModelPropertyNode} with the
     * same name.
     */
    IMXMLModelPropertyNode[] getPropertyNodes(String propertyName);

    /**
     * If this node has the same name as a sibling, then this field stores that
     * it is the i'th one, in attribute-then-child-tag order. If it is the only
     * one, its index is {@code IMXMLModelPropertyContainerNode.NO_INDEX}.
     * 
     * @return The index as an <code>int</code>.
     */
    int getIndex();
}
