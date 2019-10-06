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

package mxml.tags;

import org.apache.royale.compiler.clients.MXMLC;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.io.SWFDump;
import org.apache.royale.utils.*;

import as.ASFeatureTestsBase;

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
 * Base class for MXML feature tests which compile MXML code with MXMLC and run it in the standalone Flash Player.
 * 
 * @author Gordon Smith
 */
@SuppressWarnings("deprecation")
public class MXMLFeatureTestsBase
{
	private static boolean generateResultFile = false;
	
	private static final String NAMESPACE_2009 = "http://ns.adobe.com/mxml/2009";
	private static final String NAMESPACE_TEST = "library://ns.apache.org/royale/test";
    
	public MXMLFeatureTestsBase()
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
	
	protected void compileAndRun(String mxml, boolean withFramework, boolean withRPC, boolean withSpark, String[] otherOptions)
	{
		System.out.println("Generating test:");

		// Write the MXML into a temp file.
		ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
		String tempDir = testAdapter.getTempDir();
		File tempMXMLFile = null;
		try
		{
			tempMXMLFile = File.createTempFile(getClass().getSimpleName(), ".mxml", new File(tempDir));
			tempMXMLFile.deleteOnExit();

			BufferedWriter out = new BufferedWriter(new FileWriter(tempMXMLFile));
		    out.write(mxml);
		    out.close();
		}
		catch (IOException e1) 
		{
			e1.printStackTrace();
			fail("Error generating test code");
		}

		// Build the list of SWCs to compile against on the library path.
		List<String> swcs = new ArrayList<String>();
    	String customSwcPath = FilenameNormalization.normalize("target/custom.swc");
		swcs.add(customSwcPath);
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
		String libraryPath = "-library-path=" + StringUtils.join(swcs.toArray(new String[swcs.size()]), ",");
		
		List<String> args = new ArrayList<String>();
		args.add("-debug=true");
		// Force the testsuite to use en_US as locale, otherwise
		// the testsuite will only pass on systems with en_US as
		// locale.
		args.add("-locale=en_US");
        if (hasFlashPlayerGlobal)
        	args.add("-external-library-path=" + testAdapter.getPlayerglobal().getPath());
        else {
        	String jsSwcPath = FilenameNormalization.normalize("../compiler-externc/target/js.swc");
        	args.add("-external-library-path=" + jsSwcPath);
        }
		args.add(libraryPath);
		args.add("-namespace=" + NAMESPACE_2009 + "," + testAdapter.getFlexManifestPath("mxml-2009"));
    	String customManifestPath = FilenameNormalization.normalize("src/test/resources/custom-manifest.xml");
		args.add("-namespace+=" + NAMESPACE_TEST + "," + customManifestPath);
		if (otherOptions != null)
		{
			Collections.addAll(args, otherOptions);
		}
		args.add(tempMXMLFile.getAbsolutePath());

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
		StringBuilder sb = new StringBuilder("Unxpected compilation problems:\n");
		for (ICompilerProblem problem : problems)
		{
			sb.append(problem.toString());
			sb.append('\n');
		}
		assertThat(sb.toString(), exitCode, is(0));

		// Run the SWF in the standalone player amd wait until the SWF calls System.exit().
		String swf = FilenameNormalization.normalize(tempMXMLFile.getAbsolutePath());
		swf = swf.replace(".mxml", ".swf");
		if (hasFlashPlayerExecutable)
		{
			File playerExecutable = testAdapter.getFlashplayerDebugger();
			String[] runArgs = new String[] { playerExecutable.getPath(), swf };
			try
			{
				System.out.println("Executing test:\n" + Arrays.toString(runArgs));
	
				// TODO: Hack to add the directory containing the temp swf to the flashplayer trust.
				new FlashplayerSecurityHandler().trustFile(tempMXMLFile.getParentFile());
	
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
					className = className.replace(".", "_");
					String resultName = className + "_" + methodName + "_swfdump.xml";
					File resultFile = new File(testAdapter.getUnitTestBaseDir(), "swfdumps/" + resultName);
					if (!resultFile.exists())
					{
						if (generateResultFile)
							ASFeatureTestsBase.writeResultFile(resultFile, tempMXMLFile.getName(), ours);
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
						String tempClassName = tempMXMLFile.getName();
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
	
	protected void compileAndRun(String mxml)
	{
		compileAndRun(mxml, false, false, false, null);
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

}
