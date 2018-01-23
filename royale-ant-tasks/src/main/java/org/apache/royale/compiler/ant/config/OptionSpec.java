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

package org.apache.royale.compiler.ant.config;

/**
 * Represents the name of a configuration option.
 */
public class OptionSpec
{
    public OptionSpec(String fullName, String alias)
    {
        this.fullName = fullName;
        
        int i = fullName.lastIndexOf('.');
        shortName = i != -1 ? fullName.substring(i + 1) : fullName;
        
        this.alias = alias;
    }

    public OptionSpec(String fullName)
    {
        this(fullName, null);
    }

    private String fullName;
    private String shortName;
    private String alias;

    public String getFullName()
    {
    	return fullName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public String getAlias()
    {
        return alias;
    }

    public boolean matches(String option)
    {
        boolean result = false;

        if (option.equals(getFullName()))
            result = true;

        else if (option.equals(getShortName()))
            result = true;

        else if (option.equals(getAlias()))
            result = true;

        return result;
    }
}
