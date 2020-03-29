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

package org.apache.royale.compiler.tools.unpack;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;

/**
 */
@Mojo(name="unpack-resources",defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class UnpackResourceMojo
        extends AbstractMojo {

    private static final int KILOBYTE = 1024;
    private static final int MEGABYTE = KILOBYTE * 1024;
    private static final int BUFFER_MAX = MEGABYTE;

    @Parameter
    private String resource;

    @Parameter(defaultValue="${project.build.directory}/downloads")
    private File targetDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(resource == null) {
            throw new MojoExecutionException("Config parameter 'resource' required for this goal.");
        }

        InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
        if(is == null) {
            throw new MojoExecutionException("Could not find resource " + resource);
        }

        if(!targetDirectory.exists() && !targetDirectory.mkdirs()) {
            throw new MojoExecutionException("Could not create output directory " + targetDirectory.getPath());
        }

        final byte[] data = new byte[BUFFER_MAX];
        ArchiveInputStream archiveInputStream = null;
        ArchiveEntry entry;
        try {
            archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(
                    new BufferedInputStream(is));

            while ((entry = archiveInputStream.getNextEntry()) != null) {
                final File outputFile = new File(targetDirectory, entry.getName());

                // Entry is a directory.
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        if(!outputFile.mkdirs()) {
                            throw new MojoExecutionException(
                                    "Could not create output directory " + outputFile.getAbsolutePath());
                        }
                    }
                }

                // Entry is a file.
                else {
                    final FileOutputStream fos = new FileOutputStream(outputFile);
                    BufferedOutputStream dest = null;
                    try {
                        dest = new BufferedOutputStream(fos, BUFFER_MAX);

                        int count;
                        while ((count = archiveInputStream.read(data, 0, BUFFER_MAX)) != -1) {
                            dest.write(data, 0, count);
                        }
                    } finally {
                        if(dest != null) {
                            dest.flush();
                            dest.close();
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Error unpacking resources", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error unpacking resources", e);
        } catch (ArchiveException e) {
            throw new MojoExecutionException("Error unpacking resources", e);
        } finally {
            if(archiveInputStream != null) {
                try {
                    archiveInputStream.close();
                } catch(Exception e) {
                    // Ignore...
                }
            }
        }
    }

}
