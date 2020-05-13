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

import java.io.File;
import java.io.PrintWriter;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name="generate-problems-enum",defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ProblemEnumGeneratorMojo
    extends BaseProblemGeneratorMojo
{
    @Parameter(defaultValue="${project.basedir}/src/main/java/org/apache/royale/compiler/problems",
            property="inputDir", required=true)
    private File inputDirectory;

    @Parameter(defaultValue="${project.build.directory}/generated-sources/build-tools",
            property="outputDir", required=true)
    private File outputDirectory;

    @Parameter(defaultValue="org/apache/royale/compiler/problems/ProblemID.java",
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
    public void execute() throws MojoExecutionException {
        super.execute();

        // Add the output directory to the source path.
        project.addCompileSourceRoot(outputDirectory.getPath());
    }

    @Override
    protected void printHeader(PrintWriter writer) {
        writer.println("/*");
        writer.println(" *  Licensed to the Apache Software Foundation (ASF) under one or more");
        writer.println(" *  contributor license agreements.  See the NOTICE file distributed with");
        writer.println(" *  this work for additional information regarding copyright ownership.");
        writer.println(" *  The ASF licenses this file to You under the Apache License, Version 2.0");
        writer.println(" *  (the \"License\"); you may not use this file except in compliance with");
        writer.println(" *  the License.  You may obtain a copy of the License at");
        writer.println();
        writer.println(" *      http://www.apache.org/licenses/LICENSE-2.0");
        writer.println();
        writer.println(" *  Unless required by applicable law or agreed to in writing, software");
        writer.println(" *  distributed under the License is distributed on an \"AS IS\" BASIS,");
        writer.println(" *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        writer.println(" *  See the License for the specific language governing permissions and");
        writer.println(" *  limitations under the License.");
        writer.println(" */");
        writer.println();
        writer.println("package org.apache.royale.compiler.problems;");
        writer.println();
        writer.println("import java.util.EnumSet;");
        writer.println("import com.google.common.collect.ImmutableMap;");
        writer.println();
        writer.println("public enum ProblemID");
        writer.println("{");
    }

    @Override
    protected void printEntry(PrintWriter writer, File source, boolean last) {
        writer.println("    " + getProblemEnumEntry(source.getName()) + (last ? ";" : ","));
    }

    @Override
    protected void printFooter(PrintWriter writer) {
        writer.println();
        writer.println("    private ProblemID(Class<? extends CompilerProblem> problemClass)");
        writer.println("    {");
        writer.println("        this.problemClass = problemClass;");
        writer.println("    }");
        writer.println();
        writer.println("    private final Class<? extends CompilerProblem> problemClass;");
        writer.println("    public final Class<? extends CompilerProblem> getProblemClass()");
        writer.println("    {");
        writer.println("        return problemClass;");
        writer.println("    }");
        writer.println();
        writer.println("    public static final ProblemID getID(ICompilerProblem problem)");
        writer.println("    {");
        writer.println("        final CompilerProblem problemImpl = (CompilerProblem)problem;");
        writer.println("        return classToIDMap.get(problemImpl.getClass());");
        writer.println("    }");
        writer.println();
        writer.println("    public static final ProblemID getID(Class<?> problemClass)");
        writer.println("    {");
        writer.println("        return classToIDMap.get(problemClass);");
        writer.println("    }");
        writer.println();
        writer.println("    private static final ImmutableMap<Class<? extends CompilerProblem>, ProblemID> classToIDMap = createClassToIDMap();");
        writer.println("    private static ImmutableMap<Class<? extends CompilerProblem>, ProblemID> createClassToIDMap()");
        writer.println("    {");
        writer.println("        final ImmutableMap.Builder<Class<? extends CompilerProblem>, ProblemID> builder = new ImmutableMap.Builder<Class<? extends CompilerProblem>, ProblemID>();");
        writer.println("        for (ProblemID id : EnumSet.allOf(ProblemID.class))");
        writer.println("            builder.put(id.getProblemClass(), id);");
        writer.println("        return builder.build();");
        writer.println("    }");
        writer.println("}");
    }

    private static String getProblemEnumEntry(String problemClassName) {
        String problemTypeName = problemClassName.substring(0, problemClassName.length() - "class".length());
        String enumConstantName = getEnumName(problemTypeName.replaceAll("Problem$", ""));
        return enumConstantName + "(" + problemTypeName + ".class" + ")";
    }

    private static String getEnumName(String problemTypeName) {
        return "PROBLEM_" + problemTypeName;
    }

    @Override
    protected void clean(File outputFile) throws MojoExecutionException {
        // If the file already exists, delete it before generating output.
        if(outputFile.exists()) {
            if(!outputFile.delete()) {
                throw new MojoExecutionException("Could not clear previously created file: " + outputFile.getPath());
            }
        }
    }

}
