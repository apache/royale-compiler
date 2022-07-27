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
import org.apache.royale.compiler.tree.as.IBlockNode;
import org.apache.royale.compiler.tree.as.IConditionalNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IStatementNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Checks the number of nested blocks in a function.
 */
public class MaxBlockDepthRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkFunctionNode((IFunctionNode) node, tokenQuery, problems);
		});
		return result;
	}

	public int maximum = 4;

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		int maxBlockDepth = getMaxDepth(functionNode.getScopedNode());
		if (maxBlockDepth <= maximum) {
			return;
		}
		problems.add(new MaxBlockDepthLinterProblem(functionNode, maxBlockDepth, maximum));
	}

	private int getMaxDepth(IASNode node) {
		int maxDepth = 0;
		for(int i = 0; i < node.getChildCount(); i++) {
			IASNode child = node.getChild(i);
			if (child instanceof IFunctionNode) {
				// nested functions don't count
				continue;
			}
			if (child instanceof IStatementNode) {
				IStatementNode statementNode = (IStatementNode) child;
				IASNode possibleBlock = null;
				if (statementNode.getChildCount() > 0) {
					possibleBlock = statementNode.getChild(statementNode.getChildCount() - 1);
					if (possibleBlock instanceof IConditionalNode) {
						IConditionalNode conditionalNode = (IConditionalNode) possibleBlock;
						if (conditionalNode.getChildCount() > 0) {
							possibleBlock = conditionalNode.getChild(conditionalNode.getChildCount() - 1);
						}
					}
				}
				if (possibleBlock instanceof IBlockNode) {
					int childMaxDepth = getMaxDepth(possibleBlock) + 1;
					if (maxDepth < childMaxDepth) {
						maxDepth = childMaxDepth;
					}
				}
			}
			else if (child instanceof IBlockNode) {
				int childMaxDepth = getMaxDepth(child) + 1;
				if (maxDepth < childMaxDepth) {
					maxDepth = childMaxDepth;
				}
			}
		}
		return maxDepth;
	}

	public static class MaxBlockDepthLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Function '${functionName}' has blocks nested ${depth} levels deep, but expected no deeper than ${maxDepth} levels";

		public MaxBlockDepthLinterProblem(IFunctionNode node, int depth, int maxDepth)
		{
			super(node.getParametersContainerNode());
			this.depth = depth;
			this.maxDepth = maxDepth;
			functionName = node.getName();
		}

		public String functionName;
		public int depth;
		public int maxDepth;
	}
}
