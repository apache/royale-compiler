package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestFlexJSMXMLApplication extends FlexJSTestBase
{

    @Ignore
    @Test
    public void testFile()
    {
        String fileName = "wildcard_import";

        IMXMLFileNode node = compileMXML(fileName, true,
                "test-files/flexjs/files", false);

        mxmlBlockWalker.visitFile(node);
        
        //writeResultToFile(writer.toString(), fileName);

        assertOut(getCodeFromFile(fileName + "_result", true, "flexjs/files"));
    }

    //@Test
    public void testFlexJSMainFile()
    {
        String fileName = "FlexJSTest_2013_03_11";

        IMXMLFileNode node = compileMXML(fileName, true,
                "test-files/flexjs/files", false);

        mxmlBlockWalker.visitFile(node);

        //writeResultToFile(writer.toString(), fileName);

        assertOut(getCodeFromFile(fileName + "_result", true, "flexjs/files"));
    }

    //@Test
    public void testFlexJSInitialViewFile()
    {
        String fileName = "MyInitialView_2013_03_11";

        IMXMLFileNode node = compileMXML(fileName, true,
                "test-files/flexjs/files", false);

        mxmlBlockWalker.visitFile(node);

        //writeResultToFile(writer.toString(), fileName);

        assertOut(getCodeFromFile(fileName + "_result", true, "flexjs/files"));
    }

}
