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

package org.apache.royale.compiler.problems;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.config.CommandLineConfigurator;
import org.apache.royale.compiler.exceptions.ConfigurationException;

/**
 * This class is the base class for all problems found during configuration processing.
 */
public class ConfigurationProblem extends CompilerProblem
{
    public static final String DESCRIPTION = "${reason}.\n${location}";
    
    public static final int errorCode = 1319;
    
    public ConfigurationProblem(String fileName, int line, String reason)
    {
        this(fileName, -1, -1, line, -1, reason);
    }
    
    public ConfigurationProblem(String fileName, int start, int end, int line, int column, String reason)
    {
        // line is zero-based so subtract one.
        super(fileName, start, end, line > 0 ? line - 1 : line, column, !CommandLineConfigurator.SOURCE_COMMAND_LINE.equals(fileName));
        this.reason = reason;
        if (fileName != null && !CommandLineConfigurator.SOURCE_COMMAND_LINE.equals(fileName))
            location = String.format("%s (line: %d)", fileName, line);
        else
            location = "";
    }
    
    public ConfigurationProblem(ConfigurationException e)
    {
        this(e.getSource(), ISourceLocation.UNKNOWN, ISourceLocation.UNKNOWN,
             e.getLine(), e.getColumn(), e.getMessage());
    }
    
    public final String reason;
    public final String location;
}
