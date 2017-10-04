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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.royale.compiler.common.IEmbedResolver;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.exceptions.CodegenInterruptedException;
import org.apache.royale.compiler.internal.projects.ASProject;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnit;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnitFactory;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.mxml.IMXMLEmbedNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.utils.FilenameNormalization;

/**
 * Node to represent the MXML @Embed compiler directive
 */
class MXMLEmbedNode extends MXMLCompilerDirectiveNodeBase implements IMXMLEmbedNode, IEmbedResolver
{
    /**
     * Constructor.
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLEmbedNode(MXMLNodeBase parent)
    {
        super(parent);

        // When @Embed() is found in a property value, the MXMLEmbedNode
        // gets created and later wrapped by an MXMLClassNode.
        // So it doesn't not initially have a parent.
        if (parent != null)
            setContainingSourceFilename();
    }

    private String containingSourceFilename = null;

    @Override
    public void setParent(NodeBase parent)
    {
        super.setParent(parent);

        if (parent != null)
            setContainingSourceFilename();
    }

    private void setContainingSourceFilename()
    {
        // TODO Refactor this to avoid walking up to the file node.
        IMXMLFileNode fileNode = (IMXMLFileNode)getAncestorOfType(IMXMLFileNode.class);
        containingSourceFilename = FilenameNormalization.normalize(fileNode.getSourcePath());
    }

    @Override
    public void initializeFromText(MXMLTreeBuilder builder,
                                   String text, ISourceLocation location)
    {
        parseTextAndSetAttributes(builder, text, location, IMetaAttributeConstants.ATTRIBUTE_EMBED);

        MXMLFileNode fileNode = builder.getFileNode();
        fileNode.addEmbedNode(this);
    }

    @Override
    public EmbedCompilationUnit resolveCompilationUnit(ICompilerProject project, Collection<ICompilerProblem> problems) throws InterruptedException
    {
        assert (project instanceof ASProject);
        return EmbedCompilationUnitFactory.getCompilationUnit((ASProject)project, containingSourceFilename, this, attributes, problems);
    }

    @Override
    public EmbedCompilationUnit resolveCompilationUnit(ICompilerProject project) throws InterruptedException
    {
        assert (project instanceof ASProject);
        Collection<ICompilerProblem> ignoredProblems = new ArrayList<ICompilerProblem>();
        return resolveCompilationUnit(project, ignoredProblems);
    }

    @Override
    public IClassDefinition getClassReference(ICompilerProject project)
    {
        IClassDefinition classReference = super.getClassReference(project);
        if (classReference != null)
            return classReference;

        try
        {
            // generated embed compilation unit has not been resolved yet,
            // so resolve it and set the reference.
            List<ICompilerProblem> problems = new LinkedList<ICompilerProblem>();
            EmbedCompilationUnit cu = resolveCompilationUnit(project, problems);
            if (cu == null)
                return null;
            setClassReference((RoyaleProject)project, cu.getName());
        }
        catch (InterruptedException e)
        {
            throw new CodegenInterruptedException(e);
        }

        return super.getClassReference(project);
    }

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLEmbedID;
    }

    @Override
    public String getName()
    {
        return "Embed";
    }
}
