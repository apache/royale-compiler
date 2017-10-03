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

import org.apache.royale.compiler.problems.annotations.DefaultSeverity;

@DefaultSeverity(CompilerProblemSeverity.WARNING)
public final class MissingFactoryClassInFrameMetadataProblem extends CompilerProblem
{
    public static final String DESCRIPTION =
        "This compilation unit did not have a ${FACTORY_CLASS} specified in [${FRAME}] metadata to load the configured runtime shared libraries. To compile without runtime shared libraries either set the ${STATIC_LINK_RUNTIME_SHARED_LIBRARIES} option to ${TRUE} or remove the ${RUNTIME_SHARED_LIBRARIES} option.";
    		
    public static final int warningCode = 5016;
    public MissingFactoryClassInFrameMetadataProblem(String sourcePath)
    {
        super(sourcePath);
    }
    
    // Prevent these from being localized.
    public final String FACTORY_CLASS = "factoryClass";
    public final String FRAME = "Frame";
    public final String STATIC_LINK_RUNTIME_SHARED_LIBRARIES = "-static-link-runtime-shared-libraries";
    public final String TRUE = "true";
    public final String RUNTIME_SHARED_LIBRARIES = "-runtime-shared-libraries";
}
