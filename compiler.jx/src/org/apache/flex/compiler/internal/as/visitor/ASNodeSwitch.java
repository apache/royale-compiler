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
import org.apache.flex.compiler.tree.as.IImportNode;
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
            return;
        }

        switch (node.getNodeID())
        {
        case FileID:
            visitor.visitFile((IFileNode) node);
            return;

        case PackageID:
            visitor.visitPackage((IPackageNode) node);
            return;

        case ClassID:
            visitor.visitClass((IClassNode) node);
            return;

        case GetterID:
            visitor.visitGetter((IGetterNode) node);
            return;

        case SetterID:
            visitor.visitSetter((ISetterNode) node);
            return;

        case FunctionID:
            visitor.visitFunction((IFunctionNode) node);
            return;

        case ArgumentID:
        case ArgumentRestID:
            visitor.visitParameter((IParameterNode) node);
            return;

        case VariableID:
        case BindableVariableID:
            visitor.visitVariable((IVariableNode) node);
            return;

        case NamespaceID:
            visitor.visitNamespace((INamespaceNode) node);
            return;

        case CatchID:
            visitor.visitCatch((ICatchNode) node);
            return;

        case ForEachLoopID:
        case ForLoopID:
            visitor.visitForLoop((IForLoopNode) node);
            return;

        case FinallyID:
        case DefaultID:
        case ElseID:
        case TerminalID:
            visitor.visitTerminal((ITerminalNode) node);
            return;

        case TryID:
            visitor.visitTry((ITryNode) node);
            return;

        case WithID:
            visitor.visitWith((IWithNode) node);
            return;

        case IfStatementID:
            visitor.visitIf((IIfNode) node);
            return;

        case SwitchID:
            visitor.visitSwitch((ISwitchNode) node);
            return;

        case WhileLoopID:
        case DoWhileLoopID:
            visitor.visitWhileLoop((IWhileLoopNode) node);
            return;

        case FunctionCallID:
            visitor.visitFunctionCall((IFunctionCallNode) node);
            return;

        case TypedExpressionID:
            visitor.visitTypedExpression((ITypedExpressionNode) node);
            return;

        case IdentifierID:
        case NamespaceIdentifierID:
        case NonResolvingIdentifierID:
            visitor.visitIdentifier((IIdentifierNode) node);
            return;

            //case LiteralIntegerZeroID:
        case LiteralIntegerID:
            //case LiteralIntegerZeroID:
        case LiteralUintID:
            visitor.visitNumericLiteral((INumericLiteralNode) node);
            return;

            //        case LiteralArrayID:
            //        case LiteralBooleanID:
            //        case LiteralNullID:
            //        case LiteralNumberID:
            //        case LiteralObjectID:
            //        case LiteralRegexID:
            //        case LiteralStringID:
            //        case LiteralVoidID:
            //        case LiteralXMLID:
            //        case LiteralID:
            //            visitor.visitLiteral((ILiteralNode) node);
            //            return;

            //        case MemberAccessExpressionID:
            //            visitor.visitMemberAccessExpression((IMemberAccessExpressionNode) node);
            //            return;

        case ArrayIndexExpressionID:
            visitor.visitDynamicAccess((IDynamicAccessNode) node);
            return;

            //        case NamespaceAccessExpressionID:
            //            visitor.visitNamespaceAccessExpression((NamespaceAccessExpressionNode) node);
            //            return;

            //        case TODO:
            //            visitor.visitBinaryOperator((IBinaryOperatorNode) node);
            //            break;
            //
            //        case TODO:
            //            visitor.visitUnaryOperator((IUnaryOperatorNode) node);
            //            break;

        case ReturnStatementID:
            visitor.visitReturn((IReturnNode) node);
            return;

        case ThrowsStatementID:
            visitor.visitThrow((IThrowNode) node);
            return;

        case TernaryExpressionID:
            visitor.visitTernaryOperator((ITernaryOperatorNode) node);
            return;

        case BlockID:
            visitor.visitBlock((IBlockNode) node);
            return;

        case LabledStatementID:
            visitor.visitLabeledStatement((LabeledStatementNode) node);
            return;

        case BreakID:
        case ContinueID:
        case GotoID:
            visitor.visitIterationFlow((IIterationFlowNode) node);
            return;

            //        case ObjectLiteralValuePairID:
            //            visitor.visitObjectLiteralValuePair((IObjectLiteralValuePairNode) node);
            //            return;

        case SuperID:
        case VoidID:
            visitor.visitLanguageIdentifierNode((ILanguageIdentifierNode) node);
            return;

        case DefaultXMLStatementID:
            visitor.visitDefaultXMLNamespace((IDefaultXMLNamespaceNode) node);
            return;

            //        case TODO:
            //            visitor.visitKeyword((IKeywordNode) node);
            //            break;

        default:
            break;
        }

        // IExpressionNode
        if (node instanceof IEmbedNode)
        {
            visitor.visitEmbed((IEmbedNode) node);
        }
        else if (node instanceof IObjectLiteralValuePairNode)
        {
            visitor.visitObjectLiteralValuePair((IObjectLiteralValuePairNode) node);
        }
        else if (node instanceof NamespaceAccessExpressionNode)
        {
            visitor.visitNamespaceAccessExpression((NamespaceAccessExpressionNode) node);
        }
        else if (node instanceof IMemberAccessExpressionNode)
        {
            visitor.visitMemberAccessExpression((IMemberAccessExpressionNode) node);
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
        else if (node instanceof IImportNode)
        {
            visitor.visitImport((IImportNode) node);
        }
        else if (node instanceof ILiteralNode)
        {
            visitor.visitLiteral((ILiteralNode) node);
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
