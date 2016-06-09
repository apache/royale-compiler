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

package org.apache.flex.maven.flexjs;

import org.apache.flex.tools.FlexTool;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * goal which compiles a project into a flexjs swc library.
 */
@Mojo(name="compile-extern",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileExternMojo
    extends BaseMojo
{

    @Parameter(defaultValue = "${project.artifactId}-${project.version}-extern.swc")
    protected String outputFileName;

    @Parameter(defaultValue = "false")
    private boolean skipExtern;

    @Component
    private MavenProjectHelper projectHelper;

    @Override
    protected String getToolGroupName() {
        return "Falcon";
    }

    @Override
    protected String getFlexTool() {
        return FlexTool.FLEX_TOOL_COMPC;
    }

    @Override
    protected String getConfigFileName() {
        return "compile-extern-config.xml";
    }

    protected File getOutput() {
        return new File(outputDirectory, outputFileName);
    }

    @Override
    protected boolean skip() {
        return skipExtern;
    }

    @Override
    public void execute() throws MojoExecutionException
    {
        super.execute();

        if(getOutput().exists()) {
            // Add the extern to the artifact.
            projectHelper.attachArtifact(project, getOutput(), "extern");
        }
    }

    @Override
    protected List<Namespace> getNamespaces() {
        List<Namespace> namespaces = new LinkedList<Namespace>();
        for(Namespace namespace : super.getNamespaces()) {
            if(namespace.getType().equals(Namespace.TYPE_DEFAULT) || namespace.getType().equals(Namespace.TYPE_JS)) {
                namespaces.add(namespace);
            }
        }
        return namespaces;
    }

    @Override
    protected List<Define> getDefines() {
        List<Define> defines = super.getDefines();
        defines.add(new Define("COMPILE::JS", "true"));
        defines.add(new Define("COMPILE::AS3", "false"));
        return defines;
    }

    @Override
    protected boolean includeLibrary(Artifact library) {
        return "extern".equalsIgnoreCase(library.getClassifier());
    }

}
