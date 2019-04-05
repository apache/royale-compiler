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

package as;

import org.apache.royale.compiler.clients.MXMLC;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.io.SWFDump;
import org.apache.royale.utils.*;
import utils.FlashplayerSecurityHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


/**
 * Base class for AS feature tests which compile AS code with MXMLC and run it in the standalone Flash Player.
 * Copied and modified from MXMLFeatureTestsBase.java
 */
@SuppressWarnings("deprecation")
public class ASFeatureTestsBase
{
	private static boolean generateResultFile = false;
	
	public ASFeatureTestsBase()
	{
        ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
		File playerExecutable = testAdapter.getFlashplayerDebugger();
		if(playerExecutable == null || !playerExecutable.isFile() || !playerExecutable.exists()) {
			hasFlashPlayerExecutable = false;
		}
	    File playerGlobal = testAdapter.getPlayerglobal();
		if(playerGlobal == null || !playerGlobal.isFile() || !playerGlobal.exists()) {
			hasFlashPlayerGlobal = false;
		}
	}

	protected boolean hasFlashPlayerExecutable = true;
	protected boolean hasFlashPlayerGlobal = true;
	
	private static final String NAMESPACE_2009 = "http://ns.adobe.com/mxml/2009";

	protected File generateTempFile(String source)
	{
        // Write the MXML into a temp file.
        ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
        String tempDir = testAdapter.getTempDir();
        File tempASFile = null;
        try
        {
            tempASFile = File.createTempFile(getClass().getSimpleName(), ".as", new File(tempDir));
            tempASFile.deleteOnExit();

            BufferedWriter out = new BufferedWriter(new FileWriter(tempASFile));
            String className = tempASFile.getName();
            // chop off .as
            className = className.substring(0, className.length() - 3);
            
            source = source.replaceAll("%0", className);
            out.write(source);
            out.close();
        }
        catch (IOException e1) 
        {
            e1.printStackTrace();
            fail("Error generating test code");
        }
        return tempASFile;
	}
	
	protected String compile(File tempASFile, String source, boolean withFramework, boolean withRPC, boolean withSpark, String[] otherOptions, boolean checkExitCode)
	{
        System.out.println("Generating test:");

        ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
        // Build the list of SWCs to compile against on the library path.
        List<String> swcs = new ArrayList<String>();
        if (withFramework)
        {
            swcs.add(testAdapter.getFlexArtifact("framework").getPath());
            swcs.add(testAdapter.getFlexArtifactResourceBundle("framework").getPath());
        }
        if (withRPC)
        {
            swcs.add(testAdapter.getFlexArtifact("rpc").getPath());
            swcs.add(testAdapter.getFlexArtifactResourceBundle("rpc").getPath());
        }
        if (withSpark)
        {
            swcs.add(testAdapter.getFlexArtifact("spark").getPath());
            swcs.add(testAdapter.getFlexArtifactResourceBundle("spark").getPath());
        }

        List<String> args = new ArrayList<String>();
        //args.add("-debug");
        if (hasFlashPlayerGlobal)
        	args.add("-external-library-path=" + testAdapter.getPlayerglobal().getPath());
        else {
        	String jsSwcPath = FilenameNormalization.normalize("../compiler-externc/target/js.swc");
        	args.add("-external-library-path=" + jsSwcPath);
        }
        if(swcs.size() > 0) {
            String libraryPath = "-library-path=" + StringUtils.join(swcs.toArray(new String[swcs.size()]), ",");
            args.add(libraryPath);
        }
        if (withFramework || withRPC || withSpark)
            args.add("-namespace=" + NAMESPACE_2009 + "," + testAdapter.getFlexManifestPath("mxml-2009"));
        if (otherOptions != null)
        {
            Collections.addAll(args, otherOptions);
        }
        args.add(tempASFile.getAbsolutePath());

        // Use MXMLC to compile the MXML file against playerglobal.swc and possibly other SWCs.
        MXMLC mxmlc = new MXMLC();
        StringBuffer cmdLine = new StringBuffer();
        for(String arg : args) {
            cmdLine.append(arg).append(" ");
        }
	    
        System.out.println("Compiling test:\n" + cmdLine.toString());
        int exitCode = mxmlc.mainNoExit(args.toArray(new String[args.size()]));

        // Check that there were no compilation problems.
        List<ICompilerProblem> problems = mxmlc.getProblems().getProblems();
        StringBuilder sb = new StringBuilder(checkExitCode && problems.size() > 0 ? "Unexpected compilation problems:\n" : "");
        for (ICompilerProblem problem : problems)
        {
            sb.append(problem.toString());
            sb.append('\n');
        }
        
        System.out.println("After compile:\n" + sb.toString());
        if (checkExitCode)
            assertThat(sb.toString(), exitCode, is(0));
        return sb.toString();

	}
	
