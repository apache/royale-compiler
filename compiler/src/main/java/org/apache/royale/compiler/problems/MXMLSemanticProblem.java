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

import org.apache.royale.compiler.common.ISourceLocation;

/**
 * This is the base class for semantic problems in MXML documents.
 * <p>
 * Note that its constructors allow you to pass MXML DOM objects
 * such as {@link org.apache.royale.compiler.mxml.IMXMLTagData} and {@link org.apache.royale.compiler.mxml.IMXMLTagAttributeData},
 * from which the problem location is derived.
 */
public class MXMLSemanticProblem extends SemanticProblem
{
    public static final String DESCRIPTION =
        "Internal problem during semantic analysis of MXML";
    
    public static final int errorCode = 1441;
    
    public MXMLSemanticProblem(ISourceLocation site)
    {
        super(site);
    }
    
    public MXMLSemanticProblem(String sourcePath)
    {
        super(sourcePath);
    }
}
