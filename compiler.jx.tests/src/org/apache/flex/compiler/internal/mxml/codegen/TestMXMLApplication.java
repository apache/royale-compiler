package org.apache.flex.compiler.internal.mxml.codegen;

import org.apache.flex.compiler.test.MXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestMXMLApplication extends MXMLTestBase
{

    @Ignore
    @Test
    public void MXML_simple()
    {
        String code = "<s:application />";
        
        IMXMLFileNode node = compileMXML(code);

        walker.visitFile(node);
    }

    @Ignore
    @Test
    public void MXMLApplication_component()
    {
        String code = "<component className=\"org.apache.flex.core:Application\" name=\"Application\" uri=\"library://ns.apache.org/flexjs/basic\"  />";
        
        IMXMLFileNode node = compileMXML(code);

        walker.visitFile(node);
    }

}
