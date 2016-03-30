package org.apache.flex.compiler.internal.codegen.js.sourcemaps;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.test.SourceMapTestBase;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;

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
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitBinaryOperatorNode_Minus()
    {
        IBinaryOperatorNode node = getBinaryNode("a - b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitBinaryOperatorNode_Divide()
    {
        IBinaryOperatorNode node = getBinaryNode("a / b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitBinaryOperatorNode_Modulo()
    {
        IBinaryOperatorNode node = getBinaryNode("a % b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitBinaryOperatorNode_Multiply()
    {
        IBinaryOperatorNode node = getBinaryNode("a * b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitUnaryOperatorNode_PostIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a++");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 3);
    }

    @Test
    public void testVisitUnaryOperatorNode_PreIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("++a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 2);
    }

    @Test
    public void testVisitUnaryOperatorNode_PostDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a--");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 3);
    }

    @Test
    public void testVisitUnaryOperatorNode_PreDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("--a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 2);
    }

    //----------------------------------
    // Arithmetic compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_PlusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a += b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_MinusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a -= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_DivideAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a /= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_ModuloAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a %= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_MultiplyAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a *= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    //----------------------------------
    // Assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Assignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a = b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    //----------------------------------
    // Bitwise
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_BitwiseAnd()
    {
        IBinaryOperatorNode node = getBinaryNode("a & b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseLeftShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a << b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitUnaryOperatorNode_BitwiseNot()
    {
        IUnaryOperatorNode node = getUnaryNode("~a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseOr()
    {
        IBinaryOperatorNode node = getBinaryNode("a | b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseRightShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a >> b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseUnsignedRightShift()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>> b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 6);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseXOR()
    {
        IBinaryOperatorNode node = getBinaryNode("a ^ b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    //----------------------------------
    // Bitwise compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_BitwiseAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseLeftShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a <<= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 6);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a |= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseRightShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 6);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseUnsignedRightShiftAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a >>>= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 7);
    }

    @Test
    public void testVisitBinaryOperatorNode_BitwiseXORAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ^= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    //----------------------------------
    // Comparison
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Equal()
    {
        IBinaryOperatorNode node = getBinaryNode("a == b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_GreaterThan()
    {
        IBinaryOperatorNode node = getBinaryNode("a > b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitBinaryOperatorNode_GreaterThanEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a >= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_NotEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a != b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_LessThan()
    {
        IBinaryOperatorNode node = getBinaryNode("a < b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
    }

    @Test
    public void testVisitBinaryOperatorNode_LessThanEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a <= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_StrictEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a === b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 6);
    }

    @Test
    public void testVisitBinaryOperatorNode_StrictNotEqual()
    {
        IBinaryOperatorNode node = getBinaryNode("a !== b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 6);
    }

    //----------------------------------
    // Logical
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_LogicalAnd()
    {
        IBinaryOperatorNode node = getBinaryNode("a && b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalAndAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a &&= b");
        asBlockWalker.visitBinaryOperator(node);
        //a = a && b
        assertMapping(node, 0, 1, 0, 1, 0, 4);
        assertMapping(node, 0, 1, 0, 5, 0, 9);
    }

    @Test
    public void testVisitUnaryOperatorNode_LogicalNot()
    {
        IUnaryOperatorNode node = getUnaryNode("!a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 1);
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalOr()
    {
        IBinaryOperatorNode node = getBinaryNode("a || b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperatorNode_LogicalOrAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a ||= b");
        asBlockWalker.visitBinaryOperator(node);
        //a = a || b
        assertMapping(node, 0, 1, 0, 1, 0, 4);
        assertMapping(node, 0, 1, 0, 5, 0, 9);
    }

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
