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
 * Feature tests for the MXML {@code <Metadata>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLMetadataTagTests extends MXMLFeatureTestsBase
{
	/**
	 * Combines various code snippets to make a complete one-file MXML Sprite-based application.
	 */
    protected String getMXML(String[] metadata, String[] moreMetadata, String[] asserts)
    {
        String[] template;
        if (hasFlashPlayerGlobal)
        {
        	template = new String[]
	        {
	            "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*' xmlns:custom='library://ns.apache.org/royale/test'",
	            "          enterFrame='enterFrameHandler(event)'>",
	            "    %1",
	            "    <fx:Script>",
	            "    <![CDATA[",
	            "        import custom.TestInstance;",
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
	            "    %2",
	            "</d:Sprite>"
	        };
        }
        else
        {
        	template = new String[]
  	        {
  	            "<fx:Object xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:custom='library://ns.apache.org/royale/test' >",
  	            "    %1",
  	            "    <fx:Script>",
  	            "    <![CDATA[",
	            "        import custom.TestInstance;",
  	            "        private function assertEqual(message:String, actualValue:*, expectedValue:*):void",
  	            "        {",
  	            "        }",
  	            "    ]]>",
  	            "    </fx:Script>",
  	            "    %2",
  	            "</fx:Object>"
  	        };        	
        }
        String mxml = StringUtils.join(template, "\n");
        mxml = mxml.replace("%1", StringUtils.join(metadata, "\n    "));
        mxml = mxml.replace("%2", StringUtils.join(moreMetadata, "\n    "));
        mxml = mxml.replace("%3", StringUtils.join(asserts, "\n            "));
        return mxml;
    }
    
    protected String getMXML(String[] metadata, String[] asserts)
    {
    	return getMXML(metadata, new String[0], asserts);
    }
    
	@Test
	public void MXMLMetadataTag_oneTag()
	{
        String[] metadata = new String[]
        {
            "<fx:Metadata>",
            "    [AAA(a1='1', a2='2')]",
            "</fx:Metadata>"
        };
        String[] asserts = new String[]
        {
            // The <Metadata> tag should produce class-level metadata
            // that we can inspect at runtime with describeType().
            // Inside the root tag returned by describeType() should be
            //     <metadata name="AAA">
            //         <arg key="a1" value="1"/>
            //         <arg key="a2" value="2"/>
            //     </metadata>
        	"var dt:XML = describeType(this);",
        	"var aaa:XMLList = dt.metadata.(@name == 'AAA');",
            "assertEqual('aaa.arg[0].@key.toString()', aaa.arg[0].@key.toString(), 'a1');",
            "assertEqual('aaa.arg[0].@value.toString()', aaa.arg[0].@value.toString(), '1');",
            "assertEqual('aaa.arg[1].@key.toString()', aaa.arg[1].@key.toString(), 'a2');",
            "assertEqual('aaa.arg[1].@value.toString()', aaa.arg[1].@value.toString(), '2');",
        };
        String mxml = getMXML(metadata, asserts);
        String[] otherOptions = new String[]
        {
        	"-keep-as3-metadata=AAA" // this should not be necessary; the old compiler seems to have kept all MXML metadata
        };
        compileAndRun(mxml, false, false, false, otherOptions);
	}
    
	@Test
	public void MXMLMetadataTag_twoTags()
	{
        String[] metadata = new String[]
        {
            "<fx:Metadata>",
            "    [AAA(a1='1')]",
            "</fx:Metadata>"
        };
        String[] moreMetadata = new String[]
        {
            "<fx:Metadata>",
            "    [BBB(b1='1')]",
            "</fx:Metadata>"
        };
        String[] asserts = new String[]
        {
        	"var dt:XML = describeType(this);",
        	"var aaa:XMLList = dt.metadata.(@name == 'AAA');",
            "assertEqual('aaa.arg[0].@key.toString()', aaa.arg[0].@key.toString(), 'a1');",
            "assertEqual('aaa.arg[0].@value.toString()', aaa.arg[0].@value.toString(), '1');",
        	"var bbb:XMLList = dt.metadata.(@name == 'BBB');",
            "assertEqual('bbb.arg[0].@key.toString()', bbb.arg[0].@key.toString(), 'b1');",
            "assertEqual('bbb.arg[0].@value.toString()', bbb.arg[0].@value.toString(), '1');",
        };
        String mxml = getMXML(metadata, moreMetadata, asserts);
        String[] otherOptions = new String[]
        {
        	"-keep-as3-metadata=AAA,BBB" // this should not be necessary; the old compiler seems to have kept all MXML metadata
        };
        compileAndRun(mxml, false, false, false, otherOptions);
	}
	
	// TODO Add tests with CDATA, comments, entities, etc.
}
