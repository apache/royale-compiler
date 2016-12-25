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

import java.io.File;
import java.util.List;

import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSBackend;
import org.apache.flex.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.utils.FilenameNormalization;
import org.apache.flex.utils.ITestAdapter;
import org.apache.flex.utils.TestAdapterFactory;
import org.junit.Ignore;

@Ignore
public class FlexJSTestBase extends TestBase
{
    protected static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    @Override
    public void setUp()
    {
    	project = new FlexJSProject(workspace, backend);
        super.setUp();

        asEmitter = backend.createEmitter(writer);
        mxmlEmitter = backend.createMXMLEmitter(writer);

        asBlockWalker = backend.createWalker(project, errors, asEmitter);
        mxmlBlockWalker = backend.createMXMLWalker(project, errors,
                mxmlEmitter, asEmitter, asBlockWalker);
    }

    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/" + env.FPVER + "/playerglobal.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/framework.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "\\frameworks\\libs\\rpc.swc")));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Core.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/HTML.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Binding.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Network.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Collections.swc"));

        super.addLibraries(libraries);
    }

    @Override
    protected void addNamespaceMappings(List<IMXMLNamespaceMapping> namespaceMappings)
    {
        namespaceMappings
                .add(new MXMLNamespaceMapping(
                        "library://ns.apache.org/flexjs/basic", new File(
                                env.ASJS + "/frameworks/as/basic-manifest.xml")
                                .getAbsolutePath()));

        super.addNamespaceMappings(namespaceMappings);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(env.ASJS + "/examples/FlexJSTest_basic/src"));
        sourcePaths.add(new File(testAdapter.getUnitTestBaseDir(), "flexjs/files"));

        super.addSourcePaths(sourcePaths);
    }

    @Override
    protected IBackend createBackend()
    {
        return new MXMLFlexJSBackend();
    }

    //--------------------------------------------------------------------------
    // Node "factory"
    //--------------------------------------------------------------------------

    public static final int WRAP_LEVEL_NONE = 0;
    public static final int WRAP_LEVEL_DOCUMENT = 1;

    protected IMXMLNode getNode(String code, Class<? extends IMXMLNode> type,
            int wrapLevel)
    {
        if (wrapLevel >= WRAP_LEVEL_DOCUMENT)
            code = ""
                    + "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/flexjs/basic\">"
                    + code + "</basic:Application>";

        IMXMLFileNode node = compileMXML(code);

        return findFirstDescendantOfType(node, type);
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
