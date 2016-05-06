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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

/**
 * goal which compiles a project into a flexjs swc library.
 */
@Mojo(name="compile-as",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileASMojo
    extends BaseMojo
{

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swc")
    private String outputFileName;

    @Parameter(defaultValue = "false")
    private boolean skipSwc;

    @Override
    protected String getToolGroupName() {
        return "Falcon";
    }

    @Override
    protected String getFlexTool() {
        return FlexTool.FLEX_TOOL_COMPC;
    }

    @Override
    protected String getConfigFileName() {
        return "compile-as-config.xml";
    }

    @Override
    protected File getOutput() {
        return new File(outputDirectory, outputFileName);
    }

    @Override
    protected boolean skip() {
        return skipSwc;
    }

    @Override
    protected List<String> getCompilerArgs(File configFile) {
        List<String> args = super.getCompilerArgs(configFile);
        args.add("-define=COMPILE::AS3,true");
        args.add("-define=COMPILE::JS,false");
        return args;
    }

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();

        if(getOutput().exists()) {
            // Attach the file created by the compiler as artifact file to maven.
            project.getArtifact().setFile(getOutput());
        }
    }

    @Override
    protected boolean includeLibrary(Artifact library) {
        return !"extern".equalsIgnoreCase(library.getClassifier());
    }

}
