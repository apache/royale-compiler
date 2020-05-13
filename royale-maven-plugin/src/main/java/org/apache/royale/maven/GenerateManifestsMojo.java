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

import org.apache.royale.maven.utils.DependencyHelper;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.RepositorySystemSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 */
@Mojo(name="generate-manifests",defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateManifestsMojo
        extends AbstractMojo
{

    @Parameter(defaultValue = "namespaces")
    protected String namespaceDirectory;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue="${project.build.directory}")
    protected File outputDirectory;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repositorySystemSession;

    @Component
    private ProjectDependenciesResolver projectDependenciesResolver;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<Artifact> allLibraries = DependencyHelper.getAllLibraries(
                project, repositorySystemSession, projectDependenciesResolver);
        Map<String, Map<String, String>> namespaces = new HashMap<String, Map<String, String>>();
        for(Artifact library : allLibraries) {
            File libraryFile = library.getFile();
            // Check if the file exists and is a SWC.
            if(libraryFile.exists() && libraryFile.getName().endsWith(".swc")) {
                // Get the component data from the current library.
                Map<String, Map<String, String>> curLibNamespaces = getNamespacesFromLibrary(libraryFile);

                // Merge that data with the current index.
                for(Map.Entry<String, Map<String, String>> namespace : curLibNamespaces.entrySet()) {
                    String namespaceUri = namespace.getKey();
                    if(!namespaces.containsKey(namespaceUri)) {
                        namespaces.put(namespaceUri, new HashMap<String, String>());
                    }
                    for(Map.Entry<String, String> component : namespace.getValue().entrySet()) {
                        namespaces.get(namespaceUri).put(component.getKey(), component.getValue());
                    }
                }
            }
        }

        // Serialize the namespace information into separate files.
        for(Map.Entry<String, Map<String, String>> namespace : namespaces.entrySet()) {
            createNamespace(namespace.getKey(), namespace.getValue());
        }
    }

    private Map<String, Map<String, String>> getNamespacesFromLibrary(File library) {
        Map<String, Map<String, String>> namespaces = new HashMap<String, Map<String, String>>();
        try {
            // Open the file as a zip
            byte[] catalogBytes = null;
            FileInputStream fin = new FileInputStream(library);
            BufferedInputStream bin = new BufferedInputStream(fin);
            ZipInputStream zin = new ZipInputStream(bin);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.getName().equals("catalog.xml")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = zin.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.close();
                    catalogBytes = out.toByteArray();
                    break;
                }
            }
            zin.close();
            
            // Read the catalog.xml file inside.
            if(catalogBytes != null) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document catalog = builder.parse(new ByteArrayInputStream(catalogBytes));
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    xpath.setNamespaceContext(new NamespaceContext() {
                        public String getNamespaceURI(String prefix) {
                            return prefix.equals("cat") ? "http://www.adobe.com/flash/swccatalog/9" : null;
                        }
                        public Iterator getPrefixes(String val) {
                            return null;
                        }
                        public String getPrefix(String uri) {
                            return null;
                        }
                    });
                    XPathExpression expr = xpath.compile("/cat:swc/cat:components/cat:component");
                    Object result = expr.evaluate(catalog, XPathConstants.NODESET);
                    NodeList nodes = (NodeList) result;
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element componentElement = (Element) nodes.item(i);
                        String className = componentElement.getAttribute("className");
                        String name = componentElement.getAttribute("name");
                        String uri = componentElement.getAttribute("uri");
                        if(!namespaces.containsKey(uri)) {
                           namespaces.put(uri, new HashMap<String, String>());
                        }
                        namespaces.get(uri).put(name, className);
                    }
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return namespaces;
    }

    private void createNamespace(String namespaceUri, Map<String, String> components) throws MojoExecutionException {
        File namespaceDir = new File(outputDirectory, namespaceDirectory);
        if(!namespaceDir.exists()) {
            if(!namespaceDir.mkdirs()) {
                throw new MojoExecutionException(
                        "Could not create namespace output directory at " + namespaceDir.getPath());
            }
        }

        String namespaceFilename = namespaceUri.replaceAll(":", "-").replaceAll("/", "-") + ".xml";
        File namespaceOutputFile = new File(namespaceDir, namespaceFilename);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document namespaceDoc = factory.newDocumentBuilder().newDocument();
            Element componentPackageElement = namespaceDoc.createElement("componentPackage");
            // TODO: Check if the compiler doesn't trip over this ...
            componentPackageElement.setAttribute("namespace-uri", namespaceUri);
            namespaceDoc.appendChild(componentPackageElement);
            for (Map.Entry<String, String> component : components.entrySet()) {
                Element componentElement = namespaceDoc.createElement("component");
                componentElement.setAttribute("id", component.getKey());
                componentElement.setAttribute("class", component.getValue().replace(":", "."));
                componentPackageElement.appendChild(componentElement);
            }


            try {
                Source source = new DOMSource(namespaceDoc);
                Result result = new StreamResult(namespaceOutputFile);

                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, result);
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

}
