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

public class TestMetadata extends BaseFormatterTests {
	@Test
	public void testUnknownMetaTagOnClassWithoutAttributes() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"[UnknownMetaTag]\n" +
			"class MyClass\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"[UnknownMetaTag]\n" +
				"class MyClass\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testUnknownMetaTagOnClassWithoutAttributeNameAndWithValue() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"[UnknownMetaTag(\"value\")]\n" +
			"class MyClass\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"[UnknownMetaTag(\"value\")]\n" +
				"class MyClass\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testUnknownMetaTagOnClassWithAttributeAndWithValue() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"[UnknownMetaTag(unknownAttr=\"value\")]\n" +
			"class MyClass\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"[UnknownMetaTag(unknownAttr=\"value\")]\n" +
				"class MyClass\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testUnknownMetaTagOnClassWithMultipleAttributes() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"[UnknownMetaTag(unknownAttr1=\"value1\",unknownAttr2=\"value2\")]\n" +
			"class MyClass\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"[UnknownMetaTag(unknownAttr1=\"value1\", unknownAttr2=\"value2\")]\n" +
				"class MyClass\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testEventMetaTagOnClassWithNameAndTypeAttributes() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"[Event(name=\"change\", type=\"flash.events.Event\")]\n" +
			"class MyClass\n" +
			"{\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"[Event(name=\"change\", type=\"flash.events.Event\")]\n" +
				"class MyClass\n" +
				"{\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testBindableMetaTagOnFieldWithoutAttributes() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"class MyClass\n" +
			"{\n" +
			"\t[Bindable]\n" +
			"\tpublic var myVar:String;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class MyClass\n" +
				"{\n" +
				"\t[Bindable]\n" +
				"\tpublic var myVar:String;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testBindableMetaTagOnFieldWithValue() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"class MyClass\n" +
			"{\n" +
			"\t[Bindable(\"myVarChanged\")]\n" +
			"\tpublic var myVar:String;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class MyClass\n" +
				"{\n" +
				"\t[Bindable(\"myVarChanged\")]\n" +
				"\tpublic var myVar:String;\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testBindableMetaTagOnFieldWithEventAttribute() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceAfterKeywordsInControlFlowStatements = true;
		formatter.placeOpenBraceOnNewLine = true;
		formatter.insertSpaces = false;
		String result = formatter.formatActionScriptText(
		// @formatter:off
			"class MyClass\n" +
			"{\n" +
			"\t[Bindable(event = \"myVarChanged\")]\n" +
			"\tpublic var myVar:String;\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"class MyClass\n" +
				"{\n" +
				"\t[Bindable(event=\"myVarChanged\")]\n" +
				"\tpublic var myVar:String;\n" +
				"}",
				// @formatter:on
				result);
	}
}
