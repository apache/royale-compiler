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
import org.apache.royale.compiler.definitions.IDefinition;

/**
 * This class is the base class for all problems found during semantic analysis.
 */
public abstract class SemanticProblem extends AbstractSemanticProblem
{
    public static final String DESCRIPTION =
        "Internal problem during semantic analysis";
    
    public SemanticProblem(ISourceLocation site)
    {
        super(site);
    }
    
    public SemanticProblem(IDefinition site)
    {
        super(site);
    }
    
    public SemanticProblem(String sourcePath)
    {
        super(sourcePath);
    }
}
