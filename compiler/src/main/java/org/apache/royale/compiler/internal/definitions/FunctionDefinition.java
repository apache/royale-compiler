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

import static org.apache.royale.compiler.common.ISourceLocation.UNKNOWN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.constants.INamespaceConstants;
import org.apache.royale.compiler.definitions.*;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.problems.ConflictingDefinitionProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IContainerNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;
import org.apache.royale.compiler.tree.as.IFunctionNode;

import com.google.common.base.Predicate;

/**
 * Each instance of this class represents the definition of an ActionScript
 * function in the symbol table.
 * <p>
 * After a function definition is in the symbol table, it should always be
 * accessed through the read-only <code>IFunctionDefinition</code> interface.
 */
public class FunctionDefinition extends ScopedDefinitionBase implements IFunctionDefinition
{
    private static final ParameterDefinition[] NO_PARAMETERS = new ParameterDefinition[0];

    public FunctionDefinition(String name)
    {
        super(name);
    }

    private ParameterDefinition[] parameters = NO_PARAMETERS;

    // This field is similar to typeReference on DefinitionBase.
    // For a function, typeReference is always a reference to the Function class,
    // so this field store a reference to the return type.
    private IReference returnTypeReference;

    @Override
    public void setNode(IDefinitionNode node)
    {
        super.setNode(node);

        // If we don't have a name offset, maybe we are an anonymous function
        if (getNameStart() == UNKNOWN)
        {
            if (node instanceof IFunctionNode)
            {
                IFunctionNode functionNode = (IFunctionNode)node;
                if (functionNode.getFunctionClassification() == FunctionClassification.LOCAL)
                {
                    // well, we are a local function without a name. So let's make up a name offset.
                    // so for something like: function():void
                    // we want the name offset to be the location right before the open parent of the
                    // parameter list.
                    // We will derive this offset by getting the "parameters" node and subtracting one
                    IContainerNode parametersNode = functionNode.getParametersContainerNode();

                    int synthesizedNameOffset = parametersNode.getStart();
                    if (synthesizedNameOffset > 0)
                        synthesizedNameOffset--;

                    this.setNameLocation(synthesizedNameOffset, synthesizedNameOffset);
                }
            }
        }
    }

    @Override
    public FunctionClassification getFunctionClassification()
    {
        IDefinition parent = getParent();

        if (parent instanceof IFunctionDefinition)
            return FunctionClassification.LOCAL;
        if (parent instanceof IClassDefinition)
            return FunctionClassification.CLASS_MEMBER;
        if (parent instanceof IInterfaceDefinition)
            return FunctionClassification.INTERFACE_MEMBER;
        if (parent instanceof IPackageDefinition)
            return FunctionClassification.PACKAGE_MEMBER;
        if (parent == null)
        {
            if (inPackageNamespace())
                return FunctionClassification.PACKAGE_MEMBER;

            return FunctionClassification.FILE_MEMBER;
        }

        assert false;
        return null;
    }

    @Override
    public ParameterDefinition[] getParameters()
    {
        return parameters;
    }

    @Override
    public boolean hasRequiredParameters()
    {
        for (ParameterDefinition parameter : parameters)
        {
            if (!(parameter.hasDefaultValue()) && (!parameter.isRest()))
                return true;
        }

        return false;
    }

    public void setParameters(ParameterDefinition[] value)
    {
        assert value != null : "setParameters() wants an empty array, not null";

        parameters = value;

        // Parameters from ABC may be unnamed, so we may have avoided the setContainingScope
        // logic in addDefinition().
        for (ParameterDefinition p : parameters)
        {
            p.setContainingScope(this.getContainedScope());
        }
    }

    @Override
    public String getReturnTypeAsDisplayString()
    {
        return returnTypeReference != null ? returnTypeReference.getDisplayString() : "";
    }

    /**
     * Sets a reference to the return type for this function.
     * 
     * @param returnTypeReference An {@link IReference} to a class or interface.
     */
    public void setReturnTypeReference(IReference returnTypeReference)
    {
        this.returnTypeReference = returnTypeReference;
    }

    @Override
    public IReference getReturnTypeReference()
    {
        return returnTypeReference;
    }

