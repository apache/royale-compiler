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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.flex.compiler.config.Configurator;
import org.apache.flex.compiler.driver.IBackend;
import org.apache.flex.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.flex.compiler.internal.driver.mxml.vf2js.MXMLVF2JSBackend;
import org.apache.flex.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.internal.targets.JSTarget;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLFileNode;
import org.apache.flex.compiler.tree.mxml.IMXMLNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.utils.FilenameNormalization;
import org.junit.Ignore;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

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
    public void tearDown()
    {
        asEmitter = null;
        asBlockWalker = null;
        mxmlEmitter = null;
        mxmlBlockWalker = null;
        
        super.tearDown();
    }

    @Override
    protected void addLibraries(List<File> libraries)
    {
        libraries.add(new File(FilenameNormalization.normalize(env.FPSDK
                + "/" + env.FPVER + "/playerglobal.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/framework.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/mx.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/spark.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/vf2js_mx.swc")));
        libraries.add(new File(FilenameNormalization.normalize(env.SDK
                + "/frameworks/libs/vf2js_s.swc")));
        
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
                "http://flex.apache.org/vf2js_mx/ns", new File(env.SDK,
                        "frameworks/vf2js_mx-manifest.xml").getAbsolutePath()));
        
        namespaceMappings.add(new MXMLNamespaceMapping(
                "http://flex.apache.org/vf2js_s/ns", new File(env.SDK,
                        "frameworks/vf2js_s-manifest.xml").getAbsolutePath()));
        
        super.addNamespaceMappings(namespaceMappings);
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
        return new MXMLVF2JSBackend();
    }
    
    @Override
    protected IASNode compile(String input, boolean isFileName,
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
        
        IASNode result = super.compile(onlyName, isFileName, onlyPath,
                useTempFile);
        
        tmpFile.delete();
        
        return result;
    }
    
    @Override
    protected IFileNode compileAS(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        return (IFileNode) compile(input, isFileName, inputDir, useTempFile);
    }
    
    @Override
    protected IMXMLFileNode compileMXML(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        return (IMXMLFileNode) compile(input, isFileName, inputDir, useTempFile);
    }

    @Override
    protected List<String> compileProject(String inputFileName,
            String inputDirName)
    {
        createTempProjectDir(new File(inputDirName).listFiles(), "");
        
        List<String> compiledFileNames = new ArrayList<String>();

        String mainFileName = "temp" + File.separator
                + inputFileName + inputFileExtension;

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
                        + "_output", tempDir);

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

    private void createTempProjectDir(File[] files, String parentPath)
    {
        for (File file : files) 
        {
            if (file.isDirectory()) 
            {
                String path = parentPath + File.separator + file.getName(); 
                
                new File(tempDir + File.separator + path).mkdirs();

                createTempProjectDir(file.listFiles(), path);
            } 
            else 
            {
                String fileName = file.getName();

                if (fileName.contains(".") && fileName.charAt(0) != '.')
                {
                    String extension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
    
                    if (extension.equals(".mxml") || extension.equals(".as"))
                    {
                        File intermediateFile = file;
                        String tempFileName = fileName.substring(0, fileName.indexOf("."));
                        File targetDir = new File(tempDir + File.separator + parentPath);

                        createTempFileWithVF2JSNamespace(intermediateFile, 
                                tempFileName, false, targetDir, extension);
                    }
                }
            }
        }
    }
    
    private File createTempFileWithVF2JSNamespace(File intermediateFile,
            String tempFileName, boolean createTempFile)
    {
        return createTempFileWithVF2JSNamespace(intermediateFile,
                tempFileName, createTempFile, tempDir, inputFileExtension); 
    }
    
    private File createTempFileWithVF2JSNamespace(File intermediateFile,
            String tempFileName, boolean createTempFile, File targetDir,
            String extension)
    {
        File tempFile = null;
        
        try 
        {
            String content = FileUtils.readFileToString(intermediateFile, "UTF-8");

            // mx (MXML)
            content = content.replace(
                    "xmlns:mx=\"library://ns.adobe.com/flex/mx\"", 
                    "xmlns:vf2js_mx=\"http://flex.apache.org/vf2js_mx/ns\"");
            content = content.replace("<mx:", "<vf2js_mx:");
            content = content.replace("</mx:", "</vf2js_mx:");

            // mx (AS)
            content = content.replace("mx.", "vf2js_mx.");

            // s (MXML)
            content = content.replace(
                    "xmlns:s=\"library://ns.adobe.com/flex/spark\"", 
                    "xmlns:vf2js_s=\"http://flex.apache.org/vf2js_s/ns\"");
            content = content.replace("<s:", "<vf2js_s:");
            content = content.replace("</s:", "</vf2js_s:");

            // s (AS)
            content = content.replace("spark.", "vf2js_s.");

            if (createTempFile)
            {
                tempFile = File.createTempFile(tempFileName, extension,
                        targetDir);
                tempFile.deleteOnExit();
            }
            else
            {
                tempFile = new File(targetDir.getAbsolutePath(),
                        tempFileName + extension);
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
