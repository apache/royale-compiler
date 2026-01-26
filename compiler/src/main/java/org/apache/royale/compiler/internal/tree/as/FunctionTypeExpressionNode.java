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

package org.apache.royale.compiler.internal.tree.as;

import java.io.StringReader;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.parsing.as.ASParser;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.parsing.as.StreamingASTokenizer;
import org.apache.royale.compiler.internal.parsing.as.StreamingTokenBuffer;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnexpectedExceptionProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IContainerNode.ContainerType;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionTypeExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * ActionScript parse tree node representing a function type expression.
 */
public class FunctionTypeExpressionNode extends ExpressionNodeBase implements IFunctionTypeExpressionNode
{
    public static String getSignatureFromDefinition(IDefinition definition)
    {
        return getSignatureFromDefinition(definition, null);
    }

    public static String getSignatureFromDefinition(IDefinition definition, String paramName)
    {
        IMetaTag[] functionTypeTags = definition.getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_FUNCTION_TYPE);
        if (functionTypeTags == null)
        {
            return null;
        }

        IMetaTag foundMetaTag = null;
        for (IMetaTag functionTypeTag : functionTypeTags)
        {
            String metaParamName = functionTypeTag.getAttributeValue(IMetaAttributeConstants.NAME_FUNCTION_TYPE_PARAM_NAME);
            if ((paramName == null && metaParamName == null)
                || (paramName != null && paramName.equals(metaParamName)))
            {
                foundMetaTag = functionTypeTag;
                break;
            }
        }
        if (foundMetaTag == null)
        {
            return null;
        }
        
        String signature = foundMetaTag.getAttributeValue(IMetaAttributeConstants.NAME_FUNCTION_TYPE_SIGNATURE);
        if (signature == null)
        {
            return null;
        }
        
