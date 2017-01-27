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

package org.apache.flex.compiler.clients;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.flex.compiler.clients.MXMLJSC.JSOutputType;
import org.apache.flex.compiler.clients.MXMLJSC.JSTargetType;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.config.ConfigurationValue;
import org.apache.flex.compiler.exceptions.ConfigurationException;
import org.apache.flex.compiler.exceptions.ConfigurationException.CannotOpen;
import org.apache.flex.compiler.internal.config.annotations.Arguments;
import org.apache.flex.compiler.internal.config.annotations.Config;
import org.apache.flex.compiler.internal.config.annotations.InfiniteArguments;
import org.apache.flex.compiler.internal.config.annotations.Mapping;

import com.google.common.collect.ImmutableList;

/**
 * The {@link JSConfiguration} class holds all compiler arguments needed for
 * compiling ActionScript to JavaScript.
 * <p>
 * Specific flags are implemented here for the configuration to be loaded by the
 * configure() method of {@link MXMLJSC}.
 * <p>
 * This class inherits all compiler arguments from the MXMLC compiler.
 * 
 * @author Michael Schmalle
 */
public class JSConfiguration extends Configuration
{
    public JSConfiguration()
    {
    }

    //
    // 'compiler.targets' option
    //

    protected final List<String> targets = new ArrayList<String>();

    public List<String> getCompilerTargets()
    {
    	if (targets.size() == 0)
    		targets.add(JSTargetType.JS_FLEX.getText());
        return targets;
    }

    /**
     * The list of compiler outputs to generate
     */
    @Config(allowMultiple = true, isPath = false)
    @Mapping({ "compiler", "targets" })
    @Arguments("type")
    @InfiniteArguments
    public void setCompilerTargets(ConfigurationValue cv, String[] targetlist)
    {
    	for (String target : targetlist)
    		targets.add(target);
    }

    //
    // 'js-output-type'
    //

    @Config
    @Mapping("js-output-type")
    public void setJSOutputType(ConfigurationValue cv, String value)
            throws ConfigurationException
    {
         targets.clear();
         targets.add(value);
    }

    //
    // 'source-map'
    //

    private boolean sourceMap = false;

    public boolean getSourceMap()
    {
        return sourceMap;
    }

    @Config
    @Mapping("source-map")
    public void setSourceMap(ConfigurationValue cv, boolean value)
            throws ConfigurationException
    {
        sourceMap = value;
    }

}
