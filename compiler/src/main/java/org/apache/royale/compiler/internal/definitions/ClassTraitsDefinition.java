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

package org.apache.royale.compiler.internal.definitions;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.common.ASModifier;
import org.apache.royale.compiler.common.ModifiersSet;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.filespecs.IFileSpecification;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.ITypeNode;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * Definition to represent the class traits, which holds all the static members,
 * and whose baseclass is "Class". This is implemented as a wrapper around the
 * existing TypeDefinition - most methods just proxy to the wrapped instance.
 */
public final class ClassTraitsDefinition extends TypeDefinitionBase
{
    /**
     * The class, or interface this class trait is for
     */
    private TypeDefinitionBase clazz;

    ClassTraitsDefinition(TypeDefinitionBase clazz)
    {
        super(clazz.getStorageName(), null);
        this.clazz = clazz;
    }

    /**
     * Get the scope for this traits - will return the static scope so lookups
     * in the returned scope will only find statics.
     */
    @Override
    public ASScope getContainedScope()
    {
        return ((TypeScope)clazz.getContainedScope()).getStaticScope();
    }

    /**
     * Class traits are never dynamic
     */
    @Override
    public boolean isDynamic()
    {
        return false;
    }

    /**
     * Codegen should never ask for the AET Name of a class traits
     */
    @Override
    public Name getMName(ICompilerProject project)
    {
        assert false : "CodeGen should never ask for the name of a Class Traits";
        return null;
    }

    /**
     * isInstanceOf for the class traits - basically will return true if type is
     * Class or Object
     */
    @Override
    public boolean isInstanceOf(ITypeDefinition type, ICompilerProject project)
    {
        // A class is considered an instance of itself.
        if (type == this)
            return true;

        if (type instanceof IClassDefinition)
        {
            // We're trying to determine whether this class
            // is derived from a specified class ('type').
            // Iterate the superclass chain looking for 'type'.
            // superclass chain for these kinds of definitions always starts with "Class"

            IDefinition classType = project.getBuiltinType(IASLanguageConstants.BuiltinType.CLASS);
            Iterator<IClassDefinition> iter = ((ClassDefinitionBase)classType).classIterator(project, true);
            while (iter.hasNext())
            {
                IClassDefinition cls = iter.next();
                if (cls == type)
                    return true;
            }
            return false;
        }
        // Must be an interface or something else, either way
        // we are not an instanceof it.
        return false;
    }

    @Override
    public boolean matches(DefinitionBase node)
    {
        // There will only ever be one of these per ITypeDefinition
        return node == this;
    }

    /*
     * Everything down from here is overrides to just proxy to the underlying
     * class
     */
    @Override
    public ITypeNode getNode()
    {
        return clazz.getNode();
    }

    @Override
    public Set<IInterfaceDefinition> resolveAllInterfaces(ICompilerProject project)
    {
        return Collections.emptySet();
    }

    @Override
    public int getNameStart()
    {
        return clazz.getNameStart();
    }

    @Override
    public String getPackageName()
    {
        return clazz.getPackageName();
    }

    @Override
    public IInterfaceDefinition[] resolveInterfacesImpl (ICompilerProject project)
    {
        return new IInterfaceDefinition[0];
    }

    @Override
    public String getQualifiedName()
    {
        return clazz.getQualifiedName();
    }

    @Override
    public IFileSpecification getFileSpecification()
    {
        return clazz.getFileSpecification();
    }

    @Override
    public String getSourcePath()
    {
        return clazz.getSourcePath();
    }

    @Override
    public int getStart()
    {
        return clazz.getStart();
    }

    @Override
    public int getEnd()
    {
        return clazz.getEnd();
    }

    @Override
    public int getLine()
    {
        return clazz.getLine();
    }

    @Override
    public int getColumn()
    {
        return clazz.getColumn();
    }

    @Override
    public int getAbsoluteStart()
    {
        return clazz.getAbsoluteStart();
    }

    @Override
    public int getAbsoluteEnd()
    {
        return clazz.getAbsoluteEnd();
    }

    @Override
    public int getNameEnd()
    {
        return clazz.getNameEnd();
    }

    @Override
    public int getNameLine()
    {
        return clazz.getNameLine();
    }

    @Override
    public int getNameColumn()
    {
        return clazz.getNameColumn();
    }

    @Override
    public String getContainingFilePath()
    {
        return clazz.getContainingFilePath();
    }

    @Override
    public String getContainingSourceFilePath(ICompilerProject project)
    {
        return clazz.getContainingSourceFilePath(project);
    }

    @Override
    public boolean isTopLevelDefinition()
    {
        return clazz.isTopLevelDefinition();
    }

    @Override
    public boolean isFinal()
    {
        return clazz.isFinal();
    }

    @Override
    public boolean isNative()
    {
        return clazz.isNative();
    }

    @Override
    public boolean isOverride()
    {
        return clazz.isOverride();
    }

    @Override
    public boolean isStatic()
    {
        return clazz.isStatic();
    }

    @Override
    public boolean hasModifier(ASModifier modifier)
    {
        return clazz.hasModifier(modifier);
    }

    @Override
    public boolean hasNamespace(INamespaceReference namespace, ICompilerProject project)
    {
        return clazz.hasNamespace(namespace, project);
    }

    @Override
    public INamespaceReference getNamespaceReference()
    {
        return clazz.getNamespaceReference();
    }

    @Override
    public IMetaTag[] getAllMetaTags()
    {
        return clazz.getAllMetaTags();
    }

    @Override
    public IMetaTag[] getMetaTagsByName(String name)
    {
        return clazz.getMetaTagsByName(name);
    }

    @Override
    public boolean hasMetaTagByName(String name)
    {
        return clazz.hasMetaTagByName(name);
    }

    @Override
    public IMetaTag getMetaTagByName(String name)
    {
        return clazz.getMetaTagByName(name);
    }

    @Override
    public boolean hasExplicitComment()
    {
        return clazz.hasExplicitComment();
    }

    @Override
    public ASFileScope getFileScope()
    {
        return clazz.getFileScope();
    }

    @Override
    public ModifiersSet getModifiers()
    {
        return clazz.getModifiers();
    }

    @Override
    public String getBaseName()
    {
        return clazz.getBaseName();
    }

    @Override
    public IASScope getContainingScope()
    {
        return clazz.getContainingScope();
    }

    @Override
    public INamespaceDefinition getProtectedNamespaceReference()
    {
        return clazz.getProtectedNamespaceReference();
    }

    @Override
    public INamespaceDefinition getStaticProtectedNamespaceReference()
    {
        return clazz.getStaticProtectedNamespaceReference();
    }

    @Override
    protected String getLocationString()
    {
        return clazz.getLocationString();
    }

    @Override
    protected String getNamespaceReferenceAsString()
    {
        return clazz.getNamespaceReferenceAsString();
    }

    @Override
    public boolean isInProject(ICompilerProject project)
    {
        return clazz.isInProject(project);
    }
}
