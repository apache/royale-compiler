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

package org.apache.royale.compiler.internal.scopes;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.internal.common.Counter;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.internal.projects.CompilerProject;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.scopes.IDefinitionSet;
import org.apache.royale.compiler.tree.as.IScopedNode;
import org.apache.royale.compiler.units.ICompilationUnit;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ForwardingCollection;

/**
 * ASScopeBase is the abstract base class for all lexical scopes. It has three
 * concrete subclasses: <code>ASProjectScope</code> for project scopes,
 * <code>ASFileScope</code> for file scopes, and <code>ASScope</code> for class,
 * interface, function, and <code>with</code> scopes.
 * <p>
 * The primary purpose of a lexical scope is to store a set of definitions that
 * are potentially visible if the scope is in the chain of scopes used to
 * resolve an identifier.
 */
public abstract class ASScopeBase implements IASScope
{
    public static final Set<INamespaceDefinition> allNamespacesSet = null;

    /**
     * Used only for debugging, as part of {@link this.toString()}.
     */
    private static void indent(StringBuilder sb, int level)
    {
        // Indent six spaces for each scope-nesting level.
        for (int i = 0; i < level; i++)
        {
            sb.append("      ");
        }
    }

    /**
     * Constructor
     */
    public ASScopeBase()
    {
        // Start out with an empty definiton store until a definition is added.
        definitionStore = EmptyDefinitionStore.SINGLETON;
        
        if (Counter.COUNT_SCOPES)
            countScopes();
    }

    /**
     * Storage for definitions in this scope,
     * organized into sets of definitions with the same base name.
     */
    protected IDefinitionStore definitionStore;

    /**
     * Minimizes the memory used by this scope.
     * <p>
     * The definition store does not get compacted,
     * but subclasses override this to compact other data structures.
     */
    public void compact()
    {
    }

    /**
     * Adds the specified definition to this scope.
     * 
     * @param definition The {@link IDefinition} to be added.
     */
    public void addDefinition(IDefinition definition)
    {
        if (definition == null)
            return;

        addDefinitionToStore(definition);

        if (definition instanceof DefinitionBase)
            ((DefinitionBase)definition).setContainingScope(this);
    }
    
    /**
     * Helper method called by {@link #addDefinition}().
     * <p>
     * It handles actually adding the definition to the store.
     * It first tries to add it to the current store.
     * If it won't fit, it creates a bigger store and adds it to that.
     * 
     * @param definition The {@link IDefinition} to be added.
     */
    protected void addDefinitionToStore(IDefinition definition)
    {
        if (!definitionStore.add(definition))
        {
            definitionStore = definitionStore.createLargerStore();
            definitionStore.add(definition);
        }
    }

    /**
     * Removes the specified definition from this scope.
     * 
     * @param definition The {@link IDefinition} to be removed.
     */
    public void removeDefinition(IDefinition definition)
    {
        removeDefinitionFromStore(definition);
        
        // TODO It seems like a good idea to null out the containing
        // scope of a definition after we remove it from that scope.
        // But this makes various tests fail. 
//        if (definition instanceof DefinitionBase)
//            ((DefinitionBase)definition).setContainingScope(null);
    }

    /**
     * Helper method called by {@link #removeDefinition}().
     * <p>
     * It handles actually removing the definition from the store.
     * It does not bother to check whether the store could be
     * downgraded with one that has a smaller capacity.
     * 
     * @param definition The {@link IDefinition} to be added.
     */
    protected void removeDefinitionFromStore(IDefinition definition)
    {
        definitionStore.remove(definition);
    }

    @Override
    public IScopedNode getScopeNode()
    {
        return null;
    }

    @Override
    public IScopedDefinition getDefinition()
    {
        return null;
    }

    @Override
    public IDefinitionSet getLocalDefinitionSetByName(String baseName)
    {
        return definitionStore.getDefinitionSetByName(baseName);
    }

    @Override
    public Collection<String> getAllLocalNames()
    {
        return definitionStore.getAllNames();
    }
    
    @Override
    public Collection<IDefinitionSet> getAllLocalDefinitionSets()
    {
        return definitionStore.getAllDefinitionSets();
    }

    @Override
    public Collection<IDefinition> getAllLocalDefinitions()
    {
        return definitionStore.getAllDefinitions();
    }

    /**
     * Adds definitions with the specified base name whose namespaces match the
     * specified namespace set to the specified collection of definitions.
     * <p>
     * If more that one definition is added to the collection by this method,
     * then the reference is ambiguous
     * 
     * @param project {@link ICompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param defs Collection that found {@link IDefinition}'s are added to.
     * @param baseName Name of property to find.
     * @param namespaceSet Namespace set in which the qualifier of any matching
     * definition must exist to be considered a match.
     */
    public final void getLocalProperty(ICompilerProject project, Collection<IDefinition> defs, String baseName, Set<INamespaceDefinition> namespaceSet)
    {
        getLocalProperty(project, defs, baseName, namespaceSet, true);
    }

