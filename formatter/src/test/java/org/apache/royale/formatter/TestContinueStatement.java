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

public class TestContinueStatement extends BaseFormatterTests {

	@Test
	public void testWithoutLabel() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"continue;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"continue;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithoutLabelAndWithoutSemicolon() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"continue",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"continue;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithLabel() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"continue label;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"continue label;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithLabelAndWithoutSemicolon() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"continue label",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"continue label;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithLabelOnNextLineCausesAutomaticSemicolonInsertion() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"continue\n" +
			"label;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"continue;\n" +
				"label;",
				// @formatter:on
				result);
	}
}
