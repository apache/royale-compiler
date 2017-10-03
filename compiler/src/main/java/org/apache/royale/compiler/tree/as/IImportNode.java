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

import org.apache.royale.compiler.common.IImportTarget;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * An AST node representing an <code>import</code> directive.
 * <p>
 * The shape of this node is:
 * <pre>
 * IImportNode 
 *   IExpressionNode <-- getImportNameNode()
 * </pre>
 * For example, <pre>import flash.display.Sprite;</pre> is represented as
 * <pre>
 * IImportNode
 *   IFullNameNode
 *     IFullNameNode
 *       IIdentifierNode "flash"
 *       IIdentifierNode "display"
 *     IIdentifierNode "Sprite"
 * </pre>
 */
public interface IImportNode extends IASNode
{
    /**
     * Represents a kind of import that his node represents
     */
    static enum ImportKind
    {
        /**
         * An MXML style namespace import, of the form
         * xmlns:prefix="my.component.*"
         */
        MXML_NAMESPACE_IMPORT,
        
        /**
         * A standard ActionScipt import that follows traditional scoping rules
         */
        AS_SCOPED_IMPORT,

        /**
         * An import that is implicit in the given scope, caused by generated
         * code found in MXML
         */
        IMPLICIT_IMPORT
    }

    /**
     * Get the name of the target definition we are importing
     * 
     * @return name of package to import
     */
    String getImportName();

    /**
     * Returns the expression underlying this import node
     * 
     * @return an {@link IExpressionNode} for this import
     */
    // TODO Shouldn't this be IIdentifierNode?
    IExpressionNode getImportNameNode();

    /**
     * Returns the type that this import represents
     * 
     * @return an {@link IImportNode}
     */
    ImportKind getImportKind();

    /**
     * Creates an import target for the given import
     * 
     * @return an {@link IImportTarget}
     */
    IImportTarget getImportTarget();

    /**
     * Returns the alias for this import, if one exists
     *
     * @return an alias or null
     */
    String getImportAlias();

    /**
     * Returns whether an import statement is a wildcard import or not
     * 
     * @return true if a wildcard import
     */
    boolean isWildcardImport();
    
    /**
     * Returns the definition being imported, or <code>null</code>
     * if this is a wildcard import or if the import name
     * cannot be resolved.
     * 
     * @param project An {@link ICompilerProject} within which references
     * are resolved.
     * @return An {@link IDefinition} or <code>null</code>.
     */
    IDefinition resolveImport(ICompilerProject project);
}
