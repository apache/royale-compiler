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

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.parsing.as.ASToken;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.internal.tree.as.parts.FunctionContentsPart;
import org.apache.royale.compiler.tree.as.IArrowFunctionBindNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IFunctionObjectNode;

/**
 * ActionScript parse tree node representing a function type expression.
 */
public class ArrowFunctionBindNode extends FunctionCallNode implements IArrowFunctionBindNode
{
    private FunctionObjectNode arrowFunction;

    /**
     * Constructor.
     */
    public ArrowFunctionBindNode(FunctionObjectNode arrowFunction)
    {
        super(createOuterBindFunction(arrowFunction));
        this.arrowFunction = arrowFunction;
        span(arrowFunction);

        ContainerNode args = getArgumentsNode();
        args.addItem(LanguageIdentifierNode.buildThis());
        args.addItem(arrowFunction);
        args.span(arrowFunction);
    }

    @Override
    public IFunctionObjectNode getFunctionObjectNode()
    {
        return arrowFunction;
    }

    private static FunctionObjectNode createOuterBindFunction(FunctionObjectNode arrowFunction)
    {
        FunctionContentsPart contentsPart = new FunctionContentsPart();

        // some tokens and nodes must have a valid location
        // just place them before the source location
        int start = arrowFunction.getAbsoluteStart();
        int end = start;
        int line = arrowFunction.getLine();
        int column = arrowFunction.getColumn();

        ContainerNode paramsContainerNode = contentsPart.getParametersNode();

        // context:* parameter
        IdentifierNode contextNameNode = new IdentifierNode("context");
        ParameterNode contextParamNode = new ParameterNode(contextNameNode);
        ASToken contextColonToken = new ASToken(ASTokenTypes.TOKEN_COLON, start, end, line, column, ":");
        ASToken starToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_STAR, start, end, line, column, IASLanguageConstants.ANY_TYPE);
        LanguageIdentifierNode starTypeNode = LanguageIdentifierNode.buildAnyType(starToken);
        contextParamNode.setType(contextColonToken, starTypeNode);
        paramsContainerNode.addChild(contextParamNode);
        
        // func:Function parameter
        ASToken funcColonToken = new ASToken(ASTokenTypes.TOKEN_COLON, start, end, line, column, ":");
        IdentifierNode funcNameNode = new IdentifierNode("func");
        ParameterNode funcParamNode = new ParameterNode(funcNameNode);
        IdentifierNode funcTypeNode = new IdentifierNode("Function");
        funcParamNode.setType(funcColonToken, funcTypeNode);
        paramsContainerNode.addChild(funcParamNode);

        IdentifierNode functionNameNode = new IdentifierNode("");
        functionNameNode.startBefore(arrowFunction);
        functionNameNode.endBefore(arrowFunction);
        FunctionNode functionNode = new FunctionNode(functionNameNode, contentsPart);

        // Function return type
        ASToken colonToken = new ASToken(ASTokenTypes.TOKEN_COLON, start, end, line, column, ":");
        IdentifierNode funcReturnTypeNode = new IdentifierNode("Function");
        functionNode.setType(colonToken, funcReturnTypeNode);

        BlockNode body = functionNode.getScopedNode();
        body.setContainerType(IContainerNode.ContainerType.BRACES);

        FunctionObjectNode innerBindFunction = createInnerBindFunction("func", "context", arrowFunction);

        // return funcName.apply(context, rest)
        ASToken returnToken = new ASToken(ASTokenTypes.TOKEN_KEYWORD_RETURN, start, end, line, column, IASKeywordConstants.RETURN);
        ReturnNode returnNode = new ReturnNode(returnToken);
        returnNode.setStatementExpression(innerBindFunction);
        body.addItem(returnNode);

        return new FunctionObjectNode(functionNode);
    }

    private static FunctionObjectNode createInnerBindFunction(String funcName, String contextName, ISourceLocation sourceLocation)
    {
        FunctionContentsPart contentsPart = new FunctionContentsPart();

        // some tokens and nodes must have a valid location
        // just place them before the source location
        int start = sourceLocation.getAbsoluteStart();
        int end = start;
        int line = sourceLocation.getLine();
        int column = sourceLocation.getColumn();

        // ...rest parameter
        ContainerNode paramsContainerNode = contentsPart.getParametersNode();
        IdentifierNode restNameNode = new IdentifierNode(IASLanguageConstants.REST_IDENTIFIER);
        ParameterNode restParamNode = new ParameterNode(restNameNode);
        restParamNode.setIsRestParameter(true);
        paramsContainerNode.addChild(restParamNode);

        IdentifierNode functionNameNode = new IdentifierNode("");
        functionNameNode.startBefore(sourceLocation);
        functionNameNode.endBefore(sourceLocation);
        FunctionNode functionNode = new FunctionNode(functionNameNode, contentsPart);

        // * return type
        ASToken starToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_STAR, start, end, line, column, IASLanguageConstants.ANY_TYPE);
        LanguageIdentifierNode anyTypeNode = LanguageIdentifierNode.buildAnyType(starToken);
        ASToken colonToken = new ASToken(ASTokenTypes.TOKEN_COLON, start, end, line, column, ":");
        functionNode.setType(colonToken, anyTypeNode);

        BlockNode body = functionNode.getScopedNode();
        body.setContainerType(IContainerNode.ContainerType.BRACES);

        // funcName.apply(context, rest)
        ASToken memberAccessToken = new ASToken(ASTokenTypes.TOKEN_OPERATOR_MEMBER_ACCESS, start, end, line, column, ".");
        IdentifierNode funcNameNode = new IdentifierNode(funcName);
        IdentifierNode applyNameNode = new IdentifierNode("apply");
        MemberAccessExpressionNode memberAccessNode = new MemberAccessExpressionNode(funcNameNode, memberAccessToken, applyNameNode);
        FunctionCallNode functionCallNode = new FunctionCallNode(memberAccessNode);
        ContainerNode args = functionCallNode.getArgumentsNode();
        args.addItem(new IdentifierNode(contextName));
        args.addItem(new IdentifierNode(IASLanguageConstants.REST_IDENTIFIER));

        // return funcName.apply(context, rest)
        ASToken returnToken = new ASToken(ASTokenTypes.TOKEN_KEYWORD_RETURN, start, end, line, column, IASKeywordConstants.RETURN);
        ReturnNode returnNode = new ReturnNode(returnToken);
        returnNode.setStatementExpression(functionCallNode);
        body.addItem(returnNode);

        return new FunctionObjectNode(functionNode);
    }
}
