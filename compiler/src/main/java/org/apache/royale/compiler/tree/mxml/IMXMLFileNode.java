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

import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IFileNode;

/**
 * This AST node represents an MXML file. It is the root of an MXML AST.
 */
public interface IMXMLFileNode extends IMXMLNode, IFileNode
{
    /**
     * Returns the project that created this file node.
     * 
     * @return An {@link ICompilerProject} object.
     */
    ICompilerProject getCompilerProject();

    /**
     * Returns the node representing the root tag within this file. This is the
     * sole child of this {@link IOldMXMLFileNode}.
     * 
     * @return an {@link IMXMLDocumentNode} object.
     */
    @Override
    IMXMLDocumentNode getDocumentNode();

    /**
     * Returns a list of nodes representing embedded assets.
     * <p>
     * These can be either nodes representing the initialization value of a
     * variable with <code>[Embed(...)]</code> metadata, or nodes representing
     * the compiler directive <code>@Embed(...)</code>).
     * 
     * @return A list of {@link IEmbedResolver} objects.
     */
    List<IEmbedResolver> getEmbedNodes();

    /**
     * @return A list of {@link IMXMLStyleNode}'s in this MXML document.
     */
    List<IMXMLStyleNode> getStyleNodes();

    /**
     * Get the CSS semantic information.
     * 
     * @return CSS semantic information.
     */
    CSSCompilationSession getCSSCompilationSession();
}
