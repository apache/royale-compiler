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
public class TestIdentifier extends BaseFormatterTests {
	@Test
	public void testBasic() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"identifier;",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryPlus() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"+identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"+identifier;",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryPlusWithLeadingOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var a = +identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var a = +identifier;",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinus() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"-identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"-identifier;",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinusWithLeadingOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var a = -identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var a = -identifier;",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinusWithParentheses() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"(-identifier);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"(-identifier);",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinusWithBrackets() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a[-identifier];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a[-identifier];",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinusWithBlock() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"{\n" +
			"\t-identifier;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"{\n" +
				"\t-identifier;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinusWithSemicolon() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a;\n" +
			"-identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a;\n" +
				"-identifier;",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinusWithReturn() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"return -identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"return -identifier;",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinusWithComma() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var array = [identifier, -identifier];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array = [identifier, -identifier];",
				// @formatter:on
				result);
	}

	@Test
	public void testUnaryMinusWithColon() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var obj = {field: -identifier};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj = {field: -identifier};",
				// @formatter:on
				result);
	}

	@Test
	public void testNot() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"!identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"!identifier;",
				// @formatter:on
				result);
	}

	@Test
	public void testDoubleNot() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"!!identifier;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"!!identifier;",
				// @formatter:on
				result);
	}
}