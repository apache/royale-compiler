/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.royale.test.ant.tasks.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

public class CompilationFileSetCollection
{
    protected List<FileSet> filesets;
    
    public CompilationFileSetCollection()
    {
        filesets = new ArrayList<FileSet>();
    }
    
    public void add(FileSet fileset)
    {
        filesets.add(fileset);
    }
    
    public boolean provided()
    {
        return filesets.size() != 0;
    }
    
    public boolean isEmpty()
    {
        if(filesets.isEmpty())
        {
            return true;
        }
        
        int includeCount = 0;
        
        for(FileSet fileset : filesets)
        {
            if(fileset.getDir().exists())
            {
                DirectoryScanner scanner = fileset.getDirectoryScanner();
                includeCount += scanner.getIncludedFilesCount();
            }
        }
        
        return includeCount == 0;
    }
    
    public boolean exists()
    {
        for(FileSet fileset : filesets)
        {
            if(!fileset.getDir().exists())
            {
                return false;
            }
        }
        
        return true;
    }
}
