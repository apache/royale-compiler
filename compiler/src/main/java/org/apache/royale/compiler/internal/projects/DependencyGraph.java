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

package org.apache.royale.compiler.internal.projects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.exceptions.CircularDependencyException;
import org.apache.royale.compiler.internal.graph.Graph;
import org.apache.royale.compiler.internal.graph.GraphEdge;
import org.apache.royale.compiler.internal.graph.TopologicalSort;
import org.apache.royale.compiler.internal.units.CompilationUnitBase;
import org.apache.royale.compiler.internal.units.EmbedCompilationUnit;
import org.apache.royale.compiler.internal.units.InvisibleCompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * Class to track dependencies in a {@link CompilerProject}.
 * <p>
 * There are four types of dependency that may exist from one
 * {@link ICompilationUnit} to another {@link ICompilationUnit}.
 * <dl>
 * <dt>Inheritance Dependency</dt>
 * <dd>If {@link ICompilationUnit} A contains a definition that extends a class
 * or implements an interface defined in {@link ICompilationUnit} B, then there
 * is an inheritance dependency from A to B.</dd>
 * <dt>Signature Dependency</dt>
 * <dd>If any of:
 * <ul>
 * <li>class member variable ( or constant ) type annotation expression</li>
 * <li>class method return type annotation expression</li>
 * <li>class method parameter type annotation expression</li>
 * <li>script variable ( or constant ) type annotation expression</li>
 * <li>script function return type annotation expression</li>
 * <li>script function parameter type annotation expression</li>
 * </ul>
 * in {@link ICompilationUnit} A refers to a definition from
 * {@link ICompilationUnit} B, then there is a signature dependency from A to B.
 * </dd>
 * <dt>Namespace Dependency</dt>
 * <dd>If {@link ICompilationUnit} A depends on a namespace definition from
 * {@link ICompilationUnit} B, then there is a namespace dependency from A to B.
 * </dd>
 * </dl>
 * <dt>Expression Dependency</dt>
 * <dd>If {@link ICompilationUnit} A contains any reference to
 * {@link ICompilationUnit} B that does not result in any other type of
 * dependency, then there exists an expression dependency from A to B.</dd>
 * </dl>
 * <p>
 * The code in this class intends to make adding or updating a dependency edge
 * very fast and thread safe.
 */
public final class DependencyGraph
{
    public static final class Dependency implements Comparable<Dependency>
    {
        public String qname;
        public DependencyType type;
        
        public Dependency(String qname, DependencyType type)
        {
            this.qname = qname;
            this.type = type;
        }

        @Override
        public int compareTo(Dependency o)
        {
            return qname.compareTo(o.qname);
        }
        
        @Override
        public boolean equals(Object o)
        {
            if(o instanceof Dependency)
            {
                Dependency other = (Dependency)o;
                return other.qname.equals(qname);
            }
            else
            {
                return super.equals(o);
            }
        }
        
        @Override
        public int hashCode()
        {
            return qname.hashCode();
        }
    }
    
    /**
     * Class to hold information about an edge in the DependencyGraph.
     */
    static final class Edge extends GraphEdge<ICompilationUnit> implements Comparable<Edge>
    {
        private Map<String, DependencyTypeSet> dependencies;
        private DependencyTypeSet dependencySet;
        
        /**
         * @param referencingCompilationUnit
         * @param declaringCompilationUnit
         */
        
        private Edge(ICompilationUnit referencingCompilationUnit, ICompilationUnit declaringCompilationUnit)
        {
            super(referencingCompilationUnit, declaringCompilationUnit);

            assert (!(referencingCompilationUnit instanceof InvisibleCompilationUnit)) : "InvisibleCompilationUnit should never have an edge";
            assert (!(declaringCompilationUnit instanceof InvisibleCompilationUnit)): "InvisibleCompilationUnit should never have an edge";

            this.dependencySet = DependencyTypeSet.noneOf();
            this.dependencies = new HashMap<String, DependencyTypeSet>();
        }

        /**
         */
        public boolean getIsInheritanceDependency()
        {
            return dependencySet.contains(DependencyType.INHERITANCE);
        }

