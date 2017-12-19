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
 * Feature tests for the MXML {@code <Component>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLComponentTagTests extends MXMLInstanceTagTestsBase
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
	
	// Note: <Component> creates an instance of mx.core.ClassFactory
	// and therefore must be compiled against framework.swc.
	
    @Test
    public void MXMLComponentTag_basic()
    {
        String[] declarations = new String[]
        {
            "<fx:Component id='c1' className='MyTestInstance'>",
            "    <d:Sprite name='s1'/>",
            "</fx:Component>"
        };
        String[] asserts = new String[]
        {
        	"import mx.core.ClassFactory", // TODO Should this have to be imported? And should c1 be type IFactory or ClassFactory?
            "assertEqual('c1', c1 is ClassFactory, true);",
            "var generator:Class = ClassFactory(c1).generator;",
            "assertEqual('new generator() is Sprite', new generator() is Sprite, true);",
            "var properties:Object = ClassFactory(c1).properties;",
            "assertEqual('countProperties(properties)', countProperties(properties), 1);",
            "assertEqual('properties.outerDocument', properties.outerDocument, this);",
            "assertEqual('c1.newInstance() is Sprite', c1.newInstance() is Sprite, true);",
            "assertEqual('c1.newInstance().name', c1.newInstance().name, 's1');",
            "assertEqual('c1.newInstance().outerDocument', c1.newInstance().outerDocument, this);",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, false, false, null);
    }
}
