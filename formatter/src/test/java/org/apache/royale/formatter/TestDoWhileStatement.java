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

public class TestDoWhileStatement extends BaseFormatterTests {
	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do {\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do\n" +
				"{\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do\n" +
			"{\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do\n" +
				"{\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyBlock1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do\n" +
			"{\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyBlock2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do {\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testInsertSpaceAfterControlFlowKeyword() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" +
			"while(true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testDisableInsertSpaceAfterControlFlowKeyword() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = false;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" +
				"while(true);",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithStatement1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do {\n" +
			"\tstatement;\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithStatement2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithStatement1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {\n" +
				"\tstatement;\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithStatement2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do {\n" +
			"\tstatement;\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {\n" +
				"\tstatement;\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testBodyWithoutParentheses() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do statement;\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do\n" +
				"\tstatement;\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testWithBodyIsSemicolonOnSameLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do;\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do;\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithEmptyBlock() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"{\n" +
			"\tdo\n" +
			"\t{\n" +
			"\t}\n" +
			"\twhile (true);\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tdo\n" +
				"\t{\n" +
				"\t}\n" +
				"\twhile (true);\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithBodyWithoutParentheses() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"{\n" +
			"\tdo\n" +
			"\t\tstatement;\n" +
			"\twhile (true);\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tdo\n" +
				"\t\tstatement;\n" +
				"\twhile (true);\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIndentWithBodyWithParentheses() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"{\n" +
			"\tdo\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\twhile (true);\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tdo\n" +
				"\t{\n" +
				"\t\tstatement;\n" +
				"\t}\n" +
				"\twhile (true);\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNextIntentWithBodyIsSemicolonOnSameLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"{\n" +
			"\tdo;\n" +
			"\twhile (true);\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tdo;\n" +
				"\twhile (true);\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCommentBetweenTryStatementAndBody() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do // comment\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" +
			"while (true);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do // comment\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" +
				"while (true);",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do {\n" +
			"}\n" +
			"while (condition1);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {}\n" +
				"while (condition1);",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do\n" +
			"{\n" +
			"}\n" +
			"while (condition1);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {}\n" +
				"while (condition1);",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock3() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do\n" +
			"{}\n" +
			"while (condition1);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {}\n" +
				"while (condition1);",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock4() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do {}\n" +
			"while (condition1);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {}\n" +
				"while (condition1);",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock5() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"do{}\n" +
			"while (condition1);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"do {}\n" +
				"while (condition1);",
				// @formatter:on
				result);
	}
}
