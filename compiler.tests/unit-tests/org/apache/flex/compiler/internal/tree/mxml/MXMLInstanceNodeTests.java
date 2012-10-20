package org.apache.flex.compiler.internal.tree.mxml;

import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLInstanceNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLInstanceNodeTests extends MXMLClassReferenceNodeTests
{
	protected static String PREFIX =
		"<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*'>\n" +
	    "    <fx:Declarations>\n" +
		"        ";
			
    protected static String POSTFIX =
		"\n" +
		"    </fx:Declarations>\n" +
		"</d:Sprite>";
    
    @Override
    protected IMXMLFileNode getMXMLFileNode(String code)
    {
    	return super.getMXMLFileNode(PREFIX + code + POSTFIX);
    }
    
	@Test
	public void MXMLInstanceNodeBase_placeholder()
	{
	}
}
