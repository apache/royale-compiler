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

package org.apache.royale.compiler.embedding.transcoders;

import java.util.Collection;
import java.util.Map;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;

/**
 * Base interface for all embed transcoders
 */
public interface ITranscoder
{
    /**
     * The relevant SWF tags
     * @param tags The transcoded asset tags
     * @param problems The collection of compiler problems to which this method will add problems.
     * @return map of symbol name to character asset tags.  null if error.  the
     * returned map may not be modified.
     */
    Map<String, ICharacterTag> getTags(Collection<ITag> tags, Collection<ICompilerProblem> problems);

    String getBaseClassQName();

    /**
     * @return The name of the base class of the generated class
     */
    String getBaseClassName();

    /**
     * Analyze the attributes
     * @param location Source location from where the embed came from
     * @param problems Any problems discovered in the EmbedNode
     * @return false if analyze failed
     */
    boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems);
    
    /**
     * Build an AST to represent the embedded asset class
     * 
     * @param problems The collection of compiler problems to which this method will add problems.
     * @param filename The path to the file being embedded.
     * @return generated class AST
     */
    IFileNode buildAST(Collection<ICompilerProblem> problems, String filename);

    /**
     * Build ABC to represent the embedded asset class
     * 
     * @param project The compiler project.
     * @param problems The collecton of compiler problems to which this method will add problems.
     * @return generated class ABC
     */
    byte[] buildABC(ICompilerProject project, Collection<ICompilerProblem> problems);
}
