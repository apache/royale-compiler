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

public class TestSwitchStatement extends BaseFormatterTests {
	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition) {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyBlock() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition) {\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseAndStatement() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tcase condition:\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseAndMultipleStatements() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t\tstatement;\n" +
			"\t\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tcase condition:\n" +
				"\t\tstatement;\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseAndEmptyBlockEnablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t{\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tcase condition:\n" +
				"\t{\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseAndEmptyBlockDisablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t{\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition) {\n" +
				"\tcase condition: {\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseAndBlockWithStatementEnablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t{\n" +
			"\t\tbreak;\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tcase condition:\n" +
				"\t{\n" +
				"\t\tbreak;\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseAndBlockWithStatementDisablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t{\n" +
			"\t\tbreak;\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition) {\n" +
				"\tcase condition: {\n" +
				"\t\tbreak;\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseAndStatementAfterBlockEnablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				// notice that the break statement is outside the curly braces,
				// so the curly braces aren't considered a "body" for the case
				// clause and they get indented an extra level.
				"switch (condition)\n" +
				"{\n" +
				"\tcase condition:\n" +
				"\t\t{\n" +
				"\t\t\tstatement;\n" +
				"\t\t}\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseAndStatementAfterBlockDisablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				// notice that the break statement is outside the curly braces,
				// so the curly braces aren't considered a "body" for the case
				// clause, and they get indented an extra level. the opening
				// brace also doesn't appear on the same line as the case.
				"switch (condition) {\n" +
				"\tcase condition:\n" +
				"\t\t{\n" +
				"\t\t\tstatement;\n" +
				"\t\t}\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithDefaultClauseAndStatement() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault:\n" +
			"\t\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tdefault:\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithDefaultClauseAndMultipleStatements() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault:\n" +
			"\t\tstatement;\n" +
			"\t\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tdefault:\n" +
				"\t\tstatement;\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithDefaultClauseAndEmptyBlockEnablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault:\n" +
			"\t{\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tdefault:\n" +
				"\t{\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithDefaultClauseAndEmptyBlockDisablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault:\n" +
			"\t{\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition) {\n" +
				"\tdefault: {\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithDefaultClauseAndBlockWithStatementEnablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault:\n" +
			"\t{\n" +
			"\t\tbreak;\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tdefault:\n" +
				"\t{\n" +
				"\t\tbreak;\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithDefaultClauseAndBlockWithStatementDisablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault:\n" +
			"\t{\n" +
			"\t\tbreak;\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition) {\n" +
				"\tdefault: {\n" +
				"\t\tbreak;\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithDefaultClauseAndStatementAfterBlockEnablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault:\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				// notice that the break statement is outside the curly braces,
				// so the curly braces aren't considered a "body" for the
				// default clause and they get indented an extra level.
				"switch (condition)\n" +
				"{\n" +
				"\tdefault:\n" +
				"\t\t{\n" +
				"\t\t\tstatement;\n" +
				"\t\t}\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithDefaultClauseAndStatementAfterBlockDisablePlaceOpenBraceOnNewLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault:\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				// notice that the break statement is outside the curly braces,
				// so the curly braces aren't considered a "body" for the
				// default clause and they get indented an extra level. the
				// opening brace also doesn't appear on the same line as default.
				"switch (condition) {\n" +
				"\tdefault:\n" +
				"\t\t{\n" +
				"\t\t\tstatement;\n" +
				"\t\t}\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithMultipleCaseClauses() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t\tbreak;\n" +
			"\tcase condition:\n" +
			"\t\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tcase condition:\n" +
				"\t\tbreak;\n" +
				"\tcase condition:\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseAndDefaultClauses() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\t\tbreak;\n" +
			"\tdefault:\n" +
			"\t\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tcase condition:\n" +
				"\t\tbreak;\n" +
				"\tdefault:\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCaseClauseFallthrough() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase condition:\n" +
			"\tcase condition:\n" +
			"\t\tbreak;\n" +
			"\tcase condition:\n" +
			"\t\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tcase condition:\n" +
				"\tcase condition:\n" +
				"\t\tbreak;\n" +
				"\tcase condition:\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithEmptyBlock() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"{\n" +
			"\tswitch (condition)\n" +
			"\t{\n" +
			"\t}\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tswitch (condition)\n" +
				"\t{\n" +
				"\t}\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithCaseClause() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"{\n" +
			"\tswitch (condition)\n" +
			"\t{\n" +
			"\t\tcase clause:\n" +
			"\t\t\tbreak;\n" +
			"\t}\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tswitch (condition)\n" +
				"\t{\n" +
				"\t\tcase clause:\n" +
				"\t\t\tbreak;\n" +
				"\t}\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithCaseClauseAndEmptyBlock() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"{\n" +
			"\tswitch (condition)\n" +
			"\t{\n" +
			"\t\tcase clause:\n" +
			"\t\t{\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tswitch (condition)\n" +
				"\t{\n" +
				"\t\tcase clause:\n" +
				"\t\t{\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithCaseClauseAndBlockWithStatement() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"{\n" +
			"\tswitch (condition)\n" +
			"\t{\n" +
			"\t\tcase clause:\n" +
			"\t\t{\n" +
			"\t\t\tbreak;\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tswitch (condition)\n" +
				"\t{\n" +
				"\t\tcase clause:\n" +
				"\t\t{\n" +
				"\t\t\tbreak;\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithDefaultClause() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"{\n" +
			"\tswitch (condition)\n" +
			"\t{\n" +
			"\t\tdefault:\n" +
			"\t\t\tbreak;\n" +
			"\t}\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tswitch (condition)\n" +
				"\t{\n" +
				"\t\tdefault:\n" +
				"\t\t\tbreak;\n" +
				"\t}\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithDefaultClauseAndEmptyBlock() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"{\n" +
			"\tswitch (condition)\n" +
			"\t{\n" +
			"\t\tdefault:\n" +
			"\t\t{\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tswitch (condition)\n" +
				"\t{\n" +
				"\t\tdefault:\n" +
				"\t\t{\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithDefaultClauseAndBlockWithStatement() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"{\n" +
			"\tswitch (condition)\n" +
			"\t{\n" +
			"\t\tdefault:\n" +
			"\t\t{\n" +
			"\t\t\tbreak;\n" +
			"\t\t}\n" +
			"\t}\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tswitch (condition)\n" +
				"\t{\n" +
				"\t\tdefault:\n" +
				"\t\t{\n" +
				"\t\t\tbreak;\n" +
				"\t\t}\n" +
				"\t}\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testCommentOnSameLineAsCaseClause() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tcase clause://what\n" +
			"\t\tbreak;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tcase clause: // what\n" +
				"\t\tbreak;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testCommentOnSameLineAsDefaultClause() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"switch (condition)\n" +
			"{\n" +
			"\tdefault://what\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"switch (condition)\n" +
				"{\n" +
				"\tdefault: // what\n" +
				"}",
				// @formatter:on
				result);
	}
}
