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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.royale.compiler.clients.MXMLJSC;
import org.apache.royale.compiler.codegen.as.IASEmitter;
import org.apache.royale.compiler.codegen.mxml.IMXMLEmitter;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.driver.IBackend;
import org.apache.royale.compiler.internal.codegen.as.ASFilterWriter;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.projects.RoyaleJSProject;
import org.apache.royale.compiler.internal.projects.RoyaleProjectConfigurator;
import org.apache.royale.compiler.internal.projects.ISourceFileHandler;
import org.apache.royale.compiler.internal.targets.JSTarget;
import org.apache.royale.compiler.internal.tree.as.FunctionNode;
import org.apache.royale.compiler.internal.tree.as.ImportNode;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IFileNode;
import org.apache.royale.compiler.tree.mxml.IMXMLFileNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.ISyntaxTreeRequestResult;
import org.apache.royale.utils.EnvProperties;
import org.apache.royale.compiler.visitor.as.IASBlockWalker;
import org.apache.royale.compiler.visitor.mxml.IMXMLBlockWalker;
import org.apache.royale.utils.FilenameNormalization;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Ignore
public class TestBase implements ITestBase
{
    private static ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    protected List<ICompilerProblem> errors;

    protected static EnvProperties env = EnvProperties.initiate();

    protected static Workspace workspace = new Workspace();
    protected RoyaleJSProject project;

    protected IBackend backend;
    protected ASFilterWriter writer;

    protected IASEmitter asEmitter;
    protected IMXMLEmitter mxmlEmitter;

    protected IASBlockWalker asBlockWalker;
    protected IMXMLBlockWalker mxmlBlockWalker;

    protected String inputFileExtension;

    protected String mCode;

    protected File tempDir;

    protected List<File> sourcePaths = new ArrayList<File>();
    protected List<File> libraries = new ArrayList<File>();
    protected List<IMXMLNamespaceMapping> namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();

    @Before
    public void setUp()
    {
        DefinitionBase.setPerformanceCachingEnabled(true);

        errors = new ArrayList<ICompilerProblem>();

        if (project == null)
        {
            backend = createBackend();
        	project = new RoyaleJSProject(workspace, backend);
        	project.setProxyBaseClass("custom.TestProxy");
        }
        project.setProblems(errors);
        RoyaleProjectConfigurator.configure(project);
        try {
	        Configurator projectConfigurator = backend.createConfigurator();
	        project.setTargetSettings(projectConfigurator.getTargetSettings(null));
        }
        catch (UnsupportedOperationException e)
        {
        }

        writer = backend.createWriterBuffer(project);

        try
        {
            ISourceFileHandler sfh = backend.getSourceFileHandlerInstance();
            inputFileExtension = "." + sfh.getExtensions()[0];
        }
        catch (Exception e)
        {
            inputFileExtension = ".as";
        }

        sourcePaths = new ArrayList<File>();
        libraries = new ArrayList<File>();
        namespaceMappings = new ArrayList<IMXMLNamespaceMapping>();

        tempDir = new File(TestAdapterFactory.getTestAdapter().getTempDir()); // ensure this exists
    }

    @After
    public void tearDown()
    {
        DefinitionBase.setPerformanceCachingEnabled(false);
        backend = null;
        writer = null;
    }

    protected IBackend createBackend()
    {
        return null;
    }

    protected void assertErrors(String errorReport)
    {
    	StringBuilder actualErrors = new StringBuilder();
    	
    	// switch to a different set of problems for any other 
    	// threads still outputting problems.  Otherwise we
    	// can get a concurrent modification exception
    	project.setProblems(new ArrayList<ICompilerProblem>());
    	
    	for (ICompilerProblem problem : errors)
    	{
    		if (problem.toString().equals("An externally-visible definition with the name 'Array' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'Function' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'Object' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'String' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'Boolean' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'Number' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'Error' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'RangeError' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'ReferenceError' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'TypeError' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'int' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'uint' was unexpectedly found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.Array' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.Function' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.Object' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.String' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.Number' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.Boolean' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.Error' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.RangeError' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.ReferenceError' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.TypeError' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.int' was found."))
    			continue;
    		if (problem.toString().equals("No externally-visible definition with the name 'typedefs.as.classes.uint' was found."))
    			continue;
    		if (problem.toString().equals("An externally-visible definition with the name 'RoyaleTest_A' was unexpectedly found."))
    			continue;
    		if (problem.toString().startsWith("No externally-visible definition with the name 'TestRoyaleGlobalFunctions"))
    			continue;
    		actualErrors.append(problem.toString());
    	}
        assertThat(actualErrors.toString(), is(errorReport));
    }
    
