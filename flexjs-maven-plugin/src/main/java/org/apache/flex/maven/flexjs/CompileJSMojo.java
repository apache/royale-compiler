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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.RepositorySystemSession;

import java.io.File;

/**
 * goal which compiles a project into a flexjs swc library.
 */
@Mojo(name="compile-js",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileJSMojo
    extends AbstractMojo
{

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue="${basedir}/src/main/config/compile-js-config.xml")
    private File compileJsConfigFile;

    @Parameter(defaultValue="${project.build.directory}")
    private File outputDirectory;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swc")
    private String outputFileName;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repositorySystemSession;

    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;

    public void execute()
        throws MojoExecutionException
    {
        if(!compileJsConfigFile.exists()) {
            getLog().info(" - compilation config file '" + compileJsConfigFile.getPath() +
                    "' not found, skipping compilation");
            return;
        }

        File generatedSourcesOutputDir = new File(outputDirectory, "generated-sources/flexjs");
        if(!generatedSourcesOutputDir.exists()) {
            if(!generatedSourcesOutputDir.mkdirs()) {
                throw new MojoExecutionException("Could not create directory " + generatedSourcesOutputDir.getPath());
            }
        }

        // Get the falcon tool group.
        FlexToolRegistry toolRegistry = new FlexToolRegistry();
        FlexToolGroup toolGroup = toolRegistry.getToolGroup("FlexJS");
        if(toolGroup == null) {
            throw new MojoExecutionException("Could not find tool group: Falcon");
        }

        // Get an instance of the compiler and run the build.
        FlexTool compc = toolGroup.getFlexTool(FlexTool.FLEX_TOOL_COMPC);
        File outputFile = new File(outputDirectory, outputFileName);

/*            <!--arg value="+flexlib=${FLEX_HOME}/frameworks" />
            <arg value="+playerglobal.version=${playerglobal.version}" />
            <arg value="+env.PLAYERGLOBAL_HOME=${env.PLAYERGLOBAL_HOME}" />
            <arg value="+env.AIR_HOME=${env.AIR_HOME}" />
            <arg value="-external-library-path+=${JS.SWC}" />
            <!-- this is not on external-library path otherwise goog.requires are not generated -->
            <arg value="-library-path+=${GCL.SWC}" />*/

        String[] args = {"+flexlib=../../..", "-js-output-type=FLEXJS", "-keep-asdoc", "-load-config=" + compileJsConfigFile.getPath(),
                "-output=" + new File(outputDirectory.getPath(), "generated-sources/flexjs").getPath(),
                "-define=COMPILE::AS3,false", "-define=COMPILE::JS,true"};
        compc.execute(args);

        // Attach the file created by the compiler as artifact file to maven.
        project.getArtifact().setFile(outputFile);

    }

}
