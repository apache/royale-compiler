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
package org.apache.royale.test.ant.tasks;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FilterSet;
import org.apache.tools.ant.types.FilterSetCollection;
import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.URLResource;
import org.apache.tools.ant.util.ResourceUtils;
import org.apache.royale.test.ant.LoggingUtil;
import org.apache.royale.test.ant.tasks.configuration.CompilationConfiguration;

public class Compilation
{
    private final String BASIC_APPLICATION_CLASS = "Application";
    private final String MXML2006_PREFIX = "mx";
    private final String MXML2006_NAMESPACE="xmlns:" + MXML2006_PREFIX + "=\"http://www.adobe.com/2006/mxml\"";
    private final String MXML2009_PREFIX = "fx";
    private final String MXML2009_NAMESPACE="xmlns:" + MXML2009_PREFIX + "=\"http://ns.adobe.com/mxml/2009\"";
    private final String BASIC_PREFIX = "js";
    private final String BASIC_NAMESPACE = "xmlns:" + BASIC_PREFIX + "=\"library://ns.apache.org/royale/basic\"";
    private final String CI_LISTENER = "CIListener";
    private final String AIR_CI_LISTENER = "AirCIListener";
    private final String TESTRUNNER_TEMPLATE = "TestRunner.template";
    private final String TESTRUNNER_FILE = "TestRunner.mxml";
    private final String MXMLC_RELATIVE_PATH = "lib/mxmlc.jar";
    private final String FRAMEWORKS_RELATIVE_PATH = "frameworks";
    private final String SWF_FILENAME = "TestRunner.swf";
    
    private CompilationConfiguration configuration;
    private Project project;
    private String mxmlcPath;
    
    public Compilation(Project project, CompilationConfiguration configuration)
    {
        this.project = project;
        this.configuration = configuration;
        mxmlcPath = configuration.getRoyaleHome().getAbsolutePath() + File.separatorChar + MXMLC_RELATIVE_PATH;
    }
    
    public File compile() throws BuildException
    {
        configuration.log();

        File runnerFile = generateTestRunnerFromTemplate(configuration.getWorkingDir());
        File finalFile = new File(configuration.getWorkingDir().getAbsolutePath() + File.separatorChar + SWF_FILENAME);
        
        Java compilationTask = createJavaTask(runnerFile, finalFile);
        LoggingUtil.log("Compiling test classes: [" + configuration.getTestSources().getCanonicalClasses(", ") + "]", true);
        LoggingUtil.log(compilationTask.getCommandLine().describeCommand());
        
        if(compilationTask.executeJava() != 0)
        {
            throw new BuildException("Compilation failed:\n" + project.getProperty("MXMLC_ERROR"));
        }
        
        return finalFile;
    }
    
    private File generateTestRunnerFromTemplate(File workingDir) throws BuildException
    {
        try
        {
            int sdkVersion = getSDKVersion();
            
            String namespaces = MXML2009_NAMESPACE + "\n" + BASIC_NAMESPACE;
            String ciListener = configuration.getPlayer().equals("flash") ? CI_LISTENER : AIR_CI_LISTENER;
            
            File runner = new File(workingDir.getAbsolutePath() + File.separatorChar + TESTRUNNER_FILE);
            
            //Template location in JAR
            URLResource template = new URLResource(getClass().getResource("/" + TESTRUNNER_TEMPLATE));
            
            //Create tokens to filter
            FilterSet filters = new FilterSet();
            filters.addFilter("APPLICATION_PREFIX", BASIC_PREFIX);
            filters.addFilter("APPLICATION_CLASS", BASIC_APPLICATION_CLASS);
            filters.addFilter("NAMESPACES", namespaces);
            filters.addFilter("MXML_PREFIX", MXML2009_PREFIX);
            filters.addFilter("CI_LISTENER_CLASS", ciListener);
            filters.addFilter("CLASS_REFS", configuration.getTestSources().getClasses());
            filters.addFilter("IMPORT_REFS", configuration.getTestSources().getImports());
            
            //Copy descriptor template to SWF folder performing token replacement
            ResourceUtils.copyResource(
                template,
                new FileResource(runner),
                new FilterSetCollection(filters),
                null,
                true,
                false,
                null,
                null,
                project
            );
            
            LoggingUtil.log("Created test runner at [" + runner.getAbsolutePath() + "]");
            
            return runner;
        }
        catch (Exception e)
        {
            throw new BuildException("Could not create test runner from template.", e);
        }
    }
    
    private int getSDKVersion()
    {
        String outputProperty = "SDK_VERSION";
        
        //Execute mxmlc to find SDK version number
        Java task = new Java();
        task.setFork(true);
        task.setFailonerror(true);
        task.setJar(new File(mxmlcPath));
        task.setProject(project);
        task.setDir(project.getBaseDir());
        task.setOutputproperty(outputProperty);
        
        Argument versionArgument = task.createArg();
        versionArgument.setValue("--version");
        
        task.execute();
        
        //Parse version number and return as int
        String output = project.getProperty(outputProperty);
        int prefixIndex = output.indexOf("Version ");
        int version = Integer.parseInt(output.substring(prefixIndex + 8, prefixIndex + 9));
        
        LoggingUtil.log("Found SDK version: " + version);
        
        return version;
    }
    
    private Java createJavaTask(File runnerFile, File finalFile)
    {
        String frameworksPath = configuration.getRoyaleHome().getAbsolutePath() + File.separatorChar + FRAMEWORKS_RELATIVE_PATH;
        
        Java task = new Java();
        task.setFork(true);
        task.setFailonerror(true);
        task.setJar(new File(mxmlcPath));
        task.setProject(project);
        task.setDir(project.getBaseDir());
        task.setMaxmemory("256M"); //MXMLC needs to eat
        task.setErrorProperty("MXMLC_ERROR");
        
        Argument royaleLibArgument = task.createArg();
        royaleLibArgument.setLine("+royalelib \"" + frameworksPath + "\"");
        
        if(configuration.getPlayer().equals("air"))
        {
            Argument airConfigArgument = task.createArg();
            airConfigArgument.setValue("+configname=air");
        }
        
        Argument outputFile = task.createArg();
        outputFile.setLine("-output \"" + finalFile.getAbsolutePath() + "\"");
        
        Argument sourcePath = task.createArg();
        sourcePath.setLine("-source-path " + configuration.getSources().getPathElements(" ") + " " + configuration.getTestSources().getPathElements(" "));
        
        determineLibraryPath( task );
      
        determineLoadConfigArgument( task );
         
        Argument debug = task.createArg();
        debug.setLine( "-debug=" + configuration.getDebug() );

        Argument headlessServer = task.createArg();
        headlessServer.setLine("-headless-server=true");
        
        
        Argument mainFile = task.createArg();
        mainFile.setValue(runnerFile.getAbsolutePath());
        
        return task;
    }
    
    
    private void determineLoadConfigArgument(Java java)
    {
        if(configuration.getLoadConfig() != null)
        {
            Argument argument = java.createArg();
            argument.setLine(configuration.getLoadConfig().getCommandLineArgument());
        }
    }

    private void determineLibraryPath(Java java)
    {
        if(!configuration.getLibraries().getPathElements(" -library-path+=").isEmpty())
        {
            Argument libraryPath = java.createArg();
            libraryPath.setLine("-library-path+=" + configuration.getLibraries().getPathElements(" -library-path+="));
        }
    }
    
}
