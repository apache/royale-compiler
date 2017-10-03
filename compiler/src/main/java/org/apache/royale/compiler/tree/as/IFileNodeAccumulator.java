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

import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;

/**
 * Any nodes/information which are to be collected during parsing and stored on
 * the FileNode should be added to this interface.
 */
public interface IFileNodeAccumulator
{
    /**
     * Returns the list of EmbedNodes
     * 
     * @return IEmbedNode list or empty list
     */
    List<IEmbedResolver> getEmbedNodes();

    /**
     * Adds an embed node to the list of embed nodes within this file.
     * 
     * @param node Node to add
     */
    void addEmbedNode(IEmbedResolver node);

    /**
     * Returns the list of the ImportNodes
     * 
     * @return ImportNode list or empty list
     */
    List<IImportNode> getImportNodes();

    /**
     * Adds an import node to the list of import nodes within this file.
     * 
     * @param node Node to add
     */
    void addImportNode(IImportNode node);

    /**
     * Adds a Resource Bundle to the set of Resource Bundles that are referenced
     * within this file. A ResourceBundle stated as required by either using a
     * [ResourceBundle(...)] metadata or a @Resource compiler directive (MXML
     * only).
     * 
     * @param bundleName name of the Resource Bundle
     */
    void addRequiredResourceBundle(String bundleName);

    /**
     * @return the set of ResourceBundles required for the associated file.
     */
    Set<String> getRequiredResourceBundles();
    
    /**
     * Add a function node whose body contents are deferred.
     * 
     * @param functionNode Function node.
     */
    void addDeferredFunctionNode(FunctionNode functionNode);
}