    protected void assertOut(String code, boolean keepMetadata)
    {
    	mCode = removeGeneratedString(writer.toString());
    	if (!keepMetadata)
    		mCode = removeMetadata(mCode);
        //System.out.println(mCode);
        /*if (!code.equals(mCode)) {
            System.out.println("mCode:\n"+mCode);
            System.out.println("code:\n"+code);
        }*/
        assertThat(mCode, is(code));
    }
    
    protected void assertOutPostProcess(String code, boolean keepMetadata)
    {
    	mCode = removeGeneratedString(asEmitter.postProcess(writer.toString()));
    	if (!keepMetadata)
    		mCode = removeMetadata(mCode);
        //System.out.println(mCode);
        /*if (!code.equals(mCode)) {
            System.out.println("mCode:\n"+mCode);
            System.out.println("code:\n"+code);
        }*/
        assertThat(mCode, is(code));
    }
    
    protected void assertOutMXMLPostProcess(String code, boolean keepMetadata)
    {
    	mCode = removeGeneratedString(mxmlEmitter.postProcess(writer.toString()));
    	if (!keepMetadata)
    		mCode = removeMetadata(mCode);
        //System.out.println(mCode);
        /*if (!code.equals(mCode)) {
            System.out.println("mCode:\n"+mCode);
            System.out.println("code:\n"+code);
        }*/
        assertThat(mCode, is(code));
    }
    
    protected void assertOut(String code)
    {
        assertOut(code, false);
    }
    
    protected void assertOutWithMetadata(String code)
    {
        assertOut(code, true);
    }
    
    protected String removeMetadata(String code)
    {
    	int c = code.indexOf("\n\n\n/**\n * Metadata");
    	if (c != -1)
    		return code.substring(0, c);
    	return code;
    }

    protected String removeGeneratedString(String code)
    {
    	int c = code.indexOf(" * Generated by Apache Royale Compiler");
    	if (c != -1)
    	{
    		int c2 = code.indexOf("\n", c);
    		String newString = code.substring(0, c);
    		newString += code.substring(c2 + 1);
    		return newString;
    	}
    	return code;
    }
    
    @Override
    public String toString()
    {
        return writer.toString();
    }

    protected IFileNode compileAS(String input)
    {
        return compileAS(input, false, "");
    }

    protected IFileNode compileAS(String input, boolean isFileName,
            String inputDir)
    {
        return compileAS(input, isFileName, inputDir, true);
    }

    protected IFileNode compileAS(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        return (IFileNode) compile(input, isFileName, inputDir, useTempFile);
    }

