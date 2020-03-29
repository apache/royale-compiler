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

import org.apache.royale.maven.trust.TrustHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.inject.Inject;
import java.io.File;

/**
 * This little mojo simply adds the maven build output directory to the
 * list of trusted sources for the FlashPlayer.
 */
@Mojo(name="trust",defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class TrustMojo
        extends AbstractMojo
{

    @Parameter(defaultValue="${project.build.directory}")
    private File outputDirectory;

    @Inject
    private TrustHandler securityHandler;

    public void execute()
            throws MojoExecutionException {
        securityHandler.trustDirectory(outputDirectory);
    }

}
