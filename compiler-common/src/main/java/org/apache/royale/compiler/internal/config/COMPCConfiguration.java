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

package org.apache.royale.compiler.internal.config;

import java.util.List;

import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.ConfigurationValue;
import org.apache.royale.compiler.exceptions.ConfigurationException;
import org.apache.royale.compiler.internal.config.annotations.ArgumentNameGenerator;
import org.apache.royale.compiler.internal.config.annotations.Config;
import org.apache.royale.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.royale.compiler.internal.config.annotations.Mapping;
import org.apache.royale.compiler.internal.config.annotations.SoftPrerequisites;

/**
 * A Configuration to override some behaviors of the default configuration.
 */
public class COMPCConfiguration extends Configuration
{
    public COMPCConfiguration()
    {
        super();
        
        // Override MXMLC defaults
        setDebug(true);
    }

    /**
     * COMPC ignores RSL settings.
     */
    @Override
    @Config(allowMultiple = true)
    @Mapping({"runtime-shared-library-path"})
    @SoftPrerequisites({"static-link-runtime-shared-libraries"})
    @ArgumentNameGenerator(RSLArgumentNameGenerator.class)
    @InfiniteArguments
    public void setRuntimeSharedLibraryPath(
            ConfigurationValue cfgval,
            List<String> urls) throws ConfigurationException
    {
    }
}
