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

import java.util.EnumSet;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.projects.DefinitionPriority;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.tree.as.NilNode;
import org.apache.royale.compiler.internal.tree.as.PackageNode;
import org.apache.royale.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.royale.compiler.internal.units.ASCompilationUnit;
import org.apache.royale.compiler.internal.units.requests.ASFileScopeRequestResult;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * CU for handling tree we build for config processing.
 */
class ConfigCompilationUnit extends ASCompilationUnit
{
    public static final class ConfigFileNode extends FileNode
    {
        private static final long serialVersionUID = -2201798599052884838L;

        public ConfigFileNode(IWorkspace workspace, String pathName)
        {
            super(workspace, pathName);
            setSourcePath(pathName);
            PackageNode n = new PackageNode(new NilNode(), null);
            addItem(n);
            initializeScope(null);
            processAST(EnumSet.of(PostProcessStep.CALCULATE_OFFSETS));
        }

        public ScopedBlockNode getTargetConfigScope()
        {
            return ((PackageNode)getChild(0)).getScopedNode();
        }

        @Override
        public void processAST(EnumSet<PostProcessStep> features)
        {
            //only allow normalization, not scope population
            super.processAST(EnumSet.of(PostProcessStep.CALCULATE_OFFSETS));
        }

    }

    /**
     * @param project
     * @param path
     */
    ConfigCompilationUnit(CompilerProject project, String path)
    {
        super(project, path, DefinitionPriority.BasePriority.SOURCE_LIST);
    }

    @Override
    protected FileNode createFileNode(IFileSpecification specification)
    {
        return new ConfigFileNode(getProject().getWorkspace(), getAbsoluteFilename());
    }

    @Override
    protected void verifyAST(IASNode ast)
    {
        // Don't try to verify ASTs produced by a ConfigCompilationUnit
        // because none of the nodes have source location info.
    }
    
    @Override
    protected void addProblemsToProject(ASFileScopeRequestResult result)
    {
        // don't do anything here
    }
}
