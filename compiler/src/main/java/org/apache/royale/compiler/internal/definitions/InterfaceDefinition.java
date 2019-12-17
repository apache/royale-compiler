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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.RecursionGuard;
import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IPackageDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.definitions.references.IReference;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.problems.CircularTypeReferenceProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.IncompatibleInterfaceMethodProblem;
import org.apache.royale.compiler.problems.UnimplementedInterfaceMethodProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IDefinitionSet;

/**
 * Each instance of this class represents the definition of an ActionScript
 * interface in the symbol table.
 * <p>
 * After an interface definition is in the symbol table, it should always be
 * accessed through the read-only <code>IInterfaceDefinition</code> interface.
 */
public class InterfaceDefinition extends TypeDefinitionBase implements IInterfaceDefinition
{
    public InterfaceDefinition(String name)
    {
        super(name);
        interfaceNamespace = null;
    }

    private IReference[] extendedInterfaces;

    /**
     * The namespace to use as the interface namespace.
     */
    private NamespaceDefinition.ILanguageNamespaceDefinition interfaceNamespace;

    @Override
    public InterfaceClassification getInterfaceClassification()
    {
        IDefinition parent = getParent();

        if (parent instanceof IPackageDefinition)
            return InterfaceClassification.PACKAGE_MEMBER;
        if (parent == null)
        {
            if (inPackageNamespace())
                return InterfaceClassification.PACKAGE_MEMBER;

            return InterfaceClassification.FILE_MEMBER;
        }

        assert false;
        return null;
    }

    @Override
    public String[] getExtendedInterfacesAsDisplayStrings()
    {
        if (extendedInterfaces == null)
            return new String[0];

        String[] interfaces = new String[extendedInterfaces.length];
        for (int i = 0, l = extendedInterfaces.length; i < l; ++i) {
            if(extendedInterfaces[i] != null) {
                interfaces[i] = extendedInterfaces[i].getDisplayString();
            }
        }

        return interfaces;
    }

    public void setExtendedInterfaceReferences(IReference[] extendedInterfaces)
    {
        this.extendedInterfaces = extendedInterfaces;
    }

    @Override
    public IReference[] getExtendedInterfaceReferences()
    {
        return extendedInterfaces;
    }

    @Override
    public IInterfaceDefinition[] resolveExtendedInterfaces(ICompilerProject project)
    {
        return ((CompilerProject)project).getCacheForScope(getContainedScope()).resolveInterfaces();
    }

    /**
     * resolve the interfaces this interface extends
     * @param project   the active project
     * @return          An Array of interfaces that this interface extends
     */
    @Override
    public IInterfaceDefinition[] resolveInterfacesImpl (ICompilerProject project)
    {
        int n = extendedInterfaces != null ? extendedInterfaces.length : 0;

        // An interface which extends no other interfaces is considered
        // to have an inheritance dependency on Object.
        if (n == 0)
        {
            addDependencyOnBuiltinType((CompilerProject)project, BuiltinType.OBJECT,
                                       DependencyType.INHERITANCE);
        }
        
        IInterfaceDefinition[] result = new IInterfaceDefinition[n];

        for (int i = 0; i < n; i++)
        {
            ITypeDefinition typeDefinition =
                    resolveType(extendedInterfaces[i], project, DependencyType.INHERITANCE);

            if (!(typeDefinition instanceof IInterfaceDefinition))
                typeDefinition = null;

            result[i] = (IInterfaceDefinition)typeDefinition;
        }

        return this.filterNullInterfaces(result);
    }
    
    /**
     * Adds a dependency from the compilation unit for this interface
     * to the the compilation unit for the specified builtin type,
     * such as <code>Object</code>.
     * 
     * @param project The {@link CompilerProject} used for resolving references.
     * @param builtinType The {@link BuiltinType} type this interface is dependent on.
     * @param dependencyType The {@link DependencyType} to be created.
     */
    private void addDependencyOnBuiltinType(CompilerProject project, BuiltinType builtinType,
                                            DependencyType dependencyType)
    {
        getContainingASScope().addDependencyOnBuiltinType(project, builtinType, dependencyType);
    }

    @Override
    public boolean isInstanceOf(final ITypeDefinition type, ICompilerProject project)
    {
        // An interface is considered an instance of itself.
        if (type == this)
            return true;

        //  An interface is an instance of Object by definition.
        if (project.getBuiltinType(BuiltinType.OBJECT).equals(type))
            return true;

        // Since 'this' is an interface, 'type' must also be an interface.
        // (An interface can't be a kind of class.)
        if (!(type instanceof IInterfaceDefinition))
            return false;

        // We're trying to determine whether this interface
        // extends a specified interface ('type').
        // Iterate all of the interfaces that this class extends,
        // looking for 'type'.
        Iterator<IInterfaceDefinition> iter = interfaceIterator(project, false);
        while (iter.hasNext())
        {
            IInterfaceDefinition intf = iter.next();
            if (intf == type)
                return true;
        }
        return false;
    }

