package org.apache.flex.maven.flexjs;

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
 * goal which compiles a project into a flexjs swc library.
 */
@Mojo(name="compile",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileMojo
    extends AbstractMojo
{

    @Parameter(defaultValue="${basedir}/src/main/config/compile-config.xml")
    private File configFile;

    @Parameter(defaultValue="${project.build.directory}")
    private File outputDirectory;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swc")
    private String outputFileName;

    public void execute()
        throws MojoExecutionException
    {
        FlexToolRegistry toolRegistry = new FlexToolRegistry();
        FlexToolGroup toolGroup = toolRegistry.getToolGroup("Falcon");
        FlexTool compc = toolGroup.getFlexTool(FlexTool.FLEX_TOOL_COMPC);
        File outputFile = new File(outputDirectory, outputFileName);
        String[] args = {"+flexlib=externs", "-debug", "-load-config=" + configFile.getPath(),
                "-output=" + outputFile.getPath()};
        compc.execute(args);
    }

}
