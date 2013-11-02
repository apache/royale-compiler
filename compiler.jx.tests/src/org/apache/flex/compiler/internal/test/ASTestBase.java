package org.apache.flex.compiler.internal.test;

import java.io.File;
import java.util.List;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.as.ASBackend;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IDynamicAccessNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IUnaryOperatorNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

@Ignore
public class ASTestBase extends TestBase
{

    @Override
    public void setUp()
    {
        super.setUp();

        asEmitter = backend.createEmitter(writer);
        asBlockWalker = backend.createWalker(project, errors, asEmitter);
    }

    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/11.1/playerglobal.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/framework.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/spark.swc")));

        super.addLibraries(libraries);
    }

    @Override
    protected IBackend createBackend()
    {
        return new ASBackend();
    }

    //--------------------------------------------------------------------------
    // Node "factory"
    //--------------------------------------------------------------------------

    protected static final int WRAP_LEVEL_MEMBER = 3;
    protected static final int WRAP_LEVEL_CLASS = 2;
    protected static final int WRAP_LEVEL_PACKAGE = 1;

    protected IASNode getNode(String code, Class<? extends IASNode> type)
    {
        return getNode(code, type, WRAP_LEVEL_MEMBER, false);
    }

    protected IASNode getNode(String code, Class<? extends IASNode> type,
            int wrapLevel)
    {
        return getNode(code, type, wrapLevel, false);
    }

    protected IASNode getNode(String code, Class<? extends IASNode> type,
            int wrapLevel, boolean includePackage)
    {
        if (wrapLevel == WRAP_LEVEL_MEMBER)
            code = "function falconTest_a():void {" + code + "}";

        if (wrapLevel >= WRAP_LEVEL_CLASS)
            code = "public class FalconTest_A {" + code + "}";

        if (wrapLevel >= WRAP_LEVEL_PACKAGE)
            code = "package" + ((includePackage) ? " foo.bar" : "") + " {"
                    + code + "}";

        IFileNode node = compileAS(code);

        if (type.isInstance(node))
            return node;

        return findFirstDescendantOfType(node, type);
    }

    protected IInterfaceNode getInterfaceNode(String code)
    {
        return (IInterfaceNode) getNode(code, IInterfaceNode.class,
                WRAP_LEVEL_PACKAGE);
    }

    protected IAccessorNode getAccessor(String code)
    {
        return (IAccessorNode) getNode(code, IAccessorNode.class,
                WRAP_LEVEL_CLASS);
    }

    protected IVariableNode getField(String code)
    {
        return (IVariableNode) getNode(code, IVariableNode.class,
                WRAP_LEVEL_CLASS);
    }

    protected IFunctionNode getMethod(String code)
    {
        return (IFunctionNode) getNode(code, IFunctionNode.class,
                WRAP_LEVEL_CLASS);
    }

    protected IFunctionNode getMethodWithPackage(String code)
    {
        return (IFunctionNode) getNode(code, IFunctionNode.class,
                WRAP_LEVEL_CLASS, true);
    }

    protected IExpressionNode getExpressionNode(String code,
            Class<? extends IASNode> type)
    {
        return (IExpressionNode) getNode(code, type);
    }

    protected IBinaryOperatorNode getBinaryNode(String code)
    {
        return (IBinaryOperatorNode) getNode(code, IBinaryOperatorNode.class);
    }

    protected INamespaceAccessExpressionNode getNamespaceAccessExpressionNode(
            String code)
    {
        return (INamespaceAccessExpressionNode) getNode(code,
                INamespaceAccessExpressionNode.class);
    }

    protected IDynamicAccessNode getDynamicAccessNode(String code)
    {
        return (IDynamicAccessNode) getNode(code, IDynamicAccessNode.class);
    }

    protected IUnaryOperatorNode getUnaryNode(String code)
    {
        return (IUnaryOperatorNode) getNode(code, IUnaryOperatorNode.class);
    }

    protected IVariableNode getVariable(String code)
    {
        return (IVariableNode) getNode(code, IVariableNode.class);
    }

}
