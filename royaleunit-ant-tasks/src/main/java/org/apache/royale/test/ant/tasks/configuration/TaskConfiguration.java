/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.royale.test.ant.tasks.configuration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.royale.test.ant.LoggingUtil;
import org.apache.royale.test.ant.tasks.types.LoadConfig;

public class TaskConfiguration
{
    private final String DEFAULT_WORKING_PATH = ".";
    private final String DEFAULT_REPORT_PATH = ".";
    private final List<String> VALID_PLAYERS = Arrays.asList(new String[]{"flash", "air", "html"});
    
    private String player = "flash";
    private File reportDir = null;
    private File workingDir = null;
    private boolean verbose = false;
    private File royaleHome = null;
    
    private Project project;
    private CompilationConfiguration compilationConfiguration;
    private TestRunConfiguration testRunConfiguration;
    
    public TaskConfiguration(Project project)
    {
        this.project = project;
        this.compilationConfiguration = new CompilationConfiguration();
        this.testRunConfiguration = new TestRunConfiguration();
        
        if(project.getProperty("ROYALE_HOME") != null)
        {
            this.royaleHome = new File(project.getProperty("ROYALE_HOME"));
        }
    }
    
    //Used to verify that a string is also a properly formatted URL
    //When determining if the passed 'swf' property value is remote or local this is crucial.
    protected boolean isValidURL(String urlStr ) {
         try {
             URL url = new URL( urlStr );
             LoggingUtil.log("my protocol " + url.getProtocol().toString() );
             if( url.getProtocol().toUpperCase().equals("HTTP") || url.getProtocol().toUpperCase().equals("HTTPS") ) {
                 LoggingUtil.log("Valid URL returning TRUE" );
                 return true;	    		
             } else {
                 //no protocol so this isn't a URL at all, it might a local path or an invalid address
                 LoggingUtil.log("Valid URL returning FALSE" );
                 return false;
             }
             
         }
         catch( MalformedURLException e ) {
             return false;
         }
    }
    
    public CompilationConfiguration getCompilationConfiguration()
    {
        return compilationConfiguration;
    }
    
    public TestRunConfiguration getTestRunConfiguration()
    {
        return testRunConfiguration;
    }

    public void setCommand(String commandPath)
    {
        if (commandPath != null && commandPath.length() > 0) {
            testRunConfiguration.setCommand(project.resolveFile(commandPath));
        }
    }
    
    public void setDisplay(int display)
    {
        testRunConfiguration.setDisplay(display);
    }

    public void setFailOnTestFailure(boolean failOnTestFailure)
    {
        testRunConfiguration.setFailOnTestFailure(failOnTestFailure);
    }

    public void setFailureProperty(String failureProperty)
    {
        testRunConfiguration.setFailureProperty(failureProperty);
    }
    
    public void addSource(FileSet fileset)
    {
        fileset.setProject(project);
        compilationConfiguration.addSource(fileset);
    }
    
    public void addTestSource(FileSet fileset)
    {
        fileset.setProject(project);
        compilationConfiguration.addTestSource(fileset);
    }
    
    public void addLibrary(FileSet fileset)
    {
        fileset.setProject(project);
        compilationConfiguration.addLibrary(fileset);
    }
    
    public void setHeadless(boolean headless)
    {
        testRunConfiguration.setHeadless(headless);
    }

    public void setLocalTrusted(boolean isLocalTrusted)
    {
        testRunConfiguration.setLocalTrusted(isLocalTrusted);
    }

    public void setPlayer(String player)
    {
        this.player = player;
    }

    public void setPort(int port)
    {
        testRunConfiguration.setPort(port);
    }

    public void setReportDir(String reportDirPath)
    {
        this.reportDir = project.resolveFile(reportDirPath);
    }

    public void setServerBufferSize(int serverBufferSize)
    {
        testRunConfiguration.setServerBufferSize(serverBufferSize);
    }

    public void setSocketTimeout(int socketTimeout)
    {
        testRunConfiguration.setSocketTimeout(socketTimeout);
    }

    public void setSwf(String swf)
    {
        //match the swf URL to see if it's a remote location, if so, set the url instead of swf.
        
        File localFile = project.resolveFile(swf);
        
        if( localFile.exists() ) {
            testRunConfiguration.setSwf(localFile);
            LoggingUtil.log("Local path to SWF was given and SWF property will be used.");  
        } else if( isValidURL( swf ) ) {
            testRunConfiguration.setUrl(swf);
            LoggingUtil.log("Remote path to SWF was given, setting URL property instead of SWF");
        } else {
            LoggingUtil.log("SWF and URL not set, file did not resolve to a local path or a remote path, please verify your format and try again.");
        }
        
    }
    
