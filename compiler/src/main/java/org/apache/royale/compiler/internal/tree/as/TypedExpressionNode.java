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

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.AppliedVectorDefinitionFactory;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ITypedExpressionNode;

/**
 * AST node for generic type such as: {@code Vector.<T>}.
 */
public class TypedExpressionNode extends ExpressionNodeBase implements ITypedExpressionNode
{
    /**
     * Constructor.
     * <p>
     * Creates a {@code TypedExpressionNode} from its two components and the
     * {@code .<} token.
     * 
     * @param collection The generic collection type node.
     * @param type Type parameter node.
     * @param openToken The {@code .<} token. Null-able.
     */
    public TypedExpressionNode(ExpressionNodeBase collection, ExpressionNodeBase type, ASToken openToken)
    {
        assert collection != null : "collection type node can't be null";
        assert type != null : "type parameter node can't be null";

        collectionNode = collection;
        typeNode = type;
        
        if (openToken == null)
        {
            typeOperatorStart = ISourceLocation.UNKNOWN;
            typeOperatorEnd = ISourceLocation.UNKNOWN;
        }
        else
        {
            typeOperatorStart = openToken.getStart();
            typeOperatorEnd = openToken.getEnd();
        }
        
        span(collection, type);
    }

    /**
     * Constructor.
     * 
     * @param collection The node representing the base type (i.e., Vector).
     * @param type The node representing the element type.
     */
    public TypedExpressionNode(ExpressionNodeBase collection, ExpressionNodeBase type)
    {
        this(collection, type, null);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected TypedExpressionNode (TypedExpressionNode other)
    {
        super(other);
        
        this.collectionNode = other.collectionNode != null ? other.collectionNode.copy() : null;
        this.typeNode = other.typeNode != null ? other.typeNode.copy() : null;
        this.typeOperatorStart = other.typeOperatorStart;
        this.typeOperatorEnd = other.typeOperatorEnd;
    }

    /**
     * The {@code Vector} in {@code Vector.<T>}.
     */
    private final ExpressionNodeBase collectionNode;

    /**
     * The {@code T} in {@code Vector.<T>}.
     */
    private final ExpressionNodeBase typeNode;

    /**
     * Offset at which the type operator {@code .<} starts
     */
    private final int typeOperatorStart;

    /**
     * Offset at which the type operator {@code .<} ends
     */
    private final int typeOperatorEnd;

    //
    // NodeBase overrides
    //
    
    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.TypedExpressionID;
    }

    @Override
    public int getChildCount()
    {
        if (typeNode != null && collectionNode != null)
            return 2;
        else if (typeNode != null || collectionNode != null)
            return 1;
        return 0;
    }

    @Override
    public IASNode getChild(int i)
    {
        switch (i)
        {
            case 0:
            {
                if (typeNode != null && collectionNode != null)
                {
                    if (typeNode.getAbsoluteStart() < collectionNode.getAbsoluteStart())
                        return typeNode;
                }
                return collectionNode;
            }
            case 1:
            {
                if (typeNode != null && collectionNode != null)
                {
                    if (typeNode.getAbsoluteStart() < collectionNode.getAbsoluteStart())
                        return collectionNode;
                }
                return typeNode;
            }
        }

        return null;
    }
    
    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (collectionNode != null)
            collectionNode.setParent(this);
        
        if (typeNode != null)
            typeNode.setParent(this);
    }

    //
    // ExpressionNodeBase overrides
    //
    
    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        IDefinition base = collectionNode.resolve(project);
        IDefinition resolvedType = null;
        if (base != null)
        {
            IDefinition vectorDef = project.getBuiltinType(IASLanguageConstants.BuiltinType.VECTOR);
            if (base == vectorDef) // Only works with Vector for now
            {
                IDefinition param = typeNode.resolve(project);
                if (param instanceof ITypeDefinition)
                    resolvedType = AppliedVectorDefinitionFactory.newVector(project, (ITypeDefinition)param);
            }
        }
        return resolvedType;
    }

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        IDefinition d = resolve(project);
        if (d != null)
            return d.resolveType(project);

        return null;
    }

    @Override
    protected TypedExpressionNode copy()
    {
        return new TypedExpressionNode(this);
    }

    @Override
    public Name getMName(ICompilerProject project)
    {
        // resolve the result of the expression, which will
        // return a AppliedVectorDefinition for a vectorized type
        // or null in other cases.
        IDefinition d = resolve(project);
        if (d != null)
            return ((DefinitionBase)d).getMName(project);

        return null;
    }

    @Override
    public String computeSimpleReference()
    {
        return getName();
    }

    @Override
    public IReference computeTypeReference()
    {
        IReference base = collectionNode.computeTypeReference();
        IReference param = typeNode.computeTypeReference();
        return ReferenceFactory.parameterizedReference(getWorkspace(), base, param);
    }
    
    //
    // ITypedExpressionNode implementations
    //
    
    @Override
    public String getName()
    {
        StringBuilder builder = new StringBuilder();
        if (collectionNode instanceof IIdentifierNode)
        {
            builder.append(((IIdentifierNode)collectionNode).getName());
        }
        if (typeNode instanceof IIdentifierNode)
        {
            builder.append(".<");
            builder.append(((IIdentifierNode)typeNode).getName());
            builder.append(">");
        }
        return builder.toString();
    }

    @Override
    public IdentifierType getIdentifierType()
    {
        return IdentifierType.TYPED_NAME;
    }

    @Override
    public IExpressionNode getCollectionNode()
    {
        return collectionNode;
    }

    @Override
    public IExpressionNode getTypeNode()
    {
        return typeNode;
    }

    @Override
    public boolean hasTypedOperator()
    {
        return typeOperatorStart != ISourceLocation.UNKNOWN && typeOperatorEnd != ISourceLocation.UNKNOWN;
    }
    
    //
    // Other methods
    //
    
    // Add to interface?
    public int getTypedOperatorStart()
    {
        return typeOperatorStart;
    }

    // Add to interface?
    public int getTypedOperatorEnd()
    {
        return typeOperatorEnd;
    }
}
