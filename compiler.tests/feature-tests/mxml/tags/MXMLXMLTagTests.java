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

package mxml.tags;

import org.junit.Test;

/**
 * Feature tests for the MXML <XML> tag.
 * 
 * @author Gordon Smith
 */
public class MXMLXMLTagTests extends MXMLInstanceTagTestsBase
{
    @Test
    public void MXMLXMLTag_empty()
    {
        String[] declarations = new String[]
        {
            "<fx:XML id='x1'>",
            "</fx:XML>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1', x1, null);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
	
    @Test
    public void MXMLXMLTag_emptyRootTag()
    {
        String[] declarations = new String[]
        {
            "<fx:XML id='x1' xmlns=''>",
            "    <root/>",
            "</fx:XML>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XML', x1 is XML, true);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
	
    @Test
    public void MXMLXMLTag_oneTagWithText()
    {
        String[] declarations = new String[]
        {
            "<fx:XML id='x1' xmlns=''>",
            "    <root>",
            "        <a>abc</a>",
            "    </root>",
            "</fx:XML>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XML', x1 is XML, true);",
            "assertEqual('x1.a.toString()', x1.a.toString(), 'abc');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
	
    @Test
    public void MXMLXMLTag_oneTagWithAttributes()
    {
        String[] declarations = new String[]
        {
            "<fx:XML id='x1' xmlns=''>",
            "    <root>",
            "        <a b='1' c='2'/>",
            "    </root>",
            "</fx:XML>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XML', x1 is XML, true);",
            "assertEqual('x1.a.@b.toString()', x1.a.@b.toString(), '1');",
            "assertEqual('x1.a.@c.toString()', x1.a.@c.toString(), '2');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    	
    @Test
    public void MXMLXMLTag_oneTagWithTwoChildTags()
    {
        String[] declarations = new String[]
        {
            "<fx:XML id='x1' xmlns=''>",
            "    <root>",
            "        <a>",
            "            <b>b0</b>",
            "            <b>b1</b>",
            "        </a>",
            "    </root>",
            "</fx:XML>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XML', x1 is XML, true);",
            "assertEqual('x1.a.b[0]', x1.a.b[0].toString(), 'b0');",
            "assertEqual('x1.a.b[1]', x1.a.b[1].toString(), 'b1');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
}
