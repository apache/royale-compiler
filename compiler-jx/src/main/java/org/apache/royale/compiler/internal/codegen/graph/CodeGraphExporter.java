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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.asdoc.IASDocComment;
import org.apache.royale.compiler.asdoc.IASDocTag;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IDocumentableDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IGetterDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.ISetterDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.projects.ICompilerProject;

public final class CodeGraphExporter
{
    private final ICompilerProject project;
    private final Set<String> exportedQualifiedNames = new HashSet<String>();
    private final Map<String, ITypeDefinition> externalDefinitions = new HashMap<String, ITypeDefinition>();

    public CodeGraphExporter(ICompilerProject project)
    {
        this.project = project;
    }

    public CodeGraphModel export(Collection<IDefinition> definitions, String target, String module)
    {
        CodeGraphModel model = new CodeGraphModel(target, module);
        exportedQualifiedNames.clear();
        externalDefinitions.clear();
        for (IDefinition definition : definitions)
        {
            if (definition.isPublic() && isSupportedType(definition))
                exportedQualifiedNames.add(definition.getQualifiedName());
        }
        for (IDefinition definition : definitions)
        {
            if (definition.isPublic() && isSupportedType(definition))
                model.addSymbol(exportType((ITypeDefinition)definition));
        }
        for (ITypeDefinition externalDefinition : externalDefinitions.values())
        {
            CodeGraphSymbol externalSymbol = createSymbol(externalDefinition, getTypeKind(externalDefinition));
            externalSymbol.setExternal(true);
            model.addExternalSymbol(externalSymbol);
        }
        return model;
    }

    private boolean isSupportedType(IDefinition definition)
    {
        return definition instanceof IClassDefinition || definition instanceof IInterfaceDefinition;
    }

    private CodeGraphSymbol exportType(ITypeDefinition definition)
    {
        CodeGraphSymbol symbol = createSymbol(definition, getTypeKind(definition));
        addMetadata(symbol, definition);
        addASDoc(symbol, definition);
        if (definition instanceof IClassDefinition)
        {
            IClassDefinition classDefinition = (IClassDefinition)definition;
            IClassDefinition baseClass = classDefinition.resolveBaseClass(project);
            if (baseClass != null)
                symbol.setBaseType(createReference(baseClass));
            for (IInterfaceDefinition interfaceDefinition : classDefinition.resolveImplementedInterfaces(project))
                symbol.addInterface(createReference(interfaceDefinition));
            IFunctionDefinition constructor = classDefinition.getConstructor();
            if (constructor != null && !constructor.isImplicit())
                symbol.addMember(exportFunction(constructor, definition));
        }
        else
        {
            IInterfaceDefinition interfaceDefinition = (IInterfaceDefinition)definition;
            for (IInterfaceDefinition extendedInterface : interfaceDefinition.resolveExtendedInterfaces(project))
                symbol.addInterface(createReference(extendedInterface));
        }

        for (IDefinition memberDefinition : definition.getContainedScope().getAllLocalDefinitions())
        {
            boolean isConstructor = memberDefinition instanceof IFunctionDefinition
                    && ((IFunctionDefinition)memberDefinition).isConstructor();
            if (!memberDefinition.isPublic() || memberDefinition.isImplicit() || isConstructor)
                continue;
            if (memberDefinition instanceof IFunctionDefinition)
                symbol.addMember(exportFunction((IFunctionDefinition)memberDefinition, definition));
            else if (memberDefinition instanceof IVariableDefinition)
                symbol.addMember(exportVariable((IVariableDefinition)memberDefinition, definition));
        }
        return symbol;
    }

    private CodeGraphSymbol exportFunction(IFunctionDefinition definition, ITypeDefinition declaringType)
    {
        String kind;
        String id;
        if (definition instanceof IGetterDefinition)
        {
            kind = "getter";
            id = CodeGraphIdFactory.accessor(declaringType.getQualifiedName(), definition.getBaseName(), true);
        }
        else if (definition instanceof ISetterDefinition)
        {
            kind = "setter";
            id = CodeGraphIdFactory.accessor(declaringType.getQualifiedName(), definition.getBaseName(), false);
        }
        else
        {
            kind = definition.isConstructor() ? "constructor" : "method";
            id = createCallableId(definition, declaringType);
        }

        CodeGraphSymbol symbol = new CodeGraphSymbol(id, definition.getQualifiedName(), definition.getBaseName(),
                definition.getPackageName(), kind);
        addMetadata(symbol, definition);
        addASDoc(symbol, definition);
        symbol.setDeclaringType(createReference(declaringType));
        if (definition instanceof IGetterDefinition || definition instanceof ISetterDefinition)
        {
            ITypeDefinition typeDefinition = definition.resolveType(project);
            if (typeDefinition != null)
                symbol.setType(createReference(typeDefinition));
        }
        else if (!definition.isConstructor())
        {
            ITypeDefinition returnType = definition.resolveReturnType(project);
            if (returnType != null)
                symbol.setReturnType(createReference(returnType));
        }
        for (IParameterDefinition parameterDefinition : definition.getParameters())
        {
            ITypeDefinition parameterType = parameterDefinition.resolveType(project);
            CodeGraphReference typeReference = parameterType == null ? null : createReference(parameterType);
            Object defaultValue = parameterDefinition.hasDefaultValue()
                    ? parameterDefinition.resolveDefaultValue(project) : null;
            symbol.addParameter(new CodeGraphParameter(parameterDefinition.getBaseName(), typeReference,
                    parameterDefinition.hasDefaultValue(), parameterDefinition.isRest(), defaultValue));
        }
        return symbol;
    }