        /**
         */
        public boolean getIsSignatureDependency()
        {
            return dependencySet.contains(DependencyType.SIGNATURE);
        }

        /**
         */
        public boolean getIsNamespaceDependency()
        {
            return dependencySet.contains(DependencyType.NAMESPACE);
        }

        /**
         */
        public boolean getIsExpressionDependency()
        {
            return dependencySet.contains(DependencyType.EXPRESSION);
        }
        /**
         * @param set A set of dependencies
         * @return True if any of the union of the parameter set and this Edge's dependencySet is non-null.
         * 
         */
        public boolean typeInSet(DependencyTypeSet set)
        {
            for (DependencyType t : set)
            {
                if (dependencySet.contains(t))
                    return true;
            }
            return false;
        }
        
        /**
         * Adds a dependency of a {@link DependencyType} on a definition 
         * with qname to this Edge.
         * 
         * @param qname The definition qualified name that is depended on
         * @param types {@link DependencyType}'s to add to this edge.
         */
        private void addDependency(String qname, DependencyTypeSet types)
        {
            DependencyTypeSet typeSet = dependencies.get(qname);
            if(typeSet != null)
            {
                DependencyTypeSet newTypeSet = DependencyTypeSet.copyOf(typeSet);
                newTypeSet.addAll(types);
                this.dependencies.put(qname, newTypeSet);
            }
            else
            {
                this.dependencies.put(qname, DependencyTypeSet.copyOf(types));
            }
            dependencySet.addAll(types);
        }
        
        /**
         * Adds a dependency of a {@link DependencyType} on a definition 
         * with qname to this Edge.
         * 
         * @param qname The definition qualified name that is depended on
         * @param type {@link DependencyType} to add to this edge.
         */
        private void addDependency(String qname, DependencyType type)
        {
            DependencyTypeSet typeSet = dependencies.get(qname);
            if (typeSet != null)
            {
                DependencyTypeSet newTypeSet = DependencyTypeSet.copyOf(typeSet);
                newTypeSet.add(type);
                this.dependencies.put(qname, newTypeSet);
            }
            else
            {
                this.dependencies.put(qname, DependencyTypeSet.of(type));
            }
            dependencySet.add(type);
        }
        
        /**
         * Adds an anonymous dependency of a {@link DependencyType} on a definition 
         * to this Edge.
         * @param type {@link DependencyType}'s to add to this edge.
         */
        private void addDependency(DependencyType type)
        {
            dependencySet.add(type);
        }
        
        // Adding toString method for debugging.
        @Override
        public String toString()
        {
            String result = getFrom().getName() + " -> " + getTo().getName() + " [ ";
            if (getIsInheritanceDependency())
                result += "inheritance ";
            if (getIsSignatureDependency())
                result += "signature ";
            if (getIsNamespaceDependency())
                result += "namespace ";
            if (getIsExpressionDependency())
                result += "expression ";
            result += "]";
            return result;
        }
        
        /**
         * @return A map of all named dependee qnames of this edge to the
         * {@link DependencyType} that they depend on.
         */

        public Map<String, DependencyTypeSet> getNamedDependencies()
        {
            return this.dependencies;
        }
        
        @Override
        public int compareTo(Edge edge2)
        {
            // First compare the names of the "from" compilation units.
            int fromCompare = getFrom().getName().compareTo(edge2.getFrom().getName());
            if (fromCompare == 0)
            {
                // Then compare the names of the "to" compilation units.
                return getTo().getName().compareTo(edge2.getTo().getName());
            }
            else 
            {
                return fromCompare;
            }
        }
        
        public DependencyTypeSet getAllDependencies()
        {
            return dependencySet;
        }        
    }
    
    private Edge getEdge(ICompilationUnit referencingCompilationUnit, ICompilationUnit declaringCompilationUnit)
    {
        Edge result = graph.getEdge(referencingCompilationUnit, declaringCompilationUnit);
        if (result == null)
        {
            result = new Edge(referencingCompilationUnit, declaringCompilationUnit);
            graph.setEdge(result);
        }
        return result;
    }

