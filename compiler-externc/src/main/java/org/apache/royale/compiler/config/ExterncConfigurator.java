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

package org.apache.royale.compiler.config;

import org.apache.royale.compiler.clients.ExternCConfiguration;
import org.apache.royale.compiler.exceptions.ConfigurationException;

public class ExterncConfigurator extends Configurator
{
    /**
     * Constructor
     */
    public ExterncConfigurator()
    {
        this(ExternCConfiguration.class);
    }

    /**
     * Constructor
     */
    public ExterncConfigurator(Class<? extends ExternCConfiguration> configurationClass)
    {
        super(configurationClass);
    }
    
    @Override
    protected void validateSWCInputs() throws ConfigurationException
    {
        ExternCConfiguration configuration = (ExternCConfiguration) getConfiguration();
        if (configuration.getTypedefs().isEmpty())
        {
            throw new ConfigurationException.NoSwcInputs( null, null, -1 );
        }
    }
}
