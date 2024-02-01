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
 * Base class for feature tests for MXML instance tags.
 * 
 * @author Gordon Smith
 */
public class MXMLInstanceTagTests extends MXMLInstanceTagTestsBase
{
    @Test
    public void MXMLInstanceTag_basic()
    {
        String[] declarations = new String[]
        {
            "<custom:TestInstance id='b'/>"
        };
        String[] asserts = new String[]
        {
	        "assertEqual('b', b is TestInstance, true);"	
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLInstanceTag_propertiesAsAttributes()
    {
        String[] declarations = new String[]
        {
            "<custom:TestInstance id='b' name='whatever' value='123.4' color='0xff9900' visible='true'/>"
        };
        String[] asserts = new String[]
        {
	        "assertEqual('b', b is TestInstance, true);",
	        "assertEqual('b.name', b.name, 'whatever');",
	        "assertEqual('b.value', b.value, 123.4);",
	        "assertEqual('b.color', b.color, 0xff9900);",
	        "assertEqual('b.visible', b.visible, true);"
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLInstanceTag_propertiesAsTags()
    {
        String[] declarations = new String[]
        {
            "<custom:TestInstance id='b'>",
            "  <custom:name>whatever</custom:name>",
            "  <custom:value>123.4</custom:value>",
            "  <custom:color>0xff9900</custom:color>",
            "  <custom:visible>true</custom:visible>",
            "</custom:TestInstance>"
        };
        String[] asserts = new String[]
        {
	        "assertEqual('b', b is TestInstance, true);",
	        "assertEqual('b.name', b.name, 'whatever');",
	        "assertEqual('b.value', b.value, 123.4);",
	        "assertEqual('b.color', b.color, 0xff9900);",
	        "assertEqual('b.visible', b.visible, true);"
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
}