    @Override
    public Set<IInterfaceDefinition> resolveAllInterfaces(ICompilerProject project)
    {
        Set<IInterfaceDefinition> interfaces = new HashSet<IInterfaceDefinition>();

        Iterator<IInterfaceDefinition> iter = interfaceIterator(project, false);
        while (iter.hasNext())
        {
            IInterfaceDefinition intf = iter.next();
            interfaces.add(intf);
        }

        return interfaces;
    }

    @Override
    public Iterator<IInterfaceDefinition> interfaceIterator(ICompilerProject project, boolean includeThis)
    {
        return new InterfaceIterator(this, project, includeThis, null);
    }

    public Iterator<IInterfaceDefinition> interfaceIterator(ICompilerProject project, boolean includeThis, Collection<ICompilerProblem> problems)
    {

        return new InterfaceIterator(this, project, includeThis, problems);
    }

    /**
     * Iterates over all the Interfaces that are implemented/extended by a given
     * interface/class If a problem collection is passed in, will detect
     * circular dependencies.
     */
    public static class InterfaceIterator implements Iterator<IInterfaceDefinition>
    {
        /**
         * Creates iterator for all interfaces extended by an interface
         * 
         * @param thisInterface is the interface we are interested in
         * @param problems may be null if problem reporting not needed
         */
        public InterfaceIterator(IInterfaceDefinition thisInterface, ICompilerProject project, boolean includeThis, Collection<ICompilerProblem> problems)
        {
            this.project = project;
            initFromInterface(Collections.singleton(thisInterface), includeThis, problems);
        }

        /**
         * Creates iterator for all interfaces implemented by a class
         * 
         * @param cls is the class definition we are interested in
         * @param problems may be null if problem reporting not needed
         */
        public InterfaceIterator(ClassDefinitionBase cls, ICompilerProject project, Collection<ICompilerProblem> problems)
        {
            this.project = project;

            // First we walk the class hierarchy (not interface hierarchy) to gather all the 
            // "first level" interfaces implemented by cls
            Set<IInterfaceDefinition> clsInterfaces = new HashSet<IInterfaceDefinition>();

            Iterator<IClassDefinition> classIterator = cls.classIterator(project, true);

            while (classIterator.hasNext())
            {
                IClassDefinition nextClass = classIterator.next();
                ClassDefinitionBase classDefinitionBase = (ClassDefinitionBase)nextClass;

                // Note: getImplementedIntefaceRefs will not return one for IEventDispatcher
                // on [Bindable] classes. Since we don't want to see IEventDisp, we iterate this way.
                IReference[] refs = nextClass.getImplementedInterfaceReferences();

                InterfaceDefinition[] idefs = classDefinitionBase.resolveImplementedInterfaces(project, problems);
                for (int i = 0; i < refs.length; ++i)
                {
                    if (idefs[i] != null)
                    {
                        // Can be null if the interface doesn't actually exist
                        // we don't report a problem here - someone else does that
                        clsInterfaces.add(idefs[i]);
                    }
                }
            }

            // now that we have all the "first level" interfaces, do the full analysis
            initFromInterface(clsInterfaces, true, problems);
        }

        /*********** member fields **********/

        private final ICompilerProject project;

        // Constructor puts everything that will be iterated in this set
        Set<IInterfaceDefinition> theInterfaces = new HashSet<IInterfaceDefinition>();
        Iterator<IInterfaceDefinition> underlyingIterator = null;

        /********** member functions ************/

        /**
         * Follows the inheritance of a some interfaces to find all interfaces,
         * and detect loops Does a depth first search. This is critical, becuase
         * otherwise it would be difficult or impossible to differentiate
         * between a true dependency loop and a legal "diamond hierarchy".
         * 
         * @param thisInterfaces is a set of interfaces to be analyzed.
         * @param includeThis will cuase thisInterfaces to be aded to
         * this.theInterfaces
         */
        private void initFromInterface(Set<IInterfaceDefinition> thisInterfaces, boolean includeThis, Collection<ICompilerProblem> problems)
        {
            for (IInterfaceDefinition iface : thisInterfaces)
            {
                if (includeThis)
                {
                    theInterfaces.add(iface);
                }
                RecursionGuard guard = new RecursionGuard();
                analyze(iface, guard, problems);
            }
            underlyingIterator = theInterfaces.iterator();
        }

