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
package org.apache.royale.test.ant.launcher;

import org.apache.royale.test.ant.LoggingUtil;

public enum OperatingSystem
{
    WINDOWS, MACOSX, LINUX;

    private static final String SUN_WINDOWS = "windows";
    private static final String SUN_MACOSX = "mac os x";
    private static final String OPENJDK_MACOSX = "darwin";
    
    /**
     * Searches for Windows and Mac specificially and if not found defaults to Linux.
     */
    public static OperatingSystem identify()
    {
        OperatingSystem os = null;
        String env = System.getProperty("os.name").toLowerCase();

        if (env.startsWith(SUN_WINDOWS))
        {
            LoggingUtil.log("OS: [Windows]");
            os = OperatingSystem.WINDOWS;
        } 
        else if (env.contains(SUN_MACOSX) || env.contains(OPENJDK_MACOSX))
        {
            LoggingUtil.log("OS: [Mac]");
            os = OperatingSystem.MACOSX;
        } 
        else
        {
            LoggingUtil.log("OS: [Linux]");
            os = OperatingSystem.LINUX;
        }
        
        return os;
    }
}
