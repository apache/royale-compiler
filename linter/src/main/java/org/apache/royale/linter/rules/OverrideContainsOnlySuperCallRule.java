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

import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode;
import org.apache.royale.compiler.tree.as.ILanguageIdentifierNode.LanguageIdentifierKind;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that an overridden function contains more than a call to super.
 */
public class OverrideContainsOnlySuperCallRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkFunctionNode((IFunctionNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (!functionNode.hasModifier(ASModifier.OVERRIDE)) {
			return;
		}
		IScopedNode scopedNode = functionNode.getScopedNode();
		
		if (scopedNode.getChildCount() == 0 || scopedNode.getChildCount() > 1) {
			return;
		}
		IASNode child = scopedNode.getChild(0);
		if (!(child instanceof IFunctionCallNode)) {
			return;
		}
		IFunctionCallNode functionCallNode = (IFunctionCallNode) child;
		IExpressionNode nameNode = functionCallNode.getNameNode();
		if (!(nameNode instanceof IMemberAccessExpressionNode)) {
			return;
		}
		IMemberAccessExpressionNode memberAccess = (IMemberAccessExpressionNode) nameNode;
		if (!(memberAccess.getLeftOperandNode() instanceof ILanguageIdentifierNode)) {
			return;
		}
		ILanguageIdentifierNode superNode = (ILanguageIdentifierNode) memberAccess.getLeftOperandNode();
		if (!LanguageIdentifierKind.SUPER.equals(superNode.getKind())) {
			return;
		}
		if (!(memberAccess.getRightOperandNode() instanceof IIdentifierNode)) {
			return;
		}
		IIdentifierNode rightNode = (IIdentifierNode) memberAccess.getRightOperandNode();
		if (!functionNode.getName().equals(rightNode.getName())) {
			return;
		}
		problems.add(new OverrideContainsOnlySuperCallLinterProblem(functionNode));
	}

	public static class OverrideContainsOnlySuperCallLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Method override '${functionName}' must contain more than call to 'super.${functionName}'";

		public OverrideContainsOnlySuperCallLinterProblem(IFunctionNode node)
		{
			super(node.getNameExpressionNode());
			functionName = node.getName();
		}

		public String functionName;
	}
}
