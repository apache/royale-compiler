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

import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IKeywordNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.junit.Assert;
import org.junit.Test;

public class ASFieldMembersTests extends ASTestBase
{
	/*
		* Field, Constant, [Namespace]
		* 
		* var foo;
		* var foo:int;
		* var foo:int = 42;
		* private var foo:int;
		* private var foo:int = 42;
		* protected var foo:int;
		* public var foo:int;
		*/

	//--------------------------------------------------------------------------
	// Field
	//--------------------------------------------------------------------------

	@Test
	public void testField()
	{
		IVariableNode node = getField("var foo;");

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(7, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(7, node.getEndColumn() - parentNode.getColumn());

		IKeywordNode keywordNode = (IKeywordNode) node.getChild(0);
		Assert.assertEquals(ASTNodeID.KeywordVarID, keywordNode.getNodeID());
		Assert.assertEquals(0, keywordNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(3, keywordNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, keywordNode.getLine() - node.getLine());
		Assert.assertEquals(0, keywordNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, keywordNode.getEndLine() - node.getLine());
		Assert.assertEquals(3, keywordNode.getEndColumn() - node.getColumn());

		IExpressionNode nameExpressionNode = node.getNameExpressionNode();
		Assert.assertEquals(4, nameExpressionNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(7, nameExpressionNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode.getLine() - node.getLine());
		Assert.assertEquals(4, nameExpressionNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode.getEndLine() - node.getLine());
		Assert.assertEquals(7, nameExpressionNode.getEndColumn() - node.getColumn());
	}

	@Test
	public void testField_withType()
	{
		IVariableNode node = getField("var foo:int;");

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(11, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(11, node.getEndColumn() - parentNode.getColumn());

		IKeywordNode keywordNode = (IKeywordNode) node.getChild(0);
		Assert.assertEquals(ASTNodeID.KeywordVarID, keywordNode.getNodeID());
		Assert.assertEquals(0, keywordNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(3, keywordNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, keywordNode.getLine() - node.getLine());
		Assert.assertEquals(0, keywordNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, keywordNode.getEndLine() - node.getLine());
		Assert.assertEquals(3, keywordNode.getEndColumn() - node.getColumn());

		IExpressionNode nameExpressionNode = node.getNameExpressionNode();
		Assert.assertEquals(4, nameExpressionNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(7, nameExpressionNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode.getLine() - node.getLine());
		Assert.assertEquals(4, nameExpressionNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, nameExpressionNode.getEndLine() - node.getLine());
		Assert.assertEquals(7, nameExpressionNode.getEndColumn() - node.getColumn());

		IExpressionNode varTypeNode = node.getVariableTypeNode();
		Assert.assertEquals(8, varTypeNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(11, varTypeNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, varTypeNode.getLine() - node.getLine());
		Assert.assertEquals(8, varTypeNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, varTypeNode.getEndLine() - node.getLine());
		Assert.assertEquals(11, varTypeNode.getEndColumn() - node.getColumn());
	}

	@Test
	public void testField_withTypeValue()
	{
		IVariableNode node = getField("var foo:int = 123;");

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(17, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(17, node.getEndColumn() - parentNode.getColumn());

		IKeywordNode keywordNode = (IKeywordNode) node.getChild(0);
		Assert.assertEquals(ASTNodeID.KeywordVarID, keywordNode.getNodeID());
		Assert.assertEquals(0, keywordNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(3, keywordNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, keywordNode.getLine() - node.getLine());
		Assert.assertEquals(0, keywordNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, keywordNode.getEndLine() - node.getLine());
		Assert.assertEquals(3, keywordNode.getEndColumn() - node.getColumn());

		IExpressionNode nameExpressionNode = node.getNameExpressionNode();
		Assert.assertEquals(4, nameExpressionNode.getAbsoluteStart() - node.getAbsoluteStart());
		Assert.assertEquals(7, nameExpressionNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, nameExpressionNode.getLine() - node.getLine());
		Assert.assertEquals(4, nameExpressionNode.getColumn() - node.getColumn());
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
		Assert.assertEquals(17, assignedValueNode.getAbsoluteEnd() - node.getAbsoluteStart());
		Assert.assertEquals(0, assignedValueNode.getLine() - node.getLine());
		Assert.assertEquals(14, assignedValueNode.getColumn() - node.getColumn());
		Assert.assertEquals(0, assignedValueNode.getEndLine() - node.getLine());
		Assert.assertEquals(17, assignedValueNode.getEndColumn() - node.getColumn());
	}
	
}
