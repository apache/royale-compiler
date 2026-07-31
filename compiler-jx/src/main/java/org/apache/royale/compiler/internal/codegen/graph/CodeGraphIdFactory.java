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

import java.util.List;

public final class CodeGraphIdFactory
{
    private static final String SCHEME = "as3://";

    private CodeGraphIdFactory()
    {
    }

    public static String definition(String qualifiedName)
    {
        return SCHEME + qualifiedName.replace('.', '/');
    }

    public static String member(String ownerQualifiedName, String memberName)
    {
        return definition(ownerQualifiedName) + "#" + memberName;
    }

    public static String accessor(String ownerQualifiedName, String propertyName, boolean getter)
    {
        return member(ownerQualifiedName, propertyName) + (getter ? ":get" : ":set");
    }

    public static String callable(String ownerQualifiedName, String callableName, List<String> parameterTypes)
    {
        StringBuilder result = new StringBuilder(member(ownerQualifiedName, callableName));
        appendParameters(result, parameterTypes);
        return result.toString();
    }

    public static String packageCallable(String qualifiedName, List<String> parameterTypes)
    {
        StringBuilder result = new StringBuilder(definition(qualifiedName));
        appendParameters(result, parameterTypes);
        return result.toString();
    }

    private static void appendParameters(StringBuilder result, List<String> parameterTypes)
    {
        result.append('(');
        for (int i = 0; i < parameterTypes.size(); i++)
        {
            if (i > 0)
                result.append(',');
            result.append(parameterTypes.get(i));
        }
        result.append(')');
    }

    public static String constructor(String ownerQualifiedName, List<String> parameterTypes)
    {
        return callable(ownerQualifiedName, "constructor", parameterTypes);
    }
}