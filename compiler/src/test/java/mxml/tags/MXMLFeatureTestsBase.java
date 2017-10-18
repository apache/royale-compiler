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
import org.apache.royale.utils.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class MXMLFeatureTestsBase
{
	private static final String NAMESPACE_2009 = "http://ns.adobe.com/mxml/2009";
    
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
		// Force the testsuite to use en_US as locale, otherwise
		// the testsuite will only pass on systems with en_US as
		// locale.
		args.add("-locale=en_US");
		args.add("-external-library-path=" + testAdapter.getPlayerglobal().getPath());
		args.add(libraryPath);
		args.add("-namespace=" + NAMESPACE_2009 + "," + testAdapter.getFlexManifestPath("mxml-2009"));
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

		// Check the existence of the flashplayer executable
		File playerExecutable = testAdapter.getFlashplayerDebugger();
		if(!playerExecutable.isFile() || !playerExecutable.exists()) {
			fail("The flashplayer executable " + testAdapter.getFlashplayerDebugger().getPath() + " doesn't exist.");
		}

		// Run the SWF in the standalone player amd wait until the SWF calls System.exit().
		String swf = FilenameNormalization.normalize(tempMXMLFile.getAbsolutePath());
		swf = swf.replace(".mxml", ".swf");
		String[] runArgs = new String[] { testAdapter.getFlashplayerDebugger().getPath(), swf };
		try
		{
			System.out.println("Executing test:\n" + Arrays.toString(runArgs));
			exitCode = executeCommandWithTimeout(runArgs, 40);
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
