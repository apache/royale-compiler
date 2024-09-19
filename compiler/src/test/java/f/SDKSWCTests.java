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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.royale.compiler.clients.COMPC;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.utils.EnvProperties;
import org.junit.Test;

import com.google.common.collect.ObjectArrays;


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
	private static EnvProperties env = EnvProperties.initiate();
	
	private static final String TEXTLAYOUT_NAME = "textLayout";
	
	private String[] extraArgs = new String[]{}; 
	
	private void compileSWC(String projectName)
	{
		// Construct a command line which simply loads the project's config file.
		assertNotNull("FLEX_HOME not set in unittest.properties", env.SDK);
		assertNotNull("PLAYERGLOBAL_HOME not set in unittest.properties", env.FPSDK);
		assertNotNull("AIR_HOME not set in unittest.properties", env.AIRSDK);
        assertNotNull("TLF_HOME not set in unittest.properties", env.TLF);
		
		System.setProperty("royalelib", env.SDK + "/frameworks");
		
		String output = null;
		String outputSwcName = projectName;
		try
		{
			if(outputSwcName.length() < 3)
				outputSwcName = "_" + outputSwcName;
			File tmpFile = File.createTempFile(outputSwcName, ".swc");
			tmpFile.deleteOnExit();
			output = tmpFile.getAbsolutePath();
		}
		catch (IOException e)
		{
		}

		String configFile;
		if (projectName.equals(TEXTLAYOUT_NAME))
		{
		    configFile = env.TLF + "/compile-config.xml";
		}
		else
		{
		    configFile = env.SDK + "/frameworks/projects/" + projectName + "/compile-config.xml";
		}
		
		String[] baseArgs = new String[]
		{
			"-load-config+=" + configFile,
			"+env.PLAYERGLOBAL_HOME=" + env.FPSDK,
			"+playerglobal.version=" + env.FPVER,
			"-output=" + output
		};
		
		String [] allArgs = ObjectArrays.concat(baseArgs, extraArgs, String.class);
		
		// Run the COMPC client with the specified command line.
		COMPC compc = new COMPC();
		compc.mainNoExit(allArgs);
		
		// Check that the SWC compiled cleanly.
		List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
		for (ICompilerProblem problem : compc.getProblems().getFilteredProblems())
		{
			problems.add(problem);
		}
		assertThat(problems.size(), is(0));
	}
	
	@Test
	public void advancedgridsSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };

		compileSWC("advancedgrids");
	}
	
	@Test
	public void airframeworkSWC()
	{
        extraArgs = new String[]
        {
            "+env.AIR_HOME=" + env.AIRSDK,
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("airframework");
	}
	
	@Test
	public void airsparkSWC()
	{
        extraArgs = new String[]
        {
            "+env.AIR_HOME=" + env.AIRSDK,
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("airspark");
	}
	
	@Test
	public void apacheSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("apache");
	}
	
	@Test
	public void authoringsupportSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("authoringsupport");
	}
	
	@Test
	public void automationSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("automation");
	}
	
	@Test
	public void automation_agentSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.DuplicateQNameInSourcePathProblem",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("automation_agent");
	}
	
	@Test
	public void automation_airsparkSWC()
	{
        extraArgs = new String[]
        {
            "+env.AIR_HOME=" + env.AIRSDK,
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("automation_airspark");
	}
	
    @Test
    public void automation_airSWC()
    {
        extraArgs = new String[]
        {
            "+env.AIR_HOME=" + env.AIRSDK,
            "-ignore-problems+=org.apache.royale.compiler.problems.DuplicateQNameInSourcePathProblem",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
        compileSWC("automation_air");
    }
    
	@Test
	public void automation_dmvSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.DuplicateQNameInSourcePathProblem",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("automation_dmv");
	}
	
	@Test
	public void automation_flashflexkitSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("automation_flashflexkit");
	}
	
	@Test
	public void automation_sparkSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("automation_spark");
	}
	
	@Test
	public void chartsSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
            "-locale=",
        };
        
		compileSWC("charts");
	}
	
    @Test
    public void coreSWC()
    {
        extraArgs = new String[]
        {
            "-load-config+=" + env.SDK + "/frameworks/projects/framework/framework-config.xml",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
        compileSWC("core");
    }
    
    @Test
    public void mxSWC()
    {
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
            "-locale="
        };
        
        compileSWC("mx");
    }
    
    @Test
    public void experimentalSWC() // WARNINGS -> FLEX-33731
    {
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.DuplicateSkinStateProblem",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
        compileSWC("experimental");
    }
    
	@Test
	public void flash_integrationSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("flash-integration");
	}
	
	@Test
	public void frameworkSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
            "-load-config+=" + env.SDK + "/frameworks/projects/framework/framework-config.xml"
        };
        
		compileSWC("framework");
	}
	
	@Test
	public void haloSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("halo");
	}
	
	@Test
	public void mobilecomponentsSWC()
	{
        extraArgs = new String[]
        {
            "+env.AIR_HOME=" + env.AIRSDK,
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("mobilecomponents");
	}
	
	@Test
	public void mobilethemeSWC() // WARNINGS -> FLEX-33305
	{
        extraArgs = new String[]
        {
             "-ignore-problems+=org.apache.royale.compiler.problems.NoDefinitionForSWCDependencyProblem",
			 "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("mobiletheme");
	}
	
	/*
	erikdebruin: the playerglobal project doesn't contain source that needs to
	             be compiled with COMPC

	@Test
	public void playerglobalSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("playerglobal");
	}
	*/
	
	@Test
	public void rpcSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("rpc");
	}
	
	@Test
	public void spark_dmvSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("spark_dmv");
	}
	
	@Test
	public void sparkskinsSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
		compileSWC("sparkskins");
	}
	
	@Test
    public void sparkSWC()
    {
		// TODO: DuplicateClassDefinitionProblem is actually an error
		// the duplicate class is a generated embed class with a name that
		// starts with: framework_swc/Assets_swf$
		// the source path and containing file path are both equal.
		// either the SWC defines the same type twice, or there's a bug in the
		// compiler code that is loading the same SWC twice.
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.DuplicateSkinStateProblem",
			"-ignore-problems+=org.apache.royale.compiler.problems.NullUsedWhereOtherExpectedProblem",
			"-ignore-problems+=org.apache.royale.compiler.problems.NonBooleanUsedWhereBooleanExpectedProblem",
			"-ignore-problems+=org.apache.royale.compiler.problems.DuplicateClassDefinitionProblem",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };

        compileSWC("spark");
    }
    
	@Test
	public void textLayoutSWC()
	{
        extraArgs = new String[]
        {
            "+source.dir=./textlayout",
            "-define=CONFIG::debug,false",
            "-define=CONFIG::release,true",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
	            
        compileSWC(TEXTLAYOUT_NAME);
	}

    @Test
    public void tool_airSWC()
    {
        extraArgs = new String[]
        {
            "+env.AIR_HOME=" + env.AIRSDK,
            "-ignore-problems+=org.apache.royale.compiler.problems.DuplicateQNameInSourcePathProblem",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
        compileSWC("tool_air");
    }
	
	@Test
	public void toolSWC()
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.DuplicateQNameInSourcePathProblem",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("tool");
	}	
	
	@Test
	public void wireframeSWC() // WARNINGS -> FLEX-33310
	{
        extraArgs = new String[]
        {
            "-ignore-problems+=org.apache.royale.compiler.problems.DuplicateSkinStateProblem",
            "-ignore-problems+=org.apache.royale.compiler.problems.ThisUsedInClosureProblem",
        };
        
		compileSWC("wireframe");
	}

}
