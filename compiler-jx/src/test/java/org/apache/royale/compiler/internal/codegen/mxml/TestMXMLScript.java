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

import org.apache.royale.compiler.internal.test.MXMLTestBase;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;
import org.junit.Test;

public class TestMXMLScript extends MXMLTestBase
{

    @Test
    public void testEmptyScript()
    {
        String code = "" + "<fx:Script><![CDATA[]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitScript(node);

        assertOut("<script><![CDATA[]]></script>");
    }

    @Test
    public void testSimpleScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    private var GREETING:String = \"Hello world!\";"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitScript(node);

        assertOut("<script><![CDATA[\n\tprivate var GREETING:String = \"Hello world!\";\n]]></script>");
    }

    @Test
    public void testMultiLineScript()
    {
        String code = "" + "<fx:Script><![CDATA["
                + "    public var goodbye:String = \"Bye bye :-(\";"
                + "    private const GREETING:String = \"Hello world!\";"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitScript(node);

        assertOut("<script><![CDATA[\n\tpublic var goodbye:String = \"Bye bye :-(\";\n\tprivate var GREETING:String = \"Hello world!\";\n]]></script>");
    }

    @Test
    public void testComplexScript()
    {
        // TODO (erikdebruin) fix indentation...
        String code = "" + "<fx:Script><![CDATA[" + "    var n:int = 3;"
                + "    for (var i:int = 0; i < n; i++)" + "    {"
                + "        Alert.show(\"Hi\");" + "    }" + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitScript(node);

        assertOut("<script><![CDATA[\n\tvar n:int = 3;\n\tfor (var i:int = 0; i < n; i++) {\n\tAlert.show(\"Hi\");\n};\n]]></script>");
    }

    // TODO (erikdebruin) this isn't working...
    @Test
    public void testFunctionScript()
    {
//        String code = "" + "<fx:Script><![CDATA["
//                + "    public static function beNice(input:*):Object" + "    {"
//                + "        Alert.show(\"I'm nice :-P\");"
//                + "        return null;" + "    }" + "]]></fx:Script>";
//
//        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
//                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);
//
//        mxmlBlockWalker.visitScript(node);
//
//        System.out.println(writer.toString());
//
//        assertOut("<script><![CDATA[\n\tvar n:int = 3;\n\tfor (var i:int = 0; i < n; i++) {\n\tAlert.show(\"Hi\");\n};\n]]></script>");
    }
}
