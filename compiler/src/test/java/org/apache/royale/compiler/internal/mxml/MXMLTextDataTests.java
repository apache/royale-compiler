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

package org.apache.royale.compiler.internal.mxml;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import org.apache.royale.compiler.mxml.IMXMLTextData;
import org.apache.royale.compiler.mxml.IMXMLUnitData;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLTextData}.
 * 
 * @author Gordon Smith
 */
public class MXMLTextDataTests extends MXMLUnitDataTests
{
	private IMXMLTextData getMXMLTextData(String[] code)
	{
		IMXMLUnitData unitData = getMXMLUnitData(code);
		assertThat("instanceOf", unitData, is(instanceOf(IMXMLTextData.class)));
		return (IMXMLTextData)unitData;
	}

	@Test
	public void MXMLTextData_text()
	{
		String[] code = new String[]
		{
			"abc"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.TEXT));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(code[0]));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}

	@Test
	public void MXMLTextData_textWithWhitespace()
	{
		String[] code = new String[]
		{
			" a b c "
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.TEXT));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(code[0]));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}

	
	@Test
	public void MXMLTextData_text_entity()
	{
		String[] code = new String[]
		{
			"&lt;"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.TEXT));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(code[0]));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
	
	@Test
	public void MXMLTextData_text_databinding()
	{
		String[] code = new String[]
		{
			"{abc}"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.TEXT));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(code[0]));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}

	@Test
	public void MXMLTextData_whitespace()
	{
		String[] code = new String[]
		{
			" \t\r\n"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.WHITESPACE));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(code[0]));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}

	@Test
	public void MXMLTextData_comment_empty()
	{
		String[] code = new String[]
		{
			"<!-- -->"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.COMMENT));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(""));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
	
	@Test
	public void MXMLTextData_comment()
	{
		String[] code = new String[]
		{
			"<!--abc-->"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.COMMENT));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(""));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
	
	@Test
	public void MXMLTextData_ASDoc_empty()
	{
		String[] code = new String[]
		{
			"<!--- -->"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.ASDOC));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(""));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
	
	@Test
	public void MXMLTextData_ASDoc()
	{
		String[] code = new String[]
		{
			"<!---abc-->"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.ASDOC));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(""));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
	
	@Test
	public void MXMLTextData_CDATA_empty()
	{
		String[] code = new String[]
		{
			"<![CDATA[]]>"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.CDATA));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(""));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
	
	@Test
	public void MXMLTextData_CDATA_whitespace()
	{
		String[] code = new String[]
		{
			"<![CDATA[ \t\r\n]]>"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.CDATA));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is(" \t\r\n"));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
	
	@Test
	public void MXMLTextData_CDATA_abc()
	{
		String[] code = new String[]
		{
			"<![CDATA[abc]]>"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.CDATA));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is("abc"));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
	
	@Test
	public void MXMLTextData_CDATA_metacharacters()
	{
		String[] code = new String[]
		{
			"<![CDATA[<&>]]>"
		};
		IMXMLTextData textData = getMXMLTextData(code);
		assertThat("getTextType", textData.getTextType(), is(IMXMLTextData.TextType.CDATA));
		assertThat("getContent", textData.getContent(), is(code[0]));
		assertThat("getCompilableText", textData.getCompilableText(), is("<&>"));
		//assertThat("getCompilableTextStart", textData.getCompilableTextStart(), is(0));
	}
}
