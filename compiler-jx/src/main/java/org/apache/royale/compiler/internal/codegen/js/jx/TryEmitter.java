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

package org.apache.royale.compiler.internal.codegen.js.jx;

import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.JSSubEmitter;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.CatchScope;
import org.apache.royale.compiler.internal.semantics.PostProcessStep;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.*;

import java.util.Collection;
import java.util.EnumSet;

public class TryEmitter extends JSSubEmitter implements
        ISubEmitter<ITryNode>
{
    public TryEmitter(IJSEmitter emitter)
    {
        super(emitter);
    }

    @Override
    public void emit(ITryNode node)
    {
        startMapping(node);
        writeToken(ASEmitterTokens.TRY);
        endMapping(node);
        getWalker().walk(node.getStatementContentsNode());
        if (node.getCatchNodeCount() >= 1) {
            if (node.getCatchNodeCount() == 1) {
                getWalker().walk(node.getCatchNode(0));
            } else {
                getWalker().walk(codeGenMultipleCatchSupport(node));
            }
        }

        ITerminalNode fnode = node.getFinallyNode();
        if (fnode != null)
        {
            startMapping(fnode);
            write(ASEmitterTokens.SPACE);
            writeToken(ASEmitterTokens.FINALLY);
            endMapping(fnode);
            getWalker().walk(fnode);
        }
    }


    //Everything below here is specific to Multi-Catch support
    //--------------------------------------------------------
    public static final String ROYALE_MULTI_CATCH_ERROR_NAME = "$$royaleMultiCatchErr";

    private final EnumSet<PostProcessStep> postProcess = EnumSet.of(
            PostProcessStep.POPULATE_SCOPE);

    /**
     * Create a new implementation of the multiple catch sequence to support the typed nature of the original
     * implementation in the untyped JS runtime. Multiple catches of type catch (e:ErrorType) are expressed as
     * if (caughtErr is ErrorType) { var e:ErrorType = caughtErr; ... original catch contents } else... other catch clauses else... throw caughtError
     * @param node The original Try node to generate JS multi-catch support for
     * @return a new Catch node that has the internally generated alternate implementation for multi-catch
     */
    private ICatchNode codeGenMultipleCatchSupport(ITryNode node) {
        int catchCount = node.getCatchNodeCount();
        boolean hasCatchAll = false;
        IdentifierNode royaleErr = new IdentifierNode(ROYALE_MULTI_CATCH_ERROR_NAME);
        IdentifierNode ErrorClassIdentifier = new IdentifierNode("Error");
        ParameterNode argumentNode = new ParameterNode(royaleErr, ErrorClassIdentifier);
        argumentNode.addChild(royaleErr);
        argumentNode.addChild(royaleErr);
        CatchNode wrapper = new CatchNode(argumentNode);
        wrapper.setParent((NodeBase) node);
        argumentNode.setParent(wrapper);
        BlockNode wrapperBlock = wrapper.getContentsNode();
        wrapperBlock.setContainerType(IContainerNode.ContainerType.BRACES);
        wrapperBlock.setParent(wrapper);
        wrapper.setSourcePath(node.getSourcePath());
        wrapper.setLine(node.getEndLine());
        wrapper.setEndLine(node.getEndLine());
        wrapper.setColumn(node.getEndColumn()+1);
        wrapper.setEndColumn(node.getEndColumn()+1);
        IfNode ifNode = new IfNode(null);

        for (int i = 0; i < catchCount; i++)
        {
            BaseStatementNode rewrittenCatch;
            ICatchNode childCatch = node.getCatchNode(i);
            int childChildren = childCatch.getStatementContentsNode().getChildCount();
            ParameterNode catchParam = (ParameterNode)childCatch.getCatchParameterNode();

            if (catchParam.getTypeNode().resolve(getProject()).equals(getProject().getBuiltinType(IASLanguageConstants.BuiltinType.ANY_TYPE))) {
                hasCatchAll = true;
                //if we are at first catch node when we encounter this, just short-circuit and return it, 'nothing else matters'
                if (i == 0) {
                    return childCatch;
                }
            }

            if (hasCatchAll) {
                TerminalNode terminalNode = createTerminalCatchAllNode(catchParam, royaleErr, (CatchNode) childCatch);
                ifNode.addBranch(terminalNode);
                rewrittenCatch = terminalNode;

            } else {
                ConditionalNode conditionalNode = createConditionalNode(catchParam, royaleErr, (CatchNode) childCatch);
                ifNode.addBranch(conditionalNode);

                conditionalNode.setSourceLocation(childCatch);
                rewrittenCatch = conditionalNode;
            }

            for (int j = 0; j < childChildren; j++) {
                rewrittenCatch.getContentsNode().addChild((NodeBase) childCatch.getStatementContentsNode().getChild(j));
            }
            if (hasCatchAll) {
                //no point continuing, any following catch clauses will never execute, 'nothing else matters'
                break;
            }
        }
        if (!hasCatchAll) { //then we need to re-throw the original top-level error, as an 'uncaught' error
            TerminalNode terminalNode = createTerminalThrowNode(royaleErr);
            ifNode.addBranch(terminalNode);
        }
        wrapperBlock.addChild(ifNode);
        wrapper.runPostProcess(postProcess,((NodeBase) node).getASScope());
        return wrapper;
    }

    private void addVarStartToRewrittenCatch(ParameterNode parameterNode,  IdentifierNode assignedValue, BaseStatementNode parentNode, PseudoCatchBlock content, CatchNode originalCatch) {
        content.setParent(parentNode);
        content.setContainerType(IContainerNode.ContainerType.BRACES);
        content.setSourceLocation(originalCatch.getContentsNode());
        IdentifierNode name = new IdentifierNode(parameterNode.getName());
        name.setSourceLocation(parameterNode.getNameExpressionNode());
        ExpressionNodeBase type = parameterNode.getTypeNode().copyForInitializer(originalCatch);
        
        VariableNode varNode = new ReplacementCatchParam(name,type);
        varNode.setAssignedValue(null, assignedValue);

        content.addChild(varNode);
    }

    private ConditionalNode createConditionalNode(ParameterNode parameterNode,IdentifierNode hoistedError, CatchNode originalCatch) {
        // [else] if (hoistedError is parameterNodeClass) {
        // var parameterNodeName = $$royaleMultiCatchErr;
        // (followed by)...original catch clause contents
        // }
        PseudoCatchParam conditionalNode = new PseudoCatchParam((CatchScope) originalCatch.getScope());
        BinaryOperatorNodeBase check = getEmitter().getGeneratedTypeCheck(hoistedError, parameterNode.getTypeNode());
        check.setSourceLocation(parameterNode);
        conditionalNode.setConditionalExpression(check);
        check.setParent(conditionalNode);

        addVarStartToRewrittenCatch(parameterNode, hoistedError, conditionalNode, (PseudoCatchBlock)conditionalNode.getContentsNode(), originalCatch );
        return conditionalNode;
    }


    private TerminalNode createTerminalThrowNode(IdentifierNode hoistedError) {
        // else { throw hoistedError; }
        TerminalNode terminalNode = new TerminalNode(getElseToken());
        //throw hoistedError (from ROYALE_MULTI_CATCH_ERROR_NAME)
        ThrowNode throwNode = new ThrowNode(null);
        //clone the Identifier to avoid issues with parenting chains
        IdentifierNode localCopy = new IdentifierNode(hoistedError.getName());
        localCopy.setSourceLocation(hoistedError);
        throwNode.setStatementExpression(localCopy);
        BlockNode contents = terminalNode.getContentsNode();
        contents.setContainerType(IContainerNode.ContainerType.BRACES);
        contents.setParent(terminalNode);
        contents.addChild(throwNode);
        return terminalNode;
    }

    private TerminalNode createTerminalCatchAllNode(ParameterNode parameterNode,IdentifierNode hoistedError, CatchNode originalCatch) {
        //  else {
        //  var parameterNodeName = $$royaleMultiCatchErr;
        //  (followed by)...original catch clause contents
        // }
        PseudoCatchAllParam terminalNode = new PseudoCatchAllParam((CatchScope) originalCatch.getScope());

        addVarStartToRewrittenCatch(parameterNode, hoistedError, terminalNode, (PseudoCatchBlock)terminalNode.getContentsNode(), originalCatch );

        terminalNode.setSourceLocation(originalCatch);
        return terminalNode;
    }

    static ASToken getElseToken(){
        return new ASToken(ASTokenTypes.TOKEN_KEYWORD_ELSE, -1,-1, -1, -1, "else");
    }
}


