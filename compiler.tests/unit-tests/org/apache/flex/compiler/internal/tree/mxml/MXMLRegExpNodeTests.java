package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLRegExpNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLRegExpNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLRegExpNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLRegExpNode getMXMLRegExpNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLRegExpNode node = (IMXMLRegExpNode)findFirstDescendantOfType(fileNode, IMXMLRegExpNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLRegExpID));
		assertThat("getName", node.getName(), is("RegExp"));
		return node;
	}
	
	@Test
	public void MXMLRegExpNode_empty1()
	{
		String code = "<fx:RegExp/>";
		IMXMLRegExpNode node = getMXMLRegExpNode(code);
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLRegExpNode_empty2()
	{
		String code = "<fx:RegExp></fx:RegExp>";
		IMXMLRegExpNode node = getMXMLRegExpNode(code);
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLRegExpNode_empty3()
	{
		String code = "<fx:RegExp> \t\r\n</fx:RegExp>";
		IMXMLRegExpNode node = getMXMLRegExpNode(code);
		assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
}
