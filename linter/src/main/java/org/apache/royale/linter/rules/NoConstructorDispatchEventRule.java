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
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Check that a constructor does not call `dispatchEvent` because it's likely
 * that no listeners have been added yet.
 */
public class NoConstructorDispatchEventRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.FunctionCallID, (node, tokenQuery, problems) -> {
			checkFunctionCallNode((IFunctionCallNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkFunctionCallNode(IFunctionCallNode functionCallNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IFunctionNode functionNode = (IFunctionNode) functionCallNode.getAncestorOfType(IFunctionNode.class);
		if (functionNode == null || !functionNode.isConstructor()) {
			return;
		}
		IExpressionNode nameNode = functionCallNode.getNameNode();
		if (nameNode instanceof IIdentifierNode) {
			IIdentifierNode identifierNode = (IIdentifierNode) nameNode;
			if ("dispatchEvent".equals(identifierNode.getName())) {
				problems.add(new NoConstructorDispatchEventLinterProblem(functionNode, identifierNode));
				return;
			}
			return;
		}
		if (nameNode instanceof IMemberAccessExpressionNode) {
			IMemberAccessExpressionNode memberAccess = (IMemberAccessExpressionNode) nameNode;
			if (memberAccess.getRightOperandNode() instanceof IIdentifierNode) {
				IIdentifierNode identifierNode = (IIdentifierNode) memberAccess.getRightOperandNode();
				if ("dispatchEvent".equals(identifierNode.getName())) {
					if (memberAccess.getLeftOperandNode() instanceof ILanguageIdentifierNode) {
						ILanguageIdentifierNode langIdentifierNode = (ILanguageIdentifierNode) memberAccess.getLeftOperandNode();
						if (LanguageIdentifierKind.THIS.equals(langIdentifierNode.getKind())
								|| LanguageIdentifierKind.SUPER.equals(langIdentifierNode.getKind())) {
							problems.add(new NoConstructorDispatchEventLinterProblem(functionNode, identifierNode));
							return;
						}
					}
				}
			}
			return;
		}
	}

	public static class NoConstructorDispatchEventLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Constructor '${functionName}' must not call 'dispatchEvent'";

		public NoConstructorDispatchEventLinterProblem(IFunctionNode functionNode, IExpressionNode dispatchEventNode) {
			super(dispatchEventNode);
			functionName = functionNode.getName();
		}

		public String functionName;
	}
}
