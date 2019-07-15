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
import org.apache.royale.compiler.internal.driver.mxml.MXMLBackend;
import org.apache.royale.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLNode;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.Ignore;

@Ignore
public class MXMLTestBase extends TestBase
{

    protected ITestAdapter testAdapter;

    @Override
    public void setUp()
    {
        super.setUp();

        testAdapter = TestAdapterFactory.getTestAdapter();

        asEmitter = backend.createEmitter(writer);
        mxmlEmitter = backend.createMXMLEmitter(writer);

        asBlockWalker = backend.createWalker(project, errors, asEmitter);
        mxmlBlockWalker = backend.createMXMLWalker(project, errors,
                mxmlEmitter, asEmitter, asBlockWalker);
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
    protected void addLibraries(List<File> libraries)
    {
        //libraries.addAll(testAdapter.getLibraries(true));

        super.addLibraries(libraries);
    }

    @Override
    protected void addNamespaceMappings(
            List<IMXMLNamespaceMapping> namespaceMappings)
    {
        namespaceMappings.add(new MXMLNamespaceMapping(
                "http://ns.adobe.com/mxml/2009",
                FilenameNormalization.normalize("../compiler/src/test/resources/mxml-2009-manifest.xml")));
    	String customManifestPath = FilenameNormalization.normalize("../compiler/src/test/resources/custom-manifest.xml");
    	namespaceMappings.add(new MXMLNamespaceMapping(
    			"library://ns.apache.org/royale/test",
    			customManifestPath));

        super.addNamespaceMappings(namespaceMappings);
    }

    @Override
    protected IBackend createBackend()
    {
        return new MXMLBackend();
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
            code = "<custom:Button " + code + "></custom:Button>";

        if (wrapLevel >= WRAP_LEVEL_DOCUMENT)
            code = ""
                    + "<custom:TestInstance xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                    + " xmlns:custom=\"library://ns.apache.org/royale/test\">"
                    + code
                    + "</custom:TestInstance>";

        IMXMLFileNode node = compileMXML(code);
        assertNotNull(node);

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