    private String createCallableId(IFunctionDefinition definition, ITypeDefinition declaringType)
    {
        List<String> parameterTypes = new ArrayList<String>();
        for (IParameterDefinition parameterDefinition : definition.getParameters())
        {
            ITypeDefinition parameterType = parameterDefinition.resolveType(project);
            parameterTypes.add(parameterType == null
                    ? parameterDefinition.getTypeAsDisplayString() : parameterType.getQualifiedName());
        }
        if (definition.isConstructor())
            return CodeGraphIdFactory.constructor(declaringType.getQualifiedName(), parameterTypes);
        return CodeGraphIdFactory.callable(declaringType.getQualifiedName(), definition.getBaseName(), parameterTypes);
    }

    private CodeGraphSymbol exportVariable(IVariableDefinition definition, ITypeDefinition declaringType)
    {
        String kind = definition instanceof IConstantDefinition ? "constant" : "field";
        CodeGraphSymbol symbol = new CodeGraphSymbol(
                CodeGraphIdFactory.member(declaringType.getQualifiedName(), definition.getBaseName()),
                definition.getQualifiedName(), definition.getBaseName(), definition.getPackageName(), kind);
        addMetadata(symbol, definition);
            addASDoc(symbol, definition);
        symbol.setDeclaringType(createReference(declaringType));
        ITypeDefinition typeDefinition = definition.resolveType(project);
        if (typeDefinition != null)
            symbol.setType(createReference(typeDefinition));
        return symbol;
    }

    private CodeGraphSymbol createSymbol(IDefinition definition, String kind)
    {
        return new CodeGraphSymbol(CodeGraphIdFactory.definition(definition.getQualifiedName()),
                definition.getQualifiedName(), definition.getBaseName(), definition.getPackageName(), kind);
    }

    private String getTypeKind(ITypeDefinition definition)
    {
        return definition instanceof IClassDefinition ? "class" : "interface";
    }

    private void addMetadata(CodeGraphSymbol symbol, IDefinition definition)
    {
        for (IMetaTag metaTag : definition.getAllMetaTags())
        {
            if (IMetaTag.GO_TO_DEFINITION_HELP.equals(metaTag.getTagName()))
                continue;
            CodeGraphMetadata metadata = new CodeGraphMetadata(metaTag.getTagName());
            for (IMetaTagAttribute attribute : metaTag.getAllAttributes())
            {
                metadata.addAttribute(new CodeGraphMetadataAttribute(attribute.getKey(), attribute.getValue()));
            }
            symbol.addMetadata(metadata);
        }
    }

    private void addASDoc(CodeGraphSymbol symbol, IDefinition definition)
    {
        if (!(definition instanceof IDocumentableDefinition))
            return;
        IASDocComment comment = ((IDocumentableDefinition)definition).getExplicitSourceComment();
        if (comment == null)
            return;
        if (comment.getDescription() == null)
            comment.compile();
        CodeGraphASDoc asDoc = new CodeGraphASDoc(normalizeASDocText(comment.getDescription()));
        Map<String, List<IASDocTag>> tags = comment.getTags();
        if (tags != null)
        {
            List<String> tagNames = new ArrayList<String>(tags.keySet());
            Collections.sort(tagNames);
            for (String tagName : tagNames)
            {
                List<IASDocTag> tagValues = tags.get(tagName);
                if (tagValues == null || tagValues.isEmpty())
                {
                    asDoc.addTag(new CodeGraphASDocTag(tagName, null));
                    continue;
                }
                for (IASDocTag tag : tagValues)
                    asDoc.addTag(new CodeGraphASDocTag(tagName, normalizeASDocText(tag.getDescription())));
            }
        }
        symbol.setASDoc(asDoc);
    }

    private String normalizeASDocText(String value)
    {
        if (value == null)
            return null;
        return value.replace("\\\"", "\"");
    }

    private CodeGraphReference createReference(ITypeDefinition definition)
    {
        String qualifiedName = definition.getQualifiedName();
        boolean external = !exportedQualifiedNames.contains(qualifiedName);
        if (external)
            externalDefinitions.put(qualifiedName, definition);
        return new CodeGraphReference(CodeGraphIdFactory.definition(qualifiedName), qualifiedName, external, false);
    }
}