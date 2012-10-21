package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNumberNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLNumberNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLNumberNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLNumberNode getMXMLNumberNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLNumberNode node = (IMXMLNumberNode)findFirstDescendantOfType(fileNode, IMXMLNumberNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLNumberID));
		assertThat("getName", node.getName(), is("Number"));
		return node;
	}
	
	@Test
	public void MXMLNumberNode_empty1()
	{
		String code = "<fx:Number/>";
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLNumberNode_empty2()
	{
		String code = "<fx:Number></fx:Number>";
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLNumberNode_empty3()
	{
		String code = "<fx:Number> \t\r\n</fx:Number>";
		IMXMLNumberNode node = getMXMLNumberNode(code);
		assertThat("getValue", node.getValue(), is(Double.NaN));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
}
