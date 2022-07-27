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
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

public class LocalVarShadowsFieldRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.VariableID, (node, tokenQuery, problems) -> {
			checkVariableNode((IVariableNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkVariableNode(IVariableNode variableNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IScopedNode containingScope = variableNode.getContainingScope();
		if (containingScope == null) {
			return;
		}
		IASNode possibleType = containingScope.getParent();
		if (possibleType instanceof ITypeNode || possibleType instanceof IPackageNode) {
			return;
		}
		IClassNode containingClass = (IClassNode) variableNode.getAncestorOfType(IClassNode.class);
		if (containingClass == null) {
			return;
		}
		IScopedNode classScope = containingClass.getScopedNode();
		for(int i = 0; i < classScope.getChildCount(); i++) {
			IASNode child = classScope.getChild(i);
			if (child instanceof IVariableNode) {
				IVariableNode childVar = (IVariableNode) child;
				if (variableNode.getName().equals(childVar.getName())) {
					problems.add(new LocalVarShadowsFieldLinterProblem(variableNode, containingClass));
					return;
				}
			}
		}
	}

	public static class LocalVarShadowsFieldLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Local variable '${varName}' has the same name as field in class '${className}'";

		public LocalVarShadowsFieldLinterProblem(IVariableNode variableNode, IClassNode classNode)
		{
			super(variableNode.getNameExpressionNode());
			varName = variableNode.getName();
			className = classNode.getName();
		}

		public String varName;
		public String className;
	}
}
