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
import org.apache.royale.compiler.tree.as.IBlockNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IPackageNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Checks for empty blocks, as long as they aren't the bodies of classes,
 * interfaces, or packages.
 */
public class EmptyNestedBlockRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.BlockID, (node, tokenQuery, problems) -> {
			checkBlockNode((IBlockNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkBlockNode(IBlockNode blockNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (!isNested(blockNode)) {
			return;
		}
		if (!isEmptyBlock(blockNode, tokenQuery)) {
			return;
		}
		problems.add(new EmptyNestedBlockLinterProblem(blockNode));
	}

	private boolean isNested(IBlockNode blockNode) {
		IASNode parentNode = blockNode.getParent();
		return parentNode != null
			&& !(parentNode instanceof IPackageNode)
			&& !(parentNode instanceof ITypeNode)
			&& !(parentNode instanceof IFunctionNode);
	}

	private boolean isEmptyBlock(IBlockNode blockNode, TokenQuery tokenQuery) {
		if (blockNode.getChildCount() > 0) {
			return false;
		}
		if (tokenQuery.getCommentsInside(blockNode).length > 0) {
			return false;
		}
		return true;
	}

	public static class EmptyNestedBlockLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Block must not be empty";

		public EmptyNestedBlockLinterProblem(IBlockNode node)
		{
			super(node);
		}
	}
}
