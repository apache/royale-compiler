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
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Create a problems_en.properties file filled with localization strings from
 * classes extending org.apache.flex.compiler.CompilerProblem.
 * 
 * @see org.apache.flex.compiler.problems.CompilerProblem
 */
public class ProblemLocalizer
{
    private static final String NEW_LINE = System.getProperty("line.separator");

    /**
     * Entry point.
     * 
     * @param args [0] = root directory to search for compiled problem classes 
     * and in which to create the properties file
     */
    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws Throwable
    {
        File rootDir = new File(args[0]);
        StringBuilder sb = new StringBuilder();

        List<Class> classes = getClasses(rootDir, "org.apache.flex.compiler.problems");
        for (Class c : classes)
        {
            if (org.apache.flex.compiler.problems.CompilerProblem.class.isAssignableFrom(c))
            {
                String name = c.getSimpleName();
                Object description = null;

                try
                {
                    Field f = c.getDeclaredField("DESCRIPTION");
                    if (f != null)
                        description = f.get(null);
                }
                catch (Throwable t)
                {
                    // Ignore problem descriptions we couldn't access
                }

                if (description != null && description instanceof
                    String)
                {
                    String descriptionString = description.toString();

                    // Escape unwanted chars
                    descriptionString = descriptionString.replaceAll("\r\n", "\\\\r\\\\n");
                    descriptionString = descriptionString.replaceAll("\n", "\\\\n");

                    sb.append(name).append("=").append(descriptionString).append(NEW_LINE);
                }
            }
        }

        // Write out the messages_en.properties
        try
        {
            File propertiesFile = new File(args[1], "messages_en.properties");
            // This may not be buffered, but the performance of this doesn't matter.
            // Open the FileWriter for append. The message file contains
            // static messages so append the problem messages to the end.
            FileWriter fileWriter = new FileWriter(propertiesFile, true);
            PrintWriter out = new PrintWriter(fileWriter);
            out.write(sb.toString());
            out.flush();
            out.close();
        }
        catch (Throwable t)
        {
            System.err.println("Error writing problems file: " + t.getMessage());
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
    @SuppressWarnings("rawtypes")
    private static List<Class> getClasses(File dir, String packageName)
            throws ClassNotFoundException
    {   
        List<Class> classes = new ArrayList<Class>();
        if (!dir.exists())
        {
            return classes;
        }

        File[] files = dir.listFiles();
        for (File file : files)
        {
            final String fileName = file.getName();
            
            if (file.isDirectory())
            {
                String subPackage = packageName + "." + fileName;
                if (packageName.length() == 0)
                    subPackage = fileName;
                subPackage = subPackage.replace('/', '.');

                classes.addAll(getClasses(file, subPackage));
            }
            else if (file.getName().endsWith(".class"))
            {
                // Combine package and file name, strip *.class file extension
                String className = packageName + '.' +
                                   fileName.substring(0, fileName.length() - 6);
                try
                {
                    classes.add(Class.forName(className));
                }
                catch (Throwable t)
                {
                    // Ignore classes we can't find
                }
            }
        }
        return classes;
    }

}
