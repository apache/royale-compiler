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
 * A simple interface to report information while processing an FXG docuemnt.
 */
public interface IFXGLogger
{
    static final int ALL = 0;
    static final int DEBUG = 10000;
    static final int INFO = 20000;
    static final int WARN = 30000;
    static final int ERROR = 40000;
    static final int NONE = Integer.MAX_VALUE;

    int getLevel();
    void setLevel(int level);

    void debug(Object message);
    void debug(Object message, Throwable t);
    void debug(Object message, Throwable t, String location, int line, int column);

    void error(Object message);
    void error(Object message, Throwable t);
    void error(Object message, Throwable t, String location, int line, int column);

    void info(Object message);
    void info(Object message, Throwable t);
    void info(Object message, Throwable t, String location, int line, int column);

    void log(int level, Object message);
    void log(int level, Object message, Throwable t);
    void log(int level, Object message, Throwable t, String location, int line, int column);

    void warn(Object message);
    void warn(Object message, Throwable t);
    void warn(Object message, Throwable t, String location, int line, int column);
}
