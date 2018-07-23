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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;

/**
 * Thread safe implementation of IGraph. While the graph will always be in a
 * consistent state, the graph may change between method calls, so clients
 * running some sort of algorithm over the graph may need to do additional
 * synchronization to make sure their results are consistent.
 */
public class SynchronizedGraph<V, E extends IGraphEdge<V>> extends Graph<V, E>
{

    private final ReadWriteLock lock;

    public SynchronizedGraph()
    {
        super();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public boolean addVertex(V vertex)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
    		System.out.println("SynchronizedGraph.addVertex waiting for lock for " + getClass().getSimpleName());
        lock.writeLock().lock();
        try
        {
            return super.addVertex(vertex);
        }
        finally
        {
            lock.writeLock().unlock();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
        		System.out.println("SynchronizedGraph.addVertex dpne with lock for " + getClass().getSimpleName());
        }
    }
    
    @Override
    public void removeVertex(V vertex)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
    		System.out.println("SynchronizedGraph.removeVertex waiting for lock for " + getClass().getSimpleName());
        lock.writeLock().lock();
        try
        {
            super.removeVertex(vertex);
        }
        finally
        {
            lock.writeLock().unlock();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
        		System.out.println("SynchronizedGraph.removeVertex dpne with lock for " + getClass().getSimpleName());
        }
    }

    public E setEdge(V from, V to, E e)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
    		System.out.println("SynchronizedGraph.setEdge waiting for lock for " + getClass().getSimpleName());
        lock.writeLock().lock();
        try
        {
            return super.setEdge(e);
        }
        finally
        {
            lock.writeLock().unlock();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
        		System.out.println("SynchronizedGraph.setEdge dpne with lock for " + getClass().getSimpleName());
        }
    }
    
    @Override
    public E getEdge(V from, V to)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
    		System.out.println("SynchronizedGraph.getEdge waiting for lock for " + getClass().getSimpleName());
        lock.writeLock().lock();
        try
        {
            return super.getEdge(from, to);
        }
        finally
        {
            lock.writeLock().unlock();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
        		System.out.println("SynchronizedGraph.getEdge dpne with lock for " + getClass().getSimpleName());
        }
    }

    @Override
    public Set<E> getOutgoingEdges(V vertex)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
    		System.out.println("SynchronizedGraph.getOutgoingEdges waiting for lock for " + getClass().getSimpleName());
        lock.readLock().lock();
        try
        {
            Set<E> result = super.getOutgoingEdges(vertex);
            return result == Collections.EMPTY_SET ? result : Collections.unmodifiableSet(new HashSet<E>(result));
        }
        finally
        {
            lock.readLock().unlock();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
        		System.out.println("SynchronizedGraph.getOutgoingEdges dpne with lock for " + getClass().getSimpleName());
        }
    }

    @Override
    public Set<E> getIncomingEdges(V vertex)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
    		System.out.println("SynchronizedGraph.getIncomingEdges waiting for lock for " + getClass().getSimpleName());
        lock.readLock().lock();
        try
        {
            Set<E> result = super.getIncomingEdges(vertex);
            return result == Collections.EMPTY_SET ? result : Collections.unmodifiableSet(new HashSet<E>(result));
        }
        finally
        {
            lock.readLock().unlock();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
        		System.out.println("SynchronizedGraph.getIncomingEdges dpne with lock for " + getClass().getSimpleName());
        }
    }

    @Override
    public E removeEdge(E edge)
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
    		System.out.println("SynchronizedGraph.removeEdge waiting for lock for " + getClass().getSimpleName());
        lock.writeLock().lock();
        try
        {
            return super.removeEdge(edge);
        }
        finally
        {
            lock.writeLock().unlock();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
        		System.out.println("SynchronizedGraph.removeEdge dpne with lock for " + getClass().getSimpleName());
        }
    }

    @Override
    public Set<V> getVertices()
    {
    	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
    		System.out.println("SynchronizedGraph.getVertices waiting for lock for " + getClass().getSimpleName());
        lock.readLock().lock();
        try
        {
            Set<V> result = super.getVertices();
            return Collections.unmodifiableSet(new HashSet<V>(result));
        }
        finally
        {
            lock.readLock().unlock();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH) == CompilerDiagnosticsConstants.SYNCHRONIZED_GRAPH)
        		System.out.println("SynchronizedGraph.getVertices dpne with lock for " + getClass().getSimpleName());
        }
    }
    
    public Lock writeLock()
    {
        return lock.writeLock();
    }
    
    public Lock readLock()
    {
        return lock.readLock();
    }
}
