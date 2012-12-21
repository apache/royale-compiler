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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.clients.MXMLC;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.utils.EnvProperties;
import org.apache.flex.utils.FilenameNormalization;
import org.apache.flex.utils.StringUtils;


/**
 * Base class for MXML feature tests which compile MXML code with MXMLC and run it in the standalone Flash Player.
 * 
 * @author Gordon Smith
 */
public class MXMLFeatureTestsBase
{
	private static EnvProperties env = EnvProperties.initiate();
	
	private static final String PLAYERGLOBAL_SWC = FilenameNormalization.normalize(env.FPSDK + "\\11.1\\playerglobal.swc");
	
	private static final String FRAMEWORK_SWC = FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\framework.swc");
	private static final String FRAMEWORK_RB_SWC = FilenameNormalization.normalize(env.SDK + "\\frameworks\\locale\\en_US\\framework_rb.swc");
	
	private static final String RPC_SWC = FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\rpc.swc");
	private static final String RPC_RB_SWC = FilenameNormalization.normalize(env.SDK + "\\frameworks\\locale\\en_US\\rpc_rb.swc");

	private static final String SPARK_SWC = FilenameNormalization.normalize(env.SDK + "\\frameworks\\libs\\spark.swc");
	private static final String SPARK_RB_SWC = FilenameNormalization.normalize(env.SDK + "\\frameworks\\locale\\en_US\\spark_rb.swc");
	
	private static final String NAMESPACE_2009 = "http://ns.adobe.com/mxml/2009";
    
	private static final String MANIFEST_2009 = FilenameNormalization.normalize(env.SDK + "\\frameworks\\mxml-2009-manifest.xml");
    
    // The Ant script for compiler.tests copies a standalone player to the temp directory.
    private static final String FLASHPLAYER = FilenameNormalization.normalize("temp/FlashPlayer.exe");

	protected void compileAndRun(String mxml, boolean withFramework, boolean withRPC, boolean withSpark)
	{
		// Write the MXML into a temp file.
		String tempDir = FilenameNormalization.normalize("temp");
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
		}
		
		// Build the list of SWCs to compile against on the library path.
		List<String> swcs = new ArrayList<String>();
		if (withFramework)
		{
			swcs.add(FRAMEWORK_SWC);
			swcs.add(FRAMEWORK_RB_SWC);
		}
		if (withRPC)
		{
			swcs.add(RPC_SWC);
			swcs.add(RPC_RB_SWC);
		}
		if (withSpark)
		{
			swcs.add(SPARK_SWC);
			swcs.add(SPARK_RB_SWC);
		}
		String libraryPath = "-library-path=" + StringUtils.join(swcs.toArray(new String[0]), ",");
		
		// Use MXMLC to compile the MXML file against playerglobal.swc and possibly other SWCs.
		MXMLC mxmlc = new MXMLC();
		String[] args = new String[]
        {
			"-external-library-path=" + PLAYERGLOBAL_SWC,
			libraryPath,
			"-namespace=" + NAMESPACE_2009 + "," + MANIFEST_2009,
			tempMXMLFile.getAbsolutePath()
		};
		int exitCode = mxmlc.mainNoExit(args);
		
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
		args = new String[] { FLASHPLAYER, swf };
		try
		{
			Process process = Runtime.getRuntime().exec(args);
			process.waitFor();
			exitCode = process.exitValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	    // Check that the runtime exit code was 0, meaning that no asserts failed.
		assertThat(exitCode, is(0));
	}
	
	protected void compileAndRun(String mxml)
	{
		compileAndRun(mxml, false, false, false);
	}
}
