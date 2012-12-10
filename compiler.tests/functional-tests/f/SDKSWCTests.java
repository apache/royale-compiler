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
		
		String airHome = System.getenv("AIR_HOME");
		assertNotNull("Environment variable AIR_HOME is not set", airHome);
		
		String output = null;
		String outputSwcName = projectName;
		try
		{
			if(outputSwcName.length() < 3)
				outputSwcName = "_" + outputSwcName;
			output = File.createTempFile(outputSwcName, ".swc").getAbsolutePath();
		}
		catch (IOException e)
		{
		}

		String configFile = flexHome + "/frameworks/projects/" + projectName + "/compile-config.xml";
		String[] args = new String[]
		{
			"-load-config=" + configFile,
			"+env.PLAYERGLOBAL_HOME=" + playerglobalHome,
			"+env.AIR_HOME=" + airHome,
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
	
	@Ignore
	@Test
	public void advancedgridsSWC()
	{
		compileSWC("advancedgrids");
	}
	
	@Ignore
	@Test
	public void airframeworkSWC()
	{
		compileSWC("airframework");
	}
	
	@Ignore
	@Test
	public void airsparkSWC()
	{
		compileSWC("airspark");
	}
	
	@Test
	public void apacheSWC()
	{
		compileSWC("apache");
	}
	
	@Test
	public void authoringsupportSWC()
	{
		compileSWC("authoringsupport");
	}
	
	@Test
	public void automationSWC()
	{
		compileSWC("automation");
	}
	
	@Ignore
	@Test
	public void automation_agentSWC()
	{
		compileSWC("automation_agent");
	}
	
	@Ignore
	@Test
	public void automation_airSWC()
	{
		compileSWC("automation_air");
	}
	
	@Ignore	
	@Test
	public void automation_airsparkSWC()
	{
		compileSWC("automation_airspark");
	}
	
	@Ignore
	@Test
	public void automation_dmvSWC()
	{
		compileSWC("automation_dmv");
	}
	
	@Test
	public void automation_flashflexkitSWC()
	{
		compileSWC("automation_flashflexkit");
	}
	
	@Ignore
	@Test
	public void automation_sparkSWC()
	{
		compileSWC("automation_spark");
	}
	
	@Ignore
	@Test
	public void chartsSWC()
	{
		compileSWC("charts");
	}
	
	@Test
	public void coreSWC()
	{
		compileSWC("core");
	}
	
	@Test
	public void flash_integrationSWC()
	{
		compileSWC("flash-integration");
	}
	
	@Test
	public void frameworkSWC()
	{
		compileSWC("framework");
	}
	
	@Test
	public void haloSWC()
	{
		compileSWC("halo");
	}
	
	@Test
	public void mobilecomponentsSWC()
	{
		compileSWC("mobilecomponents");
	}
	
	@Ignore
	@Test
	public void mobilethemeSWC()
	{
		compileSWC("mobiletheme");
	}
	
	@Ignore
	@Test
	public void mxSWC()
	{
		compileSWC("mx");
	}
	
	@Ignore
	@Test
	public void playerglobalSWC()
	{
		compileSWC("playerglobal");
	}
	
	@Test
	public void rpcSWC()
	{
		compileSWC("rpc");
	}
	
	@Ignore
	@Test
	public void sparkSWC()
	{
		compileSWC("spark");
	}
	
	@Test
	public void spark_dmvSWC()
	{
		compileSWC("spark_dmv");
	}
	
	@Ignore
	@Test
	public void sparkskinsSWC()
	{
		compileSWC("sparkskins");
	}
	
	@Ignore
	@Test
	public void textLayoutSWC()
	{
		compileSWC("textLayout");
	}
	
	@Ignore
	@Test
	public void toolSWC()
	{
		compileSWC("tool");
	}	

	@Ignore
	@Test
	public void tool_airSWC()
	{
		compileSWC("tool_air");
	}
	
	@Ignore
	@Test
	public void wireframeSWC()
	{
		compileSWC("wireframe");
	}

}
