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

package org.apache.royale.compiler.tools.problems;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name="generate-problems-resource-bundle",defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ProblemResourceBundleGeneratorMojo
    extends BaseProblemGeneratorMojo
{
    @Parameter(defaultValue="${project.basedir}/src/main/java/org/apache/royale/compiler/problems",
            property="inputDir", required=true)
    private File inputDirectory;

    @Parameter(defaultValue="${project.build.directory}/classes/org/apache/royale/compiler",
            property="outputDir", required=true)
    private File outputDirectory;

    @Parameter(defaultValue="messages_en.properties",
            property="outputFile", required=true)
    private String outputFile;

    @Parameter(defaultValue="${project}")
    private MavenProject project;

    @Override
    protected File getInputDirectory() {
        return inputDirectory;
    }

    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected String getOutputFile() {
        return outputFile;
    }

    @Override
    protected void printEntry(PrintWriter writer, File source, boolean last) {
        // don't use println otherwise file has windows line-endings on windows and
        // won't compare to osx version
        writer.print(getProblemName(source) + "=" + getProblemDescription(source) + "\n");
    }

    private String getProblemName(File sourceFile) {
        String fileName = sourceFile.getName();
        return fileName.substring(0, fileName.length() - "class".length());
    }

    private String getProblemDescription(File sourceFile) {
        try {
            BufferedReader sourceFileReader = new BufferedReader(new FileReader(sourceFile));
            String line;
            StringBuilder sb = null;
            while((line = sourceFileReader.readLine()) != null) {
                if(line.contains("DESCRIPTION")) {
                    sb = new StringBuilder();
                }
                if(sb != null) {
                    sb.append(line);
                    if(line.trim().endsWith(";")) {
                        break;
                    }
                }
            }
            sourceFileReader.close();
            if(sb != null) {
                line = sb.toString();
                return line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void clean(File outputFile) throws MojoExecutionException {
        // TODO: Clear all the content after: "# Messages for Compiler problems are written below by ProblemLocalizer."
    }

}
