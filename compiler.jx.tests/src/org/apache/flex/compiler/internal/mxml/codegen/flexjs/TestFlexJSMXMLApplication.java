package org.apache.flex.compiler.internal.mxml.codegen.flexjs;

import java.io.File;

import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestFlexJSMXMLApplication extends FlexJSTestBase
{

    @Ignore
    @Test
    public void testBasicAppMx()
    {
        String code = ""
                + "<basic:Application xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                + "</basic:Application>";

        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);

        // TODO (erikdebruin) this needs to become JS Goog output ;-)
        assertOut("<Application>\n</Application>");
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
