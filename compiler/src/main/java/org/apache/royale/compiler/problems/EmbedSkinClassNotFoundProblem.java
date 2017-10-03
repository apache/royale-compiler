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
 * This problem is created when trying to embed a skin asset, but the specified
 * skinClass could not be found
 */
public final class EmbedSkinClassNotFoundProblem extends CompilerProblem
{
    public static final String DESCRIPTION = 
        "Class ${skinClass} could not be found";

    public static final int errorCode = 1351;
    
    public EmbedSkinClassNotFoundProblem(ISourceLocation site, String skinClass)
    {
        super(site);
        this.skinClass = skinClass;
    }

    public final String skinClass;
}
