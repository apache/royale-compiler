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

public class TestAsDocComment  extends BaseFormatterTests {
	@Test
	public void testOneLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"/** This is a comment */",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"/** This is a comment */",
				// @formatter:on
				result);
	}

	@Test
	public void testSpacesBeforeAsterisks() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"/**\n" +
			"* This is a comment\n" +
			"*/",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"/**\n" +
				" * This is a comment\n" +
				" */",
				// @formatter:on
				result);
	}

	@Test
	public void testBeforeClass() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"/**\n" +
			" * This is a comment\n" +
			" */\n" +
			"class MyClass\n" + 
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"/**\n" +
				" * This is a comment\n" +
				" */\n" +
				"class MyClass\n" + 
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testBeforeClassInPackage() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"package com.example\n" +
			"{\n" +
			"\t/**\n" +
			"\t * This is a comment\n" +
			"\t */\n" +
			"\tclass MyClass\n" + 
			"\t{\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"package com.example\n" +
				"{\n" +
				"\t/**\n" +
				"\t * This is a comment\n" +
				"\t */\n" +
				"\tclass MyClass\n" + 
				"\t{\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testBeforeInterface() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"/**\n" +
			" * This is a comment\n" +
			" */\n" +
			"interface MyInterface\n" + 
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"/**\n" +
				" * This is a comment\n" +
				" */\n" +
				"interface MyInterface\n" + 
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testBeforeField() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"class MyClass\n" + 
			"{\n" +
			"\t/**\n" +
			"\t * This is comment\n" +
			"\t */\n" + 
			"\tpublic var myVar:String;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class MyClass\n" + 
				"{\n" +
				"\t/**\n" +
				"\t * This is comment\n" +
				"\t */\n" + 
				"\tpublic var myVar:String;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testBeforeMethod() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"class MyClass\n" + 
			"{\n" +
			"\t/**\n" +
			"\t * This is comment\n" +
			"\t */\n" + 
			"\tpublic function myMethod():void\n" +
			"\t{\n" +
			"\t}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class MyClass\n" + 
				"{\n" +
				"\t/**\n" +
				"\t * This is comment\n" +
				"\t */\n" + 
				"\tpublic function myMethod():void\n" +
				"\t{\n" +
				"\t}\n" +
				"}",
				// @formatter:on
				result);
	}
	@Test
	public void testListing() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"/**\n" +
			" * Description.\n" +
			" * \n" +
			" * <listing>\n" +
			" * // before\n" +
			" * \n" +
			" * </listing>\n" +
			" * \n" +
			" * @see test\n" +
			" */",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"/**\n" +
				" * Description.\n" +
				" *\n" +
				" * <listing>\n" +
				" * // before\n" +
				" * \n" +
				" * </listing>\n" +
				" *\n" +
				" * @see test\n" +
				" */",
				// @formatter:on
				result);
	}
}
