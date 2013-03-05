package org.apache.flex.compiler.internal.codegen.mxml;

import org.apache.flex.compiler.internal.test.MXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestMXMLAttributes extends MXMLTestBase
{

    @Ignore
    @Test
    public void testIdAttribute()
    {
        // TODO (erikdebruin) id attributes are a special case...
        String code = "id=\"myGrp\"";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_NODE);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("id=\"myBtn\"");
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
