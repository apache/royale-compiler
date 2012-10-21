package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLClassNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLClassNode}.
 * 
 * @author Gordon Smith
 */
public class MXMLClassNodeTests extends MXMLExpressionNodeBaseTests
{
	private IMXMLClassNode getMXMLClassNode(String code)
	{
		IMXMLFileNode fileNode = getMXMLFileNode(code);
		IMXMLClassNode node = (IMXMLClassNode)findFirstDescendantOfType(fileNode, IMXMLClassNode.class);
		assertThat("getNodeID", node.getNodeID(), is(ASTNodeID.MXMLClassID));
		assertThat("getName", node.getName(), is("Class"));
		return node;
	}
	
	@Test
	public void MXMLClassNode_empty1()
	{
		String code = "<fx:Class/>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("getValue", node.getValue(project), is((ITypeDefinition)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
	
	@Test
	public void MXMLClassNode_empty2()
	{
		String code = "<fx:Class></fx:Class>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("getValue", node.getValue(project), is((ITypeDefinition)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLClassNode_empty3()
	{
		String code = "<fx:Class> \t\r\n</fx:Class>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("getValue", node.getValue(project), is((ITypeDefinition)null));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}

	@Test
	public void MXMLClassNode_flashDisplaySprite()
	{
		String code = "<fx:Class>flash.display.Sprite</fx:Class>";
		IMXMLClassNode node = getMXMLClassNode(code);
		assertThat("getValue", node.getValue(project).getQualifiedName(), is("flash.display.Sprite"));
		//assertThat("getExpressionNode", node.getExpressionNode(), is((IASNode)null));
	}
}
