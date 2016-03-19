package org.apache.flex.compiler.tools.patchfiles;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Goal which pre-processes the JavaScript input files so they can be processed by EXTERNC.
 */
@Mojo(name="pre-process-sources",defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class PreProcessSourcesMojo
    extends AbstractMojo
{

    @Parameter
    private Set<String> includes = new HashSet<String>();

    @Parameter
    private Set<String> excludes = new HashSet<String>();

    @Parameter(defaultValue="${project.build.directory}/downloads")
    private File downloadesSourceDirectory;

    @Parameter
    private List<Operation> operations;

    @Parameter(defaultValue="${project}")
    private MavenProject project;

    public void execute()
        throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping("js", Collections.<String>emptySet());
        SimpleSourceInclusionScanner scan = new SimpleSourceInclusionScanner(includes, excludes);
        scan.addSourceMapping(mapping);
        try {
            Set<File> candidates = scan.getIncludedSources(downloadesSourceDirectory, null);
            for(File candidate : candidates) {
                if(operations != null) {
                    getLog().info("- Processing file: " + candidate.getPath());
                    for (Operation operation : operations) {
                        try {
                            operation.perform(candidate);
                        } catch (IOException e) {
                            throw new MojoExecutionException("Error performing pre-processing operation " +
                                    operation + " on file " + candidate.getPath(), e);
                        }
                    }
                }
            }
        } catch (InclusionScanException e) {
            throw new MojoExecutionException("Error scanning files to be processed.", e);
        }
    }



}
