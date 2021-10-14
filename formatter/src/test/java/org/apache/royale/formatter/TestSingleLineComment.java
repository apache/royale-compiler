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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.insertSpaceAtStartOfLineComment = true;
		String result = formatter.formatActionScriptText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.insertSpaceAtStartOfLineComment = false;
		String result = formatter.formatActionScriptText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.insertSpaceAtStartOfLineComment = true;
		String result = formatter.formatActionScriptText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.insertSpaceAtStartOfLineComment = true;
		String result = formatter.formatActionScriptText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.insertSpaceAtStartOfLineComment = true;
		formatter.maxPreserveNewLines = 2;
		String result = formatter.formatActionScriptText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.insertSpaceAtStartOfLineComment = true;
		String result = formatter.formatActionScriptText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		formatter.insertSpaceAtStartOfLineComment = true;
		String result = formatter.formatActionScriptText(
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

}
