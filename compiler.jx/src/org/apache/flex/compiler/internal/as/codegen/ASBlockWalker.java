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

package org.apache.flex.compiler.internal.as.codegen;

import java.util.List;

import org.apache.flex.compiler.as.codegen.IASEmitter;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IInterfaceDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.internal.semantics.SemanticUtils;
import org.apache.flex.compiler.internal.tree.as.BaseLiteralContainerNode;
import org.apache.flex.compiler.internal.tree.as.FunctionObjectNode;
import org.apache.flex.compiler.internal.tree.as.LabeledStatementNode;
import org.apache.flex.compiler.internal.tree.as.NamespaceAccessExpressionNode;
import org.apache.flex.compiler.internal.tree.as.VariableExpressionNode;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.projects.IASProject;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IBlockNode;
import org.apache.flex.compiler.tree.as.ICatchNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IDefaultXMLNamespaceNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IEmbedNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IForLoopNode.ForLoopKind;
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
import org.apache.flex.compiler.tree.as.ILiteralNode.LiteralType;
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
import org.apache.flex.compiler.tree.as.IWhileLoopNode.WhileLoopKind;
import org.apache.flex.compiler.tree.as.IWithNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagNode;
import org.apache.flex.compiler.tree.metadata.IMetaTagsNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.visitor.IASBlockVisitor;
import org.apache.flex.compiler.visitor.IASBlockWalker;
import org.apache.flex.compiler.visitor.IASNodeStrategy;

/**
 * A base implementation of the {@link IASBlockVisitor} that will walk the
 * {@link ICompilationUnit}s AST {@link IASNode} hierarchy.
 * 
 * @author Michael Schmalle
 */
public class ASBlockWalker implements IASBlockVisitor, IASBlockWalker
{
    private IASEmitter emitter;
    
    @Override
    public IASEmitter getEmitter()
    {
        return emitter;
    }
    
    private final List<ICompilerProblem> errors;

    List<ICompilerProblem> getErrors()
    {
        return errors;
    }

    //----------------------------------
    // strategy
    //----------------------------------

    private IASNodeStrategy strategy;

    public IASNodeStrategy getStrategy()
    {
        return strategy;
    }

    public void setStrategy(IASNodeStrategy value)
    {
        strategy = value;
    }

    //----------------------------------
    // project
    //----------------------------------

    private IASProject project;

    @Override
    public IASProject getProject()
    {
        return project;
    }

    public ASBlockWalker(List<ICompilerProblem> errors, IASProject project,
            IASEmitter emitter)
    {
        this.errors = errors;
        this.project = project;
        this.emitter = emitter;
        emitter.setWalker(this);
    }

    //--------------------------------------------------------------------------
    // File level
    //--------------------------------------------------------------------------

    @Override
    public void walk(IASNode node)
    {
        getStrategy().handle(node);
    }

