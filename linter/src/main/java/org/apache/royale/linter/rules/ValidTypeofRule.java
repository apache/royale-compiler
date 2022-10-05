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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that an array literal contains no empty slots (multiple repeating
 * commas with no values).
 */
public class ValidTypeofRule extends LinterRule {
	private static final Set<String> VALID_RESULTS = new HashSet<>();
	{
		VALID_RESULTS.add("boolean");
		VALID_RESULTS.add("function");
		VALID_RESULTS.add("number");
		VALID_RESULTS.add("object");
		VALID_RESULTS.add("string");
		VALID_RESULTS.add("xml");
		VALID_RESULTS.add("undefined");
	}

	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.Op_TypeOfID, (node, tokenQuery, problems) -> {
			checkUnaryOperatorNode((IUnaryOperatorNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkUnaryOperatorNode(IUnaryOperatorNode typeofNode, TokenQuery tokenQuery,
			Collection<ICompilerProblem> problems) {
		if (!OperatorType.TYPEOF.equals(typeofNode.getOperator())) {
			return;
		}
		IASNode parentNode = typeofNode.getParent();
		if (!(parentNode instanceof IBinaryOperatorNode)) {
			return;
		}
		IBinaryOperatorNode binaryOperatorNode = (IBinaryOperatorNode) parentNode;
		OperatorType parentOperatorType = binaryOperatorNode.getOperator();
		if (!OperatorType.EQUAL.equals(parentOperatorType)
				&& !OperatorType.NOT_EQUAL.equals(parentOperatorType)
				&& !OperatorType.STRICT_EQUAL.equals(parentOperatorType)
				&& !OperatorType.STRICT_NOT_EQUAL.equals(parentOperatorType)) {
			return;
		}
		IExpressionNode otherNode = binaryOperatorNode.getLeftOperandNode() == typeofNode
				? binaryOperatorNode.getRightOperandNode()
				: binaryOperatorNode.getLeftOperandNode();
		if (!(otherNode instanceof ILiteralNode)) {
			return;
		}
		ILiteralNode stringLiteral = (ILiteralNode) otherNode;
		if (!LiteralType.STRING.equals(stringLiteral.getLiteralType())) {
			return;
		}
		if (VALID_RESULTS.contains(stringLiteral.getValue())) {
			return;
		}
		problems.add(new ValidTypeofLinterProblem(stringLiteral));
	}

	public static class ValidTypeofLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "String '${value}' is not a valid result for typeof operator";

		public ValidTypeofLinterProblem(ILiteralNode node) {
			super(node);
			value = node.getValue();
		}

		public String value;
	}
}
