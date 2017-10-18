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

import org.apache.royale.compiler.tree.as.IASNode;

/**
 *  Diagnostic emitted when the semantic analyzer detects
 *  an attempted access to an inaccessible property, e.g.,
 *  a private var in a superclass.
 */
public final class InaccessiblePropertyReferenceProblem extends StrictSemanticsProblem
{
    public static final String DESCRIPTION =
        "Attempted access of inaccessible property ${propertyName} through a reference with static type ${className}.";

    public static final int errorCode = 1178;

    public InaccessiblePropertyReferenceProblem(IASNode site, String propertyName, String className)
    {
        super(site);
        this.propertyName = propertyName;
        this.className = className;
    }
    
    public final String propertyName;
    public final String className;
}
