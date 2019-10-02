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

package org.apache.royale.compiler.codegen.as;

import java.io.Writer;

import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.INestingEmitter;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.royale.compiler.tree.as.*;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;
import org.apache.royale.compiler.visitor.IASNodeStrategy;
import org.apache.royale.compiler.visitor.IBlockWalker;

/**
 * The {@link IASEmitter} interface allows abstraction between the
 * {@link IASNodeStrategy} and the current output buffer {@link Writer}.
 * 
 * @author Michael Schmalle
 */
public interface IASEmitter extends INestingEmitter
{
    IBlockWalker getWalker();

    void setWalker(IBlockWalker asBlockWalker);

    IDocEmitter getDocEmitter();

    void setDocEmitter(IDocEmitter value);

    String postProcess(String output);

    void emitImport(IImportNode node);

    void emitPackageHeader(IPackageDefinition definition);

    void emitPackageHeaderContents(IPackageDefinition definition);

    void emitPackageContents(IPackageDefinition definition);

    void emitPackageFooter(IPackageDefinition definition);

    /**
     * Emit a Class.
     * 
     * @param node The {@link IClassNode} class.
     */
    void emitClass(IClassNode node);

    /**
     * Emit an Interface.
     * 
     * @param node The {@link IInterfaceNode} class.
     */
    void emitInterface(IInterfaceNode node);

    /**
     * Emit a documentation comment for a Class field or constant
     * {@link IVariableNode}.
     * 
     * @param node The {@link IVariableNode} class field member.
     */
    void emitFieldDocumentation(IVariableNode node);

    /**
     * Emit a full Class field member.
     * 
     * @param node The {@link IVariableNode} class field member.
     */
    void emitField(IVariableNode node);

    /**
     * Emit a documentation comment for a Class method {@link IFunctionNode}.
     * 
     * @param node The {@link IFunctionNode} class method member.
     */
    void emitMethodDocumentation(IFunctionNode node);

    /**
     * Emit a full Class or Interface method member.
     * 
     * @param node The {@link IFunctionNode} class method member.
     */
    void emitMethod(IFunctionNode node);

    /**
     * Emit a documentation comment for a Class method {@link IGetterNode}.
     * 
     * @param node The {@link IGetterNode} class accessor member.
     */
    void emitGetAccessorDocumentation(IGetterNode node);

    /**
     * Emit a full Class getter member.
     * 
     * @param node The {@link IVariableNode} class getter member.
     */
    void emitGetAccessor(IGetterNode node);

    /**
     * Emit a documentation comment for a Class accessor {@link IGetterNode}.
     * 
     * @param node The {@link ISetterNode} class accessor member.
     */
    void emitSetAccessorDocumentation(ISetterNode node);

    /**
     * Emit a full Class setter member.
     * 
     * @param node The {@link ISetterNode} class setter member.
     */
    void emitSetAccessor(ISetterNode node);

    void emitParameter(IParameterNode node);

    /**
     * Emit a namespace member.
     * 
     * @param node The {@link INamespaceNode} class member.
     */
    void emitNamespace(INamespaceNode node);

    //--------------------------------------------------------------------------
    // Statements
    //--------------------------------------------------------------------------

    /**
     * Emit a statement found within an {@link IBlockNode}.
     * 
     * @param node The {@link IASNode} statement.
     */
    void emitStatement(IASNode node);

    /**
     * Emit a <code>if(){}else if(){}else{}</code> statement.
     * 
     * @param node The {@link IIfNode} node.
     */
    void emitIf(IIfNode node);

    /**
     * Emit a <code>for each</code> statement.
     * 
     * @param node The {@link IForLoopNode} node.
     */
    void emitForEachLoop(IForLoopNode node);

    /**
     * Emit a <code>for</code> statement.
     * 
     * @param node The {@link IForLoopNode} node.
     */
    void emitForLoop(IForLoopNode node);

    /**
     * Emit a <code>switch(){}</code> statement.
     * 
     * @param node The {@link ISwitchNode} node.
     */
    void emitSwitch(ISwitchNode node);

    /**
     * Emit a <code>while(){}</code> statement.
     * 
     * @param node The {@link IWhileLoopNode} node.
     */
    void emitWhileLoop(IWhileLoopNode node);

