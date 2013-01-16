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

package org.apache.flex.compiler.internal.as.visitor;

import org.apache.flex.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.flex.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IBlockNode;
import org.apache.flex.compiler.tree.as.ICatchNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefaultXMLNamespaceNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IEmbedNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IIfNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.IIterationFlowNode;
import org.apache.flex.compiler.tree.as.IKeywordNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.INamespaceNode;
import org.apache.flex.compiler.tree.as.INumericLiteralNode;
import org.apache.flex.compiler.tree.as.IObjectLiteralValuePairNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IReturnNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ISwitchNode;
import org.apache.flex.compiler.tree.as.ITerminalNode;
import org.apache.flex.compiler.tree.as.ITernaryOperatorNode;
import org.apache.flex.compiler.tree.as.IThrowNode;
import org.apache.flex.compiler.tree.as.ITryNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.as.IWhileLoopNode;
import org.apache.flex.compiler.tree.as.IWithNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagsNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IASBlockVisitor;
import org.apache.flex.compiler.visitor.IASNodeStrategy;

/**
 * The {@link ASNodeSwitch} class is an {@link IASNodeStrategy} implementation
 * that handles {@link IASNode} types based on the node interface type.
 * <p>
 * All traversable {@link ASTNodeID} node visitor methods are found within the
 * class {@link #handle(IASNode)} method's if else statements.
 * 
 * @author Michael Schmalle
 */
public class ASNodeSwitch implements IASNodeStrategy
{
    private IASBlockVisitor visitor;

    /**
     * Creates a new node switch using the {@link #visitor} to handle the
     * {@link IASNode} in the current traverse.
     * 
     * @param visitor The {@link IASBlockVisitor} strategy that will visit an
     * {@link IASNode} based on it's type.
     */
    public ASNodeSwitch(IASBlockVisitor visitor)
    {
        this.visitor = visitor;
    }

