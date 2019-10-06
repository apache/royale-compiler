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
package org.apache.royale.compiler.internal.codegen.mxml;

import static org.junit.Assert.assertNotNull;

import org.apache.royale.compiler.internal.test.MXMLTestBase;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

public class TestMXMLApplication extends MXMLTestBase
{

    @Test
    public void testBasicApp()
    {
        String code = ""
                + "<custom:TestInstance xmlns:custom=\"library://ns.apache.org/royale/test\">"
                + "</custom:TestInstance>";

        IMXMLFileNode node = compileMXML(code);
        assertNotNull(node);

        mxmlBlockWalker.visitFile(node);

        assertOut("<TestInstance>\n\t\n</TestInstance>");
    }

    @Test
    public void testBasicAppWithOneComponent()
    {
        String code = ""
                + "<custom:TestInstance xmlns:custom=\"library://ns.apache.org/royale/test\">"
                + "    <custom:Button id=\"myBtn\" label=\"Hello world\"></custom:Button>"
                + "</custom:TestInstance>";

        IMXMLFileNode node = compileMXML(code);
        assertNotNull(node);

        mxmlBlockWalker.visitFile(node);

        assertOut("<TestInstance>\n\t<Button id=\"myBtn\" label=\"Hello world\"></Button>\n</TestInstance>");
    }

    @Test
    public void testBasicAppWithTwoComponents()
    {
        String code = ""
                + "<custom:TestInstance xmlns:custom=\"library://ns.apache.org/royale/test\">"
                + "    <custom:Label id=\"myLbl\" text=\"Bye bye\"></custom:Label>"
                + "    <custom:Button id=\"myBtn\" label=\"Hello world\"></custom:Button>"
                + "</custom:TestInstance>";

        IMXMLFileNode node = compileMXML(code);
        assertNotNull(node);

        mxmlBlockWalker.visitFile(node);

        assertOut("<TestInstance>\n\t<Label id=\"myLbl\" text=\"Bye bye\"></Label>\n\t<Button id=\"myBtn\" label=\"Hello world\"></Button>\n</TestInstance>");
    }

    @Test
    public void testBasicAppWithSimpleScript()
    {
        String code = ""
                + "<custom:TestInstance xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:custom=\"library://ns.apache.org/royale/test\">"
                + "    <fx:Script><![CDATA["
                + "        private const GREETING:String = \"Hello world!\""
                + "    ]]></fx:Script>" + "</custom:TestInstance>";

        IMXMLFileNode node = compileMXML(code);
        assertNotNull(node);

        mxmlBlockWalker.visitFile(node);

        assertOut("<TestInstance>\n\t<script><![CDATA[\n\t\tprivate var GREETING:String = \"Hello world!\";\n\t]]></script>\n</TestInstance>");
    }

    @Test
    public void testDefaultApp()
    {
        String code = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<custom:TestInstance xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                + "               xmlns:custom=\"library://ns.apache.org/royale/test\" "
                + "               minWidth=\"955\" minHeight=\"600\">"
                + "    <fx:Declarations>"
                + "        <!-- Place non-visual elements (e.g., services, value objects) here -->"
                + "    </fx:Declarations>" + "</custom:TestInstance>";

        IMXMLFileNode node = compileMXML(code);
        assertNotNull(node);

        mxmlBlockWalker.visitFile(node);

        assertOut("<TestInstance minWidth=\"955\" minHeight=\"600\">\n\t\n</TestInstance>");
    }

}
