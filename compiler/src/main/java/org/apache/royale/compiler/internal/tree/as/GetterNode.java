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

import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.GetterDefinition;
import org.apache.royale.compiler.internal.tree.as.parts.IAccessorFunctionContentsPart;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IGetterNode;

/**
 * ActionScript parse tree node representing a getter definition (e.g. function
 * get foo():Number)
 */
public class GetterNode extends AccessorNode implements IGetterNode
{
    /**
     * Constructor.
     * 
     * @param nameNode The node holding name of getter.
     */
    public GetterNode(IASToken functionKeyword, IASToken getKeyword, IdentifierNode nameNode)
    {
        super(functionKeyword, getKeyword, nameNode);
    }

    /**
     * Constructor from SWC with a custom content part.
     * 
     * @param node An identifier node specifying the name of the getter.
     * @param part An object storing the <code>get</code> keyword.
     */
    public GetterNode(IdentifierNode node, IAccessorFunctionContentsPart part)
    {
        super(node, part);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.GetterID;
    }
    
    //
    // FunctionNode overrides
    //

    @Override
    FunctionDefinition buildDefinition()
    {
        FunctionDefinition definition = super.buildDefinition();
        definition.setTypeReference(definition.getReturnTypeReference());
        return definition;
    }

    @Override
    protected FunctionDefinition createFunctionDefinition(String name)
    {
        return new GetterDefinition(name);
    }
    
    //
    // AccessorNode overrides
    //
    
    @Override
    public IExpressionNode getAssignedValueNode()
    {
        return null;
    }
    
    //
    // IVariableNode implementations
    //

    @Override
    public IExpressionNode getVariableTypeNode()
    {
        return getTypeNode();
    }

    @Override
    public String getVariableType()
    {
        return getReturnType();
    }

    @Override
    public boolean isConst()
    {
        return false;
    }
}