    /**
     * Default constructor
     */
    public DependencyGraph()
    {
        graph = new Graph<ICompilationUnit, Edge>();
        lock = new ReentrantReadWriteLock();
    }

    private final Graph<ICompilationUnit, Edge> graph;
    private final ReadWriteLock lock;

    /**
     * Adds a dependency to the dependency graph.
     * 
     * @param depender {@link ICompilationUnit} with a reference to a definition
     * defined by the other {@link ICompilationUnit}.
     * @param dependee {@link ICompilationUnit} with a definition referred to by
     * the other {@link ICompilationUnit}.
     * @param dt The dependency types to add from the "depender" to the "dependee".
     * @param targetQName A qname of the definition of the dependee that the depender depends on
     */
    public void addDependency(ICompilationUnit depender,
                              ICompilationUnit dependee,
                              DependencyTypeSet dt, String targetQName)
    {
        // The compiler does not record self references in the same compilation unit.
        if (depender == dependee)
            return;

        assert !dependee.isInvisible()
            : "invisible units do not export symbols to the project scope, so nothing should depend one them.";

        assert (!(depender instanceof InvisibleCompilationUnit))
            : "depender should only ever be an InvisibleCompilationUnit delegate, never an InvisibleCompilationUnit";

        lock.writeLock().lock();
        try
        {
            Edge e = getEdge(depender, dependee);
            e.addDependency(targetQName, dt);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Adds a dependency to the dependency graph.
     * 
     * @param depender {@link ICompilationUnit} with a reference to a definition
     * defined by the other {@link ICompilationUnit}.
     * @param dependee {@link ICompilationUnit} with a definition referred to by
     * the other {@link ICompilationUnit}.
     * @param dt The type of dependency to add from the "depender" to the "dependee".
     * @param targetQName A qname of the definition of the dependee that the depender depends on
     */
    public void addDependency(ICompilationUnit depender,
                              ICompilationUnit dependee,
                              DependencyType dt, String targetQName)
    {
        // The compiler does not record self references in the same compilation unit.
        if (depender == dependee)
            return;

        assert !dependee.isInvisible()
            : "invisible units do not export symbols to the project scope, so nothing should depend one them.";

        if (depender instanceof InvisibleCompilationUnit)
            depender = ((InvisibleCompilationUnit)depender).getDelegate();

        lock.writeLock().lock();
        try
        {
            Edge e = getEdge(depender, dependee);
            e.addDependency(targetQName, dt);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Adds an anonymous dependency to the dependency graph.
     * 
     * @param depender {@link ICompilationUnit} with a reference to a definition
     * defined by the other {@link ICompilationUnit}.
     * @param dependee {@link ICompilationUnit} with a definition referred to by
     * the other {@link ICompilationUnit}.
     * @param dt The type of dependency to add from the "depender" to the "dependee".
     */
    public void addDependency(ICompilationUnit depender,
                              ICompilationUnit dependee,
                              DependencyType dt)
    {
        // The compiler does not record self references in the same compilation unit.
        if (depender == dependee)
            return;

        assert !dependee.isInvisible()
            : "invisible units do not export symbols to the project scope, so nothing should depend one them.";

        if (depender instanceof InvisibleCompilationUnit)
            depender = ((InvisibleCompilationUnit)depender).getDelegate();

        lock.writeLock().lock();
        try
        {
            Edge e = getEdge(depender, dependee);
            e.addDependency(dt);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Remove all of the outgoing dependencies on a compilation unit.
     * 
     * @param cu The compilation to remove dependencies from.
     */
    public void removeDependencies(ICompilationUnit cu)
    {
        Set<Edge> edges = getOutgoingEdges(cu);
        for (final Edge edge : edges)
        {
            removeDependency(cu, edge.getTo());
        }
    }
    
    /**
     * Remove a dependency in the dependency graph.
     * 
     * @param depender {@link ICompilationUnit} with a reference to a definition
     * defined by the other {@link ICompilationUnit}.
     * @param dependee {@link ICompilationUnit} with a definition referred to by
     * the other {@link ICompilationUnit}.
     */
    private void removeDependency(ICompilationUnit depender,
                                  ICompilationUnit dependee)
    {
        // The compiler does not record self references in the same compilation unit.
        if (depender == dependee)
            return;

        assert !dependee.isInvisible()
            : "invisible units do not export symbols to the project scope, so nothing should depend one them.";

        if (depender instanceof InvisibleCompilationUnit)
            depender = ((InvisibleCompilationUnit)depender).getDelegate();

        lock.writeLock().lock();
        try
        {
            Edge e = getEdge(depender, dependee);
            graph.removeEdge(e);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Computes the list of all {@link ICompilationUnit}'s that the specified
     * collection of {@link ICompilationUnit}'s depends directly or indirectly.
     * {@link ICompilationUnit}'s that contain definitions that extend or
     * implement definitions from other {@link ICompilationUnit}'s will occur in
     * the list after the {@link ICompilationUnit}'s that contains the
     * definitions that are extended or implemented.
     *
     * In cases where {@link ICompilationUnit}s have no real dependencies between
     * them this function employs a lexical sort on the {@link ICompilationUnit}'s
     * name.
     * 
     * @param roots Collection of {@link ICompilationUnit}'s that will be in the
     * returned List along with any {@link ICompilationUnit}'s they depend on
     * directly or indirectly.
     * @return List of all {@link ICompilationUnit}'s that the specified
     * collection of {@link ICompilationUnit}'s depends directly or indirectly.
     */
    public List<ICompilationUnit> topologicalSort(Collection<ICompilationUnit> roots)
    {
        return topologicalSort(roots, new Comparator<ICompilationUnit>()
        {
            @Override
            public int compare(ICompilationUnit o1, ICompilationUnit o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
    
    public CircularDependencyException lastCircularDependencyException;

    /**
     * Computes the list of all {@link ICompilationUnit}'s that the specified
     * collection of {@link ICompilationUnit}'s depends directly or indirectly.
     * {@link ICompilationUnit}'s that contain definitions that extend or
     * implement definitions from other {@link ICompilationUnit}'s will occur in
     * the list after the {@link ICompilationUnit}'s that contains the
     * definitions that are extended or implemented.
     * 
     * @param roots Collection of {@link ICompilationUnit}'s that will be in the
     * returned List along with any {@link ICompilationUnit}'s they depend on
     * directly or indirectly.
     * @param comparator The comparator of last resort used to order
     * {@link ICompilationUnit}s when there are no real dependencies between them.
     * @return List of all {@link ICompilationUnit}'s that the specified
     * collection of {@link ICompilationUnit}'s depends directly or indirectly.
     */
    public List<ICompilationUnit> topologicalSort(Collection<ICompilationUnit> roots, final Comparator<ICompilationUnit> comparator)
    {
        lock.readLock().lock();
        try
        {
        	lastCircularDependencyException = null;
            final ArrayList<ICompilationUnit> sortedList = new ArrayList<ICompilationUnit>(graph.getVertices().size());
            TopologicalSort.IVisitor<ICompilationUnit, Edge> visitor =
                    new TopologicalSort.IVisitor<ICompilationUnit, Edge>()
                {
                    @Override
                    public void visit(ICompilationUnit v)
                    {
                        assert v != null;           // this might mean there was some race condidition...
                        sortedList.add(v);
                    }

                    @Override
                    public boolean isTopologicalEdge(Edge e)
                    {
                        return e.getIsInheritanceDependency();
                    }

                    @Override
                    public int compare(ICompilationUnit a, ICompilationUnit b)
                    {
                        return comparator.compare(a, b);
                    }
                };
            try
            {
                TopologicalSort.sort(graph, roots, visitor);
            }
            catch (CircularDependencyException e1)
            {
    			if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.GOOG_DEPS) == CompilerDiagnosticsConstants.GOOG_DEPS)
    			{
    				System.out.println("Circular Dependency Found");
    				@SuppressWarnings("unchecked")
					ImmutableList<ICompilationUnit> nodes = (ImmutableList<ICompilationUnit>) e1.getCircularDependency();
    				for (ICompilationUnit node : nodes)
    				{
    					try {
							System.out.println(node.getQualifiedNames().toString());
						} catch (InterruptedException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
    				}
    				System.out.println("End of Circular Dependency");
    			}
    			lastCircularDependencyException = e1;
                assert false : "CircularDependencyException";
            }
            return sortedList;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Computes the set of {@link ICompilationUnit}'s that should be cleaned
     * given a collection of {@link ICompilationUnit}'s that are known to need
     * cleaning.
     * <p>
     * This method is not thread safe and must not be called while any
     * compilation is occurring in any project in the workspace associated with
     * the specified compilation units.
     * 
     * @param roots {@link ICompilationUnit}'s that are known to need cleaning
     * @return Set of {@link ICompilationUnit}'s that should be cleaned.
     */
    public static Set<ICompilationUnit> computeInvalidationSet(Iterable<ICompilationUnit> roots)
    {
        
        HashSet<ICompilationUnit> result = new HashSet<ICompilationUnit>();
        LinkedList<Edge> workList = new LinkedList<Edge>();
        for (ICompilationUnit unit : roots)
        {
            assert unit instanceof CompilationUnitBase;
            CompilationUnitBase compilationUnit = (CompilationUnitBase)unit;
            boolean alreadyVisited = !result.add(compilationUnit);
            if (!alreadyVisited)
                workList.addAll(compilationUnit.getProject().getDependencyGraph().getIncomingEdges(compilationUnit));
        }
        DependencyTypeSet recursiveInvalidationSet = DependencyTypeSet.of(DependencyType.INHERITANCE, DependencyType.SIGNATURE, DependencyType.NAMESPACE);
        HashSet<Edge> visitedEdges = new HashSet<Edge>();
        while (!workList.isEmpty())
        {
            Edge currentEdge = workList.pop();
            if (visitedEdges.add(currentEdge))
            {
                assert currentEdge.getFrom() instanceof CompilationUnitBase;
                CompilationUnitBase dependentUnit = (CompilationUnitBase)currentEdge.getFrom();
                result.add(dependentUnit);
                if (currentEdge.typeInSet(recursiveInvalidationSet))
                    workList.addAll(dependentUnit.getProject().getDependencyGraph().getIncomingEdges(dependentUnit));
            }
        }
        return result;
    }

    /**
     * Add a  {@link ICompilationUnit} to the dependency graph.
     * @param cu {@link ICompilationUnit} to add to the dependency graph
     */
    public void addCompilationUnit(ICompilationUnit cu)
    {
        if (cu instanceof InvisibleCompilationUnit)
            cu = ((InvisibleCompilationUnit)cu).getDelegate();

        lock.writeLock().lock();
        try
        {
            graph.addVertex(cu);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Add a Collection of {@link ICompilationUnit}'s to the dependency graph.
     * @param c Collection of {@link ICompilationUnit}'s to add to the dependency graph
     */
    public void addCompilationUnits(Collection<ICompilationUnit> c)
    {
        Collection<ICompilationUnit> transformed = Collections2.transform(c, new Function<ICompilationUnit, ICompilationUnit>() {
            @Override
            public ICompilationUnit apply(ICompilationUnit input)
            {
                if (input instanceof InvisibleCompilationUnit)
                    return ((InvisibleCompilationUnit)input).getDelegate();
                else
                    return input;
            }});

        lock.writeLock().lock();
        try
        {
            graph.addVertices(transformed);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes an {@link ICompilationUnit} from the dependency graph.
     */
    public void removeCompilationUnit(ICompilationUnit cu)
    {
        if (cu instanceof InvisibleCompilationUnit)
            cu = ((InvisibleCompilationUnit)cu).getDelegate();

        lock.writeLock().lock();
        try
        {
            graph.removeVertex(cu);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    Set<Edge> getOutgoingEdges(ICompilationUnit cu)
    {
        if (cu instanceof InvisibleCompilationUnit)
            cu = ((InvisibleCompilationUnit)cu).getDelegate();

        lock.readLock().lock();
        try
        {
            Set<Edge> edges = graph.getOutgoingEdges(cu);
            return edges;
        }
        finally
        {
            lock.readLock().unlock();            
        }
    }

    Set<Edge> getIncomingEdges(ICompilationUnit cu)
    {
        if (cu instanceof InvisibleCompilationUnit)
            cu = ((InvisibleCompilationUnit)cu).getDelegate();

        lock.readLock().lock();
        try
        {
            Set<Edge> edges = graph.getIncomingEdges(cu);
            return edges;
        }
        finally
        {
            lock.readLock().unlock();            
        }
    }

    /**
     * Get the Set of {@link ICompilationUnit}'s depended on directly by the specified {@link ICompilationUnit}.
     * @param cu {@link ICompilationUnit} whose set of direct dependencies will be returned.
     * @return A new Set of {@link ICompilationUnit}'s that the specified {@link ICompilationUnit} directly depends on.
     */
    public Set<ICompilationUnit> getDirectDependencies(ICompilationUnit cu)
    {
        Set<Edge> outgoingEdges = getOutgoingEdges(cu);
        Set<ICompilationUnit> result = new HashSet<ICompilationUnit>(outgoingEdges.size());
        for (Edge e : outgoingEdges)
        {
            result.add(e.getTo());
        }
        return result;
    }

    /**
     * Get the Set of {@link ICompilationUnit}'s that directly depended on the specified {@link ICompilationUnit}.
     * @param cu A compilation unit.
     * @param types Set of dependency types used to filter the returned set of {@link ICompilationUnit}'s.
     * @return A new Set of {@link ICompilationUnit}'s that directly depend on the
     * specified {@link ICompilationUnit}.
     */
    public Set<ICompilationUnit> getDirectReverseDependencies(ICompilationUnit cu, DependencyTypeSet types)
    {
        Set<Edge> incomingEdges = getIncomingEdges(cu);
        Set<ICompilationUnit> result = new HashSet<ICompilationUnit>(incomingEdges.size());
        for (Edge e : incomingEdges)
        {
            if (e.typeInSet(types))
                result.add(e.getFrom());
        }
        return result;
    }

    /**
     * Adds an {@link EmbedCompilationUnit} to the dependency graph.
     * This is just like adding any other compilation unit, except
     * for the fact that we may be in the middle of compiling a project.
     * Thus, when we add an embed compilation unit we grab the dependency graph
     * write lock.
     * @param unit The embed compilation unit to be added.
     */
    public void addEmbedCompilationUnit(EmbedCompilationUnit unit)
    {
        assert !unit.isInvisible();
        lock.writeLock().lock();
        try
        {
            addCompilationUnit(unit);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * @param unit An {@link ICompilationUnit} to be checked
     * @return True if the {@link ICompilationUnit} unit exists in this {@link DependencyGraph}
     */
    public boolean contains(ICompilationUnit unit)
    {
        return graph.getVertices().contains(unit);
    }

    /**
     * Finds the named dependencies between two compilation unit
     * @param from The depender {@link ICompilationUnit}
     * @param to The dependee {@link ICompilationUnit}
     * @return A copied non-synchronous {@link Map} from definition qname {@link String} to a {@link DependencyTypeSet}, 
     * representing the dependencies between two {@link ICompilationUnit}
     */
    public Map<String, DependencyTypeSet> getDependencySet(ICompilationUnit from, ICompilationUnit to)
    {
        assert !to.isInvisible()
            : "invisible compilation units must not be in the dependency graph";
        return new HashMap<String, DependencyTypeSet>(getEdge(from, to).getNamedDependencies());
    }
    
    /**
     * Finds the dependencies between two {@link ICompilationUnit}s
     * @param from The depender {@link ICompilationUnit}
     * @param to The dependee {@link ICompilationUnit}
     * @return A copy of an {@link DependencyTypeSet} that is active between the two compilation units
     */
    public DependencyTypeSet getDependencyTypes(ICompilationUnit from, ICompilationUnit to)
    {
        assert !to.isInvisible()
            : "invisible compilation units must not be in the dependency graph";
        return DependencyTypeSet.copyOf(getEdge(from, to).getAllDependencies());
    }
    
    /**
     * @return An unmodifiable view of all the compilation units in the dependency graph.
     */
    public Collection<ICompilationUnit> getCompilationUnits()
    {
        return graph.getVertices();
    }
    
}
