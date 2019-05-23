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
            context = new DefaultContext();
        }
        
        return context;
    }
}
