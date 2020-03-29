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

import org.apache.flex.tools.FlexTool;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

/**
 * goal which compiles the asdoc documentation for the project.
 */
@Mojo(name="compile-asdoc",defaultPhase = LifecyclePhase.SITE)
public class CompileASDocMojo
    extends BaseMojo
{

    @Parameter(defaultValue = "asdoc")
    private String asdocDirectoryName;

    @Parameter(defaultValue = "false")
    private boolean skipASDoc;

    @Parameter(defaultValue = "false")
    private boolean skipAS;
    
    private ThreadLocal<Type> type = new ThreadLocal<Type>();

    @Override
    protected String getToolGroupName() {
        return "Royale";
    }

    @Override
    protected String getFlexTool() {
        return FlexTool.FLEX_TOOL_ASDOC;
    }

    @Override
    protected String getConfigFileName() throws MojoExecutionException {
        if(type.get() == null) {
            throw new MojoExecutionException("type not set");
        }
        switch (type.get()) {
            case SWF:
                return "compile-asdoc-swf-config.xml";
            case JS:
                return "compile-asdoc-js-config.xml";
        }
        return null;
    }

    @Override
    protected File getOutput() throws MojoExecutionException {
        if(type.get() == null) {
            throw new MojoExecutionException("type not set");
        }
        switch (type.get()) {
            case SWF:
                return new File(new File(outputDirectory, asdocDirectoryName), "swf");
            case JS:
                return new File(new File(outputDirectory, asdocDirectoryName), "js");
        }
        return null;
    }

    @Override
    protected boolean skip() {
        return skipASDoc;
    }

    @Override
    public void execute() throws MojoExecutionException {
        // We are using a ThreadLocal in this case in order to control the
        // mode the two methods getOutput and getDefines are in in a threadsafe
        // manner. Currently it wouldn't be necessary, but we never know how the
        // compiler will be instantiated in the future. This method is safe in
        // any way it could be used (Multiple executions in parallel with Maven).
        try {
            if (!skipAS)
            {
                // Execute the ASDoc generation for SWF
                getLog().info("Generating SWF apidocs");
                type.set(Type.SWF);
                File outputDirectory = getOutput();
                if (!outputDirectory.exists()) {
                    if (!outputDirectory.mkdirs()) {
                        throw new MojoExecutionException("Could not create output directory for apidocs " + outputDirectory.getPath());
                    }
                }
                super.execute();
                getLog().info("Finished");
            }

            // Execute the ASDoc generation for JavaScript
            getLog().info("Generating JS apidocs");
            type.set(Type.JS);
            outputDirectory = getOutput();
            if (!outputDirectory.exists()) {
                if (!outputDirectory.mkdirs()) {
                    throw new MojoExecutionException("Could not create output directory for apidocs " + outputDirectory.getPath());
                }
            }
            super.execute();
            getLog().info("Finished");
        } finally {
            type.remove();
        }

        // TODO: Merge both outputs in order to create one XML per class containing which elements are available for SWF and which ones for JS in one file
        // TODO: Send each merged XML through an XSLT that produces XHTML

    }

    @Override
    protected List<String> getCompilerArgs(File configFile) throws MojoExecutionException {
        List<String> args = super.getCompilerArgs(configFile);
        args.add("-js-output-type=royale_dita");
        return args;
    }

    @Override
    protected List<Define> getDefines() throws MojoExecutionException {
        List<Define> defines = super.getDefines();
        if(type.get() == null) {
            throw new MojoExecutionException("type not set");
        }
        switch (type.get()) {
            case SWF:
                defines.add(new Define("COMPILE::JS", "false"));
                defines.add(new Define("COMPILE::SWF", "true"));
                break;
            case JS:
                defines.add(new Define("COMPILE::JS", "true"));
                defines.add(new Define("COMPILE::SWF", "false"));
                break;
        }
        return defines;
    }

    private enum Type {
        SWF,
        JS
    }

}
