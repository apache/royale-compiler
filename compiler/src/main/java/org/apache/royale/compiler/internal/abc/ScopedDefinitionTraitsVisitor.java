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

import java.util.Vector;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.ClassInfo;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.abc.semantics.Traits;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IParameterDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.definitions.references.ReferenceFactory;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.ConstantDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.definitions.FunctionDefinition;
import org.apache.royale.compiler.internal.definitions.GetterDefinition;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.definitions.ParameterDefinition;
import org.apache.royale.compiler.internal.definitions.SetterDefinition;
import org.apache.royale.compiler.internal.definitions.TypeDefinitionBase;
import org.apache.royale.compiler.internal.definitions.VariableDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.FunctionScope;
import org.apache.royale.compiler.scopes.IASScope;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * This {@link ITraitsVisitor} creates definition for each trait, and add the
 * definitions to a scope object.
 */
public class ScopedDefinitionTraitsVisitor implements ITraitsVisitor
{
    private static final IReference TYPE_ANY = ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.ANY_TYPE); 
    private static final IReference TYPE_FUNCTION = ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.FUNCTION);

    private final ASScope scope;
    private final boolean isStatic;
    private final ABCScopeBuilder scopeBuilder;
    private final INamespaceReference interfNamespace;

    public ScopedDefinitionTraitsVisitor(final ABCScopeBuilder owner, final IASScope scope,
            boolean isStatic)
    {
        this(owner, scope, isStatic, null);
    }
    
    public ScopedDefinitionTraitsVisitor(final ABCScopeBuilder owner, final IASScope scope,
                                         boolean isStatic, INamespaceReference interfNamespace)
    {
        assert scope instanceof ASScope;

        this.scopeBuilder = owner;
        this.scope = (ASScope)scope;
        this.isStatic = isStatic;
        this.interfNamespace = interfNamespace;
    }

    @Override
    public ITraitVisitor visitSlotTrait(int kind, Name name, int slot_id, Name slot_type, Object slot_value)
    {
        final String definitionName = getDefinitionName(name);

        final DefinitionBase def;        
        kind &= ABCConstants.TRAIT_KIND_MASK;
        switch (kind)
        {
            case ABCConstants.KIND_SLOT:
                def = new VariableDefinition(definitionName, slot_value);
                break;
            case ABCConstants.KIND_CONST:
                if (slot_value instanceof Namespace)
                    def = NamespaceDefinition.createNamespaceDefinition(definitionName, (Namespace)slot_value);
                else
                    def = new ConstantDefinition(definitionName, slot_value);
                break;
                
            default:
                throw new IllegalStateException("Invalid slot kind: " + kind);
        }
        
        final INamespaceReference namespaceReference = getNamespaceReference(name);
        def.setNamespaceReference(namespaceReference);

        if (isStatic)
            def.setStatic();

        def.setTypeReference(slot_type == null ? TYPE_ANY : scopeBuilder.getReference(slot_type));
        
        scope.addDefinition(def);

        return new CollectMetadataTraitVisitor(def);
    }

    @Override
    public ITraitVisitor visitMethodTrait(int kind, Name name, int disp_id, MethodInfo method)
    {
        final String definitionName = getDefinitionName(name);

        FunctionDefinition methodDef;
        
        kind &= ABCConstants.TRAIT_KIND_MASK;
        switch (kind)
        {
            case ABCConstants.KIND_METHOD:
                methodDef = new FunctionDefinition(definitionName);
                break;
            case ABCConstants.KIND_GETTER:
                methodDef = new GetterDefinition(definitionName);
                break;
            case ABCConstants.KIND_SETTER:
                methodDef = new SetterDefinition(definitionName);
                break;
            case ABCConstants.KIND_FUNCTION:
                methodDef = new FunctionDefinition(definitionName);
                break;
            default:
                throw new IllegalStateException("Invalid method kind:" + kind);
        }
        
        final INamespaceReference namespaceReference = getNamespaceReference(name);
        methodDef.setNamespaceReference(namespaceReference);

        int paramTypesSize = method.getParamTypes().size();
        final ParameterDefinition params[] = new ParameterDefinition[paramTypesSize + (method.needsRest() ? 1 : 0)];
        if (params.length > 0)
        {
            ASScope methodScope = new FunctionScope(scope);
            methodScope.setContainingDefinition(methodDef);
            methodDef.setContainedScope(methodScope);
            Vector<PooledValue> defaultValues = method.getDefaultValues();
            int firstOptionalParam = paramTypesSize - defaultValues.size();
            for (int i = 0; i < paramTypesSize; i++)
            {
                final Name paramType = method.getParamTypes().get(i);
                final String paramName = i < method.getParamNames().size() ? method.getParamNames().get(i) : MethodInfo.UNKNOWN_PARAM_NAME;
                params[i] = new ParameterDefinition(paramName);
                params[i].setTypeReference(paramType == null ? TYPE_ANY : scopeBuilder.getReference(paramType));
                if (i >= firstOptionalParam)
                {
                    Object defaultValue = defaultValues.get(i - firstOptionalParam).getValue();
                    params[i].setDefaultValue(defaultValue);
                }
                methodScope.addDefinition(params[i]);
            }
            if( method.needsRest() )
            {
                ParameterDefinition rest = new ParameterDefinition(MethodInfo.UNKNOWN_PARAM_NAME);
                rest.setRest();
                rest.setTypeReference(ReferenceFactory.builtinReference(IASLanguageConstants.BuiltinType.ARRAY));
                params[paramTypesSize] = rest;
            }
        }
        methodDef.setParameters(params);

        Name returnType = method.getReturnType();
        methodDef.setReturnTypeReference(returnType == null ? TYPE_ANY : scopeBuilder.getReference(returnType));
        
        // The type of a getter or setter is its property type
        // (i.e., the getter's return type or the setter's parameter type).
        // The type of a method or function is "Function".
        switch (kind)
        {
            case ABCConstants.KIND_GETTER:
                methodDef.setTypeReference(methodDef.getReturnTypeReference());
                break;
            case ABCConstants.KIND_SETTER:
                methodDef.setTypeReference(methodDef.getParameters()[0].getTypeReference());
                break;
            case ABCConstants.KIND_METHOD:
            case ABCConstants.KIND_FUNCTION:
                methodDef.setTypeReference(TYPE_FUNCTION);
                break;
            default:
                throw new IllegalStateException("Invalid method kind:" + kind);
        }

        if (isStatic)
            methodDef.setStatic();

        scope.addDefinition(methodDef);

        return new CollectMetadataTraitVisitor(methodDef);
    }
    

    @Override
    public ITraitVisitor visitClassTrait(int kind, Name name, int slot_id, ClassInfo clazz)
    {
        final TypeDefinitionBase classDef = scopeBuilder.classDefinitions.get(clazz);
        assert classDef != null : "Null class def at #" + slot_id;
        scope.addDefinition(classDef);
        classDef.getContainedScope().setContainingScope(scope);

        // Need to setup the scopes for the constructor and any params
        // here instead of ABCScopeBuilder, as we need to have a handle to the
        // class scope which isn't set until here.
        if (classDef instanceof ClassDefinition)
        {
            FunctionDefinition ctor = (FunctionDefinition)((ClassDefinition)classDef).getConstructor();
            classDef.getContainedScope().addDefinition(ctor);

            IParameterDefinition[] params = ctor.getParameters();
            if (params.length > 0)
            {
                ASScope ctorScope = new FunctionScope(scope);
                ctorScope.setContainingDefinition(ctor);
                ctor.setContainedScope(ctorScope);

                for (IParameterDefinition param : params)
                {
                    ctorScope.addDefinition(param);
                }
            }
        }

        return new CollectMetadataTraitVisitor(classDef);
    }
    
    private static boolean legalDefinitionNsset(Nsset nsSet)
    {
        if (nsSet == null)
            return false;
        if (nsSet.length() == 1)
            return true;
        return Iterables.all(nsSet, new Predicate<Namespace>() {

            @Override
            public boolean apply(Namespace ns)
            {
                return ns.getApiVersion() != ABCConstants.NO_API_VERSION;
            }
            @Override
            public boolean test(Namespace input)
            {
                return apply(input);
            }
            }); 
    }
    
    public static String getDefinitionName(Name name)
    {
        final String baseName = name.getBaseName();

        // A definition can only ever have one namespace entry
        // otherwise it is an invalid SWC.
        Nsset nsSet = name.getQualifiers();
        if (!legalDefinitionNsset(nsSet))
        {
            throw new IllegalStateException("Definition " + baseName + " can have only one qualifier or all qualifiers should be versioned namespaces");
        }

        return baseName;
    }
    
    private INamespaceReference getNamespaceReference(Name name)
    {
        final Namespace namespace = Iterables.getFirst(name.getQualifiers(), null);
        assert namespace != null;
        
        INamespaceReference namespaceReference =
            (INamespaceReference)scopeBuilder.getNamespaceReferenceForNamespace(namespace);
        
        // Interface Namespaces are encoded as regular user defined namespaces in the ABC, but internally
        // we want them to be InterfaceNamespaceDefinitions.  If we come across a user defined namespace while
        // building the traits for an interface, and it matches the interface namespace, then use the interface
        // namespace instead so that other processing that relies on InterfaceNamespaceDefinitions works right.
        if( interfNamespace != null && interfNamespace.equals(namespaceReference) )
            namespaceReference = interfNamespace;
        
        return namespaceReference;
    }

    @Override
    public void visit()
    {
    }

    @Override
    public void visitEnd()
    {
    }

    @Override
    public Traits getTraits()
    {
        return null;
    }

}
