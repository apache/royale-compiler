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

import org.apache.royale.compiler.tree.as.INamespaceDecorationNode;

/**
 * Only one of public, private, protected, or internal can be specified on a
 * definition.
 */
public final class MultipleReservedNamespaceAttributesProblem extends SyntaxProblem
{
    public static final String DESCRIPTION =
            "Only one of ${PUBLIC}, ${PRIVATE}, ${PROTECTED}, or ${INTERNAL} can be specified on a definition.";

    public static final int errorCode = 1154;

    public MultipleReservedNamespaceAttributesProblem(INamespaceDecorationNode site)
    {
        super(site, site.getName());
    }
    
    public final String PUBLIC = "public";
    public final String PRIVATE = "private";
    public final String PROTECTED = "protected";
    public final String INTERNAL= "internal";
}
