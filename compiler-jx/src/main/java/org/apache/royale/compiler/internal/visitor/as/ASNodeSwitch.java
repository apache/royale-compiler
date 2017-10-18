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

package org.apache.royale.compiler.internal.visitor.as;

import org.apache.royale.compiler.internal.tree.as.BinaryOperatorAsNode;
import org.apache.royale.compiler.internal.tree.as.BinaryOperatorIsNode;
import org.apache.royale.compiler.internal.tree.as.ConfigConditionBlockNode;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IBlockNode;
import org.apache.royale.compiler.tree.as.ICatchNode;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefaultXMLNamespaceNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IEmbedNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IFunctionObjectNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.IIterationFlowNode;
import org.apache.royale.compiler.tree.as.IKeywordNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.as.INamespaceNode;
import org.apache.royale.compiler.tree.as.INumericLiteralNode;
import org.apache.royale.compiler.tree.as.IObjectLiteralValuePairNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IReturnNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.ISwitchNode;
import org.apache.royale.compiler.tree.as.ITerminalNode;
import org.apache.royale.compiler.tree.as.ITernaryOperatorNode;
import org.apache.royale.compiler.tree.as.IThrowNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.ITypedExpressionNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IUseNamespaceNode;
import org.apache.royale.compiler.tree.as.IVariableExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.as.IWhileLoopNode;
import org.apache.royale.compiler.tree.as.IWithNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagsNode;
import org.apache.royale.compiler.visitor.IASNodeStrategy;
import org.apache.royale.compiler.visitor.IBlockVisitor;
import org.apache.royale.compiler.visitor.as.IASBlockVisitor;

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
    public ASNodeSwitch(IBlockVisitor visitor)
    {
        this.visitor = (IASBlockVisitor) visitor;
    }

    @Override
    public void handle(IASNode node)
    {
        if (node == null)
            return;

        // TODO (mschmalle) Still working on the switch, its complication in the expressions
        switch (node.getNodeID())
        {
        case ContainerID:
            visitor.visitContainer((IContainerNode) node);
            return;

        case ConfigBlockID:
        	ConfigConditionBlockNode condcomp = (ConfigConditionBlockNode)node;
        	if (condcomp.getChildCount() > 0) // will be 0 if conditional compile variable is false
                visitor.visitBlock((IBlockNode) node);
            return;

        case E4XFilterID:
            visitor.visitE4XFilter((IMemberAccessExpressionNode) node);
            return;

        case FileID:
            visitor.visitFile((IFileNode) node);
            return;

        case PackageID:
            visitor.visitPackage((IPackageNode) node);
            return;

        case ClassID:
            visitor.visitClass((IClassNode) node);
            return;

        case InterfaceID:
            visitor.visitInterface((IInterfaceNode) node);
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
            if (node instanceof ILanguageIdentifierNode)
            {
                visitor.visitLanguageIdentifierNode((ILanguageIdentifierNode) node);
                return;
            }
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
            // XXX this should be removed
            visitor.visitLanguageIdentifierNode((ILanguageIdentifierNode) node);
            return;

        case DefaultXMLStatementID:
            visitor.visitDefaultXMLNamespace((IDefaultXMLNamespaceNode) node);
            return;

            //        case TODO:
            //            visitor.visitKeyword((IKeywordNode) node);
            //            break;
        case VariableExpressionID:
            visitor.visitVariableExpression((IVariableExpressionNode) node);
            return;
        case FunctionObjectID:
        case AnonymousFunctionID:
            visitor.visitFunctionObject((IFunctionObjectNode) node);
            return;
        
        default:
            break;
        }

        // IExpressionNode
        if (node instanceof IUseNamespaceNode)
        {
            visitor.visitUseNamespace((IUseNamespaceNode) node);
        }
        else if (node instanceof IEmbedNode)
        {
            visitor.visitEmbed((IEmbedNode) node);
        }
        else if (node instanceof IObjectLiteralValuePairNode)
        {
            visitor.visitObjectLiteralValuePair((IObjectLiteralValuePairNode) node);
        }
        else if (node instanceof NamespaceAccessExpressionNode)
        {
            visitor.visitNamespaceAccessExpression((INamespaceAccessExpressionNode) node);
        }
        else if (node instanceof IMemberAccessExpressionNode)
        {
            visitor.visitMemberAccessExpression((IMemberAccessExpressionNode) node);
        }
        else if (node instanceof IBinaryOperatorNode)
        {
            if (node instanceof BinaryOperatorAsNode)
                visitor.visitAsOperator((IBinaryOperatorNode) node);
            else if (node instanceof BinaryOperatorIsNode)
                visitor.visitIsOperator((IBinaryOperatorNode) node);
            else
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