    @Override
    public ITypeDefinition resolveReturnType(ICompilerProject project)
    {
        // The return type of a constructor is its class,
        // not <code>void</code>.
        if (isConstructor())
        {
            IDefinition typeDef = getParent();
            if (typeDef instanceof ITypeDefinition)
                return (ITypeDefinition)typeDef;
            else
                return null;
        }

        // TODO We don't really need to make this a signature dependency
        // if this function is a function closure.  If this function
        // is a closure then we could make this an expression dependency
        // instead.
        DependencyType dt = DependencyType.SIGNATURE;
        return resolveType(returnTypeReference, project, dt);
    }

    @Override
    public boolean isConstructor()
    {
        return (flags & FLAG_CONSTRUCTOR) != 0;
    }

    public void setAsConstructor(ClassDefinition classDef)
    {
        flags |= FLAG_CONSTRUCTOR;

        classDef.setConstructor(this);
    }

    @Override
    public boolean isCastFunction()
    {
        return (flags & FLAG_CAST_FUNCTION) != 0;
    }

    public void setCastFunction()
    {
        flags |= FLAG_CAST_FUNCTION;
    }

    @Override
    public boolean inlineFunction()
    {
        // only attempt to inline a function if it has the inline m
        if (!isInline())
            return false;

        if (canFunctionBeInlined())
            return true;

        return false;
    }

    @Override
    public final boolean isInline()
    {
        IMetaTag inlineMetaData = getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_INLINE);
        if (inlineMetaData == null)
            return false;

