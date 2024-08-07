
/*
*
*  Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  See the NOTICE file distributed with
*  this work for additional information regarding copyright ownership.
*  The ASF licenses this file to You under the Apache License, Version 2.0
*  (the "License"); you may not use this file except in compliance with
*  the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*/

package org.apache.royale.compiler.internal.tree.as;

import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Assert;
import org.junit.Test;

public class ASStatementsTests extends ASTestBase
{
	//----------------------------------
	// var declaration
	//----------------------------------

	@Test
	public void testVarDeclaration()
	{
		IVariableNode node = (IVariableNode) getNode("var a;",
				IVariableNode.class);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(5, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(5, node.getEndColumn() - parentNode.getColumn());

		IExpressionNode nameExpressionNode = node.getNameExpressionNode();
		Assert.assertEquals(4, nameExpressionNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(5, nameExpressionNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode.getLine() - node.getLine());
		Assert.assertEquals(4, nameExpressionNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode.getEndLine() - node.getLine());
		Assert.assertEquals(5, nameExpressionNode.getEndColumn() - node.getColumn());
	}

	@Test
	public void testVarDeclaration_withType()
	{
		IVariableNode node = (IVariableNode) getNode("var a:int;",
				IVariableNode.class);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(9, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(9, node.getEndColumn() - parentNode.getColumn());

		IExpressionNode nameExpressionNode = node.getNameExpressionNode();
		Assert.assertEquals(4, nameExpressionNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(5, nameExpressionNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode.getLine() - node.getLine());
		Assert.assertEquals(4, nameExpressionNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode.getEndLine() - node.getLine());
		Assert.assertEquals(5, nameExpressionNode.getEndColumn() - node.getColumn());

		IExpressionNode varTypeNode = node.getVariableTypeNode();
		Assert.assertEquals(6, varTypeNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(9, varTypeNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, varTypeNode.getLine() - node.getLine());
		Assert.assertEquals(6, varTypeNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, varTypeNode.getEndLine() - node.getLine());
		Assert.assertEquals(9, varTypeNode.getEndColumn() - node.getColumn());
	}

	@Test
	public void testVarDeclaration_withTypeAssignedValue()
	{
		IVariableNode node = (IVariableNode) getNode("var a:int = 42;",
				IVariableNode.class);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(14, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(14, node.getEndColumn() - parentNode.getColumn());

		IExpressionNode nameExpressionNode = node.getNameExpressionNode();
		Assert.assertEquals(4, nameExpressionNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(5, nameExpressionNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode.getLine() - node.getLine());
		Assert.assertEquals(4, nameExpressionNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode.getEndLine() - node.getLine());
		Assert.assertEquals(5, nameExpressionNode.getEndColumn() - node.getColumn());

		IExpressionNode varTypeNode = node.getVariableTypeNode();
		Assert.assertEquals(6, varTypeNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(9, varTypeNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, varTypeNode.getLine() - node.getLine());
		Assert.assertEquals(6, varTypeNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, varTypeNode.getEndLine() - node.getLine());
		Assert.assertEquals(9, varTypeNode.getEndColumn() - node.getColumn());

		IExpressionNode assignedValueNode = node.getAssignedValueNode();
		Assert.assertEquals(12, assignedValueNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(14, assignedValueNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, assignedValueNode.getLine() - node.getLine());
		Assert.assertEquals(12, assignedValueNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, assignedValueNode.getEndLine() - node.getLine());
		Assert.assertEquals(14, assignedValueNode.getEndColumn() - node.getColumn());
	}

	@Test
	public void testVarDeclaration_withList()
	{
		IVariableNode node = (IVariableNode) getNode(
				"var a:int = 4, b:int = 11, c:int = 42;", IVariableNode.class);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(37, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(37, node.getEndColumn() - parentNode.getColumn());

		IExpressionNode nameExpressionNode1 = node.getNameExpressionNode();
		Assert.assertEquals(4, nameExpressionNode1.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(5, nameExpressionNode1.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode1.getLine() - node.getLine());
		Assert.assertEquals(4, nameExpressionNode1.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode1.getEndLine() - node.getLine());
		Assert.assertEquals(5, nameExpressionNode1.getEndColumn() - node.getColumn());

		IExpressionNode varTypeNode1 = node.getVariableTypeNode();
		Assert.assertEquals(6, varTypeNode1.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(9, varTypeNode1.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, varTypeNode1.getLine() - node.getLine());
		Assert.assertEquals(6, varTypeNode1.getColumn() - node.getColumn());
		Assert.assertEquals(0, varTypeNode1.getEndLine() - node.getLine());
		Assert.assertEquals(9, varTypeNode1.getEndColumn() - node.getColumn());

		IExpressionNode assignedValueNode1 = node.getAssignedValueNode();
		Assert.assertEquals(12, assignedValueNode1.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(13, assignedValueNode1.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, assignedValueNode1.getLine() - node.getLine());
		Assert.assertEquals(12, assignedValueNode1.getColumn() - node.getColumn());
		Assert.assertEquals(0, assignedValueNode1.getEndLine() - node.getLine());
		Assert.assertEquals(13, assignedValueNode1.getEndColumn() - node.getColumn());

		IVariableNode node2 = (IVariableNode) node.getChild(4);
		Assert.assertNotNull(node2);

		IExpressionNode nameExpressionNode2 = node2.getNameExpressionNode();
		Assert.assertEquals(15, nameExpressionNode2.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(16, nameExpressionNode2.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode2.getLine() - node.getLine());
		Assert.assertEquals(15, nameExpressionNode2.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode2.getEndLine() - node.getLine());
		Assert.assertEquals(16, nameExpressionNode2.getEndColumn() - node.getColumn());

		IExpressionNode varTypeNode2 = node2.getVariableTypeNode();
		Assert.assertEquals(17, varTypeNode2.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(20, varTypeNode2.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, varTypeNode2.getLine() - node.getLine());
		Assert.assertEquals(17, varTypeNode2.getColumn() - node.getColumn());
		Assert.assertEquals(0, varTypeNode2.getEndLine() - node.getLine());
		Assert.assertEquals(20, varTypeNode2.getEndColumn() - node.getColumn());

		IExpressionNode assignedValueNode2 = node2.getAssignedValueNode();
		Assert.assertEquals(23, assignedValueNode2.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(25, assignedValueNode2.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, assignedValueNode2.getLine() - node.getLine());
		Assert.assertEquals(23, assignedValueNode2.getColumn() - node.getColumn());
		Assert.assertEquals(0, assignedValueNode2.getEndLine() - node.getLine());
		Assert.assertEquals(25, assignedValueNode2.getEndColumn() - node.getColumn());

		IVariableNode node3 = (IVariableNode) node.getChild(5);
		Assert.assertNotNull(node3);

		IExpressionNode nameExpressionNode3 = node3.getNameExpressionNode();
		Assert.assertEquals(27, nameExpressionNode3.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(28, nameExpressionNode3.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode3.getLine() - node.getLine());
		Assert.assertEquals(27, nameExpressionNode3.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode3.getEndLine() - node.getLine());
		Assert.assertEquals(28, nameExpressionNode3.getEndColumn() - node.getColumn());

		IExpressionNode varTypeNode3 = node3.getVariableTypeNode();
		Assert.assertEquals(29, varTypeNode3.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(32, varTypeNode3.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, varTypeNode3.getLine() - node.getLine());
		Assert.assertEquals(29, varTypeNode3.getColumn() - node.getColumn());
		Assert.assertEquals(0, varTypeNode3.getEndLine() - node.getLine());
		Assert.assertEquals(32, varTypeNode3.getEndColumn() - node.getColumn());

		IExpressionNode assignedValueNode3 = node3.getAssignedValueNode();
		Assert.assertEquals(35, assignedValueNode3.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(37, assignedValueNode3.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, assignedValueNode3.getLine() - node.getLine());
		Assert.assertEquals(35, assignedValueNode3.getColumn() - node.getColumn());
		Assert.assertEquals(0, assignedValueNode3.getEndLine() - node.getLine());
		Assert.assertEquals(37, assignedValueNode3.getEndColumn() - node.getColumn());
	}

	//----------------------------------
	// const declaration
	//----------------------------------

	@Test
	public void testConstDeclaration()
	{
		IVariableNode node = (IVariableNode) getNode("const a = 42;",
				IVariableNode.class);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(12, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(12, node.getEndColumn() - parentNode.getColumn());

		IExpressionNode nameExpressionNode = node.getNameExpressionNode();
		Assert.assertEquals(6, nameExpressionNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(7, nameExpressionNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode.getLine() - node.getLine());
		Assert.assertEquals(6, nameExpressionNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode.getEndLine() - node.getLine());
		Assert.assertEquals(7, nameExpressionNode.getEndColumn() - node.getColumn());

		IExpressionNode assignedValueNode = node.getAssignedValueNode();
		Assert.assertEquals(10, assignedValueNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(12, assignedValueNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, assignedValueNode.getLine() - node.getLine());
		Assert.assertEquals(10, assignedValueNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, assignedValueNode.getEndLine() - node.getLine());
		Assert.assertEquals(12, assignedValueNode.getEndColumn() - node.getColumn());
	}

	@Test
	public void testConstDeclaration_withType()
	{
		IVariableNode node = (IVariableNode) getNode("const a:int = 42;",
				IVariableNode.class);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(16, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(16, node.getEndColumn() - parentNode.getColumn());

		IExpressionNode nameExpressionNode = node.getNameExpressionNode();
		Assert.assertEquals(6, nameExpressionNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(7, nameExpressionNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode.getLine() - node.getLine());
		Assert.assertEquals(6, nameExpressionNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode.getEndLine() - node.getLine());
		Assert.assertEquals(7, nameExpressionNode.getEndColumn() - node.getColumn());

		IExpressionNode varTypeNode = node.getVariableTypeNode();
		Assert.assertEquals(8, varTypeNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(11, varTypeNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, varTypeNode.getLine() - node.getLine());
		Assert.assertEquals(8, varTypeNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, varTypeNode.getEndLine() - node.getLine());
		Assert.assertEquals(11, varTypeNode.getEndColumn() - node.getColumn());

		IExpressionNode assignedValueNode = node.getAssignedValueNode();
		Assert.assertEquals(14, assignedValueNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(16, assignedValueNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, assignedValueNode.getLine() - node.getLine());
		Assert.assertEquals(14, assignedValueNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, assignedValueNode.getEndLine() - node.getLine());
		Assert.assertEquals(16, assignedValueNode.getEndColumn() - node.getColumn());
	}
}