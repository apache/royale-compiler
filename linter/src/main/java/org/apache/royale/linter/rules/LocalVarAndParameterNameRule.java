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
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IParameterNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that local variable and parameter names match a specific pattern.
 */
public class LocalVarAndParameterNameRule extends LinterRule {
	public static final Pattern DEFAULT_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");

	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.VariableID, (node, tokenQuery, problems) -> {
			checkVariableNode((IVariableNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.ArgumentID, (node, tokenQuery, problems) -> {
			checkParameterNode((IParameterNode) node, tokenQuery, problems);
		});
		return result;
	}

	public Pattern pattern;

	private void checkVariableNode(IVariableNode variableNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (variableNode.isConst()) {
			return;
		}
		IScopedNode containingScope = variableNode.getContainingScope();
		if (containingScope == null) {
			return;
		}
		IASNode possibleType = containingScope.getParent();
		if (possibleType instanceof ITypeNode || possibleType instanceof IPackageNode) {
			return;
		}
		String variableName = variableNode.getName();
		Pattern thePattern = pattern;
		if (thePattern == null) {
			thePattern = DEFAULT_NAME_PATTERN;
		}
		Matcher matcher = thePattern.matcher(variableName);
		if (matcher.matches()) {
			return;
		}
		problems.add(new LocalVarNameLinterProblem(variableNode, thePattern));
	}

	private void checkParameterNode(IParameterNode paramNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		String variableName = paramNode.getName();
		Pattern thePattern = pattern;
		if (thePattern == null) {
			thePattern = DEFAULT_NAME_PATTERN;
		}
		Matcher matcher = thePattern.matcher(variableName);
		if (matcher.matches()) {
			return;
		}
		problems.add(new ParameterNameLinterProblem(paramNode, thePattern));
	}

	public static class LocalVarNameLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Variable name '${varName}' does not match the pattern '${pattern}'";

		public LocalVarNameLinterProblem(IVariableNode node, Pattern pattern)
		{
			super(node.getNameExpressionNode());
			this.pattern = pattern.toString();
			varName = node.getName();
		}

		public String pattern;
		public String varName;
	}

	public static class ParameterNameLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Parameter name '${paramName}' does not match the pattern '${pattern}'";

		public ParameterNameLinterProblem(IVariableNode node, Pattern pattern)
		{
			super(node.getNameExpressionNode());
			this.pattern = pattern.toString();
			paramName = node.getName();
		}

		public String pattern;
		public String paramName;
	}
}
