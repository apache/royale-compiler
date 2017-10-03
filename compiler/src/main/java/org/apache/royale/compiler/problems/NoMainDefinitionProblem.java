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

package org.apache.royale.compiler.problems;


/**
 * {@link ICompilerProblem} implementation when a {@link org.apache.royale.compiler.units.ICompilationUnit} does
 * not define an definition it should based on its location in the source path
 * directory hierarchy.
 * <p>
 * For example, if the source path is ./foo and there is a source file
 * ./foo/p/A.as then this {@link ICompilerProblem} will be emitted when A.as
 * does not define a public or internal definition in package p.
 */
public final class NoMainDefinitionProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "No externally-visible definition with the name '${qname}' was found.";

    public static final int errorCode = 1493;
    
    public NoMainDefinitionProblem(String fileName, String qname)
    {
        super(fileName);
        this.qname = qname;
    }

    public final String qname;
}
