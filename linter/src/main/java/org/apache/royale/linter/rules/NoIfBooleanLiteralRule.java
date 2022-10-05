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

import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIfNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that a boolean literal value is not used as an 'if' condition.
 */
public class NoIfBooleanLiteralRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.IfStatementID, (node, tokenQuery, problems) -> {
			checkIfNode((IIfNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkIfNode(IIfNode ifNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IExpressionNode conditionalNode = ifNode.getConditionalExpressionNode();
		if (!(conditionalNode instanceof ILiteralNode)) {
			return;
		}
		ILiteralNode literalNode = (ILiteralNode) conditionalNode;
		if (!LiteralType.BOOLEAN.equals(literalNode.getLiteralType())) {
			return;
		}
		problems.add(new NoIfBooleanLiteralLinterProblem(literalNode));
	}

	public static class NoIfBooleanLiteralLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Condition is always '${value}'";

		public NoIfBooleanLiteralLinterProblem(ILiteralNode node)
		{
			super(node);
			value = node.getValue();
		}

		public String value;
	}
}
