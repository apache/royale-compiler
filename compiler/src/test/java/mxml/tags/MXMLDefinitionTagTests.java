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

import org.apache.royale.utils.StringUtils;
import org.junit.Test;

/**
 * Feature tests for the MXML {@code <Definition>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLDefinitionTagTests extends MXMLInstanceTagTestsBase
{
	/**
	 * Combines various code snippets to make a complete one-file MXML Sprite-based application.
	 */
    protected String getMXML(String[] definitions, String[] declarations, String[] asserts)
    {
        String[] template = new String[]
        {
            "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*' ",
            "          enterFrame='enterFrameHandler(event)'>",
            "    <fx:Library>",
            "        %1",
            "    </fx:Library>",
            "    <fx:Declarations>",
            "        %2",
            "    </fx:Declarations>",
            "    <fx:Script>",
            "    <![CDATA[",
            "        private function assertEqual(message:String, actualValue:*, expectedValue:*):void",
            "        {",
            "            if (actualValue !== expectedValue)",
            "            {",
            "                trace(message, actualValue, expectedValue);",
            "                System.exit(1);",
            "            }",
            "        }",
            "        private function enterFrameHandler(event:Event):void",
            "        {",
            "            %3",
            "            System.exit(0);",
            "        }",
            "    ]]>",
            "    </fx:Script>",
            "</d:Sprite>"
        };
        String mxml = StringUtils.join(template, "\n");
        mxml = mxml.replace("%1", StringUtils.join(definitions, "\n        "));
        mxml = mxml.replace("%2", StringUtils.join(declarations, "\n        "));
        mxml = mxml.replace("%3", StringUtils.join(asserts, "\n            "));
        return mxml;
    }
    
    @Test
    public void MXMLDefinitionTag_basic()
    {
        String[] definitions = new String[]
        {
            "<fx:Definition name='MySprite'>",
            "    <d:Sprite/>",
            "</fx:Definition>"
        };
        String[] declarations = new String[]
        {
            "<fx:MySprite id='s1'/>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('s1 is Sprite', s1 is Sprite, true);",
        };
        String mxml = getMXML(definitions, declarations, asserts);
        compileAndRun(mxml);
    }
}
