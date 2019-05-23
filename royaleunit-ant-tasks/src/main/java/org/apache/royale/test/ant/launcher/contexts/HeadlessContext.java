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

import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.royale.test.ant.LoggingUtil;
import org.apache.royale.test.ant.launcher.commands.headless.XvncException;
import org.apache.royale.test.ant.launcher.commands.headless.XvncStartCommand;
import org.apache.royale.test.ant.launcher.commands.headless.XvncStopCommand;
import org.apache.royale.test.ant.launcher.commands.player.PlayerCommand;

/**
 * Context used to wrap a call to the player command in a start and stop of a vncserver.
 * All vncserver commands are blocking.
 */
public class HeadlessContext implements ExecutionContext
{
    private PlayerCommand playerCommand;
    private int startDisplay;
    private int finalDisplay;
    private Project project;
    
    public HeadlessContext(int display)
    {
        this.startDisplay = display;
    }
    
    public void setProject(Project project)
    {
        this.project = project;
    }
    
    public void setCommand(PlayerCommand command)
    {
        this.playerCommand = command;
    }
    
    public void start() throws IOException
    {
        // setup vncserver on the provided display
        XvncStartCommand xvncStart = new XvncStartCommand(startDisplay);
        xvncStart.setProject(project);
        
        LoggingUtil.log("Starting xvnc", true);
        
        // execute the maximum number of cycle times before throwing an exception
        while (xvncStart.execute() != 0)
        {
            LoggingUtil.log("Cannot start xnvc on :" + xvncStart.getCurrentDisplay() + ", cycling ...");
            
            try
            {
                xvncStart.cycle();
            }
            catch (XvncException xe) {
                throw new IOException(xe);
            }
        }
            
        finalDisplay = xvncStart.getCurrentDisplay();
        
        //setup player command to use the right display in its env when launching
        playerCommand.setEnvironment(new String[]{ "DISPLAY=:" + finalDisplay });
        LoggingUtil.log("Setting DISPLAY=:" + finalDisplay);
        
        //prep anything the command needs to run
        playerCommand.prepare();
    }
    
    public void stop(Process playerProcess) throws IOException
    {
        // destroy the process related to the player if it exists
        if(playerProcess != null)
        {
            playerProcess.destroy();
        }
        
        // Now stop the vncserver that the player has been destroyed
        XvncStopCommand xvncStop = new XvncStopCommand(finalDisplay);
        xvncStop.setProject(project);
        xvncStop.execute();
    }
}
