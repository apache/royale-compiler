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

import java.lang.ref.SoftReference;
import java.util.*;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.RecursionGuard;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.constants.IMetaAttributeConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.metadata.IMetaTagAttribute;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.as.codegen.BindableHelper;
import org.apache.royale.compiler.internal.definitions.metadata.MetaTag;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASProjectScope;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.scopes.ASScopeCache;
import org.apache.royale.compiler.internal.scopes.TypeScope;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.ClassNode;
import org.apache.royale.compiler.problems.AmbiguousReferenceProblem;
import org.apache.royale.compiler.problems.DuplicateInterfaceProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.UnknownInterfaceProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.utils.Version;
import com.google.common.collect.Iterables;

public abstract class ClassDefinitionBase extends TypeDefinitionBase implements IClassDefinition
{
    protected ClassDefinitionBase(String name)
    {
        super(name);
        
        alternatives = null;
    }

    @Override
    public IClassDefinition[] resolveAncestry(ICompilerProject project)
    {
        return Iterables.toArray(classIterable(project, true), IClassDefinition.class);
    }

    /**
     * Resolve one of the implemented interfaces.
     * 
     * @param project {@link ICompilerProject} whose symbol table should be used
     * to resolve references.
     * @param i Index indicating which implemented interface to resolve.
     * @return {@link IInterfaceDefinition} for the implemented interface if the
     * reference can be resolved, null otherwise.
     */
    public InterfaceDefinition resolveImplementedInterface(ICompilerProject project, int i)
    {
        IReference[] implementedInterfaces = getImplementedInterfaceReferences();
        if ((implementedInterfaces != null) && (implementedInterfaces.length > i))
        {
            ITypeDefinition typeDefinition = resolveType(implementedInterfaces[i], project, DependencyType.INHERITANCE);
            if (typeDefinition instanceof IInterfaceDefinition)
                return (InterfaceDefinition)typeDefinition;
        }
        return null;
    }

    @Override
    public IInterfaceDefinition[] resolveImplementedInterfaces(ICompilerProject project)
    {
        if( this.getImplementedInterfaceReferences().length > 0 )
            return ((CompilerProject)project).getCacheForScope(getContainedScope()).resolveInterfaces();
        
        return new IInterfaceDefinition[0];
    }

    /**
     * Resolve the implemented interfaces of this Class
     * @param project   the active project
     * @return          An array of all the interfaces this class implements
     */
    public IInterfaceDefinition[] resolveInterfacesImpl (ICompilerProject project)
    {
        IInterfaceDefinition[] implementedInterfaces =
            resolveImplementedInterfaces(project, (Collection<ICompilerProblem>)null);
        
        // Don't return null elements for interfaces that can't be resolved.
        return filterNullInterfaces(implementedInterfaces);
    }

    /**
     * Version of resolveImplementedInterfaces that will log Problems associated
     * with resolving the implemented interfaces This will not log any problems
     * if null is passed in for the problem collection.
     * 
     * @param project project to resolve the interfaces in
     * @param problems a Collection to add problems to if any are encountered,
     * or null if the caller is not interested in the problems.
     * @return Array of InterfaceDefinitions which are the interfaces
     * implemented by this class in the given project
     */
    public InterfaceDefinition[] resolveImplementedInterfaces(ICompilerProject project, Collection<ICompilerProblem> problems)
    {
        return resolveImplementedInterfaces(project, problems, true);
    }

