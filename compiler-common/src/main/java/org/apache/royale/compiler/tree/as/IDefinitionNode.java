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

package org.apache.royale.compiler.tree.as;

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

/**
 * An AST node representing a declaration of a package, class,
 * interface, function, parameter, variable, constant, or namespace.
 * <p>
 * Each {@link IDefinitionNode} produces an {@link IDefinition}.
 */
public interface IDefinitionNode extends /* IAdaptable, */IASNode
{
    /**
     * Get the name of the type
     * 
     * @return type name
     */
    String getName();

    /**
     * Gets the expression that represents the name of this
     * {@link IDefinitionNode}
     * 
     * @return the {@link IExpressionNode} that represents the name of this type
     */
    // TODO Should this return IIdentifierNode?
    IExpressionNode getNameExpressionNode();

    /**
     * Get the file path in which this type is defined
     * 
     * @return file path
     */
    String getContainingFilePath();

    /**
     * Get the local start offset of the name
     * 
     * @return local start offset of the name
     */
    int getNameStart();

    /**
     * Get the local end offset of the name
     * 
     * @return local end offset of the name
     */
    int getNameEnd();

    /**
     * Get the absolute start offset of the name
     * 
     * @return absolute start offset of the name
     */
    int getNameAbsoluteStart();

    /**
     * Get the absolute end offset of the name
     * 
     * @return absolute end offset of the name
     */
    int getNameAbsoluteEnd();

    /**
     * Get package name that applies to this node
     * 
     * @return String containing fully-qualified package name.
     */
    @Override
    String getPackageName();

    /**
     * Get the qualified name of this type
     * 
     * @return the fully qualified name of this type
     */
    String getQualifiedName();

    /**
     * Get the short name of this type (without any package information)
     * 
     * @return the short name of this type (without any package information)
     */
    String getShortName();

    /**
     * Is the given modifier present on this definition? see {@link ASModifier}
     * for the list of modifiers
     * 
     * @return true if the modifier is included in the set
     */
    boolean hasModifier(ASModifier modifier);

    /**
     * Is the given namespace present on this definition? Namespaces include
     * private, protected, public and internal, plus any custom namespaces
     * available in scope
     * 
     * @param namespace the namespace to check for
     * @return true if the namespace exists
     */
    boolean hasNamespace(String namespace);

    /**
     * Returns the namespace that this member belongs to
     * 
     * @return public, private, protected, internal, custom namespace or null
     */
    String getNamespace();

    /**
     * Is this definition an implicit definition that doesn't actually appear in
     * the source file? Examples include <code>this</code>, <code>super</code>,
     * default constructors, and cast functions.
     */
    boolean isImplicit();

    /**
     * Get the metadata tags of this node.
     * 
     * @return the {@link IMetaTagsNode} or null
     */
    IMetaTagsNode getMetaTags();
    
    /**
     * Get the {@link IMetaInfo}s for all the metadat tags of this node.
     * @return An array of {@link IMetaInfo}s.  Never null.
     */
    IMetaInfo[] getMetaInfos(); 

    IDefinition getDefinition();
}
