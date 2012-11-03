package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLScriptNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLScriptNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLScriptNodeTests extends MXMLNodeBaseTests
{
	private static String PREFIX =
	    "<d:Sprite xmlns:fx='http://ns.adobe.com/mxml/2009' xmlns:d='flash.display.*'>\n\t";
				
	private static String POSTFIX =
		"\n</d:Sprite>";
	    
	private static String EOL = "\n\t";
	
    @Override
    protected IMXMLFileNode getMXMLFileNode(String code)
    {
    	return super.getMXMLFileNode(PREFIX + code + POSTFIX);
    }
    
	private IMXMLScriptNode getMXMLScriptNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLScriptNode node = (IMXMLScriptNode)findFirstDescendantOfType(fileNode, IMXMLScriptNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLScriptID));
		assertThat("getName", node.getName(), is("Script"));
		return node;
	}
	
	@Test
	public void MXMLScriptNode_empty1()
	{
		String code = "<fx:Script/>";
		IMXMLScriptNode node = getMXMLScriptNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLScriptNode_empty2()
	{
		String code = "<fx:Script></fx:Script>";
		IMXMLScriptNode node = getMXMLScriptNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLScriptNode_empty3()
	{
		String code = "<fx:Script/> \t\r\n<fx:Script/>";
		IMXMLScriptNode node = getMXMLScriptNode(code);
		assertThat("getChildCount", node.getChildCount(), is(0));
	}
	
	@Test
	public void MXMLScriptNode_var_and_function()
	{
		String code =
			"<fx:Script>" + EOL +
			"    private var i:int = 1;" + EOL +
			"    private function f():void { };" + EOL +
			"</fx:Script>";
		IMXMLScriptNode node = getMXMLScriptNode(code);
		assertThat("getChildCount", node.getChildCount(), is(2));
		assertThat("child 0", node.getChild(0).getNodeID(), is(ASTNodeID.VariableID));
		assertThat("child 1", node.getChild(1).getNodeID(), is(ASTNodeID.FunctionID));
	}
}
