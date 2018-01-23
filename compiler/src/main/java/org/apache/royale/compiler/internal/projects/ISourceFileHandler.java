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

package org.apache.royale.compiler.internal.projects;

import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * Factory interface for creating CompilationUnits.
 */
public interface ISourceFileHandler
{
    /**
     * Gets an array of Strings containing the file extensions that handler
     * supports.
     * <p>
     * Caller should not modify returned array.
     * 
     * @return Array of Strings containing the file extensions that handler
     * supports.
     */
    String[] getExtensions();
    
    /**
     * Determines if a new {@link ICompilationUnit} should be created
     * for the specified file, qualified name, and locale.
     * @param project {@link CompilerProject} For which a {@link ICompilationUnit} would be needed.
     * @param path Absolute file name for which a {@link ICompilationUnit} would be needed.
     * @param locale locale of the file if the file is locale dependent or 
     * <code>null</code> if the file is not locale dependent.
     * @return true if a new {@link ICompilationUnit} should be created, false otherwise.
     */
    boolean needCompilationUnit(CompilerProject project,
                                String path,
                                String qname,
                                String locale);

    /**
     * Creates a new {@link ICompilationUnit} instance for the specified project
     * and file.
     * 
     * @param project {@link CompilerProject} the new {@link ICompilationUnit}
     * will be added to.
     * @param path Path of the file which should be parsed by the new
     * {@link ICompilationUnit}.
     * @param priority {@link DefinitionPriority.BasePriority} used to determine
     * if definitions defined by the new {@link ICompilationUnit} shadow
     * definitions defined by other {@link ICompilationUnit}s.
     * @param order The index of the entry in the source path or source list
     * that is causing this method to be called.
     * @param qname The fully-qualified name of the one externally-visible
     * definition expected to be found in this compilation unit, or null if none
     * is expected. This name is determined from the name of the file and the
     * file's location relative to the source path.
     * @param locale locale of the file if the file is locale dependent or
     * <code>null</code> if the file is not locale dependent.
     * @return A new {@link ICompilationUnit}.
     */
    ICompilationUnit createCompilationUnit(CompilerProject project,
                                           String path,
                                           DefinitionPriority.BasePriority priority,
                                           int order,
                                           String qname, 
                                           String locale);
    
    /**
     * Determines if the handler can create invisible compilation units.
     * 
     * @return true if the handler can create invisible compilation units, false
     * otherwise.
     */
    boolean canCreateInvisibleCompilationUnit();
}
