////////////////////////////////////////////////////////////////////////////////
//
//  Licensed to the Apache Software Foundation (ASF) under one or more
//  contributor license agreements.  See the NOTICE file distributed with
//  this work for additional information regarding copyright ownership.
//  The ASF licenses this file to You under the Apache License, Version 2.0
//  (the "License"); you may not use this file except in compliance with
//  the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package org.apache.royale.linter.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Checks that a type has been declared for all variables, function parameters,
 * and function returns.
 */
public class MissingTypeRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.VariableID, (node, tokenQuery, problems) -> {
			checkVariableNode((IVariableNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkFunctionNode((IFunctionNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkVariableNode(IVariableNode variableNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IExpressionNode typeNode = variableNode.getVariableTypeNode();
		if (isValidTypeNode(typeNode)) {
			return;
		}
		problems.add(new MissingVariableTypeLinterProblem(variableNode));
	}

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		for (IParameterNode paramNode : functionNode.getParameterNodes()) {
			if (paramNode.isRest()) {
				//it's okay for a rest parameter not to have a type
				continue;
			}
			IExpressionNode typeNode = paramNode.getVariableTypeNode();
			if (!isValidTypeNode(typeNode)) {
				problems.add(new MissingFunctionParameterTypeLinterProblem(paramNode));
			}
		}
		if (functionNode.isConstructor()) {
			return;
		}
		IExpressionNode typeNode = functionNode.getReturnTypeNode();
		if (!isValidTypeNode(typeNode)) {
			problems.add(new MissingFunctionReturnTypeLinterProblem(functionNode));
		}
	}

	private boolean isValidTypeNode(IExpressionNode typeNode) {
		if (typeNode == null) {
			return false;
		}
		if (typeNode instanceof IdentifierNode) {
			IdentifierNode identifierNode = (IdentifierNode) typeNode;
			// isImplicit() is not on the interface, for some reason
			if (identifierNode.isImplicit()) {
				return false;
			}
		}
		return true;
	}

	public static class MissingVariableTypeLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Missing type for variable '${varName}'";

		public MissingVariableTypeLinterProblem(IVariableNode node)
		{
			super(node);
			varName = node.getName();
		}

		public String varName;
	}

	public static class MissingFunctionParameterTypeLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Missing type for function parameter '${paramName}'";

		public MissingFunctionParameterTypeLinterProblem(IParameterNode node)
		{
			super(node);
			paramName = node.getName();
		}

		public String paramName;
	}

	public static class MissingFunctionReturnTypeLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Missing function return type";

		public MissingFunctionReturnTypeLinterProblem(IFunctionNode node)
		{
			super(node);
		}
	}
}
