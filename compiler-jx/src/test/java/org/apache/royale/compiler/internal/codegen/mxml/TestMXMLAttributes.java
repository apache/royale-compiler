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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.royale.compiler.internal.test.MXMLTestBase;
import org.apache.royale.compiler.tree.mxml.IMXMLInstanceNode;
import org.apache.royale.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

public class TestMXMLAttributes extends MXMLTestBase
{

    @Test
    public void testIdAttribute()
    {
        // (erikdebruin) id attributes are a special case...
        
        String code = "id=\"myBtn\"";

        IMXMLInstanceNode node = (IMXMLInstanceNode) getNode(
                code, IMXMLInstanceNode.class,
                MXMLTestBase.WRAP_LEVEL_NODE);
        
        mxmlBlockWalker.visitInstance(node);

        // this was getChild() before removing dependency on Flash
        // not sure why Button would have a child, but also, in MXMLTestBase
        // there are two calls to findFirstDescendant, which doesn't make sense
        // assertThat(((IMXMLInstanceNode) node.getChild(0)).getID(), is("myBtn"));
        assertThat(((IMXMLInstanceNode) node).getID(), is("myBtn"));
    }

    @Test
    public void testSimpleBooleanAttribute()
    {
        String code = "visible=\"false\"";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_NODE);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("visible=\"false\"");
    }

    @Test
    public void testSimpleIntAttribute()
    {
        String code = "x=\"100\"";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_NODE);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("x=\"100\"");
    }

    @Test
    public void testSimpleNumberAttribute()
    {
        String code = "width=\"1.5\"";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_NODE);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("width=\"1.5\"");
    }

    @Test
    public void testSimpleStringAttribute()
    {
        String code = "label=\"Click me ;-)\"";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_NODE);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("label=\"Click me ;-)\"");
    }

    @Test
    public void testSimpleUintAttribute()
    {
        String code = "color=\"0xFF0000\"";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_NODE);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("color=\"16711680\"");
    }

}
