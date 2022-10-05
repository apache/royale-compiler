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

import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ILiteralContainerNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Check that an array literal contains no empty slots (multiple repeating commas with no values).
 */
public class NoSparseArrayRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.ArrayLiteralID, (node, tokenQuery, problems) -> {
			checkLiteralContainerNode((ILiteralContainerNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkLiteralContainerNode(ILiteralContainerNode arrayLiteralNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (!LiteralType.ARRAY.equals(arrayLiteralNode.getLiteralType())) {
			return;
		}
		ContainerNode contentsNode = arrayLiteralNode.getContentsNode();
		if (contentsNode == null) {
			return;
		}
		for (int i = 0; i < contentsNode.getChildCount(); i++) {
			IASNode child = contentsNode.getChild(i);
			if (ASTNodeID.NilID.equals(child.getNodeID())) {
				problems.add(new NoSparseArrayLinterProblem(arrayLiteralNode));
				return;
			}
		}
	}

	public static class NoSparseArrayLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Array literals must not be sparse";

		public NoSparseArrayLinterProblem(ILiteralContainerNode node)
		{
			super(node);
		}
	}
}
