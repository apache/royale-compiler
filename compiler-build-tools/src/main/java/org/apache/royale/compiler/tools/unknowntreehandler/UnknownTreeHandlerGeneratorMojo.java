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

package org.apache.royale.compiler.tools.unknowntreehandler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name="generate-unknown-tree-handler",defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class UnknownTreeHandlerGeneratorMojo
    extends AbstractMojo
{
    @Parameter(defaultValue="${project.basedir}/src/main/unknowntreehandler",property="inputDir",required=true)
    private File inputDir;

    @Parameter(property="inputFile",required=true)
    private String inputFile;

    @Parameter(defaultValue="${project.build.directory}/generated-sources/unknowntreehandler",property="outputDir",required=true)
    private File outputDirectory;

    @Parameter(property="outputFile",required=true)
    private String outputFile;

    @Parameter(defaultValue="${project}")
    private MavenProject project;

    public void execute()
        throws MojoExecutionException
    {
        File input = new File(inputDir, inputFile);
        if(!input.exists()) {
            throw new MojoExecutionException("Could not read file: " + input.getAbsolutePath());
        }

        // Make sure the output directory exists.
        File output = new File(outputDirectory, outputFile);
        if(!output.getParentFile().exists()) {
            if(!output.getParentFile().mkdirs()) {
                throw new MojoExecutionException("Could not create output directory: " +
                        output.getParentFile().getAbsolutePath());
            }
        }

        String[] args = {input.getAbsolutePath(), output.getAbsolutePath()};
        try {
            UnknownTreePatternInputOutput.main(args);
        } catch(Exception e) {
            throw new MojoExecutionException("Caught error while executing UnknownTreePatternInputOutput.main", e);
        }

        project.addCompileSourceRoot(outputDirectory.getPath());
    }
}