    @Override
    public void handle(IASNode node)
    {
        if (node instanceof ICompilationUnit)
        {
            visitor.visitCompilationUnit((ICompilationUnit) node);
        }
        else if (node instanceof IFileNode)
        {
            visitor.visitFile((IFileNode) node);
        }
        else if (node instanceof IPackageNode)
        {
            visitor.visitPackage((IPackageNode) node);
        }

        // ITypeNode
        else if (node instanceof IClassNode)
        {
            visitor.visitClass((IClassNode) node);
        }
        else if (node instanceof IInterfaceNode)
        {
            visitor.visitInterface((IInterfaceNode) node);
        }

        // IScopedDefinitionNode
        else if (node instanceof IGetterNode)
        {
            visitor.visitGetter((IGetterNode) node);
        }
        else if (node instanceof ISetterNode)
        {
            visitor.visitSetter((ISetterNode) node);
        }
        else if (node instanceof IFunctionNode)
        {
            visitor.visitFunction((IFunctionNode) node);
        }

        // IVariableNode
        else if (node instanceof IParameterNode)
        {
            visitor.visitParameter((IParameterNode) node);
        }
        else if (node instanceof IVariableNode)
        {
            visitor.visitVariable((IVariableNode) node);
        }
        else if (node instanceof INamespaceNode)
        {
            visitor.visitNamespace((INamespaceNode) node);
        }

        // IStatementNode
        else if (node instanceof ICatchNode)
        {
            visitor.visitCatch((ICatchNode) node);
        }
        else if (node instanceof IForLoopNode)
        {
            visitor.visitForLoop((IForLoopNode) node);
        }
        else if (node instanceof ITerminalNode)
        {
            visitor.visitTerminal((ITerminalNode) node);
        }
        else if (node instanceof ITryNode)
        {
            visitor.visitTry((ITryNode) node);
        }
        else if (node instanceof IWithNode)
        {
            visitor.visitWith((IWithNode) node);
        }

        // IConditionalNode after statements
        //  > IConditionalNode > IStatementNode
        else if (node instanceof IIfNode)
        {
            visitor.visitIf((IIfNode) node);
        }
        else if (node instanceof ISwitchNode)
        {
            visitor.visitSwitch((ISwitchNode) node);
        }
        else if (node instanceof IWhileLoopNode)
        {
            visitor.visitWhileLoop((IWhileLoopNode) node);
        }
        
        // IExpressionNode
        else if (node instanceof IEmbedNode)
        {
            visitor.visitEmbed((IEmbedNode) node);
        }
        else if (node instanceof IFunctionCallNode)
        {
            visitor.visitFunctionCall((IFunctionCallNode) node);
        }
        else if (node instanceof ITypedExpressionNode)
        {
            visitor.visitTypedExpression((ITypedExpressionNode) node);
        }
        else if (node instanceof IIdentifierNode)
        {
            visitor.visitIdentifier((IIdentifierNode) node);
        }

        // ILiteralNode > IExpressionNode
        else if (node instanceof INumericLiteralNode)
        {
            visitor.visitNumericLiteral((INumericLiteralNode) node);
        }
        else if (node instanceof ILiteralNode)
        {
            visitor.visitLiteral((ILiteralNode) node);
        }

        // IBinaryOperatorNode > IOperator
        else if (node instanceof IMemberAccessExpressionNode)
        {
            visitor.visitMemberAccessExpression((IMemberAccessExpressionNode) node);
        }
        else if (node instanceof IDynamicAccessNode)
        {
            visitor.visitDynamicAccess((IDynamicAccessNode) node);
        }
        else if (node instanceof NamespaceAccessExpressionNode)
        {
            visitor.visitNamespaceAccessExpression((NamespaceAccessExpressionNode) node);
        }
        else if (node instanceof IBinaryOperatorNode)
        {
            visitor.visitBinaryOperator((IBinaryOperatorNode) node);
        }

        // IUnaryOperatorNode > IOperator
        else if (node instanceof IUnaryOperatorNode)
        {
            visitor.visitUnaryOperator((IUnaryOperatorNode) node);
        }

        else if (node instanceof IReturnNode)
        {
            visitor.visitReturn((IReturnNode) node);
        }
        else if (node instanceof IThrowNode)
        {
            visitor.visitThrow((IThrowNode) node);
        }
        else if (node instanceof ITernaryOperatorNode)
        {
            visitor.visitTernaryOperator((ITernaryOperatorNode) node);
        }

        // Container
        else if (node instanceof IBlockNode)
        {
            visitor.visitBlock((IBlockNode) node);
        }

        // TODO (mschmalle) Organize leaf

        else if (node instanceof LabeledStatementNode)
        {
            visitor.visitLabeledStatement((LabeledStatementNode) node);
        }
        else if (node instanceof IIterationFlowNode)
        {
            visitor.visitIterationFlow((IIterationFlowNode) node);
        }
        else if (node instanceof IObjectLiteralValuePairNode)
        {
            visitor.visitObjectLiteralValuePair((IObjectLiteralValuePairNode) node);
        }
        else if (node instanceof ILanguageIdentifierNode)
        {
            visitor.visitLanguageIdentifierNode((ILanguageIdentifierNode) node);
        }
        else if (node instanceof IDefaultXMLNamespaceNode)
        {
            visitor.visitDefaultXMLNamespace((IDefaultXMLNamespaceNode) node);
        }
        else if (node instanceof IKeywordNode)
        {
            visitor.visitKeyword((IKeywordNode) node);
        }
        else if (node instanceof IMetaTagsNode)
        {
            visitor.visitMetaTags((IMetaTagsNode) node);
        }
        else if (node instanceof IMetaTagNode)
        {
            visitor.visitMetaTag((IMetaTagNode) node);
        }

        else if (node instanceof IExpressionNode)
        {
            visitor.visitExpression((IExpressionNode) node);
        }
        else
        {
            throw new RuntimeException("handle() not found "
                    + node.getClass().getName());
        }
    }
}
