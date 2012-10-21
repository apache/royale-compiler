package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLUintNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLUintNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLUintNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLUintNode getMXMLUintNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLUintNode node = (IMXMLUintNode)findFirstDescendantOfType(fileNode, IMXMLUintNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLUintID));
		assertThat("getName", node.getName(), is("uint"));
		return node;
	}
	
	@Test
	public void MXMLUintNode_empty1()
	{
		String code = "<fx:uint/>";
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLUintNode_empty2()
	{
		String code = "<fx:uint></fx:uint>";
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLUintNode_empty3()
	{
		String code = "<fx:uint> \t\r\n</fx:uint>";
		IMXMLUintNode node = getMXMLUintNode(code);
		assertThat("getValue", node.getValue(), is(0L));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
}
