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

public class TestFormatterOff extends BaseFormatterTests {
	@Test
	public void testAS3FormatterOff() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = true;
		settings.tabSize = 2;
		settings.maxPreserveNewLines = 2;
		ASTokenFormatter formatter = new ASTokenFormatter(settings);
		String result = formatter.format("file.as",
		// @formatter:off
			"\t// @formatter:off\n" +
			"for(var i:int=0;i<3;i++){\n" +
			"\ttrace(i)//print to console\n" +
			"\n" +
			"\n" +
			"\n" +
			"}\n" +
			"\t// @formatter:on\n" +
			"for(var i:int=0;i<3;i++){\n" +
			"\ttrace(i)//print to console\n" +
			"\n" +
			"\n" +
			"\n" +
			"}",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"// @formatter:off\n" +
				"for(var i:int=0;i<3;i++){\n" +
				"\ttrace(i)//print to console\n" +
				"\n" +
				"\n" +
				"\n" +
				"}\n" +
				"\t// @formatter:on\n" +
				"for (var i:int = 0; i < 3; i++)\n" +
				"{\n" +
				"  trace(i); // print to console\n" +
				"\n" +
				"}",
				// @formatter:on
				result);
	}

	@Test
	public void testMXMLFormatterOff() {
		FormatterSettings settings = new FormatterSettings();
		settings.insertSpaceAfterKeywordsInControlFlowStatements = true;
		settings.placeOpenBraceOnNewLine = true;
		settings.insertSpaces = true;
		settings.tabSize = 2;
		settings.maxPreserveNewLines = 2;
		MXMLTokenFormatter formatter = new MXMLTokenFormatter(settings);
		String result = formatter.format("file.mxml",
		// @formatter:off
			"<mx:Application>\n" +
			"\t<!-- @formatter:off -->\n" +
			"\t<mx:Button />\n" +
			"\n" +
			"\n" +
			"\n" +
			"\t<!-- @formatter:on -->\n" +
			"\t<mx:Button />\n" +
			"\n" +
			"\n" +
			"\n" +
			"</mx:Application>",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"<mx:Application>\n" +
				"  <!-- @formatter:off -->\n" +
				"\t<mx:Button />\n" +
				"\n" +
				"\n" +
				"\n" +
				"\t<!-- @formatter:on -->\n" +
				"  <mx:Button/>\n" +
				"\n" +
				"</mx:Application>",
				// @formatter:on
				result);
	}
}