    protected void compileAndExpectErrors(String source, boolean withFramework, boolean withRPC, boolean withSpark, String[] otherOptions, String errors)
    {
        File tempASFile = generateTempFile(source);
        String results = compile(tempASFile, source, withFramework, withRPC, withSpark, otherOptions, false);
        assertThat(results, is(errors));
    }
	protected void compileAndRun(String source, boolean withFramework, boolean withRPC, boolean withSpark, String[] otherOptions)
	{
	    int exitCode = 0;
	    File tempASFile = generateTempFile(source);
	    compile(tempASFile, source, withFramework, withRPC, withSpark, otherOptions, true);
        ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
		// Check the existence of the flashplayer executable
		// Run the SWF in the standalone player amd wait until the SWF calls System.exit().
		String swf = FilenameNormalization.normalize(tempASFile.getAbsolutePath());
		swf = swf.replace(".as", ".swf");
		if (hasFlashPlayerExecutable)
		{
			File playerExecutable = testAdapter.getFlashplayerDebugger();
			String[] runArgs = new String[] { playerExecutable.getPath(), swf };
			try
			{
				System.out.println("Executing test:\n" + Arrays.toString(runArgs));
	
				// TODO: Hack to add the directory containing the temp swf to the flashplayer trust.
				new FlashplayerSecurityHandler().trustFile(tempASFile.getParentFile());
	
				exitCode = executeCommandWithTimeout(runArgs, 20);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				// If we just print the stacktrace the exitCode is still 0 and the test will pass.
				fail("Got exception");
			}
			
		    // Check that the runtime exit code was 0, meaning that no asserts failed.
			assertThat(exitCode, is(0));
		}
		else
		{
			StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
			boolean foundUs = false;
			for (StackTraceElement ste : stackTraceElements)
			{
				String className = ste.getClassName();
				String methodName = ste.getMethodName();
				if (className.equals("as.ASFeatureTestsBase") &&
					methodName.equals("compileAndRun"))
				{
					foundUs = true;
					continue;
				}
				if (foundUs)
				{
				    StringWriter out = new StringWriter();
				    PrintWriter writer = new PrintWriter(out);
					try {
						SWFDump.abcOption = true;
						SWFDump.dumpSwf(writer, SWFDump.toURL(new File(swf)), null);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String ours = out.toString();
					ours = ours.replaceAll("\r\n", "\n");
					className = className.replace(".", "_");
					String resultName = className + "_" + methodName + "_swfdump.xml";
					File resultFile = new File(testAdapter.getUnitTestBaseDir(), "swfdumps/" + resultName);
					if (!resultFile.exists())
					{
						if (generateResultFile)
							writeResultFile(resultFile, tempASFile.getName(), ours);
						System.out.println(resultName);
						System.out.println(ours);
						fail("result file " + resultFile.getAbsolutePath() + " does not exist");
					}
					String resultText = null;
					try {
						FileReader fileReader = new FileReader(resultFile);
						StringBuffer stringBuffer = new StringBuffer();
						int numCharsRead;
						char[] charArray = new char[1024];
						while ((numCharsRead = fileReader.read(charArray)) > 0) {
							stringBuffer.append(charArray, 0, numCharsRead);
						}
						fileReader.close();
						resultText = stringBuffer.toString();
						resultText = resultText.replaceAll("\r\n", "\n");
						String tempClassName = tempASFile.getName();
						tempClassName = tempClassName.replace(".as", "");
						tempClassName = tempClassName.replace(".mxml", "");
						resultText = resultText.replaceAll("%0", tempClassName);
						int c = resultText.indexOf("<!--");
						int c2 = resultText.indexOf("-->\n");
						if (c != -1)
						{
							resultText = resultText.substring(0, c) + resultText.substring(c2 + 4);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					// remove file length because the generated classname could have a different
					// length and affect the swf length
					int c = resultText.indexOf("<!-- framecount");
					int c2 = resultText.indexOf("-->\n", c);
					resultText = resultText.substring(0, c) + resultText.substring(c2 + 4);
					c = ours.indexOf("<!-- framecount");
					c2 = ours.indexOf("-->\n", c);
					ours = ours.substring(0, c) + ours.substring(c2 + 4);
					// remove asc:compiler metadata as Eclipse builds don't get the right value
					c = ours.indexOf("<asc:compiler");
					c2 = ours.indexOf("/>\n", c);
					ours = ours.substring(0, c) + ours.substring(c2 + 4);
					c = resultText.indexOf("<asc:compiler");
					c2 = resultText.indexOf("/>\n", c);
					resultText = resultText.substring(0, c) + resultText.substring(c2 + 4);
                    // remove parsing swf comment as it contains local path to swf
                    c = ours.indexOf("<!-- Parsing swf");
                    c2 = ours.indexOf("-->\n", c);
                    ours = ours.substring(0, c) + ours.substring(c2 + 4);
                    c = resultText.indexOf("<!-- Parsing swf");
                    c2 = resultText.indexOf("-->\n", c);
                    resultText = resultText.substring(0, c) + resultText.substring(c2 + 4);
					// output contains 'succ[...]' which can be in different order
					ours = ours.replaceAll("succs=\\[.*\\]", "");
					resultText = resultText.replaceAll("succs=\\[.*\\]", "");
					// string padding can be different if classname is longer so collapse spaces
					ours = ours.replaceAll("[ ]+", " ");
					resultText = resultText.replaceAll("[ ]+", " ");
					// trim every line.  For some reason there are different numbers of trailing spaces on some lines.
					String[] ourlines = ours.split("\n");
					int n = ourlines.length;
					for (int i = 0; i < n; i++)
					{
						ourlines[i] = ourlines[i].trim();
					}
					ours = StringUtils.join(ourlines, "\n");
					String[] resultLines = resultText.split("\n");
					n = resultLines.length;
					for (int i = 0; i < n; i++)
					{
						resultLines[i] = resultLines[i].trim();
					}
					resultText = StringUtils.join(resultLines, "\n");
					assertThat(ours, is(resultText));
					break;
				}
			}
		}
	}
	
	protected void compileAndRun(String source)
	{
		compileAndRun(source, false, false, false, null);
	}

	public static int executeCommandWithTimeout(String[] args, long timeoutInSeconds) throws Exception {
		ExecutorService service = Executors.newSingleThreadExecutor();
		Process process = Runtime.getRuntime().exec(args);
		try {
			Callable<Integer> call = new CallableProcess(process);
			Future<Integer> future = service.submit(call);
			return future.get(timeoutInSeconds, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			throw new Exception("Process failed to execute", e);
		} catch (TimeoutException e) {
			process.destroy();
			throw new Exception("Process timed out", e);
		} finally {
			service.shutdown();
		}
	}

	private static class CallableProcess implements Callable<Integer> {
		private Process p;

		public CallableProcess(Process process) {
			p = process;
		}

		public Integer call() throws Exception {
			return p.waitFor();
		}
	}

	/**
	 * Combines various code snippets to make a complete one-file application.
	 */
    protected String getAS(String[] imports, String[] declarations, String[] testCode, String[] extraCode)
    {
    	String[] template;
    	
    	if (hasFlashPlayerGlobal)
    	{
	        template = new String[]
	        {
	            "package {",
	            "import flash.display.Sprite;",
	            "import flash.events.Event;",
	            "import flash.system.System;",
	            "%1",
	            "public class %0 extends flash.display.Sprite",
	            "{",
	            "    public function %0()",
	            "    {",
	            "        loaderInfo.addEventListener(flash.events.Event.INIT, initHandler);",
	            "    }",
	            "    %2",
	            "    private function initHandler(e:flash.events.Event):void",
	            "    {",
	            "        %3",
	            "        System.exit(0);",            
	            "    }",
	            "    private function assertEqual(message:String, actualValue:*, expectedValue:*):void",
	            "    {",
	            "        if (actualValue !== expectedValue)",
	            "        {",
	            "            trace(message, actualValue, expectedValue);",
	            "            System.exit(1);",
	            "        }",
	            "    }",
	            "}",
	            "}",
	            "%4"
	        };
    	}
    	else
    	{
	        template = new String[]
  	        {
  	            "package {",
  	            "%1",
  	            "public class %0",
  	            "{",
  	            "    public function %0()",
  	            "    {",
  	            "    }",
  	            "    %2",
  	            "    private function initHandler(e:Object):void",
  	            "    {",
  	            "        %3",
  	            "    }",
	            "    private function assertEqual(message:String, actualValue:*, expectedValue:*):void",
	            "    {",
	            "    }",
  	            "}",
  	            "}",
  	            "%4"
  	        };
    	}
        String source = StringUtils.join(template, "\n");
        source = source.replace("%1", StringUtils.join(imports, "\n"));
        source = source.replace("%2", StringUtils.join(declarations, "\n        "));
        source = source.replace("%3", StringUtils.join(testCode, "\n"));
        source = source.replace("%4", StringUtils.join(extraCode, "\n"));
        return source;
    }
    
	/**
	 * Combines various code snippets to make a complete one-file application.
	 */
    public static void writeResultFile(File file, String className, String results)
    {
		className = className.replace(".as", "");
		className = className.replace(".mxml", "");
    	results = results.replaceAll(className, "%0");
    	int firstLine = results.indexOf("\n");
    	results = results.substring(0, firstLine + 1) + ASF_XML_HEADER + results.substring(firstLine + 1);
    	BufferedWriter out = null;
    	try {
        	FileWriter fw = new FileWriter(file.getAbsolutePath());
    	    out = new BufferedWriter(fw);
    	    out.write(results);
    	}
    	catch (IOException e)
    	{
    	    System.out.println("Exception ");
    	}
    	finally
    	{
    		if (out != null)
    		{
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    }
    
    public static String ASF_XML_HEADER = "<!--\n" +
"\n" +
"  Licensed to the Apache Software Foundation (ASF) under one or more\n" +
"  contributor license agreements.  See the NOTICE file distributed with\n" +
"  this work for additional information regarding copyright ownership.\n" +
"  The ASF licenses this file to You under the Apache License, Version 2.0\n" +
"  (the \"License\"); you may not use this file except in compliance with\n" +
"  the License.  You may obtain a copy of the License at\n" +
"\n" +
"      http://www.apache.org/licenses/LICENSE-2.0\n" +
"\n" +
"  Unless required by applicable law or agreed to in writing, software\n" +
"  distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
"  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
"  See the License for the specific language governing permissions and\n" +
"  limitations under the License.\n" +
"\n" +
"-->\n";

}
