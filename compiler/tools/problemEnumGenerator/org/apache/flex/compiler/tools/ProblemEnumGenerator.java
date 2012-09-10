/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

/**
 * Create a problems_en.properties file filled with localization strings from
 * classes extending org.apache.flex.compiler.CompilerProblem.
 * 
 * @see org.apache.flex.compiler.problems.CompilerProblem
 */
public class ProblemEnumGenerator
{

    /**
     * Program that generates a java enum with an entry for every compiler problem
     * class that can be instiated ( ie abstract classes are not in the enum ).
     */
    public static void main(String[] args) throws Throwable
    {
        File problemsDir = new File(args[0]);
        File output = new File(args[1], "ProblemID.java");
        if (!output.getParentFile().exists())
            output.getParentFile().mkdirs();
        PrintWriter writer = new PrintWriter(new FileWriter(output));
        try
        {
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
            writer.println("package org.apache.flex.compiler.problems;");
            writer.println();
            writer.println("import java.util.EnumSet;");
            writer.println("import com.google.common.collect.ImmutableMap;");
            writer.println();
            writer.println("public enum ProblemID");
            writer.println("{");
            
            Collection<String> problemClassNames =
                getProblemClasses(problemsDir);
            Collection<String> problemEnumEntries = 
                getProblemEnumEntries(problemClassNames);
            
            int i = 0;
            int n = problemEnumEntries.size();
            for (String enumEntry : problemEnumEntries)
            {
                writer.print("    " + enumEntry);
                if (i < (n - 1))
                {
                    writer.println(",");
                    ++i;
                }
                else
                {
                    writer.println(";");
                }
            }
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
        finally
        {
            writer.close();
        }
    }
    
    private static Collection<String> getProblemEnumEntries(Collection<String> problemClassNames)
    {
        return Collections2.transform(problemClassNames, new Function<String, String>() {

            @Override
            public String apply(String input)
            {
                return getProblemEnumEntry(input);
            }});
    }
    
    private static String getProblemEnumEntry(String problemClassName)
    {
        final String enumConstantName = getEnumName(problemClassName);
        return enumConstantName + "(" + problemClassName + ".class" + ")";
    }
    
    private static String getEnumName(String problemClassName)
    {
        return "PROBLEM_" + problemClassName.replaceAll("Problem$", "");
    }

    private static ImmutableSet<String> nonProblemClassesToFilter =
            new ImmutableSet.Builder<String>()
                .add("DefaultSeverity.java")
                .add("CompositeProblemFilter.java")
                .add("FilteredIterator.java")
                .add("CompilerProblemSeverity.java")
                .build();
    
    
    private static boolean isProblemClass(File javaFile)
    {
        if (nonProblemClassesToFilter.contains(javaFile.getName()))
            return false;
        final String javaClassName = FilenameUtils.getBaseName(javaFile.getAbsolutePath());
        try
        {
            Collection<String> lines =  Files.readLines(javaFile, Charset.forName("UTF8"));
            for (String line : lines)
            {
                if (line.matches("^\\s*public\\s+final\\s+class\\s+" + javaClassName + "\\s+extends\\s+.*"))
                    return true;
                if (line.matches("^\\s*final\\s+public\\s+class\\s+" + javaClassName + "\\s+extends\\s+.*"))
                    return true;
                if (line.matches("^\\s*public\\s+class\\s+" + javaClassName + "\\s+extends\\s+.*"))
                    return true;
            }
            return false;
        }
        catch (IOException e)
        {
            return false;
        }
        
    }
    
    /**
     * Recursively search a directory and its sub-directories for .class files.
     * Use the file name to determine what equivalent class name should be
     * available to the current classloader.
     * 
     * @param dir - the directory to search
     * @param packageName - keep track of the package name searched so far
     * @return list of classes found
     * @throws ClassNotFoundException
     */
    private static Collection<String> getProblemClasses(File dir)
    {   
        Collection<File> problemFiles = FileUtils.listFiles(dir, new String[] { "java" }, true);
        return Collections2.transform(Collections2.filter(problemFiles, new Predicate<File>() {

            @Override
            public boolean apply(File input)
            {
                return isProblemClass(input);
            }}), new Function<File, String>() {

            @Override
            public String apply(File input)
            {
                return FilenameUtils.getBaseName(input.getAbsolutePath());
            }});
    }

}
