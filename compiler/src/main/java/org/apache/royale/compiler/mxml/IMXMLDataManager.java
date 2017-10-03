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

package org.apache.royale.compiler.mxml;

import org.apache.royale.compiler.filespecs.IFileSpecification;

/**
 * The {@code IMXMLDataManager} of the {@code IWorkspace} maintains a cache of
 * {@code IMXMLData} objects that serve as DOMs for MXML files.
 * <p>
 * If an MXML file is used in multiple projects, its {@code MXMLData} can be
 * shared. Its symbol table and parse tree cannot be shared, because the meaning
 * of various MXML tags could be different in different projects.
 */
public interface IMXMLDataManager
{
    /**
     * Gets the {@code IMXMLData} for the specified MXML file. If the manager
     * already has the {@code IMXMLData} for the file, it returns it. Otherwise,
     * it will parse the MXML file, store the {@code IMXMLData}, and return it.
     * 
     * @param fileSpec An {@code IFileSpecification} object specifying an MXML
     * file.
     * @return An {@code IMXMLData} object representing the tags, attributes, and
     * text in the MXML file.
     */
    IMXMLData get(IFileSpecification fileSpec);

    /**
     * Removes any cached {@link IMXMLData} objects for the specified file.
     * 
     * @param fileSpec An {@code IFileSpecification} for the file being
     * invalidated.
     */
    void invalidate(IFileSpecification fileSpec);
}
