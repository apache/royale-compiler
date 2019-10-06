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

import java.io.Reader;
import java.util.Collection;

import org.apache.royale.compiler.internal.parsing.ISourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragment;
import org.apache.royale.compiler.internal.parsing.SourceFragmentsReader;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.OffsetLookup;
import org.apache.royale.compiler.internal.scopes.MXMLFileScope;
import org.apache.royale.compiler.internal.tree.as.ExpressionNodeBase;
import org.apache.royale.compiler.internal.tree.as.NodeBase;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLTagAttributeData;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingAttributeNode;

/**
 * Implementation of the {@code IMXMLBindingAttributeNode} interface.
 */
class MXMLBindingAttributeNode extends MXMLNodeBase implements IMXMLBindingAttributeNode
{
    /**
     * Constructor
     * 
     * @param parent The parent node of this node, or <code>null</code> if there
     * is no parent.
     */
    MXMLBindingAttributeNode(NodeBase parent)
    {
        super(parent);
    }

    /**
     * Constructor
     * 
     * @param parent The parent node of this node
     * @param expr The IExpressionNode for this BindingAttributeNode
     */
    MXMLBindingAttributeNode(NodeBase parent, IExpressionNode expr)
    {
        super(parent);
        this.expressionNode = expr;
    }

    private String attributeName; // "source" or "destination"

    private IExpressionNode expressionNode;

    @Override
    public ASTNodeID getNodeID()
    {
        // TODO Auto-generated method stub
        return ASTNodeID.MXMLBindingAttributeID;
    }

    @Override
    public String getName()
    {
        return attributeName;
    }

    public void setName(String s)
    {
    	attributeName = s;
    }
    
    @Override
    public int getChildCount()
    {
        return expressionNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        return i == 0 ? expressionNode : null;
    }

    @Override
    public IExpressionNode getExpressionNode()
    {
        return expressionNode;
    }

    public void initializeFromAttribute(MXMLTreeBuilder builder,
                                        IMXMLTagAttributeData attribute)
    {
        attributeName = attribute.getName();
        setLocation(attribute);
        expressionNode = createExpressionNode(builder, attribute);

        // ASParser creates the ExpressionNodeBase as a child of a FileNode.
        // Make it a child of the this MXMLBindingAttributeNode.
        if (expressionNode != null)
            ((ExpressionNodeBase)expressionNode).setParent(this);
    }

    private IExpressionNode createExpressionNode(MXMLTreeBuilder builder,
                                                 IMXMLTagAttributeData attribute)
    {
        Workspace workspace = builder.getWorkspace();
        Collection<ICompilerProblem> problems = builder.getProblems();

        ISourceFragment[] fragments = attribute.getValueFragments(problems);

        // Adjust fragment offsets from local to absolute,
        // to take into account include files, source attributes, etc.
        final MXMLFileScope fileScope = builder.getFileScope();
        final OffsetLookup offsetLookup = fileScope.getOffsetLookup();
        assert offsetLookup != null : "Expected OffsetLookup on FileScope.";
        for (ISourceFragment fragment : fragments)
        {
            int physicalStart = fragment.getPhysicalStart();
            final int[] absoluteOffsets = offsetLookup.getAbsoluteOffset(attribute.getSourcePath(), physicalStart);
            ((SourceFragment)fragment).setPhysicalStart(absoluteOffsets[0]);
        }

        // Parse the fragments inside the databinding expression.
        Reader reader = new SourceFragmentsReader(attribute.getSourcePath(), fragments);
        return ASParser.parseDataBinding(workspace, reader, problems);
    }

    /**
     * For debugging only. Builds a string such as <code>"source"</code> from
     * the attribute name.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append('"');
        sb.append(getName());
        sb.append('"');

        return true;
    }
}
