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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.royale.abc.ABCConstants;
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
import org.apache.royale.compiler.projects.IASProject;
import org.apache.royale.compiler.tree.as.IDocumentableDefinitionNode;

/**
 * Translates compiler definitions into the portable code graph model.
 */
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
            if (definition.isPublic() && !isExcludedFromPublicAPI(definition) && isSupportedType(definition))
                exportedQualifiedNames.add(definition.getQualifiedName());
        }
        for (IDefinition definition : definitions)
        {
            if (!definition.isPublic() || isExcludedFromPublicAPI(definition))
                continue;
            if (isSupportedType(definition))
                model.addSymbol(exportType((ITypeDefinition)definition));
            else if (definition instanceof IFunctionDefinition)
                model.addSymbol(exportFunction((IFunctionDefinition)definition, null));
            else if (definition instanceof IVariableDefinition)
                model.addSymbol(exportVariable((IVariableDefinition)definition, null, false));
        }
        for (ITypeDefinition externalDefinition : externalDefinitions.values())
        {
            CodeGraphSymbol externalSymbol = createSymbol(externalDefinition, getTypeKind(externalDefinition));
            externalSymbol.setExternal(true);
            String origin = externalDefinition.getContainingFilePath();
            if (origin != null)
                externalSymbol.setOrigin(new File(origin).getName());
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
            String baseClassName = classDefinition.getBaseClassAsDisplayString();
            if (baseClass != null || (baseClassName != null && !baseClassName.isEmpty()))
                symbol.setBaseType(createReference(baseClass, baseClassName));
            addInterfaces(symbol, classDefinition.resolveImplementedInterfaces(project),
                    classDefinition.getImplementedInterfacesAsDisplayStrings());
            IFunctionDefinition constructor = classDefinition.getConstructor();
            if (constructor != null && !constructor.isImplicit())
                symbol.addMember(exportFunction(constructor, definition));
        }
        else
        {
            IInterfaceDefinition interfaceDefinition = (IInterfaceDefinition)definition;
            addInterfaces(symbol, interfaceDefinition.resolveExtendedInterfaces(project),
                    interfaceDefinition.getExtendedInterfacesAsDisplayStrings());
        }

        Set<String> collidingVariableNames = getStaticVariableCollisions(definition);
        for (IDefinition memberDefinition : definition.getContainedScope().getAllLocalDefinitions())
        {
            if (!isExportedMember(memberDefinition, definition))
                continue;
            if (memberDefinition instanceof IFunctionDefinition)
                symbol.addMember(exportFunction((IFunctionDefinition)memberDefinition, definition));
            else if (memberDefinition instanceof IVariableDefinition)
                symbol.addMember(exportVariable((IVariableDefinition)memberDefinition, definition,
                        collidingVariableNames.contains(memberDefinition.getBaseName())));
        }
        return symbol;
    }

    private Set<String> getStaticVariableCollisions(ITypeDefinition definition)
    {
        Set<String> staticNames = new HashSet<String>();
        Set<String> instanceNames = new HashSet<String>();
        for (IDefinition memberDefinition : definition.getContainedScope().getAllLocalDefinitions())
        {
            if (!(memberDefinition instanceof IVariableDefinition)
                    || !isExportedMember(memberDefinition, definition))
                continue;
            (memberDefinition.isStatic() ? staticNames : instanceNames).add(memberDefinition.getBaseName());
        }
        staticNames.retainAll(instanceNames);
        return staticNames;
    }

    private boolean isExportedMember(IDefinition memberDefinition, ITypeDefinition definition)
    {
        boolean isConstructor = memberDefinition instanceof IFunctionDefinition
                && ((IFunctionDefinition)memberDefinition).isConstructor();
        boolean isPublic = memberDefinition.isPublic() || definition instanceof IInterfaceDefinition;
        return isPublic && !memberDefinition.isImplicit() && !isConstructor
                && !isExcludedFromPublicAPI(memberDefinition);
    }

    private void addInterfaces(CodeGraphSymbol symbol, IInterfaceDefinition[] definitions, String[] displayNames)
    {
        int count = Math.max(definitions.length, displayNames.length);
        for (int i = 0; i < count; i++)
        {
            IInterfaceDefinition definition = i < definitions.length ? definitions[i] : null;
            String displayName = i < displayNames.length ? displayNames[i] : null;
            CodeGraphReference reference = createReference(definition, displayName);
            if (reference != null)
                symbol.addInterface(reference);
        }
    }

    private CodeGraphSymbol exportFunction(IFunctionDefinition definition, ITypeDefinition declaringType)
    {
        String kind;
        String id;
        if (declaringType == null)
        {
            kind = "function";
            id = createCallableId(definition, null);
        }
        else if (definition instanceof IGetterDefinition)
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
        addDefinitionDetails(symbol, definition);
        if (symbol.getVisibility().isEmpty() && declaringType != null)
            symbol.setVisibility(declaringType.getNamespaceReference().getBaseName());
        addMetadata(symbol, definition);
        addASDoc(symbol, definition);
        if (declaringType != null)
            symbol.setDeclaringType(createReference(declaringType));
        IFunctionDefinition overriddenFunction = definition.resolveOverriddenFunction(project);
        if (overriddenFunction != null)
            symbol.setOverriddenMember(createFunctionReference(overriddenFunction));
        CodeGraphReference implementedMember = resolveImplementedMember(definition, declaringType);
        if (implementedMember != null)
            symbol.setImplementedMember(implementedMember);
        if (definition instanceof IGetterDefinition || definition instanceof ISetterDefinition)
        {
            ITypeDefinition typeDefinition = definition.resolveType(project);
            symbol.setType(createReference(typeDefinition, definition.getTypeAsDisplayString()));
        }
        else if (!definition.isConstructor())
        {
            ITypeDefinition returnType = definition.resolveReturnType(project);
            symbol.setReturnType(createReference(returnType, definition.getReturnTypeAsDisplayString()));
        }
        for (IParameterDefinition parameterDefinition : definition.getParameters())
        {
            ITypeDefinition parameterType = parameterDefinition.resolveType(project);
            CodeGraphReference typeReference = createReference(parameterType,
                    parameterDefinition.getTypeAsDisplayString());
            Object defaultValue = parameterDefinition.hasDefaultValue()
                    ? parameterDefinition.resolveDefaultValue(project) : null;
            if (defaultValue == ABCConstants.UNDEFINED_VALUE)
                defaultValue = "undefined";
            else if (defaultValue == ABCConstants.NULL_VALUE)
                defaultValue = null;
            symbol.addParameter(new CodeGraphParameter(parameterDefinition.getBaseName(), typeReference,
                    parameterDefinition.hasDefaultValue(), parameterDefinition.isRest(), defaultValue));
        }
        return symbol;
    }

    private CodeGraphReference resolveImplementedMember(IFunctionDefinition definition,
            ITypeDefinition declaringType)
    {
        if (!(declaringType instanceof IClassDefinition))
            return null;
        String signature = getFunctionSignature(definition);
        CodeGraphReference result = null;
        Iterator<IInterfaceDefinition> interfaces = ((IClassDefinition)declaringType).interfaceIterator(project);
        while (interfaces.hasNext())
        {
            IInterfaceDefinition interfaceDefinition = interfaces.next();
            for (IDefinition memberDefinition : interfaceDefinition.getContainedScope().getAllLocalDefinitions())
            {
                if (!(memberDefinition instanceof IFunctionDefinition))
                    continue;
                IFunctionDefinition candidate = (IFunctionDefinition)memberDefinition;
                if (!signature.equals(getFunctionSignature(candidate)))
                    continue;
                CodeGraphReference reference = createFunctionReference(candidate);
                if (result == null || reference.getId().compareTo(result.getId()) < 0)
                    result = reference;
            }
        }
        return result;
    }

    private String getFunctionSignature(IFunctionDefinition definition)
    {
        StringBuilder result = new StringBuilder();
        result.append(definition instanceof IGetterDefinition ? "get:" :
                definition instanceof ISetterDefinition ? "set:" : "function:");
        result.append(definition.getBaseName()).append('(');
        for (IParameterDefinition parameterDefinition : definition.getParameters())
        {
            ITypeDefinition parameterType = parameterDefinition.resolveType(project);
            result.append(parameterType == null ? parameterDefinition.getTypeAsDisplayString()
                    : parameterType.getQualifiedName()).append(',');
        }
        return result.append(')').toString();
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
        if (declaringType == null)
            return CodeGraphIdFactory.packageCallable(definition.getQualifiedName(), parameterTypes);
        return CodeGraphIdFactory.callable(declaringType.getQualifiedName(), definition.getBaseName(), parameterTypes);
    }

    private CodeGraphSymbol exportVariable(IVariableDefinition definition, ITypeDefinition declaringType,
            boolean hasStaticCollision)
    {
        String kind = definition instanceof IConstantDefinition ? "constant"
            : declaringType == null ? "variable" : "field";
        String id = declaringType == null ? CodeGraphIdFactory.definition(definition.getQualifiedName())
            : hasStaticCollision && definition.isStatic()
                    ? CodeGraphIdFactory.staticMember(declaringType.getQualifiedName(), definition.getBaseName())
                    : CodeGraphIdFactory.member(declaringType.getQualifiedName(), definition.getBaseName());
        CodeGraphSymbol symbol = new CodeGraphSymbol(id,
                definition.getQualifiedName(), definition.getBaseName(), definition.getPackageName(), kind);
        addDefinitionDetails(symbol, definition);
        addMetadata(symbol, definition);
        addASDoc(symbol, definition);
        if (declaringType != null)
            symbol.setDeclaringType(createReference(declaringType));
        ITypeDefinition typeDefinition = definition.resolveType(project);
        symbol.setType(createReference(typeDefinition, definition.getTypeAsDisplayString()));
        if (definition.getVariableNode() != null && definition.getVariableNode().getAssignedValueNode() != null)
            symbol.setInitialValue(definition.resolveInitialValue(project));
        return symbol;
    }

    private boolean isExcludedFromPublicAPI(IDefinition definition)
    {
        if (!(definition instanceof IDocumentableDefinition))
            return false;
        IASDocComment comment = ((IDocumentableDefinition)definition).getExplicitSourceComment();
        if (comment == null)
            return false;
        if (comment.getDescription() == null)
            comment.compile();
        return comment.hasTag("private");
    }

    private CodeGraphSymbol createSymbol(IDefinition definition, String kind)
    {
        CodeGraphSymbol symbol = new CodeGraphSymbol(CodeGraphIdFactory.definition(definition.getQualifiedName()),
                definition.getQualifiedName(), definition.getBaseName(), definition.getPackageName(), kind);
        addDefinitionDetails(symbol, definition);
        return symbol;
    }

    private void addDefinitionDetails(CodeGraphSymbol symbol, IDefinition definition)
    {
        symbol.setVisibility(definition.getNamespaceReference().getBaseName());
        symbol.setStatic(definition.isStatic());
        symbol.setFinal(definition.isFinal());
        symbol.setDynamic(definition.isDynamic());
        symbol.setOverride(definition.isOverride());
        symbol.setAbstract(definition.isAbstract());
        symbol.setNative(definition.isNative());
        String source = definition.getContainingSourceFilePath(project);
        if (source == null)
            return;
        File sourceFile = new File(source).getAbsoluteFile();
        if (project instanceof IASProject)
        {
            for (File sourceRoot : ((IASProject)project).getSourcePath())
            {
                String relativeSource = relativize(sourceRoot.getAbsoluteFile(), sourceFile);
                if (relativeSource != null)
                {
                    symbol.setSource(relativeSource);
                    return;
                }
            }
        }
        symbol.setSource(sourceFile.getName());
    }

    private String relativize(File root, File file)
    {
        String rootPath = root.toURI().normalize().getPath();
        String filePath = file.toURI().normalize().getPath();
        if (!filePath.startsWith(rootPath))
            return null;
        return filePath.substring(rootPath.length());
    }

    private CodeGraphReference createFunctionReference(IFunctionDefinition definition)
    {
        ITypeDefinition owner = (ITypeDefinition)definition.getAncestorOfType(ITypeDefinition.class);
        String id = createCallableId(definition, owner);
        boolean external = owner != null && !exportedQualifiedNames.contains(owner.getQualifiedName());
        return new CodeGraphReference(id, definition.getQualifiedName(), external, false);
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
                if ("type".equals(attribute.getKey()) && isTypeBearingMetadata(metaTag.getTagName()))
                {
                    IDefinition typeDefinition = project.resolveQNameToDefinition(attribute.getValue());
                    metadata.addReference(createReference(typeDefinition instanceof ITypeDefinition
                            ? (ITypeDefinition)typeDefinition : null, attribute.getValue()));
                }
            }
            if (metaTag.getTagNode() instanceof IDocumentableDefinitionNode)
                metadata.setASDoc(createASDoc(((IDocumentableDefinitionNode)metaTag.getTagNode()).getASDocComment()));
            symbol.addMetadata(metadata);
        }
    }

    private boolean isTypeBearingMetadata(String name)
    {
        return "Event".equals(name) || "Style".equals(name) || "Effect".equals(name);
    }

    private void addASDoc(CodeGraphSymbol symbol, IDefinition definition)
    {
        if (!(definition instanceof IDocumentableDefinition))
            return;
        IASDocComment comment = ((IDocumentableDefinition)definition).getExplicitSourceComment();
        CodeGraphASDoc asDoc = createASDoc(comment);
        if (asDoc != null)
            symbol.setASDoc(asDoc);
    }

    private CodeGraphASDoc createASDoc(IASDocComment comment)
    {
        if (comment == null)
            return null;
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
        return asDoc;
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

    private CodeGraphReference createReference(ITypeDefinition definition, String displayName)
    {
        if (definition != null)
            return createReference(definition);
        if (displayName == null || displayName.isEmpty())
            return null;
        return new CodeGraphReference(CodeGraphIdFactory.definition(displayName), displayName, true, true);
    }
}