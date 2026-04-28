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

package org.apache.royale.compiler.internal.projects;

import org.apache.royale.compiler.clients.JSConfiguration;
import org.apache.royale.compiler.config.Configuration;

/**
 * This class applies configuration settings to a RoyaleJSProject.
 */
public class RoyaleJSProjectConfigurator extends RoyaleProjectConfigurator
{
    /**
     * Constructor
     */
    public RoyaleJSProjectConfigurator(Class<? extends Configuration> configurationClass)
    {
        super(configurationClass);
    }
    
    @Override
    protected boolean applyConfiguration()
    {
        boolean result = super.applyConfiguration();
        if (result && configuration instanceof JSConfiguration && project instanceof RoyaleJSProject)
        {
            JSConfiguration jsConfiguration = (JSConfiguration) configuration;
            RoyaleJSProject royaleProject = (RoyaleJSProject) project;
            royaleProject.setDefaultInitializers(jsConfiguration.getJsDefaultInitializers());
        }
        return result;
    }
}