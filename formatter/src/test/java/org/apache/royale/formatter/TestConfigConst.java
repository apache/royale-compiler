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

public class TestConfigConst extends BaseFormatterTests {
	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {\n" + 
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"{\n" + 
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock3() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"{\n" + 
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"{\n" + 
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyBlock1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyBlock2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {\n" + 
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {\n" + 
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyBlock3() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" + 
			"{\n" + 
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {\n" + 
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"{\n" + 
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {}",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {\n" + 
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {}",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock3() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"{}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {}",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock4() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {}",
				// @formatter:on
				result);
	}

	@Test
	public void testCollapseEmptyBlock5() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS{}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithStatement1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithStatement2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithStatement1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithStatement2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithStatementAfter1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {\n" +
			"\tstatement;\n" +
			"}\n" + 
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" + 
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithStatementAfter2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" + 
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"{\n" +
				"\tstatement;\n" +
				"}\n" + 
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithStatementAfter1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS {\n" +
			"\tstatement;\n" +
			"}\n" + 
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {\n" +
				"\tstatement;\n" +
				"}\n" + 
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithStatementAfter2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"{\n" +
			"\tstatement;\n" +
			"}\n" + 
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS {\n" +
				"\tstatement;\n" +
				"}\n" + 
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testAssignment() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"var b:Boolean = COMPILE::JS;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var b:Boolean = COMPILE::JS;",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithCondition1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (COMPILE::JS)\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (COMPILE::JS)\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithCondition2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (COMPILE::JS) {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (COMPILE::JS)\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithCondition1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (COMPILE::JS)\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (COMPILE::JS) {\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithCondition2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"if (COMPILE::JS) {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"if (COMPILE::JS) {\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfClass() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"class MyClass {}\n" + 
			"\n" +
			"COMPILE::SWF\n" +
			"class MyClass {}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"class MyClass {}\n" + 
				"\n" +
				"COMPILE::SWF\n" +
				"class MyClass {}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfClassInPackageWithNamespace() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"package\n" + 
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tpublic class MyClass {}\n" + 
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tpublic class MyClass {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"package\n" + 
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tpublic class MyClass {}\n" + 
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tpublic class MyClass {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfClassInPackageWithoutNamespace() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"package\n" + 
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tclass MyClass {}\n" + 
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tclass MyClass {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"package\n" + 
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tclass MyClass {}\n" + 
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tclass MyClass {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfClassInPackageWithDynamicModifier() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"package\n" + 
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tdynamic class MyClass {}\n" + 
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tdynamic class MyClass {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"package\n" + 
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tdynamic class MyClass {}\n" + 
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tdynamic class MyClass {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfClassInPackageWithAbstractModifier() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"package\n" + 
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tabstract class MyClass {}\n" + 
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tabstract class MyClass {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"package\n" + 
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tabstract class MyClass {}\n" + 
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tabstract class MyClass {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfClassInPackageWithFinalModifier() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"package\n" + 
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tfinal class MyClass {}\n" + 
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tfinal class MyClass {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"package\n" + 
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tfinal class MyClass {}\n" + 
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tfinal class MyClass {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfInterface() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"COMPILE::JS\n" +
			"interface MyInterface {}\n" + 
			"\n" +
			"COMPILE::SWF\n" +
			"interface MyInterface {}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"COMPILE::JS\n" +
				"interface MyInterface {}\n" + 
				"\n" +
				"COMPILE::SWF\n" +
				"interface MyInterface {}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfInterfaceInPackageWithNamespace() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"package\n" + 
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tpublic interface MyInterface {}\n" + 
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tpublic interface MyInterface {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"package\n" + 
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tpublic interface MyInterface {}\n" + 
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tpublic interface MyInterface {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfInterfaceInPackageWithoutNamespace() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"package\n" + 
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tinterface MyInterface {}\n" + 
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tinterface MyInterface {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"package\n" + 
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tinterface MyInterface {}\n" + 
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tinterface MyInterface {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfMethodWithNamespace() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"class MyClass\n" +
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tpublic function myMethod():void {}\n" +
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tpublic function myMethod():void {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class MyClass\n" +
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tpublic function myMethod():void {}\n" +
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tpublic function myMethod():void {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfMethodWithoutNamespace() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"class MyClass\n" +
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\tfunction myMethod():void {}\n" +
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\tfunction myMethod():void {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class MyClass\n" +
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\tfunction myMethod():void {}\n" +
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\tfunction myMethod():void {}\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testConfigConditionOfMethodWithOverride() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = false;
		settings.collapseEmptyBlocks = true;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"class MyClass extends OtherClass\n" +
			"{\n" +
			"\tCOMPILE::JS\n" +
			"\toverride public function myMethod():void {}\n" +
			"\n" +
			"\tCOMPILE::SWF\n" +
			"\toverride public function myMethod():void {}\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class MyClass extends OtherClass\n" +
				"{\n" +
				"\tCOMPILE::JS\n" +
				"\toverride public function myMethod():void {}\n" +
				"\n" +
				"\tCOMPILE::SWF\n" +
				"\toverride public function myMethod():void {}\n" +
				"}",
				// @formatter:on
				result);
	}
}
