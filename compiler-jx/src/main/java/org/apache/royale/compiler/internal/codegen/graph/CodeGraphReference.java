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

package org.apache.royale.compiler.internal.codegen.graph;

public final class CodeGraphReference
{
    private final String id;
    private final String qualifiedName;
    private final boolean external;
    private final boolean unresolved;

    public CodeGraphReference(String id, String qualifiedName, boolean external, boolean unresolved)
    {
        this.id = id;
        this.qualifiedName = qualifiedName;
        this.external = external;
        this.unresolved = unresolved;
    }

    public String getId()
    {
        return id;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public boolean isExternal()
    {
        return external;
    }

    public boolean isUnresolved()
    {
        return unresolved;
    }
}