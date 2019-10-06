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
package org.apache.royale.compiler.internal.codegen.js.sourcemaps;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.js.royale.RoyaleBackend;
import org.apache.royale.compiler.internal.test.SourceMapTestBase;
import org.apache.royale.compiler.internal.tree.as.ArrayLiteralNode;
import org.apache.royale.compiler.internal.tree.as.ObjectLiteralNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IIterationFlowNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IReturnNode;
import org.apache.royale.compiler.tree.as.ITernaryOperatorNode;
import org.apache.royale.compiler.tree.as.IThrowNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;

import org.junit.Test;

public class TestSourceMapExpressions extends SourceMapTestBase
{
    //----------------------------------
    // Primary expression keywords
    //----------------------------------

    //----------------------------------
    // Arithmetic
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Plus()
    {
        IBinaryOperatorNode node = getBinaryNode("a + b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // +
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_Minus()
    {
        IBinaryOperatorNode node = getBinaryNode("a - b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // -
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_Divide()
    {
        IBinaryOperatorNode node = getBinaryNode("a / b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // /
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_Modulo()
    {
        IBinaryOperatorNode node = getBinaryNode("a % b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // %
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_Multiply()
    {
        IBinaryOperatorNode node = getBinaryNode("a * b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // *
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitUnaryOperatorNode_PostIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a++");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 3); // ++
    }

    @Test
    public void testVisitUnaryOperatorNode_PreIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("++a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 2); // ++
        assertMapping(node, 0, 2, 0, 2, 0, 3); // a
    }

    @Test
    public void testVisitUnaryOperatorNode_PostDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a--");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 3); // --
    }

    @Test
    public void testVisitUnaryOperatorNode_PreDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("--a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 2); // --
        assertMapping(node, 0, 2, 0, 2, 0, 3); // a
    }

    //----------------------------------
    // Arithmetic compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_PlusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a += b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // +=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_MinusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a -= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // -=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_DivideAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a /= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // /=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_ModuloAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a %= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // %=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_MultiplyAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a *= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // *=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    //----------------------------------
    // Assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Assignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a = b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // =
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentLiteral()
    {
        IBinaryOperatorNode node = getBinaryNode("a = 123.2");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // =
        assertMapping(node, 0, 4, 0, 4, 0, 9); // 123.2
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentLiteralWithCompileTimeIntCoercion()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:int;a = 123.2");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // =
        assertMapping(node, 0, 4, 0, 4, 0, 7); // 123
    }

    @Test
    public void testVisitBinaryOperatorNode_AssignmentLiteralWithCompileTimeUintCoercion()
    {
        IBinaryOperatorNode node = getBinaryNode("var a:uint;a = -123");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // =
        assertMapping(node, 0, 4, 0, 4, 0, 14); // 4294967173
    }

    //----------------------------------
    // Bitwise
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_BitwiseAnd()
    {
        IBinaryOperatorNode node = getBinaryNode("a & b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // &
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseLeftShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a << b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // <<
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitUnaryOperatorNode_BitwiseNot()
    {
        IUnaryOperatorNode node = getUnaryNode("~a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // ~
        assertMapping(node, 0, 1, 0, 1, 0, 2); // a
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseOr()
    {
        IBinaryOperatorNode node = getBinaryNode("a | b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // |
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseRightShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a >> b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // >>
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseUnsignedRightShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>> b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 6); // >>>
        assertMapping(node, 0, 6, 0, 6, 0, 7); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseXOR()
    {
        IBinaryOperatorNode node = getBinaryNode("a ^ b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // ^
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    //----------------------------------
    // Bitwise compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_BitwiseAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // &=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseLeftShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a <<= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 6); // <<=
        assertMapping(node, 0, 6, 0, 6, 0, 7); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a |= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // |=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseRightShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 6); // >>=
        assertMapping(node, 0, 6, 0, 6, 0, 7); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseUnsignedRightShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>>= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 7); // >>>=
        assertMapping(node, 0, 7, 0, 7, 0, 8); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseXORAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ^= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // ^=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    //----------------------------------
    // Comparison
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Equal()
    {
        IBinaryOperatorNode node = getBinaryNode("a == b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // ==
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_GreaterThan()
    {
        IBinaryOperatorNode node = getBinaryNode("a > b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // >
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_GreaterThanEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a >= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // >=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_NotEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a != b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // !=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_LessThan()
    {
        IBinaryOperatorNode node = getBinaryNode("a < b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // <
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_LessThanEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a <= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // <=
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_StrictEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a === b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 6); // ===
        assertMapping(node, 0, 6, 0, 6, 0, 7); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_StrictNotEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a !== b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 6); // !==
        assertMapping(node, 0, 6, 0, 6, 0, 7); // b
    }

    //----------------------------------
    // Logical
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_LogicalAnd()
    {
        IBinaryOperatorNode node = getBinaryNode("a && b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // &&
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &&= b");
        asBlockWalker.visitBinaryOperator(node);
        //a = a && b
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // =
        assertMapping(node, 0, 0, 0, 4, 0, 5); // a
        assertMapping(node, 0, 1, 0, 5, 0, 9); // &&
        assertMapping(node, 0, 0, 0, 0, 0, 1); // b
    }

    @Test
    public void testVisitUnaryOperatorNode_LogicalNot()
    {
        IUnaryOperatorNode node = getUnaryNode("!a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // !
        assertMapping(node, 0, 1, 0, 1, 0, 2); // a
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalOr()
    {
        IBinaryOperatorNode node = getBinaryNode("a || b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // ||
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ||= b");
        asBlockWalker.visitBinaryOperator(node);
        //a = a || b
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // =
        assertMapping(node, 0, 0, 0, 4, 0, 5); // a
        assertMapping(node, 0, 1, 0, 5, 0, 9); // ||
        assertMapping(node, 0, 0, 0, 0, 0, 1); // b
    }

    //----------------------------------
    // Other
    //----------------------------------

    @Test
    public void testVisitDynamicAccessNode_1()
    {
        IDynamicAccessNode node = getDynamicAccessNode("a[b]");
        asBlockWalker.visitDynamicAccess(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 2); // [
        assertMapping(node, 0, 2, 0, 2, 0, 3); // b
        assertMapping(node, 0, 3, 0, 3, 0, 4); // ]
    }

    @Test
    public void testVisitDynamicAccessNode_2()
    {
        IDynamicAccessNode node = getDynamicAccessNode("a[b[c][d]]");
        asBlockWalker.visitDynamicAccess(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1);   // a
        assertMapping(node, 0, 1, 0, 1, 0, 2);   // [
        assertMapping(node, 0, 2, 0, 2, 0, 3);   // b
        assertMapping(node, 0, 3, 0, 3, 0, 4);   // [
        assertMapping(node, 0, 4, 0, 4, 0, 5);   // c
        assertMapping(node, 0, 5, 0, 5, 0, 6);   // ]
        assertMapping(node, 0, 6, 0, 6, 0, 7);   // [
        assertMapping(node, 0, 7, 0, 7, 0, 8);   // d
        assertMapping(node, 0, 8, 0, 8, 0, 9);   // ]
        assertMapping(node, 0, 9, 0, 9, 0, 10);  // ]
    }

    @Test
    public void testVisitBinaryOperatorNode_Comma()
    {
        IBinaryOperatorNode node = getBinaryNode("a, b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 3); // ,
        assertMapping(node, 0, 3, 0, 3, 0, 4); // b
    }

    @Test
    public void testVisitTernaryOperatorNode()
    {
        ITernaryOperatorNode node = (ITernaryOperatorNode) getExpressionNode(
                "a ? b : c", ITernaryOperatorNode.class);
        asBlockWalker.visitTernaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 4); // ?
        assertMapping(node, 0, 4, 0, 4, 0, 5); // b
        assertMapping(node, 0, 5, 0, 5, 0, 8); // :
        assertMapping(node, 0, 8, 0, 8, 0, 9); // c
    }

    @Test
    public void testVisitUnaryOperator_Delete()
    {
        IUnaryOperatorNode node = getUnaryNode("delete a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 7); // delete
        assertMapping(node, 0, 7, 0, 7, 0, 8); // a
    }

    @Test
    public void testVisitMemberAccess_1()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getExpressionNode(
                "a.b", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 2); // .
        assertMapping(node, 0, 2, 0, 2, 0, 3); // b
    }

    @Test
    public void testVisitMemberAccess_2()
    {
        IMemberAccessExpressionNode node = (IMemberAccessExpressionNode) getExpressionNode(
                "a.b.c.d", IMemberAccessExpressionNode.class);
        asBlockWalker.visitMemberAccessExpression(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1);  // a
        assertMapping(node, 0, 1, 0, 1, 0, 2);  // .
        assertMapping(node, 0, 2, 0, 2, 0, 3);  // b
        assertMapping(node, 0, 3, 0, 3, 0, 4);  // .
        assertMapping(node, 0, 4, 0, 4, 0, 5);  // c
        assertMapping(node, 0, 5, 0, 5, 0, 6);  // .
        assertMapping(node, 0, 6, 0, 6, 0, 7);  // d
    }

    @Test
    public void testVisitBinaryOperator_In()
    {
        IBinaryOperatorNode node = getBinaryNode("a in b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 5); // in
        assertMapping(node, 0, 5, 0, 5, 0, 6); // b
    }

    @Test
    public void testVisitBinaryOperator_Instancof()
    {
        IBinaryOperatorNode node = getBinaryNode("a instanceof b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1);     // a
        assertMapping(node, 0, 1, 0, 1, 0, 13);    // instanceof
        assertMapping(node, 0, 13, 0, 13, 0, 14);  // b
    }

    @Test
    public void testVisitBinaryOperator_New()
    {
        IFunctionCallNode node = (IFunctionCallNode) getExpressionNode(
                "new Object()", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertMapping(node, 0, 0, 0, 0, 0, 4);  // new
        assertMapping(node, 0, 4, 0, 4, 0, 10); // Object
    }

    @Test
    public void testVisitObjectLiteral_1()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = {a:1}", ObjectLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //{a: 1}
        assertMapping(node, 0, 0, 0, 0, 0, 1); // {
        assertMapping(node, 0, 1, 0, 1, 0, 2); // a
        assertMapping(node, 0, 2, 0, 2, 0, 3); // :
        assertMapping(node, 0, 4, 0, 4, 0, 5); // }
    }

    @Test
    public void testVisitObjectLiteral_2()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = {a:1,b:{c:2,d:{e:4}}}", ObjectLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //{a:1, b:{c:2, d:{e:4}}}
        assertMapping(node, 0, 0, 0, 0, 0, 1);    // {
        assertMapping(node, 0, 1, 0, 1, 0, 2);    // a
        assertMapping(node, 0, 2, 0, 2, 0, 3);    // :
        assertMapping(node, 0, 4, 0, 4, 0, 6);    // ,
        assertMapping(node, 0, 5, 0, 6, 0, 7);    // b
        assertMapping(node, 0, 6, 0, 7, 0, 8);    // :
        assertMapping(node, 0, 7, 0, 8, 0, 9);    // {
        assertMapping(node, 0, 8, 0, 9, 0, 10);   // c
        assertMapping(node, 0, 9, 0, 10, 0, 11);  // :
        assertMapping(node, 0, 11, 0, 12, 0, 14); // ,
        assertMapping(node, 0, 12, 0, 14, 0, 15); // d
        assertMapping(node, 0, 13, 0, 15, 0, 16); // :
        assertMapping(node, 0, 14, 0, 16, 0, 17); // {
        assertMapping(node, 0, 15, 0, 17, 0, 18); // e
        assertMapping(node, 0, 16, 0, 18, 0, 19); // :
        assertMapping(node, 0, 18, 0, 20, 0, 21); // }
        assertMapping(node, 0, 19, 0, 21, 0, 22); // }
        assertMapping(node, 0, 20, 0, 22, 0, 23); // }
        
    }

    @Test
    public void testVisitObjectLiteral_3()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = { a: 12,  bb: 2   \t}", ObjectLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //{a:12, bb:2}
        assertMapping(node, 0, 0, 0, 0, 0, 1);    // {
        assertMapping(node, 0, 2, 0, 1, 0, 2);    // a
        assertMapping(node, 0, 3, 0, 2, 0, 3);    // :
        assertMapping(node, 0, 7, 0, 5, 0, 7);    // ,
        assertMapping(node, 0, 10, 0, 7, 0, 9);   // bb
        assertMapping(node, 0, 12, 0, 9, 0, 10);  // :
        assertMapping(node, 0, 19, 0, 11, 0, 12); // }
    }

    @Test
    public void testVisitObjectLiteral_4()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = {a:1,\nb:2}", ObjectLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //{a:1, b:2}
        assertMapping(node, 0, 0, 0, 0, 0, 1);   // {
        assertMapping(node, 0, 1, 0, 1, 0, 2);   // a
        assertMapping(node, 0, 2, 0, 2, 0, 3);   // :
        assertMapping(node, 0, 3, 0, 3, 0, 4);   // 1
        assertMapping(node, 0, 4, 0, 4, 0, 6);   // ,
        assertMapping(node, 1, 0, 0, 6, 0, 7);   // b
        assertMapping(node, 1, 1, 0, 7, 0, 8);   // :
        assertMapping(node, 1, 2, 0, 8, 0, 9);   // 2
        assertMapping(node, 1, 3, 0, 9, 0, 10);  // }
    }

    @Test
    public void testVisitArrayLiteral_1()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [0,1,2]", ArrayLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //[0, 1, 2]
        assertMapping(node, 0, 0, 0, 0, 0, 1); // [
        assertMapping(node, 0, 1, 0, 1, 0, 2); // 0
        assertMapping(node, 0, 2, 0, 2, 0, 4); // ,
        assertMapping(node, 0, 3, 0, 4, 0, 5); // 1
        assertMapping(node, 0, 4, 0, 5, 0, 7); // ,
        assertMapping(node, 0, 5, 0, 7, 0, 8); // 2
        assertMapping(node, 0, 6, 0, 8, 0, 9); // ]
    }

    @Test
    public void testVisitArrayLiteral_2()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [0,[0,1,[0,1]],2,[1,2]]", ArrayLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //[0, [0, 1, [0, 1]], 2, [1, 2]]
        assertMapping(node, 0, 0, 0, 0, 0, 1);    // [
        assertMapping(node, 0, 2, 0, 2, 0, 4);    // ,
        assertMapping(node, 0, 3, 0, 4, 0, 5);    // [
        assertMapping(node, 0, 5, 0, 6, 0, 8);    // ,
        assertMapping(node, 0, 7, 0, 9, 0, 11);   // ,
        assertMapping(node, 0, 8, 0, 11, 0, 12);  // [
        assertMapping(node, 0, 10, 0, 13, 0, 15); // ,
        assertMapping(node, 0, 12, 0, 16, 0, 17); // ]
        assertMapping(node, 0, 13, 0, 17, 0, 18); // ]
        
        assertMapping(node, 0, 14, 0, 18, 0, 20); // ,
        assertMapping(node, 0, 16, 0, 21, 0, 23); // ,
        assertMapping(node, 0, 17, 0, 23, 0, 24); // [
        assertMapping(node, 0, 19, 0, 25, 0, 27); // ,
        assertMapping(node, 0, 21, 0, 28, 0, 29); // ]
        assertMapping(node, 0, 22, 0, 29, 0, 30); // ]
    }

    @Test
    public void testVisitArrayLiteral_3()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [ 0,  123, 45   \t]", ArrayLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //[0, 123, 45]
        assertMapping(node, 0, 0, 0, 0, 0, 1);    // [
        assertMapping(node, 0, 3, 0, 2, 0, 4);    // ,
        assertMapping(node, 0, 9, 0, 7, 0, 9);    // ,
        assertMapping(node, 0, 17, 0, 11, 0, 12); // ]
    }

    @Test
    public void testVisitArrayLiteral_4()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [0,\n123, 45]", ArrayLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //[0, 123, 45]
        assertMapping(node, 0, 0, 0, 0, 0, 1);    // [
        assertMapping(node, 0, 1, 0, 1, 0, 2);    // 0
        assertMapping(node, 0, 2, 0, 2, 0, 4);    // ,
        assertMapping(node, 1, 0, 0, 4, 0, 7);    // 123
        assertMapping(node, 1, 3, 0, 7, 0, 9);    // ,
        assertMapping(node, 1, 5, 0, 9, 0, 11);   // 45
        assertMapping(node, 1, 7, 0, 11, 0, 12);  // 45
    }

    @Test
    public void testVisitUnaryOperatorNode_Typeof()
    {
        IUnaryOperatorNode node = getUnaryNode("typeof(a)");
        asBlockWalker.visitUnaryOperator(node);
        //typeof(a)
        assertMapping(node, 0, 0, 0, 0, 0, 7); // typeof(
        assertMapping(node, 0, 0, 0, 8, 0, 9); // )
    }

    @Test
    public void testVisitUnaryOperatorNode_Typeof_NoParens()
    {
        // TODO (mschmalle) the notation without parenthesis is also valid in AS/JS
        IUnaryOperatorNode node = getUnaryNode("typeof a");
        asBlockWalker.visitUnaryOperator(node);
        //typeof(a)
        assertMapping(node, 0, 0, 0, 0, 0, 7); // typeof(
        assertMapping(node, 0, 0, 0, 8, 0, 9); // )
    }

    @Test
    public void testVisitUnaryOperatorNode_Void()
    {
        IUnaryOperatorNode node = getUnaryNode("void a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 5); // void
        assertMapping(node, 0, 5, 0, 5, 0, 6); // a
    }

    @Test
    public void testVisitIterationFlowNode_BreakWithoutLabel()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("break",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 5); // break
    }

    @Test
    public void testVisitIterationFlowNode_BreakWithLabel()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("break label",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 6);  // break
        assertMapping(node, 0, 6, 0, 6, 0, 11); // label
    }

    @Test
    public void testVisitIterationFlowNode_ContinueWithoutLabel()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("continue",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 8); // continue
    }

    @Test
    public void testVisitIterationFlowNode_ContinueWithLabel()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("continue label",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 9); // continue
        assertMapping(node, 0, 9, 0, 9, 0, 14); // label
    }

    @Test
    public void testVisitReturnWithoutValue()
    {
        IReturnNode node = (IReturnNode) getNode("return", IReturnNode.class);
        asBlockWalker.visitReturn(node);
        assertMapping(node, 0, 0, 0, 0, 0, 6); // return
    }

    @Test
    public void testVisitReturnWithValue()
    {
        IReturnNode node = (IReturnNode) getNode("return 0", IReturnNode.class);
        asBlockWalker.visitReturn(node);
        assertMapping(node, 0, 0, 0, 0, 0, 7); // return
        assertMapping(node, 0, 7, 0, 7, 0, 8); // 0
    }

    @Test
    public void testThrow()
    {
        IThrowNode node = (IThrowNode) getNode("throw a", IThrowNode.class);
        asBlockWalker.visitThrow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 6); // throw
        assertMapping(node, 0, 6, 0, 6, 0, 7); // a
    }

    @Test
    public void testVisitFunctionCall_1()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("a()", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 2); // (
        assertMapping(node, 0, 2, 0, 2, 0, 3); // )
    }

    @Test
    public void testVisitFunctionCall_2()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("a(b)", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 2); // (
        assertMapping(node, 0, 2, 0, 2, 0, 3); // b
        assertMapping(node, 0, 3, 0, 3, 0, 4); // )
    }

    @Test
    public void testVisitFunctionCall_3()
    {
        IFunctionCallNode node = (IFunctionCallNode) getNode("a(b, c)", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1); // a
        assertMapping(node, 0, 1, 0, 1, 0, 2); // (
        assertMapping(node, 0, 2, 0, 2, 0, 3); // b
        assertMapping(node, 0, 3, 0, 3, 0, 5); // ,
        assertMapping(node, 0, 5, 0, 5, 0, 6); // c
        assertMapping(node, 0, 6, 0, 6, 0, 7); // )
    }

    protected IBackend createBackend()
    {
        return new RoyaleBackend();
    }
}
