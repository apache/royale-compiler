package org.apache.flex.compiler.internal.mxml.codegen;

import org.apache.flex.compiler.internal.test.MXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestMXMLScript extends MXMLTestBase
{

    @Test
    public void testEmptyScript()
    {
        String code = ""
                + "<fx:Script><![CDATA[]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitScript(node);

        assertOut("");
    }

    @Ignore
    @Test
    public void testSimpleScript()
    {
        // TODO (erikdebruin) handle actual script contents... 
        //                    maybe use AS emitter?
        
        String code = ""
                + "<fx:Script><![CDATA["
                + "    private const GREETING:String = \"Hello world!\""
                + "]]></fx:Script>";

        IMXMLScriptNode node = (IMXMLScriptNode) getNode(code,
                IMXMLScriptNode.class, MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitScript(node);

        assertOut("");
    }

}
