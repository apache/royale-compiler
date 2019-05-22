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
package org.apache.royale.test.ant.launcher.commands.headless;

import java.io.IOException;

import org.apache.royale.test.ant.LoggingUtil;
import org.apache.royale.test.ant.launcher.commands.Command;

public class XvncStartCommand extends Command
{
    private final String VNC_SERVER_COMMAND = "vncserver";
    private final int MAX_DISPLAY_CYCLES = 2;
    
    private int baseDisplay;
    private int currentDisplay;
    
    public XvncStartCommand(int display)
    {
        super();
        this.baseDisplay = display;
        this.currentDisplay = display;
    }
    
    public void cycle() throws XvncException
    {
        if((currentDisplay - baseDisplay) == MAX_DISPLAY_CYCLES)
        {
            throw new XvncException(baseDisplay, currentDisplay);
        }
        
        currentDisplay++;
    }
    
    public int getCurrentDisplay()
    {
        return currentDisplay;
    }
    
    @Override
    public int execute() throws IOException
    {
        getCommandLine().setExecutable(VNC_SERVER_COMMAND);
        getCommandLine().addArguments(new String[]{":" + String.valueOf(currentDisplay)});
        
        LoggingUtil.log("Attempting start on :" + currentDisplay + " ...");
        
        return super.execute();
    }
}
