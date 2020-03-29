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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 */
@Mojo(name="generate-extern",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class GenerateExterncMojo
        extends BaseMojo
{

    @Parameter
    private FileSet[] externcInput;

    @Parameter(defaultValue = "generated-sources/externc")
    private String outputDirectoryName;

    @Parameter
    private ExterncConfig externcConfig;

    @Override
    protected String getToolGroupName() {
        return "Royale";
    }

    @Override
    protected String getFlexTool() {
        return "EXTERNC";
    }

    @Override
    protected String getConfigFileName() throws MojoExecutionException {
        return "generate-externc-config.xml";
    }

    @Override
    protected boolean skip() {
        return externcInput == null;
    }

    @Override
    protected File getOutput() throws MojoExecutionException {
        return new File(outputDirectory, outputDirectoryName);
    }

    @Override
    protected VelocityContext getVelocityContext() throws MojoExecutionException {
        VelocityContext context = super.getVelocityContext();

        List<File> includedFiles = new LinkedList<File>();
        FileSetManager fileSetManager = new FileSetManager();
        for(FileSet fileSet : externcInput) {
            String[] fileSetIncludes = fileSetManager.getIncludedFiles(fileSet);
            if((fileSetIncludes != null) && (fileSetIncludes.length > 0)) {
                for(String include : fileSetIncludes) {
                    includedFiles.add(new File(fileSet.getDirectory(), include));
                }
            }
        }
        context.put("sourcePath", includedFiles);
        if(externcConfig != null) {
            context.put("classExcludes", externcConfig.classExcludes);
            context.put("fieldExcludes", externcConfig.fieldExcludes);
            context.put("excludes", externcConfig.excludes);
        }
        
        return context;
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
            File[] typeDirectories = outputDirectory.listFiles();
            if(typeDirectories != null) {
                for (File typeDirectory : typeDirectories) {
                    project.addCompileSourceRoot(typeDirectory.getPath());
                }
            }
        }
    }

    @Override
    protected List<Define> getDefines() throws MojoExecutionException {
        List<Define> defines = super.getDefines();
        defines.add(new Define("COMPILE::JS", "true"));
        defines.add(new Define("COMPILE::SWF", "false"));
        return defines;
    }

}
