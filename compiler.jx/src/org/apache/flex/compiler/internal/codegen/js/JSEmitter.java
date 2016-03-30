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

package org.apache.flex.compiler.internal.codegen.js;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.flex.compiler.clients.JSConfiguration;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitter;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IConditionalNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IFunctionObjectNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.IIterationFlowNode;
import org.apache.flex.compiler.tree.as.IKeywordNode;
import org.apache.flex.compiler.tree.as.ILiteralContainerNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.INumericLiteralNode;
import org.apache.flex.compiler.tree.as.IObjectLiteralValuePairNode;
import org.apache.flex.compiler.tree.as.IOperatorNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IReturnNode;
import org.apache.flex.compiler.tree.as.ITerminalNode;
import org.apache.flex.compiler.tree.as.ITernaryOperatorNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.tree.as.IWhileLoopNode;
import org.apache.flex.compiler.utils.ASNodeUtils;
import org.apache.flex.compiler.visitor.IBlockWalker;

import com.google.debugging.sourcemap.FilePosition;

/**
 * @author Michael Schmalle
 */
public class JSEmitter extends ASEmitter implements IJSEmitter
{
    private JSSessionModel model;
    
    @Override
    public JSSessionModel getModel()
    {
        return model;
    }
    
    private SourceMapMapping lastMapping;
    
    private Stack<String> nameStack = new Stack<String>();
    
    private List<SourceMapMapping> sourceMapMappings;
    
    public List<SourceMapMapping> getSourceMapMappings()
    {
        return sourceMapMappings;
    }

    public JSEmitter(FilterWriter out)
    {
        super(out);
        
        model = new JSSessionModel();
        sourceMapMappings = new ArrayList<SourceMapMapping>();
    }

    @Override
    public String formatQualifiedName(String name)
    {
        return name;
    }
    
