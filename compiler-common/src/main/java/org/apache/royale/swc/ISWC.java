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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swc.dita.IDITAList;


/**
 * This is the interface of a model representing a SWC file.
 */
public interface ISWC
{
    /**
     * Get the SWC version information.
     * @return version
     */
    ISWCVersion getVersion();
    
    /**
     * Get the manifest information.
     * @return manifest information
     */
    List<ISWCComponent> getComponents();
    
    /**
     * Get the SWF libraries. For example: library.swf.
     * @return libraries
     */
    Collection<ISWCLibrary> getLibraries();
    
    /**
     * Get a library entry by path.
     * @param libraryPath The path of a library.  For exmaple: library.swf.
     * @return libraries
     */
    ISWCLibrary getLibrary(String libraryPath);
    
    /**
     * Get the assets information encoded in the file tags.
     * @return assets
     */
    Map<String, ISWCFileEntry> getFiles();

    /**
     * Get the asset information encoded in the file tags for the specific filename
     * 
     * @param filename The filename to search for in the swc
     * @return ISWCFileEntry for the filename, or null if not contained within the SWC
     */
    ISWCFileEntry getFile(String filename);

    /**
     * Get the SWC file object.
     * @return file object
     */
    File getSWCFile();
    
    /**
     * Get the {@link IDITAList} for any DITA entries contained in this SWC
     * @return an {@link IDITAList} or null if this SWC dosn't contain DITA content
     */
    IDITAList getDITAList();
    
    /**
     * Determine whether this SWC is an ANE file.
     * 
     * @return true if the this SWC is an ANE file, false otherwise.
     */
    boolean isANE();
    
    /**
     * The problems found while reading a SWC from disk or writing a SWC to disk.
     * 
     * @return a collection of problems.
     */
    Collection<ICompilerProblem> getProblems();
    
}
