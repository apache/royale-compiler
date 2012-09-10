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

package org.apache.flex.compiler.internal.definitions;

import java.util.Iterator;


import org.apache.flex.compiler.definitions.IAccessorDefinition;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IGetterDefinition;
import org.apache.flex.compiler.definitions.IInterfaceDefinition;
import org.apache.flex.compiler.definitions.INamespaceDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ISetterDefinition;
import org.apache.flex.compiler.projects.ICompilerProject;
import org.apache.flex.compiler.scopes.IDefinitionSet;
import org.apache.flex.compiler.tree.as.IVariableNode;
import org.apache.flex.compiler.definitions.IScopedDefinition;
import org.apache.flex.compiler.definitions.references.INamespaceReference;
import org.apache.flex.compiler.internal.scopes.ASScope;

/**
 * {@code AccessorDefinition} is the abstract base class for definitions that
 * represent getters and setters.
 */
public abstract class AccessorDefinition extends FunctionDefinition implements IAccessorDefinition
{
    public AccessorDefinition(String name)
    {
        super(name);
    }

    @Override
    public AccessorDefinition resolveCorrespondingAccessor(ICompilerProject project)
    {
        IDefinition parent = getParent();

        if (parent instanceof IClassDefinition)
        {
            // This accessor is in a class, so look for a corresponding one
            // in this class and then in all superclasses.
            Iterator<IClassDefinition> iter = ((IClassDefinition)parent).classIterator(project, true);
            while (iter.hasNext())
            {
                IClassDefinition cls = iter.next();

                AccessorDefinition correspondingAccessor =
                        findCorrespondingAccessor(cls, project);

                if (correspondingAccessor != null)
                    return correspondingAccessor;
            }
        }
        else if (parent instanceof IInterfaceDefinition)
        {
            // This accessor is in an interface, so look for a corresponding one
            // in this interface and then in all superinterfaces.
            Iterator<IInterfaceDefinition> iter = ((IInterfaceDefinition)parent).interfaceIterator(project, true);
            while (iter.hasNext())
            {
                IInterfaceDefinition intf = iter.next();

                AccessorDefinition correspondingAccessor =
                        findCorrespondingAccessor(intf, project);

                if (correspondingAccessor != null)
                    return correspondingAccessor;
            }
        }
        else if (parent instanceof IPackageDefinition)
        {
            IPackageDefinition pd = (IPackageDefinition)parent;
            return findCorrespondingAccessor(pd, project);
        }
        else if (parent == null)
        {
            // if the parent definition is null, we must be at file scope, so must search the scope 
            // directly
            ASScope scope = this.getContainingASScope();
            return findCorrespondingAccessor(scope, project);
        }
        else
            assert false; // we should have code for all cases...

        return null;
    }

    /**
     * Looks in a specified class or interface for an accessor that corresponds
     * to "this". i.e. if "this" is a getter, find the matching setter.
     * 
     * @param type is the definition to search for corresponding def
     * @return an accessor definition that matches, or null if none found
     */

    private AccessorDefinition findCorrespondingAccessor(IScopedDefinition type, ICompilerProject project)
    {
        final ASScope scope = (ASScope)type.getContainedScope();
        return findCorrespondingAccessor(scope, project);
    }

    /**
     * Looks in a specified scope for an accessor that corresponds to "this".
     * i.e. if "this" is a getter, find the matching setter.
     * 
     * @param scope is the scope to search for corresponding def
     * @return an accessor definition that matches, or null if none found
     */

    private AccessorDefinition findCorrespondingAccessor(ASScope scope, ICompilerProject project)
    {
        final String name = getBaseName();
        final INamespaceReference namespaceReference = getNamespaceReference();
        final boolean isStatic = isStatic();
        
        // If the namespace is bad and dosn't resolve, then we can't find corresponding accessor.
        final INamespaceDefinition thisNamespaceDef = namespaceReference.resolveNamespaceReference(project);
        if (thisNamespaceDef == null)
            return null; 

        final IDefinitionSet definitionSet = scope.getLocalDefinitionSetByName(name);

        if (definitionSet == null)
            return null;

        final int n = definitionSet.getSize();
        for (int i = 0; i < n; i++)
        {
            IDefinition d = definitionSet.getDefinition(i);
            if (d instanceof IAccessorDefinition)
            {
                final IAccessorDefinition definition = (IAccessorDefinition)d;
                
                // If this is a static accessor, we want another static accessor.
                // If this is an instance accessor, we want another instance accessor. 
                if (definition.isStatic() == isStatic)
                {
                    // If this is a getter, we want a setter, and vice versa.
                    if (this instanceof IGetterDefinition && definition instanceof ISetterDefinition ||
                        this instanceof ISetterDefinition && definition instanceof IGetterDefinition)
                    {
                        // The namespace must match or it isn't considering to correspond.
                        INamespaceReference testDefRef = definition.getNamespaceReference();
                        INamespaceDefinition testNamespaceDef = testDefRef.resolveNamespaceReference(project);
                        if (thisNamespaceDef.equals(testNamespaceDef))
                            return (AccessorDefinition)definition;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get the classification for this variable (local, argument, class member,
     * etc)
     * 
     * @return variable classification
     */
    @Override
    public VariableClassification getVariableClassification()
    {
        IDefinition parent = getParent();

        if (parent instanceof IFunctionDefinition)
            return VariableClassification.LOCAL;
        if (parent instanceof IClassDefinition)
            return VariableClassification.CLASS_MEMBER;
        if (parent instanceof IInterfaceDefinition)
            return VariableClassification.INTERFACE_MEMBER;
        if (parent instanceof IPackageDefinition)
            return VariableClassification.PACKAGE_MEMBER;
        if (parent == null)
        {
            if (inPackageNamespace())
                return VariableClassification.PACKAGE_MEMBER;

            return VariableClassification.FILE_MEMBER;
        }

        assert false;
        return null;
    }

    @Override
    public IVariableNode getVariableNode()
    {
        return (IVariableNode)super.getNode();
    }

    @Override
    public Object resolveInitialValue(ICompilerProject project)
    {
        return null;
    }

    @Override
    public boolean inlineFunction()
    {
        // if inlining has been enabled, don't need to check
        // for inline keyword, as inline all getters/setters
        // as long as they meet the correct criteria.
        if (canFunctionBeInlined())
            return true;

        return false;
    }
}
