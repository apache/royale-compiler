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

package org.apache.royale.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

/**
 * Utility class to normalize filenames.  All Files entering the driver
 * should be run through this class
 */
public class FilenameNormalization
{
    /**
     * Normalizes an array of files.
     * 
     * @see #normalize(File)
     * @param files Array of files to normalize.
     * @return Array of normalized files.
     */
    public static File[] normalize(File[] files)
    {
        File[] result = new File[files.length];
        for (int i = 0; i < files.length; ++i)
        {
            result[i] = normalize(files[i]);
        }
        return result;
    }

    /**
     * Normalize a {@link File}.  This method normalizes the case of the file path
     * characters and then calls {@link FilenameUtils#normalize(String)}.
     * @see FilenameUtils#normalize(String)
     * @param f A file.
     * @return The normalized File.
     */
    public static File normalize(File f)
    {
        String caseNormalizedAbsPath = f.getAbsolutePath();
        return new File(FilenameUtils.normalize(caseNormalizedAbsPath));
    }

    /**
     * Normalize a {@link String}. This method normalizes the case of the file
     * path characters and then calls {@link FilenameUtils#normalize(String)}.
     * 
     * @see FilenameUtils#normalize(String)
     * @param path The fiel path.
     * @return The normalized String. If the given path is already normalized,
     * the original string object will be returned.
     */
    public static String normalize(String path)
    {
        File f = new File(path);
        String caseNormalizedAbsPath = f.getAbsolutePath();
        String normalized = FilenameUtils.normalize(caseNormalizedAbsPath);

        if (normalized == null)
            return path;

        // If the path is already normalized, return the original string object
        // to prevent duplicated string objects.
        if (normalized.equals(path))
            return path;
        else
            return normalized;
    }
    
    /**
     * Determines whether a file path is in normalized form.
     * 
     * @param path A file path.
     */
    public static boolean isNormalized(String path)
    {
        String normalizedPath = normalize(path);
        return normalizedPath.equals(path);
    }

    /**
     * Get the normalized file path of a Java {@link File} object.
     * 
     * @param file File object.
     * @return Normalized file path.
     */
    public static String normalizeFileToPath(File file)
    {
        return normalize(file.getAbsolutePath());
    }
}
