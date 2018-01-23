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
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * This AST node represents an MXML tag which maps to an ActionScript class,
 * either as an instance of that class or a definition of a subclass of this
 * class.
 * <p>
 * An {@link IMXMLClassReferenceNode} has N child nodes; each is an
 * {@link IMXMLSpecifierNode} specifying the value of a property, the value of a
 * style, or ActionScript code to handle an event.
 */
public interface IMXMLClassReferenceNode extends IMXMLNode
{
    /**
     * Gets the definition of the ActionScript class corresponding to this MXML
     * tag.
     * 
     * @return An {@link IClassDefinition} for the ActionScript class.
     */
    IClassDefinition getClassReference(ICompilerProject project);

    /**
     * Does the class referenced by this node implement
     * <code>mx.core.IMXMLObject</code>?
     * 
     * @return <code>true</code> if it does.
     */
    boolean isMXMLObject();

    /**
     * Does the class referenced by this node implement
     * <code>mx.core.IVisualElementContainer</code>?
     * 
     * @return <code>true</code> if it does.
     */
    boolean isVisualElementContainer();

    /**
     * Does the class referenced by this node implement
     * <code>mx.core.IDeferredInstantiatonUIComponent</code>?
     * 
     * @return <code>true</code> if it does.
     */
    boolean isDeferredInstantiationUIComponent();

    /**
     * Does the class referenced by this node implement
     * <code>mx.core.IContainer</code>?
     * 
     * @return <code>true</code> if it does.
     */
    boolean isContainer();

    /**
     * Gets the child node specifying a particular property. It may have been
     * specified in MXML either by an attribute or by a child tag.
     * 
     * @param name The name of the property.
     * @return An {@link IMXMLPropertySpecifierNode} specifying that property.
     */
    IMXMLPropertySpecifierNode getPropertySpecifierNode(String name);

    /**
     * Gets all the child nodes that specify properties. These may have been
     * specified in MXML either by attributes or by child tags.
     * 
     * @return An array of {@link IMXMLPropertySpecifierNode} objects.
     */
    IMXMLPropertySpecifierNode[] getPropertySpecifierNodes();

    /**
     * Gets the child node specifying a particular event. It may have been
     * specified in MXML either by an attribute or by a child tag.
     * 
     * @param name The name of the event.
     * @return An {@link IMXMLEventSpecifierNode} specifying that event.
     */
    IMXMLEventSpecifierNode getEventSpecifierNode(String name);

    /**
     * Gets all the child nodes that specify events. These may have been
     * specified in MXML either by attributes or by child tags.
     * 
     * @return An array of {@link IMXMLEventSpecifierNode} objects.
     */
    IMXMLEventSpecifierNode[] getEventSpecifierNodes();

    /**
     * Gets all the child nodes that represent properties, styles, and events
     * and which have a particular suffix specifying a state or state group.
     * 
     * @param suffix The name of a state or state group.
     * @return An array of {@link IMXMLSpecifierNode} objects.
     */
    IMXMLSpecifierNode[] getSpecifierNodesWithSuffix(String suffix);

    /**
     * Determines whether this node needs to generate a
     * <code>UIComponentDescriptor</code>.
     * 
     * @return <code>true</code> if it needs a descriptor.
     */
    boolean needsDescriptor();

    /**
     * Determines whether this node needs to set its
     * <code>documentDescriptor</code> property.
     * 
     * @return <code>true</code> if it needs a document descriptor.
     */
    boolean needsDocumentDescriptor();
}
