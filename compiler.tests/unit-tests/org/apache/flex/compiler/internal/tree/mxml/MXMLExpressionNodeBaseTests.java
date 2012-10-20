package org.apache.flex.compiler.internal.tree.mxml;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.mxml.IMXMLExpressionNode;
import org.junit.Test;

/**
 * JUnit tests for {@link MXMLExpressionNodeBase}.
 * 
 * @author Gordon Smith
 */
public class MXMLExpressionNodeBaseTests extends MXMLInstanceNodeTests
{
	protected void testExpressionLocation(IMXMLExpressionNode node, int expectedStart, int expectedEnd)
	{
		IASNode expressionNode = node.getExpressionNode();
		assertThat(expressionNode.getStart(), is(PREFIX.length() + expectedStart));
		assertThat(expressionNode.getEnd(), is(PREFIX.length() + expectedEnd));
	}
	
	@Test
	public void MXMLExpressionNodeBase_placeholder()
	{
	}
}