    protected IASNode compile(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        File tempFile = (useTempFile) ? writeCodeToTempFile(input, isFileName, inputDir) :
                new File(inputDir + File.separator + input + inputFileExtension);

        addDependencies();

        String normalizedMainFileName = FilenameNormalization
                .normalize(tempFile.getAbsolutePath());

        Collection<ICompilationUnit> mainFileCompilationUnits = workspace
                .getCompilationUnits(normalizedMainFileName, project);

        ICompilationUnit cu = null;
        for (ICompilationUnit cu2 : mainFileCompilationUnits)
        {
            if (cu2 != null)
                cu = cu2;
        }

        IASNode fileNode = null;
        try
        {
        	ISyntaxTreeRequestResult result = cu.getSyntaxTreeRequest().get();
        	ICompilerProblem[] problems = result.getProblems();
        	if (problems.length > 0)
        	{
        		for (ICompilerProblem problem : problems)
        			System.out.println(problem.toString());
        		return null;
        	}
            fileNode = result.getAST();
            project.getDependencies(cu);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return fileNode;
    }

    protected List<String> compileProject(String inputFileName,
            String inputDirName) 
    {
    	return compileProject(inputFileName, inputDirName, new StringBuilder(), true);
    }
    
    protected List<String> compileProject(String inputFileName,
            String inputDirName, StringBuilder sb, boolean ignoreErrors) 
    {
        List<String> compiledFileNames = new ArrayList<String>();

        String mainFileName = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                inputDirName + "/" + inputFileName + inputFileExtension).getPath();

        addDependencies();

        ICompilationUnit mainCU = Iterables
                .getOnlyElement(workspace.getCompilationUnits(
                        FilenameNormalization.normalize(mainFileName), project));

        project.mainCU = mainCU;
        Configurator projectConfigurator = backend.createConfigurator();

        JSTarget target = (JSTarget) backend.createTarget(project,
                projectConfigurator.getTargetSettings(null), null);

        ArrayList<ICompilerProblem> errors = new ArrayList<ICompilerProblem>();
        target.build(mainCU, errors);

        if (!ignoreErrors && errors.size() > 0)
        {
        	for (ICompilerProblem error : errors)
        	{
        		String fn = error.getSourcePath();
                if(fn != null) {
                    int c = fn.indexOf(testAdapter.getUnitTestBaseDir().getPath());
                    fn = fn.substring(c);
                    sb.append(fn);
                    sb.append("(" + error.getLine() + ":" + error.getColumn() + ")\n");
                    sb.append(error.toString() + "\n");
                }
        	}
        	System.out.println(sb.toString());
        	return compiledFileNames;
        }
        List<ICompilationUnit> reachableCompilationUnits = project
                .getReachableCompilationUnitsInSWFOrder(ImmutableSet.of(mainCU));
        for (final ICompilationUnit cu : reachableCompilationUnits)
        {
            try
            {
                ICompilationUnit.UnitType cuType = cu.getCompilationUnitType();

                if (cuType == ICompilationUnit.UnitType.AS_UNIT
                        || cuType == ICompilationUnit.UnitType.MXML_UNIT)
                {
                    File outputRootDir = new File(
                            FilenameNormalization.normalize(tempDir
                                    + File.separator + inputDirName));

                    String qname = cu.getQualifiedNames().get(0);

                    compiledFileNames.add(qname.replace(".", "/"));

                    final File outputClassFile = getOutputClassFile(qname
                            + "_output", outputRootDir);

                    ASFilterWriter writer = backend.createWriterBuffer(project);
                    IASEmitter emitter = backend.createEmitter(writer);
                    IASBlockWalker walker = backend.createWalker(project,
                            errors, emitter);

                    walker.visitCompilationUnit(cu);

                    //System.out.println(writer.toString());

                    BufferedOutputStream out = new BufferedOutputStream(
                            new FileOutputStream(outputClassFile));

                    out.write(emitter.postProcess(writer.toString()).getBytes());
                    out.flush();
                    out.close();
                }
            }
            catch (Exception e)
            {
                //System.out.println(e.getMessage());
            }
        }

        /*
        File outputRootDir = new File(
                FilenameNormalization.normalize(tempDir
                        + File.separator + inputDirName));
        String qname;
		try {
			qname = mainCU.getQualifiedNames().get(0);
	        final File outputClassFile = getOutputClassFile(qname
	                + "_output", outputRootDir);
	        appendLanguageAndXML(outputClassFile.getAbsolutePath(), qname);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
        return compiledFileNames;
    }

    protected void writeFile(String path, String content, boolean append)
    throws IOException
	{
		File tgtFile = new File(path);
		
		if (!tgtFile.exists())
		    tgtFile.createNewFile();
		
		FileWriter fw = new FileWriter(tgtFile, append);
		fw.write(content);
		fw.close();
	}
    
    /*
    private void appendLanguageAndXML(String path, String projectName) throws IOException
    {
        StringBuilder appendString = new StringBuilder();
        appendString.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
        appendString.append(ASEmitterTokens.PAREN_OPEN.getToken());
        appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
        appendString.append(JSRoyaleEmitterTokens.LANGUAGE_QNAME.getToken());
        appendString.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
        appendString.append(ASEmitterTokens.PAREN_CLOSE.getToken());
        appendString.append(ASEmitterTokens.SEMICOLON.getToken());
        appendString.append("\n");

        String fileData = readCode(new File(path));
        int reqidx = fileData.indexOf(appendString.toString());
	    if (reqidx == -1 && project instanceof RoyaleJSProject && ((RoyaleJSProject)project).needLanguage)
        {
	    	boolean afterProvide = false;
            reqidx = fileData.lastIndexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
            if (reqidx == -1)
            {
            	afterProvide = true;
                reqidx = fileData.lastIndexOf(JSGoogEmitterTokens.GOOG_PROVIDE.getToken());
            }
            reqidx = fileData.indexOf(";", reqidx);
            String after = fileData.substring(reqidx + 1);
            String before = fileData.substring(0, reqidx + 1);
            if (afterProvide)
            	before += "\n";
            String s = before + "\n" + appendString.toString() + after;
            writeFile(path, s, false);
        }
        
        StringBuilder appendStringXML = new StringBuilder();
        appendStringXML.append(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
        appendStringXML.append(ASEmitterTokens.PAREN_OPEN.getToken());
        appendStringXML.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
        appendStringXML.append(IASLanguageConstants.XML);
        appendStringXML.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
        appendStringXML.append(ASEmitterTokens.PAREN_CLOSE.getToken());
        appendStringXML.append(ASEmitterTokens.SEMICOLON.getToken());
        appendStringXML.append("\n");

        if (project instanceof RoyaleJSProject && ((RoyaleJSProject)project).needXML)
        {
	        fileData = readCode(new File(path));
	        reqidx = fileData.indexOf(appendStringXML.toString());
	        if (reqidx == -1)
	        {
		    	boolean afterProvide = false;
	            reqidx = fileData.lastIndexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
	            if (reqidx == -1)
	            {
	            	afterProvide = true;
	                reqidx = fileData.lastIndexOf(JSGoogEmitterTokens.GOOG_PROVIDE.getToken());
	            }
	            reqidx = fileData.lastIndexOf(JSGoogEmitterTokens.GOOG_REQUIRE.getToken());
	            if (reqidx == -1)
	                reqidx = fileData.lastIndexOf(JSGoogEmitterTokens.GOOG_PROVIDE.getToken());
	            reqidx = fileData.indexOf(";", reqidx);
	            String after = fileData.substring(reqidx + 1);
	            String before = fileData.substring(0, reqidx + 1);
	            if (afterProvide)
	            	before += "\n";
	            String s = before + "\n" + appendStringXML.toString() + after;
	            writeFile(path, s, false);
	        }
        }
    }
*/
	protected String readCode(File file)
	{
	    String code = "";
	    try
	    {
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	                new FileInputStream(file), "UTF8"));
	
	        String line = in.readLine();
	
	        while (line != null)
	        {
	            code += line + "\n";
	            line = in.readLine();
	        }
	        code = code.substring(0, code.length() - 1);
	
	        in.close();
	    }
	    catch (Exception e)
	    {
	        // nothing to see, move along...
	    }
	
