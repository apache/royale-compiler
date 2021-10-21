package org.apache.royale.formatter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestMXMLScript extends BaseFormatterTests {
	@Test
	public void testEmptyScriptNoCdata() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