    /**
     * Adds definitions with the specified base name whose namespaces match the
     * specified namespace set to the specified collection of definitions.
     * <p>
     * If more that one definition is added to the collection by this method,
     * then the reference is ambiguous
     * 
     * @param project {@link ICompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param defs Collection that found {@link IDefinition}'s are added to.
     * @param baseName Name of property to find.
     * @param namespaceSet Namespace set in which the qualifier of any matching
     * definition must exist to be considered a match.
     * @param getContingents If true, non-contingent definitions are ignored. If
     * false, contingent definitions are ignored.
     */
    // TODO Remove the override in ASProjectScope 
    // and make this final again when we start using Set<IDefinition>. 
    protected final void getLocalProperty(ICompilerProject project, Collection<IDefinition> defs, String baseName, Set<INamespaceDefinition> namespaceSet, boolean getContingents)
    {
        defs = new FilteredCollection<IDefinition>(new NamespaceSetPredicate(project, namespaceSet), defs);
        getLocalProperty(project, defs, baseName, getContingents);
    }

    /**
     * Adds definitions with the specified base name to the specified collection of definitions.
     * <p>
     * If additional constraints on the definitions are required, then the Collection passed in should
     * implement those.  Most commonly, this will be a {@link FilteredCollection} with a {@link NamespaceSetPredicate}
     * as the predicate.
     * <p>
     * If more that one definition is added to the collection by this method,
     * then the reference is ambiguous
     *
     * @param project {@link ICompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param defs Collection that found {@link IDefinition}'s are added to.
     * @param baseName Name of property to find.
     * @param getContingents If true, non-contingent definitions are ignored. If
     * false, contingent definitions are ignored.
     */
    protected final void getLocalProperty(ICompilerProject project, Collection<IDefinition> defs, String baseName, boolean getContingents)
    {
        IDefinitionSet defSet = getLocalDefinitionSetByName(baseName);
        if (defSet != null)
        {
            int nDefs = defSet.getSize();
            for (int i = 0; i < nDefs; ++i)
            {
                IDefinition definition = defSet.getDefinition(i);
                if ((!definition.isContingent() || (getContingents && isContingentDefinitionNeeded(project, definition))))
                {
                    defs.add(definition);
                }
            }
        }
    }

    /**
     * Check whether there is a matching definition in the class hierarchy
     * already defined in which case the contingent definition is not needed.
     * 
     * @param project The compiler project.
     * @param definition A definition.
     */
    public boolean isContingentDefinitionNeeded(ICompilerProject project, IDefinition definition)
    {
        assert (definition.isContingent()) : "contingentNeeded() called on non-contingent definition";

        IASScope containingScope = definition.getContainingScope();

        // for now contingent definitions are only ever class members, so the containing scope def must
        // be a ClassDefinition.  In future this rule can be changed, but code to search the class
        // hierarchy will become more complex, as the whole definition resolution code needs to be updated
        assert (containingScope.getDefinition() instanceof ClassDefinition) : "contingent definitions containing scope must be a Class";
        ClassDefinition containingType = (ClassDefinition)containingScope.getDefinition();

        String contingentName = definition.getBaseName();
        for (ITypeDefinition type : definition.isStatic() ? containingType.staticTypeIterable(project, false) : containingType.typeIteratable(project, false))
        {
            ASScope typeScope = (ASScope)type.getContainedScope();
            List<IDefinition> defs = new LinkedList<IDefinition>();
            typeScope.getLocalProperty(project, defs, contingentName, null, false);
            // found a non contingent definition, so this contingent def is
            // not needed.
            if (!defs.isEmpty())
                return false;
        }

        return true;
    }

    /**
     * Adds all local definitions from this scope to the specified collections
     * of definitions that have a namespace qualifier in the specified
     * definition set.
     * 
     * @param project {@link CompilerProject} used to resolve reference to
     * definitions outside of the {@link ICompilationUnit} that contains this
     * scope.
     * @param defs Collection that found {@link IDefinition}'s are added to.
     * @param namespaceSet Namespace set in which the qualifier of any matching
     * definition must exist to be considered a match.
     * @param extraNamespace A single extra {@link INamespaceDefinition} that is
     * considered part of the namespace set by this method. This is used when
     * resolving protected definitions in a class scope.
     */
    public void getAllLocalProperties(CompilerProject project, Collection<IDefinition> defs, Set<INamespaceDefinition> namespaceSet, INamespaceDefinition extraNamespace)
    {
        for (IDefinitionSet definitionSet : getAllLocalDefinitionSets())
        {
            for (int i = 0; i < definitionSet.getSize(); ++i)
            {
                // we don't want do add definitions that are promises.
                // when this function is called, any definitions that are still promises
                // are never going to be resolved - they represent bad code
                IDefinition definition = definitionSet.getDefinition(i);
                if (!(definition instanceof ASProjectScope.DefinitionPromise) &&
                    (!definition.isContingent() || isContingentDefinitionNeeded(project, definition)))
                {
                    if ((extraNamespace != null) && (extraNamespace == definition.getNamespaceReference()))
                    {
                        defs.add(definition);
                    }
                    else if (namespaceSet == null)
                    {
                        defs.add(definition);
                    }
                    else
                    {
                        INamespaceDefinition ns = definition.resolveNamespace(project);
                        if (ns != null && (extraNamespace != null) && ((ns == extraNamespace) || (ns.equals(extraNamespace))))
                        {
                            defs.add(definition);
                        }
                        else if (defs != null && namespaceSet.contains(ns))
                        {
                            defs.add(definition);
                        }
                    }
                }
            }
        }
    }

