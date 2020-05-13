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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.as.codegen.BindableHelper;
import org.apache.royale.compiler.internal.driver.mxml.royale.MXMLRoyaleBackend;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.compiler.tree.mxml.IMXMLScriptNode;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.Ignore;

@Ignore
public class RoyaleTestBase extends TestBase
{
    protected static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    @Override
    public void setUp()
    {
        backend = createBackend();
        BindableHelper.PROPERTY_CHANGE = "valueChange";
    	project = new RoyaleJSProject(workspace, backend);
        super.setUp();

        asEmitter = backend.createEmitter(writer);
        mxmlEmitter = backend.createMXMLEmitter(writer);
        asEmitter.setParentEmitter(mxmlEmitter);

        asBlockWalker = backend.createWalker(project, errors, asEmitter);
        mxmlBlockWalker = backend.createMXMLWalker(project, errors,
                mxmlEmitter, asEmitter, asBlockWalker);
    }

    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/" + env.FPVER + "/playerglobal.swc")));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Core.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Basic.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/HTML.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Binding.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Network.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Charts.swc"));
        libraries.add(new File(env.ASJS + "/frameworks/libs/Collections.swc"));
    	String jsSwcPath = FilenameNormalization.normalize("../compiler-externc/target/js.swc");
		libraries.add(new File(jsSwcPath));

        super.addLibraries(libraries);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(env.ASJS + "/examples/RoyaleTest_basic/src"));
        sourcePaths.add(new File(testAdapter.getUnitTestBaseDir(), "royale/files"));

        super.addSourcePaths(sourcePaths);
    }

    @Override
    protected IBackend createBackend()
    {
        return new MXMLRoyaleBackend();
    }

    //--------------------------------------------------------------------------
    // Node "factory"
    //--------------------------------------------------------------------------

    public static final int WRAP_LEVEL_NONE = 0;
    public static final int WRAP_LEVEL_DOCUMENT = 1;

    protected IASNode getASNode(String code, Class<? extends IASNode> type)
    {
        code = ""
                + "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\"><fx:Script><![CDATA["
                + code + "]]></fx:Script></basic:Application>";

        IMXMLFileNode node = compileMXML(code);
        assertNotNull(node);

        return findFirstASDescendantOfType(node, type);
    }

    protected IMXMLNode getNode(String code, Class<? extends IMXMLNode> type,
            int wrapLevel)
    {
        if (wrapLevel >= WRAP_LEVEL_DOCUMENT)
        {
            code = ""
                    + "<basic:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:basic=\"library://ns.apache.org/royale/basic\">"
                    + code + "</basic:Application>";
        }

        IMXMLFileNode node = compileMXML(code);
        assertNotNull(node);

        return findFirstDescendantOfType(node, type);
    }

    protected IASNode findFirstASDescendantOfType(IMXMLNode node,
                                                  Class<? extends IASNode> nodeType)
    {
        IMXMLScriptNode scriptNode = (IMXMLScriptNode) findFirstDescendantOfType(node, IMXMLScriptNode.class);
        if (scriptNode != null)
        {
            for (IASNode asNode : scriptNode.getASNodes())
            {
                if (nodeType.isInstance(asNode))
                {
                    return asNode;
                }
            }
        }
        return null;
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
