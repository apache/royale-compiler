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

package org.apache.royale.abc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import org.apache.royale.abc.semantics.InstanceInfo;
import org.apache.royale.abc.semantics.Name;

/**
 * ClassDependencySort contains data structures and algorithms to sort a list of
 * classes into dependency order.
 */
public abstract class ClassDependencySort
{
    /**
     * The objects being sorted need to show an InstanceInfo.
     */
    public static interface IInstanceInfoProvider
    {
        public InstanceInfo getInstanceInfo();
    }

    /**
     * Perform a topological sort by inheritance.
     * 
     * @return a Collection of classes, sorted so that classes appear before
     * their superclasses or inherited interfaces.
     * @throws IllegalStateException if there are circular dependencies.
     */
    public static <T extends IInstanceInfoProvider> Collection<T> getSorted(Collection<T> classes)
    {
        ArrayList<T> sortedClasses = new ArrayList<T>();

        //  First establish dependency relationships between classes.
        ArrayList<DependencyTracker<T>> workingSet = new ArrayList<DependencyTracker<T>>();
        for (T raw_x : classes)
        {
            DependencyTracker<T> x = new DependencyTracker<T>(raw_x);
            for (DependencyTracker<T> y : workingSet)
                x.establishDependency(y);
            workingSet.add(x);
        }

        //  Perform a simple set-oriented topological sort:
        //  yield any class that has no dependencies,remove
        //  that class from its dependents' dependencies, 
        //  iterate until done.
        boolean classProcessed;

        do
        {
            classProcessed = false;

            Iterator<DependencyTracker<T>> it = workingSet.iterator();
            while (it.hasNext())
            {
                DependencyTracker<T> n = it.next();

                if (n.dependencies.isEmpty())
                {
                    classProcessed = true;
                    sortedClasses.add(n.payload);

                    //  Remove this dependency from dependents' graphs.
                    for (DependencyTracker<T> dep : n.dependents)
                        dep.dependencies.remove(n);

                    it.remove();
                }
            }
        }
        while (classProcessed);

        if (!workingSet.isEmpty())
            throw new IllegalStateException("cycle in class dependency graph");

        return sortedClasses;
    }

    /**
     * Track the dependency relationships of an object being sorted.
     */
    static class DependencyTracker<T extends IInstanceInfoProvider>
    {
        /**
         * @param payload - the object to be sorted.
         */
        DependencyTracker(T payload)
        {
            this.payload = payload;
        }

        /**
         * The object being sorted.
         */
        T payload;

        /**
         * The set of objects that depend on this.payload
         */
        Set<DependencyTracker<T>> dependents = new HashSet<DependencyTracker<T>>();

        /**
         * The set of objects that this.payload depends upon.
         */
        Set<DependencyTracker<T>> dependencies = new HashSet<DependencyTracker<T>>();

        /**
         * Establish dependency relationships between this node and another.
         */
        void establishDependency(DependencyTracker<T> other)
        {
            InstanceInfo myInstance = this.payload.getInstanceInfo();
            InstanceInfo otherInstance = other.payload.getInstanceInfo();

            if (myInstance.superName != null && myInstance.superName.equals(otherInstance.name))
                this.makeDependentOf(other);
            else if (otherInstance.superName != null && otherInstance.superName.equals(myInstance.name))
                other.makeDependentOf(this);

            for (Name myInterface : myInstance.interfaceNames)
            {
                if (myInterface != null && myInterface.equals(otherInstance.name))
                    this.makeDependentOf(other);
            }

            for (Name otherInterface : otherInstance.interfaceNames)
            {
                if (otherInterface.equals(myInstance.name))
                    other.makeDependentOf(this);
            }

            if (this.dependencies.contains(other) && other.dependencies.contains(this))
            {
                throw new IllegalStateException("simple cycle in dependency graph");
            }
        }

        /**
         * Note that another object is dependent on this.payload.
         * 
         * @param other - the DependencyTracker tracking the other object.
         */
        void makeDependentOf(DependencyTracker<T> other)
        {
            assert other != this;
            this.dependencies.add(other);
            other.dependents.add(this);
        }
    }
}
