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
 * Feature tests for the MXML {@code <WebService>} tag.
 * <p>
 * The {@code <WebService>} tag is an ordinary instance tag from the compiler's
 * point of view, except for the fact that a {@code <Webservice>} tag can have
 * special child {@code <operation>} tags mixed in with its other child tags
 * for properties and events.
 * These are not property tags, because {@code WebService} has no <code>operation</code> property.
 * Instead, each {@code <operation>} tag creates an instance of <code>mx.rpc.soap.mxml.Operation</code>
 * and adds it as a dynamic property of the <code>operations</code> object
 * of the <code>WebService</code> instance; the name of the property in this object
 * is the name specified by the <code>name</code> attribute on the {@code <operation>} tag.
 * 
 * @author Gordon Smith
 */
public class MXMLWebServiceTagTests extends MXMLInstanceTagTestsBase
{
	@Override
	protected String getOtherNamespaces()
	{
		return "xmlns:mx='library://ns.adobe.com/flex/mx'";
	}
	
    @Test
    public void MXMLWebServiceTag()
    {
        String[] declarations = new String[]
        {
            "<mx:WebService id='ws1'>",
    		"</mx:WebService>"
        };
        String[] scriptDeclarations = new String[]
        {
        };
        String[] asserts = new String[]
        {
            "assertEqual('ws1 is WebService', ws1 is WebService, true);",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, true, false, null);
    }
	
    @Test
    public void MXMLWebServiceTag_description()
    {
        String[] declarations = new String[]
        {
            "<mx:WebService id='ws1'>",
            "    <mx:description>whatever</mx:description>",
    		"</mx:WebService>"
        };
        String[] scriptDeclarations = new String[]
        {
        };
        String[] asserts = new String[]
        {
            "assertEqual('ws1 is WebService', ws1 is WebService, true);",
            "assertEqual('ws1.description', ws1.description, 'whatever');",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, true, false, null);
    }
	
    @Test
    public void MXMLWebServiceTag_operation()
    {
        String[] declarations = new String[]
        {
            "<mx:WebService id='ws1' wsdl='https://example.com'>",
    		"    <mx:operation name='op1'/>",
    		"</mx:WebService>"
        };
        String[] scriptDeclarations = new String[]
        {
        	"import mx.rpc.soap.mxml.Operation;"
        };
        String[] asserts = new String[]
        {
            "assertEqual('ws1 is WebService', ws1 is WebService, true);",
            "assertEqual('ws1.operations.op1', ws1.operations['op1'] is Operation, true);",
            "assertEqual('ws1.operations.op1.name', ws1.operations['op1'].name, 'op1');",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, true, false, null);
    }

    @Test
    public void MXMLWebServiceTag_twoOperations()
    {
        String[] declarations = new String[]
        {
            "<mx:WebService id='ws1'>",
    		"    <mx:operation name='op1'/>",
            "    <mx:description>whatever</mx:description>",
    		"    <mx:operation name='op2'/>",
    		"</mx:WebService>"
        };
        String[] scriptDeclarations = new String[]
        {
        	"import mx.rpc.soap.mxml.Operation;"
        };
        String[] asserts = new String[]
        {
            "assertEqual('ws1 is WebService', ws1 is WebService, true);",
            "assertEqual('ws1.description', ws1.description, 'whatever');",
            "assertEqual('ws1.operations.op1', ws1.operations['op1'] is Operation, true);",
            "assertEqual('ws1.operations.op1.name', ws1.operations['op1'].name, 'op1');",
            "assertEqual('ws1.operations.op2', ws1.operations['op2'] is Operation, true);",
            "assertEqual('ws1.operations.op2.name', ws1.operations['op2'].name, 'op2');",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, true, false, null);
    }
	
