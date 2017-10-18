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

package org.apache.royale.compiler.fxg.logging;

/**
 * Utility API to get access to the logger for the current thread from anywhere
 * in the library.
 */
public class FXGLog
{
    private static ThreadLocal<IFXGLogger> currentLogger = new ThreadLocal<IFXGLogger>();

    private FXGLog()
    {
    }

    /**
     * @return The logger for the current thread. 
     */
    public static IFXGLogger getLogger()
    {
        IFXGLogger logger = currentLogger.get();

        if (logger == null)
        {
            logger = FXGLoggerFactory.createDefaultLogger();
            setLogger(logger);
        }

        return logger;
    }

    /**
     * Sets the logger for the current thread. 
     */
    public static void setLogger(IFXGLogger value)
    {
        currentLogger.set(value);
    }
    
}
