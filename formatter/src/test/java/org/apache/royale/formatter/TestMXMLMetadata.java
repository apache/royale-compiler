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

public class TestMXMLMetadata extends BaseFormatterTests {
	@Test
	public void testEmptyMetadataNoCdata() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Application>\n" +
			"<fx:Metadata>\n" +
			"</fx:Metadata>\n" +
			"</s:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Application>\n" +
				"\t<fx:Metadata>\n" +
				"\t</fx:Metadata>\n" +
				"</s:Application>",
				// @formatter:on
				result);
	}

	@Test
	public void testEmptyMetadataWithCdata() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Application>\n" +
			"<fx:Metadata>\n" +
			"<![CDATA[\n" +
			"]]>\n" +
			"</fx:Metadata>\n" +
			"</s:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Application>\n" +
				"\t<fx:Metadata>\n" +
				"\t\t<![CDATA[\n" +
				"\t\t]]>\n" +
				"\t</fx:Metadata>\n" +
				"</s:Application>",
				// @formatter:on
				result);
	}

	@Test
	public void testSingleMetadata() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Application>\n" +
			"<fx:Metadata>\n" +
			"[UnknownMetaTag]\n" +
			"</fx:Metadata>\n" +
			"</s:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Application>\n" +
				"\t<fx:Metadata>\n" +
				"\t\t[UnknownMetaTag]\n" +
				"\t</fx:Metadata>\n" +
				"</s:Application>",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleMetadata() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Application>\n" +
			"<fx:Metadata>\n" +
			"[UnknownMetaTag1]\n" +
			"[UnknownMetaTag2]\n" +
			"</fx:Metadata>\n" +
			"</s:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Application>\n" +
				"\t<fx:Metadata>\n" +
				"\t\t[UnknownMetaTag1]\n" +
				"\t\t[UnknownMetaTag2]\n" +
				"\t</fx:Metadata>\n" +
				"</s:Application>",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleMetadataWithAttributes() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Application>\n" +
			"<fx:Metadata>\n" +
			"[UnknownMetaTag1(attr1=\"one\", attr2=\"two\")]\n" +
			"[UnknownMetaTag2(attr1=\"one\")]\n" +
			"</fx:Metadata>\n" +
			"</s:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Application>\n" +
				"\t<fx:Metadata>\n" +
				"\t\t[UnknownMetaTag1(attr1=\"one\", attr2=\"two\")]\n" +
				"\t\t[UnknownMetaTag2(attr1=\"one\")]\n" +
				"\t</fx:Metadata>\n" +
				"</s:Application>",
				// @formatter:on
				result);
	}
	
}