	    return code;
	}

    protected File getOutputClassFile(String qname, File outputFolder)
    {
        String[] cname = qname.split("\\.");
        String sdirPath = outputFolder + File.separator;
        if (cname.length > 0)
        {
            for (int i = 0, n = cname.length - 1; i < n; i++)
            {
                sdirPath += cname[i] + File.separator;
            }

            File sdir = new File(sdirPath);
            if (!sdir.exists())
                sdir.mkdirs();

            qname = cname[cname.length - 1];
        }

        return new File(sdirPath + qname + "." + backend.getOutputExtension());
    }

    protected IMXMLFileNode compileMXML(String input)
    {
        return compileMXML(input, false, "");
    }

    protected IMXMLFileNode compileMXML(String input, boolean isFileName,
            String inputDir)
    {
        return compileMXML(input, isFileName, inputDir, true);
    }

    protected IMXMLFileNode compileMXML(String input, boolean isFileName,
            String inputDir, boolean useTempFile)
    {
        return (IMXMLFileNode) compile(input, isFileName, inputDir, useTempFile);
    }

    protected File writeCodeToTempFile(String input, boolean isFileName,
            String inputDir)
    {
        File tempASFile = null;
        try
        {
            String tempFileName = (isFileName) ? input : getClass()
                    .getSimpleName();

            tempASFile = File.createTempFile(tempFileName, inputFileExtension,
                    new File(TestAdapterFactory.getTestAdapter().getTempDir()));
            tempASFile.deleteOnExit();

            String code = "";
            if (!isFileName)
            {
                code = input;
            }
            else
            {
                code = getCodeFromFile(input, false, inputDir);
            }

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempASFile),"UTF-8"));
            out.write(code);
            out.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        return tempASFile;
    }

    protected void writeResultToFile(String result, String fileName)
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(tempDir, fileName + ".js")),
                    "utf-8"));
            writer.write(result);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * Overridable setup of dependencies, default adds source, libraries and
     * namepsaces.
     * <p>
     * The test will then set the dependencies on the current
     * {@link ICompilerProject}.
     */
    protected void addDependencies()
    {
        addSourcePaths(sourcePaths);
        addLibraries(libraries);
        if (libraries.size() == 0)
        {
        	String jsSwcPath = FilenameNormalization.normalize("../compiler-externc/target/js.swc");
    		libraries.add(new File(jsSwcPath));
        	String customSwcPath = FilenameNormalization.normalize("../compiler/target/custom.swc");
    		libraries.add(new File(customSwcPath));
        }
        addNamespaceMappings(namespaceMappings);

        project.setSourcePath(sourcePaths);
        project.setLibraries(libraries);
        project.setNamespaceMappings(namespaceMappings);
    }

    protected void addLibraries(List<File> libraries)
    {
    }

    protected void addSourcePaths(List<File> sourcePaths)
    {
        sourcePaths.add(tempDir);
    }

    protected void addNamespaceMappings(
            List<IMXMLNamespaceMapping> namespaceMappings)
    {
    }

    protected String getCodeFromFile(String fileName, boolean isJS,
            String sourceDir)
    {
        File testFile = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                sourceDir + "/" + fileName
                        + (isJS ? ".js" : inputFileExtension));

        return readCodeFile(testFile);
    }

    protected String getCodeFromFile(String fileName,
            String sourceDir)
    {
        File testFile = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(),
                sourceDir + "/" + fileName);

        return readCodeFile(testFile);
    }

    protected String readCodeFile(File file)
    {
        boolean isResult = file.getName().contains("_result") || file.getName().equals("output.js");
        String code = "";
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF8"));

            String line = in.readLine();
            if (line.contains("/**") && isResult)
            {
                // eat opening comment which should be apache header
                while (line != null)
                {
                    line = in.readLine();
                    if (line.contains("*/"))
                    {
                        line = in.readLine();
                        break;
                    }
                }
            }
            else if (line.contains("<!--") && isResult)
            {
                // eat opening comment which should be apache header
                while (line != null)
                {
                    line = in.readLine();
                    if (line.contains("-->"))
                    {
                        line = in.readLine();
                        break;
                    }
                }            	
            }

            while (line != null)
            {
                code += line + "\n";
                line = in.readLine();
            }
            code = code.substring(0, code.length() - 1);
            code = removeGeneratedString(code);

            in.close();
        }
        catch (Exception e)
        {
        }
        return code;
    }

    protected IASNode findFirstDescendantOfType(IASNode node,
            Class<? extends IASNode> nodeType)
    {
        int n = node.getChildCount();
        for (int i = 0; i < n; i++)
        {
            IASNode child = node.getChild(i);
            if (child instanceof ImportNode)
                continue;   // not interested in these and they have BinaryOps inside
            if (child instanceof FunctionNode)
            {
                ((FunctionNode) child).parseFunctionBody(errors);
            }
            if (nodeType.isInstance(child))
                return child;

            IASNode found = findFirstDescendantOfType(child, nodeType);
            if (found != null)
                return found;
        }

        return null;
    }

    protected int compileAndPublishProject(String projectFolderPath,
            String projectName, String mainFileName)
    {
    	String sourceFolderName = tempDir + "/" + projectName + "/src";
        IOFileFilter asFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".as"));
        File sourceFolder = new File(sourceFolderName);
        File projectFolder = new File(TestAdapterFactory.getTestAdapter().getUnitTestBaseDir(), projectFolderPath);
        try {
			FileUtils.copyDirectory(projectFolder, sourceFolder, asFilter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int numArgs = 3;
		if (env.GOOG != null) numArgs++;
		String[] args = new String[numArgs];
		File mainFile = new File(sourceFolder, mainFileName);
		//args[0] = "-compiler.targets=JS";
		int index = 0;
		args[index++] = "-external-library-path=" + env.ASJS + "/js/libs/js.swc";
		args[index++] = "-remove-circulars";
		if (env.GOOG != null)
			args[index++] = "-closure-lib=" + env.GOOG;
		args[index++] = mainFile.getAbsolutePath();
		int exitCode = MXMLJSC.staticMainNoExit(args);
		return exitCode;
    }

}
