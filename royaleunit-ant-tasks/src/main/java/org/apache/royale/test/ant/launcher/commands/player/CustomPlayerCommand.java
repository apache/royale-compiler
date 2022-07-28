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
package org.apache.royale.test.ant.launcher.commands.player;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.apache.royale.test.ant.LoggingUtil;
import org.apache.royale.test.ant.launcher.commands.process.ProcessCommand;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;

public class CustomPlayerCommand implements ProcessCommand
{
    private DefaultPlayerCommand proxiedCommand;
    private File executable;
    private String[] executableArgs;

    public ProcessCommand getProxiedCommand()
    {
        return proxiedCommand;
    }
    
    public void setProxiedCommand(DefaultPlayerCommand playerCommand)
    {
        this.proxiedCommand = playerCommand;
    }

    public File getExecutable()
    {
        return executable;
    }
    
    public void setExecutable(File executable)
    {
        this.executable = executable;
    }

    public String[] getExecutableArgs()
    {
        return executableArgs;
    }
    
    public void setExecutableArgs(String[] executableArgs)
    {
        this.executableArgs = executableArgs;
    }
    
    public void setProject(Project project)
    {
        proxiedCommand.setProject(project);
    }
    
    public void setSwf(File swf)
    {
        proxiedCommand.setSwf(swf);
    }
    
    public String getUrl() {
        return proxiedCommand.getUrl();
    }

    public void setUrl(String url) {
        proxiedCommand.setUrl(url);
    }
    
    public File getFileToExecute()
    {
        return proxiedCommand.getFileToExecute();
    }
    
    public void prepare()
    {
        proxiedCommand.prepare();
        
        proxiedCommand.getCommandLine().setExecutable(executable.getAbsolutePath());
        proxiedCommand.getCommandLine().clearArgs();
        
        int customArgsCount = executableArgs != null ? executableArgs.length : 0;
        String[] args = new String[customArgsCount + 1];
        if (executableArgs != null)
        {
            System.arraycopy(executableArgs, 0, args, 0, executableArgs.length);
        }
        if(getUrl() != null)
        {    	  
            args[args.length - 1] = getUrl();
        } 
        else 
        {  
            args[args.length - 1] = getFileToExecute().getAbsolutePath();
        }
        proxiedCommand.getCommandLine().addArguments(args);
    }
    
    public Process launch() throws IOException
    {
        LoggingUtil.log(proxiedCommand.getCommandLine().describeCommand());
        
        //execute the command directly
        return Runtime.getRuntime().exec(
                proxiedCommand.getCommandLine().getCommandline(), 
                getJointEnvironment(), 
                proxiedCommand.getProject().getBaseDir());
    }

    public void setEnvironment(String[] variables)
    {
        proxiedCommand.setEnvironment(variables);
    }

    /**
     * Combine process environment variables and command's environment to emulate the default
     * behavior of the Execute task.  Needed especially when user expects environment to be 
     * available to custom command (e.g. - xvnc with player not on path).
     */
    @SuppressWarnings("unchecked")
    private String[] getJointEnvironment()
    {
        Vector procEnvironment = Execute.getProcEnvironment();
        String[] environment = new String[procEnvironment.size() + proxiedCommand.getEnvironment().length];
        System.arraycopy(procEnvironment.toArray(), 0, environment, 0, procEnvironment.size());
        System.arraycopy(proxiedCommand.getEnvironment(), 0, environment, procEnvironment.size(), proxiedCommand.getEnvironment().length);
        
        return environment;
    }
}
