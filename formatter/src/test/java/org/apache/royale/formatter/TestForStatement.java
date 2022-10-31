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

public class TestForStatement extends BaseFormatterTests {
	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"for (var i:int = 0; i < 3; i++) {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for (var i:int = 0; i < 3; i++)\n" +
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
			"for (var i:int = 0; i < 3; i++)\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for (var i:int = 0; i < 3; i++) {\n" +
				"}",
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
			"for(var i:int = 0; i < 3; i++)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for (var i:int = 0; i < 3; i++)\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
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
			"for (var i:int = 0; i < 3; i++)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for(var i:int = 0; i < 3; i++)\n" +
				"{\n" +
				"\tstatement;\n" +
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
			"for (var i:int = 0; i < 3; i++) {\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for (var i:int = 0; i < 3; i++)\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"for (var i:int = 0; i < 3; i++)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for (var i:int = 0; i < 3; i++) {\n" +
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
			"for (var i:int = 0; i < 3; i++) statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for (var i:int = 0; i < 3; i++)\n" +
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
			"for (var i:int = 0; i < 3; i++);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for (var i:int = 0; i < 3; i++);",
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
			"\tfor (var i:int = 0; i < 3; i++)\n" +
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
				"\tfor (var i:int = 0; i < 3; i++)\n" +
				"\t{\n" +
				"\t\tstatement;\n" +
				"\t}\n" +
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
			"\tfor (var i:int = 0; i < 3; i++)\n" +
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
				"\tfor (var i:int = 0; i < 3; i++)\n" +
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
			"\tfor (var i:int = 0; i < 3; i++)\n" +
			"\t\tstatement;\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tfor (var i:int = 0; i < 3; i++)\n" +
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
			"\tfor (var i:int = 0; i < 3; i++)\n" +
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
				"\tfor (var i:int = 0; i < 3; i++)\n" +
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
			"\tfor (var i:int = 0; i < 3; i++);\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tfor (var i:int = 0; i < 3; i++);\n" +
				"\tstatement;\n" +
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
			"for (var i:int = 0; i < 3; i++) {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"for (var i:int = 0; i < 3; i++) {}",
				// @formatter:on
				result);
	}
}
