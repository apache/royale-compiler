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
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Check that the left side of in, is, and instanceof is not negated unsafely.
 */
public class UnsafeNegationRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.Op_InID, (node, tokenQuery, problems) -> {
			checkBinaryOperatorNode((IBinaryOperatorNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.Op_IsID, (node, tokenQuery, problems) -> {
			checkBinaryOperatorNode((IBinaryOperatorNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.Op_InstanceOfID, (node, tokenQuery, problems) -> {
			checkBinaryOperatorNode((IBinaryOperatorNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkBinaryOperatorNode(IBinaryOperatorNode binaryOperatorNode, TokenQuery tokenQuery,
			Collection<ICompilerProblem> problems) {
		IExpressionNode leftSide = binaryOperatorNode.getLeftOperandNode();
		if (!(leftSide instanceof IUnaryOperatorNode)) {
			return;
		}
		IUnaryOperatorNode unaryOpertor = (IUnaryOperatorNode) leftSide;
		if (!OperatorType.LOGICAL_NOT.equals(unaryOpertor.getOperator())) {
			return;
		}
		problems.add(new UnsafeNegationLinterProblem(binaryOperatorNode));
	}

	public static class UnsafeNegationLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Must not use negation on left side of in, is, and instanceof operators. Did you mean to use parentheses?";

		public UnsafeNegationLinterProblem(IASNode node) {
			super(node);
		}
	}
}
