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
 * Feature tests for the MXML {@code <Model>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLModelTagTests extends MXMLInstanceTagTestsBase
{
	// This <Script> function is used by some of the asserts in the tests below
	// to count the number of properties of an Object.
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
	
	//@Ignore
    @Test
    public void MXMLModelTag_empty()
    {
        String[] declarations = new String[]
        {
            "<fx:Model id='m1'>",
            "</fx:Model>"
        };
        String[] asserts = new String[]
        {
        	"import mx.utils.ObjectProxy;",
            "assertEqual('m1 is ObjectProxy', m1 is ObjectProxy, true);",
            "assertEqual('countProperties(m1)', countProperties(m1), 0);"
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, false, false, null);
    }
	
    @Test
    public void MXMLModelTag_emptyRoot()
    {
        String[] declarations = new String[]
        {
            "<fx:Model id='m1'>",
            "    <root/>",
            "</fx:Model>"
        };
        String[] asserts = new String[]
        {
            "import mx.utils.ObjectProxy;",
            "assertEqual('m1 is ObjectProxy', m1 is ObjectProxy, true);",
            "assertEqual('countProperties(m1)', countProperties(m1), 0);"
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, false, false, null);
    }
    
    @Test
    public void MXMLModelTag_oneTag()
    {
        String[] declarations = new String[]
        {
            "<fx:Model id='m1'>",
            "    <root>",
            "        <a>xxx</a>",
            "    </root>",
            "</fx:Model>"
        };
        String[] asserts = new String[]
        {
            "import mx.utils.ObjectProxy;",
            "assertEqual('m1 is ObjectProxy', m1 is ObjectProxy, true);",
            "assertEqual('countProperties(m1)', countProperties(m1), 1);",
            "assertEqual('m1.a', m1.a, 'xxx');"
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, false, false, null);
    }
    
    @Test
    public void MXMLModelTag_fourTags()
    {
        String[] declarations = new String[]
        {
            "<fx:Model id='m1'>",
            "    <root>",
            "        <a>1</a>",
            "        <b>2</b>",
            "        <a>3</a>",
            "        <b>4</b>",
            "    </root>",
            "</fx:Model>"
        };
        String[] asserts = new String[]
        {
            "import mx.utils.ObjectProxy;",
            "assertEqual('m1 is ObjectProxy', m1 is ObjectProxy, true);",
            "assertEqual('countProperties(m1)', countProperties(m1), 2);",
            "assertEqual('m1.a.length', m1.a.length, 2);",
            "assertEqual('m1.a[0]', m1.a[0], 1);",
            "assertEqual('m1.a[1]', m1.a[1], 3);",
            "assertEqual('m1.b.length', m1.b.length, 2);",
            "assertEqual('m1.b[0]', m1.b[0], 2);",
            "assertEqual('m1.b[1]', m1.b[1], 4);"
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, false, false, null);
    }
    
    @Test
    public void MXMLModelTag_attributes()
    {
        String[] declarations = new String[]
        {
            "<fx:Model id='m1'>",
            "    <root>",
            "        <a b='1' c='2'/>",
            "    </root>",
            "</fx:Model>"
        };
        String[] asserts = new String[]
        {
            "import mx.utils.ObjectProxy;",
            "assertEqual('m1 is ObjectProxy', m1 is ObjectProxy, true);",
            "assertEqual('countProperties(m1)', countProperties(m1), 1);",
            "assertEqual('countProperties(m1.a)', countProperties(m1.a), 2);",
            "assertEqual('m1.a.b', m1.a.b, 1);",
            "assertEqual('m1.a.c', m1.a.c, 2);"
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, false, false, null);
    }
    

}
