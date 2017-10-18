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

package org.apache.royale.compiler.units.requests;

import java.util.Set;

import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.units.ICompilationUnit;
import com.google.common.collect.ImmutableSet;

/**
 * Interface implemented by result objects for the request syntax tree operation of
 * {@link ICompilationUnit}'s.
 */
public interface ISyntaxTreeRequestResult extends IRequestResult
{
    /**
     * @return null, when there is no AST. The root node of the AST when there
     * is an AST. For a CompilationUnit that processes source, this should be a
     * FileNode or MXMLFileNode
     */
    IASNode getAST() throws InterruptedException;

    /**
     * @return A long integer representing a timestamp of when the files for this {@link ICompilationUnit} was last modified
     */
    long getLastModified();

    /**
     * @return the resource bundles that are referenced in the file 
     * associated with this tree.
     */
    Set<String> getRequiredResourceBundles() throws InterruptedException;

    /**
     * @return Set of filenames which are included by this AST.
     * An empty Set is returned if no files are included.
     */
    ImmutableSet<String> getIncludedFiles();
}
