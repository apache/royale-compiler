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

package org.apache.flex.compiler.internal.js.codegen.amd;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.clients.IBackend;
import org.apache.flex.compiler.internal.as.codegen.TestWalkerBase;
import org.apache.flex.compiler.internal.js.driver.amd.AMDBackend;
import org.apache.flex.compiler.internal.units.SourceCompilationUnitFactory;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IInterfaceNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypeNode;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.utils.EnvProperties;
import org.apache.flex.utils.FilenameNormalization;

/**
 * This class tests the production of AMD JavaScript for AS package.
 * 
 * @author Michael Schmalle
 */
public abstract class AMDTestProjectBase extends TestWalkerBase
{
    private static EnvProperties env = EnvProperties.initiate();

    protected IFileNode fileNode;

    protected IClassNode classNode;

    protected IInterfaceNode interfaceNode;

    @Override
    public void setUp()
    {
        super.setUp();

        String qualifiedName = getTypeUnderTest();
        String target = qualifiedName.replace(".", File.separator);

        target = FilenameNormalization
                .normalize("test-files/amd/simple-project/src"
                        + File.separator + target + ".as");

        fileNode = compile(target);
        ITypeNode type = (ITypeNode) findFirstDescendantOfType(fileNode,
                ITypeNode.class);
        if (type instanceof IClassNode)
            classNode = (IClassNode) type;
        else if (type instanceof IInterfaceNode)
            interfaceNode = (IInterfaceNode) type;

    }

    abstract protected String getTypeUnderTest();

    protected IFileNode compile(String main)
    {
        List<File> sourcePaths = new ArrayList<File>();
        addSourcePaths(sourcePaths);
        project.setSourcePath(sourcePaths);

        List<File> libraries = new ArrayList<File>();
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "\\11.1\\playerglobal.swc")));

        addLibrary(libraries);
        project.setLibraries(libraries);

        // Use the MXML 2009 manifest.
        List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();
        IMXMLNamespaceMapping mxml2009 = new MXMLNamespaceMapping(
                "http://ns.adobe.com/mxml/2009", env.SDK
                        + "\\frameworks\\mxml-2009-manifest.xml");
        namespaceMappings.add(mxml2009);
        project.setNamespaceMappings(namespaceMappings);

        ICompilationUnit cu = null;
        String normalizedMainFileName = FilenameNormalization.normalize(main);

        SourceCompilationUnitFactory compilationUnitFactory = project
                .getSourceCompilationUnitFactory();
        File normalizedMainFile = new File(normalizedMainFileName);
        if (compilationUnitFactory.canCreateCompilationUnit(normalizedMainFile))
        {
            Collection<ICompilationUnit> mainFileCompilationUnits = workspace
                    .getCompilationUnits(normalizedMainFileName, project);
            for (ICompilationUnit cu2 : mainFileCompilationUnits)
            {
                if (cu2 != null)
                    cu = cu2;
            }
        }

        IFileNode fileNode = null;
        try
        {
            fileNode = (IFileNode) cu.getSyntaxTreeRequest().get().getAST();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return fileNode;
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        super.addSourcePaths(sourcePaths);
        sourcePaths.add(new File(FilenameNormalization
                .normalize("test-files/amd/simple-project/src")));
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
