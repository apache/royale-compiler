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
import java.util.Locale;

/**
 * A reliable way to identify and compare file paths.
 * Makes it impossible to do a case-sensitive comparison.
 */
public class FileID
{
    /**
     * Constructor.
     * 
     * @param file will be normalized
     */
    public FileID(File file)
    {
        lowerCasePath = fileToLowerCaseString(file);
        assert lowerCasePath != null;
        originalFile = file;
    }
   
    private final String lowerCasePath;
    private final File originalFile;        // the actual file that was passed in ctor
    
    @Override
    public String toString()
    {
        return lowerCasePath;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof FileID))
            return false;
        
        FileID otherId = (FileID)other;
        
        return this.lowerCasePath.equals(otherId.lowerCasePath);  
    }
    
    @Override
    public int hashCode()
    {
        return this.lowerCasePath.hashCode();
    }

    /**
     * 
     * @return the original file object passed to our ctor.
     */
    public File getFile()
    {
        return originalFile;        // return the original, so we preserve the case. 
                                    // (Otherwise we could re-create from lowerCasePath)
    }
    
    /**
     * Helper function. We run all strings through here to make sure
     * we never to a case sensitive compare. Also normalizes them.
     */
    static String fileToLowerCaseString(File f)
    {
        // First convert to normalized path
        final String normalizedPath = FilenameNormalization.normalize(f.getAbsolutePath());
        
        // now make lower case
        // Use US locale for case conversion.
        // This is preferable to using whatever locale the user happens to have, even though
        // it's possible that this won't be correct on every possible file system.
        return normalizedPath.toLowerCase(Locale.US);  
    }
    
    String getLowerCasePath()
    {
        return this.lowerCasePath;
    }
}
