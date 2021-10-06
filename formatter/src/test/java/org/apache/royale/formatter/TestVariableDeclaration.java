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
public class TestVariableDeclaration extends BaseFormatterTests {
	@Test
	public void testWithoutTypeAndWithoutInitializer() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"var myVar",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var myVar;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithTypeAndWithoutInitializer() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"var myVar:String",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var myVar:String;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithTypeAndWithInitializer() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"var myVar:Number = 123.4;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var myVar:Number = 123.4;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithAnyType() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"var myVar:*;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var myVar:*;",
				// @formatter:on
				result);
	}

	@Test
	public void testWithVectorAnyType() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatText(
		// @formatter:off
			"var myVar:Vector.<*>;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var myVar:Vector.<*>;",
				// @formatter:on
				result);
	}
}
