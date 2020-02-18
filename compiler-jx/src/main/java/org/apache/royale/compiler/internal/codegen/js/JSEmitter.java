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

package org.apache.royale.compiler.internal.codegen.js;

import java.io.FilterWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.codegen.IASGlobalFunctionConstants;
import org.apache.royale.compiler.codegen.IDocEmitter;
import org.apache.royale.compiler.codegen.IEmitter;
import org.apache.royale.compiler.codegen.ISubEmitter;
import org.apache.royale.compiler.codegen.js.IJSEmitter;
import org.apache.royale.compiler.codegen.js.IMappingEmitter;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IAppliedVectorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.internal.codegen.as.ASEmitter;
import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.internal.codegen.js.jx.BlockCloseEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.BlockOpenEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.CatchEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.DoWhileLoopEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.DynamicAccessEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ForLoopEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.FunctionCallArgumentsEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.IfEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.IterationFlowEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.LanguageIdentifierEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.LiteralContainerEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.MemberKeywordEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.NumericLiteralEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ObjectLiteralValuePairEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ParameterEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ParametersEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ReturnEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.SourceMapDirectiveEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.StatementEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.SwitchEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.TernaryOperatorEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.ThrowEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.TryEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.UnaryOperatorEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.WhileLoopEmitter;
import org.apache.royale.compiler.internal.codegen.js.jx.WithEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleDocEmitter;
import org.apache.royale.compiler.internal.codegen.js.royale.JSRoyaleEmitterTokens;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ICatchNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IFunctionObjectNode;
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.compiler.tree.as.IIterationFlowNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILiteralContainerNode;
import org.apache.royale.compiler.tree.as.INumericLiteralNode;
import org.apache.royale.compiler.tree.as.IObjectLiteralValuePairNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IReturnNode;
import org.apache.royale.compiler.tree.as.ISwitchNode;
import org.apache.royale.compiler.tree.as.ITernaryOperatorNode;
import org.apache.royale.compiler.tree.as.IThrowNode;
import org.apache.royale.compiler.tree.as.ITryNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.ITypedExpressionNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IWhileLoopNode;
import org.apache.royale.compiler.tree.as.IWithNode;

import com.google.debugging.sourcemap.FilePosition;

