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

public class TestMemberAccess extends BaseFormatterTests {
	@Test
	public void testMemberAccess() {
		FormatterSettings settings = new FormatterSettings();
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"this.that;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"this.that;",
				// @formatter:on
				result);
	}

	@Test
	public void testMemberAccessWithNewLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"this\n.that;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"this\n\t.that;",
				// @formatter:on
				result);
	}

	@Test
	public void testNullConditionalWithNewLine() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"this\n?.that;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"this\n\t?.that;",
				// @formatter:on
				result);
	}
}