    /**
     * Version of resolveImplementedInterfaces that will log Problems associated
     * with resolving the implemented interfaces This will not log any problems
     * if null is passed in for the problem collection. This method will also
     * find, or not find, implicit implemented interfaces, depending on the
     * value of the includeImplicit flag. Implicit interfaces are things like
     * IEventDispatcher when a Class is marked [Bindable] - the user does not
     * need to explicitly implement IEventDispatcher, but the compiler must act
     * as if the interface was implemented.
     * 
     * @param project project to resolve the interfaces in
     * @param problems a Collection to add problems to if any are encountered,
     * or null if the caller is not interested in the problems.
     * @param includeImplicits true, if implicit interfaces should be found,
     * false, if they should not be found.
     * @return Array of InterfaceDefinitions which are the interfaces
     * implemented by this class in the given project
     */
    public InterfaceDefinition[] resolveImplementedInterfaces(ICompilerProject project, Collection<ICompilerProblem> problems, boolean includeImplicits)
    {
        IReference[] implementedInterfaces = getImplementedInterfaceReferences();
        if (implementedInterfaces != null)
        {
            int n = implementedInterfaces.length;
            InterfaceDefinition[] result = new InterfaceDefinition[n];

            Set<InterfaceDefinition> seenInterfaces = null;
            if (problems != null)
            {
                seenInterfaces = new HashSet<InterfaceDefinition>();
            }

            for (int i = 0; i < n; i++)
            {
                IReference implementedInterface = implementedInterfaces[i];
                
                ITypeDefinition typeDefinition =
                    resolveType(implementedInterface, project, DependencyType.INHERITANCE);

                if (!(typeDefinition instanceof InterfaceDefinition))
                {
                    IDefinition idef = null;
                    if (typeDefinition == null)
                    {
                        idef = implementedInterface.resolve(project, (ASScope)this.getContainingASScope(), DependencyType.INHERITANCE, true);
                    }
                    if (problems != null)
                    {
                        if (idef instanceof AmbiguousDefinition)
                            problems.add(new AmbiguousReferenceProblem(getNode(), implementedInterface.getDisplayString()));
                        else
                            problems.add(unknownInterfaceProblem(implementedInterface, i));                        
                    }

                    typeDefinition = null;
                }
                else if (seenInterfaces != null)
                {
                    if (seenInterfaces.contains(typeDefinition))
                    {
                        if (problems != null)
                            problems.add(duplicateInterfaceProblem(implementedInterface, i));
                    }
                    
                    seenInterfaces.add((InterfaceDefinition)typeDefinition);
                }
                
                if (problems != null)
                {
                    // Report a problem if the interface is deprecated
                    // and the reference to it is not within a deprecated API.
                    if (typeDefinition != null && typeDefinition.isDeprecated())
                    {
                        IASNode node = getInterfaceNode(i);
                        if (!SemanticUtils.hasDeprecatedAncestor(node))
                        {
                            ICompilerProblem problem = SemanticUtils.createDeprecationProblem(typeDefinition, node);
                            problems.add(problem);
                        }
                    }
                }
                 
                result[i] = (InterfaceDefinition)typeDefinition;
            }

            if (includeImplicits)
            {
                if (needsEventDispatcher(project))
                {
                    ITypeDefinition iEventDispatcher = resolveType(BindableHelper.STRING_IEVENT_DISPATCHER, project, null);
                    if (iEventDispatcher instanceof InterfaceDefinition)
                    {
                        InterfaceDefinition[] newResult = new InterfaceDefinition[result.length + 1];
                        System.arraycopy(result, 0, newResult, 0, result.length);
                        newResult[result.length] = (InterfaceDefinition)iEventDispatcher;
                        result = newResult;
                    }
                }
            }
            return result;
        }
        return new InterfaceDefinition[0];
    }

    /**
     * Determine if this class needs to add an implicit 'implements
     * flash.events.IEventDispatcher' due to the class, or some of its members
     * being marked bindable. If this class is marked bindable, or if it has
     * members that are marked bindable then this class will need to implement
     * IEventDispatcher if no baseclass already implements IEventDispatcher, and
     * no implemented interface extends IEventDispatcher.
     * <p>
     * The result of this method is cached in the {@link ASScopeCache} for the
     * {@link TypeScope} contained by this class.
     * 
     * @param project The project to use to resolve interfaces and base classes
     * @return true if this class needs to add IEventDispatcher to its interface
     * list, and should implement the IEventDispatcher methods.
     */
    public boolean needsEventDispatcher(ICompilerProject project)
    {
        if (isImplicit())
            return false;
        return ((CompilerProject)project).getCacheForScope(getContainedScope()).needsEventDispatcher();
    }

