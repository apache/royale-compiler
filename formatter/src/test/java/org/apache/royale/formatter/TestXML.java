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

public class TestXML extends BaseFormatterTests {
	@Test
	public void testEmptyRootElementOnOneLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"<root></root>;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<root></root>;",
				// @formatter:on
				result);
	}

	@Test
	public void testEmptyRootElementOnMultipleLines() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"<root>\n" +
			"</root>;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<root>\n" +
				"</root>;",
				// @formatter:on
				result);
	}

	@Test
	public void testSelfClosingRootElement() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceBeforeAndAfterBinaryOperators = true;
		settings.placeOpenBraceOnNewLine = false;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"<root/>;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<root/>;",
				// @formatter:on
				result);
	}
}
