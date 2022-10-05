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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that function names match a specific pattern.
 */
public class FunctionNameRule extends LinterRule {
	public static final Pattern DEFAULT_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");

	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkFunctionNode((IFunctionNode) node, tokenQuery, problems);
		});
		return result;
	}

	public Pattern pattern;

	private void checkFunctionNode(IFunctionNode functionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (functionNode.isConstructor()) {
			// checked by ClassNameRule
			return;
		}
		String functionName = functionNode.getName();
		if (functionName == null || functionName.length() == 0) {
			return;
		}
		Pattern thePattern = pattern;
		if (thePattern == null) {
			thePattern = DEFAULT_NAME_PATTERN;
		}
		Matcher matcher = thePattern.matcher(functionName);
		if (matcher.matches()) {
			return;
		}
		problems.add(new FunctionNameLinterProblem(functionNode, thePattern));
	}

	public static class FunctionNameLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Function name '${functionName}' does not match the pattern '${pattern}'";

		public FunctionNameLinterProblem(IFunctionNode node, Pattern pattern)
		{
			super(node.getNameExpressionNode());
			this.pattern = pattern.toString();
			functionName = node.getName();
		}

		public String pattern;
		public String functionName;
	}
}
