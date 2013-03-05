package org.apache.flex.compiler.internal.mxml.codegen.flexjs;

import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestFlexJSMXMLApplication extends FlexJSTestBase
{

    // TODO (erikdebruin) this needs to become JS Goog output ;-)

    @Test
    public void testBasicFlexJSApp()
    {
        String code = ""
                + "<basic:Application xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                + "</basic:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application>\n</Application>");
    }

    @Test
    public void testFlexJSAppWithEvent()
    {
        String code = ""
                + "<basic:Application xmlns:basic=\"library://ns.apache.org/flexjs/basic\""
                + "                   initialize=\"MyModel(model).labelText='Hello World'\">"
                + "</basic:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application initialize=\"MyModel(model).labelText = 'Hello World'\">\n</Application>");
    }

    @Test
    public void testFlexJSAppWithNode()
    {
        String code = ""
                + "<basic:Application xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                + "    <basic:beads />" + "</basic:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application>\n\t<beads></beads>\n</Application>");
    }

    @Test
    public void testFlexJSAppWithNodeAndChild()
    {
        String code = ""
                + "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                + "                   xmlns:local=\"*\""
                + "                   xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                + "    <basic:beads>"
                + "        <basic:HTTPService id=\"service\">"
                + "            <basic:LazyCollection id=\"collection\">"
                + "                <basic:inputParser>"
                + "                    <basic:JSONInputParser />"
                + "                </basic:inputParser>"
                + "                <basic:itemConverter>"
                + "                    <local:StockDataJSONItemConverter />"
                + "                </basic:itemConverter>"
                + "            </basic:LazyCollection id=\"collection\">"
                + "        </basic:HTTPService>" + "    </basic:beads>"
                + "</basic:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application>\n\t<beads>\n\t\t<HTTPService id=\"service\">\n\t\t\t<beads>\n\t\t\t\t<LazyCollection id=\"collection\">\n\t\t\t\t\t<inputParser>\n\t\t\t\t\t\t<JSONInputParser>\n\t\t\t\t\t\t</JSONInputParser>\n\t\t\t\t\t</inputParser>\n\t\t\t\t\t<itemConverter>\n\t\t\t\t\t\t<StockDataJSONItemConverter>\n\t\t\t\t\t\t</StockDataJSONItemConverter>\n\t\t\t\t\t</itemConverter>\n\t\t\t\t</LazyCollection>\n\t\t\t</beads>\n\t\t</HTTPService>\n\t</beads>\n</Application>");
    }

    @Ignore
    @Test
    public void MXMLClassNode_flashDisplaySprite()
    {
        IMXMLFileNode node = compileMXML("FlexJSTest", true, asjsRoot
                + "examples/FlexJSTest_again", false);

        mxmlBlockWalker.visitFile(node);

        //System.out.println(writer.toString());

        assertOut("");
    }
}
