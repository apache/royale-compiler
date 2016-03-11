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

package org.apache.flex.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


/**
 *  EnvProperties checks in following order for a value.
 * 
 *  1) unittest.properties 
 *  2) environment variables
 *  3) for key FLEX_HOME & PLAYERGLOBAL_HOME sets a default value.
 */
public class EnvProperties {
	
    /**
     * FLEX_HOME
     */
    public String SDK;
    
    /**
     * TLF_HOME
     */
    public String TLF;
    
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
	
    /**
     * PLAYERGLOBAL_VERSION
     */
    public String FPVER;
    
	
	private static EnvProperties env;
	
	public static EnvProperties initiate() {
		if(env == null) {
			env = new EnvProperties();
			env.setup();
		}
		return env;
	}
	
	private void setup()
	{
        String prefix = "";
		Properties p = new Properties();
        String envFileName = FilenameNormalization.normalize("../env.properties");
        try {
            File f = new File(envFileName);
            if (f.exists())
            {
            	p.load(new FileInputStream( f ));
                prefix = "env.";
            }
        } catch (FileNotFoundException e) {
            System.out.println(envFileName + " not found");
            try {
                File f = new File("unittest.properties");
                p.load(new FileInputStream( f ));
            } catch (FileNotFoundException e1) {
                System.out.println("unittest.properties not found");
            } catch (IOException e1) {
	            // Ignore
            }
        } catch (IOException e) {
	        // Ignore
        }
		
		SDK = p.getProperty(prefix + "FLEX_HOME", System.getenv("FLEX_HOME"));
		if(SDK == null)
		{
            SDK = FilenameNormalization.normalize("../../flex-sdk");
	        File mxmlc = new File(SDK + "/lib/mxmlc.jar");
	        if (!mxmlc.exists())
	            SDK = FilenameNormalization.normalize("../compiler/generated/dist/sdk");
		}
		System.out.println("environment property - FLEX_HOME = " + SDK);
		
		FPSDK = p.getProperty(prefix + "PLAYERGLOBAL_HOME", System.getenv("PLAYERGLOBAL_HOME"));
		if(FPSDK == null)
			FPSDK = FilenameNormalization.normalize("../compiler/generated/dist/sdk/frameworks/libs/player");
		System.out.println("environment property - PLAYERGLOBAL_HOME = " + FPSDK);
        
        FPVER = p.getProperty(prefix + "PLAYERGLOBAL_VERSION", System.getenv("PLAYERGLOBAL_VERSION"));
        if (FPVER == null)
            FPVER = "11.1";
        System.out.println("environment property - PLAYERGLOBAL_VERSION = " + FPVER);
        
        TLF = p.getProperty(prefix + "TLF_HOME", System.getenv("TLF_HOME"));
        if (TLF == null)
        {
            TLF = FilenameNormalization.normalize("../../flex-tlf");
        }
        System.out.println("environment property - TLF_HOME = " + TLF);
		
		AIRSDK = p.getProperty(prefix + "AIR_HOME", System.getenv("AIR_HOME"));
		System.out.println("environment property - AIR_HOME = " + AIRSDK);
		
		FDBG = p.getProperty(prefix + "FLASHPLAYER_DEBUGGER", System.getenv("FLASHPLAYER_DEBUGGER"));
		System.out.println("environment property - FLASHPLAYER_DEBUGGER = " + FDBG);
	}

}
