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
 * Feature tests for the MXML {@code <HTTPService>} and {@code <request>} tags.
 * <p>
 * The {@code <HTTPService>} tag is an ordinary instance tag from the compiler's
 * point of view, except for the fact that an {@code <HTTPService>} tag can have
 * special child {@code <request>} tags mixed in with its other child tags
 * for properties and events.
 * A {@code <request>} tag is a special kind of property tag,
 * corresponding to the <code>request</code> property.
 * This property has type <code>Object</code>, but you don't have to write
 * an MXML {@code <Object>} tag to set its value; instead you just
 * write the name/value pairs.
 * <p>
 * Each {@code <request>} tag is represented by a child {@code IMXMLHTTPServiceRequestPropertyNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLHTTPServiceTagTests extends MXMLInstanceTagTestsBase
{
	@Override
	protected String getOtherNamespaces()
	{
		return "xmlns:mx='library://ns.adobe.com/flex/mx'";
	}
	
    @Test
    public void MXMLHTTPServiceTag_withRequest()
    {
        String[] declarations = new String[]
        {
            "<mx:HTTPService id='hs1'>",
            "    <mx:url>http://whatever</mx:url>",
    		"    <mx:request xmlns=''>",
    		"        <a>abc</a>",
    		"        <b>123</b>",
    		"        <c>false</c>",
    		"    </mx:request>",
    		"    <mx:method>POST</mx:method>",
    		"</mx:HTTPService>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('hs1 is HTTPService', hs1 is HTTPService, true);",
            "assertEqual('hs1.url', hs1.url, 'http://whatever');",
            "assertEqual('hs1.request.a', hs1.request['a'], 'abc');",
            "assertEqual('hs1.request.b', hs1.request['b'], 123);",
            "assertEqual('hs1.request.c', hs1.request['c'], false);",
            "assertEqual('hs1.method', hs1.method, 'POST');"
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml, true, true, false, null);
    }
}
