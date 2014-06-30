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
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.driver.mxml.flexjs.MXMLFlexJSBackend;
import org.apache.flex.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

@Ignore
public class VF2JSTestBase extends TestBase
{

    @Override
    public void setUp()
    {
        project = new FlexJSProject(workspace);
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
                + "/frameworks/libs/spark.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/vf2js.swc")));
        
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
                "http://flex.apache.org/vf2js/ns", new File(env.SDK,
                        "frameworks/vf2js-manifest.xml").getAbsolutePath()));
        
        super.addNamespaceMappings(namespaceMappings);
    }

    @Override
    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(new File(FilenameNormalization.normalize("test-files/vf2js/files")));

        super.addSourcePaths(sourcePaths);
    }

    @Override
    protected IBackend createBackend()
    {
        return new MXMLFlexJSBackend();
    }
    
    @Override
    protected IMXMLFileNode compileMXML(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        File intermediateFile = new File(
                inputDir + File.separator + input + inputFileExtension);
        
        File tmpFile = createTempFileWithVF2JSNamespace(intermediateFile, input,
                false);
        
        String fileName = tmpFile.getName();
        String onlyName = fileName.substring(0, fileName.lastIndexOf('.'));
        String filePath = tmpFile.getAbsolutePath();
        String onlyPath = filePath.substring(0,
                filePath.lastIndexOf(File.separator));
        
        IMXMLFileNode result = 
                (IMXMLFileNode) compile(onlyName, isFileName, onlyPath, useTempFile);
        
        tmpFile.delete();
        
        return result;
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
            code = "<vf2js:Button " + code + "></vf2js:Button>";

        if (wrapLevel >= WRAP_LEVEL_DOCUMENT)
            code = ""
                    + "<vf2js:Application xmlns:fx=\"http://ns.adobe.com/mxml/2009\"\n"
                    + "                   xmlns:vf2js=\"http://flex.apache.org/vf2js/ns\">\n"
                    + code + "\n"
                    + "</vf2js:Application>";
        
        File intermediateFile = writeCodeToTempFile(code, false, "");

        File tmpFile = createTempFileWithVF2JSNamespace(intermediateFile,
                getClass().getSimpleName(), true);
        
        String fileName = tmpFile.getName();
        String onlyName = fileName.substring(0, fileName.lastIndexOf('.'));
        String filePath = tmpFile.getAbsolutePath();
        String onlyPath = filePath.substring(0,
                filePath.lastIndexOf(File.separator));
        
        IMXMLFileNode node = compileMXML(onlyName, true, onlyPath, false);

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

    private File createTempFileWithVF2JSNamespace(File intermediateFile,
            String tempFileName, boolean createTempFile)
    {
        File tempFile = null;
        
        try 
        {
            String content = FileUtils.readFileToString(intermediateFile, "UTF-8");
            content = content.replace("<s:", "<vf2js:");
            content = content.replace("</s:", "</vf2js:");
            if (createTempFile)
            {
                tempFile = File.createTempFile(tempFileName, inputFileExtension,
                        tempDir);
                tempFile.deleteOnExit();
            }
            else
            {
                tempFile = new File(tempDir.getAbsolutePath(), tempFileName + inputFileExtension);
            }
            FileUtils.writeStringToFile(tempFile, content, "UTF-8");
        } 
        catch (IOException e) 
        {
            throw new RuntimeException("Generating file failed", e);
        }

        return tempFile;
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
