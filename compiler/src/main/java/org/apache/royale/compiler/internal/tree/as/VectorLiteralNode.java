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

import org.apache.royale.compiler.definitions.AppliedVectorDefinitionFactory;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;

public class VectorLiteralNode extends BaseLiteralContainerNode
{
    /**
     * Constructor.
     */
    public VectorLiteralNode()
    {
        super(new LiteralNode(LiteralType.VECTOR, ""));
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected VectorLiteralNode(VectorLiteralNode other)
    {
        super(other);
        
        this.collectionTypeNode = other.collectionTypeNode != null ? other.collectionTypeNode.copy() : null;
    }

    private ExpressionNodeBase collectionTypeNode;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.VectorLiteralID;
    }

    @Override
    public int getChildCount()
    {
        if (collectionTypeNode != null && contentsNode != null)
            return 2;

        else if (collectionTypeNode != null || contentsNode != null)
            return 1;

        return 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i == 0)
            return collectionTypeNode != null ? collectionTypeNode : contentsNode;

        if (i == 1)
            return contentsNode;

        return null;
    }
    
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (collectionTypeNode != null)
            collectionTypeNode.setParent(this);

        if (contentsNode != null)
            contentsNode.setParent(this);
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        IDefinition type = collectionTypeNode.resolve(project);
        if (type instanceof ITypeDefinition)
        {
            IDefinition v = AppliedVectorDefinitionFactory.newVector(
                project, (ITypeDefinition)type);
            return v;
        }
        return null;
    }
    
    // TODO Should there be an override for resolveType()?
    
    @Override
    protected VectorLiteralNode copy()
    {
        return new VectorLiteralNode(this);
    }

    @Override
    public boolean isDynamicExpression(ICompilerProject project)
    {
        return collectionTypeNode.isDynamicExpression(project);
    }

    //
    // BaseLiteralContainerNode overrides
    //

    @Override
    public LiteralType getLiteralType()
    {
        return LiteralType.VECTOR;
    }
    
    //
    // Other methods
    //

    public ExpressionNodeBase getCollectionTypeNode()
    {
        return collectionTypeNode;
    }

    public void setCollectionTypeNode(ExpressionNodeBase collectionType)
    {
        collectionTypeNode = collectionType;
    }



}
