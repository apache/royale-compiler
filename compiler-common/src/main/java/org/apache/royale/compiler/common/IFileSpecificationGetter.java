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

package org.apache.royale.compiler.common;

import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Interface that abstracts the acquisition of {@link IFileSpecification}'s which
 * are used to open files by the compiler.
 */
public interface IFileSpecificationGetter
{
    /**
     * Returns the most recent {@link IFileSpecification} given to the
     * implementation for a specified path. If the implementation has not seen
     * the specified path before, a new FileSpecification is returned.
     * 
     * @param fileName Normalized absolute file name for which a
     * {@link IFileSpecification} should be returned.
     * @return The most recent {@link IFileSpecification} given to the
     * implementation for a specified file name.
     */
    IFileSpecification getFileSpecification(String fileName);
    
    /**
     * Returns the {@link IWorkspace} associated with the implementation, must
     * never be null.
     * 
     * @return the {@link IWorkspace} associated with the implementation, must
     * never be null.
     */
    IWorkspace getWorkspace();
}
