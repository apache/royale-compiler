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
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.MXMLTokenQuery;
import org.apache.royale.linter.MXMLTokenVisitor;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.TokenVisitor;

/**
 * Checks for line or block comments that are empty.
 */
public class EmptyCommentRule extends LinterRule {
	@Override
	public Map<Integer, TokenVisitor> getTokenVisitors() {
		Map<Integer, TokenVisitor> result = new HashMap<>();
		result.put(ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT, (token, tokenQuery, problems) -> {
			checkSingleLineComment(token, tokenQuery, problems);
		});
		result.put(ASTokenTypes.HIDDEN_TOKEN_MULTI_LINE_COMMENT, (token, tokenQuery, problems) -> {
			checkMultiLineComment(token, tokenQuery, problems);
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

	private void checkSingleLineComment(IASToken comment, TokenQuery tokenQuery,
			Collection<ICompilerProblem> problems) {
		String commentText = comment.getText();
		commentText = commentText.substring(2).trim();
		if (commentText.length() > 0) {
			return;
		}
		problems.add(new EmptyCommentLinterProblem(comment));
	}

	private void checkMultiLineComment(IASToken comment, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		String commentText = comment.getText();
		commentText = commentText.substring(2, commentText.length() - 2).trim();
		if (commentText.length() > 0) {
			return;
		}
		problems.add(new EmptyCommentLinterProblem(comment));
	}

	private void checkMXMLComment(IMXMLToken comment, MXMLTokenQuery tokenQuery,
			Collection<ICompilerProblem> problems) {
		String commentText = comment.getText();
		boolean isASDoc = commentText.startsWith("<!---") && commentText.length() > 7;
		if (isASDoc) {
			// see ASDocRule
			return;
		}
		commentText = commentText.substring(4, commentText.length() - 3).trim();
		if (commentText.length() > 0) {
			return;
		}
		problems.add(new EmptyCommentLinterProblem(comment));
	}

	public static class EmptyCommentLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Comment must not be empty";

		public EmptyCommentLinterProblem(IASToken token) {
			super(token);
		}

		public EmptyCommentLinterProblem(IMXMLToken token) {
			super((ISourceLocation) token);
		}
	}
}
