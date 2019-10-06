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

package org.apache.royale.compiler.internal.tree.as;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;
import org.apache.royale.compiler.tree.as.IUseNamespaceNode;

public class UseNamespaceNode extends FixedChildrenNode implements IUseNamespaceNode
{
    /**
     * Constructor.
     * 
     * @param namespace The expression node representing the namespace being used.
     */
    public UseNamespaceNode(ExpressionNodeBase namespace)
    {
        setTargetNamespace(namespace);
    }

    /**
     * Package to import
     */
    protected INamespaceDecorationNode namespaceNode;
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.UseID;
    }

    @Override
    public int getChildCount()
    {
        return namespaceNode != null ? 1 : 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return namespaceNode;
        
        return null;
    }
    
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (namespaceNode != null)
        {
            ((NodeBase)namespaceNode).normalize(fillInOffsets);
            ((NodeBase)namespaceNode).setParent(this);
        }
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
            NamespaceDefinition.addUseNamespaceDirectiveToScope(scope, getTargetNamespaceNode());
    }
    
    //
    // IUseNamespaceNode implementations
    //
    
    @Override
    public INamespaceDecorationNode getTargetNamespaceNode()
    {
        return namespaceNode;
    }

    @Override
    public String getTargetNamespace()
    {
        return namespaceNode != null ? namespaceNode.getName() : "";
    }

    //
    // Other methods
    //

    public void setTargetNamespace(ExpressionNodeBase namespace)
    {
        if (namespace != null)
        {
            if (namespace instanceof FullNameNode)
            {
                namespaceNode = new QualifiedNamespaceExpressionNode((FullNameNode)namespace);
            }
            else
            {
                namespaceNode = new NamespaceIdentifierNode(((IIdentifierNode)namespace).getName());
                ((NodeBase)namespaceNode).setSourcePath(namespace.getSourcePath());
                ((NodeBase)namespaceNode).span(namespace.getAbsoluteStart(), namespace.getAbsoluteEnd(), namespace.getLine(), namespace.getColumn(), namespace.getEndLine(), namespace.getEndColumn());
            }
        }
    }
}
