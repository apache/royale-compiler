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
 * Feature tests for the MXML {@code <RemoteObject>} and {@code <method>} tags.
 * <p>
 * The {@code <RemoteObject>} tag is an ordinary instance tag from the compiler's
 * point of view, except for some compile magic that allows a {@code <RemoteObject>} tag
 * to have special child {@code <method>} tags mixed in with its other child tags
 * for properties and events.
 * These are not property tags, because {@code RemoteObject} has no <code>method</code> property.
 * Instead, each {@code <method>} tag creates an instance of <code>mx.rpc.remoting.mxml.Operation</code>
 * and adds it as a dynamic property of the <code>operations</code> object
 * of the <code>RemoteObject</code> instance.
 * 
 * @author Gordon Smith
 */
public class MXMLRemoteObjectTagTests extends MXMLInstanceTagTestsBase
{
	@Override
	protected String getOtherNamespaces()
	{
		return "xmlns:mx='library://ns.adobe.com/flex/mx'";
	}
	
    @Test
    public void MXMLRemoteObjectTag_twoMethods()
    {
        String[] declarations = new String[]
        {
            "<mx:RemoteObject id='ro1'>",
    		"    <mx:method name='m1'/>",
            "    <mx:destination>http://whatever</mx:destination>",
    		"    <mx:method name='m2'/>",
    		"</mx:RemoteObject>"
        };
        String[] scriptDeclarations = new String[]
        {
        	"import mx.rpc.remoting.mxml.Operation;"
        };
        String[] asserts = new String[]
        {
            "assertEqual('ro1 is RemoteObject', ro1 is RemoteObject, true);",
            "assertEqual('ro1.destination', ro1.destination, 'http://whatever');",
            "assertEqual('ro1.operations.m1', ro1.operations['m1'] is Operation, true);",
            "assertEqual('ro1.operations.m1.name', ro1.operations['m1'].name, 'm1');",
            "assertEqual('ro1.operations.m2', ro1.operations['m2'] is Operation, true);",
            "assertEqual('ro1.operations.m2.name', ro1.operations['m2'].name, 'm2');",
        };
        String mxml = getMXML(declarations, scriptDeclarations, asserts);
        compileAndRun(mxml, true, true, false, null);
    }
}