import org.apache.royale.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.royale.compiler.utils.NativeUtils;

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
        String name = fnode.getName();
        //may have a name, or may be anonymous
        if(name.length() > 0)
        {
            write(ASEmitterTokens.SPACE);
            write(name);
        }
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
        sourceMapDirectiveEmitter.isExterns = getModel().isExterns;
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
        IEmitter parentEmitter = getParentEmitter();
        if (parentEmitter != null && parentEmitter instanceof IMappingEmitter)
        {
            IMappingEmitter mappingParent = (IMappingEmitter) parentEmitter;
            mappingParent.startMapping(node, line, column);
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

        //prefer forward slash
        sourcePath = sourcePath.replace('\\', '/');

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
        IEmitter parentEmitter = getParentEmitter();
        if (parentEmitter != null && parentEmitter instanceof IMappingEmitter)
        {
            IMappingEmitter mappingParent = (IMappingEmitter) parentEmitter;
            mappingParent.endMapping(node);
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

	@Override
	public String formatPrivateName(String className, String name) {
		// TODO Auto-generated method stub
		return className.replace(".", "_") + "_" + name;
	}

    public String formatPrivateName(String className, String name, Boolean nameFirst) {
        if (nameFirst) {
            return name + "_" +className.replace(".", "_");
        } else {
            return formatPrivateName(className, name);
        }
    }

    public void emitAssignmentCoercion(IExpressionNode assignedNode, IDefinition definition)
    {
        IDefinition assignedDef = null;
        IDefinition assignedTypeDef = null;
        ICompilerProject project = getWalker().getProject();
        if (assignedNode != null)
        {
            assignedDef = assignedNode.resolve(project);
            assignedTypeDef = assignedNode.resolveType(project);
            if (project.getBuiltinType(BuiltinType.ANY_TYPE).equals(assignedTypeDef))
            {
                IDefinition resolvedXMLDef = SemanticUtils.resolveXML(assignedNode, project);
                if (resolvedXMLDef != null)
                {
                    assignedDef = resolvedXMLDef;
                    assignedTypeDef = SemanticUtils.resolveTypeXML(assignedNode, project);
                }
            }
        }
		String coercionStart = null;
        String coercionEnd = null;
        boolean avoidCoercion = false;
		if (project.getBuiltinType(BuiltinType.INT).equals(definition))
		{
			boolean needsCoercion = false;
			if (assignedNode instanceof INumericLiteralNode)
			{
				INumericLiteralNode numericLiteral = (INumericLiteralNode) assignedNode;
                INumericLiteralNode.INumericValue numericValue = numericLiteral.getNumericValue();
                startMapping(assignedNode);
                if(numericValue.toString().startsWith("0x"))
                {
                    //for readability, keep the same formatting
                    write("0x" + Integer.toHexString(numericValue.toInt32()));
                }
                else
                {
                    write(Integer.toString(numericValue.toInt32()));
                }
                endMapping(assignedNode);
                return;
			}
			else if(assignedNode instanceof BinaryOperatorAsNode)
            {
                needsCoercion = true;
            }
			else if(!project.getBuiltinType(BuiltinType.INT).equals(assignedTypeDef))
			{
				needsCoercion = true;
			}
			if (needsCoercion)
			{
				coercionStart = "(";
				coercionEnd = ") >> 0";
			}
		}
		else if (project.getBuiltinType(BuiltinType.UINT).equals(definition))
		{
			boolean needsCoercion = false;
			if (assignedNode instanceof INumericLiteralNode)
			{
                INumericLiteralNode numericLiteral = (INumericLiteralNode) assignedNode;
                INumericLiteralNode.INumericValue numericValue = numericLiteral.getNumericValue();
                startMapping(assignedNode);
                if(numericValue.toString().startsWith("0x"))
                {
                    //for readability, keep the same formatting
                    write("0x" + Long.toHexString(numericValue.toUint32()));
                }
                else
                {
                    write(Long.toString(numericValue.toUint32()));
                }
                endMapping(assignedNode);
                return;
			}
            else if(assignedNode instanceof BinaryOperatorAsNode)
            {
                needsCoercion = true;
            }
			else if(!project.getBuiltinType(BuiltinType.UINT).equals(assignedTypeDef))
			{
				needsCoercion = true;
			}
			if (needsCoercion)
			{
				coercionStart = "(";
				coercionEnd = ") >>> 0";
            }
        }
        else if (project.getBuiltinType(BuiltinType.NUMBER).equals(definition)
                && !project.getBuiltinType(BuiltinType.NUMBER).equals(assignedTypeDef)
                && !project.getBuiltinType(BuiltinType.INT).equals(assignedTypeDef)
                && !project.getBuiltinType(BuiltinType.UINT).equals(assignedTypeDef))
        {
			boolean needsCoercion = true;
            if (assignedNode instanceof IDynamicAccessNode)
            {
                IDynamicAccessNode dynamicAccess = (IDynamicAccessNode) assignedNode;
                IDefinition dynamicAccessIndexDef = dynamicAccess.getRightOperandNode().resolveType(project);
                if (project.getBuiltinType(BuiltinType.NUMBER).equals(dynamicAccessIndexDef))
                {
                    IDefinition leftDef = dynamicAccess.getLeftOperandNode().resolveType(project);
                    IMetaTag[] metas = leftDef.getAllMetaTags();
                    for (IMetaTag meta : metas)
                    {
                        if (meta.getTagName().equals(IMetaAttributeConstants.ATTRIBUTE_ARRAYELEMENTTYPE))
                        {
                            IMetaTagAttribute[] attrs = meta.getAllAttributes();
                            for (IMetaTagAttribute attr : attrs)
                            {
                                String t = attr.getValue();
                                if (t.equals(IASLanguageConstants.Number))
                                {
                                    needsCoercion = false;
                                    //explicitly prevent other coercion detection rules from picking this up
                                    avoidCoercion = true;
                                }
                            }
                        }
                    }
                }
            }
            if (needsCoercion)
            {
                coercionStart = "Number(";
            }
        }
        else if (project.getBuiltinType(BuiltinType.BOOLEAN).equals(definition)
                && !project.getBuiltinType(BuiltinType.BOOLEAN).equals(assignedTypeDef))
        {
            if (project.getBuiltinType(BuiltinType.NULL).equals(assignedTypeDef)
                    || (assignedDef != null && assignedDef.getQualifiedName().equals(IASLanguageConstants.UNDEFINED)))
            {
                //null and undefined are coerced to false
                startMapping(assignedNode);
                write(IASLanguageConstants.FALSE);
                endMapping(assignedNode);
                return;
            }
			if (assignedNode instanceof INumericLiteralNode)
			{
                INumericLiteralNode numericLiteral = (INumericLiteralNode) assignedNode;
                INumericLiteralNode.INumericValue numericValue = numericLiteral.getNumericValue();
                //zero is coerced to false, and everything else is true
                String booleanValue = numericValue.toNumber() == 0.0
                        ? IASLanguageConstants.FALSE
                        : IASLanguageConstants.TRUE;
                startMapping(assignedNode);
                write(booleanValue);
                endMapping(assignedNode);
                return;
			}
            coercionStart = "!!(";
        }
        else if (project.getBuiltinType(BuiltinType.STRING).equals(definition)
                && !project.getBuiltinType(BuiltinType.STRING).equals(assignedTypeDef)
                && !project.getBuiltinType(BuiltinType.NULL).equals(assignedTypeDef)
                && !(project.getBuiltinType(BuiltinType.ANY_TYPE).equals(assignedTypeDef)
                        && SemanticUtils.isToStringFunctionCall(assignedNode, project)))
        {
            if(assignedDef != null && assignedDef.getQualifiedName().equals(IASLanguageConstants.UNDEFINED))
            {
                //undefined is coerced to null
                startMapping(assignedNode);
                write(IASLanguageConstants.NULL);
                endMapping(assignedNode);
                return;
            }
            boolean emitStringCoercion = true;
            IDocEmitter docEmitter = getDocEmitter();
            if (docEmitter instanceof JSRoyaleDocEmitter)
            {
                JSRoyaleDocEmitter royaleDocEmitter = (JSRoyaleDocEmitter) docEmitter;
                emitStringCoercion = royaleDocEmitter.emitStringConversions;
            }
            if (emitStringCoercion)
            {
                coercionStart = "org.apache.royale.utils.Language.string(";
            }
        }
        if ( assignedDef != null
                && assignedDef instanceof IAppliedVectorDefinition
                && assignedNode instanceof TypedExpressionNode) {
            //assign a Vector class as the assigned value, e.g. var c:Class = Vector.<int>
            if (project instanceof RoyaleJSProject
                    && ((RoyaleJSProject)project).config.getJsVectorEmulationClass() != null) {
                startMapping(assignedNode);
                write(((RoyaleJSProject)project).config.getJsVectorEmulationClass());
                endMapping(assignedNode);
            } else {
                startMapping(assignedNode);
                write(JSRoyaleEmitterTokens.SYNTH_VECTOR);
                write(ASEmitterTokens.PAREN_OPEN);
                write(ASEmitterTokens.SINGLE_QUOTE);
                //the element type of the Vector:
                write(formatQualifiedName(((TypedExpressionNode)assignedNode).getTypeNode().resolve(project).getQualifiedName()));
                write(ASEmitterTokens.SINGLE_QUOTE);
                write(ASEmitterTokens.PAREN_CLOSE);
                endMapping(assignedNode);
                if (project instanceof RoyaleJSProject)
                    ((RoyaleJSProject)project).needLanguage = true;
                getModel().needLanguage = true;
            }
            return;
        }
        if (assignedDef instanceof IClassDefinition
                && assignedNode instanceof IdentifierNode
                && ((IdentifierNode)assignedNode).getName().equals(IASGlobalFunctionConstants.Vector)
                && project instanceof RoyaleJSProject
                && ((RoyaleJSProject)project).config.getJsVectorEmulationClass() == null   ){
            startMapping(assignedNode);
            write(JSRoyaleEmitterTokens.SYNTH_VECTOR);
            write(ASEmitterTokens.PAREN_OPEN);
            //null to signify not a valid constructor
            write(ASEmitterTokens.NULL);
            write(ASEmitterTokens.PAREN_CLOSE);
            endMapping(assignedNode);
            if (project instanceof RoyaleJSProject)
                ((RoyaleJSProject)project).needLanguage = true;
            getModel().needLanguage = true;
            return;
        }
        if (coercionStart == null
                && !avoidCoercion
                && assignedTypeDef !=null
                && definition !=null
                && (project.getBuiltinType(BuiltinType.ANY_TYPE).equals(assignedTypeDef)
                || project.getBuiltinType(BuiltinType.OBJECT).equals(assignedTypeDef))
                && !(project.getBuiltinType(BuiltinType.ANY_TYPE).equals(definition)
                        || project.getBuiltinType(BuiltinType.OBJECT).equals(definition))) {
            //catch leftovers: remaining implicit coercion of loosely typed assigned values to strongly typed context
            //assignment to Class definitions is excluded because there is no 'Class' type in JS
            //Possibility: 'Class' could be implemented as a synthType
            boolean needsCoercion = ((RoyaleJSProject)project).config.getJsComplexImplicitCoercions();
    
            IDocEmitter docEmitter = getDocEmitter();
            if (docEmitter instanceof JSRoyaleDocEmitter)
            {
                JSRoyaleDocEmitter royaleDocEmitter = (JSRoyaleDocEmitter) docEmitter;
                //check for local toggle
                if (needsCoercion) needsCoercion = !(royaleDocEmitter.getLocalSettingAsBoolean(
                        JSRoyaleEmitterTokens.SUPPRESS_COMPLEX_IMPLICIT_COERCION, false));
                else {
                    if (royaleDocEmitter.hasLocalSetting(JSRoyaleEmitterTokens.SUPPRESS_COMPLEX_IMPLICIT_COERCION.getToken())) {
                        needsCoercion = !(royaleDocEmitter.getLocalSettingAsBoolean(
                                JSRoyaleEmitterTokens.SUPPRESS_COMPLEX_IMPLICIT_COERCION, false));
                    }
                }
                if (needsCoercion) {
                    //check for individual specified suppression
                    
                    String definitionName = definition.getQualifiedName();
                    //for Vectors, use the unqualified name to match the source code
                    if (NativeUtils.isVector(definitionName)) {
                        definitionName = definition.getBaseName();
                    }
                    
                    if (royaleDocEmitter.getLocalSettingIncludesString(
                            JSRoyaleEmitterTokens.SUPPRESS_COMPLEX_IMPLICIT_COERCION,
                            definitionName))
                    {
                        needsCoercion = false;
                    }
                    
                }
            }
            
            //Avoid specific compile-time 'fake' class(es)
            if (needsCoercion && definition.getQualifiedName().equals("org.apache.royale.core.WrappedHTMLElement")) {
                //*actual* coercion fails here, because this is not actually instantiated, it is
                //simply a type definition representing the 'wrapped' (or tagged) HTMLElement
                needsCoercion = false;
            }
            
            //Avoid XML/XMLList:
            if (needsCoercion && project.getBuiltinType(BuiltinType.XML) != null) {
                if (project.getBuiltinType(BuiltinType.XML).equals(definition)
                        || project.getBuiltinType(BuiltinType.XMLLIST).equals(definition)) {
                    //XML/XMLList has complex output and would need more work
                    needsCoercion = false;
                }
            }
            
            //avoid scenario with ArrayElementType specified as metadata definition type - assume it is 'typed'
            if (needsCoercion && assignedNode instanceof IDynamicAccessNode)
            {
                IDynamicAccessNode dynamicAccess = (IDynamicAccessNode) assignedNode;
                IDefinition dynamicAccessIndexDef = dynamicAccess.getRightOperandNode().resolveType(project);
                if (project.getBuiltinType(BuiltinType.NUMBER).equals(dynamicAccessIndexDef))
                {
                    IDefinition leftDef = dynamicAccess.getLeftOperandNode().resolveType(project);
                    if (leftDef != null) {
                        IMetaTag[] metas = leftDef.getAllMetaTags();
                        for (IMetaTag meta : metas)
                        {
                            if (meta.getTagName().equals(IMetaAttributeConstants.ATTRIBUTE_ARRAYELEMENTTYPE))
                            {
                                IMetaTagAttribute[] attrs = meta.getAllAttributes();
                                for (IMetaTagAttribute attr : attrs)
                                {
                                    String t = attr.getValue();
                                    if (t.equals(definition.getQualifiedName()))
                                    {
                                        needsCoercion = false;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (needsCoercion && project.getBuiltinType(BuiltinType.STRING).equals(definition)) {
                //explicit suppression of String coercion
                if (docEmitter instanceof JSRoyaleDocEmitter)
                {
                    JSRoyaleDocEmitter royaleDocEmitter = (JSRoyaleDocEmitter) docEmitter;
                    needsCoercion = royaleDocEmitter.emitStringConversions;
                }
                if (needsCoercion
                        && assignedNode instanceof FunctionCallNode
                        && ((FunctionCallNode) assignedNode).getNameNode() instanceof MemberAccessExpressionNode
                        && ((MemberAccessExpressionNode)((FunctionCallNode) assignedNode).getNameNode()).getRightOperandNode()  instanceof IdentifierNode
                        && ((IdentifierNode)(((MemberAccessExpressionNode)((FunctionCallNode) assignedNode).getNameNode()).getRightOperandNode())).getName().equals("toString")) {
                        //even if toString() is called in an untyped way, assume a call to a method named 'toString' is actually providing a String
                    needsCoercion = false;
                }
            }
            
            if (needsCoercion) {
                //add a comment tag leader, so implicit casts are identifiable in the output
                coercionStart = "/* implicit cast */ "
                        + JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken()
                        + ASEmitterTokens.MEMBER_ACCESS.getToken()
                        + ASEmitterTokens.AS.getToken()
                        + ASEmitterTokens.PAREN_OPEN.getToken();
                String coercionTypeString = formatQualifiedName(definition.getQualifiedName());
                if (NativeUtils.isSyntheticJSType(coercionTypeString)) {
                    String synthCall;
                    String synthethicType;
                    if (NativeUtils.isVector(coercionTypeString)) {
                        synthCall = JSRoyaleEmitterTokens.SYNTH_VECTOR.getToken();
                        synthethicType = formatQualifiedName(coercionTypeString.substring(8, coercionTypeString.length() -1));
                    } else {
                        synthCall = JSRoyaleEmitterTokens.SYNTH_TYPE.getToken();
                        synthethicType = coercionTypeString;
                    }
                    coercionTypeString = synthCall
                            + ASEmitterTokens.PAREN_OPEN.getToken()
                            + ASEmitterTokens.SINGLE_QUOTE.getToken()
                            + synthethicType
                            + ASEmitterTokens.SINGLE_QUOTE.getToken()
                            + ASEmitterTokens.PAREN_CLOSE.getToken();
                }
                
                coercionEnd = ASEmitterTokens.COMMA.getToken()
                        + ASEmitterTokens.SPACE.getToken()
                        + coercionTypeString
                        + ASEmitterTokens.COMMA.getToken()
                        + ASEmitterTokens.SPACE.getToken()
                        + ASEmitterTokens.TRUE.getToken()
                        + ASEmitterTokens.PAREN_CLOSE.getToken();
                if (project instanceof RoyaleJSProject)
                    ((RoyaleJSProject)project).needLanguage = true;
                getModel().needLanguage = true;
            }
        }

		if (coercionStart != null)
		{
			write(coercionStart);
        }
        emitAssignedValue(assignedNode);
		if (coercionStart != null)
		{
			if (coercionEnd != null)
			{
				write(coercionEnd);
			}
			else
			{
				write(")");
			}
		}
    }

}
