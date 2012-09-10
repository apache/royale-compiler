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

package org.apache.flex.compiler.mxml;

import org.apache.flex.compiler.filespecs.IFileSpecification;

/**
 * The {@code IMXMLDataManager} of the {@code IWorkspace} maintains a cache
 * of {@code MXMLData} objects that serve as DOMs for MXML files.
 * <p>
 * If an MXML file is used in multiple projects, its {@code MXMLData}
 * can be shared. Its symbol table and parse tree cannot be shared,
 * because the meaning of various MXML tags could be different
 * in different projects.
 */
public interface IMXMLDataManager
{
    /**
     * Get the {@code MXMLData} for the specified MXML file.
     * If the manager already has the {@code MXMLData} for the file, it returns it.
     * Otherwise, it will parse the MXML file, store the {@code MXMLData}, and return it.
     * 
     * @param fileSpec An {@code IFileSpecification} object specifying an MXML file.
     * 
     * @return An {@code MXMLData} object representing
     * the tags, attributes, and text in the MXML file.
     */
    MXMLData get(IFileSpecification fileSpec);
    
    /**
     * Removes any cache'd {@link MXMLData}'s for the specified
     * file.
     * @param fileSpec
     */
    void invalidate(IFileSpecification fileSpec);
}
