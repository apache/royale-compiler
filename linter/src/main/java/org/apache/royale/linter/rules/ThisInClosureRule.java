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

import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBlockNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Checks for use of the 'this' keyword in closures.
 */
public class ThisInClosureRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkFunctionNode((IFunctionNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IFunctionNode ancestorFunction = (IFunctionNode) functionNode.getAncestorOfType(IFunctionNode.class);
		if (ancestorFunction == null) {
			return;
		}
		IBlockNode blockNode = getBody(functionNode);
		if (blockNode == null) {
			return;
		}
		for(IASToken token : tokenQuery.getTokens(blockNode)) {
			if (token.getType() == ASTokenTypes.TOKEN_KEYWORD_THIS) {
				problems.add(new ThisInClosureLinterProblem(token));
			}
		}
	}

	private IBlockNode getBody(IFunctionNode functionNode) {
		if (functionNode.getChildCount() == 0) {
			return null;
		}
		IASNode lastChild = functionNode.getChild(functionNode.getChildCount() - 1);
		if (lastChild instanceof IBlockNode) {
			return (IBlockNode) lastChild;
		}
		return null;
	}

	public static class ThisInClosureLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Closure must not contain 'this' keyword";

		public ThisInClosureLinterProblem(IASToken token)
		{
			super(token);
		}
	}
}