    @Override
    public void emitLocalNamedFunction(IFunctionNode node)
    {
        startMapping(node);
        FunctionNode fnode = (FunctionNode)node;
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.SPACE);
        write(fnode.getName());
        endMapping(node);
        emitParameters(fnode.getParameterNodes());
        emitFunctionScope(fnode.getScopedNode());
    }
    
    @Override
    public void emitFunctionObject(IFunctionObjectNode node)
    {
        startMapping(node);
        FunctionNode fnode = node.getFunctionNode();
        write(ASEmitterTokens.FUNCTION);
        endMapping(node);
        emitParameters(fnode.getParameterNodes());
        emitFunctionScope(fnode.getScopedNode());
    }

    public void emitClosureStart()
    {
    	
    }

    public void emitClosureEnd(IASNode node)
    {
    	
    }
    
    public void emitSourceMapDirective(ITypeNode node)
    {
        boolean sourceMap = false;
        
        IBlockWalker walker = getWalker();
        FlexJSProject project = (FlexJSProject) walker.getProject();
        if (project != null)
        {
            JSConfiguration config = project.config;
            if (config != null)
            {
                sourceMap = config.getSourceMap();
            }
        }
        
        if (sourceMap)
        {
            writeNewline();
            write("//# sourceMappingURL=./" + node.getName() + ".js.map");
        }
    }

    public void emitParameters(IParameterNode[] nodes)
    {
        write(ASEmitterTokens.PAREN_OPEN);
        int len = nodes.length;
        for (int i = 0; i < len; i++)
        {
            IParameterNode node = nodes[i];
            getWalker().walk(node); //emitParameter
            if (i < len - 1)
            {
                writeToken(ASEmitterTokens.COMMA);
            }
        }
        write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        startMapping(node);
        super.emitParameter(node);
        endMapping(node);
    }

    @Override
    public void emitNumericLiteral(INumericLiteralNode node)
    {
        startMapping((ISourceLocation) node);
        super.emitNumericLiteral(node);
        endMapping((ISourceLocation) node);
    }

    @Override
    public void emitLiteralContainer(ILiteralContainerNode node)
    {
        final IContainerNode cnode = node.getContentsNode();
        final IContainerNode.ContainerType type = cnode.getContainerType();
        String preFix = null;
        String postFix = null;

        if (type == IContainerNode.ContainerType.BRACES)
        {
            preFix = ASEmitterTokens.BLOCK_OPEN.getToken();
            postFix = ASEmitterTokens.BLOCK_CLOSE.getToken();
        }
        else if (type == IContainerNode.ContainerType.BRACKETS)
        {
            preFix = ASEmitterTokens.SQUARE_OPEN.getToken();
            postFix = ASEmitterTokens.SQUARE_CLOSE.getToken();
        }
        else if (type == IContainerNode.ContainerType.IMPLICIT)
        {
            // nothing to write, move along
        }
        else if (type == IContainerNode.ContainerType.PARENTHESIS)
        {
            preFix = ASEmitterTokens.PAREN_OPEN.getToken();
            postFix = ASEmitterTokens.PAREN_CLOSE.getToken();
        }

        if (preFix != null)
        {
            startMapping(node);
            write(preFix);
            endMapping(node);
        }

        final int len = cnode.getChildCount();
        for (int i = 0; i < len; i++)
        {
            IASNode child = cnode.getChild(i);
            getWalker().walk(child);
            if (i < len - 1)
            {
                //we're mapping the comma to the literal container, but we use
                //the child line/column in case the comma is not on the same
                //line as the opening { or [
                startMapping(node, child.getLine(), child.getColumn() + child.getAbsoluteEnd() - child.getAbsoluteStart());
                writeToken(ASEmitterTokens.COMMA);
                endMapping(node);
            }
        }

        if (postFix != null)
        {
            startMapping(node, node.getAbsoluteEnd() - node.getAbsoluteStart() - 1);
            write(postFix);
            endMapping(node);
        }
    }

    @Override
    public void emitObjectLiteralValuePair(IObjectLiteralValuePairNode node)
    {
        ISourceLocation sourceLocationNode = (ISourceLocation) node;
        
        IExpressionNode nameNode = node.getNameNode();
        if (!(nameNode instanceof ILiteralNode))
        {
            startMapping(nameNode);
        }
        getWalker().walk(node.getNameNode());
        if (!(nameNode instanceof ILiteralNode))
        {
            endMapping(nameNode);
        }
        
        startMapping(sourceLocationNode, nameNode.getAbsoluteEnd() - sourceLocationNode.getAbsoluteStart());
        write(ASEmitterTokens.COLON);
        endMapping(sourceLocationNode);
        
        getWalker().walk(node.getValueNode());
    }

    @Override
    public void emitReturn(IReturnNode node)
    {
        IExpressionNode rnode = node.getReturnValueNode();
        boolean hasReturnValue = rnode != null && rnode.getNodeID() != ASTNodeID.NilID;
        
        startMapping(node);
        write(ASEmitterTokens.RETURN);
        if (hasReturnValue)
        {
            write(ASEmitterTokens.SPACE);
        }
        endMapping(node);
        
        if (hasReturnValue)
        {
            getWalker().walk(rnode);
        }
    }

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSEmitterTokens.ARRAY);
    }

    @Override
    public void emitMemberKeyword(IDefinitionNode node)
    {
        IKeywordNode keywordNode = null;
        for(int i = 0; i < node.getChildCount(); i++)
        {
            IASNode childNode = node.getChild(i);
            if (childNode instanceof IKeywordNode)
            {
                keywordNode = (IKeywordNode) childNode;
                break; 
            }
        }
        if (keywordNode != null)
        {
            startMapping(keywordNode);
        }
        if (node instanceof IFunctionNode)
        {
            writeToken(ASEmitterTokens.FUNCTION);
        }
        else if (node instanceof IVariableNode)
        {
            writeToken(ASEmitterTokens.VAR);
        }
        if (keywordNode != null)
        {
            endMapping(keywordNode);
        }
    }

    @Override
    public void emitConditional(IConditionalNode node, boolean isElseIf)
    {
        startMapping(node);
        if (isElseIf)
        {
            writeToken(ASEmitterTokens.ELSE);
        }
        writeToken(ASEmitterTokens.IF);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);
        
        IASNode conditionalExpression = node.getChild(0);
        getWalker().walk(conditionalExpression);
        
        startMapping(node, conditionalExpression.getAbsoluteEnd() - node.getAbsoluteStart());
        write(ASEmitterTokens.PAREN_CLOSE);
        IContainerNode xnode = (IContainerNode) node.getStatementContentsNode();
        if (!isImplicit(xnode))
            write(ASEmitterTokens.SPACE);
        endMapping(node);

        getWalker().walk(node.getChild(1)); // BlockNode
    }

    @Override
    public void emitElse(ITerminalNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(0);
        
        // if an implicit if, add a newline with no space
        final boolean isImplicit = isImplicit(cnode);
        if (isImplicit)
            writeNewline();
        else
            write(ASEmitterTokens.SPACE);
        
        startMapping(node);
        write(ASEmitterTokens.ELSE);
        if (!isImplicit)
            write(ASEmitterTokens.SPACE);
        endMapping(node);

        getWalker().walk(node); // TerminalNode
    }

    @Override
    public void emitTernaryOperator(ITernaryOperatorNode node)
    {
        if (ASNodeUtils.hasParenOpen((IOperatorNode) node))
            write(ASEmitterTokens.PAREN_OPEN);
        
        IExpressionNode conditionalNode = node.getConditionalNode();
        getWalker().walk(conditionalNode);
        
        startMapping(node, conditionalNode.getAbsoluteEnd() - node.getAbsoluteStart());
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.TERNARY);
        endMapping(node);
        
        IExpressionNode leftOperandNode = node.getLeftOperandNode();
        getWalker().walk(leftOperandNode);

        startMapping(node, leftOperandNode.getAbsoluteEnd() - node.getAbsoluteStart());
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.COLON);
        endMapping(node);
        
        getWalker().walk(node.getRightOperandNode());
        
        if (ASNodeUtils.hasParenClose((IOperatorNode) node))
            write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitWhileLoop(IWhileLoopNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(1);
        
        startMapping(node);
        writeToken(ASEmitterTokens.WHILE);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);

        IASNode conditionalExpression = node.getConditionalExpressionNode();
        getWalker().walk(conditionalExpression);
        
        startMapping(node, conditionalExpression.getAbsoluteEnd() - node.getAbsoluteStart());
        write(ASEmitterTokens.PAREN_CLOSE);
        if (!isImplicit(cnode))
            write(ASEmitterTokens.SPACE);
        endMapping(node);
        
        getWalker().walk(node.getStatementContentsNode());
    }

    @Override
    public void emitDoLoop(IWhileLoopNode node)
    {
        IContainerNode cnode = (IContainerNode) node.getChild(0);
        
        startMapping(node);
        write(ASEmitterTokens.DO);
        if (!isImplicit(cnode))
            write(ASEmitterTokens.SPACE);
        endMapping(node);

        IASNode statementContents = node.getStatementContentsNode();
        getWalker().walk(statementContents);
        
        startMapping(node, statementContents.getAbsoluteEnd() - statementContents.getAbsoluteStart());
        if (!isImplicit(cnode))
            write(ASEmitterTokens.SPACE);
        else
            writeNewline(); // TODO (mschmalle) there is something wrong here, block should NL
        write(ASEmitterTokens.WHILE);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);

        IASNode conditionalExpression = node.getConditionalExpressionNode();
        getWalker().walk(conditionalExpression);
        
        startMapping(node, conditionalExpression.getAbsoluteEnd() - node.getAbsoluteStart());
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
        endMapping(node);
    }

    @Override
    public void emitPreUnaryOperator(IUnaryOperatorNode node)
    {
        startMapping(node);
        write(node.getOperator().getOperatorText());
        IExpressionNode opNode = node.getOperandNode();
        endMapping(node);
        getWalker().walk(opNode);
    }

    @Override
    public void emitPostUnaryOperator(IUnaryOperatorNode node)
    {
        IExpressionNode operandNode = node.getOperandNode();
        getWalker().walk(operandNode);
        startMapping(node, operandNode.getAbsoluteEnd() - operandNode.getAbsoluteStart());
        write(node.getOperator().getOperatorText());
        endMapping(node);
    }

    @Override
    public void emitDeleteOperator(IUnaryOperatorNode node)
    {
        startMapping(node);
        writeToken(node.getOperator().getOperatorText());
        endMapping(node);
        getWalker().walk(node.getOperandNode());
    }

    @Override
    public void emitVoidOperator(IUnaryOperatorNode node)
    {
        startMapping(node);
        writeToken(node.getOperator().getOperatorText());
        endMapping(node);
        getWalker().walk(node.getOperandNode());
    }

    @Override
    public void emitTypeOfOperator(IUnaryOperatorNode node)
    {
        startMapping(node);
        write(node.getOperator().getOperatorText());
        write(ASEmitterTokens.PAREN_OPEN);
        endMapping(node);
        IExpressionNode operandNode = node.getOperandNode();
        getWalker().walk(operandNode);
        startMapping(node);
        write(ASEmitterTokens.PAREN_CLOSE);
        endMapping(node);
    }

    @Override
    public void emitIterationFlow(IIterationFlowNode node)
    {
        startMapping(node);
        write(node.getKind().toString().toLowerCase());
        IIdentifierNode lnode = node.getLabelNode();
        if (lnode != null)
        {
            write(ASEmitterTokens.SPACE);
            endMapping(node);
            getWalker().walk(lnode);
        }
        else
        {
            endMapping(node);
        }
    }

    public void pushSourceMapName(ISourceLocation node)
    {
        boolean isValidMappingScope = node instanceof ITypeNode
                || node instanceof IPackageNode
                || node instanceof IFunctionNode;
        if(!isValidMappingScope)
        {
            throw new IllegalStateException("A source mapping scope must be a package, type, or function.");
        }
        
        IDefinitionNode definitionNode = (IDefinitionNode) node;
        String nodeName = definitionNode.getQualifiedName();
        ITypeDefinition typeDef = EmitterUtils.getTypeDefinition(definitionNode);
        if (typeDef != null)
        {
            boolean isConstructor = node instanceof IFunctionNode &&
                    ((IFunctionNode) node).isConstructor();
            boolean isStatic = definitionNode.hasModifier(ASModifier.STATIC);
            if (isConstructor)
            {
                nodeName = typeDef.getQualifiedName() + ".constructor";
            }
            else if (isStatic)
            {
                nodeName = typeDef.getQualifiedName() + "." + nodeName;
            }
            else
            {
                nodeName = typeDef.getQualifiedName() + ".prototype." + nodeName;
            }
        }
        nameStack.push(nodeName);
    }
    
    public void popSourceMapName()
    {
        nameStack.pop();
    }

    public void startMapping(ISourceLocation node)
    {
        startMapping(node, node.getLine(), node.getColumn());
    }

    public void startMapping(ISourceLocation node, int startOffset)
    {
        startMapping(node, node.getLine(), node.getColumn() + startOffset);
    }
    
    public void startMapping(ISourceLocation node, int line, int column)
    {
        if (lastMapping != null)
        {
            FilePosition sourceStartPosition = lastMapping.sourceStartPosition;
            throw new IllegalStateException("Cannot start new mapping when another mapping is already started. "
                    + "Previous mapping at Line " + sourceStartPosition.getLine()
                    + " and Column " + sourceStartPosition.getColumn()
                    + " in file " + lastMapping.sourcePath);
        }
        
        String sourcePath = node.getSourcePath();
        if (sourcePath == null)
        {
            //if the source path is null, this node may have been generated by
            //the compiler automatically. for example, an untyped variable will
            //have a node for the * type.
            if (node instanceof IASNode)
            {
                IASNode parentNode = ((IASNode) node).getParent();
                if (parentNode != null)
                {
                    //try the parent node
                    startMapping(parentNode);
                }
            }
            return;
        }
        
        String nodeName = null;
        if (nameStack.size() > 0)
        {
            nodeName = nameStack.lastElement();
        }
        SourceMapMapping mapping = new SourceMapMapping();
        mapping.sourcePath = sourcePath;
        mapping.name = nodeName;
        mapping.sourceStartPosition = new FilePosition(line, column);
        mapping.destStartPosition = new FilePosition(getCurrentLine(), getCurrentColumn());
        lastMapping = mapping;
    }

    public void endMapping(ISourceLocation node)
    {
        if (lastMapping == null)
        {
            throw new IllegalStateException("Cannot end mapping when a mapping has not been started");
        }
        
        lastMapping.destEndPosition = new FilePosition(getCurrentLine(), getCurrentColumn());
        sourceMapMappings.add(lastMapping);
        lastMapping = null;
    }

    /**
     * Adjusts the line numbers saved in the source map when a line should be
     * added during post processing.
     *
     * @param lineIndex
     */
    protected void addLineToMappings(int lineIndex)
    {
        for (SourceMapMapping mapping : sourceMapMappings)
        {
            FilePosition destStartPosition = mapping.destStartPosition;
            int startLine = destStartPosition.getLine();
            if(startLine > lineIndex)
            {
                mapping.destStartPosition = new FilePosition(startLine + 1, destStartPosition.getColumn());
                FilePosition destEndPosition = mapping.destEndPosition;
                mapping.destEndPosition = new FilePosition(destEndPosition.getLine() + 1, destEndPosition.getColumn());
            }
        }
    }

    /**
     * Adjusts the line numbers saved in the source map when a line should be
     * removed during post processing.
     * 
     * @param lineIndex
     */
    protected void removeLineFromMappings(int lineIndex)
    {
        for (SourceMapMapping mapping : sourceMapMappings)
        {
            FilePosition destStartPosition = mapping.destStartPosition;
            int startLine = destStartPosition.getLine();
            if(startLine > lineIndex)
            {
                mapping.destStartPosition = new FilePosition(startLine - 1, destStartPosition.getColumn());
                FilePosition destEndPosition = mapping.destEndPosition;
                mapping.destEndPosition = new FilePosition(destEndPosition.getLine() - 1, destEndPosition.getColumn());
            }
        }
    }

}
