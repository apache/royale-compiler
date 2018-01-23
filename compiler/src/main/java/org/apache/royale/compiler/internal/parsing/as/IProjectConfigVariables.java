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

package org.apache.royale.compiler.internal.parsing.as;

import java.util.Collection;
import java.util.List;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.tree.as.ConfigConstNode;
import org.apache.royale.compiler.internal.tree.as.ConfigNamespaceNode;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Interface for interacting with config variables from the commandline or from
 * project settings
 */
public interface IProjectConfigVariables
{
    /**
     * Returns the config variables from commandline and project arguments
     * 
     * @return a list of {@link ConfigConstNode} objects or an empty list
     */
    List<ConfigConstNode> getConfigVariables();

    /**
     * Returns the config namespaces built from commandline and project
     * arguments
     * 
     * @return a list of {@link ConfigNamespaceNode} objects or an empty list
     */
    List<ConfigNamespaceNode> getConfigNamespaces();

    /**
     * Returns the config namespace names built from commandline and project
     * arguments
     * 
     * @return a list of names or an empty list
     */
    List<String> getConfigNamespaceNames();

    /**
     * Returns the list of definitions required to process config variables
     * 
     * @return a list of {@link IDefinition} objects for: String, Number, int,
     * unit and boolean
     */
    List<IDefinition> getRequiredDefinitions();

    /**
     * Returns a list of {@link ICompilerProblem}s encountered when parsing the
     * config variables from the command line
     */
    Collection<ICompilerProblem> getProblems();

    boolean equals(IProjectConfigVariables other);
}
