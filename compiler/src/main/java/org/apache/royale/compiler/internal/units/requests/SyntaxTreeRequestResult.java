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

package org.apache.royale.compiler.internal.units.requests;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNodeAccumulator;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import com.google.common.collect.ImmutableSet;


/**
 * Generic implementation of {@link ISyntaxTreeRequestResult}.
 */
public class SyntaxTreeRequestResult implements ISyntaxTreeRequestResult
{
    public SyntaxTreeRequestResult(IASNode tree, ImmutableSet<String> includedFiles, long lastModified, Collection<ICompilerProblem> problems)
    {
        this.tree = tree;
        this.includedFiles = includedFiles;
        this.problems = problems.toArray(new ICompilerProblem[problems.size()]);
        this.lastModified = lastModified;
        // TODO: re-enable this assert after fixing CMP-670
        //assert DefinitionUtils.areTreeOffsetsConsistent(tree);
    }

    public SyntaxTreeRequestResult(long lastModified, Collection<ICompilerProblem> problems)
    {
        this(null, ImmutableSet.<String>of(), lastModified, problems);
    }

    private final IASNode tree;
    private final ImmutableSet<String> includedFiles;
    private final ICompilerProblem[] problems;
    private final long lastModified;

    @Override
    public ICompilerProblem[] getProblems()
    {
        return problems;
    }

    @Override
    public IASNode getAST()
    {
        return tree;
    }

    @Override
    public long getLastModified()
    {
        return this.lastModified;
    }
    
    @Override
    public Set<String> getRequiredResourceBundles() 
    {
        if(tree instanceof IFileNodeAccumulator)
        {
            return ((IFileNodeAccumulator)tree).getRequiredResourceBundles();
        }
        
        return Collections.emptySet();
    }

    @Override
    public ImmutableSet<String> getIncludedFiles()
    {
        return includedFiles;
    }
}
