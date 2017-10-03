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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.exceptions.CircularDependencyException;
import org.apache.royale.compiler.exceptions.LibraryCircularDependencyException;
import org.apache.royale.compiler.internal.graph.Graph;
import org.apache.royale.compiler.internal.graph.GraphEdge;
import org.apache.royale.compiler.internal.graph.TopologicalSort;
import org.apache.royale.compiler.units.ICompilationUnit;

/**
 * A dependency graph of libraries. Each vertex on the graph represents a 
 * library. Each outgoing edge represents a set of classes that cause the "from"
 * vertex to be dependent on the "to" vertex.
 */
public class LibraryDependencyGraph
{
    /**
     * Class to hold information about an edge in the LibraryDependencyGraph.
     * 
     */
    static final class Edge extends GraphEdge<String> implements Comparable<Edge>
    {
        private Map<String, DependencyTypeSet> dependencies;
        private DependencyTypeSet dependencySet;
        
        /**
         * @param referencingLibrary
         * @param declaringLibrary
         */
        
        private Edge(String referencingLibrary, String declaringLibrary)
        {
            super(referencingLibrary, declaringLibrary);
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
         * Adds a dependency of a set of {@link DependencyType} on a definition 
         * with qname to this Edge.
         * 
         * @param qname The definition qualified name that is depended on
         * @param types {@link DependencyType}'s to add to this edge.
         */
        public void addDependency(String qname, DependencyTypeSet types)
        {
            DependencyTypeSet typeSet = dependencies.get(qname);
            if (typeSet != null)
            {
                DependencyTypeSet newTypeSet = DependencyTypeSet.copyOf(typeSet);
                newTypeSet.addAll(types);
                this.dependencies.put(qname, newTypeSet);
            }
            else
            {
                this.dependencies.put(qname, types);
            }
            dependencySet.addAll(types);
        }
        
        /**
         * Adds an anonymous dependency of a {@link DependencyType} on a definition 
         * to this Edge.
         * @param type {@link DependencyType}'s to add to this edge.
         */
        public void addDependency(DependencyType type)
        {
            dependencySet.add(type);
        }
        
        // Adding toString method for debugging.
        @Override
        public String toString()
        {
            String result = getFrom() + " -> " + getTo() + " [ ";
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
            int fromCompare = getTo().compareTo(edge2.getTo());
            if (fromCompare == 0)
            {
                return getTo().compareTo(edge2.getTo());
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

    /**
     * Create an empty library dependency graph.
     */
    public LibraryDependencyGraph()
    {
        graph = new Graph<String, Edge>();
        lock = new ReentrantReadWriteLock();
    }

    private final Graph<String, Edge> graph;
    private final ReadWriteLock lock;

    /**
     * Adds a dependency to the dependency graph.
     * 
     * @param depender The absolute path name of the library containing a
     * reference to a definition defined by the other library.
     * @param dependee The absolute path name of the library with a definition referred to by
     * the other library.
     */
    public void addDependency(String depender,
                              String dependee,
                              Map<String, DependencyTypeSet> dependencies)
    {
        lock.writeLock().lock();
        try
        {
            Edge e = getEdge(depender, dependee);
            
            for (Map.Entry<String, DependencyTypeSet> entry : dependencies.entrySet())
            {
                e.addDependency(entry.getKey(), entry.getValue());                
            }
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Add a  {@link ICompilationUnit} to the dependency graph.
     */
    public void addLibrary(String library)
    {
        // no need to grab the lock, as this should only ever be called from
        // a single thread at any given time.
        graph.addVertex(library);
    }

    /**
     * Get the edge between two given libraries.
     */
    private Edge getEdge(String dependentLibrary, String dependeeLibrary)
    {
        Edge result = graph.getEdge(dependentLibrary, dependeeLibrary);
        if (result == null)
        {
            result = new Edge(dependentLibrary, dependeeLibrary);
            graph.setEdge(result);
        }
        return result;
    }

    /**
     * @param library An {@link ICompilationUnit} to be checked
     * @return True if the {@link ICompilationUnit} unit exists in this {@link DependencyGraph}
     */
    public boolean contains(String library)
    {
        return graph.getVertices().contains(library);
    }

    /**
     * Finds the named dependencies between two compilation units.
     * 
     * @param from The depender {@link ICompilationUnit}
     * @param to The dependee {@link ICompilationUnit}
     * @return A copied non-synchronous {@link Map} from definition
     * qname to a {@link DependencyTypeSet}, representing the
     * dependencies between two {@link ICompilationUnit}s.
     */
    public Map<String, DependencyTypeSet> getDependencySet(String from, String to)
    {        
        return new HashMap<String, DependencyTypeSet>(getEdge(from, to).getNamedDependencies());
    }
    
    /**
     * Finds the dependencies between two compilation units.
     * 
     * @param from The depender {@link ICompilationUnit}
     * @param to The dependee {@link ICompilationUnit}
     * @return A copy of a {@link DependencyTypeSet} that is active
     * between the two compilation units
     */
    public DependencyTypeSet getDependencyTypes(String from, String to)
    {
        return DependencyTypeSet.copyOf(getEdge(from, to).getAllDependencies());
    }

    public List<String> getDependencyOrder() throws LibraryCircularDependencyException
    {

        // Sort the dependency tree of swcs.
        List<String> sortedSWCs;
        try
        {
            sortedSWCs = TopologicalSort.sort(graph, 
                    new Comparator<String>()
                    {
                        @Override
                        public int compare(String o1, String o2)
                        {
                            return o1.compareTo(o2);
                        }
                        
                    });
        }
        catch (CircularDependencyException e)
        {
            // Rethrow the exception as a LibraryCircularDependencyException.
            List<String> circularDependency = new ArrayList<String>(e.getCircularDependency().size());
            for (Object dependency : e.getCircularDependency())
            {
               circularDependency.add((String)dependency); 
            }
            
            throw new LibraryCircularDependencyException("The libraries contain a circular dependency.",
                    circularDependency);
        }
        
        return sortedSWCs;
    }

    /**
     * Get the set of all libraries a given library is dependent on.
     * 
     * @param libraryPath The absolute path of a library
     * @return A set of libraries that <code>libraryPath</code> depends on.
     * Each {@link String} in the set is the absolute path of a library.
     */
    public Set<String> getDependencies(String libraryPath)
    {
        Set<String> libraries = new HashSet<String>();
        
        // Get the outgoing nodes of libraries.
        Set<Edge> edges = graph.getOutgoingEdges(libraryPath);
        for (Edge edge : edges)
        {
            libraries.add(edge.getTo());
        }
        
        return libraries;
    }
    
}
