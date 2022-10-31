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

public class TestMXMLTag extends BaseFormatterTests {
	@Test
	public void testSelfClosingTag() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag />",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag/>",
				// @formatter:on
				result);
	}

	@Test
	public void testTagWithEmptyText() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag></s:Tag>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag></s:Tag>",
				// @formatter:on
				result);
	}

	@Test
	public void testTagWithText() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag>Hello World</s:Tag>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag>Hello World</s:Tag>",
				// @formatter:on
				result);
	}

	@Test
	public void testTagWithTextAndNewLines() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag>\n" +
			"\tHello World\n" +
			"</s:Tag>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag>\n" +
				"\tHello World\n" +
				"</s:Tag>",
				// @formatter:on
				result);
	}

	@Test
	public void testTagWithNewLineText() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag>\n" +
			"</s:Tag>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag>\n" +
				"</s:Tag>",
				// @formatter:on
				result);
	}

	@Test
	public void testNewLinesBetweenTags() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag>\n" +
			"\n" +
			"</s:Tag>\n" +
			"\n" +
			"<s:Tag/>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag>\n" +
				"\n" +
				"</s:Tag>\n" +
				"\n" +
				"<s:Tag/>",
				// @formatter:on
				result);
	}

	@Test
	public void testExcessWhitespaceBetweenTags() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag>\t\n" +
			"\n\t" +
			"\t</s:Tag>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag>\n" +
				"\n" +
				"</s:Tag>",
				// @formatter:on
				result);
	}

	@Test
	public void testMixedTextAndTagChildren1() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag>text <s:Tag/></s:Tag>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag>text <s:Tag/></s:Tag>",
				// @formatter:on
				result);
	}

	@Test
	public void testMixedTextAndTagChildren2() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag><s:Tag/> text</s:Tag>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag><s:Tag/> text</s:Tag>",
				// @formatter:on
				result);
	}

	@Test
	public void testMixedTextAndTagChildren3() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag><s:Tag/> text <s:Tag/></s:Tag>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag><s:Tag/> text <s:Tag/></s:Tag>",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleAttributes() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		settings.mxmlInsertNewLineBetweenAttributes = false;
		settings.mxmlAlignAttributes = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag one=\"1\"   two=\"2\"/>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag one=\"1\" two=\"2\"/>",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleAttributesOnePerLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		settings.mxmlInsertNewLineBetweenAttributes = true;
		settings.mxmlAlignAttributes = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag one=\"1\" two=\"2\"/>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag one=\"1\"\n" +
				"\ttwo=\"2\"/>",
				// @formatter:on
				result);
	}

	@Test
	public void testMXMLAlignAttributes() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		settings.mxmlInsertNewLineBetweenAttributes = true;
		settings.mxmlAlignAttributes = true;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Tag one=\"1\" two=\"2\" three=\"3\"/>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Tag one=\"1\"\n" +
				"\t   two=\"2\"\n" +
				"\t   three=\"3\"/>",
				// @formatter:on
				result);
	}
}