package org.apache.flex.compiler.internal.codegen.js.sourcemaps;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.test.SourceMapTestBase;
import org.apache.flex.compiler.internal.tree.as.ArrayLiteralNode;
import org.apache.flex.compiler.internal.tree.as.ObjectLiteralNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IIterationFlowNode;
import org.apache.flex.compiler.tree.as.IReturnNode;
import org.apache.flex.compiler.tree.as.ITernaryOperatorNode;
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

    //----------------------------------
    // Other
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Comma()
    {
        IBinaryOperatorNode node = getBinaryNode("a, b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 3);
    }

    @Test
    public void testVisitTernaryOperatorNode()
    {
        ITernaryOperatorNode node = (ITernaryOperatorNode) getExpressionNode(
                "a ? b : c", ITernaryOperatorNode.class);
        asBlockWalker.visitTernaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 4);
        assertMapping(node, 0, 5, 0, 5, 0, 8);
    }

    @Test
    public void testVisitUnaryOperator_Delete()
    {
        IUnaryOperatorNode node = getUnaryNode("delete a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 7);
    }

    @Test
    public void testVisitBinaryOperator_In()
    {
        IBinaryOperatorNode node = getBinaryNode("a in b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 5);
    }

    @Test
    public void testVisitBinaryOperator_Instancof()
    {
        IBinaryOperatorNode node = getBinaryNode("a instanceof b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node, 0, 1, 0, 1, 0, 13);
    }

    @Test
    public void testVisitBinaryOperator_New()
    {
        IFunctionCallNode node = (IFunctionCallNode) getExpressionNode(
                "new Object()", IFunctionCallNode.class);
        asBlockWalker.visitFunctionCall(node);
        assertMapping(node, 0, 0, 0, 0, 0, 4);
    }

    @Test
    public void testVisitObjectLiteral_1()
    {
        ObjectLiteralNode node = (ObjectLiteralNode) getExpressionNode(
                "a = {a:1}", ObjectLiteralNode.class);
        asBlockWalker.visitLiteral(node);
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
    public void testVisitArrayLiteral_1()
    {
        ArrayLiteralNode node = (ArrayLiteralNode) getExpressionNode(
                "a = [0,1,2]", ArrayLiteralNode.class);
        asBlockWalker.visitLiteral(node);
        //[0, 1, 2]
        assertMapping(node, 0, 0, 0, 0, 0, 1); // [
        assertMapping(node, 0, 2, 0, 2, 0, 4); // ,
        assertMapping(node, 0, 4, 0, 5, 0, 7); // ,
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
    public void testVisitUnaryOperatorNode_Typeof()
    {
        IUnaryOperatorNode node = getUnaryNode("typeof(a)");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 7);
        assertMapping(node, 0, 0, 0, 8, 0, 9);
    }

    @Test
    public void testVisitUnaryOperatorNode_Typeof_NoParens()
    {
        // TODO (mschmalle) the notation without parenthesis is also valid in AS/JS
        IUnaryOperatorNode node = getUnaryNode("typeof a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 7);
        assertMapping(node, 0, 0, 0, 8, 0, 9);
    }

    @Test
    public void testVisitUnaryOperatorNode_Void()
    {
        IUnaryOperatorNode node = getUnaryNode("void a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node, 0, 0, 0, 0, 0, 5);
    }

    @Test
    public void testVisitIterationFlowNode_BreakWithoutLabel()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("break",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 5);
    }

    @Test
    public void testVisitIterationFlowNode_BreakWithLabel()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("break label",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 6);
    }

    @Test
    public void testVisitIterationFlowNode_ContinueWithoutLabel()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("continue",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 8);
    }

    @Test
    public void testVisitIterationFlowNode_ContinueWithLabel()
    {
        IIterationFlowNode node = (IIterationFlowNode) getNode("continue label",
                IIterationFlowNode.class);
        asBlockWalker.visitIterationFlow(node);
        assertMapping(node, 0, 0, 0, 0, 0, 9);
    }

    @Test
    public void testVisitReturnWithoutValue()
    {
        IReturnNode node = (IReturnNode) getNode("return", IReturnNode.class);
        asBlockWalker.visitReturn(node);
        assertMapping(node, 0, 0, 0, 0, 0, 6);
    }

    @Test
    public void testVisitReturnWithValue()
    {
        IReturnNode node = (IReturnNode) getNode("return 0", IReturnNode.class);
        asBlockWalker.visitReturn(node);
        assertMapping(node, 0, 0, 0, 0, 0, 7);
    }

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
