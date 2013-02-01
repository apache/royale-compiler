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

package org.apache.flex.compiler.internal.tree.mxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.MXMLDualContentProblem;
import org.apache.flex.compiler.problems.UnexpectedExceptionProblem;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.mxml.MXMLDialect;
import org.apache.flex.compiler.internal.parsing.as.ASParser;
import org.apache.flex.compiler.internal.parsing.as.IncludeHandler;
import org.apache.flex.compiler.internal.parsing.as.OffsetLookup;
import org.apache.flex.compiler.internal.projects.CompilerProject;
import org.apache.flex.compiler.internal.projects.FlexProject;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.scopes.MXMLFileScope;
import org.apache.flex.compiler.internal.semantics.PostProcessStep;
import org.apache.flex.compiler.internal.tree.as.NodeBase;
import org.apache.flex.compiler.internal.tree.as.ScopedBlockNode;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.mxml.IMXMLLanguageConstants;
import org.apache.flex.compiler.mxml.IMXMLTagAttributeData;
import org.apache.flex.compiler.mxml.MXMLTagData;
import org.apache.flex.compiler.mxml.MXMLTextData;
import org.apache.flex.compiler.mxml.MXMLUnitData;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;

import static org.apache.flex.compiler.mxml.IMXMLLanguageConstants.*;

