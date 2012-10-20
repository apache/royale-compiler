package org.apache.flex.compiler.internal.tree.mxml;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLBooleanNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLBooleanNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLBooleanNodeTests extends MXMLExpressionNodeBaseTests
{	
	private IMXMLBooleanNode getMXMLBooleanNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLBooleanNode node = (IMXMLBooleanNode)findFirstDescendantOfType(fileNode, IMXMLBooleanNode.class);
		assertThat(node.getNodeID(), is(ASTNodeID.MXMLBooleanID));
		assertThat(node.getName(), is("Boolean"));
		return node;
	}
	
	@Test
	public void MXMLBooleanNode_empty1()
	{
		String code = "<fx:Boolean/>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(false));
		//assertThat(node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLBooleanNode_empty2()
	{
		String code = "<fx:Boolean></fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(false));
		//assertThat(node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLBooleanNode_empty3()
	{
		String code = "<fx:Boolean> \t\r\n</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(false));
		//assertThat(node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLBooleanNode_false()
	{
		String code = "<fx:Boolean>false</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(false));
		testExpressionLocation(node, 12, 17);
	}

	@Test
	public void MXMLBooleanNode_true()
	{
		String code = "<fx:Boolean>true</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(true));
		testExpressionLocation(node, 12, 16);
	}

	@Test
	public void MXMLBooleanNode_false_caseinsensitive()
	{
		String code = "<fx:Boolean>FaLsE</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(false));
		testExpressionLocation(node, 12, 17);
	}

	@Test
	public void MXMLBooleanNode_true_caseinsensitive()
	{
		String code = "<fx:Boolean>TruE</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(true));
		testExpressionLocation(node, 12, 16);
	}
	
	@Test
	public void MXMLBooleanNode_false_with_whitespace()
	{
		String code = "<fx:Boolean> false </fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(false));
		//testExpressionLocation(node, 13, 18);  // location of the MXMLLiteralNode should not include the whitespace 
	}

	@Test
	public void MXMLBooleanNode_true_with_whitespace()
	{
		String code = "<fx:Boolean> true </fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(true));
		//testExpressionLocation(node, 13, 17); // location of the MXMLLiteralNode should not include the whitespace
	}

	@Ignore
	@Test
	public void MXMLBooleanNode_abc()
	{
		String code = "<fx:Boolean>abc</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(false));
		assertThat(node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLBooleanNode_with_xmlns_attribute()
	{
		String code = "<fx:Boolean xmlns:foo='bar'>true</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(true));
		testExpressionLocation(node, 28, 32);
	}
	
	@Test
	public void MXMLBooleanNode_with_id_attribute()
	{
		String code = "<fx:Boolean id='b1'>true</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(true));
		testExpressionLocation(node, 20, 24);
	}
	
	@Test
	public void MXMLBooleanNode_with_unrecognized_attribute()
	{
		String code = "<fx:Boolean foo='bar'>true</fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(true));
		testExpressionLocation(node, 22, 26);
		// check problem
	}
	
	@Ignore
	@Test
	public void MXMLBooleanNode_with_child_tag()
	{
		String code = "<fx:Boolean>true<foo/></fx:Boolean>";
		IMXMLBooleanNode node = getMXMLBooleanNode(code);
		assertThat(node.getValue(), is(true));
	}
}
