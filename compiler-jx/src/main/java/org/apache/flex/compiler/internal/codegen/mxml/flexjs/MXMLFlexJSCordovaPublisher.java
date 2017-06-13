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

package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.flex.compiler.clients.problems.ProblemQuery;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.internal.codegen.js.jsc.JSCPublisher;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.graph.GoogDepsWriter;
import org.apache.flex.compiler.internal.graph.GoogDepsWriterCordova;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.swc.ISWC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class MXMLFlexJSCordovaPublisher extends MXMLFlexJSPublisher
{
    public MXMLFlexJSCordovaPublisher(Configuration config, FlexJSProject project)
    {
        super(project, config);
    }
    
    private String cordova = "cordova";
    private String[] pathEnv = new String[3];
    
    private boolean needNewProject;

    @Override
    protected void setupOutputFolder()
    {
    	if (!outputFolder.exists())
    		needNewProject = true;
    	super.setupOutputFolder();
    }

    @Override
    public File getOutputFolder()
    {
    	File newOutputFolder = super.getOutputFolder();
    	
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows"))
        	cordova = "cordova.cmd";
        else
        {
        	File c = new File("/usr/local/bin/cordova");
        	if (c.exists())
        	{
        		cordova = "/usr/local/bin/cordova";
        		String home = System.getenv("HOME");
    			pathEnv[0] = "HOME=" + home;
    			File bash = new File(home + File.separator + ".bash_login");
        		String path = System.getenv("PATH");
        		String java = System.getenv("JAVA_HOME");
        		if (path == null || !path.contains("node_modules") || java == null)
        		{
        	        try
        	        {
        	            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(bash), "UTF8"));

        	            String line = in.readLine();

        	            while (line != null)
        	            {
        	                if (line.startsWith("export JAVA_HOME=") && java == null)
        	                {
        	                	java = line.substring(17);
        	                }
        	                else if (line.startsWith("export PATH="))
        	                {
        	                	if (path == null)
        	                		path = "";
        	                	String oldPath = path;
        	                	path = line.substring(12);
        	                	if (path.contains("$PATH"))
        	                	{
        	                		path = path.replace("$PATH", oldPath);
        	                	}
        	                }
        	                line = in.readLine();
        	            }

        	            in.close();
	                	if (!path.contains("/usr/local/bin"))
	                	{
	                		path += ":/usr/local/bin";
	                	}
        	        }
        	        catch (Exception e)
        	        {
        	            // nothing to see, move along...
        	        }
        		}
    			pathEnv[1] = "PATH=" + path;
    			pathEnv[2] = "JAVA_HOME=" + java;
        	}
        }
        
        final String projectName = FilenameUtils.getBaseName(configuration.getTargetFile());
        
        if (needNewProject)
        {
        	String[] execParts = new String[5];
        	execParts[0] = cordova;
        	execParts[1] = "create";
        	execParts[2] = "app";
        	execParts[3] = googConfiguration.getCordovaId();
        	execParts[4] = projectName;
	        try {
				Process p = Runtime.getRuntime().exec(execParts, pathEnv, newOutputFolder);
            	String line;
            	BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            	while ((line = input.readLine()) != null) {
            	    System.out.println(line);
            	}
            	input.close();
            	BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            	while ((line = error.readLine()) != null) {
            	    System.out.println(line);
            	}
				int ret = p.exitValue();
				System.out.println("cordova create returned " + ret);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
        }
    	newOutputFolder = new File(newOutputFolder, "app");
    	newOutputFolder = new File(newOutputFolder, "www");
    	outputFolder = newOutputFolder;
    	return newOutputFolder;
    }
    
    @Override
    public boolean publish(ProblemQuery problems) throws IOException
    {
    	if (super.publish(problems))
    	{
    		cordovaPublish();
    	}
    	
    	return true;
    }
    
    private void cordovaPublish()
    {
        // The "intermediate" is the "js-debug" output.
        final File intermediateDir = outputFolder;
        final String projectName = FilenameUtils.getBaseName(configuration.getTargetFile());
    	File projectDir = intermediateDir.getParentFile();

        // The "release" is the "js-release" directory.
        File releaseDir = new File(outputParentFolder, FLEXJS_RELEASE_DIR_NAME);

        List<String> platforms = googConfiguration.getCordovaPlatforms();
        for (String platform : platforms)
        {
        	File platformDir = new File(intermediateDir, "platforms" + File.separator + platform);
        	if (!platformDir.exists())
        	{
            	String[] execParts = new String[4];
            	execParts[0] = cordova;
            	execParts[1] = "platform";
            	execParts[2] = "add";
            	execParts[3] = platform;
                try {
                	Process p = Runtime.getRuntime().exec(execParts, pathEnv, outputFolder.getParentFile());
                	String line;
                	BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                	while ((line = input.readLine()) != null) {
                	    System.out.println(line);
                	}
                	input.close();
                	BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                	while ((line = error.readLine()) != null) {
                	    System.out.println(line);
                	}
    				int ret = p.exitValue();
    				System.out.println("cordova platform returned " + ret);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
				}		
        		
        	}
        }
        
        for (String plugin : plugins)
        {
            try {
            	String[] execParts = new String[4];
            	execParts[0] = cordova;
            	execParts[1] = "plugin";
            	execParts[2] = "add";
            	execParts[3] = plugin;
            	Process p = Runtime.getRuntime().exec(execParts, pathEnv, outputFolder.getParentFile());
            	String line;
            	BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            	while ((line = input.readLine()) != null) {
            	    System.out.println(line);
            	}
            	input.close();
            	BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            	while ((line = error.readLine()) != null) {
            	    System.out.println(line);
            	}
				int ret = p.exitValue();
				System.out.println("cordova plugin returned " + ret);
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
			}		
        }
        
        try {
        	String[] execParts = new String[2];
        	execParts[0] = cordova;
        	execParts[1] = "build";
        	Process p = Runtime.getRuntime().exec(execParts, pathEnv, outputFolder.getParentFile());
        	String line;
        	BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        	while ((line = input.readLine()) != null) {
        	    System.out.println(line);
        	}
        	input.close();
        	BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        	while ((line = error.readLine()) != null) {
        	    System.out.println(line);
        	}
			int ret = p.exitValue();
			System.out.println("cordova build returned " + ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }
    
    private ArrayList<String> plugins;
    
    protected GoogDepsWriter getGoogDepsWriter(File intermediateDir, 
			String projectName, 
			JSGoogConfiguration googConfiguration, 
			List<ISWC> swcs)
	{
    	plugins = new ArrayList<String>();
    	return new GoogDepsWriterCordova(intermediateDir, projectName, googConfiguration, swcs, plugins);
	}

}
