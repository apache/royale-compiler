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

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Checks for uses of the * type.
 */
public class AnyTypeRule extends LinterRule {
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
		if (!isAnyType(typeNode)) {
			return;
		}
		problems.add(new AnyTypeOnVariableLinterProblem(variableNode));
	}

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		for (IParameterNode paramNode : functionNode.getParameterNodes()) {
			IExpressionNode typeNode = paramNode.getVariableTypeNode();
			if (isAnyType(typeNode)) {
				problems.add(new AnyTypeOnParameterLinterProblem(paramNode));
			}
		}
		if (functionNode.isConstructor()) {
			return;
		}
		IExpressionNode typeNode = functionNode.getReturnTypeNode();
		if (isAnyType(typeNode)) {
			problems.add(new AnyTypeReturnLinterProblem(functionNode));
		}
	}

	private boolean isAnyType(IExpressionNode typeNode) {
		if (!(typeNode instanceof IIdentifierNode)) {
			return false;
		}
		// isImplicit() is not on the interface, for some reason
		if (typeNode instanceof IdentifierNode && ((IdentifierNode) typeNode).isImplicit()) {
			return false;
		}
		IIdentifierNode identifierNode = (IIdentifierNode) typeNode;
		if (!IASLanguageConstants.ANY_TYPE.equals(identifierNode.getName())) {
			return false;
		}
		return true;
	}

	public static class AnyTypeOnVariableLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Must not use the * type for variable '${varName}'";

		public AnyTypeOnVariableLinterProblem(IVariableNode node)
		{
			super(node.getVariableTypeNode());
			varName = node.getName();
		}

		public String varName;
	}

	public static class AnyTypeOnParameterLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Must not use the * type for function parameter '${paramName}'";

		public AnyTypeOnParameterLinterProblem(IParameterNode node)
		{
			super(node.getVariableTypeNode());
			paramName = node.getName();
		}

		public String paramName;
	}

	public static class AnyTypeReturnLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Must not use the * type for function return type";

		public AnyTypeReturnLinterProblem(IFunctionNode node)
		{
			super(node.getReturnTypeNode());
		}
	}
}
