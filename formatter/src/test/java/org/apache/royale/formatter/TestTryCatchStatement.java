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

public class TestTryCatchStatement extends BaseFormatterTests {
	@Test
	public void testPlaceOpenBraceOnNewLineWithEmptyBlock() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"try {\n" +
			"}\n" +
			"catch (e:Object) {\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"try\n" +
				"{\n" +
				"}\n" +
				"catch (e:Object)\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithEmptyBlock() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"try\n" +
			"{\n" +
			"}\n" +
			"catch (e:Object)\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"try {\n" +
				"}\n" +
				"catch (e:Object) {\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testInsertSpaceAfterControlFlowKeyword() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"try\n" +
			"{\n" + 
			"\tstatement;\n" +
			"}\n" +
			"catch(e:Object)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"try\n" +
				"{\n" + 
				"\tstatement;\n" +
				"}\n" +
				"catch (e:Object)\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisableInsertSpaceAfterControlFlowKeyword() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = false;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"try\n" +
			"{\n" + 
			"\tstatement;\n" +
			"}\n" +
			"catch (e:Object)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);

		assertEquals(
		// @formatter:off
				"try\n" +
				"{\n" + 
				"\tstatement;\n" +
				"}\n" +
				"catch(e:Object)\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testPlaceOpenBraceOnNewLineWithStatement() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"try {\n" +
			"\tstatement;\n" +
			"}\n" +
			"catch (e:Object) {\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"try\n" +
				"{\n" + 
				"\tstatement;\n" +
				"}\n" +
				"catch (e:Object)\n" +
				"{\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testDisablePlaceOpenBraceOnNewLineWithStatement() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"try\n" +
			"{\n" + 
			"\tstatement;\n" +
			"}\n" +
			"catch (e:Object)\n" +
			"{\n" +
			"\tstatement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"try {\n" +
				"\tstatement;\n" +
				"}\n" +
				"catch (e:Object) {\n" +
				"\tstatement;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testWithCommentBetweenTryStatementAndBody() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = false;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"try // comment\n" +
			"{\n" +
			"	statement;\n" +
			"}\n" +
			"catch (e:Object) {\n" +
			"	statement;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"try // comment\n" +
				"{\n" +
				"	statement;\n" +
				"}\n" +
				"catch (e:Object) {\n" +
				"	statement;\n" +
				"}",
				// @formatter:on
				result);
	}
}
