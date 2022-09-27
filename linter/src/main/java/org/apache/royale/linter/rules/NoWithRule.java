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

import java.util.HashMap;
import java.util.Map;

import org.apache.royale.compiler.internal.parsing.as.ASTokenTypes;
import org.apache.royale.compiler.parsing.IASToken;
import org.apache.royale.compiler.problems.CompilerProblem;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.TokenVisitor;

/**
 * Checks for uses of 'with(x)'.
 */
public class NoWithRule extends LinterRule {
	@Override
	public Map<Integer, TokenVisitor> getTokenVisitors() {
		Map<Integer, TokenVisitor> result = new HashMap<>();
		result.put(ASTokenTypes.TOKEN_KEYWORD_WITH, (token, tokenQuery, problems) -> {
			problems.add(new NoWithLinterProblem(token));
		});
		return result;
	}

	public static class NoWithLinterProblem extends CompilerProblem {
		public static final String DESCRIPTION = "Must not use 'with' statement";

		public NoWithLinterProblem(IASToken token)
		{
			super(token);
		}
	}
}
