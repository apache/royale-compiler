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

import org.junit.Ignore;
import org.junit.Test;

/**
 * Feature tests for the MXML {@code <Object>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLObjectTagTests extends MXMLInstanceTagTestsBase
{
	// This <Script> function is used by some of the asserts in the tests below
	// to count the number of properties of an <Object>.
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
	
    @Test
    public void MXMLObjectTag_empty()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'>",
            "</fx:Object>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('count(o1)', countProperties(o1), 0);",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml);
    }
    
    @Ignore
    @Test
    public void MXMLObjectTag_primitiveValues()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'>false</fx:Object>",
            "<fx:Object id='o2'>true</fx:Object>",
            "<fx:Object id='o3'>123456</fx:Object>",
            "<fx:Object id='o4'>0x123456</fx:Object>",
            "<fx:Object id='o5'>#123456</fx:Object>",
            "<fx:Object id='o6'>1.5</fx:Object>",
            "<fx:Object id='o7'>1.5e3</fx:Object>",
            "<fx:Object id='o8'>NaN</fx:Object>",
            "<fx:Object id='o9'>Infinity</fx:Object>",
            "<fx:Object id='o10'>abc</fx:Object>",
        };
        String[] asserts = new String[]
        {
            "assertEqual('o1', o1, 'false');",
            "assertEqual('o2', o2, 'true');",
            "assertEqual('o3', o3, '123456');",
            "assertEqual('o4', o4, '0x123456');",
            "assertEqual('o5', o5, '#123456');",
            "assertEqual('o6', o6, '1.5');",
            "assertEqual('o7', o7, '1.5e3');",
            "assertEqual('o8', o8, 'NaN');",
            "assertEqual('o9', o9, 'Infinity');",
            "assertEqual('o10', o10, 'abc');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLObjectTag_primitivePropertyValues1()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'>",
            "    <fx:a>",
            "        <fx:Boolean>false</fx:Boolean>",
            "    </fx:a>",
            "    <fx:b>",
            "        <fx:Boolean>true</fx:Boolean>",
            "    </fx:b>",
            "    <fx:c>",
            "        <fx:int>123</fx:int>",
            "    </fx:c>",
            "    <fx:d>",
            "        <fx:uint>3000000000</fx:uint>",
            "    </fx:d>",
            "    <fx:e>",
            "        <fx:Number>1.5</fx:Number>",
            "    </fx:e>",
            "    <fx:f>",
            "        <fx:String>abc</fx:String>",
            "    </fx:f>",
            "    <fx:g>",
            "        <fx:Class>flash.display.Sprite</fx:Class>",
            "    </fx:g>",
            "</fx:Object>"
        };
        String[] asserts = new String[]
        {
            //"assertEqual('count(o1)', countProperties(o1), 7);",
            "assertEqual('o1.a', o1.a, false);",
            "assertEqual('o1.b', o1.b, true);",
            "assertEqual('o1.c', o1.c, 123);",
            "assertEqual('o1.d', o1.d, 3000000000);",
            "assertEqual('o1.e', o1.e, 1.5);",
            "assertEqual('o1.f', o1.f, 'abc');",
            "assertEqual('o1.g', o1.g, Sprite);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLObjectTag_primitivePropertyValues2()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'>",
            "    <fx:a>false</fx:a>",
            "    <fx:b>true</fx:b>",
            "    <fx:c>123</fx:c>",
            "    <fx:d>3000000000</fx:d>",
            "    <fx:e>1.5<</fx:e>",
            "    <fx:f>abc</fx:f>",
            "    <fx:g>flash.display.Sprite</fx:g>",
             "</fx:Object>"
        };
        String[] asserts = new String[]
        {
            //"assertEqual('count(o1)', countProperties(o1), 7);",
            "assertEqual('o1.a', o1.a, false);",
            "assertEqual('o1.b', o1.b, true);",
            "assertEqual('o1.c', o1.c, 123);",
            "assertEqual('o1.d', o1.d, 3000000000);",
            "assertEqual('o1.e', o1.e, 1.5);",
            "assertEqual('o1.f', o1.f, 'abc');",
            "assertEqual('o1.g', o1.g, 'flash.display.Sprite');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLObjectTag_primitivePropertyValues3()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'",
            "    a='false'",
            "    b='true'",
            "    c='123'",
            "    d='3000000000'",
            "    e='1.5'",
            "    f='abc'",
            "    g='flash.display.Sprite'/>"
        };
        String[] asserts = new String[]
        {
            //"assertEqual('count(o1)', countProperties(o1), 7);",
            "assertEqual('o1.a', o1.a, false);",
            "assertEqual('o1.b', o1.b, true);",
            "assertEqual('o1.c', o1.c, 123);",
            "assertEqual('o1.d', o1.d, 3000000000);",
            "assertEqual('o1.e', o1.e, 1.5);",
            "assertEqual('o1.f', o1.f, 'abc');",
            "assertEqual('o1.g', o1.g, 'flash.display.Sprite');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLObjectTag_arrayPropertyValue()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'>",
            "    <fx:a>",
            "        <fx:Array>",
            "            <fx:int>1</fx:int>",
            "            <fx:int>2</fx:int>",
            "        </fx:Array>",
            "    </fx:a>",
            "</fx:Object>"
        };
        String[] asserts = new String[]
        {
            //"assertEqual('count(o1)', countProperties(o1), 1);",
            "assertEqual('o1.a is Array', o1.a is Array, true);",
            "assertEqual('o1.a.length', o1.a.length, 2);",
            "assertEqual('o1.a[0]', o1.a[0], 1);",
            "assertEqual('o1.a[1]', o1.a[1], 2);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLObjectTag_vectorPropertyValue()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'>",
            "    <fx:a>",
            "        <fx:Vector type='int'>",
            "            <fx:int>1</fx:int>",
            "            <fx:int>2</fx:int>",
            "        </fx:Vector>",
            "    </fx:a>",
            "</fx:Object>"
        };
        String[] asserts = new String[]
        {
            //"assertEqual('count(o1)', countProperties(o1), 1);",
            "assertEqual('o1.a is Vector.<int>', o1.a is Vector.<int>, true);",
            "assertEqual('o1.a.length', o1.a.length, 2);",
            "assertEqual('o1.a[0]', o1.a[0], 1);",
            "assertEqual('o1.a[1]', o1.a[1], 2);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLObjectTag_objectPropertyValue()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'>",
            "    <fx:a>",
            "        <fx:Object b='123'/>",
            "    </fx:a>",
            "</fx:Object>"
        };
        String[] asserts = new String[]
        {
            //"assertEqual('count(o1)', countProperties(o1), 1);",
            //"assertEqual('count(o1.a)', count(o1.a), 1);",
            "assertEqual('o1.a.b', o1.a.b, 123);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLObjectTag_instancePropertyValue()
    {
        String[] declarations = new String[]
        {
            "<fx:Object id='o1'>",
            "    <fx:a>",
            "        <custom:TestInstance name='abc'/>",
            "    </fx:a>",
            "</fx:Object>"
        };
        String[] asserts = new String[]
        {
            //"assertEqual('count(o1)', countProperties(o1), 1);",
            "assertEqual('o1.a is TestInstance', o1.a is TestInstance, true);",
            "assertEqual('o1.a.name', o1.a.name, 'abc');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
}
