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
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.royale.compiler.constants.IASLanguageConstants.BuiltinType;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.definitions.references.INamespaceReference;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.ITypeNode;
import org.apache.royale.utils.FastStack;
import com.google.common.base.Strings;

/**
 * Instances of this class represent definitions of ActionScript types (i.e.,
 * classes and interfaces) in the symbol table.
 * <p>
 * After a type definition is in the symbol table, it should always be accessed
 * through the read-only <code>ITypeDefinition</code>,
 * <code>IClassDefinition</code>, or <code>IInterfaceDefinition</code>
 * interfaces.
 */
public abstract class TypeDefinitionBase extends MemberedDefinition implements ITypeDefinition
{
    /**
     * The ctraits for this type.
     */
    private ClassTraitsDefinition classTraits;

    public TypeDefinitionBase(String name)
    {
        super(name);
        this.classTraits = new ClassTraitsDefinition(this);
    }

    /**
     * Ctor that ClassTraitsDefinition can call to avoid infinite recursion
     */
    protected TypeDefinitionBase(String name, ClassTraitsDefinition ctraits)
    {
        super(name);
        this.classTraits = ctraits;
    }

    @Override
    public boolean isInstanceOf(String qualifiedName, ICompilerProject project)
    {
        ITypeDefinition t = resolveType(qualifiedName, project, null);
        if (t == null)
            return false;

        return isInstanceOf(t, project);
    }

    @Override
    public ITypeNode getNode()
    {
        return (ITypeNode)super.getNode();
    }

    /**
     * If the definition has metadata "__go_to_definition_help", use the
     * annotated location. Otherwise, use the location information stored on the
     * AST node.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int getNameStart()
    {
        final IMetaTag goToDefinitionHelp =
                getMetaTagByName(IMetaTag.GO_TO_DEFINITION_HELP);
        if (goToDefinitionHelp != null)
        {
            final String value = goToDefinitionHelp.getAttributeValue(
                    IMetaTag.GO_TO_DEFINITION_HELP_POS);
            if (Strings.isNullOrEmpty(value))
            {
                return UNKNOWN;
            }
            else
            {
                return Integer.valueOf(value);
            }
        }

        return super.getNameStart();
    }

    @Override
    public String getPackageName()
    {
        // We know that all interface or class definitions are top level
        // definitions so we overload this method to
        // avoid the base class method trying to run up the scope
        // chain to find the toplevel definition.
        INamespaceReference namespaceRef = getNamespaceReference();
        if (namespaceRef instanceof NamespaceDefinition.INamespaceWithPackageName)
            return ((NamespaceDefinition.INamespaceWithPackageName)namespaceRef).getNamespacePackageName();
        return "";
    }

    protected IInterfaceDefinition[] filterNullInterfaces(IInterfaceDefinition[] in)
    {
        ArrayList<IInterfaceDefinition> result = new ArrayList<IInterfaceDefinition>(in.length);
        for (IInterfaceDefinition i : in)
        {
            if (i != null)
                result.add(i);
        }
        return result.toArray(new IInterfaceDefinition[result.size()]);
    }

    @Override
    public Iterable<ITypeDefinition> typeIteratable(final ICompilerProject project, final boolean skipThis)
    {
        final TypeDefinitionBase myThis = this;
        return new Iterable<ITypeDefinition>()
        {

            @Override
            public Iterator<ITypeDefinition> iterator()
            {
                return new TypeIterator(project, myThis, skipThis);
            }

        };
    }

    /**
     * Get an iterator that will iterate over the static inheritance chain. This
     * starts with the class object, and then proceeds to Class, as class
     * objects extend Class, not whatever is specified in the extends clause.
     */
    public Iterable<ITypeDefinition> staticTypeIterable(final ICompilerProject project, final boolean skipThis)
    {
        final TypeDefinitionBase myThis = this;
        return new Iterable<ITypeDefinition>()
        {
            @Override
            public Iterator<ITypeDefinition> iterator()
            {
                return new StaticTypeIterator(project, myThis, skipThis);
            }
        };
    }

    protected static class TypeIterator implements Iterator<ITypeDefinition>
    {
        private static final int INITIAL_STACK_SIZE = 10;

        public TypeIterator(ICompilerProject project, ITypeDefinition initialType, boolean skipInitial)
        {
            this.project = project;
            stack = new FastStack<ITypeDefinition>(INITIAL_STACK_SIZE);
            visited = new HashSet<ITypeDefinition>();
            classesWithInterfaces = new FastStack<IClassDefinition>();
            init(initialType, skipInitial);
        }

        protected final ICompilerProject project;
        protected final FastStack<ITypeDefinition> stack;
        protected final Set<ITypeDefinition> visited;
        /**
         * Keep track of classes that implement interfaces, as we may need to iterate the interfaces
         */
        protected final FastStack<IClassDefinition> classesWithInterfaces;

