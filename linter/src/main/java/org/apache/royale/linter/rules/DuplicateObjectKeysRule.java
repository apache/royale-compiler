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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.internal.tree.as.ContainerNode;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.ILiteralContainerNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.tree.as.IObjectLiteralValuePairNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;

/**
 * Check that each key in an object literal is unique.
 */
public class DuplicateObjectKeysRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.ObjectLiteralExpressionID, (node, tokenQuery, problems) -> {
			checkLiteralContainerNode((ILiteralContainerNode) node, tokenQuery, problems);
		});
		return result;
	}

	private void checkLiteralContainerNode(ILiteralContainerNode objectLiteralNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (!LiteralType.OBJECT.equals(objectLiteralNode.getLiteralType())) {
			return;
		}
		ContainerNode contentsNode = objectLiteralNode.getContentsNode();
		if (contentsNode == null) {
			return;
		}
		Set<String> keyNames = new HashSet<>();
		for (int i = 0; i < contentsNode.getChildCount(); i++) {
			IASNode child = contentsNode.getChild(i);
			if (!(child instanceof IObjectLiteralValuePairNode)) {
				continue;
			}
			String keyName = null;
			IObjectLiteralValuePairNode valuePairNode = (IObjectLiteralValuePairNode) child;
			IExpressionNode nameNode = valuePairNode.getNameNode();
			if (nameNode instanceof IIdentifierNode) {
				IIdentifierNode identifierNode = (IIdentifierNode) nameNode;
				keyName = identifierNode.getName();
			} else if (nameNode instanceof ILiteralNode) {
				ILiteralNode literalNode = (ILiteralNode) nameNode;
				if (!LiteralType.STRING.equals(literalNode.getLiteralType())) {
					continue;
				}
				keyName = literalNode.getValue();
			}
			if (keyName != null) {
				if (keyNames.contains(keyName)) {
					problems.add(new DuplicateObjectKeysLinterProblem(nameNode, keyName));
				} else {
					keyNames.add(keyName);
				}
			}
		}
	}

	public static class DuplicateObjectKeysLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Object literal contains duplicate key '${keyName}'";

		public DuplicateObjectKeysLinterProblem(IExpressionNode node, String keyName)
		{
			super(node);
			this.keyName = keyName;
		}

		public String keyName;
	}
}
