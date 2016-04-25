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
package org.apache.flex.compiler.internal.test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.flex.compiler.internal.driver.js.vf2js.VF2JSBackend;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.utils.FilenameNormalization;
import org.apache.flex.utils.ITestAdapter;
import org.apache.flex.utils.TestAdapterFactory;
import org.junit.Ignore;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Ignore
public class VF2JSTestBase extends MXMLTestBase
{
    private static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    @Override
    public void setUp()
    {
    	project = new FlexJSProject(workspace);

    	super.setUp();
    }

    @Override
    public void tearDown()
    {
        asEmitter = null;
        asBlockWalker = null;
        mxmlEmitter = null;
        mxmlBlockWalker = null;
        
        super.tearDown();
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        //sourcePaths.add(new File(FilenameNormalization.normalize("")));

        super.addSourcePaths(sourcePaths);
    }

    @Override
    protected IBackend createBackend()
    {
        return new VF2JSBackend();
    }

    @Override
    protected List<String> compileProject(String inputFileName,
            String inputDirName)
    {
        List<String> compiledFileNames = new ArrayList<String>();

        String mainFileName = new File(testAdapter.getUnitTestBaseDir(),
                inputDirName + "/" + inputFileName + inputFileExtension).getPath();

        addDependencies();

        String normalizedFileName = FilenameNormalization.normalize(
                mainFileName);
        Collection<ICompilationUnit> compilationUnits = 
                workspace.getCompilationUnits(normalizedFileName, project);
        ICompilationUnit mainCU = Iterables.getOnlyElement(
                compilationUnits);
        
        if (project instanceof FlexJSProject)
            ((FlexJSProject) project).mainCU = mainCU;
        
        Configurator projectConfigurator = backend.createConfigurator();

        JSTarget target = (JSTarget) backend.createTarget(project,
                projectConfigurator.getTargetSettings(null), null);

        target.build(mainCU, new ArrayList<ICompilerProblem>());

        List<ICompilationUnit> reachableCompilationUnits = project
                .getReachableCompilationUnitsInSWFOrder(ImmutableSet.of(mainCU));
        for (final ICompilationUnit cu : reachableCompilationUnits)
        {
            ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();

            if (cuType == ICompilationUnit.UnitType.AS_UNIT
                    || cuType == ICompilationUnit.UnitType.MXML_UNIT)
            {
                File outputRootDir = new File(
                        FilenameNormalization.normalize(tempDir
                                + File.separator + inputDirName));

                String qname = "";
                try
                {
                    qname = cu.getQualifiedNames().get(0);
                }
                catch (InterruptedException error)
                {
                    System.out.println(error);
                }

                compiledFileNames.add(qname.replace(".", "/"));

                final File outputClassFile = getOutputClassFile(qname
                        + "_output", outputRootDir);

                ASFilterWriter outputWriter = backend.createWriterBuffer(project);

                //asEmitter = backend.createEmitter(outputWriter);
                //asBlockWalker = backend.createWalker(project, errors, asEmitter);

                if (cuType == ICompilationUnit.UnitType.AS_UNIT)
                {
                    asBlockWalker.visitCompilationUnit(cu);
                }
                else
                {
                    //mxmlEmitter = backend.createMXMLEmitter(outputWriter);
                    
                    //mxmlBlockWalker = backend.createMXMLWalker(project, errors,
                    //        mxmlEmitter, asEmitter, asBlockWalker);

                    mxmlBlockWalker.visitCompilationUnit(cu);
                }
                
                System.out.println(outputWriter.toString());

                try
                {
                    BufferedOutputStream out = new BufferedOutputStream(
                            new FileOutputStream(outputClassFile));

                    out.write(outputWriter.toString().getBytes());
                    out.flush();
                    out.close();
                }
                catch (Exception error)
                {
                    System.out.println(error);
                }
                
                outputWriter = null;
            }
        }

        return compiledFileNames;
    }

    //--------------------------------------------------------------------------
    // Node "factory"
    //--------------------------------------------------------------------------

    public static final int WRAP_LEVEL_DOCUMENT = 1;
    public static final int WRAP_LEVEL_NODE = 2;

    protected IMXMLNode getNode(String code, Class<? extends IMXMLNode> type,
            int wrapLevel)
    {
        if (wrapLevel >= WRAP_LEVEL_NODE)
            code = "<s:Button " + code + "></s:Button>";

        if (wrapLevel >= WRAP_LEVEL_DOCUMENT)
            code = ""
                    + "<s:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                    + "               xmlns:s=\"library://ns.adobe.com/flex/spark\"" 
                    + "               xmlns:mx=\"library://ns.adobe.com/flex/mx\">\n"
                    + code + "\n"
                    + "</s:Application>";
        
        IMXMLFileNode node = compileMXML(code);

        if (wrapLevel >= WRAP_LEVEL_NODE) // for now: attributes
        {
            IMXMLNode pnode = findFirstDescendantOfType(node, type);

            IMXMLNode cnode = findFirstDescendantOfType(pnode, type);

            return cnode;
        }
        else
        {
            return findFirstDescendantOfType(node, type);
        }
    }

    protected IMXMLNode findFirstDescendantOfType(IMXMLNode node,
            Class<? extends IMXMLNode> nodeType)
    {

        int n = node.getChildCount();
        for (int i = 0; i < n; i++)
        {
            IMXMLNode child = (IMXMLNode) node.getChild(i);
            if (nodeType.isInstance(child))
                return child;

            IMXMLNode found = findFirstDescendantOfType(child,
                    nodeType);
            if (found != null)
                return found;
        }

        return null;
    }
}
