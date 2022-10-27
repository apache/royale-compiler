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

import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.TokenQuery;
import org.apache.royale.linter.TokenVisitor;
import org.apache.royale.linter.config.LineCommentPosition;
import org.apache.royale.linter.problems.ILinterProblem;

/**
 * Checks if line comments appear beside code on the same line, or on a
 * separate line.
 */
public class LineCommentPositionRule extends LinterRule {
	@Override
	public Map<Integer, TokenVisitor> getTokenVisitors() {
		Map<Integer, TokenVisitor> result = new HashMap<>();
		result.put(ASTokenTypes.HIDDEN_TOKEN_SINGLE_LINE_COMMENT, (token, tokenQuery, problems) -> {
			checkSingleLineComment(token, tokenQuery, problems);
		});
		return result;
	}

	public LineCommentPosition position = LineCommentPosition.ABOVE;

	private void checkSingleLineComment(IASToken comment, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IASToken prevToken = tokenQuery.getSignificantTokenBefore(comment);
		if (prevToken == null) {
			return;
		}
		if (LineCommentPosition.ABOVE.equals(position)) {
			if (prevToken.getLine() == comment.getLine()) {
				// is beside the comment
				problems.add(new LineCommentPositionLinterProblem(comment, position));
			}
		}
		else if (LineCommentPosition.BESIDE.equals(position)) {
			if (prevToken.getLine() != comment.getLine()) {
				// is not beside the comment
				problems.add(new LineCommentPositionLinterProblem(comment, position));
			}
		}
	}

	public static class LineCommentPositionLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Comment must be ${position} code";

		public LineCommentPositionLinterProblem(IASToken token, LineCommentPosition position)
		{
			super(token);
			this.position = position.getPosition();
		}

		public String position;
	}
}
