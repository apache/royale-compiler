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
 * Feature tests for the MXML {@code <Vector>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLVectorTagTests extends MXMLInstanceTagTestsBase
{
	@Ignore
    @Test
    public void MXMLVectorTag_empty()
    {
        String[] declarations = new String[]
        {
            "<fx:Vector type='*' id='v1'>",
            "</fx:Vector>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('v1.length', v1.length, 0);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Ignore // compiler thinks the elements aren't compatible with type='*'
    @Test
    public void MXMLVectorTag_typeAny_primitiveElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Vector type='*' id='v1'>",
            "    <fx:Boolean>true</fx:Boolean>",
            "    <fx:int>123</fx:int>",
            "    <fx:uint>3000000000</fx:uint>",
            "    <fx:Number>1.5</fx:Number>",
            "    <fx:String>abc</fx:String>",
            "    <fx:Class>int</fx:Class>",
            "</fx:Vector>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('v1.length', v1.length, 6);",
            "assertEqual('v1[0]', v1[0], true);",
            "assertEqual('v1[1]', v1[1], 123);",
            "assertEqual('v1[2]', v1[2], 3000000000);",
            "assertEqual('v1[3]', v1[3], 1.5);",
            "assertEqual('v1[4]', v1[4], 'abc');",
            "assertEqual('v1[5]', v1[5], int);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLVectorTag_typeObject_primitiveElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Vector type='Object' id='v1'>",
            "    <fx:Boolean>true</fx:Boolean>",
            "    <fx:int>123</fx:int>",
            "    <fx:uint>3000000000</fx:uint>",
            "    <fx:Number>1.5</fx:Number>",
            "    <fx:String>abc</fx:String>",
            "    <fx:Class>int</fx:Class>",
            "</fx:Vector>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('v1.length', v1.length, 6);",
            "assertEqual('v1[0]', v1[0], true);",
            "assertEqual('v1[1]', v1[1], 123);",
            "assertEqual('v1[2]', v1[2], 3000000000);",
            "assertEqual('v1[3]', v1[3], 1.5);",
            "assertEqual('v1[4]', v1[4], 'abc');",
            "assertEqual('v1[5]', v1[5], int);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLVectorTag_arrayElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Vector type='Array' id='v1'>",
            "    <fx:Array>",
            "        <fx:String>a</fx:String>",
            "        <fx:String>b</fx:String>",
            "    </fx:Array>",
            "    <fx:Array>",
            "        <fx:String>c</fx:String>",
            "        <fx:String>d</fx:String>",
            "    </fx:Array>",
            "</fx:Vector>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('v1.length', v1.length, 2);",
            "assertEqual('v1[0].length', v1[0].length, 2);",
            "assertEqual('v1[0][0]', v1[0][0], 'a');",
            "assertEqual('v1[0][1]', v1[0][1], 'b');",
            "assertEqual('v1[1].length', v1[1].length, 2);",
            "assertEqual('v1[1][0]', v1[1][0], 'c');",
            "assertEqual('v1[1][1]', v1[1][1], 'd');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Ignore // compiler doesn't seem to handle this kind of type attribute
    @Test
    public void MXMLVectorTag_vectorElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Vector type='Vector.&lt;String&gt;' id='v1'>",
            "    <fx:Vector type='String'>",
            "        <fx:String>a</fx:String>",
            "        <fx:String>b</fx:String>",
            "    </fx:Vector>",
            "    <fx:Vector type='String'>",
            "        <fx:String>c</fx:String>",
            "        <fx:String>d</fx:String>",
            "    </fx:Vector>",
            "</fx:Vector>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('v1.length', v1.length, 2);",
            "assertEqual('v1[0].length', v1[0].length, 2);",
            "assertEqual('v1[0][0]', v1[0][0], 'a');",
            "assertEqual('v1[0][1]', v1[0][1], 'b');",
            "assertEqual('v1[1].length', v1[1].length, 2);",
            "assertEqual('v1[1][0]', v1[1][0], 'c');",
            "assertEqual('v1[1][1]', v1[1][1], 'd');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLVectorTag_objectElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Vector type='Object' id='v1'>",
            "    <fx:Object a='1' b='2'/>",
            "    <fx:Object c='3' d='4'/>",
            "</fx:Vector>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('v1.length', v1.length, 2);",
            "assertEqual('v1[0].a', v1[0].a, 1);",
            "assertEqual('v1[0].b', v1[0].b, 2);",
            "assertEqual('v1[1].c', v1[1].c, 3);",
            "assertEqual('v1[1].d', v1[1].d, 4);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLVectorTag_instanceElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Vector type='custom.TestInstance' id='v1'>",
            "    <custom:TestInstance name='a'/>",
            "    <custom:TestInstance name='b'/>",
            "</fx:Vector>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('v1.length', v1.length, 2);",
            "assertEqual('v1[0].name', v1[0].name, 'a');",
            "assertEqual('v1[1].name', v1[1].name, 'b');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    // Test 'fixed' attribute
}
