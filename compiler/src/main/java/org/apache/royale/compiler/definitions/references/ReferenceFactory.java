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

package org.apache.royale.compiler.definitions.references;

import java.util.Set;

import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.references.BuiltinReference;
import org.apache.royale.compiler.internal.definitions.references.LexicalReference;
import org.apache.royale.compiler.internal.definitions.references.NotATypeReference;
import org.apache.royale.compiler.internal.definitions.references.ParameterizedReference;
import org.apache.royale.compiler.internal.definitions.references.ResolvedQualifiersReference;
import org.apache.royale.compiler.internal.definitions.references.ResolvedReference;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.workspaces.IWorkspace;
import com.google.common.collect.ImmutableSet;

/**
 * A factory class to create instances of type {@link IReference}.
 */
public class ReferenceFactory
{
    // Constant IReferences returned by builtinReference().
    private static final IReference REFERENCE_ANY_TYPE = new BuiltinReference(IASLanguageConstants.BuiltinType.ANY_TYPE);
    private static final IReference REFERENCE_Array = new BuiltinReference(IASLanguageConstants.BuiltinType.ARRAY);
    private static final IReference REFERENCE_Boolean = new BuiltinReference(IASLanguageConstants.BuiltinType.BOOLEAN);
    private static final IReference REFERENCE_Class = new BuiltinReference(IASLanguageConstants.BuiltinType.CLASS);
    private static final IReference REFERENCE_Function = new BuiltinReference(IASLanguageConstants.BuiltinType.FUNCTION);
    private static final IReference REFERENCE_int = new BuiltinReference(IASLanguageConstants.BuiltinType.INT);
    private static final IReference REFERENCE_null = new BuiltinReference(IASLanguageConstants.BuiltinType.NULL);
    private static final IReference REFERENCE_Number = new BuiltinReference(IASLanguageConstants.BuiltinType.NUMBER);
    private static final IReference REFERENCE_Object = new BuiltinReference(IASLanguageConstants.BuiltinType.OBJECT);
    private static final IReference REFERENCE_QName = new BuiltinReference(IASLanguageConstants.BuiltinType.QNAME);
    private static final IReference REFERENCE_RegExp = new BuiltinReference(IASLanguageConstants.BuiltinType.REGEXP);
    private static final IReference REFERENCE_String = new BuiltinReference(IASLanguageConstants.BuiltinType.STRING);
    private static final IReference REFERENCE_undefined = new BuiltinReference(IASLanguageConstants.BuiltinType.Undefined);
    private static final IReference REFERENCE_uint = new BuiltinReference(IASLanguageConstants.BuiltinType.UINT);
    private static final IReference REFERENCE_Vector = new BuiltinReference(IASLanguageConstants.BuiltinType.VECTOR);
    private static final IReference REFERENCE_void = new BuiltinReference(IASLanguageConstants.BuiltinType.VOID);
    private static final IReference REFERENCE_XML = new BuiltinReference(IASLanguageConstants.BuiltinType.XML);
    private static final IReference REFERENCE_XMLList = new BuiltinReference(IASLanguageConstants.BuiltinType.XMLLIST);

    /**
     * Gets an {@link IReference} for one of the builtin types such as
     * <code>Object</code>, <code>String</code>, or <code>Array</code>.
     * 
     * @param type The {@code IASLanguageConstants.BuiltinType} you want the
     * reference for.
     * @return A {@link BuiltinReference}.
     */
    public static IReference builtinReference(IASLanguageConstants.BuiltinType type)
    {
        switch (type)
        {
            case ANY_TYPE:
                return REFERENCE_ANY_TYPE;
            case ARRAY:
                return REFERENCE_Array;
            case BOOLEAN:
                return REFERENCE_Boolean;
            case CLASS:
                return REFERENCE_Class;
            case FUNCTION:
                return REFERENCE_Function;
            case INT:
                return REFERENCE_int;
            case NULL:
                return REFERENCE_null;
            case NUMBER:
                return REFERENCE_Number;
            case OBJECT:
                return REFERENCE_Object;
            case QNAME:
                return REFERENCE_QName;
            case REGEXP:
                return REFERENCE_RegExp;
            case STRING:
                return REFERENCE_String;
            case Undefined:
                return REFERENCE_undefined;
            case UINT:
                return REFERENCE_uint;
            case VECTOR:
                return REFERENCE_Vector;
            case VOID:
                return REFERENCE_void;
            case XML:
                return REFERENCE_XML;
            case XMLLIST:
                return REFERENCE_XMLList;
            default:
                assert false : "Unknown builtin type " + type;
                return new BuiltinReference(type);
        }
    }

    /**
     * Generates an {@link IReference} for an unqualified base name.
     * 
     * @param workspace The workspace.
     * @param baseName The base name you want a reference to.
     * @return A {@link LexicalReference}.
     */
    public static IReference lexicalReference(IWorkspace workspace, String baseName)
    {
        if (workspace instanceof Workspace)
            return ((Workspace)workspace).getReferenceCache().getLexicalReference(baseName);

        return new LexicalReference(baseName);
    }



    /**
     * Generates an {@link IReference} for a base name qualified by an
     * {@link INamespaceDefinition}.
     * 
     * @param workspace The workspace.
     * @param namespace The {@link INamespaceDefinition} to use as the qualifier.
     * @param baseName The base name you want to reference to.
     * @return A {@link ResolvedQualifiersReference}.
     */
    public static IResolvedQualifiersReference resolvedQualifierQualifiedReference(
            IWorkspace workspace, INamespaceDefinition namespace, String baseName)
    {
        ImmutableSet<INamespaceDefinition> qualifiers =
                new ImmutableSet.Builder<INamespaceDefinition>().add(namespace).build();
        return new ResolvedQualifiersReference(qualifiers, baseName);
    }

