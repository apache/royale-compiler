package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLStringNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLStringNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLStringNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLStringNode getMXMLStringNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLStringNode node = (IMXMLStringNode)findFirstDescendantOfType(fileNode, IMXMLStringNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLStringID));
		assertThat("getName", node.getName(), is("String"));
		return node;
	}
	
	@Test
	public void MXMLStringNode_empty1()
	{
		String code = "<fx:String/>";
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is((String)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLStringNode_empty2()
	{
		String code = "<fx:String></fx:String>";
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is((String)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLStringNode_empty3()
	{
		String code = "<fx:String> \t\r\n</fx:String>";
		IMXMLStringNode node = getMXMLStringNode(code);
		assertThat("getValue", node.getValue(), is((String)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
}