    @Override
    public void visitCompilationUnit(ICompilationUnit unit)
    {
        debug("visitCompilationUnit()");
        IFileNode node = null;
        try
        {
            node = (IFileNode) unit.getSyntaxTreeRequest().get().getAST();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        walk(node);
    }

    @Override
    public void visitFile(IFileNode node)
    {
        debug("visitFile()");
        IASNode pnode = node.getChild(0);
        if (pnode != null)
        {
            walk(pnode); // IPackageNode
        }
        else
        {

        }
    }

    @Override
    public void visitPackage(IPackageNode node)
    {
        debug("visitPackage()");
        IPackageDefinition definition = (IPackageDefinition) node.getDefinition();
        emitter.emitPackageHeader(definition);
        emitter.emitPackageHeaderContents(definition);
        emitter.emitPackageContents(definition);
        emitter.emitPackageFooter(definition);
    }

    //--------------------------------------------------------------------------
    // Type level
    //--------------------------------------------------------------------------

    @Override
    public void visitClass(IClassNode node)
    {
        debug("visitClass()");
        emitter.emitClass(node);
    }

    @Override
    public void visitInterface(IInterfaceNode node)
    {
        debug("visitInterface()");
        emitter.emitInterface(node);
    }

    @Override
    public void visitVariable(IVariableNode node)
    {
        debug("visitVariable()");
        if (SemanticUtils.isMemberDefinition(node.getDefinition()))
        {
            emitter.emitField(node);
        }
        else
        {
            emitter.emitVarDeclaration(node);
        }
    }

    @Override
    public void visitFunction(IFunctionNode node)
    {
        debug("visitFunction()");
        if (isMemberDefinition(node.getDefinition()))
        {
            emitter.emitMethod(node);
        }
    }

    @Override
    public void visitParameter(IParameterNode node)
    {
        debug("visitParameter()");
        emitter.emitParameter(node);
    }

    @Override
    public void visitGetter(IGetterNode node)
    {
        debug("visitGetter()");
        emitter.emitGetAccessor(node);
    }

    @Override
    public void visitSetter(ISetterNode node)
    {
        debug("visitSetter()");
        emitter.emitSetAccessor(node);
    }

    @Override
    public void visitNamespace(INamespaceNode node)
    {
        debug("visitNamespace()");
        emitter.emitNamespace(node);
    }

    @Override
    public void visitFunctionCall(IFunctionCallNode node)
    {
        debug("visitFunctionCall()");
        emitter.emitFunctionCall(node);
    }

    @Override
    public void visitBlock(IBlockNode node)
    {
        debug("visitBlock()");
        if (node.getParent().getNodeID() == ASTNodeID.FunctionID)
        {
            emitter.emitFunctionBlockHeader((IFunctionNode) node.getParent());
        }

        final int len = node.getChildCount();
        for (int i = 0; i < len; i++)
        {
            emitter.emitStatement(node.getChild(i));
        }
    }

    @Override
    public void visitIf(IIfNode node)
    {
        debug("visitIf()");
        emitter.emitIf(node);
    }

    @Override
    public void visitForLoop(IForLoopNode node)
    {
        debug("visitForLoop(" + node.getKind() + ")");
        if (node.getKind() == ForLoopKind.FOR)
            visitFor(node);
        else if (node.getKind() == ForLoopKind.FOR_EACH)
            visitForEach(node);
    }

    protected void visitForEach(IForLoopNode node)
    {
        debug("visitForEach()");
        emitter.emitForEachLoop(node);
    }

    protected void visitFor(IForLoopNode node)
    {
        debug("visitFor()");
        emitter.emitForLoop(node);
    }

    @Override
    public void visitSwitch(ISwitchNode node)
    {
        debug("visitSwitch()");
        emitter.emitSwitch(node);
    }

    @Override
    public void visitWhileLoop(IWhileLoopNode node)
    {
        debug("visitWhileLoopNode()");
        if (node.getKind() == WhileLoopKind.WHILE)
        {
            emitter.emitWhileLoop(node);
        }
        else if (node.getKind() == WhileLoopKind.DO)
        {
            emitter.emitDoLoop(node);
        }
    }

    @Override
    public void visitWith(IWithNode node)
    {
        debug("visitWith()");
        emitter.emitWith(node);
    }

    @Override
    public void visitThrow(IThrowNode node)
    {
        debug("visitThrow()");
        emitter.emitThrow(node);
    }

    @Override
    public void visitTry(ITryNode node)
    {
        debug("visitTry()");
        emitter.emitTry(node);
    }

    @Override
    public void visitCatch(ICatchNode node)
    {
        debug("visitCatch()");
        emitter.emitCatch(node);
    }

    @Override
    public void visitIterationFlow(IIterationFlowNode node)
    {
        debug("visitIterationFlow()");
        emitter.emitIterationFlow(node);
    }

    @Override
    public void visitIdentifier(IIdentifierNode node)
    {
        debug("visitIdentifier(" + node.getName() + ")");
        emitter.emitIdentifier(node);
    }

    @Override
    public void visitNumericLiteral(INumericLiteralNode node)
    {
        debug("visitNumericLiteral(" + node.getNumericValue() + ")");
        emitter.emitNumericLiteral(node);
    }

    @Override
    public void visitDefaultXMLNamespace(IDefaultXMLNamespaceNode node)
    {
        debug("visitDefaultXMLNamespace()");
        walk(node.getKeywordNode()); // default xml namespace
        walk(node.getExpressionNode()); // "http://ns.whatever.com"
    }

    @Override
    public void visitKeyword(IKeywordNode node)
    {
        debug("visitKeyword(" + node.getNodeID().getParaphrase() + ")");
        emitter.emitKeyword(node);
    }

    @Override
    public void visitLiteral(ILiteralNode node)
    {
        debug("visitLiteral(" + node.getValue() + ")");
        // TODO (mschmalle) visitLiteral()
        if (node.getLiteralType() == LiteralType.NUMBER
                || node.getLiteralType() == LiteralType.BOOLEAN
                || node.getLiteralType() == LiteralType.NULL
                || node.getLiteralType() == LiteralType.NUMBER
                || node.getLiteralType() == LiteralType.REGEXP
                || node.getLiteralType() == LiteralType.STRING
                || node.getLiteralType() == LiteralType.VOID)
        {
            emitter.emitLiteral(node);
        }
        else if (node.getLiteralType() == LiteralType.ARRAY
                || node.getLiteralType() == LiteralType.OBJECT)
        {
            BaseLiteralContainerNode anode = (BaseLiteralContainerNode) node;
            IContainerNode cnode = anode.getContentsNode();
            emitter.emitLiteralContainer(cnode);
        }
    }

    @Override
    public void visitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        debug("visitMemberAccessExpression()");
        emitter.emitMemberAccessExpression(node);
    }

