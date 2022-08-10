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

public class TestArrayLiteral extends BaseFormatterTests {
	@Test
	public void testEmpty() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array = [];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array = [];",
				// @formatter:on
				result);
	}

	@Test
	public void testSingleNumberOnOneLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array = [123.4];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array = [123.4];",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleNumbersOnOneLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array = [123.4, 567.8, 901.2];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array = [123.4, 567.8, 901.2];",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleNumbersOnMultipleLines1() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array = [123.4, 567.8,\n" +
			"\t901.2];\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array = [123.4, 567.8,\n" +
				"\t\t901.2];\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleNumbersOnMultipleLines2() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array =\n" +
			"\t[\n" +
			"\t\t123.4,\n" +
			"\t\t567.8,\n" +
			"\t\t901.2\n" +
			"\t];\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array =\n" +
				"\t[\n" +
				"\t\t123.4,\n" +
				"\t\t567.8,\n" +
				"\t\t901.2\n" +
				"\t];\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleStringsOnOneLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array = [\"abc\", \"hello world\", \"\"];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array = [\"abc\", \"hello world\", \"\"];",
				// @formatter:on
				result);
	}

	@Test
	public void testFunction() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array = [\n" +
			"\tfunction():void {\n" + 
			"\t\tif (condition1\n" +
			"\t\t\t&& condition2) {\n" + 
			"\t\t\tstatement;\n" +
			"\t\t}\n" +
			"\t}\n" +
			"];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array = [\n" +
				"\t\tfunction():void\n" + 
				"\t\t{\n" +
				"\t\t\tif (condition1\n" +
				"\t\t\t\t&& condition2)\n" +
				"\t\t\t{\n" + 
				"\t\t\t\tstatement;\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t];",
				// @formatter:on
				result);
	}

	@Test
	public void testObjectLiteral() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array = [\n" +
			"\t{\n" + 
			"\t\tvalue: 123.4\n" +
			"\t}\n" +
			"];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array = [\n" +
				"\t\t{\n" + 
				"\t\t\tvalue: 123.4\n" +
				"\t\t}\n" +
				"\t];",
				// @formatter:on
				result);
	}

	@Test
	public void testNested() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"var array:Array = [\n" +
			"\t[1, 2, 3],\n" +
			"\t['one',\n" +
			"\t\t'two', 'three'],\n" +
			"\t[\n" +
			"\t\t123.4,\n" +
			"\t\t567.8\n" +
			"\t]\n" +
			"];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var array:Array = [\n" +
				"\t\t[1, 2, 3],\n" +
				"\t\t['one',\n" +
				"\t\t\t'two', 'three'],\n" +
				"\t\t[\n" +
				"\t\t\t123.4,\n" +
				"\t\t\t567.8\n" +
				"\t\t]\n" +
				"\t];",
				// @formatter:on
				result);
	}
}