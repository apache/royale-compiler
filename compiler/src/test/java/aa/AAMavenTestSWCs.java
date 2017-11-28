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

package aa;

import org.apache.commons.io.FileUtils;
import org.apache.royale.compiler.clients.COMPC;
import org.apache.royale.compiler.clients.EXTERNC;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.io.SWFDump;
import org.apache.royale.utils.ITestAdapter;
import org.apache.royale.utils.TestAdapterFactory;

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
import org.junit.Test;

/**
 * Base class for AS feature tests which compile AS code with MXMLC and run it in the standalone Flash Player.
 * Copied and modified from MXMLFeatureTestsBase.java
 */
public class AAMavenTestSWCs
{
	private static boolean generateResultFile = false;
	
	public AAMavenTestSWCs()
	{
	}
	
    private void compileSWC(File outputFile, File configFile)
    {
        // Write the MXML into a temp file.
        ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();
        
        String output = outputFile.getAbsolutePath();
                
        String[] args = new String[]
        {
            "-load-config+=" + configFile.getAbsolutePath(),
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
    public void AA_JS_EXTERNC()
    {
        // Write the MXML into a temp file.
        ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    	File externcDir = new File(testAdapter.getUnitTestBaseDir(), "../../../compiler-externc");

		String[] args = new String[1];
		args[0] = "-load-config+=" + new File(externcDir, "src/test/config/externc-config.xml").getAbsolutePath();
        // Run the COMPC client with the specified command line.
        EXTERNC externc = new EXTERNC();
        externc._mainNoExit(args);
        
        // Check that the SWC compiled cleanly.
        List<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();
        for (ICompilerProblem problem : externc.problems.getFilteredProblems())
        {
            problems.add(problem);
        }
        assertThat(problems.size(), is(0));
    }
    
    @Test
    public void AB_JS_SWC()
    {
        // Write the MXML into a temp file.
        ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    	File externcDir = new File(testAdapter.getUnitTestBaseDir(), "../../../compiler-externc");
    	File outputFile = new File(externcDir, "target/js.swc");
    	try {
    	FileUtils.copyFile(new File(externcDir, "src/test/config/compile-as-config.xml"), 
						new File(externcDir, "target/compile-as-config.xml"));
    	}
		catch (IOException e)
		{
		}

    	compileSWC(outputFile, new File(externcDir, "target/compile-as-config.xml"));
        assertThat(outputFile.exists(), is(true));
    }
    
    @Test
    public void AC_Custom_SWC()
    {
        // Write the MXML into a temp file.
        ITestAdapter testAdapter = TestAdapterFactory.getTestAdapter();

    	File baseDir = new File(testAdapter.getUnitTestBaseDir(), "../../../compiler");
    	File outputFile = new File(baseDir, "target/custom.swc");
    	try {
    	FileUtils.copyFile(new File(baseDir, "src/test/config/compile-as-config.xml"), 
						new File(baseDir, "target/compile-as-config.xml"));
    	}
		catch (IOException e)
		{
		}
    	compileSWC(outputFile, new File(baseDir, "target/compile-as-config.xml"));
        assertThat(outputFile.exists(), is(true));
    }
}

