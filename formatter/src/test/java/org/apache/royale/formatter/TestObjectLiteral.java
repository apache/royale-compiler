package org.apache.royale.formatter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestObjectLiteral extends BaseFormatterTests {
	@Test
	public void testEmpty() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
		// @formatter:off
			"var obj:Object = {};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {};",
				// @formatter:on
				result);
	}

	@Test
	public void testOneFieldOnSingleLine() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
		// @formatter:off
			"var obj:Object = {one: 123.4};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {one: 123.4};",
				// @formatter:on
				result);
	}

	@Test
	public void testOneFieldOnMultipleLines() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
		// @formatter:off
			"var obj:Object = {\n" +
			"\tone: 123.4\n" +
			"};\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {\n" +
				"\t\tone: 123.4\n" +
				"\t};\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleFields() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
		// @formatter:off
			"var obj:Object = {one: 123.4, two: \"hello world\"};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {one: 123.4, two: \"hello world\"};",
				// @formatter:on
				result);
	}

	@Test
	public void testMultipleFieldsOnMultipleLines() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
		// @formatter:off
			"var obj:Object = {\n" +
			"\tone: 123.4,\n" +
			"\ttwo: \"hello world\"\n" +
			"};\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {\n" +
				"\t\tone: 123.4,\n" +
				"\t\ttwo: \"hello world\"\n" +
				"\t};\n" +
				"statement;",
				// @formatter:on
				result);
	}


	@Test
	public void testMultipleFieldsOnMultipleLines2() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
		// @formatter:off
			"var obj:Object = {\n" +
			"\tone: 123.4,\n" +
			"\ttwo: \"hello world\", three: true\n" +
			"};\n" +
			"statement;",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {\n" +
				"\t\tone: 123.4,\n" +
				"\t\ttwo: \"hello world\", three: true\n" +
				"\t};\n" +
				"statement;",
				// @formatter:on
				result);
	}

	@Test
	public void testStringField() {
		FORMATTER formatter = new FORMATTER();
		formatter.insertSpaceBeforeAndAfterBinaryOperators = true;
		String result = formatter.formatText(
		// @formatter:off
			"var obj:Object = {\"string-field\": 123.4};",
			// @formatter:on
			problems
		);
		assertEquals(
		// @formatter:off
				"var obj:Object = {\"string-field\": 123.4};",
				// @formatter:on
				result);
	}
}