        return true;
    }

    protected final boolean canFunctionBeInlined()
    {
        // only final or static functions and top level functions can be inlined
        if (!(isFinal() || isStatic() || isTopLevelDefinition()))
            return false;

        // methods on an interface can't be inlined, only the implementation methods
        IDefinition containingDef = getContainingScope().getDefinition();
        if (containingDef instanceof InterfaceDefinition)
            return false;

        return true;
    }

    @Override
    public boolean isAbstract()
    {
        if(super.isAbstract())
        {
            return true;
        }
        IMetaTag[] metaTags = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_ABSTRACT);
        return metaTags != null && metaTags.length > 0;
    }

    /**
     * Utility to mark a definition as abstract. This method should only ever be
     * called during construction or initialization of a definition.
     */
    @Override
    public void setAbstract()
    {
        super.setAbstract();

        MetaTag abstractMetaTag = new MetaTag(this, IMetaAttributeConstants.ATTRIBUTE_ABSTRACT, new IMetaTagAttribute[0]);
        addMetaTag(abstractMetaTag);
    }

    @Override
    public boolean isPrivate()
    {
        if (super.isPrivate())
        {
            return true;
        }
        if (isConstructor())
        {
            IFunctionNode funcNode = getFunctionNode();
            if (funcNode != null
                    && INamespaceConstants.private_.equals(funcNode.getNamespace()))
            {
                // super.isPrivate() checks the namespace reference, but all
                // constructors always use this namespace reference:
                // NamespaceDefinition.getCodeModelImplicitDefinitionNamespace()
                // constructors can't use the normal private reference or
                // they'll incorrectly show up in scope searches.
                return true;
            }

            IDefinition parent = getParent();
            if (parent == null)
            {
                return false;
            }
            // if the construcutor does not have a private namespace, the parent
            // class may have [RoyalePrivateConstructor] metadata instead. this
            // is how private constructors are stored in bytecode.
            IMetaTag[] metaTags = parent.getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_PRIVATE_CONSTRUCTOR);
            return metaTags != null && metaTags.length > 0;
        }
        return false;
    }

    @Override
    public boolean overridesAncestor(ICompilerProject project)
    {
        return (resolveOverriddenFunction(project) != null);
    }

    @Override
    public FunctionDefinition resolveOverriddenFunction(ICompilerProject project)
    {
        // The function must be a class method.
        if (getFunctionClassification() != FunctionClassification.CLASS_MEMBER)
            return null;

        // Get the method's class.
        ClassDefinition cls = (ClassDefinition)getParent();

        // Iterate over the superclasses of this method's class.
        ClassDefinition base = (ClassDefinition)cls.resolveBaseClass(project);

        if (base != null)
        {
            // Adjust the namespace if this is the protected namespace
            INamespaceDefinition namespace = resolveNamespace(project);
            if (namespace == null)
                return null; // can't resolve namespace, hence overridden function can't be resolved

            INamespaceDefinition protectedNS = cls.getProtectedNamespaceReference().resolveNamespaceReference(project);
            if (namespace.equals(protectedNS))
                namespace = base.getProtectedNamespaceReference().resolveNamespaceReference(project);

            // Look for a property with the same name as this function in the base class
            // the lookup will search up the inheritance chain, so we don't have to worry about
            // walking up the inheritance chain here.
            IDefinition baseFunc = base.getContainedScope().getQualifiedPropertyFromDef(
                    project, base, this.getBaseName(), namespace, false);

            if (baseFunc instanceof FunctionDefinition) return (FunctionDefinition)baseFunc;
            
            IDefinition anyDef = base.getContainedScope().getPropertyFromDef(project, base, this.getBaseName(), new PrivatePredicate(!project.getAllowPrivateNameConflicts()), false);
            if (anyDef != null) // there might be a variable or a function in a different namespace (private vs protected)
            {
                if (this instanceof IAccessorDefinition) {
                    //cover cases between getter and setter
                    boolean otherIsGetter = anyDef instanceof IGetterDefinition;
                    if (this instanceof IGetterDefinition) {
                        if (!otherIsGetter) {
                            return null;
                        }
                    } else {
                        if (otherIsGetter) {
                            return null;
                        }
                    }
                }
                project.getProblems().add(new ConflictingDefinitionProblem(this.getFunctionNode(), this.getBaseName(), anyDef.getParent().getQualifiedName()));
            }
        }
        return null;
    }

    @Override
    public boolean isImplementation(ICompilerProject project)
    {
        return resolveImplementedFunction(project) != null;
    }

    /**
     * Return a list of FunctionDefinitions from the base interfaces that this FunctionDefinition overrides.
     * We do not allow overriding of Interface methods, but we need to find the methods that would be overriden
     * so that we can issue a diagnostic.
     *
     * This method will only work for FunctionDefinitions that are interface members.
     *
     * @param project  The project to use to resolve references
     * @return         A list of IFunctionDefinitions from any base interfaces that have the same base name
     *                 as this FunctionDefinition.  If there are none, an empty list will be returned
     */
    public List<IFunctionDefinition> resolveOverridenInterfaceFunctions(ICompilerProject project)
    {
        if( getFunctionClassification() != FunctionClassification.INTERFACE_MEMBER )
            return Collections.emptyList();

        final InterfaceDefinition interf = (InterfaceDefinition)getParent();

        // Find the methods from the base interfaces by calling getPropertiesByNameForMemberAccess
        // and passing in the namespace set that has only the interface namespaces from the extended interface
        // this way we won't find any methods declared in the interface declaring the function
        List<IDefinition> funcs = getContainingASScope().getPropertiesByNameForMemberAccess(
                (CompilerProject)project,
                this.getBaseName(),
                interf.getInterfaceNamespaceSet(project, InterfaceDefinition.InterfaceNamespaceSetOptions.DONT_INCLUDE_THIS));

        if( funcs.size() == 0 )
            return Collections.emptyList();

        List<IFunctionDefinition> conflicts = new ArrayList<IFunctionDefinition>(funcs.size());

        // Convert to a list of IFunctionDefinitions
        for( IDefinition d : funcs )
        {
            if( d instanceof IFunctionDefinition)
                conflicts.add((IFunctionDefinition)d);
        }
        return conflicts;
    }

    @Override
    public IFunctionDefinition resolveImplementedFunction(ICompilerProject project)
    {
        // The function must be a class method.
        if (getFunctionClassification() != FunctionClassification.CLASS_MEMBER)
            return null;

        // Get the method's class.
        final ClassDefinitionBase cls = (ClassDefinitionBase)getParent();

        // Iterate over all the interfaces implemented by this method's class.
        final Iterator<IInterfaceDefinition> iter = cls.interfaceIterator(project);
        while (iter.hasNext())
        {
            final IInterfaceDefinition intf = iter.next();

            // In each interface, look for a method matching this one.
            final IFunctionDefinition f = findMatchingMethod(intf, project);
            if (f != null)
                return f;
        }

        return null;
    }

    // Look in an specified class or interface for a method
    // that matches (i.e., has the same name as) this method.
    private FunctionDefinition findMatchingMethod(ITypeDefinition type, ICompilerProject project)
    {
        final String baseName = getBaseName();
        final boolean isInterface = type instanceof IInterfaceDefinition;

        // Look at the type's local definitions that have the same name as this function.
        final IASScope typeScope = type.getContainedScope();
        final IDefinitionSet definitionSet = typeScope.getLocalDefinitionSetByName(baseName);
        final int n = definitionSet != null ? definitionSet.getSize() : 0;
        for (int i = 0; i < n; i++)
        {
            IDefinition member = definitionSet.getDefinition(i);
            
            // Just look at functions.
            if (member instanceof FunctionDefinition)
            {
                // If one has the same signature as this function, return it.
                final FunctionDefinition f = (FunctionDefinition)member;
                if (hasSameNameAndSignature(f, isInterface, project))
                    return f;
            }
        }

        return null;
    }

    /**
     * Compares the signatures of two methods.
     * 
     * @param other is a function to compare "this" to. Must be from a super
     * type
     * @param otherIsInterface true if "other" is a function from an interface
     */
    private boolean hasSameNameAndSignature(IFunctionDefinition other, boolean otherIsInterface, ICompilerProject project)
    {
        // Compare method names.
        String name1 = getBaseName();
        String name2 = other.getBaseName();
        if (!name1.equals(name2))
            return false;

        // If other is an interface, then we don't need to compare namespaces.
        // As long as the signature match, and the caller guarantees to us that "other" is in fact an
        // interface above us in the derivation chain, then we can ignore the namespaces.
        // If we did not ignore them, they would not match, since by definition interfaces are in a different 
        // namespace

        if (!otherIsInterface)
        {
            // Compare method namespaces.
            // Note that equals() for namespace references
            // actually compares URIs for non-builtin namespaces.
            INamespaceReference nsRef1 = getNamespaceReference();
            INamespaceReference nsRef2 = other.getNamespaceReference();
            if (!nsRef1.equals(nsRef2))
                return false;
        }
        else
        {
            // you must only call this when "this" is a member of a class
            assert (getFunctionClassification() == FunctionClassification.CLASS_MEMBER);

            // we can only implement an interface if we are public
            if (!NamespaceDefinition.getPublicNamespaceDefinition().equals(this.getNamespaceReference()))
                return false;
        }

        return hasCompatibleSignature(other, project);
    }

    private boolean copiedMetaData = false;
    
    public boolean hasCompatibleSignature(IFunctionDefinition other, ICompilerProject project)
    {
    	if (!copiedMetaData)
    	{
    		if (other.isImplementation(project))
    		{
	    		copiedMetaData = true;
	    		IMetaTag myTag = this.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_SWFOVERRIDE);
	    		if (myTag == null)
	    		{
		    		IMetaTag tag = other.getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_SWFOVERRIDE);
		    		if (tag != null)
		    		{
		    			this.addMetaTag(tag);
		    		}
	    		}
    		}
    	}
        // Compare return types.
        ITypeDefinition returnType1 = resolveReturnType(project);
        ITypeDefinition returnType2 = other.resolveReturnType(project);
        if (!project.isCompatibleOverrideReturnType(this, returnType1, returnType2))
            return false;
            
        // Compare parameters.
        IParameterDefinition[] params1 = getParameters();
        IParameterDefinition[] params2 = other.getParameters();

        // Compare number of parameters.
        int n1 = params1 != null ? params1.length : 0;
        int n2 = params2 != null ? params2.length : 0;
        if (n1 != n2)
            return false;

        for (int i = 0; i < n1; i++)
        {
            IParameterDefinition param1 = params1[i];
            IParameterDefinition param2 = params2[i];

            // Compare ith parameter types.
            // The types must be resolved because one might be
            // "Sprite" and the other "flash.display.Sprite".
            ITypeDefinition type1 = param1.resolveType(project);
            ITypeDefinition type2 = param2.resolveType(project);
            if (type1 != type2)
            {
                if (!project.isCompatibleOverrideParameterType(this, type1, type2, i))
                    return false;
            }

            // Compare ith parameter 'rest' flag.
            boolean rest1 = param1.isRest();
            boolean rest2 = param2.isRest();
            if (rest1 != rest2)
                return false;

            // Compare ith parameter optionality.
            boolean hasDefault1 = param1.hasDefaultValue();
            boolean hasDefault2 = param2.hasDefaultValue();
            if (hasDefault1 != hasDefault2)
                return false;
        }

        // The signatures are the same.
        return true;
    }

    @Override
    public IFunctionNode getFunctionNode()
    {
        IASNode node = getNode();
        if (node instanceof IFunctionNode)
            return (IFunctionNode)node;
        return null;
    }

    /**
     * Is this definition a toplevel definition - constructors are special,
     * which is why this is overriden here
     * 
     * @return true if this definition is declared at file scope, or package
     * scope.
     */
    @Override
    public boolean isTopLevelDefinition()
    {
        // Constructors are toplevel if their class is toplevel
        if (isConstructor())
            return ((DefinitionBase)getParent()).isTopLevelDefinition();
        // Not a constructor, just follow the usual rules
        return super.isTopLevelDefinition();
    }

    // TODO Remove everything below here when Royale has been integrated into Fb and Fc.

    @Override
    public boolean matches(DefinitionBase definition)
    {
        boolean match = super.matches(definition);
        if (!match)
            return false;

        IFunctionDefinition functionDefinition = (IFunctionDefinition)definition;

        FunctionClassification classification = functionDefinition.getFunctionClassification();
        if (classification != getFunctionClassification())
            return false;

        // Along with local and file member, name offsets needs to be compared for class/interface members also.
        // This is required to differentiate members having same name belonging to different class/interface
        // within the same AS file - See FBG-3494 for an example.
        if (classification == FunctionClassification.LOCAL || classification == FunctionClassification.FILE_MEMBER
                || classification == FunctionClassification.CLASS_MEMBER || classification == FunctionClassification.INTERFACE_MEMBER)
        {
            if (functionDefinition.getNameStart() != getNameStart() ||
                functionDefinition.getNameEnd() != functionDefinition.getNameEnd())
            {
                return false;
            }
        }
        if (functionDefinition instanceof ISetterDefinition && !(this instanceof ISetterDefinition))
            return false;
        if (functionDefinition instanceof IGetterDefinition && !(this instanceof IGetterDefinition))
            return false;
        if (functionDefinition.isConstructor() && !isConstructor())
            return false;

        //TODO match params?
        if (functionDefinition.getParameters().length != getParameters().length)
            return false;

        return true;
    }

    /**
     * For debugging only. Produces a string such as
     * <code>public function f(int, String):void</code>.
     */
    @Override
    protected void buildInnerString(StringBuilder sb)
    {
        sb.append(getNamespaceReferenceAsString());
        sb.append(' ');

        if (isStatic())
        {
            sb.append(IASKeywordConstants.STATIC);
            sb.append(' ');
        }

        sb.append(IASKeywordConstants.FUNCTION);
        sb.append(' ');

        sb.append(getBaseName());

        sb.append('(');
        IParameterDefinition[] params = getParameters();
        int n = params != null ? params.length : 0;
        for (int i = 0; i < n; i++)
        {
            sb.append(params[i].toString());
            if (i < n - 1)
            {
                sb.append(',');
                sb.append(' ');
            }
        }
        sb.append(')');

        String returnType = getReturnTypeAsDisplayString();
        if (!returnType.isEmpty())
        {
            sb.append(':');
            sb.append(returnType);
        }
    }
    
    private static class PrivatePredicate implements Predicate<IDefinition>
    {
        private boolean findPrivates;

        public PrivatePredicate(boolean b)
        {
            this.findPrivates = b;
        }

        @Override
        public boolean apply(IDefinition definition)
        {
        	if (!definition.isPrivate()) return true;
            return findPrivates;
        }
        
        @Override
        public boolean test(IDefinition input)
        {
            return apply(input);
        }
    }

}
