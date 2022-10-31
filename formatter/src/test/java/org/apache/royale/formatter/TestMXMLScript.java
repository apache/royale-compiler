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

public class TestMXMLScript extends BaseFormatterTests {
	@Test
	public void testEmptyScriptNoCdata() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Application>\n" +
			"<fx:Script>\n" +
			"</fx:Script>\n" +
			"</s:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Application>\n" +
				"\t<fx:Script>\n" +
				"\t\t<![CDATA[\n" +
				"\t\t]]>\n" +
				"\t</fx:Script>\n" +
				"</s:Application>",
				// @formatter:on
				result);
	}

	@Test
	public void testEmptyScriptWithCdata() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Application>\n" +
			"<fx:Script>\n" +
			"<![CDATA[\n" +
			"]]>\n" +
			"</fx:Script>\n" +
			"</s:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Application>\n" +
				"\t<fx:Script>\n" +
				"\t\t<![CDATA[\n" +
				"\t\t]]>\n" +
				"\t</fx:Script>\n" +
				"</s:Application>",
				// @formatter:on
				result);
	}

	@Test
	public void testScriptWithActionScript() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<s:Application>\n" +
			"<fx:Script>\n" +
			"<![CDATA[\n" +
			"public var a: Number=123.4;\n" +
			"]]>\n" +
			"</fx:Script>\n" +
			"</s:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<s:Application>\n" +
				"\t<fx:Script>\n" +
				"\t\t<![CDATA[\n" +
				"\t\t\tpublic var a:Number = 123.4;\n" +
				"\t\t]]>\n" +
				"\t</fx:Script>\n" +
				"</s:Application>",
				// @formatter:on
				result);
	}
	
}
