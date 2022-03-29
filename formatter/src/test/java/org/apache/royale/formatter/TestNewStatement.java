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

// for member variables of classes, see TestFieldDeclaration
public class TestNewStatement extends BaseFormatterTests {
	@Test
	public void testWithClassNoParentheses() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"new Sprite;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"new Sprite;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithClassConstructorCall() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"new Sprite();",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"new Sprite();",
				// @formatter:on
				result);
	}

	@Test
	public void testWithVectorLiteral() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"new <Sprite>[];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"new <Sprite>[];",
				// @formatter:on
				result);
	}

	@Test
	public void testWithVectorLiteralAnyType() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"new <*>[];",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"new <*>[];",
				// @formatter:on
				result);
	}
}
