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

/**
 * Checks for empty statements. An empty statement consists of only a semicolon
 * (;) character.
 */
public class EmptyStatementRule extends LinterRule {
	@Override
	public Map<Integer, TokenVisitor> getTokenVisitors() {
		Map<Integer, TokenVisitor> result = new HashMap<>();
		result.put(ASTokenTypes.TOKEN_SEMICOLON, (token, tokenQuery, problems) -> {
			checkSemicolon(token, tokenQuery, problems);
		});
		return result;
	}

	private void checkSemicolon(IASToken semicolon, TokenQuery tokenQuery, Collection<ICompilerProblem> problems) {
		IASToken prevToken = tokenQuery.getTokenBefore(semicolon);
		if (prevToken == null) {
			return;
		}
		if (prevToken.getType() != ASTokenTypes.TOKEN_SEMICOLON) {
			return;
		}
		if (prevToken.isImplicit()) {
			return;
		}
		problems.add(new EmptyStatementLinterProblem(semicolon));
	}

	public static class EmptyStatementLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Statement must not be empty";

		public EmptyStatementLinterProblem(IASToken token)
		{
			super(token);
		}
	}
}
