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

public class TestObjectLiteral extends BaseFormatterTests {
	@Test
	public void testEmpty() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var obj:Object = {};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {};",
				// @formatter:on
				result);
	}

	@Test
	public void testOneFieldOnSingleLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var obj:Object = {one: 123.4};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {one: 123.4};",
				// @formatter:on
				result);
	}

	@Test
	public void testOneFieldOnMultipleLines() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var obj:Object = {\n" +
			"\tone: 123.4\n" +
			"};\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {\n" +
				"\t\tone: 123.4\n" +
				"\t};\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleFields() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var obj:Object = {one: 123.4, two: \"hello world\"};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {one: 123.4, two: \"hello world\"};",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleFieldsOnMultipleLines1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var obj:Object = {\n" +
			"\tone: 123.4,\n" +
			"\ttwo: \"hello world\"\n" +
			"};\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {\n" +
				"\t\tone: 123.4,\n" +
				"\t\ttwo: \"hello world\"\n" +
				"\t};\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleFieldsOnMultipleLines2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var obj:Object = {\n" +
			"\tone: 123.4,\n" +
			"\ttwo: \"hello world\", three: true\n" +
			"};\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {\n" +
				"\t\tone: 123.4,\n" +
				"\t\ttwo: \"hello world\", three: true\n" +
				"\t};\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testStringField() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var obj:Object = {\"string-field\": 123.4};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {\"string-field\": 123.4};",
				// @formatter:on
				result);
	}
}