    /**
     * Determine if this class needs to add an implicit 'implements
     * flash.events.IEventDispatcher' due to the class, or some of its members
     * being marked bindable. If this class is marked bindable, or if it has
     * members that are marked bindable then this class will need to implement
     * IEventDispatcher if no baseclass already implements IEventDispatcher, and
     * no implemented interface extends IEventDispatcher.
     * <p>
     * This method is called by the {@link ASScopeCache} and should not be
     * called by other classes. All classes other than the {@link ASScopeCache}
     * should call {@link #needsEventDispatcher(ICompilerProject)}.
     * 
     * @param project The project to use to resolve interfaces and base classes
     * @return true if this class needs to add IEventDispatcher to its interface
     * list, and should implement the IEventDispatcher methods.
     */
    public boolean computeNeedsEventDispatcher(ICompilerProject project)
    {
        if (isBindable() || getContainedScope().hasAnyBindableDefinitions())
        {
            ITypeDefinition iEventDispatcher = resolveType(BindableHelper.STRING_IEVENT_DISPATCHER, project, null);
            if (iEventDispatcher != null)
            {

                IClassDefinition baseClass = resolveBaseClass(project);
                while (baseClass != null)
                {
                    if (baseClass.isInstanceOf(iEventDispatcher, project))
                    {
                        // The base class already implements IEventDispatcher, so we don't need to implement
                        // it here
                        return false;
                    }
                    if (baseClass.needsEventDispatcher(project)) {
                        //check the base class for 'needs Bindable support'
                        //If the base class needs implicit Bindable implementation,
                        //then this sub-class will inherit its
                        //compiler-generated implementation, so this sub-class does not 'need' it
                        return false;
                    }
                    //check the full ancestor chain
                    baseClass = baseClass.resolveBaseClass(project);
                }

                InterfaceDefinition[] interfs = resolveImplementedInterfaces(project, null, false);

                for (InterfaceDefinition interf : interfs)
                {
                    if (interf != null && interf.isInstanceOf(iEventDispatcher, project))
                        return false;
                }

                // None of the base classes implement IEventDispatcher (either implicitly or explicitly)
                // and this class does not explicitly implement IEventDispatcher
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if this class needs a static event dispatcher added to it. This
     * is neccessary if it has any static properties that are bindable.
     * 
     * @param project Project to use to resolve things.
     * @return true, if we need to codegen a static event dispatcher method
     */
    public boolean needsStaticEventDispatcher(ICompilerProject project)
    {
        boolean isBindable = isBindable();
        Collection<IDefinitionSet> defs = getContainedScope().getAllLocalDefinitionSets();
        for (IDefinitionSet set : defs)
        {
            for (int i = 0, l = set.getSize(); i < l; ++i)
            {
                IDefinition d = set.getDefinition(i);
                if (isBindable && d.isStatic())
                    return true;
                else if (d.isStatic() && d.isBindable())
                    return true;
            }
        }
        return false;
    }

    /**
     * Helper method to generate an UnknownInterfaceProblem - will use the Node
     * for the implemented interfaces, if there is one, for location info,
     * therwise will use the definition for location info
     */
    private UnknownInterfaceProblem unknownInterfaceProblem(IReference interfRef, int idx)
    {
        IASNode node = getInterfaceNode(idx);
        if (node != null)
            return new UnknownInterfaceProblem(node, interfRef.getDisplayString());
        else
            return new UnknownInterfaceProblem(this, interfRef.getDisplayString());
    }

    /**
     * Helper method to generate an DuplicateInterfaceProblem - will use the
     * Node for the implemented interfaces, if there is one, for location info,
     * therwise will use the definition for location info
     */
    private DuplicateInterfaceProblem duplicateInterfaceProblem(IReference interfRef, int idx)
    {
        IASNode node = getInterfaceNode(idx);
        if (node != null)
            return new DuplicateInterfaceProblem(node, getBaseName(), interfRef.getDisplayString());
        else
            return new DuplicateInterfaceProblem(this, getBaseName(), interfRef.getDisplayString());
    }

    /**
     * Get the IASNode for the implemented interface at index i - used for error
     * reporting
     * 
     * @param i the Index of the interface you want the node for
     * @return the IASNode representing the interface in the implements clause,
     * or the Node for this class if the interface node can't be determined (at
     * least the error will then point at the right class).
     */
    private IASNode getInterfaceNode(int i)
    {
        ITypeNode typeNode = this.getNode();
        IASNode site = typeNode;
        if (typeNode instanceof ClassNode)
        {
            ClassNode clsNode = (ClassNode)typeNode;
            IExpressionNode interfs[] = clsNode.getImplementedInterfaceNodes();
            site = interfs[i];
        }
        return site;
    }

    public Iterable<IClassDefinition> classIterable(final ICompilerProject project, final boolean includeThis)
    {
        final ClassDefinitionBase initialClass = this;
        return new Iterable<IClassDefinition>()
        {
            @Override
            public Iterator<IClassDefinition> iterator()
            {
                return initialClass.classIterator(project, includeThis);
            }

        };
    }

    @Override
    public IClassIterator classIterator(ICompilerProject project, boolean includeThis)
    {
        return new ClassIterator(this, project, includeThis);
    }

    @Override
    public Iterator<IInterfaceDefinition> interfaceIterator(ICompilerProject project)
    {
        return new InterfaceDefinition.InterfaceIterator(this, project, null);
    }

    private ArrayList<IDefinition> baseDefinitions = null;
    private ArrayList<IDefinition> implDefinitions = null;
    
    @Override
    public boolean isInstanceOf(final ITypeDefinition type, ICompilerProject project)
    {
        // A class is considered an instance of itself.
        if (type == this)
            return true;

        if (type instanceof IClassDefinition)
        {
            if (!getPerformanceCachingEnabled())
            {
                baseDefinitions = null;

                // We're trying to determine whether this class
                // is derived from a specified class ('type').
                // Iterate the superclass chain looking for 'type'.
                Iterator<IClassDefinition> iter = classIterator(project, false);
                while (iter.hasNext())
                {
                    IClassDefinition cls = iter.next();
                    if (cls == type)
                        return true;
                }
                return false;
            }
        	else if (baseDefinitions == null)
        	{
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CLASS_DEFINITION_BASE) == CompilerDiagnosticsConstants.CLASS_DEFINITION_BASE)
            		System.out.println("ClassDefinitionBase waiting for lock for " + this.getQualifiedName());
	        	synchronized (this) 
	        	{
		        	if (baseDefinitions == null)
		        	{
		        		ArrayList<IDefinition> bases = new ArrayList<IDefinition>();
		        		
			            // We're trying to determine whether this class
			            // is derived from a specified class ('type').
			            // Iterate the superclass chain looking for 'type'.
			            Iterator<IClassDefinition> iter = classIterator(project, false);
			            while (iter.hasNext())
			            {
			                IClassDefinition cls = iter.next();
			                bases.add(cls);
			            }
			            baseDefinitions = bases;
		        	}
	        	}
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CLASS_DEFINITION_BASE) == CompilerDiagnosticsConstants.CLASS_DEFINITION_BASE)
            		System.out.println("ClassDefinitionBase done with lock for " + this.getQualifiedName());
        	}
            return baseDefinitions.contains(type);
        }
        else if (type instanceof IInterfaceDefinition)
        {
            if (!getPerformanceCachingEnabled())
            {
                implDefinitions = null;

                // We're trying to determine whether this class
                // implements a specified interface ('type').
                // Iterate all of the interfaces that this class implements,
                // looking for 'type'.
                Iterator<IInterfaceDefinition> iter = interfaceIterator(project);
                while (iter.hasNext())
                {
                    IInterfaceDefinition intf = iter.next();
                    if (intf == type)
                        return true;
                }
                return false;
            }
        	else if (implDefinitions == null)
        	{
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CLASS_DEFINITION_BASE) == CompilerDiagnosticsConstants.CLASS_DEFINITION_BASE)
            		System.out.println("ClassDefinitionBase waiting for lock for " + this.getQualifiedName());
	        	synchronized (this) 
	        	{
	            	if (implDefinitions == null)
	            	{
		        		ArrayList<IDefinition> impls = new ArrayList<IDefinition>();
		        		
			            // We're trying to determine whether this class
			            // implements a specified interface ('type').
			            // Iterate all of the interfaces that this class implements,
			            // looking for 'type'.
			            Iterator<IInterfaceDefinition> iter = interfaceIterator(project);
			            while (iter.hasNext())
			            {
			                IInterfaceDefinition intf = iter.next();
			                impls.add(intf);
			            }
			            implDefinitions = impls;
	            	}
	        	}
            	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.CLASS_DEFINITION_BASE) == CompilerDiagnosticsConstants.CLASS_DEFINITION_BASE)
            		System.out.println("ClassDefinitionBase done with lock for " + this.getQualifiedName());
	        }
            return implDefinitions.contains(type);
        }

    	return false;
    }

    @Override
    public Set<IInterfaceDefinition> resolveAllInterfaces(ICompilerProject project)
    {
        Set<IInterfaceDefinition> interfaces = new HashSet<IInterfaceDefinition>();

        Iterator<IInterfaceDefinition> iter = interfaceIterator(project);
        while (iter.hasNext())
        {
            IInterfaceDefinition intf = iter.next();
            interfaces.add(intf);
        }

        return interfaces;
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

    /*
     * This inner class implements an iterator that enumerates all of this
     * ClassDefinition's superclasses. <p> It will stop iterating when it
     * detects a loop in the superclass chain; at that point,
     * <code>foundLoop()</code> will return <code>true</code>.
     */
    public static class ClassIterator implements IClassIterator
    {
        public ClassIterator(IClassDefinition thisClass, ICompilerProject project, boolean includeThis)
        {
            assert thisClass != null;
            assert project != null;

            this.project = project;
            nextClass = includeThis ? thisClass : thisClass.resolveBaseClass(project);
            guard = new RecursionGuard(nextClass);
            foundLoop = false;
        }

        private ICompilerProject project;

        private IClassDefinition nextClass;

        private RecursionGuard guard;

        private boolean foundLoop;

        @Override
        public boolean hasNext()
        {
            return nextClass != null;
        }

        @Override
        public IClassDefinition next()
        {
            if (!hasNext())
                throw new NoSuchElementException();

            IClassDefinition next = nextClass;
            nextClass = nextClass.resolveBaseClass(project);

            // The RecursionGuard will detect a loop in the superclass chain.
            if (guard.isLoop(nextClass))
            {
                foundLoop = true;
                nextClass = null;
            }

            return next;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean foundLoop()
        {
            return foundLoop;
        }
    }

    /**
     * Represents the information in
     * [Alternative(replacement="...", since"...")] metadata.
     */
    private static final class AlternativeInformation
    {
        /**
         * Constructor.
         */
        public AlternativeInformation(String replacement, Version sinceVersion)
        {
            this.replacement = replacement;
            this.sinceVersion = sinceVersion;
        }

        private String replacement;
        private Version sinceVersion;

        /**
         * @return the replacement
         */
        public String getReplacement()
        {
            return replacement;
        }

        /**
         * @return the 'since' version
         */
        public Version getSinceVersion()
        {
            return sinceVersion;
        }
    }

    private SoftReference<AlternativeInformation[]> alternatives;

    private AlternativeInformation[] getAlternatives()
    {
        AlternativeInformation[] result = null;
        if (alternatives != null)
            result = alternatives.get();

        if (result != null)
            return result;

        result = buildAlternatives();
        alternatives = new SoftReference<AlternativeInformation[]>(result);

        return result;
    }

    private AlternativeInformation[] buildAlternatives()
    {
        IMetaTag[] metaTags = getMetaTagsByName(IMetaAttributeConstants.ATTRIBUTE_ALTERNATIVE);
        if (metaTags == null || metaTags.length == 0)
            return new AlternativeInformation[0];

        List<AlternativeInformation> result = new LinkedList<AlternativeInformation>();
        for (IMetaTag metaTag : metaTags)
        {
            String replacement = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_ALTERNATIVE_REPLACEMENT);
            if (replacement == null || replacement.isEmpty())
                continue;

            if (replacement.compareTo(IMetaAttributeConstants.VALUE_INSPECTABLE_ENVIRONMENT_NONE) == 0)
                continue;

            String sinceString = metaTag.getAttributeValue(IMetaAttributeConstants.NAME_ALTERNATIVE_SINCE);
            Version sinceVersion = null;
            if (sinceString != null)
            {
                try
                {
                    sinceVersion = new Version(sinceString);
                }
                catch (Exception e)
                {
                    continue;
                }
            }

            result.add(new AlternativeInformation(replacement, sinceVersion));
        }

        return result.toArray(new AlternativeInformation[result.size()]);
    }

    @Override
    public IClassDefinition[] getAlternativeClasses(ICompilerProject project, Version version)
    {
        AlternativeInformation[] alternatives = getAlternatives();
        List<IClassDefinition> result = new ArrayList<IClassDefinition>(alternatives.length);

        ASProjectScope projectScope = (ASProjectScope)project.getScope();

        for (AlternativeInformation alternative : alternatives)
        {
            Version alternativeSinceVersion = alternative.getSinceVersion();
            if (alternativeSinceVersion.compareBugFixVersionTo(version) >= 0)
            {
                String replacement = alternative.getReplacement();
                IDefinition def = projectScope.findDefinitionByName(replacement);

                // replacements should only point to classes, so ignore anything which
                // isn't a class
                if (!(def instanceof IClassDefinition))
                    continue;

                result.add((IClassDefinition)def);
            }
        }

        return result.toArray(new IClassDefinition[result.size()]);
    }

    @Override
    public IMetaTag[] findMetaTagsByName(String name, ICompilerProject project)
    {
        if (IMetaAttributeConstants.NON_INHERITING_METATAGS.contains(name))
            return getMetaTagsByName(name);

        List<IMetaTag> list = new ArrayList<IMetaTag>();

        Iterator<IClassDefinition> classIterator = classIterator(project, true);
        while (classIterator.hasNext())
        {
            IClassDefinition c = classIterator.next();
            for (IMetaTag metaTag : c.getMetaTagsByName(name))
            {
                list.add(metaTag);
            }
        }

        return list.toArray(new IMetaTag[0]);
    }

    @Override
    public abstract IReference[] getImplementedInterfaceReferences();

    @Override
    public String getIconFile()
    {
        IMetaTag iconFileMetaTag = getMetaTagByName(IMetaAttributeConstants.ATTRIBUTE_ICON_FILE);
        return iconFileMetaTag != null ? iconFileMetaTag.getValue() : null;
    }
}
