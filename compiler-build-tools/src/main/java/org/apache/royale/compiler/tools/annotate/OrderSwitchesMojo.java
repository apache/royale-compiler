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

package org.apache.royale.compiler.tools.annotate;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Goal which orders switch statement in JFlex output.
 */
@Mojo(name="order-jflex-switches",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class OrderSwitchesMojo
    extends AbstractMojo
{
    @Parameter
    protected Set<String> includes = new HashSet<String>();

    @Parameter
    protected Set<String> excludes = new HashSet<String>();

    @Parameter(defaultValue="${project.build.directory}/generated-sources")
    private File directory;
    
    public void execute()
        throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping("jflex", Collections.<String>emptySet());
        SimpleSourceInclusionScanner scan = new SimpleSourceInclusionScanner(includes, excludes);
        scan.addSourceMapping(mapping);

        try {
            Set<File> candidates = scan.getIncludedSources(directory, null);
            for(File candidate : candidates) {
                try {
                    OrderSwitches.processFile(candidate);
                } catch(AnnotateClass.AnnotateClassDeleteException e) {
                    throw new MojoExecutionException(e.getMessage());
                } catch(AnnotateClass.AnnotateClassRenameException e) {
                    throw new MojoExecutionException(e.getMessage());
                }
            }
        } catch (InclusionScanException e) {
            throw new MojoExecutionException("Error scanning files to be processed.", e);
        }
    }
}
