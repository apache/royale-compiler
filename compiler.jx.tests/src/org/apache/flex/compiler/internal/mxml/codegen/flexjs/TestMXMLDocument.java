package org.apache.flex.compiler.internal.mxml.codegen.flexjs;

import java.io.File;

import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestMXMLDocument extends FlexJSTestBase
{

    @Ignore
    @Test
    public void MXMLClassNode_flashDisplaySprite()
    {
        String path = "flexjs" + File.separator + 
                      "projects" + File.separator + 
                      "FlexJSTest" + File.separator + 
                      "src";
        
        IMXMLFileNode node = compileMXML("FlexJSTest", true, path);

        mxmlBlockWalker.visitFile(node);
    }

}
