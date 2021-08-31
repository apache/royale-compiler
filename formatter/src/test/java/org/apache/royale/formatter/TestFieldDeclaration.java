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

public class TestFieldDeclaration extends BaseFormatterTests {
	@Test
	public void testWithoutTypeAndWithoutInitializerAndWithoutNamespace() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"class ClassWithField\n" +
			"{\n" +
			"\tvar myVar\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class ClassWithField\n" +
				"{\n" +
				"\tvar myVar;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithTypeAndWithoutInitializerAndWithoutNamespace() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"class ClassWithField\n" +
			"{\n" +
			"\tvar myVar:String\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class ClassWithField\n" +
				"{\n" +
				"\tvar myVar:String;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithTypeAndWithInitializerAndWithoutNamespace() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"class ClassWithField\n" +
			"{\n" +
			"\tvar myVar:Number = 123.4;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class ClassWithField\n" +
				"{\n" +
				"\tvar myVar:Number = 123.4;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithTypeAndWithInitializerAndWithNamespace() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"class ClassWithField\n" +
			"{\n" +
			"\tpublic var myVar:Number = 123.4;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class ClassWithField\n" +
				"{\n" +
				"\tpublic var myVar:Number = 123.4;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithTypeFollowedByInlineMultilineComment() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"class ClassWithField\n" +
			"{\n" +
			"\tpublic var myVar:Array /* of Type */;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class ClassWithField\n" +
				"{\n" +
				"\tpublic var myVar:Array /* of Type */;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithFunctionInitializer() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"class ClassWithField\n" +
			"{\n" +
			"\tpublic var myVar:Function = function() {\n" +
			"\t};\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class ClassWithField\n" +
				"{\n" +
				"\tpublic var myVar:Function = function()\n" +
				"\t{\n" +
				"\t};\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithObjectInitializer() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"class ClassWithField\n" +
			"{\n" +
			"\tvar myVar:Object = {};\n" +
			"\t// correct indent?\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class ClassWithField\n" +
				"{\n" +
				"\tvar myVar:Object = {};\n" +
				"\t// correct indent?\n" +
				"}",
				// @formatter:on
				result);
	}
}
