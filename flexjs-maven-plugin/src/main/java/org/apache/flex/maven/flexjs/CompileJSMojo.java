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

import org.apache.flex.tools.FlexTool;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * goal which compiles a project into a flexjs swc library.
 */
@Mojo(name="compile-js",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CompileJSMojo
    extends BaseMojo
{

    @Parameter(defaultValue = "${project.artifactId}-${project.version}.swc")
    private String outputFileName;

    @Parameter(defaultValue = "false")
    private boolean skipSwc;

    @Parameter(defaultValue = "false")
    private boolean skipJS;

    @Override
    protected String getToolGroupName() {
        return "FlexJS";
    }

    @Override
    protected String getFlexTool() {
        return FlexTool.FLEX_TOOL_COMPC;
    }

    @Override
    protected String getConfigFileName() throws MojoExecutionException {
        return "compile-js-config.xml";
    }

    @Override
    protected File getOutput() throws MojoExecutionException {
        return new File(outputDirectory, outputFileName);
    }

    @Override
    protected boolean skip() {
        return skipSwc || skipJS;
    }

    @Override
    public void execute() throws MojoExecutionException {
        // FlexJS requires an existing SWC. If we skipped
        // the AS compilation, this doesn't exist yet so
        // we simply generate an empty swc and use that.
        if(!getOutput().exists()) {
            createEmptySwc(getOutput());
        }

        super.execute();

        if(getOutput().exists()) {
            // Attach the file created by the compiler as artifact file to maven.
            project.getArtifact().setFile(getOutput());
        }
    }

    @Override
    protected boolean isForceSwcExternalLibraryPath() {
        // The forceSwcExternalLibraryPath should only apply to Flash compilations.
        return false;
    }

    @Override
    protected List<String> getCompilerArgs(File configFile) throws MojoExecutionException {
        List<String> args = super.getCompilerArgs(configFile);
        args.add("-js-output-type=FLEXJS");
        args.add("-compiler.strict-xml=true");
        return args;
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
    protected List<Define> getDefines() throws MojoExecutionException {
        List<Define> defines = super.getDefines();
        defines.add(new Define("COMPILE::JS", "true"));
        defines.add(new Define("COMPILE::SWF", "false"));
        return defines;
    }

    @Override
    protected boolean includeLibrary(Artifact library) {
        return "typedefs".equalsIgnoreCase(library.getClassifier());
    }

    private void createEmptySwc(File outputFile) throws MojoExecutionException {
        if(!outputFile.getParentFile().exists()) {
            if(!outputFile.getParentFile().mkdirs()) {
                throw new MojoExecutionException("Could not create directory " + outputFile.getParent());
            }
        }

        // Create a dummy swc (A zip file which contains a minimal catalog.xml) */
        try {
            OutputStream stream = new FileOutputStream(outputFile);
            stream = new BufferedOutputStream(stream);
            ZipOutputStream zipStream = new ZipOutputStream(stream);
            ZipEntry entry = new ZipEntry("catalog.xml");
            zipStream.putNextEntry(entry);
            byte[] dummyCatalog = (
                    "<?xml version=\"1.0\" ?>\n" +
                    "<swc xmlns=\"http://www.adobe.com/flash/swccatalog/9\">\n" +
                    "    <versions>\n" +
                    "        <swc version=\"1.2\"/>\n" +
                    "        <compiler name=\"Apache Flex - FlexJS: Compiler: Compiler\" version=\"0.8\" build=\"0-SNAPSHOT\"/>\n" +
                    "    </versions>\n" +
                    "    <features>\n" +
                    "    </features>\n" +
                    "    <libraries>\n" +
                    "    </libraries>\n" +
                    "</swc>").getBytes();
            zipStream.write(dummyCatalog);
            zipStream.closeEntry();
            zipStream.close();
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Could not create empty zip file at " + outputFile.getPath());
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create empty zip file at " + outputFile.getPath());
        }
    }

}
