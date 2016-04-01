package org.apache.flex.compiler.internal.codegen.js.sourcemaps;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.js.flexjs.FlexJSBackend;
import org.apache.flex.compiler.internal.test.SourceMapTestBase;
import org.apache.flex.compiler.tree.as.IVariableNode;

import org.junit.Test;

public class TestSourceMapStatements extends SourceMapTestBase
{
    //----------------------------------
    // var declaration
    //----------------------------------

    @Test
    public void testVarDeclaration()
    {
        IVariableNode node = (IVariableNode) getNode("var a;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {*} */ a
        assertMapping(node, 0, 0, 0, 0, 0, 4);
        assertMapping(node, 0, 5, 0, 4, 0, 21);
    }

    @Test
    public void testVarDeclaration_withType()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a
        assertMapping(node, 0, 0, 0, 0, 0, 4);
        assertMapping(node, 0, 5, 0, 4, 0, 26);
    }

    @Test
    public void testVarDeclaration_withTypeAssignedValue()
    {
        IVariableNode node = (IVariableNode) getNode("var a:int = 42;",
                IVariableNode.class);
        asBlockWalker.visitVariable(node);
        //var /** @type {number} */ a = 42
        assertMapping(node, 0, 0, 0, 0, 0, 4);
        assertMapping(node, 0, 5, 0, 4, 0, 26);
        assertMapping(node, 0, 9, 0, 27, 0, 30);
    }

    protected IBackend createBackend()
    {
        return new FlexJSBackend();
    }
}
