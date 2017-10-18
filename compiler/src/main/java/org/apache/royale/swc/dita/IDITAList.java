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

package org.apache.royale.swc.dita;

import java.util.List;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.definitions.IDefinition;

/**
 * An IDITAList represents the entries contained within the DITA manifest for a given SWC
 */
public interface IDITAList
{
    /**
     * Returns all of the {@link IDITAEntry} elements contained within this SWC
     */
    List<IDITAEntry> getEntries();
    
    /**
     * Returns true if this list contains any entries
     */
    boolean hasEntries(); 
    
    /**
     * Returns the IDITAEntry for the given package.
     * @param packageName the package name we want to find an {@link IDITAEntry} for
     * @return an {@link IDITAEntry} or null
     */
    IDITAEntry getEntry(String packageName);
    
    /**
     * Returns an {@link IASDocComment} for the given {@link IDefinition} we are looking for.  This method will first check for a matching package entry based on the information
     * in the {@link IDefinition}, and seek in appropriately.  This is a convenience method for first calling getEntry, and then {@link IDITAList#getComment(IDefinition)}
     * @param definition the {@link IDefinition} we are looking at
     * @return an {@link IASDocComment} or null
     * @throws Exception an exception if any errors occur.  These can include IO errors among others
     */
    IASDocComment getComment(IDefinition definition) throws Exception;
}
