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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
		// @formatter:off
			"<s:Tag/>",
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		formatter.mxmlInsertNewLineBetweenAttributes = false;
		formatter.mxmlAlignAttributes = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		formatter.mxmlInsertNewLineBetweenAttributes = true;
		formatter.mxmlAlignAttributes = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		formatter.mxmlInsertNewLineBetweenAttributes = true;
		formatter.mxmlAlignAttributes = true;
		String result = formatter.formatMXMLText(
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