    @Override
    public void visitNamespaceAccessExpression(
            NamespaceAccessExpressionNode node)
    {
        debug("visitNamespaceAccessExpression()");
        emitter.emitNamespaceAccessExpression(node);
    }

    @Override
    public void visitDynamicAccess(IDynamicAccessNode node)
    {
        debug("visitDynamicAccess()");
        emitter.emitDynamicAccess(node);
    }

    @Override
    public void visitTypedExpression(ITypedExpressionNode node)
    {
        debug("visitITypedExpression()");
        emitter.emitTypedExpression(node);
    }

    @Override
    public void visitBinaryOperator(IBinaryOperatorNode node)
    {
        debug("visitBinaryOperator(" + node.getOperator().getOperatorText()
                + ")");
        emitter.emitBinaryOperator(node);
    }

    @Override
    public void visitUnaryOperator(IUnaryOperatorNode node)
    {
        debug("visitUnaryOperator()");
        emitter.emitUnaryOperator(node);
    }

    @Override
    public void visitTerminal(ITerminalNode node)
    {
        debug("visitTerminal(" + node.getKind() + ")");
        walk(node.getStatementContentsNode());
    }

    @Override
    public void visitExpression(IExpressionNode node)
    {
        debug("visitExpression()");
        // TODO (mschmalle) I think these placements are temp, I am sure a visit method
        // should exist for FunctionObjectNode, there is no interface for it right now
        if (node instanceof VariableExpressionNode)
        {
            VariableExpressionNode v = (VariableExpressionNode) node;
            walk(v.getTargetVariable());
        }
        else if (node instanceof FunctionObjectNode)
        {
            emitter.emitFunctionObject(node);
        }
    }

    @Override
    public void visitMetaTags(IMetaTagsNode node)
    {
        debug("visitMetaTags()");
        IMetaTagNode[] tags = node.getAllTags();
        for (IMetaTagNode tag : tags)
        {
            walk(tag);
        }
    }

    @Override
    public void visitMetaTag(IMetaTagNode node)
    {
        debug("visitMetaTag(" + node.getTagName() + ")");
        // TODO (mschmalle) visitMetaTag()    
    }

    @Override
    public void visitEmbed(IEmbedNode node)
    {
        debug("visitEmbed(" + node.getAttributes()[0].getValue() + ")");
        // TODO (mschmalle) visitEmbed() 
    }

    @Override
    public void visitReturn(IReturnNode node)
    {
        debug("visitReturn()");
        emitter.emitReturn(node);
    }

    @Override
    public void visitTernaryOperator(ITernaryOperatorNode node)
    {
        debug("visitTernaryOperator()");
        emitter.emitTernaryOperator(node);
    }

    @Override
    public void visitLabeledStatement(LabeledStatementNode node)
    {
        debug("visitLabeledStatement()");
        emitter.emitLabelStatement(node);
    }

    @Override
    public void visitObjectLiteralValuePair(IObjectLiteralValuePairNode node)
    {
        debug("visitIObjectLiteralValuePair()");
        emitter.emitObjectLiteralValuePair(node);
    }

    @Override
    public void visitLanguageIdentifierNode(ILanguageIdentifierNode node)
    {
        emitter.emitLanguageIdentifier(node);
    }

    //--------------------------------------------------------------------------
    // 
    //--------------------------------------------------------------------------

    protected void debug(String message)
    {
        System.out.println(message);
    }

    //--------------------------------------------------------------------------
    //  
    //--------------------------------------------------------------------------

    private static boolean isMemberDefinition(IDefinition definition)
    {
        return definition != null
                && (definition.getParent() instanceof IClassDefinition || definition
                        .getParent() instanceof IInterfaceDefinition);
    }

}
