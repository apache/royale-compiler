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

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 */
@Mojo(name="package-js",defaultPhase = LifecyclePhase.PACKAGE)
public class PackageJSMojo extends AbstractMojo {

    @Parameter(defaultValue="${project.build.directory}")
    protected File outputDirectory;

    @Parameter(defaultValue = "javascript")
    private String javascriptOutputDirectoryName;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.war")
    private String warOutputFileName;

    @Parameter
    protected boolean debug = false;

    @Component
    protected MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * This mojo should only be executed if a JS output was generated
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        File warSourceDirectory = new File(outputDirectory, javascriptOutputDirectoryName);
        warSourceDirectory = new File(new File(warSourceDirectory, "bin"), debug ? "js-debug" : "js-release");

        // If the directory exists, pack everything into one zip file.
        if(warSourceDirectory.exists()) {
            File warTargetFile = new File(outputDirectory, warOutputFileName);

            // If the output file already exists, delete it first.
            if(warTargetFile.exists()) {
                if(!warTargetFile.delete()) {
                    throw new MojoExecutionException(
                            "Could not delete existing war file at " + warSourceDirectory.getPath());
                }
            }

            // Create a new zip file with the output of the JS compiler.
            try {
                ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(warTargetFile));
                addDirectoryToZip(warSourceDirectory, warSourceDirectory, zipOutputStream);
                IOUtils.closeQuietly(zipOutputStream);
            } catch (IOException e) {
                throw new MojoExecutionException("Error creating war archive", e);
            }

            // Attach the war file to the maven project.
            if(warTargetFile.exists()) {
                projectHelper.attachArtifact( project, "war", null, warTargetFile);
            }
        }
    }

    private void addDirectoryToZip(File zipRootDirectory, File currentDirectory, ZipOutputStream zipOutputStream)
            throws IOException {
        File[] directoryContent = currentDirectory.listFiles();
        if(directoryContent != null) {
            for (File file : directoryContent) {
                if(file.isDirectory()) {
                    addDirectoryToZip(zipRootDirectory, file, zipOutputStream);
                } else {
                    String relativePath = zipRootDirectory.toURI().relativize(currentDirectory.toURI()).getPath();
                    if (relativePath.startsWith("/"))
                        relativePath = relativePath.substring(1);
                    ZipEntry zipEntry = new ZipEntry(relativePath + file.getName());
                    zipOutputStream.putNextEntry(zipEntry);
                    FileInputStream in = new FileInputStream(file);
                    IOUtils.copy(in, zipOutputStream);
                    IOUtils.closeQuietly(in);
                }
            }
        }
    }

}
