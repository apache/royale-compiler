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

package org.apache.royale.compiler.visitor.as;

import org.apache.royale.compiler.internal.codegen.as.ASBlockWalker;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
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
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.visitor.IASNodeStrategy;
import org.apache.royale.compiler.visitor.IBlockVisitor;

/**
 * The {@link IASBlockVisitor} interface allows an {@link IASNodeStrategy} to
 * delegate within it's {@link IASNodeStrategy#handle(IASNode)} method to this
 * API's visitor method.
 * <p>
 * <strong>Note</strong> This API is not complete.
 * 
 * @author Michael Schmalle
 * 
 * @see ASBlockWalker
 */
public interface IASBlockVisitor extends IBlockVisitor
{
    //--------------------------------------------------------------------------
    // Top level nodes
    //--------------------------------------------------------------------------

    void visitCompilationUnit(ICompilationUnit unit);

    void visitFile(IFileNode node);

    void visitPackage(IPackageNode node);

    void visitClass(IClassNode node);

    void visitInterface(IInterfaceNode node);

    //--------------------------------------------------------------------------
    // Member nodes
    //--------------------------------------------------------------------------

    // block var or field
    void visitVariable(IVariableNode node);

    void visitFunction(IFunctionNode node);

    void visitGetter(IGetterNode node);

    void visitSetter(ISetterNode node);

    void visitNamespace(INamespaceNode node);

    //--------------------------------------------------------------------------
    // Statement nodes
    //--------------------------------------------------------------------------

    void visitIf(IIfNode node);

    void visitForLoop(IForLoopNode node);

    void visitWhileLoop(IWhileLoopNode node);

    void visitTry(ITryNode node);

    void visitCatch(ICatchNode node);

    void visitSwitch(ISwitchNode node);

    void visitLabeledStatement(LabeledStatementNode node);

    void visitWith(IWithNode node);

    void visitThrow(IThrowNode node);

    //--------------------------------------------------------------------------
    // Statement helper nodes
    //--------------------------------------------------------------------------

    void visitIterationFlow(IIterationFlowNode node);

    // is a IVariableNode
    void visitParameter(IParameterNode node);

    void visitObjectLiteralValuePair(IObjectLiteralValuePairNode node);

    //--------------------------------------------------------------------------
    // Expression Statement nodes
    //--------------------------------------------------------------------------

    void visitMemberAccessExpression(IMemberAccessExpressionNode node);

    void visitNamespaceAccessExpression(INamespaceAccessExpressionNode node);
    
    void visitVariableExpression(IVariableExpressionNode node);

    void visitLanguageIdentifierNode(ILanguageIdentifierNode node);

    void visitReturn(IReturnNode node);

    void visitDefaultXMLNamespace(IDefaultXMLNamespaceNode node);

    void visitTypedExpression(ITypedExpressionNode node);

    // this is a IBinaryOperatorNode goes before
    void visitDynamicAccess(IDynamicAccessNode node);

    void visitTernaryOperator(ITernaryOperatorNode node);

    //--------------------------------------------------------------------------
    // Container nodes
    //--------------------------------------------------------------------------

    void visitBlock(IBlockNode node);

    //--------------------------------------------------------------------------
    // Expression nodes
    //--------------------------------------------------------------------------

    void visitFunctionObject(IFunctionObjectNode node);
    
    void visitFunctionCall(IFunctionCallNode node);

    void visitAsOperator(IBinaryOperatorNode node);

    void visitIsOperator(IBinaryOperatorNode node);

    void visitBinaryOperator(IBinaryOperatorNode node);

    void visitUnaryOperator(IUnaryOperatorNode node);

    void visitExpression(IExpressionNode node);

    //--------------------------------------------------------------------------
    // Terminal like Expression nodes
    //--------------------------------------------------------------------------

    void visitIdentifier(IIdentifierNode node);

    void visitKeyword(IKeywordNode node);

    void visitLiteral(ILiteralNode node);

    void visitNumericLiteral(INumericLiteralNode node);

    void visitTerminal(ITerminalNode node);

    //--------------------------------------------------------------------------
    // Various nodes
    //--------------------------------------------------------------------------

    void visitImport(IImportNode node);

    void visitMetaTags(IMetaTagsNode node);

    void visitMetaTag(IMetaTagNode node);

    void visitUseNamespace(IUseNamespaceNode node);

    void visitEmbed(IEmbedNode node);

    void visitContainer(IContainerNode node);

    void visitE4XFilter(IMemberAccessExpressionNode node);

}
