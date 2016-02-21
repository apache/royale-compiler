package org.apache.flex.compiler.tools;

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

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Goal which adds annotations to generated classes.
 */
@Mojo(name="add-class-annotation",defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class AnnotateClassesMojo
    extends AbstractMojo
{
    @Parameter
    protected Set<String> includes = new HashSet<String>();

    @Parameter
    protected Set<String> excludes = new HashSet<String>();

    @Parameter(defaultValue="${project.build.directory}/generated-sources")
    private File directory;

    @Parameter(property="annotation", required=true)
    private String annotation;

    public void execute()
        throws MojoExecutionException
    {
        SuffixMapping mapping = new SuffixMapping("jbg", Collections.<String>emptySet());
        SimpleSourceInclusionScanner scan = new SimpleSourceInclusionScanner(includes, excludes);
        scan.addSourceMapping(mapping);

        try {
            Set<File> candidates = scan.getIncludedSources(directory, null);
            for(File candidate : candidates) {
                processFile(candidate);
            }
        } catch (InclusionScanException e) {
            throw new MojoExecutionException("Error scanning filed to be processed.", e);
        }
    }

    private void processFile(File file) {
        if(!file.exists()) {
            System.out.println("Missing file: " + file.getPath());
            return;
        }
        System.out.println("Adding " + annotation + " to class: " + file.getPath());
        try
        {
            // Prepare to read the file.
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            File tmpOutputFile = new File(file.getParentFile(), file.getName() + ".tmp");
            FileOutputStream fileOutputStream = new FileOutputStream(tmpOutputFile);
            PrintStream outputStream = new PrintStream(fileOutputStream);
            try
            {
                // Read it line-by-line.
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    // If the line starts with "public class", output the annotation on the previous line.
                    if (line.startsWith("public class") || line.startsWith("public interface")) {
                        outputStream.println(annotation);
                    }
                    outputStream.println(line);
                }
            }
            finally
            {
                try {
                    bufferedReader.close();
                } catch(Exception e) {
                    // Ignore.
                }
                try {
                    outputStream.close();
                } catch(Exception e) {
                    // Ignore.
                }
                try {
                    fileOutputStream.close();
                } catch(Exception e) {
                    // Ignore.
                }
            }

            // Remove the original file.
            if(!file.delete()) {
                throw new MojoExecutionException("Error deleting original file at: " + file.getPath());
            }

            // Rename the temp file to the name of the original file.
            if(!tmpOutputFile.renameTo(file)) {
                throw new MojoExecutionException("Error renaming the temp file from: " + tmpOutputFile.getPath() +
                        " to: " + file.getPath());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
