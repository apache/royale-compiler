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

import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.mxml.IStateDefinition;
import org.apache.royale.compiler.mxml.IStateGroupDefinition;
import org.apache.royale.compiler.tree.as.ICommonClassNode;

/**
 * This AST node represents an MXML tag which defines a new class.
 * <p>
 * It might be the document tag, or the tag inside a
 * <code>&lt;Component&gt;</code> tag, or the tag inside a
 * <code>&lt;Definition&gt;</code> tag.
 */
public interface IMXMLClassDefinitionNode extends IMXMLClassReferenceNode, ICommonClassNode
{
    /**
     * Gets the definition of the class defined by this node.
     * 
     * @return An {@link IClassDefinition} object for the class.
     */
    IClassDefinition getClassDefinition();

    /**
     * Gets the instance node with a specified id.
     * 
     * @param id The desired id.
     * @return the {@link IMXMLInstanceNode} with that id, or <code>null</code>.
     */
    IMXMLInstanceNode getNodeWithID(String id);

    /**
     * Gets the set of state names for this class.
     * 
     * @return A Set of names Strings for the states.
     */
    Set<String> getStateNames();

    /**
     * Gets the set of state definitions for this class.
     * 
     * @return A Set of {@link IStateDefinition} objects.
     */
    Set<IStateDefinition> getStates();

    /**
     * Gets the state definition with the specified name.
     * 
     * @return An {@link IStateDefinition} or <code>null</code>.
     */
    IStateDefinition getStateByName(String stateName);
    
    /**
     * Gets the set of state group names for this class.
     * 
     * @return A Set of name Strings for the state groups.
     */
    Set<String> getStateGroupNames();

    /**
     * Gets the set of state group definitions for this class.
     * 
     * @return A Set of {@link IStateGroupDefinition} objects.
     */
    Set<IStateGroupDefinition> getStateGroups();
    
    /**
     * Gets the state group definition with the specified name.
     * 
     * @return An {@link IStateGroupDefinition} or <code>null</code>.
     */
    IStateGroupDefinition getStateGroupByName(String stateGroupName);

    /**
     * Gets the name of the state to which the <code>currentState</code>
     * property should be initialized.
     * 
     * @return The name of the initial state.
     */
    String getInitialState();

    /**
     * Gets the list of nodes in this class (in tree order) that are dependent
     * on a specified state, either through an <code>includeIn</code> or
     * <code>excludeFrom</code> attribute or through a suffix.
     * 
     * @param stateName The name of a state.
     * @return A List of {@code IMXMLNode} objects dependent on that state, or
     * null if none
     */
    List<IMXMLNode> getNodesDependentOnState(String stateName);

    /**
     * Gets the set of nodes in this class that are dependent on a state, either
     * through an <code>includeIn</code> or <code>excludeFrom</code> attribute
     * or through a suffix.
     * 
     * @return A Set of {@code IMXMLNode} objects dependent on that state, or
     * null if none
     */
    List<IMXMLNode> getAllStateDependentNodes();

    /**
     * Gets the compiler-generated id, if one is necessary, for a specified
     * instance tag that doesn't have a specified id.
     * 
     * @param instanceNode An {@link IMXMLInstanceNode} representing an instance
     * tag.
     */
    String getGeneratedID(IMXMLInstanceNode instanceNode);

    /**
     * Gets an array of all the child nodes that represent <Metadata> tags.
     * 
     * @return An array of {@link IMXMLMetadataNode} objects.
     */
    IMXMLMetadataNode[] getMetadataNodes();

    /**
     * Gets an array of all the child nodes that represent <Script> tags.
     * 
     * @return An array of {@link IMXMLScriptNode} objects.
     */
    IMXMLScriptNode[] getScriptNodes();

    /**
     * Gets an array of all the child nodes that represent <Declarations> tags.
     * 
     * @return An array of {@link IMXMLDeclarationsNode} objects.
     */
    IMXMLDeclarationsNode[] getDeclarationsNodes();

    /**
     * Determines whether this class contains data binding nodes
     * 
     * @return <code>true</code> if this class contains data binding nodes,
     * false otherwise.
     */
    boolean getHasDataBindings();
}
