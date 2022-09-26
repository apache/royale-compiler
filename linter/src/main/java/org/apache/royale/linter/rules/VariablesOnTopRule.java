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
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Check that variables are always declared at the top of a function.
 */
public class VariablesOnTopRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkFunctionNode((IFunctionNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IScopedNode scopedNode = functionNode.getScopedNode();
		if (scopedNode == null) {
			return;
		}
		boolean afterNonVariable = false;
		for (int i = 0; i < scopedNode.getChildCount(); i++) {
			IASNode child = scopedNode.getChild(i);
			afterNonVariable = checkVariablesNotAtTop(child, afterNonVariable, problems);
		}
	}

	private boolean checkVariablesNotAtTop(IASNode node, boolean afterNonVariable, Collection<ICompilerProblem> problems) {
		if (node instanceof IVariableNode) {
			IVariableNode variableNode = (IVariableNode) node;
			if (afterNonVariable) {
				problems.add(new VariablesOnTopLinterProblem(variableNode));
			}
			return afterNonVariable;
		}
		afterNonVariable = true;
		for (int i = 0; i < node.getChildCount(); i++) {
			IASNode child = node.getChild(i);
			afterNonVariable = checkVariablesNotAtTop(child, afterNonVariable, problems);
		}
		return afterNonVariable;
	}

	public static class VariablesOnTopLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Variable name '${varName}' must be declared at the top of this function";

		public VariablesOnTopLinterProblem(IVariableNode node)
		{
			super(node.getNameExpressionNode());
			varName = node.getName();
		}

		public String varName;
	}
}
