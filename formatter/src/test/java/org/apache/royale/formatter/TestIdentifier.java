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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
	public void testNot() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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