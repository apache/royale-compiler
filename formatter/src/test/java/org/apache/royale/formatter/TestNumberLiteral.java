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

public class TestNumberLiteral extends BaseFormatterTests {
	@Test
	public void testFloat() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"123.4;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"123.4;",
				// @formatter:on
				result);
	}

	@Test
	public void testNegativeFloat() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"-123.4;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"-123.4;",
				// @formatter:on
				result);
	}

	@Test
	public void testPositiveFloat() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"+123.4;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"+123.4;",
				// @formatter:on
				result);
	}

	@Test
	public void testInt() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"123;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"123;",
				// @formatter:on
				result);
	}

	@Test
	public void testNegativeInt() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"-123;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"-123;",
				// @formatter:on
				result);
	}

	@Test
	public void testPositiveInt() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"+123;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"+123;",
				// @formatter:on
				result);
	}

	@Test
	public void testHexadecimal() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"0xfe1c23;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"0xfe1c23;",
				// @formatter:on
				result);
	}

	@Test
	public void testNegativeHexadecimal() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"-0xfe1c23;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"-0xfe1c23;",
				// @formatter:on
				result);
	}

	@Test
	public void testPostiveHexadecimal() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"+0xfe1c23;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"+0xfe1c23;",
				// @formatter:on
				result);
	}

	@Test
	public void testExponential() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"1.234e5;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"1.234e5;",
				// @formatter:on
				result);
	}

	@Test
	public void testNegativeExponential() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"1.234e-5;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"1.234e-5;",
				// @formatter:on
				result);
	}

	@Test
	public void testPositiveExponential() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"+1.234e-5;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"+1.234e-5;",
				// @formatter:on
				result);
	}
}
