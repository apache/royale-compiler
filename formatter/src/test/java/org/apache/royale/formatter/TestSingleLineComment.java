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

public class TestSingleLineComment extends BaseFormatterTests {
	@Test
	public void testInsertSpaceAtStartOfLineComment() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.insertSpaceAtStartOfLineComment = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"//this is a comment",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"// this is a comment",
				// @formatter:on
				result);
	}
	@Test
	public void testDisableInsertSpaceAtStartOfLineComment() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.insertSpaceAtStartOfLineComment = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"// this is a comment",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"//this is a comment",
				// @formatter:on
				result);
	}

	@Test
	public void testAtEndOfStatement() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.insertSpaceAtStartOfLineComment = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"statement; // this is a comment",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"statement; // this is a comment",
				// @formatter:on
				result);
	}

	@Test
	public void testOnLineBeforeStatement() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.insertSpaceAtStartOfLineComment = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"// this is a comment\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"// this is a comment\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithExtraLineBeforeStatement() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.insertSpaceAtStartOfLineComment = true;
		settings.maxPreserveNewLines = 2;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"// this is a comment\n" +
			"\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"// this is a comment\n" +
				"\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testOnLineAfterStatement() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.insertSpaceAtStartOfLineComment = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"statement;\n" +
			"// this is a comment",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"statement;\n" +
				"// this is a comment",
				// @formatter:on
				result);
	}

	@Test
	public void testAtEndOfControlFlowStatement() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.insertSpaceAtStartOfLineComment = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (statement) // this is a comment\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (statement) // this is a comment\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testAtEndOfBlock() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.insertSpaceAtStartOfLineComment = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"{\n" +
			"\tstatement;\n" +
			"} // this is a comment\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\tstatement;\n" +
				"} // this is a comment\n" +
				"statement;",
				// @formatter:on
				result);
	}

}
