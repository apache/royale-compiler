package org.apache.royale.compiler.internal.codegen.wast;

import org.apache.royale.compiler.internal.test.WASTTestBase;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.junit.Test;

public class TestWASTMethodMember extends WASTTestBase {

    @Test
    public void testMethod()
    {
        IFunctionNode node = getMethod("function foo(){}");
        asBlockWalker.visitFunction(node);
        assertOut("");
    }

}
