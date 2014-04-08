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
import org.apache.flex.compiler.internal.driver.mxml.MXMLBackend;
import org.apache.flex.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

@Ignore
public class MXMLTestBase extends TestBase
{

    @Override
    public void setUp()
    {
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
                + "/11.1/playerglobal.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/framework.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/mx.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/spark.swc")));

        super.addLibraries(libraries);
    }

    @Override
    protected void addNamespaceMappings(
            List<IMXMLNamespaceMapping> namespaceMappings)
    {
        namespaceMappings
                .add(new MXMLNamespaceMapping("http://ns.adobe.com/mxml/2009",
                        new File(env.SDK, "frameworks/mxml-2009-manifest.xml")
                                .getAbsolutePath()));
        namespaceMappings.add(new MXMLNamespaceMapping(
                "library://ns.adobe.com/flex/mx", new File(env.SDK,
                        "frameworks/mx-manifest.xml").getAbsolutePath()));
        namespaceMappings.add(new MXMLNamespaceMapping(
                "library://ns.adobe.com/flex/spark", new File(env.SDK,
                        "frameworks/spark-manifest.xml").getAbsolutePath()));

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
            code = "<s:Button " + code + "></s:Button>";

        if (wrapLevel >= WRAP_LEVEL_DOCUMENT)
            code = ""
                    + "<s:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\""
                    + " xmlns:s=\"library://ns.adobe.com/flex/spark\""
                    + " xmlns:mx=\"library://ns.adobe.com/flex/mx\">" + code
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
