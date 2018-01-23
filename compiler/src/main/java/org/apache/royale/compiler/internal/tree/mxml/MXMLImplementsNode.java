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
import java.util.EnumSet;
import java.util.List;

import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragmentsReader;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.IRepairingTokenBuffer;
import org.apache.royale.compiler.internal.parsing.as.StreamingASTokenizer;
import org.apache.royale.compiler.internal.parsing.as.StreamingTokenBuffer;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.FileNode;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLLanguageConstants;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.mxml.IMXMLImplementsNode;

/**
 * Implementation of the {@code IMXMLImplementsNode} interface.
 */
class MXMLImplementsNode extends MXMLNodeBase implements IMXMLImplementsNode
{
    // The {@code interfaceNodes} field is initialized to this empty array.
    private static final IIdentifierNode[] NO_INTERFACE_NODES = new IIdentifierNode[0];

    // These strings are used to turn the MXML attribute
    //   implements="com.whatever.IFoo, com.whatever.IBar"
    // into the class declaration
    //   internal class C implements com.whatever.IFoo, com.whatever.IBar {}
    // which can be parsed by the AS parser.
    // TODO It would be preferable to be able to parse
    // just the attribute value without turning it into a fake class declaration,
    // but currently the AS parser doesn't have a production for the
    // interface list or even the implements clause.
    private static final String STRING_TO_PREPEND = "internal class C implements ";
    private static final String STRING_TO_APPEND = " {}";

    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLImplementsNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * Storage for the child nodes representing the implemented interfaces.
     */
    private IIdentifierNode[] interfaceNodes = NO_INTERFACE_NODES;

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.MXMLImplementsID;
    }

    @Override
    public String getName()
    {
        return IMXMLLanguageConstants.ATTRIBUTE_IMPLEMENTS;
    }

    @Override
    public int getChildCount()
    {
        return interfaceNodes.length;
    }

    @Override
    public IASNode getChild(int i)
    {
        return interfaceNodes[i];
    }

    @Override
    public IIdentifierNode[] getInterfaceNodes()
    {
        return interfaceNodes;
    }

    /**
     * Initializes this {@code MXMLImplementNode} from the value of the
     * <code>implements</code> attribute.
     */
    protected void initializeFromAttribute(MXMLTreeBuilder builder,
                                           IMXMLTagAttributeData attribute)
    {
        setLocation(attribute);
        adjustOffsets(builder);

        Workspace workspace = builder.getWorkspace();
        Collection<ICompilerProblem> problems = builder.getProblems();

        // Fragmentize the attribute value, to deal with XML entities.
        ISourceFragment[] valueFragments = attribute.getValueFragments(problems);
        int n = valueFragments.length;

        // Turn the attribute value into fragments for a fake class declaration
        // that can be parsed into a {@code FileNode} by the AS parser.
        ISourceFragment[] fragments = new ISourceFragment[n + 2];
        fragments[0] = new SourceFragment(STRING_TO_PREPEND, STRING_TO_PREPEND, 0, 0, 0);
        for (int i = 0; i < n; i++)
        {
            fragments[i + 1] = valueFragments[i];
        }
        fragments[n + 1] = new SourceFragment(STRING_TO_APPEND, STRING_TO_APPEND, 0, 0, 0);

        // Parse the fake class declaration into a {@code FileNode}.
        SourceFragmentsReader reader = new SourceFragmentsReader(attribute.getSourcePath(), fragments);
        StreamingASTokenizer tokenizer = new StreamingASTokenizer();
        tokenizer.setReader(reader);
        IRepairingTokenBuffer buffer = new StreamingTokenBuffer(tokenizer);
        ASParser parser = new ASParser(workspace, buffer);
        FileNode fileNode = new FileNode(builder.getFileSpecificationGetter());
        parser.parseFile(fileNode, EnumSet.of(PostProcessStep.CALCULATE_OFFSETS));
        problems.addAll(tokenizer.getTokenizationProblems());
        problems.addAll(parser.getSyntaxProblems());

        // Find the nodes representing the interfaces inside the {@code FileNode}.
        List<IIdentifierNode> interfaceNodeList = new ArrayList<IIdentifierNode>();
        if (fileNode.getChildCount() == 1 && fileNode.getChild(0) instanceof IClassNode)
        {
            IClassNode classNode = (IClassNode)fileNode.getChild(0);
            for (IExpressionNode interfaceNode : classNode.getImplementedInterfaceNodes())
            {
                if (interfaceNode instanceof IIdentifierNode)
                {
                    interfaceNodeList.add((IIdentifierNode)interfaceNode);
                    // Reparent them onto this {@code MXMLImplementsNode}.
                    ((NodeBase)interfaceNode).setParent(this);
                }
            }
        }

        // Set the nodes representing the interfaces
        // as the children of this {@code MXMLImplementsNode}.
        interfaceNodes = interfaceNodeList.toArray(new IIdentifierNode[0]);
    }
}
