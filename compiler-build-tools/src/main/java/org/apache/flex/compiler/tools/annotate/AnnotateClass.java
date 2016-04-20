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

package org.apache.flex.compiler.tools.annotate;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AnnotateClass
{
    public static class AnnotateClassDeleteException extends Exception
    {
        public AnnotateClassDeleteException(String s)
        {
            super(s);
        }
    }
    
    public static class AnnotateClassRenameException extends Exception
    {
        public AnnotateClassRenameException(String s)
        {
            super(s);
        }
    }
    
    public static void processFile(File file, String annotation) throws AnnotateClassDeleteException, AnnotateClassRenameException {
        if(!file.exists()) {
            System.out.println("Missing file: " + file.getPath());
            return;
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
            }

            // Remove the original file.
            if(!file.delete()) {
                throw new AnnotateClassDeleteException("Error deleting original file at: " + file.getPath());
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
        try {
            processFile(f, annotation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
