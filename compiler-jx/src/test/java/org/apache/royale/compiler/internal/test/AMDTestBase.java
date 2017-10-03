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
import org.apache.royale.compiler.internal.driver.js.amd.AMDBackend;
import org.apache.royale.compiler.tree.as.IClassNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.IInterfaceNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.TestAdapterFactory;

/**
 * This class tests the production of AMD JavaScript for AS package.
 * 
 * @author Michael Schmalle
 */
public abstract class AMDTestBase extends TestBase
{
    protected IFileNode fileNode;

    protected IClassNode classNode;

    protected IInterfaceNode interfaceNode;

    private String projectPath;

    @Override
    public void setUp()
    {
        super.setUp();

        asEmitter = backend.createEmitter(writer);
        asBlockWalker = backend.createWalker(project, errors, asEmitter);

        projectPath = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                "amd/simple-project/src").getPath();

        String target = getTypeUnderTest().replace(".", File.separator);
        String targetDir = projectPath + File.separator
                + target.substring(0, target.lastIndexOf(File.separator));
        String targetFile = target.substring(
                target.lastIndexOf(File.separator) + 1, target.length());

        fileNode = compileAS(targetFile, true, targetDir, false);
        ITypeNode type = (ITypeNode) findFirstDescendantOfType(fileNode,
                ITypeNode.class);
        if (type instanceof IClassNode)
            classNode = (IClassNode) type;
        else if (type instanceof IInterfaceNode)
            interfaceNode = (IInterfaceNode) type;

    }

    abstract protected String getTypeUnderTest();

    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/" + env.FPVER + "/playerglobal.swc")));

        super.addLibraries(libraries);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(FilenameNormalization.normalize(projectPath)));

        super.addSourcePaths(sourcePaths);
    }

    @Override
    protected IBackend createBackend()
    {
        return new AMDBackend();
    }

    protected IVariableNode findField(String name, IClassNode node)
    {
        IDefinitionNode[] nodes = node.getAllMemberNodes();
        for (IDefinitionNode inode : nodes)
        {
            if (inode.getName().equals(name))
                return (IVariableNode) inode;
        }
        return null;
    }

    protected IFunctionNode findFunction(String name, IClassNode node)
    {
        IDefinitionNode[] nodes = node.getAllMemberNodes();
        for (IDefinitionNode inode : nodes)
        {
            if (inode.getName().equals(name))
                return (IFunctionNode) inode;
        }
        return null;
    }

    protected IGetterNode findGetter(String name, IClassNode node)
    {
        IDefinitionNode[] nodes = node.getAllMemberNodes();
        for (IDefinitionNode inode : nodes)
        {
            if (inode.getName().equals(name) && inode instanceof IGetterNode)
                return (IGetterNode) inode;
        }
        return null;
    }

    protected ISetterNode findSetter(String name, IClassNode node)
    {
        IDefinitionNode[] nodes = node.getAllMemberNodes();
        for (IDefinitionNode inode : nodes)
        {
            if (inode.getName().equals(name) && inode instanceof ISetterNode)
                return (ISetterNode) inode;
        }
        return null;
    }
}
