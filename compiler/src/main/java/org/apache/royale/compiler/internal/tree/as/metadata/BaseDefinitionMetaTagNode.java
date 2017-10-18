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

package org.apache.royale.compiler.internal.tree.as.metadata;

import java.util.Collection;
import java.util.EnumSet;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.IMetaInfo;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.BaseDefinitionNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;

/**
 * For metatags that decorate a definition, such as Events, Effects and Styles,
 * we treat them internally as IDefinitions. This is the common baseclass for
 * all of them
 */
public abstract class BaseDefinitionMetaTagNode extends MetaTagNode implements IDefinitionNode, IDocumentableDefinitionNode
{
    /**
     * The name of the node
     */
    protected IdentifierNode nameNode;

    /**
     * Node that represents the comment range
     */
    private IASDocComment asDocComment;

    public BaseDefinitionMetaTagNode(MetaTagNode other)
    {
        super(other);
    }

    public BaseDefinitionMetaTagNode(String name)
    {
        super(name);
    }

    public void setName(IdentifierNode name)
    {
        if (name != null)
        {
            nameNode = name;
            nameNode.setParent(this);
        }
        addToMap(IMetaAttributeConstants.NAME_EVENT_NAME, getName());
    }

    @Override
    public String getName()
    {
        if (nameNode != null)
            return nameNode.getName();
        return "";
    }

    @Override
    public IExpressionNode getNameExpressionNode()
    {
        return nameNode;
    }

    public IdentifierNode getNameNode()
    {
        return nameNode;
    }

    @Override
    public int getNameEnd()
    {
        if (nameNode != null)
            return nameNode.getEnd();
        return -1;
    }

    @Override
    public int getNameStart()
    {
        if (nameNode != null)
            return nameNode.getStart();
        return -1;
    }

    @Override
    public int getNameAbsoluteStart()
    {
        if (nameNode != null)
            return nameNode.getAbsoluteStart();
        return -1;
    }

    @Override
    public int getNameAbsoluteEnd()
    {
        if (nameNode != null)
            return nameNode.getAbsoluteEnd();
        return -1;
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
    }

    @Override
    public String getShortName()
    {
        return getName();
    }

    @Override
    public String getQualifiedName()
    {
        String parentQualifiedName = getParentQualifiedName();
        if (parentQualifiedName != null && parentQualifiedName.length() > 0)
            return parentQualifiedName + "." + getShortName();
        if (getPackageName() != null && getPackageName().length() > 0)
            return getPackageName() + "." + getShortName();

        return getShortName();
    }

    /**
     * Returns true if our definition matches the passed in definition
     * 
     * @param node the {@link BaseDefinitionNode} to compare against
     * @return true if we are a match
     */
    public boolean matches(BaseDefinitionMetaTagNode node)
    {
        if (node == null)
            return false;

        if (node.getClass() != getClass())
            return false;

        if (node == this)
            return true;

        if (node.getQualifiedName().compareTo(getQualifiedName()) != 0)
            return false;

        String packageName = node.getPackageName();
        String packageName2 = getPackageName();
        if (packageName == null && packageName2 != null)
            return false;

        if (packageName != null && packageName2 != null && packageName.compareTo(packageName2) != 0)
            return false;

        if (node.getContainingFilePath().compareTo(getContainingFilePath()) != 0)
                return false;
        return true;
    }

    public String getParentQualifiedName()
    {
        IASNode parent = getParent();
        if (parent instanceof MetaTagsNode)
        {
            IASNode decNode = ((MetaTagsNode)parent).getDecoratedDefinition();
            if (decNode != null)
                return ((IDefinitionNode)decNode).getQualifiedName();
        }
        return null;
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        return false;
    }

    @Override
    public boolean hasNamespace(String namespace)
    {
        return false;
    }

    @Override
    public boolean isImplicit()
    {
        return false;
    }

    @Override
    public IMetaTagsNode getMetaTags()
    {
        return null;
    }
    
    @Override
    public IMetaInfo[] getMetaInfos()
    {
        return new IMetaInfo[0];
    }

    @Override
    public String getNamespace()
    {
        return null;
    }

    public INamespaceNode resolveNamespace()
    {
        return null;
    }

    public void setASDocComment(IASDocComment ref)
    {
        asDocComment = ref;
    }

    /**
     * Determines if this node has an explicit comment
     */
    @Override
    public boolean hasExplicitComment()
    {
        return asDocComment != null;
    }

    /**
     * Returns the raw {@link IASDocComment} without any processing
     * 
     * @return an {@link IASDocComment} or null
     */
    public IASDocComment getASDocComment()
    {
        return asDocComment;
    }
}
