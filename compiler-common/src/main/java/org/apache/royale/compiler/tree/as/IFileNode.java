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

import java.util.Collection;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.targets.ITargetAttributes;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;

/**
 * An AST node representing an ActionScript file being compiled.
 * <p>
 * An <code>IFileNode</code> is the root of the AST (abstract syntax tree)
 * produced for a file.
 * <p>
 * If the file includes other files, a single <code>IFileNode</code>
 * is produced.
 * <p>
 * The shape of this node is
 * <pre>
 * IFileNode
 *   IASNode <-- getChild(0)
 *   IASNode <-- getChild(1)
 *   ...
 * </pre>
 */
public interface IFileNode extends IScopedNode
{
    /**
     * Returns a last modification timestamp of the current include file tree
     * 
     * @return a timestamp
     */
    long getIncludeTreeLastModified();

    /**
     * @return True is this file contains any include statements
     */
    boolean hasIncludes();

    /**
     * @return OffsetLookup object of this file node.
     */
    OffsetLookup getOffsetLookup();

    /**
     * If this {@code IFileNode} can be used as the MXML main application or
     * ActionScript main class, it returns attributes related to SWF target.
     * Otherwise, this method returns null.
     * 
     * @param project Context project.
     * @return Target attributes collected from special MXML attributes or
     * ActionScript metadata tag.
     */
    ITargetAttributes getTargetAttributes(ICompilerProject project);
    
    /**
     * Gets definition nodes within this file node that are children
     * of the file node or children of a package block node.
     * 
     * @param includeDefinitionsOutsideOfPackage A flag indicating whether
     * definition nodes that aren't in a package should be included.
     * @param includeNonPublicDefinitions A flag indicating whether
     * definition nodes that don't have a public keyword should be included.
     * @return An array of {@link IDefinitionNode} objects.
     */
    IDefinitionNode[] getTopLevelDefinitionNodes(boolean includeDefinitionsOutsideOfPackage,
                                                 boolean includeNonPublicDefinitions);

    /**
     * Gets the definitions corresponding to the definitions nodes
     * within this file node that are children of the file node
     * or children of a package block node.
     * 
     * @param includeDefinitionsOutsideOfPackage A flag indicating whether
     * definitions that aren't in a package should be included.
     * @param includeNonPublicDefinitions A flag indicating whether definitions
     * that don't have a public keyword should be included.
     * @return An array of {@link IDefinition} objects.
     */
    IDefinition[] getTopLevelDefinitions(boolean includeDefinitionsOutsideOfPackage,
                                         boolean includeNonPublicDefinitions);

    /**
     * Returns the problems that are contained in this file.
     * 
     * @return An array of {@link ICompilerProblem} objects
     */
    public Collection<ICompilerProblem> getProblems();
    
    /**
     * Rebuild function body nodes.
     */
    void populateFunctionNodes();
}
