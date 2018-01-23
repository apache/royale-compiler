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

import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

/**
 * Warn the user that using a "Royale only" configuration option is not fully
 * supported.
 */
@DefaultSeverity(CompilerProblemSeverity.WARNING)
public class RoyaleOnlyConfigurationOptionNotSupported extends ConfigurationProblem
{
    public static final String DESCRIPTION =
        "'${option}' is not fully supported.";
    
    public static final int warningCode = 5043;
    
    public RoyaleOnlyConfigurationOptionNotSupported(String option, String source, int line)
    {
        super(source, line, "");
        this.option = option;
    }

    public final String option;

}
