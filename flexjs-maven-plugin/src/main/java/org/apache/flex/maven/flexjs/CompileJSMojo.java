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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * goal which compiles a project into a flexjs swc library.
 */
@Mojo(name="compile-js",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileJSMojo
    extends BaseCompileMojo
{

    @Parameter(defaultValue="${basedir}/src/main/config/compile-js-config.xml")
    private File compileJsConfigFile;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swc")
    private String outputFileName;

    @Override
    protected File getConfigFile() {
        return compileJsConfigFile;
    }

    private File getOutputDirectory() {
        return new File(outputDirectory, "generated-sources/flexjs");
    }

    @Override
    protected String[] getCompilerArgs(File configFile) {
        return new String[] {"-js-output-type=FLEXJS", "-keep-asdoc", "-load-config=" + configFile.getPath(),
                "-output=" + new File(outputDirectory.getPath(), "generated-sources/flexjs").getPath(),
                "-define=COMPILE::AS3,false", "-define=COMPILE::JS,true"};
    }

    @Override
    public void execute() throws MojoExecutionException
    {
        if(!compileJsConfigFile.exists()) {
            getLog().info(" - compilation config file '" + compileJsConfigFile.getPath() +
                    "' not found, skipping compilation");
            return;
        }

        File generatedSourcesOutputDir = getOutputDirectory();
        if(!generatedSourcesOutputDir.exists()) {
            if(!generatedSourcesOutputDir.mkdirs()) {
                throw new MojoExecutionException("Could not create directory " + generatedSourcesOutputDir.getPath());
            }
        }

        super.execute();

        // TODO: Add Source directory
    }

}
