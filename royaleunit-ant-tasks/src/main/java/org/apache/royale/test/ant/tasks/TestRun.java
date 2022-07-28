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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.royale.test.ant.IRoyaleUnitServer;
import org.apache.royale.test.ant.LoggingUtil;
import org.apache.royale.test.ant.RoyaleUnitSocketServer;
import org.apache.royale.test.ant.RoyaleUnitSocketThread;
import org.apache.royale.test.ant.RoyaleUnitWebSocketServer;
import org.apache.royale.test.ant.launcher.commands.TestRunCommand;
import org.apache.royale.test.ant.launcher.commands.TestRunCommandFactory;
import org.apache.royale.test.ant.launcher.commands.player.AdlCommand;
import org.apache.royale.test.ant.launcher.commands.player.PlayerCommandFactory;
import org.apache.royale.test.ant.launcher.commands.playwright.PlaywrightCommand;
import org.apache.royale.test.ant.launcher.commands.process.ProcessCommand;
import org.apache.royale.test.ant.launcher.contexts.ExecutionContext;
import org.apache.royale.test.ant.launcher.contexts.ExecutionContextFactory;
import org.apache.royale.test.ant.launcher.contexts.PlaywrightExecutionContext;
import org.apache.royale.test.ant.launcher.contexts.ProcessExecutionContext;
import org.apache.royale.test.ant.report.Reports;
import org.apache.royale.test.ant.tasks.configuration.TestRunConfiguration;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.microsoft.playwright.Playwright;

public class TestRun
{
    private final String TRUE = "true";
    
    private TestRunConfiguration configuration;
    private Project project;
    
    private Reports reports;

    public TestRun(Project project, TestRunConfiguration configuration)
    {
        this.project = project;
        this.configuration = configuration;
        this.reports = new Reports();
    }
    
    public void run() throws BuildException
    {
        configuration.log();

        try
        {
            // run the execution context and runtime
            TestRunCommand command = obtainTestRunCommand();
            ExecutionContext context = obtainContext(command);
            
            //start the execution context
            context.start();

            // setup daemon
            Future<Object> daemon = setupSocketThread();
        
            //launch the runtime
            Process process = null;
            Playwright playwright = null;
            if (command instanceof ProcessCommand)
            {
                process = ((ProcessCommand)command).launch();
            }
            else if (command instanceof PlaywrightCommand)
            {
                playwright = ((PlaywrightCommand)command).launch();
            }

            try
            {
                // block until daemon is completely done with all test data
                daemon.get();
            }
            finally
            {
                //stop the execution context now that socket thread is done
                if (context instanceof ProcessExecutionContext)
                {
                    ((ProcessExecutionContext)context).stop(process);
                }
                else if (context instanceof PlaywrightExecutionContext)
                {
                    ((PlaywrightExecutionContext)context).stop(playwright);
                }
            }

            // print summaries and check for failure
            analyzeReports();

        } 
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }
    
    /**
     * Fetch the player command to execute the SWF.
     * 
     * @return PlayerCommand based on user config
     */
    protected ProcessCommand obtainPlayer()
    {
        // get command from factory
        ProcessCommand command = PlayerCommandFactory.createPlayer(
                configuration.getOs(), 
                configuration.getPlayer(), 
                configuration.getCommand(), 
                configuration.getCommandArgs(),
                configuration.isLocalTrusted());
        
        command.setProject(project);
        command.setSwf(configuration.getSwf());
        command.setUrl(configuration.getUrl());
        
        if(command instanceof AdlCommand) 
        {
           ((AdlCommand)command).setPrecompiledAppDescriptor(configuration.getPrecompiledAppDescriptor());
        }
        
        return command;
    }
    
    /**
     * Fetch the test run command to execute the tests.
     * 
     * @return TestRunCommand based on user config
     */
    protected TestRunCommand obtainTestRunCommand()
    {
        // get command from factory
        TestRunCommand command = TestRunCommandFactory.createCommand(
            configuration.getOs(), 
            configuration.getPlayer(), 
            configuration.getCommand(), 
            configuration.getCommandArgs(),
            configuration.isLocalTrusted());
        
        command.setProject(project);
        command.setSwf(configuration.getSwf());
        command.setUrl(configuration.getUrl());
        
        if(command instanceof AdlCommand) 
        {
           ((AdlCommand)command).setPrecompiledAppDescriptor(configuration.getPrecompiledAppDescriptor());
        }
        
        return command;
    }
    
    /**
     * 
     * @param player PlayerCommand which should be executed
     * @return Context to wrap the execution of the PlayerCommand
     */
    protected ExecutionContext obtainContext(TestRunCommand command)
    {
        ExecutionContext context = ExecutionContextFactory.createContext(
                command,
                configuration.getOs(), 
                configuration.isHeadless(), 
                configuration.getDisplay());

        context.setProject(project);
        
        return context;
    }
    
    /**
     * Create a server socket for receiving the test reports from RoyaleUnit. We
     * read and write the test reports inside of a Thread.
     */
    protected Future<Object> setupSocketThread()
    {
        LoggingUtil.log("Setting up server process ...");

        // Create server for use by thread
        IRoyaleUnitServer server = null;
        if(!"air".equals(configuration.getPlayer()) && !"flash".equals(configuration.getPlayer()))
        {
            server = new RoyaleUnitWebSocketServer(
                configuration.getPort(), configuration.getSocketTimeout());
        }
        else
        {
            server = new RoyaleUnitSocketServer(configuration.getPort(), 
                    configuration.getSocketTimeout(), configuration.getServerBufferSize(), 
                    configuration.usePolicyFile());
        }

        // Get handle to specialized object to run in separate thread.
        Callable<Object> operation = new RoyaleUnitSocketThread(server,
                configuration.getReportDir(), reports);

        // Get handle to service to run object in thread.
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Run object in thread and return Future.
        return executor.submit(operation);
    }

    /**
     * End of test report run. Called at the end of a test run. If verbose is set
     * to true reads all suites in the suite list and prints out a descriptive
     * message including the name of the suite, number of tests run and number of
     * tests failed, ignores any errors. If any tests failed during the test run,
     * the build is halted.
     */
    protected void analyzeReports()
    {
        LoggingUtil.log("Analyzing reports ...");

        // print out all report summaries
        LoggingUtil.log("\n" + reports.getSummary(), true);

        if (reports.hasFailures())
        {
            project.setNewProperty(configuration.getFailureProperty(), TRUE);

            if (configuration.isFailOnTestFailure())
            {
                throw new BuildException("RoyaleUnit tests failed during the test run.");
            }
        }
    }
}
