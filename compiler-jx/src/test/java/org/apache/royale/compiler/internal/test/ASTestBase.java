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
package org.apache.royale.compiler.internal.test;

import java.io.File;
import java.util.List;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.driver.as.ASBackend;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IAccessorNode;
import org.apache.royale.compiler.tree.as.IBinaryOperatorNode;
import org.apache.royale.compiler.tree.as.IDynamicAccessNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IForLoopNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.INamespaceAccessExpressionNode;
import org.apache.royale.compiler.tree.as.IUnaryOperatorNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.Ignore;

@Ignore
public class ASTestBase extends TestBase
{

    protected ITestAdapter testAdapter;

    @Override
    public void setUp()
    {
        super.setUp();

        testAdapter = TestAdapterFactory.getTestAdapter();

        asEmitter = backend.createEmitter(writer);
        asBlockWalker = backend.createWalker(project, errors, asEmitter);
    }

    @Override
    protected void addDependencies()
    {
        if (libraries.size() == 0)
        {
        	String jsSwcPath = FilenameNormalization.normalize("../compiler-externc/target/js.swc");
    		libraries.add(new File(jsSwcPath));
        	String customSwcPath = FilenameNormalization.normalize("../compiler-jx/target/custom.swc");
    		libraries.add(new File(customSwcPath));
        }
        super.addDependencies();
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        //sourcePaths.add(new File(FilenameNormalization.normalize(
        //         "../../../externs/GCL/src/main/flex")));
        
        super.addSourcePaths(sourcePaths);
    }
    
    @Override
    protected void addLibraries(List<File> libraries)
    {
        //libraries.addAll(testAdapter.getLibraries(false));

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
    protected static final int WRAP_LEVEL_NONE = 0;

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
            code = "function royaleTest_a():void {" + code + "}";

        if (wrapLevel >= WRAP_LEVEL_CLASS)
            code = "public class RoyaleTest_A {" + code + "}";

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

    protected IForLoopNode getForLoopNode(String code)
    {
        return (IForLoopNode) getNode(code, IForLoopNode.class);
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

    protected IUnaryOperatorNode getUnaryNode(String code, int wrapLevel)
    {
        return (IUnaryOperatorNode) getNode(code, IUnaryOperatorNode.class, wrapLevel);
    }

    protected IVariableNode getVariable(String code)
    {
        return (IVariableNode) getNode(code, IVariableNode.class);
    }

    protected IASNode getLocalFunction(String code)
    {
        IFunctionNode method = (IFunctionNode) getNode(code, IFunctionNode.class);
        return (IFunctionNode) findFirstDescendantOfType(method, IFunctionNode.class);
    }

}
