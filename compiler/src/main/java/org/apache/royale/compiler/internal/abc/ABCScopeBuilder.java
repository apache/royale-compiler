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

package org.apache.royale.compiler.internal.abc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.ABCParser;
import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.abc.visitors.IClassVisitor;
import org.apache.royale.abc.visitors.IMethodVisitor;
import org.apache.royale.abc.visitors.IScriptVisitor;
import org.apache.royale.abc.visitors.NilABCVisitor;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.InterfaceDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.scopes.ASFileScope;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IFileScopeProvider;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Populates symbol table from an ABC file.
 */
public class ABCScopeBuilder extends NilABCVisitor
{
    private static final IReference TYPE_ANY = ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.ANY_TYPE); 
    private static final IReference TYPE_FUNCTION = ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.FUNCTION);

    IReference getReference(Name name)
    {
        if( name == null )
            return null;

        IReference ref = nameMap.get(name);
        if( ref != null )
            return ref;

        switch( name.getKind() )
        {
            case ABCConstants.CONSTANT_Qname:
                INamespaceDefinition ns = getNamespaceReferenceForNamespace(name.getSingleQualifier());
                ref = ReferenceFactory.resolvedQualifierQualifiedReference(workspace, ns, name.getBaseName());
                break;
            case ABCConstants.CONSTANT_Multiname:
                Nsset set = name.getQualifiers();
                if (set.length() != 1)
                {
                    Set<INamespaceDefinition> ns_set = new HashSet<INamespaceDefinition>(set.length());
                    for( Namespace n : set )
                        ns_set.add(getNamespaceReferenceForNamespace(n));
                    ref = ReferenceFactory.multinameReference(workspace, ns_set, name.getBaseName());
                }
                else
                {
                    INamespaceDefinition singleNS = getNamespaceReferenceForNamespace(name.getSingleQualifier());
                    ref = ReferenceFactory.resolvedQualifierQualifiedReference(workspace, singleNS, name.getBaseName());
                }
                break;
            case ABCConstants.CONSTANT_TypeName:
                // If we ever support more than Vector, we'll need to harden this code against loops
                // in the type name's.
                assert name.getTypeNameBase().getBaseName().equals("Vector") : "Vector is currently the only supported parameterized type!";
                IReference parameterizedTypeReference = getReference(name.getTypeNameBase());
                IReference parameterTypeReference = getReference(name.getTypeNameParameter());
                ref = ReferenceFactory.parameterizedReference(workspace, parameterizedTypeReference, parameterTypeReference);
                break;
            default:
                assert false : "Unsupported multiname type: " + name.getKind();
        }
        nameMap.put(name, ref);
        return ref;
    }
    /**
     * Encode a {@link Name} that refers to a global AS3 definition as a string.
     * If the number of qualifiers in the {@link Name} is greater than one this
     * method will use the first qualifier that is either of type
     * {@link ABCConstants#CONSTANT_PackageNs} or
     * {@link ABCConstants#CONSTANT_PackageInternalNs}.
     * 
     * @param name {@link Name} to encode in a String.
     * @return A String that attempts to encode the specified {@link Name}.
     */
    static String getQName(Name name)
    {
        if (name == null)
            return null;

        String baseName = name.getBaseName();

        // Look through the multiname set for a package name.
        // TODO Although most names in SWCs seem to have only
        // one namespace in their namespace set, interfaces seem
        // to have true multinames with multiple namespaces.
        // For now, just look for the package namespace.
        // Eventually we have to deal with a real multiname.
        String packageName = null;
        Nsset qualifiers = name.getQualifiers();
        if (qualifiers != null)
        {
            for (Namespace namespace : qualifiers)
            {
                if ((namespace.getKind() == ABCConstants.CONSTANT_PackageNs) || (namespace.getKind() == ABCConstants.CONSTANT_PackageInternalNs))
                {
                    packageName = namespace.getName();
                    if (packageName.length() > 0)
                        break;
                }
            }
        }

        return packageName != null && packageName.length() > 0 ?
                packageName + '.' + baseName :
                baseName;
    }

    private IReference[] getReferences(Name[] names)
    {
        IReference[] refs = null;
        int n = names.length;
        if (n != 0)
        {
            refs = new IReference[n];
            for (int i = 0; i < n; i++)
            {
                refs[i] = getReference(names[i]);
            }
        }
        return refs;
    }

    /**
     * Create an ABCScopeBuilder from ABC byte code data.
     * 
     * @param workspace workspace
     * @param abcData ABC byte code data.
     * @param path path of the file that contains the abc data.
     * @param fileScopeProvider callback that creates {@code ASFileScope}
     * objects.
     */
    public ABCScopeBuilder(final IWorkspace workspace,
                           final byte[] abcData,
                           final String path,
                           final IFileScopeProvider fileScopeProvider)
    {
        checkNotNull(workspace, "Workspace can't be null.");
        checkNotNull(abcData, "ABC data can't be null.");
        checkNotNull(path, "File path can't be null.");
        checkNotNull(fileScopeProvider, "File scope provider can't be null.");

        scopes = new ArrayList<IASScope>();
        classDefinitions = new HashMap<ClassInfo, TypeDefinitionBase>();
        abcParser = new ABCParser(abcData);
        namespacesMap = new HashMap<Namespace, INamespaceDefinition>();
        nameMap = new HashMap<Name, IReference>();
        this.workspace = workspace;
        this.path = path;
        this.fileScopeProvider = fileScopeProvider;
    }

    private final IFileScopeProvider fileScopeProvider;
    private final ABCParser abcParser;
    private final List<IASScope> scopes;

    // This is the class definition pool.
    protected final Map<ClassInfo, TypeDefinitionBase> classDefinitions;

    private final Map<Namespace, INamespaceDefinition> namespacesMap;

    private final Map<Name, IReference> nameMap;

    private final IWorkspace workspace;

    /**
     * Path of the file that contains the abc data. This field is used to set
     * the containing file path of the definitions built from ABC.
     */
    protected final String path;

    /**
     * Constructs or otherwise obtains an {@link INamespaceReference} for an
     * {@link Namespace}.
     * 
     * @param ns {@link Namespace} for which an {@link INamespaceReference}
     * should be obtained.
     * @return A {@link INamespaceReference} that wraps the specified
     * {@link Namespace}.
     */
    public INamespaceDefinition getNamespaceReferenceForNamespace(Namespace ns)
    {
        INamespaceDefinition result = namespacesMap.get(ns);
        if (result != null)
            return result;

        // Strip off versioning information.
        Namespace nonVersionedNS = ns.getApiVersion() == ABCConstants.NO_API_VERSION?
            ns :
            new Namespace(ns.getKind(), ns.getName());
        ;

        result = NamespaceDefinition.createNamespaceDefinition(nonVersionedNS);

        assert result != null;
        namespacesMap.put(ns, result);
        return result;
    }

    /**
     * Build scopes and symbol tables from ABC.
     * 
     * @return the script definition object
     * @throws IOException error
     */
    public List<IASScope> build() throws IOException
    {
        abcParser.parseABC(this);
        return this.scopes;
    }

    @Override
    public IScriptVisitor visitScript()
    {
        final ASFileScope fileScope = this.fileScopeProvider.createFileScope(workspace, path);
        assert fileScope != null : "IFileScopeProvider shouldn't create null objects.";
        scopes.add(fileScope);
        return new ScriptDefinitionBuilder(this, fileScope);
    }

    /**
     * Visit class definition pool. Build a local map from classInfo to
     * ClassDefinition. The pool is queried by children visitors.
     * <p>
     * <b>InstanceInfo.Flags</b>
     * <ul>
     * <li>ClassSealed=0x01</li>
     * <li>ClassFinal=0x02</li>
     * <li>ClassInterface=0x04</li>
     * <li>ClassProtectedNs=0x08</li>
     * </ul>
     */
    @Override
    public IClassVisitor visitClass(InstanceInfo iinfo, ClassInfo cinfo)
    {
        // Instance flags
        final boolean isSealed = (iinfo.flags & ABCConstants.CONSTANT_ClassSealed) != 0;
        final boolean isFinal = (iinfo.flags & ABCConstants.CONSTANT_ClassFinal) != 0;
        final boolean isInterface = (iinfo.flags & ABCConstants.CONSTANT_ClassInterface) != 0;

        assert iinfo.name.getKind() == ABCConstants.CONSTANT_Qname;
        String typeName = iinfo.name.getBaseName();

        final Namespace namespace = iinfo.name.getSingleQualifier();
        final String namespaceName = namespace.getName();
        final int namespaceKind = namespace.getKind();
        INamespaceReference namespaceRef = null;
        if (namespaceName.length() != 0 &&
            ((namespaceKind == ABCConstants.CONSTANT_PackageNs) || (namespaceKind == ABCConstants.CONSTANT_PackageInternalNs)))
        {
            namespaceRef = 
                ((Workspace)workspace).getPackageNamespaceDefinitionCache().get(namespaceName, namespaceKind == ABCConstants.CONSTANT_PackageInternalNs);
        }
        else
        {
            namespaceRef = NamespaceDefinition.createNamespaceDefinition(namespace);
        }

        final TypeDefinitionBase typeDefinition;
        if (isInterface)
        {
            final InterfaceDefinition interfaceDefinition = new InterfaceDefinition(typeName);

            final IReference[] extendedInterfaces = getReferences(iinfo.interfaceNames);
            interfaceDefinition.setExtendedInterfaceReferences(extendedInterfaces);

            setupCastFunction(iinfo, interfaceDefinition);

            typeDefinition = interfaceDefinition;
        }
        else
        {
            String protectedNSURI;
            if (iinfo.hasProtectedNs())
                protectedNSURI = iinfo.protectedNs.getName();
            else
            {
                String classNSURI = namespace.getName();
                protectedNSURI = (classNSURI.isEmpty() ? "" : classNSURI + ":") + typeName;
            }
            NamespaceDefinition.IProtectedNamespaceDefinition protectedNSDefinition = NamespaceDefinition.createProtectedNamespaceDefinition(protectedNSURI);
            
            final ClassDefinition classDefinition = new ClassDefinition(typeName, namespaceRef, protectedNSDefinition);
            final IReference baseClass = getReference(iinfo.superName);
            classDefinition.setBaseClassReference(baseClass);

            final IReference[] implementedInterfaces = getReferences(iinfo.interfaceNames);
            classDefinition.setImplementedInterfaceReferences(implementedInterfaces);

            setupConstructor(iinfo, classDefinition);

            typeDefinition = classDefinition;
        }

        
        final INamespaceDefinition namespaceReference = getNamespaceReferenceForNamespace(namespace);
        
        typeDefinition.setNamespaceReference((INamespaceReference)namespaceReference);

        if (!isSealed)
            typeDefinition.setDynamic();
        if (isFinal)
            typeDefinition.setFinal();

        final TypeDefinitionBuilder visitor = new TypeDefinitionBuilder(this, typeDefinition);

        classDefinitions.put(cinfo, typeDefinition);

        return visitor;
    }

    @Override
    public IMethodVisitor visitMethod(MethodInfo minfo)
    {
        return null;
    }

    private void setupConstructor(InstanceInfo iinfo, ClassDefinition classDefinition)
    {
        String ctorName = ScopedDefinitionTraitsVisitor.getDefinitionName(iinfo.name);

        FunctionDefinition ctor = new FunctionDefinition(ctorName);
        ctor.setNamespaceReference(NamespaceDefinition.getCodeModelImplicitDefinitionNamespace());
        ctor.setTypeReference(TYPE_FUNCTION);
        // NOTE: don't set a return type for constructors
        ctor.setReturnTypeReference(null);

        MethodInfo mInfo = iinfo.iInit;
        int paramTypesSize = mInfo.getParamTypes().size();
        final ParameterDefinition params[] = new ParameterDefinition[paramTypesSize + (mInfo.needsRest() ? 1 : 0)];
        if (params.length > 0)
        {
            Vector<PooledValue> defaultValues = mInfo.getDefaultValues();
            int firstOptionalParam = paramTypesSize - defaultValues.size();
            for (int i = 0; i < paramTypesSize; i++)
            {
                final Name paramType = mInfo.getParamTypes().get(i);
                final String paramName = i < mInfo.getParamNames().size() ? mInfo.getParamNames().get(i) : MethodInfo.UNKNOWN_PARAM_NAME;
                params[i] = new ParameterDefinition(paramName);
                params[i].setTypeReference(paramType == null ? TYPE_ANY : getReference(paramType));
                if (i >= firstOptionalParam)
                {
                    Object defaultValue = defaultValues.get(i - firstOptionalParam).getValue();
                    params[i].setDefaultValue(defaultValue);
                }
            }

            if (mInfo.needsRest())
            {
                ParameterDefinition rest = new ParameterDefinition(MethodInfo.UNKNOWN_PARAM_NAME);
                rest.setRest();
                rest.setTypeReference(ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.ARRAY));
                params[paramTypesSize] = rest;
            }
        }

        ctor.setParameters(params);
        ctor.setAsConstructor(classDefinition);
        ctor.setImplicit();
    }

    private void setupCastFunction(InstanceInfo iinfo, InterfaceDefinition interfaceDefinition)
    {
        String castName = ScopedDefinitionTraitsVisitor.getDefinitionName(iinfo.name);

        FunctionDefinition castFunc = new FunctionDefinition(castName);
        castFunc.setNamespaceReference(NamespaceDefinition.getCodeModelImplicitDefinitionNamespace());
        castFunc.setReturnTypeReference(ReferenceFactory.resolvedReference(interfaceDefinition));
        castFunc.setCastFunction();
        castFunc.setImplicit();
    }
}
