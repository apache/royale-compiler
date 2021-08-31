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

import java.util.Iterator;


import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IGetterDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.ISetterDefinition;
import org.apache.royale.compiler.problems.DuplicateFunctionDefinitionProblem;
import org.apache.royale.compiler.problems.UnresolvedNamespaceProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.internal.as.codegen.BindableHelper;
import org.apache.royale.compiler.internal.scopes.ASScope;

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
        final boolean isBindable = ((NamespaceDefinition)thisNamespaceDef).getAETNamespace().getName().equals(
                                    BindableHelper.bindableNamespaceDefinition.getAETNamespace().getName());

        final IDefinitionSet definitionSet = scope.getLocalDefinitionSetByName(name);

        if (definitionSet == null)
            return null;
        
        final boolean isCustomNamespace = !isBindable && !((NamespaceDefinition) thisNamespaceDef).isLanguageNamespace();

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
                        INamespaceReference testDefRef = definition.getNamespaceReference();
                        INamespaceDefinition testNamespaceDef = testDefRef.resolveNamespaceReference(project);
                        if (testNamespaceDef == null)
                        {
                        	project.getProblems().add(new UnresolvedNamespaceProblem(definition.getNode()));
                        	return null;
                        }
                        final boolean testBindable = ((NamespaceDefinition)testNamespaceDef).getAETNamespace().getName().equals(
                                BindableHelper.bindableNamespaceDefinition.getAETNamespace().getName());
                        /* aharui: namespaces shouldn't have to match.  A subclass may only override
                         * one of the protected methods, and it was legal to have a public getter with
                         * a protected setter and other combinations like that.  Either both
                         * have to be in the bindable namespace, or both are not. */
                        //follow-up: (re mismatched names) The above was true for legacy Flex compiler, but is actually not currently true for ASC 2.0
                        //there are benefits to matching the legacy behavior though.
                        //for custom namespaces however, they must match
                        if (isBindable && testBindable) return (AccessorDefinition)definition;
                        
                        if (!isBindable && !testBindable) {
                            if (isCustomNamespace) {
                                //it does need to match precisely
                                if (thisNamespaceDef.equals(testNamespaceDef))
                                    return (AccessorDefinition)definition;
                            } else {
                                //match loosely based on any language namespace, but check for local conflicts first
                                if (definition.getNamespaceReference().isLanguageNamespace()) {
                                    if (!hasConflictingLanguageNSDefinition(definitionSet, this, project))
                                        return (AccessorDefinition) definition;
                                    else return null;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
    
    public boolean isProblematic() {
        return problematic;
    }
    
    public void setIsProblematic(boolean value) {
        problematic = value;
    }
    
    private boolean problematic;
    private boolean hasConflictingLanguageNSDefinition(IDefinitionSet definitionSet , IAccessorDefinition def, ICompilerProject project) {
        final boolean isGetter = def instanceof IGetterDefinition;
        final int size = definitionSet.getSize();
        int defIndex = -1;
        for (int i=0; i<size; i++) {
            IDefinition localDef = definitionSet.getDefinition(i);
            if (!(localDef instanceof IAccessorDefinition)) continue;
            IAccessorDefinition check = (IAccessorDefinition) localDef;
            if (check == def) {
                defIndex = i;
                break;
            }
        }
        if (defIndex == -1) return false;
        for (int i=0; i<defIndex; i++) {
            IDefinition localDef = definitionSet.getDefinition(i);
            if (!(localDef instanceof IAccessorDefinition)) continue;
            IAccessorDefinition check = (IAccessorDefinition) localDef;
            boolean validCheck = isGetter ? check instanceof IGetterDefinition : check instanceof ISetterDefinition;
            if (!validCheck) continue;
            if (check.getNamespaceReference().isLanguageNamespace()) {
                if (!problematic) {
                    ((AccessorDefinition) check).problematic = true;
                    //checking occurs twice, don't add multiple duplicate problems for the same check
                    problematic = true;
                    project.getProblems().add(new DuplicateFunctionDefinitionProblem(getNode().getNameExpressionNode(), this.getBaseName()));
                }
                return true;
            }
        }
        return false;
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