    /**
     * Emit a <code>do{}while()</code> statement.
     * 
     * @param node The {@link IWhileLoopNode} node.
     */
    void emitDoLoop(IWhileLoopNode node);

    /**
     * Emit a <code>with(){}</code> statement.
     * 
     * @param node The {@link IWithNode} node.
     */
    void emitWith(IWithNode node);

    /**
     * Emit a <code>throw</code> statement.
     * 
     * @param node The {@link IThrowNode} node.
     */
    void emitThrow(IThrowNode node);

    /**
     * Emit a <code>try{}</code> statement.
     * 
     * @param node The {@link ITryNode} node.
     */
    void emitTry(ITryNode node);

    /**
     * Emit a <code>catch(){}</code> statement.
     * 
     * @param node The {@link ICatchNode} node.
     */
    void emitCatch(ICatchNode node);

    /**
     * Emit a <code>foo:{}</code> statement.
     * 
     * @param node The {@link LabeledStatementNode} node.
     */
    void emitLabelStatement(LabeledStatementNode node);

    void emitReturn(IReturnNode node);

    //--------------------------------------------------------------------------
    // Expressions
    //--------------------------------------------------------------------------

    /**
     * Emit a variable declaration found in expression statements within scoped
     * blocks.
     * 
     * @param node The {@link IVariableNode} or chain of variable nodes.
     */
    void emitVarDeclaration(IVariableNode node);

    /**
     * Emit an anonymous {@link IFunctionObjectNode}.
     * 
     * @param node The anonymous {@link IFunctionObjectNode}.
     */
    void emitFunctionObject(IFunctionObjectNode node);

    /**
     * Emit an local named function {@link IFunctionNode}.
     * 
     * @param node The local named function {@link IFunctionNode}.
     */
    void emitLocalNamedFunction(IFunctionNode node);

    /**
     * Emit a header at the start of a function block.
     * 
     * @param node The {@link IFunctionNode} node.
     */
    void emitFunctionBlockHeader(IFunctionNode node);

    /**
     * Emit a function call like <code>new Foo()</code> or <code>foo(42)</code>.
     * 
     * @param node The {@link IFunctionCallNode} node.
     */
    void emitFunctionCall(IFunctionCallNode node);
    
    void emitArguments(IContainerNode node);

    void emitIterationFlow(IIterationFlowNode node);

    void emitNamespaceAccessExpression(INamespaceAccessExpressionNode node);

    void emitMemberAccessExpression(IMemberAccessExpressionNode node);

    void emitVariableExpression(IVariableExpressionNode node);

    void emitDynamicAccess(IDynamicAccessNode node);

    void emitTypedExpression(ITypedExpressionNode node);

    void emitObjectLiteralValuePair(IObjectLiteralValuePairNode node);

    void emitIdentifier(IIdentifierNode node);

    void emitLiteral(ILiteralNode node);

    void emitLiteralContainer(ILiteralContainerNode node);

    void emitNumericLiteral(INumericLiteralNode node);

    //--------------------------------------------------------------------------
    // Operators
    //--------------------------------------------------------------------------

    void emitUnaryOperator(IUnaryOperatorNode node);

    void emitAsOperator(IBinaryOperatorNode node);

    void emitIsOperator(IBinaryOperatorNode node);

    /**
     * Emit an operator statement.
     * 
     * @param node The {@link IBinaryOperatorNode} or chain of variable nodes.
     */
    void emitBinaryOperator(IBinaryOperatorNode node);

    void emitTernaryOperator(ITernaryOperatorNode node);

    //--------------------------------------------------------------------------
    // Node
    //--------------------------------------------------------------------------

    void emitKeyword(IKeywordNode node);

    void emitLanguageIdentifier(ILanguageIdentifierNode node);

    void emitMetaTag(IMetaTagNode node);

    void emitEmbed(IEmbedNode node);
    
    void emitContainer(IContainerNode node);

    void emitE4XFilter(IMemberAccessExpressionNode node);
    
    void emitE4XDefaultNamespaceDirective(IDefaultXMLNamespaceNode node);

    void emitUseNamespace(IUseNamespaceNode node);

    void emitBlockOpen(IContainerNode node);

    void emitBlockClose(IContainerNode node);

}
