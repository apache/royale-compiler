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
import org.apache.royale.compiler.tree.as.IOperatorNode;
import org.apache.royale.linter.LinterRule;
import org.apache.royale.linter.TokenVisitor;
import org.apache.royale.linter.problems.ILinterProblem;
import org.apache.royale.linter.rules.StrictEqualityRule.StrictEqualityLinterProblem;

/**
 * Checks for uses of the '==' and '!='' operators instead of the stricter '==='
 * and '!==' operators.
 */
public class StrictEqualityRule extends LinterRule {
	@Override
	public Map<Integer, TokenVisitor> getTokenVisitors() {
		Map<Integer, TokenVisitor> result = new HashMap<>();
		result.put(ASTokenTypes.TOKEN_OPERATOR_EQUAL, (token, tokenQuery, problems) -> {
			problems.add(new StrictEqualityLinterProblem(token));
		});
		result.put(ASTokenTypes.TOKEN_OPERATOR_NOT_EQUAL, (token, tokenQuery, problems) -> {
			problems.add(new StrictEqualityLinterProblem(token));
		});
		return result;
	}

	public static class StrictEqualityLinterProblem extends CompilerProblem implements ILinterProblem {
		public static final String DESCRIPTION = "Must use ${requiredTokenText} instead of ${tokenText}";

		public StrictEqualityLinterProblem(IASToken token)
		{
			super(token);
			tokenText = token.getText();
			if (IOperatorNode.OperatorType.EQUAL.getOperatorText().equals(tokenText)) {
				requiredTokenText = IOperatorNode.OperatorType.STRICT_EQUAL.getOperatorText();
			}
			else if (IOperatorNode.OperatorType.NOT_EQUAL.getOperatorText().equals(tokenText)) {
				requiredTokenText = IOperatorNode.OperatorType.STRICT_NOT_EQUAL.getOperatorText();
			}
		}

		public String tokenText;
		public String requiredTokenText;
	}
}
