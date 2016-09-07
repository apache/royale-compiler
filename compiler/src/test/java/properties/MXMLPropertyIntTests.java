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

import org.junit.Test;

/**
 * Feature tests for MXML property tags and attributes of type {@code <int>}.
 * 
 * @author Gordon Smith
 */
public class MXMLPropertyIntTests extends MXMLPropertyTestsBase
{
	@Override
	protected String getPropertyType()
	{
		return "int";
	}
	
	@Test
	public void MXMLPropertyIntTests_default()
	{
		String[] declarations = new String[]
		{
		    "<MyComp id='mc1'/>"
		};
        String[] asserts = new String[]
        {
	        "assertEqual('mc1.p', mc1.p, 0);"	
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml, true, false, false, null);
	}
	
	@Test
	public void MXMLPropertyIntTests_attribute()
	{
		String[] declarations = new String[]
		{
		    "<MyComp id='mc1' p=' -1 '/>"
		};
        String[] asserts = new String[]
        {
	        "assertEqual('mc1.p', mc1.p, -1);"	
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml, true, false, false, null);
	}
	
	@Test
	public void MXMLPropertyIntTests_tag_text()
	{
		String[] declarations = new String[]
		{
			"<MyComp id='mc1'>",
			"    <p> -1 </p>",
			"</MyComp>"
		};
        String[] asserts = new String[]
        {
	        "assertEqual('mc1.p', mc1.p, -1);"	
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml, true, false, false, null);
	}
	
	@Test
	public void MXMLPropertyIntTests_tag_tag()
	{
		String[] declarations = new String[]
		{
			"<MyComp id='mc1'>",
			"    <p><fx:int> -1 </fx:int></p>",
			"</MyComp>"
		};
        String[] asserts = new String[]
        {
	        "assertEqual('mc1.p', mc1.p, -1);"	
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml, true, false, false, null);
	}
}
