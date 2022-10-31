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

public class TestPreserveNewLines extends BaseFormatterTests {
	@Test
	public void testNoMaxPreserveNewLines() {
		FormatterSettings settings = new FormatterSettings();
		settings.maxPreserveNewLines = 0;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"/* this is a comment */\n" +
			"\n" +
			"\n" +
			"/* this is a comment */",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"/* this is a comment */\n" +
				"\n" +
				"\n" +
				"/* this is a comment */",
				// @formatter:on
				result);
	}

	@Test
	public void testMaxPreserveNewLines() {
		FormatterSettings settings = new FormatterSettings();
		settings.maxPreserveNewLines = 2;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"/* this is a comment */\n" +
			"\n" +
			"\n" +
			"/* this is a comment */",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"/* this is a comment */\n" +
				"\n" +
				"/* this is a comment */",
				// @formatter:on
				result);
	}

	@Test
	public void testNewLinesBeforeEndOfStatement() {
		FormatterSettings settings = new FormatterSettings();
		settings.maxPreserveNewLines = 0;
		settings.insertSpaces = false;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"1 + 2" +
			"\n" +
			"\n" +
			"+ 3\n" +
			"\t+ 4;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"1 + 2" +
				"\n" +
				"\n" +
				"\t+ 3\n" +
				"\t+ 4;",
				// @formatter:on
				result);
	}
}
