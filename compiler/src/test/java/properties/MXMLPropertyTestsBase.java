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

package properties;

import mxml.tags.MXMLFeatureTestsBase;

import org.apache.royale.utils.StringUtils;

/**
 * Base class for feature tests for MXML property tags and attributes.
 * 
 * @author Gordon Smith
 */
public abstract class MXMLPropertyTestsBase extends MXMLFeatureTestsBase
{
    protected String[] getTemplate()
    {
    	if (hasFlashPlayerGlobal)
    	{
	   	    // Property-node tests use this template, which declares a component
			// with a property of a particular type. The tests then set the
			// property on a <MyComp> tag inside the <Declarations> tag.
	        return new String[]
	        {
	    	    "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009'",
	    	    "          xmlns:d='flash.display.*'",
	    	    "          xmlns='*'",
	            "          enterFrame='enterFrameHandler(event)'>",
	    	    "    <fx:Declarations>",
	    		"        <fx:Component className='MyComp'>",
	    		"            <d:Sprite>",
	    	    "                <fx:Script>",
	    		"                    public var p:%1;",
	    	    "                </fx:Script>",
	    		"            </d:Sprite>",
	    		"        </fx:Component>",
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
    	}
        return new String[]
        {
    	    "<fx:Object xmlns:fx='http://ns.adobe.com/mxml/2009'",
    	    "          xmlns:custom='library://ns.apache.org/royale/test'",
    	    "          xmlns='*'",
            "          >",
    	    "    <fx:Declarations>",
    		"        <fx:Component className='MyComp'>",
    		"            <custom:TestInstance>",
    	    "                <fx:Script>",
    		"                    public var p:%1;",
    	    "                </fx:Script>",
    		"            </custom:TestInstance>",
    		"        </fx:Component>",
    		"        %2",
    	    "    </fx:Declarations>",
            "    <fx:Script>",
            "    <![CDATA[",
            "        private function assertEqual(message:String, actualValue:*, expectedValue:*):void",
            "        {",
            "        }",
            "    ]]>",
            "    </fx:Script>",
    	    "</d:Sprite>"
        };
    }
    
	// Note: The presence of <fx:Component> means that these tests must compile against framework.swc
    // in order to find mx.core.ClassFactory.
    
    // TODO: When mx.core.ClassFactory isn't found, we currently don't report a compilation error
    // and instead compile a SWF that doesn't verify.
    
    protected String getMXML(String[] declarations, String[] asserts)
    {
        String mxml = StringUtils.join(getTemplate(), "\n");
        mxml = mxml.replace("%1", getPropertyType());
        mxml = mxml.replace("%2", StringUtils.join(declarations, "\n        "));
        mxml = mxml.replace("%3", StringUtils.join(asserts, "\n            "));
        return mxml;
    }
	
	abstract protected String getPropertyType();
}
