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

import java.util.Collection;

import org.apache.royale.compiler.embedding.IEmbedData;
import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 * Result object for the GET_ABC_BYTES operation on ICompilationUnit.
 * 
 * @see org.apache.royale.compiler.units.ICompilationUnit
 */
public interface IABCBytesRequestResult extends IRequestResult
{
    /**
     * A static byte array of length zero that has no bytecode.
     */
    static final byte[] ZEROBYTES = {};

    /**
     * A static {@link ICompilerProblem} array of length zero.
     */
    static final ICompilerProblem[] ZEROPROBLEMS = {};
    
    /**
     * @return abc bytes generated for the CompilationUnit. This value is guaranteed to be non-null.
     */
    byte[] getABCBytes();

    /**
     * @return any embedded asset generated for the CompilationUnit. This value is guaranteed to be non-null.
     */
    Collection<IEmbedData> getEmbeds();
}
