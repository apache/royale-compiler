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
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that all constructors include a call to super().
 */
public class MissingConstructorSuperRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkFunctionNode((IFunctionNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (!functionNode.isConstructor()) {
			return;
		}
		if (hasSuperCall(functionNode.getScopedNode())) {
			return;
		}
		problems.add(new MissingConstructorSuperLinterProblem(functionNode));
	}

	private boolean hasSuperCall(IASNode node) {
		if (node instanceof IFunctionCallNode) {
			IFunctionCallNode functionCallNode = (IFunctionCallNode) node;
			IExpressionNode nameNode = functionCallNode.getNameNode();
			if (nameNode instanceof ILanguageIdentifierNode) {
				ILanguageIdentifierNode identifierNode = (ILanguageIdentifierNode) nameNode;
				if (LanguageIdentifierKind.SUPER.equals(identifierNode.getKind())) {
					return true;
				}
			}
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			IASNode child = node.getChild(i);
			if (hasSuperCall(child)) {
				return true;
			}
		}
		return false;
	}

	public static class MissingConstructorSuperLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Constructor '${functionName}' does not include 'super()' call";

		public MissingConstructorSuperLinterProblem(IFunctionNode node)
		{
			super(node.getNameExpressionNode());
			functionName = node.getName();
		}

		public String functionName;
	}
}
