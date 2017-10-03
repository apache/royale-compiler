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

package org.apache.royale.compiler.internal.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.exceptions.CircularDependencyException;

/**
 * Class with static methods for topologically sorting nodes in an
 * {@link IGraph}.
 */
public final class TopologicalSort
{
    /**
     * IVisitor interface implemented by callers of the
     * {@link TopologicalSort#sort} method.
     * 
     * @param <V> Vertex type parameter of {@link IGraph}
     * @param <E> Edge type parameter of {@link IGraph}
     */
    public interface IVisitor<V, E extends IGraphEdge<V>> extends Comparator<V>
    {
        /**
         * Called by the
         * {@link TopologicalSort#sort(IGraph, Collection, IVisitor)}
         * method for each vertex in topological order.
         * 
         * @param v The vertex that is currently being visited.
         */
        public void visit(V v);

        /**
         * Called by the
         * {@link TopologicalSort#sort(IGraph, Collection, IVisitor)}
         * to determine if an edge is a topological edge.
         * 
         * @param e An edge in the {@link IGraph}.
         * @return true, if the specified edge is a topological edge, otherwise
         * false.
         */
        public boolean isTopologicalEdge(E e);
    }

    @SuppressWarnings("unchecked")
    private static <V, E extends IGraphEdge<V>> void depthFirstTraverse(IGraph<V, E> graph,
            Collection<V> vertices,
            IVisitor<V, E> visitor,
            Set<V> visitedSet,
            Set<V> referencedNonTopologicalVertices,
            Set<V> currentStackVisitedNodes)
            throws CircularDependencyException
    {
        // Sorting vertices so that we can get stable results across different runs
        // of this algorithm.
        Object[] referencedVerticesArr = vertices.toArray();
        Arrays.sort(referencedVerticesArr, (Comparator<Object>)visitor);
        for (Object referencedVertexObj : referencedVerticesArr)
        {
            V referencedVertex = (V)referencedVertexObj;
            currentStackVisitedNodes.add(referencedVertex);
            depthFirstTraverse(graph, referencedVertex, visitor, visitedSet, referencedNonTopologicalVertices, 
                    currentStackVisitedNodes);
            currentStackVisitedNodes.remove(referencedVertex);
        }
    }

    private static <V, E extends IGraphEdge<V>> void depthFirstTraverse(IGraph<V, E> graph,
            V v,
            IVisitor<V, E> visitor,
            Set<V> visitedSet,
            Set<V> referencedNonTopologicalVertices,
            Set<V> currentStackVisitedNodes)
            throws CircularDependencyException
    {
        if (!visitedSet.add(v))
            return;

        Set<E> outgoingEdges = graph.getOutgoingEdges(v);
        ArrayList<V> referencedTopologicalVertices = new ArrayList<V>(outgoingEdges.size());
        for (E e : outgoingEdges)
        {
            V referencedVertex = e.getTo();
            if (visitor.isTopologicalEdge(e))
            {
                if (currentStackVisitedNodes.contains(referencedVertex))
                {
                    // Turn the set into a list so we can append the repeating node.
                    List<V> orderedNodes = new ArrayList<V>(currentStackVisitedNodes.size() + 1);
                    orderedNodes.addAll(currentStackVisitedNodes);
                    orderedNodes.add(referencedVertex);
                    throw new CircularDependencyException(orderedNodes);
                }
                
                referencedTopologicalVertices.add(referencedVertex);
            }
            else
            {
                referencedNonTopologicalVertices.add(referencedVertex);
            }
        }

        // First sort and visit all the vertices pointed at by topological edges
        depthFirstTraverse(graph, referencedTopologicalVertices, visitor, visitedSet, referencedNonTopologicalVertices,
                currentStackVisitedNodes);

        // Second visit the current vertex.
        visitor.visit(v);
        
        referencedNonTopologicalVertices.remove(v);
    }

    /**
     * Topologically sorts nodes in an {@link IGraph}. This method assumes the
     * elimination of all non-topological edges in the graph would make the
     * graph a directed acyclic graph ( DAG ). Topological edges are those edges
     * for which the isTopologicalEdge method of the specified visitor returned
     * true. This method will call the visit method of the specified visitor for
     * each vertex, v, after all vertices pointed at by v with topological edges
     * have been visited. After a vertex, v, has been visited any other vertices
     * pointed at by v with non-topological edges are visited. The specified
     * comparator is used to ensure the order in which vertices are visited is
     * stable for a given topology.
     * <p>
     * The ordering generated by this method will be stable if the ordering of
     * all vertices in the graph using the specified comparator is stable.
     * <p>
     * Callers should detect and eliminate cycles in the graph of vertices
     * connected by topological edges before calling this method. If there are
     * cycles in the graph of vertices connected by topological edges, this
     * method will produce a potentially invalid ordering. Cycles created by
     * edges that are not topological edges are harmless.
     * 
     * @param <V> Vertex type parameter of {@link IGraph}
     * @param <E> Edge type parameter of {@link IGraph}
     * @param graph {@link IGraph} whose vertexes will be topologically sorted.
     * @param rootedVertices Collection of vertices and whose topologically
     * connected vertices should be visited.
     * @param visitor IVisitor whose visit method is called for each vertex in
     * topological ordering.
     */
    public static <V, E extends IGraphEdge<V>> void sort(IGraph<V, E> graph,
         Collection<V> rootedVertices,
         IVisitor<V, E> visitor) 
         throws CircularDependencyException
    {
        Collection<V> verticesToVisit = rootedVertices;
        int totalNumVertices = graph.getVertices().size();
        Set<V> visitedSet = new HashSet<V>(totalNumVertices);
        Set<V> referencedNonTopologicalVertices = new HashSet<V>(totalNumVertices);
        Set<V> currentStackVisitedNodes = new LinkedHashSet<V>();
        do
        {
            depthFirstTraverse(graph, verticesToVisit, visitor, visitedSet, referencedNonTopologicalVertices,
                    currentStackVisitedNodes);
            verticesToVisit = referencedNonTopologicalVertices;
            referencedNonTopologicalVertices = new HashSet<V>();
        }
        while (verticesToVisit.size() > 0);
    }

    /**
     * Convenience wrapper for
     * {@link #sort(IGraph, Collection, IVisitor)} that
     * topologically sorts all vertices in a graph and returns a list of all the
     * vertices in topological order.
     * 
     * @param <V> Vertex type parameter of {@link IGraph}
     * @param <E> Edge type parameter of {@link IGraph}
     * @param graph {@link IGraph} whose vertexes will be topologically sorted.
     * @param vertexComparator Comparator to compare two vertexes, such that the
     * topological order will be stable.
     * @return List of all vertices in the graph in topological order.
     */
    public static <V, E extends IGraphEdge<V>> List<V> sort(final IGraph<V, E> graph, final Comparator<V> vertexComparator) 
            throws CircularDependencyException
    {
        final ArrayList<V> result = new ArrayList<V>(graph.getVertices().size());
        sort(graph, graph.getVertices(), new IVisitor<V, E>()
        {
            @Override
            public void visit(V v)
            {
                result.add(v);
            }

            @Override
            public boolean isTopologicalEdge(E e)
            {
                return true;
            }

            @Override
            public int compare(V a, V b)
            {
                return vertexComparator.compare(a, b);
            }
        });
        return result;
    }
}
