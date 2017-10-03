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

package org.apache.royale.compiler.targets;

import java.util.Collection;
import java.util.zip.ZipOutputStream;

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swc.ISWC;

/**
 * A target which can build a SWC.
 */
public interface ISWCTarget extends ITarget
{
    /**
     * Builds this target and writes the resulting SWC data to the specified
     * ZipOutputStream.
     * 
     * @param output ZipOutputStream to which entries are added for the SWC
     * data.
     * @param problemCollection Collection to which any ICompilerProblems are
     * added.
     * @return true, when the SWC was successfully compiled and written to the
     * specified channel. If false, then problemCollection will have at least
     * one entry explaining why the build failed.
     */
    boolean addToZipOutputStream(ZipOutputStream output, Collection<ICompilerProblem> problemCollection);

    /**
     * Build the target SWC and collect problems. Every time the method is
     * called, a new SWC model is created.
     * 
     * @param problems compilation problems output
     * @return ISWC if build is success
     */
    ISWC build(Collection<ICompilerProblem> problems);
    
    /**
     * Get's the {@link ISWFTarget} used by this target
     * to build the library.swf in the SWC.
     * @return the {@link ISWFTarget} used by this target
     * to build the library.swf in the SWC
     * @throws InterruptedException
     */
    ISWFTarget getLibrarySWFTarget() throws InterruptedException;
}
