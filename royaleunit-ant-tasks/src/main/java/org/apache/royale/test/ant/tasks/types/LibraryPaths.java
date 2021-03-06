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

import java.io.File;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

public class LibraryPaths extends CompilationFileSetCollection
{
    public LibraryPaths()
    {
        super();
    }
    
    @Override
    public void add(FileSet fileset)
    {
        super.add(fileset);
    }
    
    public String getPathElements(String delimiter)
    {
        StringBuilder elements = new StringBuilder();
        
        for(FileSet fileset : filesets)
        {
            DirectoryScanner ds = fileset.getDirectoryScanner();
            String[] files = ds.getIncludedFiles();
            for(int i=0; i<files.length; i++)
            {
                if(files[i].endsWith(".swc"))
                {
                    elements.append("\"" + fileset.getDir().getAbsolutePath() + File.separator + files[i] + "\"");
                    elements.append(delimiter);
                }
            }
        }
        
        return elements.length() <= delimiter.length() ? "" : elements.substring(0, elements.length() - delimiter.length());
    }
}
