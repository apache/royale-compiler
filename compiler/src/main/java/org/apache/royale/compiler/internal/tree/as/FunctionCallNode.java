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

import java.util.ArrayList;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;

/**
 * ActionScript parse tree node representing a function call (e.g. myFunction(),
 * new Object())
 */
public class FunctionCallNode extends ExpressionNodeBase implements IFunctionCallNode
{
    /**
     * Constructor.
     */
    public FunctionCallNode(IASToken keywordNew, ExpressionNodeBase nameNode)
    {
        assert keywordNew != null : "Expected 'new' token.";
        assert nameNode != null : "Expected name expression.";
        
        newKeywordNode = new KeywordNode(keywordNew);
        this.nameNode = nameNode;
        argumentsNode = new ContainerNode(2);
    }

    /**
     * Constructor.
     */
    public FunctionCallNode(ExpressionNodeBase nameNode)
    {
        newKeywordNode = null;
        this.nameNode = nameNode;
        argumentsNode = new ContainerNode(2);
        setSourceLocation(nameNode);
    }

    /**
     * Copy constructor.
     *
     * @param other The node to copy.
     */
    protected FunctionCallNode(FunctionCallNode other)
    {
        super(other);

        this.newKeywordNode = other.newKeywordNode != null ? new KeywordNode(other.newKeywordNode) : null;
        this.nameNode = other.nameNode != null ? other.nameNode.copy() : null;
        this.argumentsNode = new ContainerNode(other.argumentsNode.getChildCount());

        for (IExpressionNode arg : other.getArgumentNodes())
        {
            argumentsNode.addItem(((ExpressionNodeBase)arg).copy());
        }
    }

    /**
     * The token that holds "new", if it is present
     */
    private KeywordNode newKeywordNode;

    /**
     * The node that describes the function being called
     */
    private ExpressionNodeBase nameNode;

    /**
     * The node that contains the arguments being passed
     */
    private ContainerNode argumentsNode;

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.FunctionCallID;
    }

    @Override
    public int getChildCount()
    {
        int count = 0;
        
        if (newKeywordNode != null)
            count++;
        
        if (nameNode != null)
            count++;
        
        if (argumentsNode != null)
            count++;
        
        return count;
    }

    @Override
    public IASNode getChild(int i)
    {
        if (i > getChildCount())
            return null;

        if (i == 0)
        {
            if (newKeywordNode != null)
                return newKeywordNode;
            return nameNode != null ? nameNode : argumentsNode;
        }
        else if (i == 1)
        {
            return newKeywordNode != null ? nameNode : argumentsNode;
        }
        else if (i == 2)
        {
            return argumentsNode;
        }
        
        return null;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (newKeywordNode != null)
            newKeywordNode.setParent(this);

        if (nameNode != null)
            nameNode.setParent(this);

        if (argumentsNode != null)
            argumentsNode.setParent(this);
    }

    @Override
    protected void replaceChild(NodeBase child, NodeBase target)
    {
        if (child == nameNode && target instanceof ExpressionNodeBase)
        {
            nameNode = (ExpressionNodeBase)target;
            nameNode.setParent(this);
        }
    }

    /*
     * For debugging only.
     * Builds a string such as <code>"trace"</code> from the
     * name of the function being called.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        String name = "";
        ExpressionNodeBase nameNode = getNameNode();
        if (nameNode instanceof IIdentifierNode)
            name = ((IIdentifierNode)nameNode).getName();

        sb.append('"');
        sb.append(name);
        sb.append('"');

        return true;
    }
    
    //
    // ExpressionNodeBase overrides
    //

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        IDefinition calledFunction = this.nameNode.resolve(project);

        if (calledFunction instanceof IFunctionDefinition &&
            // call of an accessor is just like call of a var
            !(calledFunction instanceof IAccessorDefinition))
        {
            //  new foo() returns the * type
            if (getNewKeywordNode() != null)
                return project.getBuiltinType(BuiltinType.ANY_TYPE);
            else {
                //special case: removeAt on a Vector needs to resolve its return type to type 'T' (element type), not 'Object' (as it does with Array method)
                if (calledFunction.getQualifiedName().equals("removeAt")
                    && calledFunction.getContainingScope() != null
                    && calledFunction.getContainingScope().getDefinition() instanceof IAppliedVectorDefinition)
                {
                    return ((IAppliedVectorDefinition)(calledFunction.getContainingScope().getDefinition())).resolveElementType(project);
                } else {
                    return ((IFunctionDefinition)calledFunction).resolveReturnType(project);
                }
            }
        }
        else if (calledFunction instanceof ITypeDefinition)
        {
            // The Date(...) function returns a String.
            if (!isNewExpression() && calledFunction.getQualifiedName().equals(IASLanguageConstants.Date))
                return project.getBuiltinType(BuiltinType.STRING);
                
            // Call of a class or interface - a cast - so the Type of the expression is the called class
            // should only have 1 argument, but semantics will check that
            return (ITypeDefinition)calledFunction;
        }

        // If we are anything else (var, accessor, etc)
        // or unknown (didn't resolve), then the result of the
        // call is '*' as far as the compiler is concerned.
        return project.getBuiltinType(BuiltinType.ANY_TYPE);
    }

    @Override
    protected FunctionCallNode copy()
    {
        return new FunctionCallNode(this);
    }

    //
    // IFunctionCallNode implementations
    //
    
    @Override
    public boolean isNewExpression()
    {
        return newKeywordNode != null;
    }

    @Override
    public ExpressionNodeBase getNameNode()
    {
        return nameNode;
    }

    @Override
    public String getFunctionName()
    {
        return nameNode instanceof IIdentifierNode ? ((IIdentifierNode)nameNode).getName() : "";
    }

    @Override
    public IDefinition resolveCalledExpression(ICompilerProject project)
    {
        IExpressionNode nameNode = getNameNode();
        return nameNode.resolve(project);
    }
    
    @Override
    public IExpressionNode[] getArgumentNodes()
    {
        ArrayList<IExpressionNode> retVal = new ArrayList<IExpressionNode>();
        if (argumentsNode != null)
        {
            int childCount = argumentsNode.getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                IASNode child = argumentsNode.getChild(i);
                if (child instanceof IExpressionNode)
                {
                    retVal.add((IExpressionNode)child);
                }
            }
        }
        return retVal.toArray(new IExpressionNode[0]);
    }

    @Override
    public boolean isSuperExpression()
    {
        return nameNode instanceof ILanguageIdentifierNode && ((ILanguageIdentifierNode)nameNode).getKind() == LanguageIdentifierKind.SUPER;
    }

    @Override
    public ContainerNode getArgumentsNode()
    {
        return argumentsNode;
    }

    @Override
    public KeywordNode getNewKeywordNode()
    {
        return newKeywordNode;
    }
    
    public void setNewKeywordNode(KeywordNode newNode)
    {
        newKeywordNode = newNode;
        if (newKeywordNode != null)
            newKeywordNode.setParent(this);
    }

    @Override
    public boolean isCallToSuper()
    {
        return nameNode instanceof ILanguageIdentifierNode &&
               nameNode.getAncestorOfType(ClassNode.class) != null &&
               ((ILanguageIdentifierNode)nameNode).getKind() == LanguageIdentifierKind.SUPER &&
               nameNode.getAncestorOfType(MemberAccessExpressionNode.class) == null &&
               getAncestorOfType(FunctionNode.class) != null;
    }
}
