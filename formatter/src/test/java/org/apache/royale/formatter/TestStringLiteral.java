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

public class TestStringLiteral extends BaseFormatterTests {
	@Test
	public void testEmptyString() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\";",
				// @formatter:on
				result);
	}

	@Test
	public void testTabEscape() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\t\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\t\";",
				// @formatter:on
				result);
	}

	@Test
	public void testCarriageReturnEscape() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\r\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\r\";",
				// @formatter:on
				result);
	}

	@Test
	public void testNewLineEscape() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\n\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\n\";",
				// @formatter:on
				result);
	}

	@Test
	public void testBackslashEscape() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\\\\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\\\\";",
				// @formatter:on
				result);
	}

	@Test
	public void testBackspaceEscape() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\b\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\b\";",
				// @formatter:on
				result);
	}

	@Test
	public void testFormFeedEscape() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\f\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\f\";",
				// @formatter:on
				result);
	}

	@Test
	public void testAsciiEscape() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\x21\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\x21\";",
				// @formatter:on
				result);
	}

	@Test
	public void testUnicodeEscape() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\u263a\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\u263a\";",
				// @formatter:on
				result);
	}

	@Test
	public void testDoubleQuoteWithEscapedDoubleQuote() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"\\\"\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"\\\"\";",
				// @formatter:on
				result);
	}

	@Test
	public void testDoubleQuoteWithUnescapedSingleQuote() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\"'\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"\"'\";",
				// @formatter:on
				result);
	}

	@Test
	public void testSingleQuoteWithEscapedSingleQuote() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"'\\'';",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"'\\'';",
				// @formatter:on
				result);
	}

	@Test
	public void testSingleQuoteWithUnescapedDoubleQuote() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"'\"';",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"'\"';",
				// @formatter:on
				result);
	}

	@Test
	public void testVerbatimDoubleQuoteWithBackslash() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"@\"\\\";",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
		"@\"\\\";",
				// @formatter:on
				result);
	}
}
