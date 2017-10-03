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

import java.util.Iterator;
import java.util.List;

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition.VariableClassification;
import org.apache.royale.compiler.internal.tree.as.parts.AccessorFunctionContentsPart;
import org.apache.royale.compiler.internal.tree.as.parts.IAccessorFunctionContentsPart;
import org.apache.royale.compiler.internal.tree.as.parts.IFunctionContentsPart;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.ICommonClassNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.IKeywordNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.decorators.IVariableTypeDecorator;
import org.apache.royale.compiler.tree.as.decorators.SymbolDecoratorProvider;

/**
 * ActionScript parse tree node that acts as a base class for getters and
 * setters.
 */
public abstract class AccessorNode extends FunctionNode implements IAccessorNode
{
    /**
     * Constructor.
     * 
     * @param nameNode node containing name of setter
     */
    public AccessorNode(IASToken functionKeyword, IASToken accessorKeyword, IdentifierNode nameNode)
    {
        super(functionKeyword, nameNode);

        if (accessorKeyword != null)
            ((IAccessorFunctionContentsPart)contentsPart).setAccessorKeyword(new KeywordNode(accessorKeyword));
    }

    /**
     * Constructor.
     * 
     * @param node An identifier node specifying the name of the accessor.
     * @param part An object storing the <code>get</code> or <code>set</code> keyword.
     */
    public AccessorNode(IdentifierNode node, IAccessorFunctionContentsPart part)
    {
        super(node, part);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        addDecorationChildren(fillInOffsets);
        addChildInOrder(contentsPart.getFunctionKeywordNode(), fillInOffsets);
        addChildInOrder(((IAccessorFunctionContentsPart)contentsPart).getAccessorKeyword(), fillInOffsets);
        addChildInOrder(nameNode, fillInOffsets);
        addChildInOrder(contentsPart.getParametersNode(), fillInOffsets);
        addChildInOrder(typeNode, fillInOffsets);
        addChildInOrder(contentsPart.getContents(), fillInOffsets);
    }

    //
    // FunctionNode overrides
    //

    @Override
    public boolean isConstructor()
    {
        return false;
    }

    @Override
    public boolean isCastFunction()
    {
        return false;
    }

    @Override
    // TODO Remove unnecessary override.
    public boolean isImplicit()
    {
        return super.isImplicit();
    }

    @Override
    public String getReturnType()
    {
        IDefinition definition = getDefinition();
        
        List<IVariableTypeDecorator> list =
            SymbolDecoratorProvider.getProvider().getVariableTypeDecorators(definition);
        
        if (list.size() > 0)
        {
            Iterator<IVariableTypeDecorator> it = list.iterator();
            while (it.hasNext())
            {
                IDefinition type = it.next().decorateVariableType(definition);
                if (type instanceof ITypeDefinition)
                    return type.getQualifiedName();
            }
        }
        
        return super.getReturnType();
    }

    @Override
    protected IFunctionContentsPart createContentsPart()
    {
        return new AccessorFunctionContentsPart();
    }
    
    //
    // IVariableNode implementations
    //
    
    @Override
    public VariableClassification getVariableClassification()
    {
        IScopedNode scopedNode = getScopeNode();
        
        IASNode node = scopedNode;
        
        if (node instanceof ICommonClassNode || node.getParent() instanceof ICommonClassNode)
            return VariableClassification.CLASS_MEMBER;
        
        if (node.getParent() instanceof IInterfaceNode)
            return VariableClassification.INTERFACE_MEMBER;
        
        if (node.getParent() instanceof PackageNode)
            return VariableClassification.PACKAGE_MEMBER;
        
        return VariableClassification.LOCAL;
    }
    
    @Override
    public IExpressionNode getAssignedValueNode()
    {
        return null;
    }

    @Override
    public int getDeclarationEnd()
    {
        return getEnd();
    }

    //
    // IAccessorNode implementations
    //

    @Override
    public IKeywordNode getAccessorKeywordNode()
    {
        return ((IAccessorFunctionContentsPart)contentsPart).getAccessorKeyword();
    }
}
