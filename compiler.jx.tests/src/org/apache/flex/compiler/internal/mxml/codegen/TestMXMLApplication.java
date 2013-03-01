package org.apache.flex.compiler.internal.mxml.codegen;

import org.apache.flex.compiler.internal.test.MXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

public class TestMXMLApplication extends MXMLTestBase
{

    @Test
    public void testBasicApp()
    {
        String code = ""
                + "<s:Application xmlns:s=\"library://ns.adobe.com/flex/spark\">"
                + "</s:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application>\n</Application>");
    }

    @Test
    public void testBasicAppWithOneComponent()
    {
        String code = ""
                + "<s:Application xmlns:s=\"library://ns.adobe.com/flex/spark\">"
                + "    <s:Button id=\"myBtn\" label=\"Hello world\"></s:Button>"
                + "</s:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application>\n\t<Button id=\"myBtn\" label=\"Hello world\"></Button>\n</Application>");
    }

    @Test
    public void testBasicAppWithTwoComponents()
    {
        String code = ""
                + "<s:Application xmlns:s=\"library://ns.adobe.com/flex/spark\">"
                + "    <s:Label id=\"myLbl\" text=\"Bye bye\"></s:Label>"
                + "    <s:Button id=\"myBtn\" label=\"Hello world\"></s:Button>"
                + "</s:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application>\n\t<Label id=\"myLbl\" text=\"Bye bye\"></Label>\n\t<Button id=\"myBtn\" label=\"Hello world\"></Button>\n</Application>");
    }

    @Test
    public void testDefaultApp()
    {
        String code = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<s:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                + "               xmlns:s=\"library://ns.adobe.com/flex/spark\" "
                + "               xmlns:mx=\"library://ns.adobe.com/flex/mx\" "
                + "               minWidth=\"955\" minHeight=\"600\">"
                + "    <fx:Declarations>"
                + "        <!-- Place non-visual elements (e.g., services, value objects) here -->"
                + "    </fx:Declarations>" + "</s:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application minHeight=600 minWidth=955>\n</Application>");
    }

    @Test
    public void testDefaultAppWithOneComponent()
    {
        String code = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<s:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                + "               xmlns:s=\"library://ns.adobe.com/flex/spark\""
                + "               xmlns:mx=\"library://ns.adobe.com/flex/mx\""
                + "               minWidth=\"955\" minHeight=\"600\">"
                + "    <fx:Declarations>"
                + "        <!-- Place non-visual elements (e.g., services, value objects) here -->"
                + "    </fx:Declarations>"
                + "    <s:Button label=\"Click me\" />" + "</s:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application minHeight=600 minWidth=955>\n\t<Button label=\"Click me\"></Button>\n</Application>");
    }

    @Test
    public void testDefaultAppSimpleScript()
    {
        // TODO (erikdebruin) handle actual script contents... 
        //                    maybe use AS parser?
        String code = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<s:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                + "               xmlns:s=\"library://ns.adobe.com/flex/spark\""
                + "               xmlns:mx=\"library://ns.adobe.com/flex/mx\""
                + "               minWidth=\"955\" minHeight=\"600\">"
                + "    <fx:Declarations>"
                + "        <!-- Place non-visual elements (e.g., services, value objects) here -->"
                + "    </fx:Declarations>" + "    <fx:Script>"
                + "        <![CDATA["
                + "            // just a comment here, for now ;-)"
                + "        ]]>" + "    </fx:Script>" + "</s:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application minHeight=600 minWidth=955>\n</Application>");
    }

}
