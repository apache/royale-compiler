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
import org.apache.royale.compiler.tree.as.IImportNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Check for import statements that import the entire package.
 */
public class WildcardImportRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.ImportID, (node, tokenQuery, problems) -> {
			checkImportNode((IImportNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkImportNode(IImportNode importNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (!importNode.isWildcardImport()) {
			return;
		}
		problems.add(new WildcardImportLinterProblem(importNode));
	}

	public static class WildcardImportLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Must not use wildcard import";

		public WildcardImportLinterProblem(IImportNode node)
		{
			super(node);
		}
	}
}
