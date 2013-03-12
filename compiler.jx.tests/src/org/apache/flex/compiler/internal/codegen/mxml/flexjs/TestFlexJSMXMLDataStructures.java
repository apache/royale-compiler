package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import org.apache.flex.compiler.internal.test.FlexJSTestBase;
import org.apache.flex.compiler.internal.test.MXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLPropertySpecifierNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestFlexJSMXMLDataStructures extends FlexJSTestBase
{

    // MXMLProperties
    
    @Ignore
    @Test
    public void testBasicFlexJSDataStructure()
    {
        String code = ""
                + "<basic:beads\">"
                + "</basic:beads>";

        IMXMLPropertySpecifierNode node = (IMXMLPropertySpecifierNode) getNode(
                code, IMXMLPropertySpecifierNode.class,
                MXMLTestBase.WRAP_LEVEL_DOCUMENT);

        mxmlBlockWalker.visitPropertySpecifier(node);

        assertOut("<beads>\n</beads>");
    }

}