/**
 * The following mainly exists because the original catch scope allows for multiple 'same name' catch parameter definitions,
 * when considered from within the containing scope of the catch clauses.
 * Those names would otherwise clash if they were hoisted to the containing scope.
 * This serves to simulate the same thing for the rewritten catch parameter definitions.
 */
class PseudoCatchBlock extends BlockNode implements IScopedNode {

    protected ASScope scope;

    public PseudoCatchBlock(CatchScope catchScope){
        super();
        scope = catchScope;

    }

    public ASScope getASScope(){
        return scope;
    }

    @Override
    public IASScope getScope(){
        return scope;
    }

    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE) ||
                set.contains(PostProcessStep.RECONNECT_DEFINITIONS))
        {
            if (this.scope == null) {
                this.scope = new CatchScope(scope);
            }
            this.scope.setContainingScope(scope);
        }

        super.analyze(set, this.scope, problems);
    }

    @Override
    public void getAllImports(Collection<String> imports)
    {
        getContainingScope().getAllImports(imports);
    }

    @Override
    public void getAllImportNodes(Collection<IImportNode> imports)
    {
        getContainingScope().getAllImportNodes(imports);
    }
}

class PseudoCatchParam extends ConditionalNode{
    public PseudoCatchParam(CatchScope scope){
        super(null);
        this.contentsNode = new PseudoCatchBlock(scope);
    }
}

class PseudoCatchAllParam extends TerminalNode{
    public PseudoCatchAllParam(CatchScope scope){
        super(TryEmitter.getElseToken());
        this.contentsNode = new PseudoCatchBlock(scope);
    }
}

class ReplacementCatchParam extends VariableNode{


    public ReplacementCatchParam(IdentifierNode nameNode, ExpressionNodeBase typeNode) {
        super(nameNode, typeNode);
    }


    @Override
    protected void analyze(EnumSet<PostProcessStep> set, ASScope scope, Collection<ICompilerProblem> problems)
    {
        if (set.contains(PostProcessStep.POPULATE_SCOPE))
        {

            String definitionName = ((IdentifierNode) nameNode).getName();

            VariableDefinition definition =
                    new VariableDefinition(definitionName);

            fillinDefinition(definition);

            definition.setDeclaredInControlFlow(true);

            definition.setInitializer(this.getAssignedValueNode());

            setDefinition(definition);
            ((CatchScope) scope).displaceParameter(definition);
            //don't run the super's POPULATE_SCOPE:
            set = set.clone();
            set.remove(PostProcessStep.POPULATE_SCOPE);
        }

        super.analyze(set, scope, problems);
    }
}
