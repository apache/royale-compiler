/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.flex.utils.FilenameNormalization;

/**
 * EnvProperties checks in following order for a value.
 * 
 * 1) unittest.properties 2) environment variables 3) for key FLEX_HOME &
 * PLAYERGLOBAL_HOME sets a default value.
 */
public class EnvProperties
{

    /**
     * FLEX_HOME
     */
    public String SDK;

    /**
     * PLAYERGLOBAL_HOME
     */
    public String FPSDK;

    /**
     * AIR_HOME
     */
    public String AIRSDK;

    /**
     * FLASHPLAYER_DEBUGGER
     */
    public String FDBG;

    private static EnvProperties env;

    public static EnvProperties initiate()
    {
        if (env == null)
        {
            env = new EnvProperties();
            env.setup();
        }
        return env;
    }

    private void setup()
    {
        Properties p = new Properties();
        try
        {
            File f = new File("unittest.properties");
            p.load(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("unittest.properties not found");
        }
        catch (IOException e)
        {
        }

        SDK = p.getProperty("FLEX_HOME", System.getenv("FLEX_HOME"));
        if (SDK == null)
            SDK = FilenameNormalization
                    .normalize("../compiler/generated/dist/sdk");
        System.out.println("environment property - FLEX_HOME = " + SDK);

        FPSDK = p.getProperty("PLAYERGLOBAL_HOME",
                System.getenv("PLAYERGLOBAL_HOME"));
        if (FPSDK == null)
            FPSDK = FilenameNormalization
                    .normalize("../compiler/generated/dist/sdk/frameworks/libs/player");
        System.out.println("environment property - PLAYERGLOBAL_HOME = "
                + FPSDK);

        AIRSDK = p.getProperty("AIR_HOME", System.getenv("AIR_HOME"));
        System.out.println("environment property - AIR_HOME = " + AIRSDK);

        FDBG = p.getProperty("FLASHPLAYER_DEBUGGER",
                System.getenv("FLASHPLAYER_DEBUGGER"));
        System.out.println("environment property - FLASHPLAYER_DEBUGGER = "
                + FDBG);
    }

}
