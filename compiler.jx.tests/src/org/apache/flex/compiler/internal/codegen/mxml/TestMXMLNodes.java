package org.apache.flex.compiler.internal.codegen.mxml;

import org.apache.flex.compiler.internal.test.MXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Test;

public class TestMXMLNodes extends MXMLTestBase
{

    @Test
    public void testSimpleNode()
    {
        String code = "<s:Button />";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button></Button>");
    }

    @Test
    public void testSimpleNodeWithId()
    {
        String code = "<s:Button id=\"myBtn\"/>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button id=\"myBtn\"></Button>");
    }

    @Test
    public void testSimpleNodeWithAttribute()
    {
        String code = "<s:Button label=\"Click me\" />";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button label=\"Click me\"></Button>");
    }

    @Test
    public void testSimpleNodeWithInnerText()
    {
        String code = "<s:Button>Click me</s:Button>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button label=\"Click me\"></Button>");
    }

    @Test
    public void testAnotherSimpleNodeWithInnerText()
    {
        String code = "<s:Label>Hello World!</s:Label>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Label text=\"Hello World!\"></Label>");
    }

    @Test
    public void testSimpleNodeWithMultipleAttributes()
    {
        String code = "<s:Button visible=\"false\" x=\"100\" width=\"1.5\" label=\"Click me ;-)\" color=\"0xFF0000\"/>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Button visible=\"false\" color=\"16711680\" width=\"1.5\" label=\"Click me ;-)\" x=\"100\"></Button>");
    }

    @Test
    public void testNodeWithChild()
    {
        String code = "<s:Group><s:RadioButton /></s:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group><RadioButton></RadioButton></Group>");
    }

    @Test
    public void testNodeWithChildAndAttribute()
    {
        String code = "<s:Group id=\"myGrp\"><s:RadioButton /></s:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group id=\"myGrp\"><RadioButton></RadioButton></Group>");
    }

    @Test
    public void testNodeWithNestedChildren()
    {
        String code = "<s:Group><s:Group><s:Group>" + "<s:RadioButton />"
                + "</s:Group></s:Group></s:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group><Group><Group><RadioButton></RadioButton></Group></Group></Group>");
    }

    @Test
    public void testNodeWithNestedChildrenAndAttribute()
    {
        String code = "<s:Group><s:Group><s:Group>"
                + "<s:RadioButton id=\"myRB\"/>"
                + "</s:Group></s:Group></s:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group><Group><Group><RadioButton id=\"myRB\"></RadioButton></Group></Group></Group>");
    }

    @Test
    public void testNodeWithNestedChildrenAndInnerText()
    {
        String code = "<s:Group><s:Group><s:Group>"
                + "<s:Button>Click me</s:Button>"
                + "</s:Group></s:Group></s:Group>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<Group><Group><Group><Button label=\"Click me\"></Button></Group></Group></Group>");
    }

}
