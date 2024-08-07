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

import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.ILiteralNode;
import org.apache.royale.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.royale.compiler.tree.as.INumericLiteralNode;
import org.junit.Assert;
import org.junit.Test;

public class ASLiteralsTests extends ASTestBase
{
	//--------------------------------------------------------------------------
	// Number
	//--------------------------------------------------------------------------

	@Test
	public void testNumber()
	{
		ILiteralNode node = (ILiteralNode) getNode("123.45;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(123.45, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(6, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(6, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_int()
	{
		ILiteralNode node = (ILiteralNode) getNode("678;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.INT, numericValue.getAssumedType());
		Assert.assertEquals(678.0, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(3, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(3, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_uint()
	{
		ILiteralNode node = (ILiteralNode) getNode("2147483648;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.UINT, numericValue.getAssumedType());
		Assert.assertEquals(2147483648.0, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(10, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(10, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_fractional_leadingZero()
	{
		ILiteralNode node = (ILiteralNode) getNode("0.123;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(0.123, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(5, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(5, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_fractional_skipLeadingZero()
	{
		ILiteralNode node = (ILiteralNode) getNode(".123;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(0.123, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(4, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(4, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_exponent()
	{
		ILiteralNode node = (ILiteralNode) getNode("123.4e5;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(123.4e5, numericValue.toInteger(), 0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(7, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(7, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_hex()
	{
		ILiteralNode node = (ILiteralNode) getNode("0xa0b1c2;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.INT, numericValue.getAssumedType());
		Assert.assertEquals(0xa0b1c2, numericValue.toInteger(), 0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(8, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(8, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_negativeZero()
	{
		ILiteralNode node = (ILiteralNode) getNode("-0;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(0.0, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(2, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(2, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_negative()
	{
		ILiteralNode node = (ILiteralNode) getNode("-123.45;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(-123.45, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(7, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(7, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_negative_int()
	{
		ILiteralNode node = (ILiteralNode) getNode("-123;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.INT, numericValue.getAssumedType());
		Assert.assertEquals(-123.0, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(4, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(4, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_negative_fractional_leadingZero()
	{
		ILiteralNode node = (ILiteralNode) getNode("-0.123;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(-0.123, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(6, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(6, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_negative_fractional_skipLeadingZero()
	{
		ILiteralNode node = (ILiteralNode) getNode("-.123;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(-0.123, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(5, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(5, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_negative_exponent()
	{
		ILiteralNode node = (ILiteralNode) getNode("-123.4e5;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(-123.4e5, numericValue.toInteger(), 0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(8, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(8, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_negative_hex()
	{
		ILiteralNode node = (ILiteralNode) getNode("-0xa0b1c2;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());
		Assert.assertEquals("-0xa0b1c2", node.getValue());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.INT, numericValue.getAssumedType());
		Assert.assertEquals(-0xa0b1c2, numericValue.toInteger(), 0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(9, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(9, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_positive()
	{
		ILiteralNode node = (ILiteralNode) getNode("+123.45;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(123.45, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(7, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(7, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_positive_int()
	{
		ILiteralNode node = (ILiteralNode) getNode("+123;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.INT, numericValue.getAssumedType());
		Assert.assertEquals(123.0, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(4, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(4, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_positive_uint()
	{
		ILiteralNode node = (ILiteralNode) getNode("+2147483648;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.UINT, numericValue.getAssumedType());
		Assert.assertEquals(2147483648.0, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(11, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(11, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_positive_fractional_leadingZero()
	{
		ILiteralNode node = (ILiteralNode) getNode("+0.123;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(0.123, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(6, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(6, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_positive_fractional_skipLeadingZero()
	{
		ILiteralNode node = (ILiteralNode) getNode("+.123;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(0.123, numericValue.toNumber(), 0.0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(5, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(5, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_positive_exponent()
	{
		ILiteralNode node = (ILiteralNode) getNode("+123.4e5;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.NUMBER, numericValue.getAssumedType());
		Assert.assertEquals(123.4e5, numericValue.toInteger(), 0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(8, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(8, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testNumber_positive_hex()
	{
		ILiteralNode node = (ILiteralNode) getNode("+0xa0b1c2;", ILiteralNode.class);
		Assert.assertEquals(LiteralType.NUMBER, node.getLiteralType());

		INumericLiteralNode numericNode = (INumericLiteralNode) node;
		INumericLiteralNode.INumericValue numericValue = numericNode.getNumericValue();
		Assert.assertNotNull(numericValue);
		Assert.assertEquals(BuiltinType.INT, numericValue.getAssumedType());
		Assert.assertEquals(0xa0b1c2, numericValue.toInteger(), 0);

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(9, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(9, node.getEndColumn() - parentNode.getColumn());
	}

	//--------------------------------------------------------------------------
	// Boolean
	//--------------------------------------------------------------------------

	@Test
	public void testBoolean_true()
	{
		ILiteralNode node = (ILiteralNode) getNode("true;", ILiteralNode.class);

		Assert.assertEquals(LiteralType.BOOLEAN, node.getLiteralType());
		Assert.assertEquals("true", node.getValue());

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(4, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(4, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testBoolean_false()
	{
		ILiteralNode node = (ILiteralNode) getNode("false;", ILiteralNode.class);

		Assert.assertEquals(LiteralType.BOOLEAN, node.getLiteralType());
		Assert.assertEquals("false", node.getValue());

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(5, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(5, node.getEndColumn() - parentNode.getColumn());
	}

	//--------------------------------------------------------------------------
	// String
	//--------------------------------------------------------------------------

	@Test
	public void testStringDouble()
	{
		ILiteralNode node = (ILiteralNode) getNode("\"hello\";", ILiteralNode.class);

		Assert.assertEquals(LiteralType.STRING, node.getLiteralType());
		Assert.assertEquals("hello", node.getValue());

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(7, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(7, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testStringDouble_empty()
	{
		ILiteralNode node = (ILiteralNode) getNode("\"\";", ILiteralNode.class);

		Assert.assertEquals(LiteralType.STRING, node.getLiteralType());
		Assert.assertEquals("", node.getValue());

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(2, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(2, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testStringSingle()
	{
		ILiteralNode node = (ILiteralNode) getNode("'hello';", ILiteralNode.class);

		Assert.assertEquals(LiteralType.STRING, node.getLiteralType());
		Assert.assertEquals("hello", node.getValue());

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(7, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(7, node.getEndColumn() - parentNode.getColumn());
	}

	@Test
	public void testStringSingle_empty()
	{
		ILiteralNode node = (ILiteralNode) getNode("'';", ILiteralNode.class);

		Assert.assertEquals(LiteralType.STRING, node.getLiteralType());
		Assert.assertEquals("", node.getValue());

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(2, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(2, node.getEndColumn() - parentNode.getColumn());
	}

	//--------------------------------------------------------------------------
	// Other
	//--------------------------------------------------------------------------

	@Test
	public void testNull()
	{
		ILiteralNode node = (ILiteralNode) getNode("null;", ILiteralNode.class);

		Assert.assertEquals(LiteralType.NULL, node.getLiteralType());
		Assert.assertEquals("null", node.getValue());

		IASNode parentNode = node.getParent();
		Assert.assertEquals(0, node.getAbsoluteStart() - parentNode.getAbsoluteStart());
		Assert.assertEquals(4, node.getAbsoluteEnd() - parentNode.getAbsoluteStart());
		Assert.assertEquals(0, node.getLine() - parentNode.getLine());
		Assert.assertEquals(0, node.getColumn() - parentNode.getColumn());
		Assert.assertEquals(0, node.getEndLine() - parentNode.getLine());
		Assert.assertEquals(4, node.getEndColumn() - parentNode.getColumn());
	}
}
