package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLIntNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLIntNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLIntNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLIntNode getMXMLIntNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLIntNode node = (IMXMLIntNode)findFirstDescendantOfType(fileNode, IMXMLIntNode.class);
		assertThat(node.getNodeID(), is(ASTNodeID.MXMLIntID));
		assertThat(node.getName(), is("int"));
		return node;
	}
	
	@Test
	public void MXMLIntNode_empty1()
	{
		String code = "<fx:int/>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(0));
		//assertThat(node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLIntNode_empty2()
	{
		String code = "<fx:int></fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(0));
		//assertThat(node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLIntNode_empty3()
	{
		String code = "<fx:int> \t\r\n</fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(0));
		//assertThat(node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLIntNode_zero()
	{
		String code = "<fx:int>0</fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(0));
		testExpressionLocation(node, 8, 9);
	}

	@Test
	public void MXMLIntNode_minusZero()
	{
		String code = "<fx:int>-0</fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(0));
		testExpressionLocation(node, 8, 10);
	}

	@Test
	public void MXMLIntNode_one()
	{
		String code = "<fx:int>1</fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(1));
		testExpressionLocation(node, 8, 9);
	}

	@Test
	public void MXMLIntNode_minusOne()
	{
		String code = "<fx:int>-1</fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(-1));
		testExpressionLocation(node, 8, 10);
	}

	@Test
	public void MXMLIntNode_maxInt()
	{
		String code = "<fx:int>2147483647</fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(2147483647));
		testExpressionLocation(node, 8, 18);
	}

	@Test
	public void MXMLIntNode_minInt()
	{
		String code = "<fx:int>-2147483648</fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(-2147483648));
		testExpressionLocation(node, 8, 19);
	}
	
	@Test
	public void MXMLIntNode_withWhitespace()
	{
		String code = "<fx:int> -123 </fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(-123));
		//testExpressionLocation(node, 9, 13); // location of the MXMLLiteralNode should not include the whitespace
	}
	
	@Ignore
	@Test
	public void MXMLIntNode_nonnumeric()
	{
		String code = "<fx:int> abc </fx:int>";
		IMXMLIntNode node = getMXMLIntNode(code);
		assertThat(node.getValue(), is(0));
		assertThat(node.getExpressionNode(), is((IASNode)null));
	}
}