    /**
     * Collection that ignores added items that for which a predicate returns
     * false.
     * 
     * @param <T> Type of items in the collection
     */
    public static final class FilteredCollection<T> extends ForwardingCollection<T>
    {
        /**
         * Constructor
         * 
         * @param predicate Predicate used to filter items as they are added
         * @param storage Collection to which items are added if the predicate
         * returns true.
         */
        public FilteredCollection(Predicate<T> predicate, Collection<T> storage)
        {
            this.predicate = predicate;
            this.storage = storage;
        }

        private final Predicate<T> predicate;
        private final Collection<T> storage;

        @Override
        protected Collection<T> delegate()
        {
            return storage;
        }

        @Override
        public boolean add(T element)
        {
            if (predicate.apply(element))
                return super.add(element);
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends T> collection)
        {
            return super.addAll(Collections2.filter(collection, predicate));
        }

    }

    /**
     * Used only in asserts.
     */
    public boolean verify()
    {
        // Verify each definition in this scope.
        Collection<String> names = getAllLocalNames();
        for (String name : names)
        {
            // Don't call getDefinitionSetByName() on the scope, because
            // for a project scope this would actualize every DefinitionPromise.
            IDefinitionSet definitionSet = definitionStore.getDefinitionSetByName(name);
            int n = definitionSet.getSize();
            for (int i = 0; i < n; i++)
            {
                IDefinition definition = definitionSet.getDefinition(i);
                ((DefinitionBase)definition).verify();
            }
        }

        return true;
    }

    /**
     * For debugging only. This method displays the definitions contained in
     * this scope, alphabetically by name. If the definitions have scopes, those
     * scopes are recursively displayed in an indented fashion.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        buildStringRecursive(sb, 0);
        return sb.toString();
    }

    /**
     * Used only for debugging, as part of {@link this.toString()}.
     */
    private void buildStringRecursive(StringBuilder sb, int level)
    {
        // Get the names of all the definitions in this scope
        // and alphabetize them.
        String[] names = definitionStore.getAllNames().toArray(new String[0]);
        Arrays.sort(names);

        // Display a header identifying the scope.
        indent(sb, level);
        sb.append(toStringHeader());
        sb.append('\n');

        for (String name : names)
        {
            indent(sb, level);
            sb.append(' ');
            sb.append(' ');
            sb.append(name.length() > 0 ? name : "\"\"");
            sb.append('\n');

            // Get the set of definitions with this name.
            // Don't call getDefinitionSetByName() on the scope, because
            // for a project scope this would actualize every DefinitionPromise.
            IDefinitionSet set = definitionStore.getDefinitionSetByName(name);

            int n = set.getSize();
            for (int i = 0; i < n; i++)
            {
                IDefinition d = set.getDefinition(i);
                indent(sb, level);
                sb.append(' ');
                sb.append(' ');
                sb.append(' ');
                sb.append(' ');
                ((DefinitionBase)d).buildString(sb, false);
                sb.append('\n');

                // If the definition has a scope, display that scope recursively.
                if (d instanceof IScopedDefinition)
                {
                    ASScopeBase containedScope = (ASScopeBase)((IScopedDefinition)d).getContainedScope();
                    if (containedScope != null)
                        containedScope.buildStringRecursive(sb, level + 1);
                }
            }
        }
    }

    /**
     * For debugging only. Called by toString() to return the header that is
     * displayed at the beginning.
     */
    protected String toStringHeader()
    {
        return getClass().getSimpleName();
    }
    
    /**
     * Counts various types of scopes that are created,
     * as well as the total number of scopes.
     */
    private void countScopes()
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COUNTER) == CompilerDiagnosticsConstants.COUNTER)
    		System.out.println("ASScopeBase incrementing counter for " + getClass().getSimpleName());
        Counter counter = Counter.getInstance();
        counter.incrementCount(getClass().getSimpleName());
        counter.incrementCount("scopes");
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.COUNTER) == CompilerDiagnosticsConstants.COUNTER)
    		System.out.println("ASScopeBase done incrementing counter for " + getClass().getSimpleName());
    }
}
