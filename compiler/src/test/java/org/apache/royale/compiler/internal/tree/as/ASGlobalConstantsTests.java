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

import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IOperatorNode.OperatorType;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Assert;
import org.junit.Test;

public class ASGlobalConstantsTests extends ASTestBase
{
	@Test
	public void testUndefined()
	{
        IVariableNode varNode = getField("var a:* = undefined;");

		IIdentifierNode node = (IIdentifierNode) varNode.getAssignedValueNode();
		Assert.assertEquals("undefined", node.getName());
		
		IDefinition definition = node.resolve(project);
		Assert.assertNotNull(definition);
		Assert.assertEquals(project.getScope().getUndefinedValueDefinition(), definition);

		Assert.assertEquals(10, node.getAbsoluteStart() - varNode.getAbsoluteStart());
		Assert.assertEquals(19, node.getAbsoluteEnd() - varNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - varNode.getLine());
		Assert.assertEquals(10, node.getColumn() - varNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - varNode.getLine());
		Assert.assertEquals(19, node.getEndColumn() - varNode.getColumn());
	}

	@Test
	public void testNaN()
	{
        IVariableNode varNode = getField("var a:Number = NaN;");

		IIdentifierNode node = (IIdentifierNode) varNode.getAssignedValueNode();
		Assert.assertEquals("NaN", node.getName());

		Assert.assertEquals(15, node.getAbsoluteStart() - varNode.getAbsoluteStart());
		Assert.assertEquals(18, node.getAbsoluteEnd() - varNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - varNode.getLine());
		Assert.assertEquals(15, node.getColumn() - varNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - varNode.getLine());
		Assert.assertEquals(18, node.getEndColumn() - varNode.getColumn());
	}

	@Test
	public void testInfinity()
	{
        IVariableNode varNode = getField("var a:Number = Infinity;");

		IIdentifierNode node = (IIdentifierNode) varNode.getAssignedValueNode();
		Assert.assertEquals("Infinity", node.getName());

		Assert.assertEquals(15, node.getAbsoluteStart() - varNode.getAbsoluteStart());
		Assert.assertEquals(23, node.getAbsoluteEnd() - varNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - varNode.getLine());
		Assert.assertEquals(15, node.getColumn() - varNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - varNode.getLine());
		Assert.assertEquals(23, node.getEndColumn() - varNode.getColumn());
	}

	@Test
	public void testNegativeInfinity()
	{
        IVariableNode varNode = getField("var a:Number = -Infinity;");

		IUnaryOperatorNode node = (IUnaryOperatorNode) varNode.getAssignedValueNode();
		Assert.assertEquals(OperatorType.MINUS, node.getOperator());

		IIdentifierNode identifierNode = (IIdentifierNode) node.getOperandNode();
		Assert.assertEquals("Infinity", identifierNode.getName());

		Assert.assertEquals(15, node.getAbsoluteStart() - varNode.getAbsoluteStart());
		Assert.assertEquals(24, node.getAbsoluteEnd() - varNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - varNode.getLine());
		Assert.assertEquals(15, node.getColumn() - varNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - varNode.getLine());
		Assert.assertEquals(24, node.getEndColumn() - varNode.getColumn());

		Assert.assertEquals(15, node.getOperatorAbsoluteStart() - varNode.getAbsoluteStart());
		Assert.assertEquals(16, node.getOperatorAbsoluteEnd() - varNode.getAbsoluteStart());
	}
}