    @Test
    public void MXMLWebServiceTag_operation_withArguments()
    {
        String[] declarations = new String[]
        {
            "<mx:WebService id='ws1' wsdl='https://example.com'>",
    		"    <mx:operation name='op1'>",
    		"        <mx:arguments>",
    		"            <a>abc</a>",
    		"            <b>123</b>",
    		"            <c>false</c>",
    		"            <d>456.7</d>",
    		"            <d>hello</d>",
    		"            <d>true</d>",
    		"            <e>",
    		"                <e_1>890.1</e_1>",
            "            </e>",
    		"            <f f_1='234.5'/>",
    		"            <g g_1='howdy'>",
    		"                <g_1>678.9</g_1>",
            "            </g>",
            "        </mx:arguments>",
            "    </mx:operation>",
    		"</mx:WebService>"
        };
        String[] scriptDeclarations = new String[]
        {
        	"import mx.rpc.soap.mxml.Operation;"
        };
        String[] asserts = new String[]
        {
            "assertEqual('ws1 is WebService', ws1 is WebService, true);",
            "assertEqual('ws1.operations.op1', ws1.operations['op1'] is Operation, true);",
            "assertEqual('ws1.operations.op1.name', ws1.operations['op1'].name, 'op1');",
            "assertEqual('ws1.operations.op1.arguments.a', ws1.operations['op1'].arguments['a'], 'abc');",
            "assertEqual('ws1.operations.op1.arguments.b', ws1.operations['op1'].arguments['b'], 123);",
            "assertEqual('ws1.operations.op1.arguments.c', ws1.operations['op1'].arguments['c'], false);",
            "assertEqual('ws1.operations.op1.arguments.d.length', ws1.operations['op1'].arguments['d'].length, 3);",
            "assertEqual('ws1.operations.op1.arguments.d[0]', ws1.operations['op1'].arguments['d'][0], 456.7);",
            "assertEqual('ws1.operations.op1.arguments.d[1]', ws1.operations['op1'].arguments['d'][1], 'hello');",
            "assertEqual('ws1.operations.op1.arguments.d[2]', ws1.operations['op1'].arguments['d'][2], true);",
            "assertEqual('ws1.operations.op1.arguments.e.e_1', ws1.operations['op1'].arguments['e']['e_1'], 890.1);",
            "assertEqual('ws1.operations.op1.arguments.f.f_1', ws1.operations['op1'].arguments['f']['f_1'], 234.5);",
            "assertEqual('ws1.operations.op1.arguments.g.g_1.length', ws1.operations['op1'].arguments['g']['g_1'].length, 2);",
            "assertEqual('ws1.operations.op1.arguments.g.g_1[0]', ws1.operations['op1'].arguments['g']['g_1'][0], 'howdy');",
            "assertEqual('ws1.operations.op1.arguments.g.g_1[1]', ws1.operations['op1'].arguments['g']['g_1'][1], 678.9);",
            "assertEqual('ws1.operations.op1.request.a', ws1.operations['op1'].request['a'], 'abc');",
            "assertEqual('ws1.operations.op1.request.b', ws1.operations['op1'].request['b'], 123);",
            "assertEqual('ws1.operations.op1.request.c', ws1.operations['op1'].request['c'], false);",
            "assertEqual('ws1.operations.op1.request.d.length', ws1.operations['op1'].request['d'].length, 3);",
            "assertEqual('ws1.operations.op1.request.d[0]', ws1.operations['op1'].request['d'][0], 456.7);",
            "assertEqual('ws1.operations.op1.request.d[1]', ws1.operations['op1'].request['d'][1], 'hello');",
            "assertEqual('ws1.operations.op1.request.d[2]', ws1.operations['op1'].request['d'][2], true);",
            "assertEqual('ws1.operations.op1.request.e.e_1', ws1.operations['op1'].request['e']['e_1'], 890.1);",
            "assertEqual('ws1.operations.op1.request.f.f_1', ws1.operations['op1'].request['f']['f_1'], 234.5);",
            "assertEqual('ws1.operations.op1.request.g.g_1.length', ws1.operations['op1'].request['g']['g_1'].length, 2);",
            "assertEqual('ws1.operations.op1.request.g.g_1[0]', ws1.operations['op1'].request['g']['g_1'][0], 'howdy');",
            "assertEqual('ws1.operations.op1.request.g.g_1[1]', ws1.operations['op1'].request['g']['g_1'][1], 678.9);",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, true, false, null);
    }
	
