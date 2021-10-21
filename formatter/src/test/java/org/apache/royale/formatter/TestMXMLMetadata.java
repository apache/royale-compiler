package org.apache.royale.formatter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestMXMLMetadata extends BaseFormatterTests {
	@Test
	public void testEmptyMetadataNoCdata() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaces = false;
		String result = formatter.formatMXMLText(
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
