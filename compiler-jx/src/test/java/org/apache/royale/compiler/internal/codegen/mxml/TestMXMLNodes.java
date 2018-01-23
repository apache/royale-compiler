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
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

public class TestMXMLNodes extends MXMLTestBase
{

    @Test
    public void testSimpleNode()
    {
        String code = "<custom:Button />";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button></Button>");
    }

    @Test
    public void testSimpleNodeWithId()
    {
        String code = "<custom:Button id=\"myBtn\"/>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button id=\"myBtn\"></Button>");
    }

    @Test
    public void testSimpleNodeWithAttribute()
    {
        String code = "<custom:Button label=\"Click me\" />";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button label=\"Click me\"></Button>");
    }

    @Test
    public void testSimpleNodeWithInnerText()
    {
        String code = "<custom:Button>Click me</custom:Button>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button label=\"Click me\"></Button>");
    }

    @Test
    public void testAnotherSimpleNodeWithInnerText()
    {
        String code = "<custom:Label>Hello World!</custom:Label>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Label text=\"Hello World!\"></Label>");
    }

    @Test
    public void testSimpleNodeWithMultipleAttributes()
    {
        String code = "<custom:Button visible=\"false\" x=\"100\" width=\"1.5\" label=\"Click me ;-)\" color=\"0xFF0000\"/>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button visible=\"false\" x=\"100\" width=\"1.5\" label=\"Click me ;-)\" color=\"16711680\"></Button>");
    }

    @Test
    public void testNodeWithChild()
    {
        String code = "<custom:Group><custom:Label /></custom:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group><Label></Label></Group>");
    }

    @Test
    public void testNodeWithChildAndAttribute()
    {
        String code = "<custom:Group id=\"myGrp\"><custom:Label /></custom:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group id=\"myGrp\"><Label></Label></Group>");
    }

    @Test
    public void testNodeWithNestedChildren()
    {
        String code = "<custom:Group><custom:Group><custom:Group>" + "<custom:Label />"
                + "</custom:Group></custom:Group></custom:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group><Group><Group><Label></Label></Group></Group></Group>");
    }

    @Test
    public void testNodeWithNestedChildrenAndAttribute()
    {
        String code = "<custom:Group><custom:Group><custom:Group>"
                + "<custom:Label id=\"myRB\"/>"
                + "</custom:Group></custom:Group></custom:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group><Group><Group><Label id=\"myRB\"></Label></Group></Group></Group>");
    }

    @Test
    public void testNodeWithNestedChildrenAndInnerText()
    {
        String code = "<custom:Group><custom:Group><custom:Group>"
                + "<custom:Button>Click me</custom:Button>"
                + "</custom:Group></custom:Group></custom:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group><Group><Group><Button label=\"Click me\"></Button></Group></Group></Group>");
    }

}
