/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.parsing.mxml;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.royale.compiler.parsing.MXMLTokenTypes;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for MXMLTokenizer.
 * 
 * @author Gordon Smith
 */
public class MXMLTokenizerTests
{
	/**
	 * Lexes MXML code into MXML tokens.
	 */
	private MXMLToken[] lex(String code)
	{
		Reader reader = new StringReader(code);
		MXMLTokenizer tokenizer = new MXMLTokenizer();
		List<MXMLToken> tokens = tokenizer.parseTokens(reader);
		IOUtils.closeQuietly(tokenizer);
		return tokens.toArray(new MXMLToken[0]);
	}
	
	/*
	 * Smallest processing instruction.
	 */
	@Test
	public void processingInstruction1()
	{
		String code = "<?xml?>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_PROCESSING_INSTRUCTION));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * An entire processing instruction is lexed as a single token.
	 */
	@Test
	public void processingInstruction2()
	{
		String code = "<?xml version='1.0' encoding=\"utf-8\"?>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_PROCESSING_INSTRUCTION));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Smallest normal comment.
	 * This is getting incorrectly lexed as an ASDoc comment.
	 */
	@Ignore
	@Test
	public void comment1()
	{
		String code = "<!---->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Normal comment with some whitespace.
	 */
	@Test
	public void comment2()
	{
		String code = "<!-- -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Normal comment with some text, including whitespace.
	 */
	@Test
	public void comment3()
	{
		String code = "<!-- xxx \t\r\nxxx -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Normal comment with a tag inside.
	 */
	@Test
	public void comment4()
	{
		String code = "<!-- <a/> -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Normal comment with an entity inside.
	 */
	@Test
	public void comment5()
	{
		String code = "<!-- &#65; -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Normal comment with a comment "nested" inside.
	 * Note that the they don't actually nest.
	 */
	@Test
	public void nestedComment()
	{
		String code = "<!-- <!-- --> -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(2));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_COMMENT));
		assertThat("0 text", tokens[0].getText(), is("<!-- <!-- -->"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TEXT));
		assertThat("1 text", tokens[1].getText(), is(" -->"));
	}
	
	/*
	 * Smallest ASDoc comment.
	 */
	@Test
	public void asdoc1()
	{
		String code = "<!----->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_ASDOC_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * ASDoc comment with some whitespace.
	 */
	@Test
	public void asdoc2()
	{
		String code = "<!--- -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_ASDOC_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * ASDoc comment with some text.
	 */
	@Test
	public void asdoc3()
	{
		String code = "<!--- xxx \t\r\nxxx -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_ASDOC_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * ASDoc comment with a tag inside.
	 */
	@Test
	public void asdoc4()
	{
		String code = "<!--- <a/> -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_ASDOC_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * ASDoc comment with an entity inside.
	 */
	@Test
	public void asdoc5()
	{
		String code = "<!---&#65; -->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_ASDOC_COMMENT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * ASDoc comment with an ASDoc comment "nested" inside.
	 * Note that they don't actually nest.
	 */
	@Test
	public void nestedASDoc()
	{
		String code = "<!--- <!--- ---> --->";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(2));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_ASDOC_COMMENT));
		assertThat("0 text", tokens[0].getText(), is("<!--- <!--- --->"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TEXT));
		assertThat("1 text", tokens[1].getText(), is(" --->"));
	}
	
	/*
	 * Pure whitespace of various kinds. TODO: What are all the kinds?
	 */
	@Test
	public void whitespace()
	{
		String code = " \t\r\n";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_WHITESPACE));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Letters with surrounding and contained whitespace are just text.
	 */
	@Test
	public void text1()
	{
		String code = " xxx yyy ";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_TEXT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Digits with surrounding and contained whitespace are just text.
	 */
	@Test
	public void text2()
	{
		String code = " 123 456 ";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_TEXT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * Punctuation characters other than < are just text.
	 */
	@Test
	public void text3()
	{
		String code = "`~!@#$%^&*()-_=+[{]};:'\",./?\\|>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(1));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_TEXT));
		assertThat("0 text", tokens[0].getText(), is(code));
	}
	
	/*
	 * An empty tag has a start token and and end token.
	 */
	@Test
	public void emptyTag1()
	{
		String code = "<a/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(2));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is("/>"));
	}
	
	/*
	 * Whitespace between the start and end is ignored.
	 */
	@Test
	public void emptyTag2()
	{
		String code = "<a />";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(2));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is("/>"));
	}
	
	/*
	 * A single underscore is a legal tag name, at least in XML.
	 */
	@Test
	public void emptyTagName1()
	{
		String code = "<_/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(2));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<_"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is("/>"));
	}
	
	/*
	 * A single colon is a legal tag name, at least in XML.
	 */
	@Test
	public void emptyTagName2()
	{
		String code = "<:/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(2));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<:"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is("/>"));
	}
	
	/*
	 * The non-first character of a tag name can be letter, digit, underscore, colon, period, or hyphen.
	 * Hyphen doesn't currently work.
	 * TODO: What other Unicode characters are allowed?
	 */
	@Ignore
	@Test
	public void emptyTagName3()
	{
		String code = "<azAZ19_:.-/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(2));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<azAZ19_:.-"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is("/>"));
	}
	
	/*
	 * Each attribute produces three tokens.
	 * Note that the text of the attribute value includes the single or double quotes.
	 */
	@Test
	public void emptyTagWithAttributes()
	{
		String code = "<a b='c' d=\"f\"/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(8));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("1 text", tokens[1].getText(), is("b"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("2 text", tokens[2].getText(), is("="));
		
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("3 text", tokens[3].getText(), is("'c'"));
		
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("4 text", tokens[4].getText(), is("d"));
		
		assertThat("5 type", tokens[5].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("5 text", tokens[5].getText(), is("="));
		
		assertThat("6 type", tokens[6].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("6 text", tokens[6].getText(), is("\"f\""));
		
		assertThat("7 type", tokens[7].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("7 text", tokens[7].getText(), is("/>"));
	}
	
	/*
	 * A single underscore is a legal attribute name, at least in XML.
	 */
	@Test
	public void attributeName1()
	{
		String code = "<a _='c'/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("1 text", tokens[1].getText(), is("_"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("2 text", tokens[2].getText(), is("="));
		
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("3 text", tokens[3].getText(), is("'c'"));
				
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is("/>"));
	}
	
	/*
	 * A single colon is a legal attribute name, at least in XML.
	 */
	@Test
	public void attributeName2()
	{
		String code = "<a :='c'/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("1 text", tokens[1].getText(), is(":"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("2 text", tokens[2].getText(), is("="));
		
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("3 text", tokens[3].getText(), is("'c'"));
				
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is("/>"));
	}
	
	/*
	 * Attribute names have the same rules as tag names.
	 */
	@Ignore
	@Test
	public void attributeName3()
	{
		String code = "<a azAZ19_:.-='c'/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("1 text", tokens[1].getText(), is("azAZ19_:.-"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("2 text", tokens[2].getText(), is("="));
		
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("3 text", tokens[3].getText(), is("'c'"));
				
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is("/>"));
	}

	/*
	 * Whitespace as attribute value.
	 */
	@Test
	public void attributeValue1()
	{
		String code = "<a b=' \t\r\n'/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("1 text", tokens[1].getText(), is("b"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("2 text", tokens[2].getText(), is("="));
		
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("3 text", tokens[3].getText(), is("' \t\r\n'"));
				
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is("/>"));
	}
	
	/*
	 * > as attribute value.
	 */
	@Test
	public void attributeValue2()
	{
		String code = "<a b='>'/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("1 text", tokens[1].getText(), is("b"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("2 text", tokens[2].getText(), is("="));
		
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("3 text", tokens[3].getText(), is("'>'"));
				
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is("/>"));
	}
	
	/*
	 * Entity as attribute value.
	 */
	@Test
	public void attributeValue3()
	{
		String code = "<a b='&#65;'/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("1 text", tokens[1].getText(), is("b"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("2 text", tokens[2].getText(), is("="));
		
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("3 text", tokens[3].getText(), is("'&#65;'"));
				
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is("/>"));
	}
	
	/*
	 * Punctuation as attribute value. Note: < and the delimiter are not allowed.
	 */
	@Test
	public void attributeValue4()
	{
		String code = "<a b='`~!@#$%^*()-_=+{}[];:\",./?\\|'/>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_NAME));
	    assertThat("1 text", tokens[1].getText(), is("b"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_EQUALS));
	    assertThat("2 text", tokens[2].getText(), is("="));
		
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_STRING));
	    assertThat("3 text", tokens[3].getText(), is("'`~!@#$%^*()-_=+{}[];:\",./?\\|'"));
				
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is("/>"));
	}
	
	/*
	 * Open/close tags.
	 */
	@Test
	public void openTag_closeTag()
	{
		String code = "<a></a>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(4));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is(">"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_CLOSE_TAG_START));
		assertThat("2 text", tokens[2].getText(), is("</a"));
		
		assertThat("3 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("3 text", tokens[1].getText(), is(">"));
	}
	
	/*
	 * Open/close tags with whitespace content.
	 */
	@Test
	public void openTag_ws_closeTag()
	{
		String code = "<a> \t\r\n</a>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is(">"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_WHITESPACE));
		assertThat("2 text", tokens[2].getText(), is(" \t\r\n"));
	    
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_CLOSE_TAG_START));
		assertThat("3 text", tokens[3].getText(), is("</a"));
		
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is(">"));
	}
	
	/*
	 * Open/close tags with text content.
	 */
	@Test
	public void openTag_text_closeTag1()
	{
		String code = "<a>xxx</a>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is(">"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_TEXT));
		assertThat("2 text", tokens[2].getText(), is("xxx"));
	    
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_CLOSE_TAG_START));
		assertThat("3 text", tokens[3].getText(), is("</a"));
		
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is(">"));
	}
	
	/*
	 * Open/close tags with text content that includes whitespace.
	 */
	@Test
	public void openTag_text_closeTag2()
	{
		String code = "<a> \t\r\nxxx \t\r\n</a>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(5));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is(">"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_TEXT));
		assertThat("2 text", tokens[2].getText(), is(" \t\r\nxxx \t\r\n"));
	    
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_CLOSE_TAG_START));
		assertThat("3 text", tokens[3].getText(), is("</a"));
		
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("4 text", tokens[4].getText(), is(">"));
	}
	
	/*
	 * Open/close tags with an empty tag as content.
	 */
	@Test
	public void openTag_emptyTag_closeTag()
	{
		String code = "<a><b/></a>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(6));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is(">"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("2 text", tokens[2].getText(), is("<b"));
	    
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_EMPTY_TAG_END));
		assertThat("3 text", tokens[3].getText(), is("/>"));
		
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_CLOSE_TAG_START));
	    assertThat("4 text", tokens[4].getText(), is("</a"));
		
	    assertThat("5 type", tokens[5].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("5 text", tokens[5].getText(), is(">"));
	}
	
	/*
	 * Open/close tags with open/close tags nested inside.
	 */
	@Test
	public void openTag_openTag_closeTag_closeTag()
	{
		String code = "<a><b></b></a>";
		MXMLToken[] tokens = lex(code);
		
		assertThat("count", tokens.length, is(8));
		
		assertThat("0 type", tokens[0].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("0 text", tokens[0].getText(), is("<a"));
		
		assertThat("1 type", tokens[1].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("1 text", tokens[1].getText(), is(">"));
		
	    assertThat("2 type", tokens[2].getType(), is(MXMLTokenTypes.TOKEN_OPEN_TAG_START));
		assertThat("2 text", tokens[2].getText(), is("<b"));
	    
		assertThat("3 type", tokens[3].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
		assertThat("3 text", tokens[3].getText(), is(">"));
		
		assertThat("4 type", tokens[4].getType(), is(MXMLTokenTypes.TOKEN_CLOSE_TAG_START));
	    assertThat("4 text", tokens[4].getText(), is("</b"));
		
	    assertThat("5 type", tokens[5].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("5 text", tokens[5].getText(), is(">"));
	   
	    assertThat("6 type", tokens[6].getType(), is(MXMLTokenTypes.TOKEN_CLOSE_TAG_START));
	    assertThat("6 text", tokens[6].getText(), is("</a"));
	    
	    assertThat("7 type", tokens[7].getType(), is(MXMLTokenTypes.TOKEN_TAG_END));
	    assertThat("7 text", tokens[7].getText(), is(">"));
	}
}
