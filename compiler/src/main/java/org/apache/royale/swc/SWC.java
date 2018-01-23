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

package org.apache.royale.swc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swc.dita.IDITAList;

/**
 * Implementation of {@code ISWC}. It contains the in-memory model of a SWC
 * file. TODO Use ZipFileSpecification as the implementation
 */
public class SWC implements ISWC
{
    /**
     * Use this constructor for reading or writing a SWC.
     * The file handle is the SWC file.
     * 
     * @param file SWC file object
     */
    public SWC(File file)
    {
        components = new ArrayList<ISWCComponent>();
        libraries = new HashMap<String, ISWCLibrary>(1);
        files = new HashMap<String, ISWCFileEntry>();
        swcFile = file;
        version = new SWCVersion();
    }

    private final List<ISWCComponent> components;
    private final Map<String, ISWCLibrary> libraries;
    private final Map<String, ISWCFileEntry> files;
    private final ISWCVersion version;
    private final File swcFile;
    private IDITAList ditaList;
    private boolean isANE;
    private final Collection<ICompilerProblem> problems = new ArrayList<ICompilerProblem>();

    public void addComponent(ISWCComponent component)
    {
        assert component != null;
        components.add(component);
    }

    public void addLibrary(ISWCLibrary library)
    {
        assert library != null;
        libraries.put(library.getPath(), library);
    }

    public void addFile(ISWCFileEntry file)
    {
        assert file != null;
        files.put(file.getPath(), file);
    }
    
    /**
     * Add a file that will be written into swc file during creation.
     * 
     * @param path destination path of the file relative to the root of swc. 
     * @param mod last modified date for the file
     * @param contentByteArray byte array representing the content of the file to write.
     */
    public void addFile(final String path, final long mod, final byte[] contentByteArray)
    {
        ISWCFileEntry entry = new ISWCFileEntry()
        {          
            @Override
            public String getPath()
            {
                 return path;
            }
            
            @Override
            public long getLastModified()
            {
                return mod;
            }
            
            @Override
            public String getContainingSWCPath()
            {
                return swcFile.getAbsolutePath();
            }
            
            @Override
            public InputStream createInputStream() throws IOException
            {
                //We will eventually need byteArray so creating a stream doesn't make sense. 
                //TODO: This requires to fix the ISWCFileEntry interface. 
                return new ByteArrayInputStream(contentByteArray);
            }
        };
        
        files.put(entry.getPath(), entry);
    }
    
    public void setDITAList(IDITAList list)
    {
        assert list != null;
        ditaList = list;
    }

    /**
     * Set whether this SWC is an ANE file or not.
     * 
     * @param isANE true if this SWC is an ANE file, false otherwise.
     */
    public void setIsANE(boolean isANE)
    {
        this.isANE = isANE;
    }
    
    @Override
    public List<ISWCComponent> getComponents()
    {
        return components;
    }

    @Override
    public Collection<ISWCLibrary> getLibraries()
    {
        return libraries.values();
    }

    @Override
    public ISWCLibrary getLibrary(String libraryPath)
    {
        return libraries.get(libraryPath);
    }

    @Override
    public ISWCVersion getVersion()
    {
        return version;
    }

    @Override
    public Map<String, ISWCFileEntry> getFiles()
    {
        return files;
    }

    @Override
    public ISWCFileEntry getFile(String filename)
    {
        return files.get(filename);
    }

    @Override
    public File getSWCFile()
    {
        return swcFile;
    }

    @Override
    public IDITAList getDITAList()
    {
        return ditaList;
    }
    
    @Override
    public boolean isANE()
    {
        return isANE;
    }
    
    /**
     * For debugging only.
     */
    @Override
    public String toString()
    {
        return getSWCFile().toString();
    }

    @Override
    public Collection<ICompilerProblem> getProblems()
    {
        return problems;
    }

    /**
     * Record a problem encountered while reading or writing the SWC.
     * 
     * @param problem the compiler problem to add to this SWC.
     */
    public void addProblem(ICompilerProblem problem)
    {
        problems.add(problem);
    }
}
