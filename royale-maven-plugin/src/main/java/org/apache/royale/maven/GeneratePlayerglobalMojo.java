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
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.velocity.VelocityContext;

/**
 */
@Mojo(name="generate-playerglobal",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class GeneratePlayerglobalMojo
        extends BaseMojo
{
    @Parameter(defaultValue = "src/main/asdoc")
    private String asdocDirectoryName;

    @Parameter(defaultValue = "generated-sources/playerglobal")
    private String outputDirectoryName;

    @Parameter(defaultValue = "false")
    private boolean skipPlayerglobal;

    @Override
    protected String getToolGroupName() {
        return "Royale";
    }

    @Override
    protected String getFlexTool() {
        return "PLAYERGLOBALC";
    }

    @Override
    protected String getConfigFileName() throws MojoExecutionException {
        return "generate-playerglobal-config.xml";
    }

    @Override
    protected File getOutput() throws MojoExecutionException {
        return new File(outputDirectory, outputDirectoryName);
    }

    @Override
    protected VelocityContext getVelocityContext() throws MojoExecutionException {
        VelocityContext context = super.getVelocityContext();
        context.put("asdocRoot", new File(asdocDirectoryName));
        return context;
    }

    @Override
    protected boolean skip() {
        if(skipPlayerglobal) {
            return true;
        }
        File inputFolder = new File(asdocDirectoryName);
        return !inputFolder.exists() || !inputFolder.isDirectory();
    }

    @Override
    public void execute() throws MojoExecutionException {
        File outputDirectory = getOutput();
        if(!outputDirectory.exists()) {
            if(!outputDirectory.mkdirs()) {
                throw new MojoExecutionException("Could not create output directory " + outputDirectory.getPath());
            }
        }

        super.execute();

        // Add eventually generated source paths to the project.
        if(outputDirectory.exists()) {
            project.addCompileSourceRoot(outputDirectory.getPath());
        }
    }

}