    public void setSwf(File swf)
    {
        testRunConfiguration.setSwf(swf);
    }
    
    public boolean isVerbose()
    {
        return verbose;
    }

    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
        LoggingUtil.VERBOSE = verbose;
    }
    
    public void setWorkingDir(String workingDirPath)
    {
        this.workingDir = project.resolveFile(workingDirPath);
    }
    
    public boolean shouldCompile()
    {
        File swf = testRunConfiguration.getSwf();
        boolean noTestSources = !compilationConfiguration.getTestSources().provided();
        return !noTestSources && (swf == null || !swf.exists());
    }
    
    public void verify() throws BuildException
    {
        validateSharedProperties();
        
        if(shouldCompile())
        {
            compilationConfiguration.validate();
        }
        
        testRunConfiguration.validate();
        
        propagateSharedConfiguration();
    }

    protected void validateSharedProperties() throws BuildException
    {
        LoggingUtil.log("Validating task attributes ...");
        
        if(!VALID_PLAYERS.contains(player))
        {
            throw new BuildException("The provided 'player' property value [" + player + "] must be either of the following values: " + VALID_PLAYERS.toString() + ".");
        }
        
        File swf = testRunConfiguration.getSwf();
        boolean noTestSources = !compilationConfiguration.getTestSources().provided();
        String swfURL = testRunConfiguration.getUrl();
        
        //Check to make sure we have a valid swf, testsource or remote url before proceeding.
        //Otherwise, notify the user to fix this before continuing.
        if ((swf == null || !swf.exists()) && noTestSources && ( swfURL == null || swfURL.equals("") ) )
        {
            throw new BuildException("The provided 'swf' property value [" + (swf == null ? "" : swf.getPath()) + "] could not be found or is not a valid remote URL.");
        }
        
        //Including a check for the swfURL
        if( ( swf == null ) && (swfURL != null && swfURL != "") && testRunConfiguration.isLocalTrusted() ) 
        {
           throw new BuildException("The provided 'swf' property points to a remote location.  Please set localTrusted = false or change the location of your swf to a local path.");
        }
        
        if(swf != null && !noTestSources)
        {
            throw new BuildException("Please specify the 'swf' property or use the 'testSource' element(s), but not both.");
        }
        
        //if we can't find the ROYALE_HOME and we're using ADL or compilation
        if((royaleHome == null || !royaleHome.exists()) && (new String("air").equals(testRunConfiguration.getPlayer()) || shouldCompile()))
        {
            throw new BuildException("Please specify, or verify the location for, the ROYALE_HOME property.  "
                    + "It is required when testing with 'air' as the player or when using the 'testSource' element.  "
                    + "It should point to the installation directory for an Apache Royale SDK.");
        }
    }
    
    protected void propagateSharedConfiguration()
    {
        LoggingUtil.log("Generating default values ...");
        
        //setup player
        compilationConfiguration.setPlayer(player);
        testRunConfiguration.setPlayer(player);
        
        //set ROYALE_HOME property to respective configs
        compilationConfiguration.setRoyaleHome(royaleHome);
        testRunConfiguration.setRoyaleHome(royaleHome);
        
        //create working directory if needed
        if (workingDir == null || !workingDir.exists())
        {
            workingDir = project.resolveFile(DEFAULT_WORKING_PATH);
            LoggingUtil.log("Using default working dir [" + workingDir.getAbsolutePath() + "]");
        }

        //create directory just to be sure it exists, already existing dirs will not be overwritten
        workingDir.mkdirs();
        
        compilationConfiguration.setWorkingDir(workingDir);
        
        //create report directory if needed
        if (reportDir == null || !reportDir.exists())
        {
            reportDir = project.resolveFile(DEFAULT_REPORT_PATH);
            LoggingUtil.log("Using default reporting dir [" + reportDir.getAbsolutePath() + "]");
        }

        //create directory just to be sure it exists, already existing dirs will not be overwritten
        reportDir.mkdir();
        
        testRunConfiguration.setReportDir(reportDir);
    }
    
    public void setDebug(boolean value)
    {
        compilationConfiguration.setDebug(value);
    }

    public void setLoadConfig(LoadConfig loadconfig)
    {
        compilationConfiguration.setLoadConfig(loadconfig);
    }

}
