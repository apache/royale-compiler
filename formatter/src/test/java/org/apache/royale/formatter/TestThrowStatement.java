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

package org.apache.royale.formatter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestThrowStatement extends BaseFormatterTests {

	@Test
	public void testWithoutValue() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.ignoreProblems = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"throw;",
			// @formatter:on
			null
		);
		assertEquals(
		// @formatter:off
				"throw;\n" +
				";",
				// @formatter:on
				result);
	}

	@Test
	public void testWithoutValueAndWithoutSemicolon() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.ignoreProblems = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"throw",
			// @formatter:on
			null
		);
		assertEquals(
		// @formatter:off
				"throw;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithValue() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"throw new Error();",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"throw new Error();",
				// @formatter:on
				result);
	}

	@Test
	public void testWithValueAndWithoutSemicolon() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"throw new Error()",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"throw new Error();",
				// @formatter:on
				result);
	}

	@Test
	public void testWithValueOnNextLineCausesAutomaticSemicolonInsertion() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.ignoreProblems = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"throw\n" +
			"new Error();",
			// @formatter:on
			null
		);
		assertEquals(
		// @formatter:off
				"throw;\n" +
				"new Error();",
				// @formatter:on
				result);
	}
}
