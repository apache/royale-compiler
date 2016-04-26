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
@Mojo(name="compile-as",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileASMojo
    extends BaseCompileMojo
{

    @Parameter(defaultValue="${basedir}/src/main/config/compile-as-config.xml")
    private File compileAsConfigFile;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swc")
    private String outputFileName;

    @Override
    protected File getConfigFile() {
        return compileAsConfigFile;
    }

    private File getOutputFile() {
        return new File(outputDirectory, outputFileName);
    }

    @Override
    protected String[] getCompilerArgs(File configFile) {
        return new String[]{"-debug", "-load-config=" + configFile.getPath(),
                "-output=" + getOutputFile().getPath(), "-define=COMPILE::AS3,true", "-define=COMPILE::JS,false"};
    }

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();
        if(getOutputFile().exists()) {
            // Attach the file created by the compiler as artifact file to maven.
            project.getArtifact().setFile(getOutputFile());
        }
    }
}
