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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that calls to event dispatcher methods don't use string values for
 * event names.
 */
public class NoStringEventNameRule extends LinterRule {
	private static final String[] EVENT_DISPATCHER_FUNCTION_NAMES = {
		"addEventListener",
		"removeEventListener",
		"hasEventListener"
	};

	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.FunctionCallID, (node, tokenQuery, problems) -> {
			checkFunctionCallNode((IFunctionCallNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkFunctionCallNode(IFunctionCallNode functionCallNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		String functionName = null;
		IExpressionNode nameNode = functionCallNode.getNameNode();
		if (nameNode instanceof IIdentifierNode) {
			IIdentifierNode identifierNode = (IIdentifierNode) nameNode;
			functionName = identifierNode.getName();
		}
		else if(nameNode instanceof IMemberAccessExpressionNode) {
			IMemberAccessExpressionNode memberAccessNode = (IMemberAccessExpressionNode) nameNode;
			IExpressionNode rightNode = memberAccessNode.getRightOperandNode();
			if (rightNode instanceof IIdentifierNode) {
				IIdentifierNode identifierNode = (IIdentifierNode) rightNode;
				functionName = identifierNode.getName();
			}
		}
		if (functionName == null) {
			return;
		}
		if (!Arrays.asList(EVENT_DISPATCHER_FUNCTION_NAMES).contains(functionName)) {
			return;
		}
		IExpressionNode[] argumentNodes = functionCallNode.getArgumentNodes();
		if (argumentNodes.length == 0) {
			return;
		}
		IExpressionNode firstArgument = argumentNodes[0];
		if (!(firstArgument instanceof ILiteralNode)) {
			return;
		}
		ILiteralNode literalNode = (ILiteralNode) firstArgument;
		if (!LiteralType.STRING.equals(literalNode.getLiteralType())) {
			return;
		}
		problems.add(new NoStringEventNameLinterProblem(functionCallNode, functionName));
	}

	public static class NoStringEventNameLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Calls to '${functionName}' must use constant value instead of string literal for event name";

		public NoStringEventNameLinterProblem(IFunctionCallNode node, String functionName)
		{
			super(node.getArgumentNodes()[0]);
			this.functionName = functionName;
		}

		public String functionName;
	}
}
