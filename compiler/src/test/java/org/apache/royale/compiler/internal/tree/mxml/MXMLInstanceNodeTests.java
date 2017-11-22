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

package org.apache.royale.compiler.internal.tree.mxml;

import org.apache.royale.utils.StringUtils;
import org.junit.Ignore;

/**
 * JUnit tests for {@link MXMLInstanceNode}.
 * 
 * @author Gordon Smith
 */
@Ignore
public class MXMLInstanceNodeTests extends MXMLClassReferenceNodeTests
{
	@Override
	protected String[] getTemplate()
	{
	    // Tests of instance nodes are done by parsing instance tags
		// inside a <Declarations> tag, using this template.
		return new String[] 
		{
		    "<fx:Object xmlns:fx='http://ns.adobe.com/mxml/2009'>",
		    "    <fx:Declarations>",
			"        %1",
		    "    </fx:Declarations>",
		    "</fx:Object>"
	    };
	}
	
	@Override
	protected String getMXML(String[] code)
    {
        String mxml = StringUtils.join(getTemplate(), "\n");
        mxml = mxml.replace("%1", StringUtils.join(code, "\n        "));
        return mxml;
    }
	
	protected int getOffset(String placeholder)
	{
		String templateString = StringUtils.join(getTemplate(),  "\n");
		return templateString.indexOf(placeholder);
	}
}
