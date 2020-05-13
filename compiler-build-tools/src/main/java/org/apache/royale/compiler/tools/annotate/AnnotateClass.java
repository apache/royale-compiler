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

package org.apache.royale.compiler.tools.annotate;

import java.io.*;

public class AnnotateClass
{
    public static class AnnotateClassDeleteException extends Exception
    {
        private static final long serialVersionUID = 1L;

        public AnnotateClassDeleteException(String s)
        {
            super(s);
        }
    }
    
    public static class AnnotateClassRenameException extends Exception
    {
        private static final long serialVersionUID = 1L;

        public AnnotateClassRenameException(String s)
        {
            super(s);
        }
    }
    
    public static void processFile(File file, String annotation, String dateStart, String dateEnd) throws AnnotateClassDeleteException, AnnotateClassRenameException {
        if(!file.exists()) {
            System.out.println("Missing file: " + file.getPath());
            return;
        }
        String comment = "";
        if (dateStart.length() > 0)
        {
            int c = dateStart.indexOf("***");
            if (c > -1)
            {
                comment = dateStart.substring(0, c);
                dateStart = dateStart.substring(c + 3);
            }
            System.out.println("searching for generated date line starting with: '" + dateStart + "'");
            if (comment.length() > 0)
                System.out.println("and comment starting with: '" + comment + "'");
        }
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
                boolean alreadyAnnotated = false;
                while ((line = bufferedReader.readLine()) != null)
                {
                    if (dateStart.length() > 0)
                    {
                        if ((comment.length() > 0 && line.startsWith(comment)) || comment.length() == 0)
                        {
                            int c = line.indexOf(dateStart);
                            if (c > -1)
                            {
                                if (dateEnd.length() > 0)
                                {
                                    int c1 = line.lastIndexOf(dateEnd);
                                    if (c1 > 0)
                                    {
                                        line = comment + dateStart + line.substring(c1);
                                    }
                                }
                                else
                                    line = comment + dateStart;
                            }
                        }
                    }
                    // If the class is already annotated, prevent us from doing it again.
                    if (line.contains(annotation)) {
                        alreadyAnnotated = true;
                        System.out.println("Annotation " + annotation + " already added to class: " + file.getPath());
                    }
                    // If the line starts with "public class", output the annotation on the previous line.
                    if (!alreadyAnnotated &&
                            (line.startsWith("public class") || line.startsWith("public interface"))) {
                        System.out.println("Adding " + annotation + " to class: " + file.getPath());
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
                try {
                    inputStreamReader.close();
                } catch(Exception e) {
                    // Ignore.
                }
                try {
                    fileInputStream.close();
                } catch(Exception e) {
                    // Ignore.
                }
            }

            // Remove the original file.
            if(!file.delete()) {
                // wait a bit then retry on Windows
                if (file.exists())
                {
                    for (int i = 0; i < 6; i++)
                    {
                        Thread.sleep(500);
                        System.gc();
                        if (file.delete())
                           break;
                    }
                    if (file.exists())
                        throw new AnnotateClassDeleteException("Error deleting original file at: " + file.getPath());
                }
            }

            // Rename the temp file to the name of the original file.
            if(!tmpOutputFile.renameTo(file)) {
                throw new AnnotateClassRenameException("Error renaming the temp file from: " + tmpOutputFile.getPath() +
                        " to: " + file.getPath());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args)
    {
        File f = new File(args[0]);
        String annotation = args[1];
        String dateStart = args[2];
        String dateEnd = args[3];
        try {
            processFile(f, annotation, dateStart, dateEnd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
