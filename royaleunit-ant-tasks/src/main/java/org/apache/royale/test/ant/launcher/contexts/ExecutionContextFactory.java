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
package org.apache.royale.test.ant.launcher.contexts;

import org.apache.royale.test.ant.launcher.OperatingSystem;
import org.apache.royale.test.ant.launcher.commands.TestRunCommand;
import org.apache.royale.test.ant.launcher.commands.process.ProcessCommand;
import org.apache.royale.test.ant.launcher.commands.playwright.PlaywrightCommand;

public class ExecutionContextFactory
{
    /**
     * Used to generate new instances of an execution context based on the OS and whether the build should run
     * headlessly.
     * 
     * @param os Current OS.
     * @param headless Should the build run headlessly.
     * @param display The vnc display number to use if headless
     * 
     * @return
     */
    public static ExecutionContext createContext(OperatingSystem os, boolean headless, int display)
    {
        boolean trulyHeadless = headless && (os == OperatingSystem.LINUX);
        ExecutionContext context = null;
        
        if(trulyHeadless)
        {
            context = new HeadlessContext(display); 
        }
        else
        {
            context = new DefaultProcessContext();
        }
        
        return context;
    }

    /**
     * Used to generate new instances of an execution context based on the test
     * run command, the OS, and whether the build should run headlessly.
     * 
     * @param os Current OS.
     * @param headless Should the build run headlessly.
     * @param display The vnc display number to use if headless
     * @param command The test run command the context is for
     * 
     * @return
     */
    public static ExecutionContext createContext(TestRunCommand command,
        OperatingSystem os, boolean headless, int display)
    {
        ExecutionContext context = null;

        if (command instanceof PlaywrightCommand)
        {
            PlaywrightExecutionContext playwrightContext = new DefaultPlaywrightContext();
            playwrightContext.setCommand((PlaywrightCommand)command);
            context = playwrightContext;
        }
        else
        {
            ProcessExecutionContext processContext = null;
            boolean trulyHeadless = headless && (os == OperatingSystem.LINUX);
            if(trulyHeadless)
            {
                processContext = new HeadlessContext(display); 
            }
            else
            {
                processContext = new DefaultProcessContext();
            }
            processContext.setCommand((ProcessCommand)command);
            context = processContext;
        }
        
        
        return context;
    }
}
