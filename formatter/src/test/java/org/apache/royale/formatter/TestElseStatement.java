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

public class TestElseStatement extends BaseFormatterTests {
	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (condition) {\n" +
			"\tstatement;\n" +
			"}\n" +
			"else {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition)\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" +
				"else\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyBlock() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (condition)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" +
			"else\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition) {\n" +
				"\tstatement;\n" +
				"}\n" +
				"else {\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyIfAndElseBlocks() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (condition) {\n" +
			"}\n" +
			"else {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition)\n" +
				"{\n" +
				"}\n" +
				"else\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyIfAndElseBlocks() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (condition)\n" +
			"{\n" +
			"}\n" +
			"else\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition) {\n" +
				"}\n" +
				"else {\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithStatement() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (condition) {\n" +
			"\tstatement;\n" +
			"}\n" +
			"else {\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition)\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" +
				"else\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithStatement() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (condition)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" +
			"else\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition) {\n" +
				"\tstatement;\n" +
				"}\n" +
				"else {\n" +
				"\tstatement;\n" +
				"}",
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
			"if (condition) statement;\n" +
			"else statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition)\n" +
				"\tstatement;\n" +
				"else\n" +
				"\tstatement;",
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
			"if (true);\n" +
			"else;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (true);\n" +
				"else;",
				// @formatter:on
				result);
	}

	// don't insert semicolon between else and block open
	@Test
	public void testWithCommentBetweenIfStatementAndBodyInsideFunction() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"function myFunction():void\n" +
			"{\n" +
			"	if(condition) // comment\n" +
			"	{\n" +
			"		statement;\n" +
			"	}\n" +
			"	else\n" +
			"	{\n" +
			"		statement;\n" +
			"	}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"function myFunction():void\n" +
				"{\n" +
				"	if (condition) // comment\n" +
				"	{\n" +
				"		statement;\n" +
				"	}\n" +
				"	else\n" +
				"	{\n" +
				"		statement;\n" +
				"	}\n" +
				"}",
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
			"\tif (condition)\n" +
			"\t{\n" +
			"\t}\n" +
			"\telse\n" +
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
				"\tif (condition)\n" +
				"\t{\n" +
				"\t}\n" +
				"\telse\n" +
				"\t{\n" +
				"\t}\n" +
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
			"\tif (true)\n" +
			"\t\tstatement;\n" +
			"\telse\n" +
			"\t\tstatement;\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tif (true)\n" +
				"\t\tstatement;\n" +
				"\telse\n" +
				"\t\tstatement;\n" +
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
			"\tif (true)\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\telse\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tif (true)\n" +
				"\t{\n" +
				"\t\tstatement;\n" +
				"\t}\n" +
				"\telse\n" +
				"\t{\n" +
				"\t\tstatement;\n" +
				"\t}\n" +
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
			"\tif (true);\n" +
			"\telse;\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tif (true);\n" +
				"\telse;\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}


	@Test
	public void testCommentBeforeStartBody() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (true)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" +
			"else // comment\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (true)\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" +
				"else // comment\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testCommentBeforeEndBody() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (true)\n" +
			"{\n" +
			"}\n" +
			"else\n" +
			"{\n" +
			"\tstatement; // comment\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (true)\n" +
				"{\n" +
				"}\n" +
				"else\n" +
				"{\n" +
				"\tstatement; // comment\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testNested() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (condition)\n" +
			"{\n" +
			"\tif (condition)\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\telse\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"}\n" +
			"else\n" +
			"{\n" +
			"\tif (condition)\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"\telse\n" +
			"\t{\n" +
			"\t\tstatement;\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition)\n" +
				"{\n" +
				"\tif (condition)\n" +
				"\t{\n" +
				"\t\tstatement;\n" +
				"\t}\n" +
				"\telse\n" +
				"\t{\n" +
				"\t\tstatement;\n" +
				"\t}\n" +
				"}\n" +
				"else\n" +
				"{\n" +
				"\tif (condition)\n" +
				"\t{\n" +
				"\t\tstatement;\n" +
				"\t}\n" +
				"\telse\n" +
				"\t{\n" +
				"\t\tstatement;\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (condition1) {\n" +
			"}\n" +
			"else {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (condition1) {}\n" +
				"else {}",
				// @formatter:on
				result);
	}
}
