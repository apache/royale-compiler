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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CodeGraphSymbol
{
    private final String id;
    private final String qualifiedName;
    private final String baseName;
    private final String packageName;
    private final String kind;
    private boolean external;
    private String source;
    private CodeGraphReference declaringType;
    private CodeGraphReference type;
    private CodeGraphReference returnType;
    private CodeGraphReference baseType;
    private CodeGraphASDoc asDoc;
    private final List<CodeGraphReference> interfaces = new ArrayList<CodeGraphReference>();
    private final List<CodeGraphParameter> parameters = new ArrayList<CodeGraphParameter>();
    private final List<CodeGraphMetadata> metadata = new ArrayList<CodeGraphMetadata>();
    private final List<CodeGraphSymbol> members = new ArrayList<CodeGraphSymbol>();

    public CodeGraphSymbol(String id, String qualifiedName, String baseName, String packageName, String kind)
    {
        this.id = id;
        this.qualifiedName = qualifiedName;
        this.baseName = baseName;
        this.packageName = packageName;
        this.kind = kind;
    }

    public String getId()
    {
        return id;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public String getKind()
    {
        return kind;
    }

    public boolean isExternal()
    {
        return external;
    }

    public void setExternal(boolean external)
    {
        this.external = external;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public CodeGraphReference getDeclaringType()
    {
        return declaringType;
    }

    public void setDeclaringType(CodeGraphReference declaringType)
    {
        this.declaringType = declaringType;
    }

    public CodeGraphReference getType()
    {
        return type;
    }

    public void setType(CodeGraphReference type)
    {
        this.type = type;
    }

    public CodeGraphReference getReturnType()
    {
        return returnType;
    }

    public void setReturnType(CodeGraphReference returnType)
    {
        this.returnType = returnType;
    }

    public CodeGraphReference getBaseType()
    {
        return baseType;
    }

    public void setBaseType(CodeGraphReference baseType)
    {
        this.baseType = baseType;
    }

    public CodeGraphASDoc getASDoc()
    {
        return asDoc;
    }

    public void setASDoc(CodeGraphASDoc asDoc)
    {
        this.asDoc = asDoc;
    }

    public void addInterface(CodeGraphReference interfaceReference)
    {
        interfaces.add(interfaceReference);
    }

    public List<CodeGraphReference> getInterfaces()
    {
        return Collections.unmodifiableList(interfaces);
    }

    public void addParameter(CodeGraphParameter parameter)
    {
        parameters.add(parameter);
    }

    public List<CodeGraphParameter> getParameters()
    {
        return Collections.unmodifiableList(parameters);
    }

    public void addMetadata(CodeGraphMetadata metadataTag)
    {
        metadata.add(metadataTag);
    }

    public List<CodeGraphMetadata> getMetadata()
    {
        return Collections.unmodifiableList(metadata);
    }

    public void addMember(CodeGraphSymbol member)
    {
        members.add(member);
    }

    public List<CodeGraphSymbol> getMembers()
    {
        return Collections.unmodifiableList(members);
    }
}