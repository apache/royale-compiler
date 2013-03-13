package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

public class TestFlexJSMXMLApplication extends FlexJSTestBase
{

    @Test
    public void testFlexJSMainFile()
    {
        String fileName = "FlexJSTest_2013_03_11";

        IMXMLFileNode node = compileMXML(fileName, true,
                "test-files/flexjs/files", false);

        mxmlBlockWalker.visitFile(node);

        //writeResultToFile(writer.toString(), fileName); // for external comparison

        assertOut(getCodeFromFile(fileName + "_result", true, "flexjs/files"));
    }

    @Test
    public void testFlexJSInitialViewFile()
    {
        String fileName = "MyInitialView_2013_03_11";

        IMXMLFileNode node = compileMXML(fileName, true,
                "test-files/flexjs/files", false);

        mxmlBlockWalker.visitFile(node);

        //writeResultToFile(writer.toString(), fileName); // for external comparison

        assertOut(getCodeFromFile(fileName + "_result", true, "flexjs/files"));
    }

}