    /**
     * Generates an {@link IReference} for a base name qualified by a package
     * name.
     * 
     * @param workspace The workspace.
     * @param packageName The package name to use as the qualifier.
     * @param baseName The base name you want a reference to.
     * @param includeInternal Indicates whether or not the reference should
     * resolve to package internal definitions.
     * @return A {@link ResolvedQualifiersReference}.
     */
    public static IResolvedQualifiersReference packageQualifiedReference(
            IWorkspace workspace, String packageName, String baseName, boolean includeInternal)
    {
        INamespaceDefinition packagePublicNS =
                ((Workspace)workspace).getPackageNamespaceDefinitionCache().get(packageName, false);
        ImmutableSet.Builder<INamespaceDefinition> namespaceSetBuilder =
                new ImmutableSet.Builder<INamespaceDefinition>().add(packagePublicNS);
        if (includeInternal)
        {
            INamespaceDefinition packageInternalNS =
                    ((Workspace)workspace).getPackageNamespaceDefinitionCache().get(packageName, true);
            namespaceSetBuilder.add(packageInternalNS);
        }
        ImmutableSet<INamespaceDefinition> namespaceSet = namespaceSetBuilder.build();
        return new ResolvedQualifiersReference(namespaceSet, baseName);
    }

    /**
     * Generates an {@link IReference} for a fully qualified name.
     * <p>
     * It is expected that the string will be of the form
     * <code>my.package.name.Foo</code>. This will be used to generate a
     * reference to <code>Foo</code> in the package <code>my.package.name</code>.
     * 
     * @param workspace The workspace.
     * @param qname A <code>String</code> representing a fully qualified name.
     * @return A {@link ResolvedQualifiersReference}.
     */
    public static IResolvedQualifiersReference packageQualifiedReference(IWorkspace workspace, String qname)
    {
        return packageQualifiedReference(workspace, qname, false);
    }

    /**
     * Generates an {@link IReference} for a fully qualified name.
     * <p>
     * It is expected that the string will be of the form
     * <code>my.package.name.Foo</code>. This will be used to generate a
     * reference to <code>Foo</code> in the package <code>my.package.name</code>.
     * 
     * @param workspace The workspace.
     * @param qname A <code>String</code> representing a fully qualified name.
     * @param includeInternal Indicates whether or not the reference should
     * resolve to package internal definitions.
     * @return A {@link ResolvedQualifiersReference}.
     */
    public static IResolvedQualifiersReference packageQualifiedReference(
            IWorkspace workspace, String qname, boolean includeInternal)
    {
        int lastIndexOfDot = qname.lastIndexOf('.');
        if (lastIndexOfDot != -1)
        {
            String unqualifiedName = qname.substring(lastIndexOfDot + 1);
            String packageName = qname.substring(0, lastIndexOfDot);
            return ReferenceFactory.packageQualifiedReference(workspace, packageName, unqualifiedName, includeInternal);
        }
        else
        {
            return ReferenceFactory.packageQualifiedReference(workspace, "", qname, includeInternal);
        }
    }

    /**
     * Generates an {@link IReference} for a multiname coming from an ABC.
     * 
     * @param workspace The workspace.
     * @param namespaces The set of {@link INamespaceDefinition}s to use as the
     * namespace set.
     * @param baseName The base name you want a reference to.
     * @return A {@link ResolvedQualifiersReference}.n
     */
    public static IResolvedQualifiersReference multinameReference(IWorkspace workspace, Set<INamespaceDefinition> namespaces, String baseName)
    {
        return new ResolvedQualifiersReference(ImmutableSet.copyOf(namespaces), baseName);
    }

    /**
     * Generates an {@link IReference} for a parameterized type, such as
     * {@code Vector.<Foo>}.
     * <p>
     * This currently only supports one type parameter, as Vector is the only
     * parameterized type, and it only takes 1 type parameter.
     * 
     * @param workspace The workspace.
     * @param base The base reference, such as <code>Vector</code> in
     * {@code Vector.<Foo>}.
     * @param param The type parameter reference, such as <code>Foo</code> in
     * {@code Vector.<Foo>}.
     * @return A {@link ParameterizedReference}.
     */
    public static IReference parameterizedReference(IWorkspace workspace, IReference base, IReference param)
    {
        return new ParameterizedReference(base, param);
    }

    /**
     * Generates an {@link IReference} that always resolves to the
     * {@link IDefinition} passed in.
     * <p>
     * This is useful for <code>Vector</code> methods, where the return type is
     * the type parameter for the <code>Vector</code> (i.e., <code>T</code> in
     * {@code Vector.<T>}).
     * 
     * @param definition The {@link IDefinition} to generate a reference to.
     * @return A {@link ResolvedReference} that resolves to <code>d</code>.
     */
    public static IReference resolvedReference(IDefinition definition)
    {
        return new ResolvedReference(definition);
    }

    /**
     * Generates an {@link IReference} for a type reference that will always be
     * an error. An example would be: class C extends a.b.c.d.Foo{} If a.b.c.d
     * was not a package name then this class would be used to represent the
     * reference. This is because a property access will be an error, but we
     * have to remember that something was specified for the base class so we
     * can report the correct error when we try and resolve it.
     * 
     * @param workspace The workspace.
     * @param baseName The base name you want a reference to.
     * @return A {@link NotATypeReference}.
     */
    public static IReference notATypeReference(IWorkspace workspace, String baseName)
    {
        return new NotATypeReference(baseName);
    }

    // This is an all-static class and no instances of it can be created.
    private ReferenceFactory()
    {
    }
}
