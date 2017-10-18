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

package org.apache.royale.compiler.internal.fxg.logging;

import org.apache.royale.compiler.fxg.logging.IFXGLogger;

/**
 * An abstract IFXGLogger implementation to redirect the various utility
 * logging methods to the core log() API:
 * <pre>
 * void log(int level, Object message, Throwable t, String location, int line, int column);
 * <pre> 
 */
public abstract class AbstractLogger implements IFXGLogger
{
    protected int level;

    protected AbstractLogger(int level)
    {
        this.level = level;
    }

    @Override
    public void debug(Object message)
    {
        log(DEBUG, message);
    }

    @Override
    public void debug(Object message, Throwable t)
    {
        log(DEBUG, message, t);
    }

    @Override
    public void debug(Object message, Throwable t, String location, int line, int column)
    {
        log(DEBUG, message, t, location, line, column);
    }

    @Override
    public void error(Object message)
    {
        log(ERROR, message);
    }

    @Override
    public void error(Object message, Throwable t)
    {
        log(ERROR, message, t);
    }

    @Override
    public void error(Object message, Throwable t, String location, int line, int column)
    {
        log(ERROR, message, t, location, line, column);
    }
    
    @Override
    public void info(Object message)
    {
        log(INFO, message);
    }

    @Override
    public void info(Object message, Throwable t)
    {
        log(INFO, message, t);
    }

    @Override
    public void info(Object message, Throwable t, String location, int line, int column)
    {
        log(INFO, message, t, location, line, column);
    }
    
    @Override
    public int getLevel()
    {
        return level;
    }

    @Override
    public void setLevel(int value)
    {
        level = value;
    }

    @Override
    public void log(int level, Object message)
    {
        log(level, message, null);
    }

    @Override
    public void log(int level, Object message, Throwable t)
    {
        log(level, message, t, null, 0, 0);
    }

    @Override
    public void warn(Object message)
    {
        log(WARN, message);
    }

    @Override
    public void warn(Object message, Throwable t)
    {
        log(WARN, message, t);
    }

    @Override
    public void warn(Object message, Throwable t, String location, int line, int column)
    {
        log(WARN, message, t, location, line, column);
    }
}
