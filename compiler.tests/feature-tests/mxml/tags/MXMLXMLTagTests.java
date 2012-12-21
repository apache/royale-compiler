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

import org.junit.Ignore;
import org.junit.Test;

/**
 * Feature tests for the MXML {@code <XML>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLXMLTagTests extends MXMLInstanceTagTestsBase
{
	// This <Script> function is used by some of the asserts in the tests below
	// to count the number of properties of an <Object>.
	private static String[] scriptDeclarations = new String[]
    {
        "private function countProperties(o:Object):int",
        "{",
        "    var count:int = 0;",
        "    for (var p:String in o)",
        "    {",
        "       count++;",
        "    }",
        "    return count;",
        "}"
	};
	
	//
	// These tests are for XML tags with an implicit format="e4x" 
	//
	
    @Test
    public void MXMLXMLTag_formatE4X_empty()
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
    public void MXMLXMLTag_formatE4X_emptyRootTag()
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
    public void MXMLXMLTag_formatE4X_oneTagWithText()
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
    public void MXMLXMLTag_formatE4X_oneTagWithAttributes()
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
    public void MXMLXMLTag_formatE4X_oneTagWithTwoChildTags()
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
    
	//
	// These tests are for XML tags with format="xml",
    // which produce a tree of XMLNode objects.
    // These tests have to be compiled against framework.swc
    // in order to get the mx.utils.XMLUtils class.
	//
    
    // This test seems to be failing because our xmlString is
    // <root xmlns=""/>
    // while the old compiler's was
    // <root/>
    @Ignore
    @Test
    public void MXMLXMLTag_formatXML_emptyRootTag()
    {
        String[] declarations = new String[]
        {
            "<fx:XML id='x1' xmlns='' format='xml'>",
            "    <root/>",
            "</fx:XML>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XMLNode', x1 is XMLNode, true);",
            "trace(x1.localName === null);",
            "assertEqual('x1.localName', x1.localName, 'root');",
            "assertEqual('x1.namespaceURI', x1.namespaceURI, null);",
            "assertEqual('countProperties(x1.attributes)', countProperties(x1.attributes), 0);",
            "assertEqual('x1.childNodes.length', x1.childNodes.length, 0);"
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, false, false, null);
    }
}
