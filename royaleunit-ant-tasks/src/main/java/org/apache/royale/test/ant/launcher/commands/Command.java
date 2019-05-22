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
package org.apache.royale.test.ant.launcher.commands;

import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.Commandline;
import org.apache.royale.test.ant.LoggingUtil;

public abstract class Command
{
    private Project project;
    private Commandline commandLine;
    private String[] environment;

    public Command()
    {
        super();
        this.commandLine = new Commandline();
        environment = new String[0];
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    public Commandline getCommandLine()
    {
        return commandLine;
    }
    
    public int execute() throws IOException
    {
        Execute execute = new Execute();
        execute.setCommandline(getCommandLine().getCommandline());
        execute.setAntRun(getProject());
        execute.setEnvironment(getEnvironment());
        
        LoggingUtil.log(getCommandLine().describeCommand());
        
        return execute.execute();
    }
    
    public Process launch() throws IOException
    {
        Execute execute = new Execute();
        execute.setCommandline(getCommandLine().getCommandline());
        execute.setAntRun(getProject());
        execute.setEnvironment(getEnvironment());
        
        LoggingUtil.log(getCommandLine().describeCommand());
        
        execute.execute();
        
        //By default we use the Ant Execute task which does not give us a handle to a process
        return null;
    }

    public void setEnvironment(String[] variables)
    {
        this.environment = variables;
    }

    public String[] getEnvironment()
    {
        return environment;
    }

}
