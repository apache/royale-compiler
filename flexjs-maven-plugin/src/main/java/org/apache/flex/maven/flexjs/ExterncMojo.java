/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flex.maven.flexjs;

import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;
import org.apache.flex.tools.FlexToolRegistry;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * goal which generates actionscript code from javascript.
 */
@Mojo(name="generate",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ExterncMojo
    extends AbstractMojo
{

    @Parameter(defaultValue="${basedir}/src/main/config/generate-config.xml")
    private File configFile;

    public void execute()
        throws MojoExecutionException
    {
        FlexToolRegistry toolRegistry = new FlexToolRegistry();
        FlexToolGroup toolGroup = toolRegistry.getToolGroup("FlexJS");
        // TODO: Change this to a flex-tool-api constant ...
        FlexTool compc = toolGroup.getFlexTool("EXTERNC");
        String[] args = {"+flexlib=externs", "-debug", "-load-config=" + configFile.getPath()};
        compc.execute(args);
    }

}