        /**
         * Recursively analyzes an interface defintion. After analysis: all base
         * interfaces will be added to this.theInterfaces any loops detected
         * will be added to problems
         */
        private void analyze(IInterfaceDefinition iface, RecursionGuard guard, Collection<ICompilerProblem> problems)
        {
            if (guard.isLoop(iface))
            {
                if (problems != null)
                    problems.add(new CircularTypeReferenceProblem(iface, iface.getBaseName()));
                return;
            }
            IInterfaceDefinition[] parentIFaces = iface.resolveExtendedInterfaces(project);
            for (IInterfaceDefinition parentIFace : parentIFaces)
            {
                // as we recurse, we need to create new recursion guards at each level. Otherwise a
                // "diamond inheritance hierarchy" would generate a false positive.
                // So we make a copy and pass it down to the next level.
                RecursionGuard childGuard = new RecursionGuard(guard);
                analyze(parentIFace, childGuard, problems);
                if (!childGuard.foundLoop)
                {
                    theInterfaces.add(parentIFace);
                }
            }
        }

        @Override
        public boolean hasNext()
        {
            return underlyingIterator.hasNext();
        }

        @Override
        public IInterfaceDefinition next()
        {
            return underlyingIterator.next();
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException(); // as per the Iterator interface spec.
        }
    }

    @Override
    public INamespaceDefinition getProtectedNamespaceReference()
    {
        return null;
    }

    @Override
    public INamespaceDefinition getStaticProtectedNamespaceReference()
    {
        return null;
    }

    /**
     * Get the namespace representing the special interface namespace. This is
     * the namespace that all interface methods go into for this interface.
     * 
     * @return the special namespace for this interface
     */
    public NamespaceDefinition.ILanguageNamespaceDefinition getInterfaceNamespaceReference()
    {
        return this.interfaceNamespace;
    }

    /**
     * Helper method to generate a URI for the interface namespace. This
     * implementation is consistent with the algorithm ASC used, so that we can
     * interoperate.
     * 
     * @return the URI to use for the namespace for this interface
     */
    String generateInterfaceURI()
    {
        String uri;
        String pack = this.getPackageName();
        String shortName = this.getBaseName();

        if (pack != "")
            uri = pack + ":" + shortName;
        else
            uri = shortName;

        return uri;
    }

    /**
     * Generate a namespace set to use for references through a reference with a
     * static type of an interface. For references through an interface, we have
     * to generate a completely different namespace set than what is used for
     * normal references. This is because interface methods really get put into
     * their own special namespace namespace instead of say 'public'. The
     * interface namespace set will consist of the namespace for this interface,
     * plus the namespaces of any interfaces it extends. The interface
     * namespaces are of type CONSTANT_Namespace, and their URI is of the form
     * <package-name>:<interface-name>. So, the URI for
     * flash.events.IEventDispatcher is "flash.events:IEventDispatcher". This is
     * used for code like: var i : flash.events.IEventDispatcher
     * i.addEventListener(...); // The namespace set for addEventListener will
     * be the one generated by this method.
     * 
     * @param project Project to use to resolve base interfaces
     * @return The namespace set to use for member refs through an interface
     * type. The returned set should not be modified.
     */
    public Set<INamespaceDefinition> getInterfaceNamespaceSet(ICompilerProject project)
    {
        // TODO check cache on compiler project to see
        // if we already know the namespace set for this scope.
        // NOTE: Need to use LinkedHashSet here to make the order of the
        // namespace set stable across runs of the compiler.

        return getInterfaceNamespaceSet(project, InterfaceNamespaceSetOptions.INCLUDE_THIS);
    }

    /**
     * Generate a namespace set to use for references through a reference with a
     * static type of an interface. For references through an interface, we have
     * to generate a completely different namespace set than what is used for
     * normal references. This is because interface methods really get put into
     * their own special namespace namespace instead of say 'public'. The
     * interface namespace set will consist of the namespace for this interface,
     * plus the namespaces of any interfaces it extends. The interface
     * namespaces are of type CONSTANT_Namespace, and their URI is of the form
     * <package-name>:<interface-name>. So, the URI for
     * flash.events.IEventDispatcher is "flash.events:IEventDispatcher". This is
     * used for code like: var i : flash.events.IEventDispatcher
     * i.addEventListener(...); // The namespace set for addEventListener will
     * be the one generated by this method.
     *
     * @param project       Project to use to resolve base interfaces
     * @param includeThis   true if the interface namespace for this interface should be included.
     * @return The namespace set to use for member refs through an interface
     * type. The returned set should not be modified.
     */
    Set<INamespaceDefinition> getInterfaceNamespaceSet (ICompilerProject project, InterfaceNamespaceSetOptions includeThis)
    {
        Set<INamespaceDefinition> result = new LinkedHashSet<INamespaceDefinition>();

        Iterator<IInterfaceDefinition> iter = this.interfaceIterator(project, includeThis == InterfaceNamespaceSetOptions.INCLUDE_THIS);

        while (iter.hasNext())
        {
            InterfaceDefinition intf = (InterfaceDefinition)iter.next();
            result.add(intf.getInterfaceNamespaceReference());
        }

        return result;
    }

