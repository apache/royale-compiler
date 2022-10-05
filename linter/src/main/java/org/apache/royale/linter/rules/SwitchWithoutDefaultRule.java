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
import org.apache.royale.compiler.tree.as.ISwitchNode;
import org.apache.royale.compiler.tree.as.ITerminalNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check for 'switch' statements that are missing a 'default' clause.
 */
public class SwitchWithoutDefaultRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.SwitchID, (node, tokenQuery, problems) -> {
			checkSwitchNode((ISwitchNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkSwitchNode(ISwitchNode switchNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		ITerminalNode defaultNode = switchNode.getDefaultNode();
		if (defaultNode != null) {
			return;
		}
		problems.add(new SwitchWithoutDefaultLinterProblem(switchNode));
	}

	public static class SwitchWithoutDefaultLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Missing 'default' clause in 'switch' statement";

		public SwitchWithoutDefaultLinterProblem(ISwitchNode node)
		{
			super(node);
		}
	}
}
