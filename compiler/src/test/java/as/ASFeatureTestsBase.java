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

import org.apache.flex.compiler.clients.MXMLC;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.utils.*;
import utils.FlashplayerSecurityHandler;

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
 * Base class for AS feature tests which compile AS code with MXMLC and run it in the standalone Flash Player.
 * Copied and modified from MXMLFeatureTestsBase.java
 */
public class ASFeatureTestsBase
{
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
        args.add("-external-library-path=" + testAdapter.getPlayerglobal().getPath());
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
        StringBuilder sb = new StringBuilder(checkExitCode ? "Unexpected compilation problems:\n" : "");
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
		File playerExecutable = testAdapter.getFlashplayerDebugger();
		if(!playerExecutable.isFile() || !playerExecutable.exists()) {
			fail("The flashplayer executable " + testAdapter.getFlashplayerDebugger().getPath() + " doesn't exist.");
		}

		// Run the SWF in the standalone player amd wait until the SWF calls System.exit().
		String swf = FilenameNormalization.normalize(tempASFile.getAbsolutePath());
		swf = swf.replace(".as", ".swf");
		String[] runArgs = new String[] { testAdapter.getFlashplayerDebugger().getPath(), swf };
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
	 * Combines various code snippets to make a complete one-file MXML Sprite-based application.
	 */
    protected String getAS(String[] imports, String[] declarations, String[] testCode, String[] extraCode)
    {
        String[] template = new String[]
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
        String source = StringUtils.join(template, "\n");
        source = source.replace("%1", StringUtils.join(imports, "\n"));
        source = source.replace("%2", StringUtils.join(declarations, "\n        "));
        source = source.replace("%3", StringUtils.join(testCode, "\n"));
        source = source.replace("%4", StringUtils.join(extraCode, "\n"));
        return source;
    }
    

}
