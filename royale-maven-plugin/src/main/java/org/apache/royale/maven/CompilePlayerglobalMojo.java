/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.royale.maven;

import java.io.File;
import java.util.List;

import org.apache.flex.tools.FlexTool;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;

/**
 * goal which compiles a project into a playerglobal swc library.
 */
@Mojo(name="compile-playerglobal",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompilePlayerglobalMojo
    extends BaseMojo
{
    @Parameter(defaultValue = "src/main/playerglobal")
    private String playerglobalSourceDirectory;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swc")
    protected String outputFileName;

    @Parameter(defaultValue = "false")
    private boolean skipPlayerglobal;

    @Parameter(defaultValue = "false")
    private boolean playerglobalAir;

    @Component
    private MavenProjectHelper projectHelper;

    @Override
    protected String getToolGroupName() {
        return "Royale";
    }

    @Override
    protected String getFlexTool() {
        return FlexTool.FLEX_TOOL_COMPC;
    }

    @Override
    protected String getConfigFileName() throws MojoExecutionException {
        return "compile-playerglobal-config.xml";
    }

    protected File getOutput() throws MojoExecutionException {
        return new File(outputDirectory, outputFileName);
    }

    @Override
    protected List<String> getCompilerArgs(File configFile) throws MojoExecutionException {
        List<String> args = super.getCompilerArgs(configFile);
        args.add("-compiler.targets=SWF");
        return args;
    }

    @Override
    protected boolean skip() {
        if(skipPlayerglobal) {
            return true;
        }
        File inputFolder = new File(project.getBasedir(), playerglobalSourceDirectory);
        return !inputFolder.exists() || !inputFolder.isDirectory();
    }

    @Override
    public void execute() throws MojoExecutionException
    {
        super.execute();

        if(getOutput().exists()) {
            // Add the extern to the artifact.
            projectHelper.attachArtifact(project, getOutput(), "typedefs");
        }
    }

    @Override
    protected List<Define> getDefines() throws MojoExecutionException {
        List<Define> defines = super.getDefines();
        defines.add(new Define("COMPILE::JS", "false"));
        defines.add(new Define("COMPILE::SWF", "true"));
        return defines;
    }

    @Override
    protected boolean includeLibrary(Artifact library) {
        return "typedefs".equalsIgnoreCase(library.getClassifier());
    }

}
