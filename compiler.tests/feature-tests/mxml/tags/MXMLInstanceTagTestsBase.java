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

import org.apache.flex.utils.StringUtils;

/**
 * Base class for feature tests for MXML instance tags.
 * 
 * @author Gordon Smith
 */
public class MXMLInstanceTagTestsBase extends MXMLFeatureTestsBase
{
	/**
	 * Combines various code snippets to make a complete one-file MXML Sprite-based application.
	 */
    protected String getMXML(String[] declarations, String[] asserts)
    {
        String[] template = new String[]
        {
            "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*'",
            "          enterFrame='enterFrameHandler(event)'>",
            "    <fx:Declarations>",
            "        %1",
            "    </fx:Declarations>",
            "    <fx:Script>",
            "    <![CDATA[",
            "        private function assertEqual(message:String, actualValue:*, expectedValue:*):void",
            "        {",
            "            if (actualValue !== expectedValue)",
            "                trace(message, actualValue, expectedValue);",
            "        }",
            "        private function enterFrameHandler(event:Event):void",
            "        {",
            "            %2",
            "        }",
            "    ]]>",
            "    </fx:Script>",
            "</d:Sprite>"
        };
        String mxml = StringUtils.join(template, "\n");
        mxml = mxml.replace("%1", StringUtils.join(declarations, "\n        "));
        mxml = mxml.replace("%2", StringUtils.join(asserts, "\n            "));
        return mxml;
    }
}
