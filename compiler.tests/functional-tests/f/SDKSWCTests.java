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

package f;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.flex.compiler.clients.COMPC;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests to compile the SWCs of the Flex SDK.
 * <p>
 * The projects to compile are in the <code>frameworks/projects</code> directory
 * referenced by the <code>FLEX_HOME</code> environment variable.
 * Each project has a config file which the <code>COMPC</code> uses to compile the SWC.
 * 
 * @author Gordon Smith
 */
public class SDKSWCTests
{
	private void compileSWC(String projectName)
	{
		// Construct a command line which simply loads the project's config file.
		String playerglobalHome = System.getenv("PLAYERGLOBAL_HOME");
		assertNotNull("Environment variable PLAYERGLOBAL_HOME is not set", playerglobalHome);
		
		String flexHome = System.getenv("FLEX_HOME");
		assertNotNull("Environment variable FLEX_HOME is not set", flexHome);
		
		String output = null;
		try
		{
			output = File.createTempFile(projectName, ".swc").getAbsolutePath();
		}
		catch (IOException e)
		{
		}

		String configFile = flexHome + "/frameworks/projects/" + projectName + "/compile-config.xml";
		String[] args = new String[]
		{
			"-load-config=" + configFile,
			"+env.PLAYERGLOBAL_HOME=" + playerglobalHome,
			"+playerglobal.version=11.1",
			"-define=CONFIG::performanceInstrumentation,false",
			"-output=" + output
		};
		
		// Run the COMPC client with the specified command line.
		COMPC compc = new COMPC();
		compc.mainNoExit(args);
		
		// Check that the SWC compiled cleanly.
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		for (ICompilerProblem problem : compc.getProblems().getFilteredProblems())
		{
			problems.add(problem);
		}
		assertThat(problems.size(), is(0));
	}
	
	@Test
	public void frameworkSWC()
	{
		compileSWC("framework");
	}
	
	@Ignore
	@Test
	public void rpcSWC()
	{
		compileSWC("rpc");
	}
	
	@Ignore
	@Test
	public void textLayoutSWC()
	{
		compileSWC("textLayout");
	}
	
	@Ignore
	@Test
	public void mxSWC()
	{
		compileSWC("mx");
	}
	
	@Ignore
	@Test
	public void sparkSWC()
	{
		compileSWC("spark");
	}
	
	// others...
}
