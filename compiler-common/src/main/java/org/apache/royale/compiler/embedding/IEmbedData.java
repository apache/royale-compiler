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

package org.apache.royale.compiler.embedding;

import java.util.Collection;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.embedding.transcoders.ITranscoder;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.swc.ISWCFileEntry;

/**
 * This is the main class which contains all information extracted from embed
 * meta data.
 */
public interface IEmbedData
{
    /**
     * Add an attribute
     * 
     * @param project containing project
     * @param location source location of the attribute
     * @param key attribute key
     * @param value attribute value
     * @param problems any problems with the key or value
     * @return true if there was an error
     */
    boolean addAttribute(ICompilerProject project, ISourceLocation location, String key, String value, Collection<ICompilerProblem> problems);

    /**
     * Returns the value of an attribute.
     * 
     * @param attribute An embed attribute.
     * @return value of an attribute.  null if attribute does not exist
     */
    Object getAttribute(EmbedAttribute attribute);

    /**
     * @return All attributes
     */
    EmbedAttribute[] getAttributes();

    /**
     * @param project The compiler project.
     * @param location The source location.
     * @param problems The colleciton of compiler projects to which this method should add problems.
     * @return true if the transcoder was successfully constructed
     */
    boolean createTranscoder(ICompilerProject project, ISourceLocation location, Collection<ICompilerProblem> problems);

    /**
     * Returns the qname of the class generated from the EmbedData.  The name
     * is guaranteed to be unique and not conflict with user space names, unless
     * a user defined class has been decorated with the embed metadata, in which
     * case, the users class name will be returned.
     * @return qname
     */
    String getQName();

    /**
     * Check if the generated class extends another
     * 
     * @return true if another class is extended
     */
    boolean generatedClassExtendsAnother();

    /**
     * Get the transcoder used by this embed.  This can be null if there was
     * a problem with the Embed directive
     * 
     * @return transcoder
     */
    ITranscoder getTranscoder();

    /**
     * 
     * @return ISWCFileEntry entry to source asset contained within swc.  null if not contained within SWC
     */
    ISWCFileEntry getSWCSource();

}
