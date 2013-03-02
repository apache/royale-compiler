package org.apache.flex.compiler.internal.mxml.codegen.flexjs;

import java.io.File;

import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestFlexJSMXMLApplication extends FlexJSTestBase
{

    /*
    <basic:beads>
        <basic:HTTPService id="service">
            <basic:LazyCollection id="collection">
                <basic:inputParser>
                    <basic:JSONInputParser />
                </basic:inputParser>
                <basic:itemConverter>
                    <local:StockDataJSONItemConverter />
                </basic:itemConverter> 
            </basic:LazyCollection>
        </basic:HTTPService>
    </basic:beads>
    //*/
    
    // TODO (erikdebruin) this needs to become JS Goog output ;-)
    
    @Test
    public void testBasicFlexJSApp()
    {
        String code = ""
                + "<basic:Application xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                + "</basic:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application>\n\t\n</Application>");
    }

    @Test
    public void testFlexJSAppWithNode()
    {
        String code = ""
                + "<basic:Application xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                + "    <basic:beads />"
                + "</basic:Application>";
        
        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        assertOut("<Application>\n\t<beads></beads>\n</Application>");
    }
    
    @Ignore
    @Test
    public void testFlexJSAppWithNodeAndChild()
    {
        String code = ""
                + "<basic:Application xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                + "    <basic:beads>"
                + "        <basic:HTTPService id=\"service\">"
                + "            <basic:LazyCollection id=\"collection\" />"
                + "        </basic:HTTPService>"
                + "    </basic:beads>"
                + "</basic:Application>";
        
        IMXMLFileNode node = compileMXML(code);
        
        mxmlBlockWalker.visitFile(node);
        
        System.out.println(writer.toString());
        
        assertOut("<Application>\n\t<beads><org.apache.flex.net.HTTPService id=\"service\"></org.apache.flex.net.HTTPService></beads>\n</Application>");
    }

    @Ignore
    @Test
    public void MXMLClassNode_flashDisplaySprite()
    {
        String path = "flexjs"
                + File.separator + "projects" + File.separator + "FlexJSTest"
                + File.separator + "src";

        IMXMLFileNode node = compileMXML("FlexJSTest", true, path);

        mxmlBlockWalker.visitFile(node);
    }

}
