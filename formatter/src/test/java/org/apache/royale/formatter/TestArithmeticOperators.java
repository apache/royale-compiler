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

public class TestArithmeticOperators extends BaseFormatterTests {
	@Test
	public void testAdditionOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a+b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a + b;",
				// @formatter:on
				result);
	}
	
	@Test
	public void testSubtractionOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a-b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a - b;",
				// @formatter:on
				result);
	}
	
	@Test
	public void testMultiplicationOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a*b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a * b;",
				// @formatter:on
				result);
	}
	
	@Test
	public void testDivisionOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a/b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a / b;",
				// @formatter:on
				result);
	}
	
	@Test
	public void testModuloOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a%b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a % b;",
				// @formatter:on
				result);
	}

	@Test
	public void testAdditionCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a+=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a += b;",
				// @formatter:on
				result);
	}
	
	@Test
	public void testSubtractionCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a-=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a -= b;",
				// @formatter:on
				result);
	}
	
	@Test
	public void testMultiplicationCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a*=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a *= b;",
				// @formatter:on
				result);
	}
	
	@Test
	public void testDivisionCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a/=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a /= b;",
				// @formatter:on
				result);
	}
	
	@Test
	public void testModuloCompoundAssignmentOperator() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a%=b;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a %= b;",
				// @formatter:on
				result);
	}

	@Test
	public void testAdditionOperatorWithParentheses1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"(a+b)+c;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"(a + b) + c;",
				// @formatter:on
				result);
	}

	@Test
	public void testAdditionOperatorWithParentheses2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a+(b+c);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a + (b + c);",
				// @formatter:on
				result);
	}

	@Test
	public void testSubtractionOperatorWithParentheses1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"(a-b)-c;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"(a - b) - c;",
				// @formatter:on
				result);
	}

	@Test
	public void testSubtractionOperatorWithParentheses2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a-(b-c);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a - (b - c);",
				// @formatter:on
				result);
	}

	@Test
	public void testMultiplicationOperatorWithParentheses1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"(a*b)*c;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"(a * b) * c;",
				// @formatter:on
				result);
	}

	@Test
	public void testMultiplicationOperatorWithParentheses2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a*(b*c);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a * (b * c);",
				// @formatter:on
				result);
	}

	@Test
	public void testDivisionOperatorWithParentheses1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"(a/b)/c;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"(a / b) / c;",
				// @formatter:on
				result);
	}

	@Test
	public void testDivisionOperatorWithParentheses2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a/(b/c);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a / (b / c);",
				// @formatter:on
				result);
	}

	@Test
	public void testModuloOperatorWithParentheses1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"(a%b)%c;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"(a % b) % c;",
				// @formatter:on
				result);
	}

	@Test
	public void testModuloOperatorWithParentheses2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"a%(b%c);",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"a % (b % c);",
				// @formatter:on
				result);
	}
}
