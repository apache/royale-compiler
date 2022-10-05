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
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that symbols in package or class scopes have a namespace.
 */
public class NoDynamicClassRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.ClassID, (node, tokenQuery, problems) -> {
			checkClassNode((IClassNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkClassNode(IClassNode classNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (!classNode.hasModifier(ASModifier.DYNAMIC)) {
			return;
		}
		problems.add(new NoDynamicClassLinterProblem(classNode));
	}

	public static class NoDynamicClassLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Class '${className}' must not be dynamic";

		public NoDynamicClassLinterProblem(IClassNode node)
		{
			super(node);
			className = node.getName();
		}

		public String className;
	}
}
