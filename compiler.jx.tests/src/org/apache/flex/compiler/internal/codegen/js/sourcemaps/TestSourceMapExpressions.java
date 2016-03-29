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
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitBinaryOperatorNode_Minus()
    {
        IBinaryOperatorNode node = getBinaryNode("a - b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitBinaryOperatorNode_Divide()
    {
        IBinaryOperatorNode node = getBinaryNode("a / b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitBinaryOperatorNode_Modulo()
    {
        IBinaryOperatorNode node = getBinaryNode("a % b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitBinaryOperatorNode_Multiply()
    {
        IBinaryOperatorNode node = getBinaryNode("a * b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitUnaryOperatorNode_PostIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a++");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getOperandNode().getEnd() - node.getOperandNode().getStart());
    }

    @Test
    public void testVisitUnaryOperatorNode_PreIncrement()
    {
        IUnaryOperatorNode node = getUnaryNode("++a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node.getLine(), node.getColumn());
    }

    @Test
    public void testVisitUnaryOperatorNode_PostDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("a--");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getOperandNode().getEnd() - node.getOperandNode().getStart());
    }

    @Test
    public void testVisitUnaryOperatorNode_PreDecrement()
    {
        IUnaryOperatorNode node = getUnaryNode("--a");
        asBlockWalker.visitUnaryOperator(node);
        assertMapping(node.getLine(), node.getColumn());
    }

    //----------------------------------
    // Arithmetic compound assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_PlusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a += b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitBinaryOperatorNode_MinusAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a -= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitBinaryOperatorNode_DivideAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a /= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitBinaryOperatorNode_ModuloAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a %= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    @Test
    public void testVisitBinaryOperatorNode_MultiplyAssignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a *= b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    //----------------------------------
    // Assignment
    //----------------------------------

    @Test
    public void testVisitBinaryOperatorNode_Assignment()
    {
        IBinaryOperatorNode node = getBinaryNode("a = b");
        asBlockWalker.visitBinaryOperator(node);
        assertMapping(node.getLine(), node.getColumn() + node.getLeftOperandNode().getEnd() - node.getLeftOperandNode().getStart());
    }

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
