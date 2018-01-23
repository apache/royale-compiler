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
 * Feature tests for the MXML {@code <Array>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLArrayTagTests extends MXMLInstanceTagTestsBase
{
    @Test
    public void MXMLArrayTag_empty()
    {
        String[] declarations = new String[]
        {
            "<fx:Array id='a1'>",
            "</fx:Array>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('a1.length', a1.length, 0);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLArrayTag_primitiveElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Array id='a1'>",
            "    <fx:Boolean>true</fx:Boolean>",
            "    <fx:int>123</fx:int>",
            "    <fx:uint>3000000000</fx:uint>",
            "    <fx:Number>1.5</fx:Number>",
            "    <fx:String>abc</fx:String>",
            "    <fx:Class>int</fx:Class>",
            "</fx:Array>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('a1.length', a1.length, 6);",
            "assertEqual('a1[0]', a1[0], true);",
            "assertEqual('a1[1]', a1[1], 123);",
            "assertEqual('a1[2]', a1[2], 3000000000);",
            "assertEqual('a1[3]', a1[3], 1.5);",
            "assertEqual('a1[4]', a1[4], 'abc');",
            "assertEqual('a1[5]', a1[5], int);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
    
    @Test
    public void MXMLArrayTag_arrayElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Array id='a1'>",
            "    <fx:Array>",
            "        <fx:String>a</fx:String>",
            "        <fx:String>b</fx:String>",
            "    </fx:Array>",
            "    <fx:Array>",
            "        <fx:String>c</fx:String>",
            "        <fx:String>d</fx:String>",
            "    </fx:Array>",
            "</fx:Array>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('a1.length', a1.length, 2);",
            "assertEqual('a1[0].length', a1[0].length, 2);",
            "assertEqual('a1[0][0]', a1[0][0], 'a');",
            "assertEqual('a1[0][1]', a1[0][1], 'b');",
            "assertEqual('a1[1].length', a1[1].length, 2);",
            "assertEqual('a1[1][0]', a1[1][0], 'c');",
            "assertEqual('a1[1][1]', a1[1][1], 'd');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLArrayTag_vectorElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Array id='a1'>",
            "    <fx:Vector type='String'>",
            "        <fx:String>a</fx:String>",
            "        <fx:String>b</fx:String>",
            "    </fx:Vector>",
            "    <fx:Vector type='String'>",
            "        <fx:String>c</fx:String>",
            "        <fx:String>d</fx:String>",
            "    </fx:Vector>",
            "</fx:Array>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('a1.length', a1.length, 2);",
            "assertEqual('a1[0].length', a1[0].length, 2);",
            "assertEqual('a1[0][0]', a1[0][0], 'a');",
            "assertEqual('a1[0][1]', a1[0][1], 'b');",
            "assertEqual('a1[1].length', a1[1].length, 2);",
            "assertEqual('a1[1][0]', a1[1][0], 'c');",
            "assertEqual('a1[1][1]', a1[1][1], 'd');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLArrayTag_objectElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Array id='a1'>",
            "    <fx:Object a='1' b='2'/>",
            "    <fx:Object c='3' d='4'/>",
            "</fx:Array>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('a1.length', a1.length, 2);",
            "assertEqual('a1[0].a', a1[0].a, 1);",
            "assertEqual('a1[0].b', a1[0].b, 2);",
            "assertEqual('a1[1].c', a1[1].c, 3);",
            "assertEqual('a1[1].d', a1[1].d, 4);",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }

    @Test
    public void MXMLArrayTag_instanceElements()
    {
        String[] declarations = new String[]
        {
            "<fx:Array id='a1'>",
            "    <custom:TestInstance name='a'/>",
            "    <custom:TestInstance name='b'/>",
            "</fx:Array>"
        };
        String[] asserts = new String[]
        {
            "assertEqual('a1.length', a1.length, 2);",
            "assertEqual('a1[0].name', a1[0].name, 'a');",
            "assertEqual('a1[1].name', a1[1].name, 'b');",
        };
        String mxml = getMXML(declarations, asserts);
        compileAndRun(mxml);
    }
}