        @Override
        public boolean hasNext()
        {
            // First walk the classes in the inheritance chain
            if( !stack.isEmpty() )
                return true;

            // after walking the inheritance chain walk the implemented interfaces
            pushInterfaces();

            return !stack.isEmpty();
        }

        @Override
        public ITypeDefinition next()
        {
            if (!hasNext())
                throw new NoSuchElementException();

            ITypeDefinition next = stack.pop();
            visited.add(next);
            pushChildren(next);
            return next;
        }

        /**
         * Push the implemented interfaces of the last class with implemented interfaces
         */
        protected void pushInterfaces()
        {
            boolean pushedInterface = false;
            while(!pushedInterface && !classesWithInterfaces.isEmpty())
            {
                IClassDefinition clazz = classesWithInterfaces.pop();

                IInterfaceDefinition[] interfaces = clazz.resolveImplementedInterfaces(project);
                for (int i = interfaces.length - 1; i >= 0; i--)
                {
                    if (interfaces[i] != null)
                    {
                        pushedInterface = true;
                        push(interfaces[i]);
                    }
                }
            }
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        protected void init(ITypeDefinition initialType, boolean skipThis)
        {
            stack.push(initialType);

            if (skipThis)
                next();
        }

        protected void push(ITypeDefinition type)
        {
            if (!(visited.contains(type)))
                stack.push(type);
        }

        protected void pushChildren(ITypeDefinition type)
        {
            // Note that the children need to be pushed onto the stack reverse
            // order of interfaces first, then the base class last, so the
            // correct order is preserved while iterating through class type classes.
            // The order preservation is required for functions such as TypeScope.getPropertyForMemberAccess()
            // where it is iterating through the extended class, then interfaces looking
            // for members, and it will return once it finds the member it is looking for
            // to do this we push the base class, and add the type to a stack of types with
            // interfaces.  After we have walked all the base classes, we go back to the types
            // with interfaces and then push the interfaces.  This gives us the correct iteration
            // order, while also making sure that we don't resolve the interfaces until we have
            // to - many iterations will stop once they find the property they are looking for,
            // and will never need to look in the interfaces.
            if (type instanceof IClassDefinition)
            {
                IClassDefinition classDef = (IClassDefinition)type;
                if( classDef.getImplementedInterfaceReferences().length > 0 )
                    classesWithInterfaces.push(classDef);

                IClassDefinition baseClass = classDef.resolveBaseClass(project);
                if (baseClass != null)
                    push(baseClass);
            }
            else if (type instanceof IInterfaceDefinition)
            {
                IInterfaceDefinition interfaceDef = (IInterfaceDefinition)type;
                IInterfaceDefinition[] baseInterfaces = interfaceDef.resolveExtendedInterfaces(project);
                for (int i = baseInterfaces.length - 1; i >= 0; i--)
                {
                    if (baseInterfaces[i] != null)
                        push(baseInterfaces[i]);
                }
            }
        }
    }

    /**
     * Iterator to iterate over the ctraits. Class objects extend Class, not
     * whatever is specified in the extends clause (that only applies to the
     * instance objects) class C{} C$ (the class object) extends Class C
     * (instance object) extends Object
     */
    protected static class StaticTypeIterator extends TypeIterator
    {
        public StaticTypeIterator(ICompilerProject project, ITypeDefinition initialType, boolean skipInitial)
        {
            super(project, initialType, skipInitial);
        }

        private ITypeDefinition initialType;

        @Override
        protected void init(ITypeDefinition initialType, boolean skipThis)
        {
            this.initialType = initialType;

            // bottom of the stack should be the Class type
            ITypeDefinition classType = (ITypeDefinition)project.getBuiltinType(BuiltinType.CLASS);
            stack.push(classType);
            // add our initial type onto the stack, unless we're skipping it where we
            // just add it straight to the visited list (see definitions of 'next()' && 'pushChildren()')
            if (skipThis)
            {
                visited.add(initialType);
            }
            else
            {
                stack.push(initialType);
            }
        }

        @Override
        protected void pushChildren(ITypeDefinition type)
        {
            if (type == initialType)
            {
                return;
            }
            super.pushChildren(type);
        }
    }

    /**
     * Resolve the type of this type - will return a Definition representing the
     * ctraits
     */
    @Override
    public TypeDefinitionBase resolveType(ICompilerProject project)
    {
        return classTraits;
    }

    /**
     * Resolve the interfaces that this definition extends (if an interface), or implements (if a class).
     * This is the implementation method that the ASScopeCache can call when there is a cache miss.  Other
     * clients should use the resolveExtendedInterfaces/resolveImplementedInterfaces
     * @param project   the active project
     * @return          an Array of IInterfaceDefinition that this type extends, or implements.
     */
    public abstract IInterfaceDefinition[] resolveInterfacesImpl (ICompilerProject project);
}