class MXMLScriptNode extends MXMLNodeBase implements IMXMLScriptNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLScriptNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * The ActionScript nodes representing the code inside the script tag.
     */
    private IASNode[] asNodes;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLScriptID;
    }

    @Override
    public IASNode getChild(int i)
    {
        return asNodes != null ? asNodes[i] : null;
    }

    @Override
    public int getChildCount()
    {
        return asNodes != null ? asNodes.length : 0;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.SCRIPT;
    }

    @Override
    public IASNode[] getASNodes()
    {
        if (asNodes == null)
            asNodes = new MXMLScriptNode[0];
        return asNodes;
    }

    @Override
    protected MXMLNodeInfo createNodeInfo(MXMLTreeBuilder builder)
    {
        return new MXMLNodeInfo(builder);
    }

    @Override
    protected void processTagSpecificAttribute(MXMLTreeBuilder builder, MXMLTagData tag,
                                               IMXMLTagAttributeData attribute,
                                               MXMLNodeInfo info)
    {
        if (attribute.isSpecialAttribute(ATTRIBUTE_SOURCE))
        {
            MXMLClassDefinitionNode classNode = getContainingClassNode();
            ASScope classScope = (ASScope)classNode.getClassDefinition().getContainedScope();

            // Resolve the attribute value to a normalized path.
            // Doing so makes this compilation unit dependent on that file.
            String sourcePath = resolveSourceAttributePath(builder, attribute, info);
            if (sourcePath != null)
            {
                FlexProject project = builder.getProject();
                Workspace workspace = builder.getWorkspace();
                Collection<ICompilerProblem> problems = builder.getProblems();
                IFileSpecification sourceFileSpec = workspace.getFileSpecification(sourcePath);
                String scriptText = builder.readExternalFile(attribute, sourcePath);

                if (scriptText != null)
                {
                    final IncludeHandler includeHandler = new IncludeHandler(builder.getFileSpecificationGetter());
                    includeHandler.setProjectAndCompilationUnit(project, builder.getCompilationUnit());
                    includeHandler.enterFile(sourcePath);
                    final MXMLFileScope fileScope = builder.getFileScope();
                    final OffsetLookup offsetLookup = fileScope.getOffsetLookup();
                    assert offsetLookup != null : "Expected OffsetLookup on FileScope.";
                    final int[] absoluteOffset = offsetLookup.getAbsoluteOffset(sourcePath, 0);

                    final ScopedBlockNode fragment = ASParser.parseFragment2(
                            scriptText,
                            sourceFileSpec.getPath(),
                            absoluteOffset[0],
                            0,
                            0,
                            problems,
                            workspace,
                            builder.getFileNode(),
                            classScope,
                            project.getProjectConfigVariables(),
                            EnumSet.of(PostProcessStep.CALCULATE_OFFSETS, PostProcessStep.RECONNECT_DEFINITIONS),
                            true /* follow includes */,
                            includeHandler);

                    builder.getFileNode().updateIncludeTreeLastModified(includeHandler.getLastModified());

                    // Make the statements inside the script tag the children of this node.
                    int n = fragment.getChildCount();
                    asNodes = new IASNode[n];
                    for (int i = 0; i < n; i++)
                    {
                        IASNode child = fragment.getChild(i);
                        asNodes[i] = child;
                        ((NodeBase)child).setParent(this);
                    }
                }
            }
        }
        else
        {
            super.processTagSpecificAttribute(builder, tag, attribute, info);
        }
    }

    @Override
    protected void processChildNonWhitespaceUnit(MXMLTreeBuilder builder, MXMLTagData tag,
                                                 MXMLTextData text,
                                                 MXMLNodeInfo info)
    {
        info.hasDualContent = true;
    }

    private MXMLClassDefinitionNode getContainingClassNode()
    {
        MXMLClassDefinitionNode result = (MXMLClassDefinitionNode)getAncestorOfType(MXMLClassDefinitionNode.class);
        assert result != null;
        return result;
    }

    @Override
    protected void initializationComplete(MXMLTreeBuilder builder, MXMLTagData tag,
                                          MXMLNodeInfo info)
    {
        super.initializationComplete(builder, tag, info);

        if (info.hasSourceAttribute && info.hasDualContent)
        {
            ICompilerProblem problem = new MXMLDualContentProblem(tag, tag.getShortName());
            builder.addProblem(problem);
            return;
        }

        final MXMLDialect mxmlDialect = builder.getMXMLDialect();
        final String sourcePath = tag.getParent().getFileSpecification().getPath();
        final MXMLClassDefinitionNode classNode = getContainingClassNode();
        final ASScope classScope = (ASScope)classNode.getClassDefinition().getContainedScope();
        final CompilerProject project = builder.getProject();
        final OffsetLookup offsetLookup = classScope.getFileScope().getOffsetLookup();

        setSourcePath(sourcePath);
        try
        {
            // parse inline ActionScript
            final List<ScopedBlockNode> scriptNodes = new ArrayList<ScopedBlockNode>();
            for (MXMLUnitData unit = tag.getFirstChildUnit(); unit != null; unit = unit.getNextSiblingUnit())
            {
                if (unit instanceof MXMLTextData)
                {
                    final MXMLTextData mxmlTextData = (MXMLTextData)unit;
                    String text = mxmlTextData.getCompilableText();
                    if (!mxmlDialect.isWhitespace(text))
                    {
                        // local offset at the end of the containing open script tag
                        assert offsetLookup != null : "Expected OffsetLookup on FileScope.";
                        final int localOffset = mxmlTextData.getParentUnitData().getAbsoluteEnd();
                        final int[] absoluteOffsets = offsetLookup.getAbsoluteOffset(sourcePath, localOffset);
                        final int absoluteOffset = absoluteOffsets[0];

                        // create an include handler and mock its state as if it is
                        // before parsing for scope building
                        final IncludeHandler includeHandler = IncludeHandler.createForASTBuilding(
                                builder.getFileSpecificationGetter(),
                                sourcePath,
                                localOffset,
                                absoluteOffset);

                        // parse and build AST
                        final EnumSet<PostProcessStep> postProcess = EnumSet.of(
                                PostProcessStep.CALCULATE_OFFSETS,
                                PostProcessStep.RECONNECT_DEFINITIONS);
                        final ScopedBlockNode node = ASParser.parseInlineScript(
                                builder.getFileNode(),
                                mxmlTextData,
                                builder.getProblems(),
                                classScope,
                                project.getProjectConfigVariables(),
                                includeHandler,
                                postProcess);
                        MXMLFileNode filenode = builder.getFileNode();
                        filenode.updateIncludeTreeLastModified(includeHandler.getLastModified());
                        assert node != null : "Expected node from ASParser.getScopesFromInlineScript().";
                        scriptNodes.add(node);
                    }
                }
            }

            // Make the statements inside the script tag the children of this node.
            for (ScopedBlockNode script : scriptNodes)
            {
                int n = script.getChildCount();
                asNodes = new IASNode[n];
                for (int i = 0; i < n; i++)
                {
                    IASNode child = script.getChild(i);
                    asNodes[i] = child;
                    ((NodeBase)child).setParent(this);
                }
            }
        }
        catch (Exception e)
        {
            builder.addProblem(new UnexpectedExceptionProblem(e));
        }
    }
}
