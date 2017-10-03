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

package org.apache.royale.compiler.internal.tree.mxml;

import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Represents an implicit import in an MXML file.
 */
public class MXMLImplicitImportNode extends ImportNode implements IImportNode
{
    // TODO Make this class package internal rather than public.

    public MXMLImplicitImportNode(ICompilerProject project, String importName)
    {
        super((ExpressionNodeBase)null);
        setImportKind(ImportKind.IMPLICIT_IMPORT);
        this.importName = importName;
        this.compilerProject = project;
    }

    private String importName;

    private ICompilerProject compilerProject;

    @Override
    public String getImportName()
    {
        return importName;
    }

    @Override
    public IExpressionNode getImportNameNode()
    {
        return null;
    }

    @Override
    public IWorkspace getWorkspace()
    {
        return compilerProject.getWorkspace();
    }
}
