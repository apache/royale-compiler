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

import org.apache.flex.maven.flexjs.types.FlexScope;
import org.apache.flex.tools.FlexTool;
import org.apache.flex.tools.FlexToolGroup;
import org.apache.flex.tools.FlexToolRegistry;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.*;
import org.eclipse.aether.RepositorySystemSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * goal which compiles a project into a flexjs swc library.
 */
@Mojo(name="compile-as",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileASMojo
    extends AbstractMojo
{

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue="${basedir}/src/main/config/compile-as-config.xml")
    private File compileAsConfigFile;

    @Parameter(defaultValue="${project.build.directory}")
    private File outputDirectory;

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swc")
    private String outputFileName;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repositorySystemSession;

    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;

    public void execute()
        throws MojoExecutionException
    {
        if(!compileAsConfigFile.exists()) {
            getLog().info(" - compilation config file '" + compileAsConfigFile.getPath() +
                    "' not found, skipping compilation");
            return;
        }

        // Prepare the config file.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            // Resolve all the dependencies.
            Set<Artifact> resolvedDependencies = resolveDependencies();

            // Parse the input document.
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document configuration = builder.parse(new FileInputStream(compileAsConfigFile));

            // Append the path-elements for the compile scope.
            Set<Artifact> compileDependencies = getDependenciesForScope(resolvedDependencies, FlexScope.COMPILE);
            outputLibraryPath(configuration, "flex-config/compiler/library-path", compileDependencies);

            // Append the path-elements for the external scope.
            Set<Artifact> externalDependencies = getDependenciesForScope(resolvedDependencies, FlexScope.EXTERNAL);
            outputLibraryPath(configuration, "flex-config/compiler/external-library-path", externalDependencies);

            // Write the modified config-file to the output directory.
            File configFileOutput = new File(outputDirectory, "compile-as-config.xml");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(configuration);
            StreamResult result = new StreamResult(configFileOutput);
            transformer.transform(source, result);

            // Get the falcon tool group.
            FlexToolRegistry toolRegistry = new FlexToolRegistry();
            FlexToolGroup toolGroup = toolRegistry.getToolGroup("Falcon");
            if(toolGroup == null) {
                throw new MojoExecutionException("Could not find tool group: Falcon");
            }

            // Get an instance of the compiler and run the build.
            FlexTool compc = toolGroup.getFlexTool(FlexTool.FLEX_TOOL_COMPC);
            File outputFile = new File(outputDirectory, outputFileName);
            String[] args = {"+flexlib=externs", "-debug", "-load-config=" + configFileOutput.getPath(),
                    "-output=" + outputFile.getPath(), "-define=COMPILE::AS3,true", "-define=COMPILE::JS,false"};
            compc.execute(args);

            // Attach the file created by the compiler as artifact file to maven.
            project.getArtifact().setFile(outputFile);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private Set<Artifact> resolveDependencies() throws MojoExecutionException {
        DefaultDependencyResolutionRequest dependencyResolutionRequest =
                new DefaultDependencyResolutionRequest(project, repositorySystemSession);
        DependencyResolutionResult dependencyResolutionResult;

        try {
            dependencyResolutionResult = projectDependenciesResolver.resolve(dependencyResolutionRequest);
        } catch (DependencyResolutionException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }

        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        if (dependencyResolutionResult.getDependencyGraph() != null
                && !dependencyResolutionResult.getDependencyGraph().getChildren().isEmpty()) {
            RepositoryUtils.toArtifacts(artifacts, dependencyResolutionResult.getDependencyGraph().getChildren(),
                    Collections.singletonList(project.getArtifact().getId()), null);
        }
        return artifacts;
    }

    private Set<Artifact> getDependenciesForScope(Set<Artifact> dependencies, FlexScope scope) {
        Set<Artifact> artifacts = new LinkedHashSet<Artifact>();
        for(Artifact dependency : dependencies) {
            FlexScope dependencyScope = FlexScope.COMPILE;
            if(dependency.getScope() != null) {
                dependencyScope = FlexScope.valueOf(dependency.getScope().toUpperCase());
            }
            if(dependencyScope == scope) {
                artifacts.add(dependency);
            }
        }
        return artifacts;
    }

    private void outputLibraryPath(Document configDocument, String baseXPath, Set<Artifact> artifacts) {
        if(!artifacts.isEmpty()) {
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            try {
                XPathExpression expr = xpath.compile(baseXPath);
                Element externalLibraryPath = (Element) expr.evaluate(configDocument, XPathConstants.NODE);
                if(externalLibraryPath != null) {
                    // Remove any existing path-elements (They are leftovers from the ant build).
                    Node child = externalLibraryPath.getFirstChild();
                    while(child != null) {
                        externalLibraryPath.removeChild(child);
                        child = externalLibraryPath.getFirstChild();
                    }

                    // Add the new path-elements.
                    for (Artifact artifact : artifacts) {
                        Element newPathElement = configDocument.createElement("path-element");
                        newPathElement.setTextContent(artifact.getFile().getAbsolutePath());
                        externalLibraryPath.appendChild(newPathElement);
                    }
                } else {
                    throw new RuntimeException("Could not find root element " + baseXPath);
                }
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Error adding path-element", e);
            }
        }
    }

}
