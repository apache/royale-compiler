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

import org.apache.flex.compiler.codegen.ISubEmitter;
import org.apache.flex.compiler.codegen.js.IJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.ISourceLocation;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitter;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.jx.BlockCloseEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.BlockOpenEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.CatchEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.DoWhileLoopEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.DynamicAccessEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ForLoopEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FunctionCallArgumentsEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.IfEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.IterationFlowEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.LanguageIdentifierEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.LiteralContainerEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MemberKeywordEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.NumericLiteralEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ObjectLiteralValuePairEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ParameterEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ParametersEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ReturnEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SourceMapDirectiveEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.StatementEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SwitchEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.TernaryOperatorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ThrowEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.TryEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.UnaryOperatorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.WhileLoopEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.WithEmitter;
import org.apache.flex.compiler.internal.codegen.js.utils.EmitterUtils;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.ICatchNode;
import org.apache.flex.compiler.tree.as.IContainerNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IFunctionObjectNode;
import org.apache.flex.compiler.tree.as.IIfNode;
import org.apache.flex.compiler.tree.as.IImportNode;
import org.apache.flex.compiler.tree.as.IIterationFlowNode;
import org.apache.flex.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.flex.compiler.tree.as.ILiteralContainerNode;
import org.apache.flex.compiler.tree.as.INumericLiteralNode;
import org.apache.flex.compiler.tree.as.IObjectLiteralValuePairNode;
import org.apache.flex.compiler.tree.as.IPackageNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.IReturnNode;
import org.apache.flex.compiler.tree.as.ISwitchNode;
import org.apache.flex.compiler.tree.as.ITernaryOperatorNode;
import org.apache.flex.compiler.tree.as.IThrowNode;
import org.apache.flex.compiler.tree.as.ITryNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IWhileLoopNode;
import org.apache.flex.compiler.tree.as.IWithNode;

import com.google.debugging.sourcemap.FilePosition;

/**
 * @author Michael Schmalle
 */
public class JSEmitter extends ASEmitter implements IJSEmitter
{
    private JSSessionModel model;
    
    public ISubEmitter<IContainerNode> blockOpenEmitter;
    public ISubEmitter<IContainerNode> blockCloseEmitter;
    public ISubEmitter<INumericLiteralNode> numericLiteralEmitter;
    public ISubEmitter<IContainerNode> parametersEmitter;
    public ISubEmitter<IParameterNode> parameterEmitter;
    public ISubEmitter<IContainerNode> functionCallArgumentsEmitter;
    public ISubEmitter<ILiteralContainerNode> literalContainerEmitter;
    public ISubEmitter<IObjectLiteralValuePairNode> objectLiteralValuePairEmitter;
    public ISubEmitter<IReturnNode> returnEmitter;
    public ISubEmitter<IDynamicAccessNode> dynamicAccessEmitter;
    public ISubEmitter<IUnaryOperatorNode> unaryOperatorEmitter;
    public ISubEmitter<ITernaryOperatorNode> ternaryOperatorEmitter;
    public ISubEmitter<IDefinitionNode> memberKeywordEmitter;
    public ISubEmitter<IIfNode> ifEmitter;
    public ISubEmitter<ISwitchNode> switchEmitter;
    public ISubEmitter<IWhileLoopNode> whileLoopEmitter;
    public ISubEmitter<IWhileLoopNode> doWhileLoopEmitter;
    public ISubEmitter<IForLoopNode> forLoopEmitter;
    public ISubEmitter<IIterationFlowNode> interationFlowEmitter;
    public ISubEmitter<ITryNode> tryEmitter;
    public ISubEmitter<ICatchNode> catchEmitter;
    public ISubEmitter<IThrowNode> throwEmitter;
    public ISubEmitter<IWithNode> withEmitter;
    public ISubEmitter<IASNode> statementEmitter;
    public ISubEmitter<ILanguageIdentifierNode> languageIdentifierEmitter;
    public SourceMapDirectiveEmitter sourceMapDirectiveEmitter;
    
    @Override
    public JSSessionModel getModel()
    {
        return model;
    }
    
    private SourceMapMapping lastMapping;
    
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