        // whitespace should not be significant in function signatures,
        // so remove it all to make it easier to parse
        return signature.replaceAll("\\s", "");
    }

    public static FunctionTypeExpressionNode createFromDefinition(IDefinition definition, ISourceLocation location, ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        assert definition != null;

        if (!(definition instanceof IVariableDefinition))
        {
            if (definition instanceof IFunctionDefinition)
            {
                IFunctionDefinition funcDef = (IFunctionDefinition) definition;
                return createFromFunctionDefinition(funcDef, project);
            }
            return null;
        }

        String signature = getSignatureFromDefinition(definition);
        if (signature == null)
        {
            return null;
        }

        ASScope asScope = (ASScope) definition.getContainingScope();
        return FunctionTypeExpressionNode.parseSignature(signature, asScope, definition, project, problems);
    }

    public static FunctionTypeExpressionNode createFromFunctionDefinition(IFunctionDefinition functionDefinition, ICompilerProject project)
    {
        assert functionDefinition != null;

        FunctionTypeExpressionNode funcTypeExprNode = new FunctionTypeExpressionNode();
        funcTypeExprNode.setSourcePath(functionDefinition.getSourcePath());
        funcTypeExprNode.setLine(functionDefinition.getLine());
        funcTypeExprNode.setColumn(functionDefinition.getColumn());
        funcTypeExprNode.setEndLine(functionDefinition.getEndLine());
        funcTypeExprNode.setEndColumn(functionDefinition.getEndColumn());
        funcTypeExprNode.setStart(functionDefinition.getStart());
        funcTypeExprNode.setEnd(functionDefinition.getEnd());

        ASScope asScope = (ASScope) functionDefinition.getContainingScope();

        ContainerNode paramContainerNode = funcTypeExprNode.getParametersContainerNode();
        for (IParameterDefinition paramDef : functionDefinition.getParameters())
        {
            IdentifierNode nameNode = new IdentifierNode(paramDef.getBaseName());
            ParameterNode paramNode = new ParameterNode(nameNode);
            paramNode.setSourcePath(paramDef.getSourcePath());
            paramNode.setLine(paramDef.getLine());
            paramNode.setColumn(paramDef.getColumn());
            paramNode.setEndLine(paramDef.getEndLine());
            paramNode.setEndColumn(paramDef.getEndColumn());
            paramNode.setStart(paramDef.getStart());
            paramNode.setEnd(paramDef.getEnd());

            ExpressionNodeBase typeNode = createTypeNodeFromParameterDefinition(paramDef, functionDefinition, asScope, project);

            ASToken typeToken = new ASToken(ASTokenTypes.TOKEN_COLON, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, ":");
            paramNode.setType(typeToken, typeNode); 
            if (paramDef.isRest())
            {
                paramNode.setIsRestParameter(true);
            }
            else if (paramDef.hasDefaultValue())
            {
                ASToken optionalToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_TERNARY, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, "?");
                paramNode.setAssignedValue(optionalToken, new IdentifierNode(""));
            }
            paramContainerNode.addItem(paramNode);
        }
        ExpressionNodeBase returnTypeNode = createReturnTypeNodeFromFunctionDefinition(functionDefinition, asScope, project);
        funcTypeExprNode.setReturnType(returnTypeNode);

        ContainerNode containerNode = new ContainerNode();
        containerNode.addChild(funcTypeExprNode);

        ScopedBlockNode container = new ScopedBlockNode();
        container.setScope(asScope);
        container.addChild(funcTypeExprNode);
        container.normalize(true);

        return funcTypeExprNode;
    }

    private static FunctionTypeExpressionNode parseSignature(String signature, ASScope scope, IDefinition sourceDefinition, ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        // prefer the actual AST node, if available
        IDefinitionNode sourceNode = sourceDefinition.getNode();
        if (sourceNode != null)
        {
            return parseSignature(signature, scope, sourceNode, project, problems);
        }
        String sourcePath = sourceDefinition.getSourcePath();
        int start = sourceDefinition.getAbsoluteStart();
        int line = sourceDefinition.getLine();
        int column = sourceDefinition.getColumn();
        return parseSignature(signature, scope, sourcePath, start, line, column, project, problems);
    }

    private static FunctionTypeExpressionNode parseSignature(String signature, ASScope scope, ISourceLocation location, ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        String sourcePath = null;
        int start = -1;
        int line = -1;
        int column = -1;
        if (location != null)
        {
            sourcePath = location.getSourcePath();
            start = location.getAbsoluteStart();
            line = location.getLine();
            column = location.getColumn();
        }
        return parseSignature(signature, scope, sourcePath, start, line, column, project, problems);
    }

    private static FunctionTypeExpressionNode parseSignature(String signature, ASScope scope, String sourcePath, int start, int line, int column, ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        FunctionTypeExpressionNode funcTypeExpr = null;
        StreamingASTokenizer tokenizer = null;
        StreamingTokenBuffer buffer = null;
        try
        {
            tokenizer = StreamingASTokenizer.createForRepairingASTokenizer(
                    new StringReader(signature),
                    sourcePath,
                    null);
            tokenizer.setSourcePositionAdjustment(start, line, column);

            buffer = new StreamingTokenBuffer(tokenizer);
            buffer.setEnableSemicolonInsertion(false);

            final ASParser parser = new ASParser(project.getWorkspace(), buffer);
            parser.setFilename(sourcePath);
            parser.setAllowEmbeds(false);
            IExpressionNode typeNode = parser.type();
            if (typeNode instanceof IFunctionTypeExpressionNode)
            {
                funcTypeExpr = (FunctionTypeExpressionNode) typeNode;
                ContainerNode containerNode = new ContainerNode();
                containerNode.addChild(funcTypeExpr);

                ScopedBlockNode container = new ScopedBlockNode();
                container.setScope(scope);
                container.addChild(funcTypeExpr);
                container.normalize(true);
            }
            else
            {
                throw new Exception("Invalid function type expression: " + signature);
            }
        }
        catch (Exception e)
        {
            problems.add(new UnexpectedExceptionProblem(e));
        }
        finally
        {
            IOUtils.closeQuietly(tokenizer);
        }
        return funcTypeExpr;
    }

    private static ExpressionNodeBase createTypeNodeFromParameterDefinition(IParameterDefinition paramDef, IFunctionDefinition funcDef, ASScope scope, ICompilerProject project)
    {
        if (paramDef == null || funcDef == null)
        {
            return createTypeNodeFromTypeDefinition(project.getBuiltinType(BuiltinType.ANY_TYPE));
        }

        // we can't add metadata directly to parameters, so we
        // need to find it on the function that contains the
        // parameter
        String signature = getSignatureFromDefinition(funcDef, paramDef.getBaseName());
        if (signature != null)
        {
            return FunctionTypeExpressionNode.parseSignature(signature, scope, paramDef, project, project.getProblems());
        }

        ITypeDefinition paramType = paramDef.resolveType(project);
        if (paramType == null)
        {
            return createTypeNodeFromTypeDefinition(project.getBuiltinType(BuiltinType.ANY_TYPE));
        }
        return createTypeNodeFromTypeDefinition(paramType);
    }

    private static ExpressionNodeBase createReturnTypeNodeFromFunctionDefinition(IFunctionDefinition funcDef, ASScope scope, ICompilerProject project)
    {
        if (funcDef == null)
        {
            return createTypeNodeFromTypeDefinition(project.getBuiltinType(BuiltinType.ANY_TYPE));
        }

        String signature = getSignatureFromDefinition(funcDef);
        if (signature != null)
        {
            return FunctionTypeExpressionNode.parseSignature(signature, scope, funcDef, project, project.getProblems());
        }

        ITypeDefinition returnType = funcDef.resolveReturnType(project);
        if (returnType == null)
        {
            return createTypeNodeFromTypeDefinition(project.getBuiltinType(BuiltinType.ANY_TYPE));
        }
        return createTypeNodeFromTypeDefinition(returnType);
    }

    private static ExpressionNodeBase createTypeNodeFromTypeDefinition(ITypeDefinition typeDefinition)
    {
        String qualifiedName = typeDefinition.getQualifiedName();
        if (qualifiedName.indexOf('.') == -1)
        {
            return new IdentifierNode(qualifiedName);
        }

        ExpressionNodeBase result = null;
        String[] parts = qualifiedName.split("\\.");
        for (String part : parts)
        {
            IdentifierNode current = new IdentifierNode(part);
            if (result == null)
            {
                result = current;
            }
            else
            {
                ASToken memberToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_MEMBER_ACCESS, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, ".");
                result = new FullNameNode(result, memberToken, current);
            }
        }
        return result;
    }

    private ContainerNode argumentsNode;
    private ExpressionNodeBase returnTypeNode;

    /**
     * Constructor.
     */
    public FunctionTypeExpressionNode()
    {
        argumentsNode = new ContainerNode(2);
        argumentsNode.setContainerType(ContainerType.PARENTHESIS);
    }

    @Override
    public ContainerNode getParametersContainerNode()
    {
        return argumentsNode;
    }

    @Override
    public IParameterNode[] getParameterNodes()
    {
        IParameterNode[] variables = {};
        
        int argumentsCount = argumentsNode.getChildCount();
        variables = new IParameterNode[argumentsCount];
        for (int i = 0; i < argumentsCount; i++)
        {
            IASNode argument = argumentsNode.getChild(i);
            if (argument instanceof IParameterNode)
                variables[i] = (IParameterNode)argument;
        }
        
        return variables;
    }
    
    @Override
    public String getReturnType()
    {
        if(returnTypeNode != null)
        {
            IIdentifierNode identifierNode = null;
            if(returnTypeNode instanceof IIdentifierNode)
            {
                identifierNode = (IIdentifierNode) returnTypeNode;
            }
            else if(returnTypeNode instanceof INamespaceAccessExpressionNode)
            {
                INamespaceAccessExpressionNode namespaceAccess = (INamespaceAccessExpressionNode) returnTypeNode;
                IExpressionNode rightOperandNode = namespaceAccess.getRightOperandNode();
                if (rightOperandNode instanceof IIdentifierNode) 
                {
                    identifierNode = (IIdentifierNode) rightOperandNode;
                }
            }
            if (identifierNode != null)
            {
                return identifierNode.getName();
            }
        }
        return "";
    }

    @Override
    public IExpressionNode getReturnTypeNode()
    {
        return returnTypeNode;
    }

    /**
     * Set the return type node. Used during parsing.
     * 
     * @param variableType node containing the variable type
     */
    public void setReturnType(ExpressionNodeBase variableType)
    {
        returnTypeNode = variableType;
    }

    @Override
    public String resolveSignature(ICompilerProject project)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        IParameterNode[] paramNodes = getParameterNodes();
        for (int i = 0; i < paramNodes.length; i++)
        {
            IParameterNode paramNode = paramNodes[i];
            if (i > 0)
            {
                sb.append(OperatorType.COMMA.getOperatorText());
            }
            if (paramNode.isRest())
            {
                sb.append(IASLanguageConstants.REST);
            }
            sb.append(paramNode.getShortName());
            if (paramNode.hasDefaultValue())
            {
                sb.append(OperatorType.CONDITIONAL.getOperatorText());
            }
            sb.append(":");
            IExpressionNode paramTypeNode = paramNode.getVariableTypeNode();
            if (paramTypeNode instanceof IFunctionTypeExpressionNode)
            {
                IFunctionTypeExpressionNode funcTypeExprNode = (IFunctionTypeExpressionNode) paramTypeNode;
                sb.append(funcTypeExprNode.resolveSignature(project));
            }
            else if (paramTypeNode != null)
            {
                IDefinition paramType = paramTypeNode.resolve(project);
                if (paramType != null)
                {
                    sb.append(paramType.getQualifiedName());
                }
                else
                {
                    sb.append(IASLanguageConstants.ANY_TYPE);
                }
            }
            else
            {
                sb.append(IASLanguageConstants.ANY_TYPE);
            }
        }
        sb.append(")=>");
        if (returnTypeNode instanceof IFunctionTypeExpressionNode)
        {
            IFunctionTypeExpressionNode funcTypeExprNode = (IFunctionTypeExpressionNode) returnTypeNode;
            sb.append(funcTypeExprNode.resolveSignature(project));
        }
        else if (returnTypeNode != null)
        {
            IDefinition returnType = returnTypeNode.resolve(project);
            if (returnType != null)
            {
                sb.append(returnType.getQualifiedName());
            }
            else
            {
                sb.append(IASLanguageConstants.ANY_TYPE);
            }
        }
        else
        {
            sb.append(IASLanguageConstants.ANY_TYPE);
        }
        return sb.toString();
    }

    //
    // NodeBase overrides
    //

    @Override
    public ASTNodeID getNodeID()
    {
        return ASTNodeID.FunctionTypeExpressionID;
    }

    @Override
    public boolean isTerminal()
    {
        return true;
    }

    /*
     * For debugging only. Builds a string such as <code>() => void</code> from
     * the function type expression parts.
     */
    @Override
    protected boolean buildInnerString(StringBuilder sb)
    {
        sb.append(getBasicSignature());
        return true;
    }

    @Override
    protected void setChildren(boolean fillInOffsets)
    {
        if (argumentsNode != null)
        {
            argumentsNode.setParent(this);
            argumentsNode.normalize(fillInOffsets);
        }

        if (returnTypeNode != null)
        {
            returnTypeNode.setParent(this);
            returnTypeNode.normalize(fillInOffsets);
        }
    }

    //
    // ExpressionNodeBase overrides
    //

    @Override
    public IDefinition resolve(ICompilerProject project)
    {
        return project.getBuiltinType(BuiltinType.FUNCTION);
    }

    @Override
    public ITypeDefinition resolveType(ICompilerProject project)
    {
        IDefinition def = resolve(project);
        if (def == null)
        {
            return null;
        }
        return def.resolveType(project);
    }
    
    @Override
    protected FunctionTypeExpressionNode copy()
    {
        return null;
    }

    @Override
    String computeSimpleReference()
    {
        return IASLanguageConstants.Function;
    }
    
    @Override
    IReference computeTypeReference()
    {
        IWorkspace w = getWorkspace();
        return ReferenceFactory.lexicalReference(w, computeSimpleReference());
    }

    @Override
    public INamespaceReference computeNamespaceReference()
    {
        return NamespaceDefinition.createNamespaceReference(getASScope(), IASLanguageConstants.Function, null);
    }
    
    private String getBasicSignature()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        IParameterNode[] paramNodes = getParameterNodes();
        for (int i = 0; i < paramNodes.length; i++)
        {
            IParameterNode paramNode = paramNodes[i];
            if (i > 0)
            {
                sb.append(OperatorType.COMMA.getOperatorText());
            }
            if (paramNode.isRest())
            {
                sb.append(IASLanguageConstants.REST);
            }
            sb.append(paramNode.getShortName());
            if (paramNode.hasDefaultValue())
            {
                sb.append(OperatorType.CONDITIONAL.getOperatorText());
            }
            sb.append(":");
            IExpressionNode paramTypeNode = paramNode.getVariableTypeNode();
            if (paramTypeNode instanceof FunctionTypeExpressionNode)
            {
                FunctionTypeExpressionNode funcTypeExprNode = (FunctionTypeExpressionNode) paramTypeNode;
                sb.append(funcTypeExprNode.getBasicSignature());
            }
            else
            {
                sb.append(paramNode.getVariableType());
            }
        }
        sb.append(")=>");
        if (returnTypeNode instanceof FunctionTypeExpressionNode)
        {
            FunctionTypeExpressionNode funcTypeExprNode = (FunctionTypeExpressionNode) returnTypeNode;
            sb.append(funcTypeExprNode.getBasicSignature());
        }
        else
        {
            sb.append(getReturnType());
        }
        return sb.toString();
    }
}
