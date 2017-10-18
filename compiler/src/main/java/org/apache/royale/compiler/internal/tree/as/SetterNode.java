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

import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.SetterDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.parts.IAccessorFunctionContentsPart;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.ISetterNode;

/**
 * ActionScript parse tree node representing a setter definition (e.g. function
 * set foo(f:Number):Void)
 */
public class SetterNode extends AccessorNode implements ISetterNode
{
    /**
     * Constructor.
     * 
     * @param nameNode The node containing the name of the setter.
     */
    public SetterNode(IASToken functionKeyword, IASToken setKeyword, IdentifierNode nameNode)
    {
        super(functionKeyword, setKeyword, nameNode);
    }

    /**
     * Constructor from SWC with a custom content part.
     * 
     * @param node An identifier node specifying the name of the setter.
     * @param part An object storing the <code>set</code> keyword.
     */
    public SetterNode(IdentifierNode node, IAccessorFunctionContentsPart part)
    {
        super(node, part);
    }
    
    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.SetterID;
    }
    
    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        super.analyze(set, scope, problems);

        FunctionDefinition definition = getDefinition();
        if (definition != null)
        {
            ParameterDefinition[] parameters = definition.getParameters();
            if (parameters != null && parameters.length > 0)
                definition.setTypeReference(parameters[0].getTypeReference());
        }
    }
    
    //
    // FunctionNode overrides
    //
    
    @Override
    protected FunctionDefinition createFunctionDefinition(String name)
    {
        return new SetterDefinition(name);
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
        IParameterNode[] arguments = getParameterNodes();
        if (arguments.length > 0) // should be true for valid setters
            return arguments[0].getVariableTypeNode();

        return null;
    }
    
    @Override
    public String getVariableType()
    {
        IParameterNode[] arguments = getParameterNodes();
        if (arguments.length > 0) // should be true for valid setters
            return arguments[0].getVariableType();

        return "";
    }

    @Override
    public boolean isConst()
    {
        return false;
    }
}
