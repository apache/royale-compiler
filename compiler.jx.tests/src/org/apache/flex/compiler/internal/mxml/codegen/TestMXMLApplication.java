package org.apache.flex.compiler.internal.mxml.codegen;

import org.apache.flex.compiler.internal.test.MXMLTestBase;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

public class TestMXMLApplication extends MXMLTestBase
{

    @Test
    public void testSimple()
    {
        String code = "" +
                "<s:Application xmlns:s=\"library://ns.adobe.com/flex/spark\">" +
                "</s:Application>";
        
        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);
        
        assertOut("<Application>\n\t\n</Application>");
    }

    
    @Test
    public void testOneComponent()
    {
        String code = "" +
        		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        		"<s:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:s=\"library://ns.adobe.com/flex/spark\" xmlns:mx=\"library://ns.adobe.com/flex/mx\">\n" +
                "\t<s:Button id=\"myBtn\" label=\"Hello world\"></s:Button>\n" +
        		"</s:Application>";
        
        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);
        
        assertOut("<Application>\n\t<Button id=\"myBtn\" label=\"Hello world\"></Button>\n</Application>");
    }

    @Ignore
    @Test
    public void MXMLApplication_component()
    {
        String code = "<component className=\"org.apache.flex.core:Application\" " +
        		"name=\"Application\" uri=\"library://ns.apache.org/flexjs/basic\" />";
        
        IMXMLFileNode node = compileMXML(code);

        mxmlBlockWalker.visitFile(node);
    }

}
