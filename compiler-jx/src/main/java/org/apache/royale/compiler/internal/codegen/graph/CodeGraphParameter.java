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

public final class CodeGraphParameter
{
    private final String name;
    private final CodeGraphReference type;
    private final boolean optional;
    private final boolean rest;
    private final Object defaultValue;

    public CodeGraphParameter(String name, CodeGraphReference type, boolean optional, boolean rest, Object defaultValue)
    {
        this.name = name;
        this.type = type;
        this.optional = optional;
        this.rest = rest;
        this.defaultValue = defaultValue;
    }

    public String getName()
    {
        return name;
    }

    public CodeGraphReference getType()
    {
        return type;
    }

    public boolean isOptional()
    {
        return optional;
    }

    public boolean isRest()
    {
        return rest;
    }

    public Object getDefaultValue()
    {
        return defaultValue;
    }
}