        blockOpenEmitter = new BlockOpenEmitter(this);
        blockCloseEmitter = new BlockCloseEmitter(this);
        numericLiteralEmitter = new NumericLiteralEmitter(this);
        parametersEmitter = new ParametersEmitter(this);
        parameterEmitter = new ParameterEmitter(this);
        functionCallArgumentsEmitter = new FunctionCallArgumentsEmitter(this);
        literalContainerEmitter = new LiteralContainerEmitter(this);
        objectLiteralValuePairEmitter = new ObjectLiteralValuePairEmitter(this);
        returnEmitter = new ReturnEmitter(this);
        dynamicAccessEmitter = new DynamicAccessEmitter(this);
        unaryOperatorEmitter = new UnaryOperatorEmitter(this);
        ternaryOperatorEmitter = new TernaryOperatorEmitter(this);
        memberKeywordEmitter = new MemberKeywordEmitter(this);
        ifEmitter = new IfEmitter(this);
        switchEmitter = new SwitchEmitter(this);
        whileLoopEmitter = new WhileLoopEmitter(this);
        doWhileLoopEmitter = new DoWhileLoopEmitter(this);
        forLoopEmitter = new ForLoopEmitter(this);
        interationFlowEmitter = new IterationFlowEmitter(this);
        tryEmitter = new TryEmitter(this);
        catchEmitter = new CatchEmitter(this);
        throwEmitter = new ThrowEmitter(this);
        withEmitter = new WithEmitter(this);
        statementEmitter = new StatementEmitter(this);
        languageIdentifierEmitter = new LanguageIdentifierEmitter(this);
        sourceMapDirectiveEmitter = new SourceMapDirectiveEmitter(this);
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
        emitParameters(fnode.getParametersContainerNode());
        emitFunctionScope(fnode.getScopedNode());
    }
    
    @Override
    public void emitFunctionObject(IFunctionObjectNode node)
    {
        startMapping(node);
        FunctionNode fnode = node.getFunctionNode();
        write(ASEmitterTokens.FUNCTION);
        endMapping(node);
        emitParameters(fnode.getParametersContainerNode());
        emitFunctionScope(fnode.getScopedNode());
    }

    public void emitClosureStart()
    {
    	
    }

    public void emitClosureEnd(IASNode node, IDefinition nodeDef)
    {
    	
    }
    
    public void emitSourceMapDirective(ITypeNode node)
    {
        sourceMapDirectiveEmitter.emit(node);
    }

    public void emitParameters(IContainerNode node)
    {
        parametersEmitter.emit(node);
    }

    @Override
    public void emitParameter(IParameterNode node)
    {
        parameterEmitter.emit(node);
    }

    @Override
    public void emitArguments(IContainerNode node)
    {
        functionCallArgumentsEmitter.emit(node);
    }

    @Override
    public void emitNumericLiteral(INumericLiteralNode node)
    {
        numericLiteralEmitter.emit(node);
    }

    @Override
    public void emitLiteralContainer(ILiteralContainerNode node)
    {
        literalContainerEmitter.emit(node);
    }

    @Override
    public void emitObjectLiteralValuePair(IObjectLiteralValuePairNode node)
    {
        objectLiteralValuePairEmitter.emit(node);
    }

    @Override
    public void emitTry(ITryNode node)
    {
        tryEmitter.emit(node);
    }

    @Override
    public void emitCatch(ICatchNode node)
    {
        catchEmitter.emit(node);
    }

    @Override
    public void emitWith(IWithNode node)
    {
        withEmitter.emit(node);
    }

    @Override
    public void emitThrow(IThrowNode node)
    {
        throwEmitter.emit(node);
    }

    @Override
    public void emitReturn(IReturnNode node)
    {
        returnEmitter.emit(node);
    }

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSEmitterTokens.ARRAY);
    }

    @Override
    public void emitDynamicAccess(IDynamicAccessNode node)
    {
        dynamicAccessEmitter.emit(node);
    }

    @Override
    public void emitMemberKeyword(IDefinitionNode node)
    {
        memberKeywordEmitter.emit(node);
    }

    @Override
    public void emitUnaryOperator(IUnaryOperatorNode node)
    {
        unaryOperatorEmitter.emit(node);
    }

    @Override
    public void emitTernaryOperator(ITernaryOperatorNode node)
    {
        ternaryOperatorEmitter.emit(node);
    }

    @Override
    public void emitLanguageIdentifier(ILanguageIdentifierNode node)
    {
        languageIdentifierEmitter.emit(node);
    }

    @Override
    public void emitStatement(IASNode node)
    {
        statementEmitter.emit(node);
    }

    @Override
    public void emitIf(IIfNode node)
    {
        ifEmitter.emit(node);
    }

    @Override
    public void emitSwitch(ISwitchNode node)
    {
        switchEmitter.emit(node);
    }

    @Override
    public void emitImport(IImportNode node)
    {
        // do nothing
    }

    @Override
    public void emitWhileLoop(IWhileLoopNode node)
    {
        whileLoopEmitter.emit(node);
    }

    @Override
    public void emitDoLoop(IWhileLoopNode node)
    {
        doWhileLoopEmitter.emit(node);
    }

    @Override
    public void emitForLoop(IForLoopNode node)
    {
        forLoopEmitter.emit(node);
    }

    @Override
    public void emitIterationFlow(IIterationFlowNode node)
    {
        interationFlowEmitter.emit(node);
    }

    @Override
    public void emitBlockOpen(IContainerNode node)
    {
        blockOpenEmitter.emit(node);
    }

    @Override
    public void emitBlockClose(IContainerNode node)
    {
        blockCloseEmitter.emit(node);
    }

    public void startMapping(ISourceLocation node)
    {
        startMapping(node, node.getLine(), node.getColumn());
    }
    
    public void startMapping(ISourceLocation node, int line, int column)
    {
        if (isBufferWrite())
        {
            return;
        }
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
                    startMapping(parentNode, line, column);
                    return;
                }
            }
        }

        SourceMapMapping mapping = new SourceMapMapping();
        mapping.sourcePath = sourcePath;
        mapping.sourceStartPosition = new FilePosition(line, column);
        mapping.destStartPosition = new FilePosition(getCurrentLine(), getCurrentColumn());
        lastMapping = mapping;
    }

    public void startMapping(ISourceLocation node, ISourceLocation afterNode)
    {
        startMapping(node, afterNode.getEndLine(), afterNode.getEndColumn());
    }

    public void endMapping(ISourceLocation node)
    {
        if (isBufferWrite())
        {
            return;
        }
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
