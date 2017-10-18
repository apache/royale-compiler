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

import java.util.Set;

/**
 * Interface to a generic graph. Vertices can be any old type of object. Edge's
 * must implement {@link IGraphEdge}. There may only be a single directional
 * edge from any vertex to any other vertex. For example, there can be an edge
 * from A to B and another edge from B to A. However it is impossible to store a
 * second edge from A to B.
 * 
 * @param <V> Type of vertices in the graph.
 * @param <E> Type of edges in the graph
 */
public interface IGraph<V, E extends IGraphEdge<V>>
{
    /**
     * Adds a vertex to the graph.
     * 
     * @param vertex Vertex to add
     * @return true if vertex was added, and false if it already exists
     */
    boolean addVertex(V vertex);
    
    /**
     * Removes a vertex from the graph
     * 
     * @param vertex Vertex to remove.
     */
    void removeVertex(V vertex);

    /**
     * Set the edge between the vertex returned from
     * {@link IGraphEdge#getFrom()} and the vertex returned from
     * {@link IGraphEdge#getTo()}. The the graph already contained an edge
     * between those two vertices that edge is replaced with the specified edge
     * and returned from this method.
     * 
     * @param edge The new edge to add to the graph.
     * @return null or the previous edge to connect the two vertices connected
     * by the specified edge.
     */
    E setEdge(E edge);

    /**
     * Gets the edge in the graph that connects the two specified vertices.
     * 
     * @param from
     * @param to
     * @return null or the edge in the graph that connects to two specified
     * vertices.
     */
    E getEdge(V from, V to);

    /**
     * Finds all the edges emanating from the specified vertex.
     * 
     * @return Set of all edges emanating from the specified vertex.
     */
    Set<E> getOutgoingEdges(final V vertex);

    /**
     * Finds all the edges terminating at the specified vertex.
     * 
     * @return Set of all edges terminating at the specified vertex.
     */
    Set<E> getIncomingEdges(final V vertex);

    /**
     * Removes the specified edge or the edge in the graph that conects the same
     * two vertices as the specified edge from the graph.
     * 
     * @param edge Edge to remove from the graph.
     * @return The edge removed from the graph.
     */
    E removeEdge(final E edge);

    /**
     * Gets the set of all vertices in the graph.
     * 
     * @return Set of all vertices in the graph.
     */
    Set<V> getVertices();
}
