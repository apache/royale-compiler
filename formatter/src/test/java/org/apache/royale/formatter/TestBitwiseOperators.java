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

public class TestBitwiseOperators extends BaseFormatterTests {
	@Test
	public void testBitwiseNotOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"~a;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"~a;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseAndOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a&b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a & b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseOrOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a|b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a | b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseXorOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a^b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a ^ b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseLeftShiftOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a<<b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a << b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseRightShiftOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a>>b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a >> b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseRightUnsignedShiftOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a>>>b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a >>> b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseAndCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a&=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a &= b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseOrCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a|=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a |= b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseXorCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a^=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a ^= b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseLeftShiftCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a<<=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a <<= b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseRightShiftCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a>>=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a >>= b;",
				// @formatter:on
				result);
	}

	@Test
	public void testBitwiseRightUnsignedShiftCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a>>>=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a >>>= b;",
				// @formatter:on
				result);
	}
}