    @Test
    public void MXMLWebServiceTag_operation_withRequest()
    {
        String[] declarations = new String[]
        {
            "<mx:WebService id='ws1' wsdl='https://example.com'>",
    		"    <mx:operation name='op1'>",
    		"        <mx:request>",
    		"            <a>abc</a>",
    		"            <b>123</b>",
    		"            <c>false</c>",
    		"            <d>456.7</d>",
    		"            <d>hello</d>",
    		"            <d>true</d>",
    		"            <e>",
    		"                <e_1>890.1</e_1>",
            "            </e>",
    		"            <f f_1='234.5'/>",
    		"            <g g_1='howdy'>",
    		"                <g_1>678.9</g_1>",
            "            </g>",
            "        </mx:request>",
            "    </mx:operation>",
    		"</mx:WebService>"
        };
        String[] scriptDeclarations = new String[]
        {
        	"import mx.rpc.soap.mxml.Operation;"
        };
        String[] asserts = new String[]
        {
            "assertEqual('ws1 is WebService', ws1 is WebService, true);",
            "assertEqual('ws1.operations.op1', ws1.operations['op1'] is Operation, true);",
            "assertEqual('ws1.operations.op1.name', ws1.operations['op1'].name, 'op1');",
            "assertEqual('ws1.operations.op1.arguments.a', ws1.operations['op1'].arguments['a'], 'abc');",
            "assertEqual('ws1.operations.op1.arguments.b', ws1.operations['op1'].arguments['b'], 123);",
            "assertEqual('ws1.operations.op1.arguments.c', ws1.operations['op1'].arguments['c'], false);",
            "assertEqual('ws1.operations.op1.arguments.d.length', ws1.operations['op1'].arguments['d'].length, 3);",
            "assertEqual('ws1.operations.op1.arguments.d[0]', ws1.operations['op1'].arguments['d'][0], 456.7);",
            "assertEqual('ws1.operations.op1.arguments.d[1]', ws1.operations['op1'].arguments['d'][1], 'hello');",
            "assertEqual('ws1.operations.op1.arguments.d[2]', ws1.operations['op1'].arguments['d'][2], true);",
            "assertEqual('ws1.operations.op1.arguments.e.e_1', ws1.operations['op1'].arguments['e']['e_1'], 890.1);",
            "assertEqual('ws1.operations.op1.arguments.f.f_1', ws1.operations['op1'].arguments['f']['f_1'], 234.5);",
            "assertEqual('ws1.operations.op1.arguments.g.g_1.length', ws1.operations['op1'].arguments['g']['g_1'].length, 2);",
            "assertEqual('ws1.operations.op1.arguments.g.g_1[0]', ws1.operations['op1'].arguments['g']['g_1'][0], 'howdy');",
            "assertEqual('ws1.operations.op1.arguments.g.g_1[1]', ws1.operations['op1'].arguments['g']['g_1'][1], 678.9);",
            "assertEqual('ws1.operations.op1.request.a', ws1.operations['op1'].request['a'], 'abc');",
            "assertEqual('ws1.operations.op1.request.b', ws1.operations['op1'].request['b'], 123);",
            "assertEqual('ws1.operations.op1.request.c', ws1.operations['op1'].request['c'], false);",
            "assertEqual('ws1.operations.op1.request.d.length', ws1.operations['op1'].request['d'].length, 3);",
            "assertEqual('ws1.operations.op1.request.d[0]', ws1.operations['op1'].request['d'][0], 456.7);",
            "assertEqual('ws1.operations.op1.request.d[1]', ws1.operations['op1'].request['d'][1], 'hello');",
            "assertEqual('ws1.operations.op1.request.d[2]', ws1.operations['op1'].request['d'][2], true);",
            "assertEqual('ws1.operations.op1.request.e.e_1', ws1.operations['op1'].request['e']['e_1'], 890.1);",
            "assertEqual('ws1.operations.op1.request.f.f_1', ws1.operations['op1'].request['f']['f_1'], 234.5);",
            "assertEqual('ws1.operations.op1.request.g.g_1.length', ws1.operations['op1'].request['g']['g_1'].length, 2);",
            "assertEqual('ws1.operations.op1.request.g.g_1[0]', ws1.operations['op1'].request['g']['g_1'][0], 'howdy');",
            "assertEqual('ws1.operations.op1.request.g.g_1[1]', ws1.operations['op1'].request['g']['g_1'][1], 678.9);",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, true, false, null);
    }
}
