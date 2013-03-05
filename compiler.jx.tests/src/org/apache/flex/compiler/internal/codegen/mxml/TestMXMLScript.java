package org.apache.flex.compiler.internal.codegen.mxml;

import org.apache.flex.compiler.internal.test.MXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.junit.Ignore;
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
                + "    private const GREETING:String = \"Hello world!\";"
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitScript(node);

        assertOut("<script><![CDATA[\n\tprivate const GREETING:String = \"Hello world!\";\n]]></script>");
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

        assertOut("<script><![CDATA[\n\tpublic var goodbye:String = \"Bye bye :-(\";\n\tprivate const GREETING:String = \"Hello world!\";\n]]></script>");
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

    @Ignore
    @Test
    public void testFunctionScript()
    {
        // TODO (erikdebruin) this isn't working...
        String code = "" + "<fx:Script><![CDATA["
                + "    public static function beNice(input:*):Object" + "    {"
                + "        Alert.show(\"I'm nice :-P\");"
                + "        return null;" + "    }" + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitScript(node);

        System.out.println(writer.toString());

        assertOut("<script><![CDATA[\n\tvar n:int = 3;\n\tfor (var i:int = 0; i < n; i++) {\n\tAlert.show(\"Hi\");\n};\n]]></script>");
    }

}
