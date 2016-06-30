/*
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

import org.apache.flex.maven.flexjs.utils.DependencyHelper;
import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;
import org.apache.flex.tools.FlexToolRegistry;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.eclipse.aether.RepositorySystemSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by christoferdutz on 22.04.16.
 */
public abstract class BaseMojo
        extends AbstractMojo
{

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue="${project.build.directory}")
    protected File outputDirectory;

    @Parameter
    private Namespace[] namespaces;

    @Parameter
    private String[] includeClasses;

    @Parameter
    private IncludeFile[] includeFiles;

    @Parameter
    private Define[] defines;

    @Parameter
    private String targetPlayer = "11.1";

    @Parameter
    private boolean includeSources = false;

    @Parameter
    protected boolean debug = false;

    @Parameter
    private Boolean includeLookupOnly = null;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repositorySystemSession;

    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;

    protected boolean skip() {
        return false;
    }

    protected abstract String getConfigFileName();

    protected abstract File getOutput();

    protected VelocityContext getVelocityContext() throws MojoExecutionException {
        VelocityContext context = new VelocityContext();

        List<Artifact> allLibraries = DependencyHelper.getAllLibraries(
                project, repositorySystemSession, projectDependenciesResolver);
        List<Artifact> libraries = getLibraries(allLibraries);
        List<Artifact> externalLibraries = getExternalLibraries(allLibraries);
        List<String> sourcePaths = getSourcePaths();
        context.put("libraries", libraries);
        context.put("externalLibraries", externalLibraries);
        context.put("sourcePaths", sourcePaths);
        context.put("namespaces", getNamespaces());
        context.put("namespaceUris", getNamespaceUris());
        context.put("includeClasses", includeClasses);
        context.put("includeFiles", includeFiles);
        context.put("defines", getDefines());
        context.put("targetPlayer", targetPlayer);
        context.put("includeSources", includeSources);
        context.put("debug", debug);
        if(includeLookupOnly != null) {
            context.put("includeLookupOnly", includeLookupOnly);
        }
        context.put("output", getOutput());

        return context;
    }

    protected abstract String getToolGroupName();

    protected abstract String getFlexTool();

    protected List<Namespace> getNamespaces() {
        List<Namespace> namespaces = new LinkedList<Namespace>();
        if(this.namespaces != null) {
            for (Namespace namespace : this.namespaces) {
                namespaces.add(namespace);
            }
        }
        return namespaces;
    }

    protected Set<String> getNamespaceUris() {
        Set<String> namespaceUris = new HashSet<String>();
        for(Namespace namespace : getNamespaces()) {
            namespaceUris.add(namespace.getUri());
        }
        return namespaceUris;
    }

    @SuppressWarnings("unchecked")
    protected List<String> getSourcePaths() {
        List<String> sourcePaths = new LinkedList<String>();
        for(String sourcePath : (List<String>) project.getCompileSourceRoots()) {
            if(new File(sourcePath).exists()) {
                sourcePaths.add(sourcePath);
            }
        }
        return sourcePaths;
    }

    protected String getSourcePath(String resourceOnPath) {
        for(String path : getSourcePaths()) {
            File tmpFile = new File(path, resourceOnPath);
            if(tmpFile.exists()) {
                return tmpFile.getPath();
            }
        }
        return null;
    }

    protected List<String> getCompilerArgs(File configFile) throws MojoExecutionException {
        List<String> args = new LinkedList<String>();
        args.add("-load-config=" + configFile.getPath());
        return args;
    }

    public void execute()
            throws MojoExecutionException
    {
        // Skip this step if not all preconditions are met.
        if(skip()) {
            return;
        }

        // Prepare the config file.
        File configFile = new File(outputDirectory, getConfigFileName());
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();
        Template template = velocityEngine.getTemplate("config/" + getConfigFileName());
        VelocityContext context = getVelocityContext();

        if(!configFile.getParentFile().exists()) {
            if(!configFile.getParentFile().mkdirs()) {
                throw new MojoExecutionException("Could not create output directory: " + configFile.getParent());
            }
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(configFile);
            template.merge(context, writer);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating config file at " + configFile.getPath());
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new MojoExecutionException("Error creating config file at " + configFile.getPath());
                }
            }
        }

        // Get the tool group.
        FlexToolRegistry toolRegistry = new FlexToolRegistry();
        FlexToolGroup toolGroup = toolRegistry.getToolGroup(getToolGroupName());
        if(toolGroup == null) {
            throw new MojoExecutionException("Could not find tool group: " + getToolGroupName());
        }

        // Get an instance of the compiler and run the build.
        FlexTool tool = toolGroup.getFlexTool(getFlexTool());
        String[] args = getCompilerArgs(configFile).toArray(new String[0]);
        getLog().info("Executing " + getFlexTool() + " in tool group " + getToolGroupName() + " with args: " + Arrays.toString(args));
        tool.execute(args);
    }

    protected List<Artifact> getLibraries(List<Artifact> artifacts) {
        List<Artifact> libraries = new LinkedList<Artifact>();
        for(Artifact artifact : artifacts) {
            if(!("provided".equalsIgnoreCase(artifact.getScope()) || "runtime".equalsIgnoreCase(artifact.getScope())) && includeLibrary(artifact)) {
                libraries.add(artifact);
            }
        }
        return libraries;
    }

    protected List<Artifact> getExternalLibraries(List<Artifact> artifacts) {
        List<Artifact> externalLibraries = new LinkedList<Artifact>();
        for(Artifact artifact : artifacts) {
            if(("provided".equalsIgnoreCase(artifact.getScope()) || "runtime".equalsIgnoreCase(artifact.getScope())) && includeLibrary(artifact)) {
                externalLibraries.add(artifact);
            }
        }
        return externalLibraries;
    }

    protected List<Define> getDefines() {
        List<Define> defines = new LinkedList<Define>();
        if(this.defines != null) {
            for(Define define : this.defines) {
                defines.add(define);
            }
        }
        return defines;
    }

    protected boolean includeLibrary(Artifact library) {
        return true;
    }

}
