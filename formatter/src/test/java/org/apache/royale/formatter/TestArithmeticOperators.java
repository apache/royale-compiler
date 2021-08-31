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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
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
}
