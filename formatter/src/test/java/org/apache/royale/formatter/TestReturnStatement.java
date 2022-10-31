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

public class TestReturnStatement extends BaseFormatterTests {
	@Test
	public void testWithoutValue() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"return;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"return;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithoutValueAndWithoutSemicolon() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"return",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"return;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithValue() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"return 123;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"return 123;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithValueAndWithoutSemicolon() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"return 123",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"return 123;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithValueOnNextLineCausesAutomaticSemicolonInsertion() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"return\n" +
			"123;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"return;\n" +
				"123;",
				// @formatter:on
				result);
	}
}
