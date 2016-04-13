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
 * Feature tests for the MXML {@code <XMLList>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLXMLListTagTests extends MXMLInstanceTagTestsBase
{
    @Test
    public void MXMLXMLListTag_empty()
    {
        String[] declarations = new String[]
        {
            "<fx:XMLList id='x1'>",
            "</fx:XMLList>"
        };
        String[] asserts = new String[]
        {
        	// An empty tag produces an XMLList with length 0.
            "assertEqual('x1 is XMLList', x1 is XMLList, true);",
            "assertEqual('x1.length()', x1.length(), 0);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLXMLListTag_oneEmptyTag()
    {
        String[] declarations = new String[]
        {
            "<fx:XMLList id='x1' xmlns=''>",
            "    <aaa/>",
            "</fx:XMLList>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XMLList', x1 is XMLList, true);",
            "assertEqual('x1.length()', x1.length(), 1);",
            "assertEqual('x1[0].nodeKind()', x1[0].nodeKind(), 'element');",
            "assertEqual('x1[0].localName()', x1[0].localName(), 'aaa');",
            "assertEqual('x1[0].namespace().uri', x1[0].namespace().uri, '');",
            "assertEqual('x1[0].attributes().length()', x1[0].attributes().length(), 0);",
            "assertEqual('x1[0].children().length()', x1[0].children().length(), 0);",

        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLXMLListTag_twoEmptyTags()
    {
        String[] declarations = new String[]
        {
            "<fx:XMLList id='x1' xmlns=''>",
            "    <aaa/>",
            "    <aaa/>",
            "</fx:XMLList>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XMLList', x1 is XMLList, true);",
            "assertEqual('x1.length()', x1.length(), 2);",
            "assertEqual('x1[0].nodeKind()', x1[0].nodeKind(), 'element');",
            "assertEqual('x1[0].localName()', x1[0].localName(), 'aaa');",
            "assertEqual('x1[0].namespace().uri', x1[0].namespace().uri, '');",
            "assertEqual('x1[0].attribute().length()', x1[0].attributes().length(), 0);",
            "assertEqual('x1[0].children().length()', x1[0].children().length(), 0);",
            "assertEqual('x1[1].nodeKind()', x1[1].nodeKind(), 'element');",
            "assertEqual('x1[1].localName()', x1[1].localName(), 'aaa');",
            "assertEqual('x1[1].namespace().uri', x1[1].namespace().uri, '');",
            "assertEqual('x1[1].attributes().length', x1[1].attributes().length(), 0);",
            "assertEqual('x1[1].children().length()', x1[1].children().length(), 0);",

        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLXMLListTag_oneTagWithAttributes()
    {
        String[] declarations = new String[]
        {
            "<fx:XMLList id='x1' xmlns=''>",
            "    <aaa a='1' b='2'/>",
            "</fx:XMLList>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XMLList', x1 is XMLList, true);",
            "assertEqual('x1.length()', x1.length(), 1);",
            "assertEqual('x1[0].nodeKind()', x1[0].nodeKind(), 'element');",
            "assertEqual('x1[0].localName()', x1[0].localName(), 'aaa');",
            "assertEqual('x1[0].namespace().uri', x1[0].namespace().uri, '');",
            "assertEqual('x1[0].attributes().length()', x1[0].attributes().length(), 2);",
            "assertEqual('x1[0].@a.toString()', x1[0].@a.toString(), '1');",
            "assertEqual('x1[0].@b.toString()', x1[0].@b.toString(), '2');",
            "assertEqual('x1[0].children().length()', x1[0].children().length(), 0);"
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLXMLListTag_oneTagWithChildren()
    {
        String[] declarations = new String[]
        {
            "<fx:XMLList id='x1' xmlns=''>",
            "    <aaa>",
            "       <bbb>xxx</bbb>",
            "       <bbb>yyy</bbb>",
            "    </aaa>",
            "</fx:XMLList>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('x1 is XMLList', x1 is XMLList, true);",
            "assertEqual('x1.length()', x1.length(), 1);",
            "assertEqual('x1[0].nodeKind()', x1[0].nodeKind(), 'element');",
            "assertEqual('x1[0].localName()', x1[0].localName(), 'aaa');",
            "assertEqual('x1[0].namespace().uri', x1[0].namespace().uri, '');",
            "assertEqual('x1[0].attributes().length()', x1[0].attributes().length(), 0);",
            "assertEqual('x1[0].children().length()', x1[0].children().length(), 2);",
            "assertEqual('x1[0].bbb[0].toString()', x1[0].bbb[0].toString(), 'xxx');",
            "assertEqual('x1[0].bbb[1].toString()', x1[0].bbb[1].toString(), 'yyy');"

        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
}