    /**
     * Enum saying whether we should include the interface namespace for this interface in the interface namespace
     * set
     */
    enum InterfaceNamespaceSetOptions
    {
        INCLUDE_THIS,       // include the namespace for this interface
        DONT_INCLUDE_THIS   // include only the base interface namespaces
    }

    @Override
    public void setNamespaceReference(INamespaceReference value)
    {
        super.setNamespaceReference(value);
        interfaceNamespace = NamespaceDefinition.createInterfaceNamespaceDefinition(this);
    }

    /**
     * Method to find all the methods declared in this interface, and validate
     * that the class definition passed in implements those methods, and that
     * they are implemented with compatible signatures
     * 
     * @param cls the class definition to check
     * @param problems a list of problems to report errors to
     */
    public void validateClassImplementsAllMethods(ICompilerProject project, ClassDefinition cls, Collection<ICompilerProblem> problems)
    {
        // Interface methods must be implemented by public methods
        INamespaceDefinition publicNs = NamespaceDefinition.getPublicNamespaceDefinition();
        ASScope classScope = cls.getContainedScope();

        for (IDefinitionSet defSet : this.getContainedScope().getAllLocalDefinitionSets())
        {
            for (int i = 0, l = defSet.getSize(); i < l; ++i)
            {
                IDefinition def = defSet.getDefinition(i);
                if (def instanceof FunctionDefinition)
                {
                    FunctionDefinition interfMethod = (FunctionDefinition)def;

                    // Skip any implicit methods added for CM compat
                    if (interfMethod.isImplicit())
                        continue;

                    // Skip the constructor method of the interface.
                    if (interfMethod.getBaseName().equals(getBaseName()))
                        continue;

                    IDefinition c = classScope.getQualifiedPropertyFromDef(project,
                                                                                    cls,
                                                                                    interfMethod.getBaseName(),
                                                                                    publicNs,
                                                                                    false);
                    // Match up getters and setters
                    if (interfMethod instanceof SetterDefinition && c instanceof GetterDefinition)
                        c = ((GetterDefinition)c).resolveCorrespondingAccessor(project);
                    else if (interfMethod instanceof GetterDefinition && c instanceof SetterDefinition)
                        c = ((SetterDefinition)c).resolveCorrespondingAccessor(project);

                    String ifaceName = this.getBaseName();
                    if (c instanceof FunctionDefinition)
                    {
                        FunctionDefinition classMethod = (FunctionDefinition)c;
                        if (!classMethod.hasCompatibleSignature(interfMethod, project))
                        {
                            problems.add(new IncompatibleInterfaceMethodProblem(classMethod,
                                    interfMethod.getBaseName(),
                                    ifaceName,
                                    cls.getBaseName()));
                        }

                    }
                    else
                    {

                        // Error, didn't implement the method
                        problems.add(new UnimplementedInterfaceMethodProblem(cls,
                                interfMethod.getBaseName(),
                                ifaceName,
                                cls.getBaseName()));
                    }
                }
            }
        }
    }

    /**
     * For debugging only. Produces a string such as
     * <code>public interface I extends I1, I2</code>.
     */
    @Override
    protected void buildInnerString(StringBuilder sb)
    {
        sb.append(getNamespaceReferenceAsString());
        sb.append(' ');

        sb.append(IASKeywordConstants.INTERFACE);
        sb.append(' ');

        sb.append(getBaseName());

        String[] extendedInterfaces = getExtendedInterfacesAsDisplayStrings();
        int n = extendedInterfaces.length;
        if (n > 0)
        {
            sb.append(' ');
            sb.append(IASKeywordConstants.IMPLEMENTS);
            sb.append(' ');
            for (int i = 0; i < n; i++)
            {
                sb.append(extendedInterfaces[i]);
                if (i < n - 1)
                {
                    sb.append(',');
                    sb.append(' ');
                }
            }
        }
    }

    @Override
    public boolean matches(DefinitionBase node)
    {
        boolean matches = super.matches(node);
        if (!matches)
            return false;

        String[] leftNames = ((InterfaceDefinition)node).getExtendedInterfacesAsDisplayStrings();
        String[] rightNames = getExtendedInterfacesAsDisplayStrings();

        if (leftNames.length != rightNames.length)
        {
            return false;
        }

        HashSet<String> hitTable = new HashSet<String>(Arrays.asList(leftNames));
        for (int i = 0; i < rightNames.length; i++)
        {
            if (!hitTable.contains(rightNames[i]))
                return false;

        }

        return true;
    }
}
