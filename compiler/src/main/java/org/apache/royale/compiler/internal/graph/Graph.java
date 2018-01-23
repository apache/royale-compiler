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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link IGraph}.
 * 
 * @param <V> vertex type
 * @param <E> edge type
 */
public class Graph<V, E extends IGraphEdge<V>> implements IGraph<V, E>
{
    private final Map<V, Map<V, E>> vertexToOutgoingEdges;
    private final Map<V, Map<V, E>> vertexToIncomingEdges;
    
    public Graph()
    {
        super();
        vertexToOutgoingEdges = new HashMap<V, Map<V, E>>();
        vertexToIncomingEdges = new HashMap<V, Map<V, E>>();
        assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
    }

    @Override
    public boolean addVertex(V vertex)
    {
        try
           
        {
            assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
            boolean vertexExists = vertexToOutgoingEdges.containsKey(vertex);
            assert vertexExists == vertexToIncomingEdges.containsKey(vertex);
            if (!vertexExists)
            {
                vertexToOutgoingEdges.put(vertex, null);
                vertexToIncomingEdges.put(vertex, null);
                return !vertexExists;
            }
        }
        finally
        {
            assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
        }
        return false;
    }
    
    private void removeVertex(Map<V, Map<V, E>> vertexMap, V vertex)
    {
        Map<V, E> edgeMap = vertexMap.get(vertex);
        if (edgeMap != null)
        {
            Collection<E> outgoingEdges = new ArrayList<E>(edgeMap.values());
            for (E e : outgoingEdges)
                removeEdge(e);
        }
    }
    
    @Override
    public void removeVertex(V vertex)
    {
        assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
        try
        {
            removeVertex(vertexToOutgoingEdges, vertex);
            removeVertex(vertexToIncomingEdges, vertex);
            vertexToOutgoingEdges.remove(vertex);
            vertexToIncomingEdges.remove(vertex);
        }
        finally
        {
            assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
        }
    }

    /**
     * Adds a collection of vertices to the graph.
     * @param vertices Collection of vertices to add.
     */
    public void addVertices(Collection<V> vertices)
    {
        for (V v : vertices)
            addVertex(v);
    }

    private E addEdgeToVertexMap(Map<V, Map<V, E>> vertexMap, V v1, V v2, E edge)
    {
        Map<V, E> edgeMap = vertexMap.get(v1);
        if (edgeMap == null)
        {
            edgeMap = new HashMap<V, E>();
            vertexMap.put(v1, edgeMap);
        }
        return edgeMap.put(v2, edge);
    }

    private E removeEdgeFromVertexMap(Map<V, Map<V, E>> vertexMap, V v1, V v2)
    {
        Map<V, E> edgeMap = vertexMap.get(v1);
        if (edgeMap == null)
            return null;
        return edgeMap.remove(v2);
    }

    @Override
    public E setEdge(E edge)
    {
        try
        {
            assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
            E existingOutgoingEdge = addEdgeToVertexMap(vertexToOutgoingEdges, edge.getFrom(), edge.getTo(), edge);
            E existingIncomingEdge = addEdgeToVertexMap(vertexToIncomingEdges, edge.getTo(), edge.getFrom(), edge);
            assert existingOutgoingEdge == existingIncomingEdge;
            return existingOutgoingEdge;
        }
        finally
        {
            assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
        }
    }

    private Set<E> getEdgeSet(Map<V, Map<V, E>> vertexMap, V v)
    {
        Map<V, E> edgeMap = vertexMap.get(v);
        @SuppressWarnings("unchecked")
        Set<E> result = (Set<E>)Collections.EMPTY_SET;
        if ((edgeMap != null) && (edgeMap.size() > 0))
        {
            Collection<E> edges = edgeMap.values();
            result = new HashSet<E>(edges.size());
            result.addAll(edges);
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Set<E> getOutgoingEdges(V vertex)
    {
        return getEdgeSet(vertexToOutgoingEdges, vertex);
    }

    @Override
    public Set<E> getIncomingEdges(V vertex)
    {
        return getEdgeSet(vertexToIncomingEdges, vertex);
    }

    @Override
    public E removeEdge(E edge)
    {
        try
        {
            assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
            E outgoingRemoved = removeEdgeFromVertexMap(vertexToOutgoingEdges, edge.getFrom(), edge.getTo());
            E incomingRemoved = removeEdgeFromVertexMap(vertexToIncomingEdges, edge.getTo(), edge.getFrom());
            assert outgoingRemoved == incomingRemoved;
            return outgoingRemoved;
        }
        finally
        {
            assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
        }
    }

    @Override
    public Set<V> getVertices()
    {
        assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
        return Collections.unmodifiableSet(vertexToIncomingEdges.keySet());
    }

    @Override
    public E getEdge(V from, V to)
    {
        assert vertexToOutgoingEdges.size() == vertexToIncomingEdges.size();
        Map<V, E> edgeMap = vertexToOutgoingEdges.get(from);
        if (edgeMap != null)
            return edgeMap.get(to);

        return null;
    }
}
