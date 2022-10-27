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

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.parsing.IMXMLToken;
import org.apache.royale.compiler.parsing.IMXMLToken.MXMLTokenKind;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.MXMLTokenQuery;
import org.apache.royale.linter.MXMLTokenVisitor;
import org.apache.royale.linter.NodeVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Checks for missing or empty ASDoc comments.
 */
public class MissingASDocRule extends LinterRule {
	@Override
	public Map<ASTNodeID, NodeVisitor> getNodeVisitors() {
		Map<ASTNodeID, NodeVisitor> result = new HashMap<>();
		result.put(ASTNodeID.ClassID, (node, tokenQuery, problems) -> {
			checkDocumentableDefinitionNode((IDocumentableDefinitionNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.InterfaceID, (node, tokenQuery, problems) -> {
			checkDocumentableDefinitionNode((IDocumentableDefinitionNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.FunctionID, (node, tokenQuery, problems) -> {
			checkDocumentableDefinitionNode((IDocumentableDefinitionNode) node, tokenQuery, problems);
		});
		result.put(ASTNodeID.VariableID, (node, tokenQuery, problems) -> {
			checkDocumentableDefinitionNode((IDocumentableDefinitionNode) node, tokenQuery, problems);
		});
		return result;
	}

	@Override
	public Map<MXMLTokenKind, MXMLTokenVisitor> getMXMLTokenVisitors() {
		Map<MXMLTokenKind, MXMLTokenVisitor> result = new HashMap<>();
		result.put(MXMLTokenKind.COMMENT, (token, tokenQuery, problems) -> {
			checkMXMLComment(token, tokenQuery, problems);
		});
		return result;
	}

	private void checkDocumentableDefinitionNode(IDocumentableDefinitionNode definitionNode, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		if (!definitionNode.hasNamespace("public")) {
			return;
		}
		IASToken token = tokenQuery.getTokenBefore(definitionNode, false, true);
		if (token.getType() == ASTokenTypes.TOKEN_ASDOC_COMMENT) {
			String docComment = token.getText();
			if (!isDocCommentEmpty(docComment)) {
				return;
			}
			problems.add(new EmptyASDocLinterProblem(token));
			return;
		}
		problems.add(new MissingASDocLinterProblem(definitionNode));
	}

	private boolean isDocCommentEmpty(String docComment) {
		docComment = docComment.substring(3, docComment.length() - 2).trim();
		String[] lines = docComment.split("\\r?\\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[0];
			line = line.trim();
			if (line.startsWith("*")) {
				line = line.substring(1).trim();
			}
			if (line.length() > 0) {
				return false;
			}
		}
		return true;
	}

	private void checkMXMLComment(IMXMLToken comment, MXMLTokenQuery tokenQuery,
			Collection<ICompilerProblem> problems) {
		String commentText = comment.getText();
		boolean isASDoc = commentText.startsWith("<!---") && commentText.length() > 7;
		if (!isASDoc) {
			return;
		}
		commentText = commentText.substring(5, commentText.length() - 3).trim();
		if (commentText.length() > 0) {
			return;
		}
		problems.add(new EmptyASDocLinterProblem(comment));
	}

	public static class MissingASDocLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Public APIs must have ASDoc comments";

		public MissingASDocLinterProblem(IDocumentableDefinitionNode node)
		{
			super(node);
		}
	}

	public static class EmptyASDocLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "ASDoc comments must not be empty";

		public EmptyASDocLinterProblem(IASToken token)
		{
			super(token);
		}

		public EmptyASDocLinterProblem(IMXMLToken token)
		{
			super((ISourceLocation) token);
		}
	}
}
