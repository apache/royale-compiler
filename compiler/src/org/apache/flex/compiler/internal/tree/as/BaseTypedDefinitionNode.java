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

package org.apache.flex.compiler.internal.tree.as;

import org.apache.flex.compiler.parsing.IASToken;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.ITypedNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;

/**
 * Base class for definitions that have a type associated with them
 */
public abstract class BaseTypedDefinitionNode extends BaseDefinitionNode implements ITypedNode
{
    /**
     * Constructor.
     */
    public BaseTypedDefinitionNode()
    {
        super();
    }

    /**
     * Offset at which the type operator (":") starts
     */
    protected int typeOperatorStart;

    /**
     * The type of the variable
     */
    protected ExpressionNodeBase typeNode;

    //
    // BaseDefinitionNode overrides
    //

    @Override
    protected void init(ExpressionNodeBase nameNode)
    {
        super.init(nameNode);
        
        typeOperatorStart = -1;
        typeNode = null;
    }
    
    //
    // ITypedNode implementations
    //
    
    @Override
    public ExpressionNodeBase getTypeNode()
    {
        return typeNode;
    }

    @Override
    public boolean hasTypeOperator()
    {
        return typeOperatorStart != -1;
    }

    @Override
    public int getTypeOperatorStart()
    {
        return typeOperatorStart;
    }

    @Override
    public int getTypeOperatorEnd()
    {
        return typeOperatorStart + 1;
    }

    //
    // Other methods
    //

    /**
     * Determines if this typed definition has an explicit type specification
     * 
     * @return true if we have an actual type
     */
    public boolean hasExplicitType()
    {
        if (typeNode instanceof IdentifierNode)
            return !((IdentifierNode)typeNode).isImplicit();

        return typeNode != null;
    }

    /**
     * Returns the type of this node in String form
     * 
     * @return the types name, or an empty string
     */
    public String getTypeName()
    {
        return hasExplicitType() ? ((IIdentifierNode)typeNode).getName() : "";
    }

    public boolean isVoidType()
    {
        return typeNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode)typeNode).getKind() == LanguageIdentifierKind.VOID;
    }

    public boolean isAnyType()
    {
        return typeNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode)typeNode).getKind() == LanguageIdentifierKind.ANY_TYPE;
    }

    /**
     * Set the type node. Used during parsing.
     * 
     * @param typeOperator ASToken containing the type operator (":")
     * @param variableType node containing the variable type
     */
    public void setType(IASToken typeOperator, ExpressionNodeBase variableType)
    {
        if (typeOperator != null)
            typeOperatorStart = typeOperator.getStart();

        typeNode = variableType;
    }
}
