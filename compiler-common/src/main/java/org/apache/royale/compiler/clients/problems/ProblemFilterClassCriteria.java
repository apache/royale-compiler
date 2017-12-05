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

package org.apache.royale.compiler.clients.problems;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.royale.compiler.problems.ICompilerProblem;

/**
 *  IFilter problems based on their class; the filter supports
 *  a negative set (don't accept any instances of a given class)
 *  and a positive set (only accept instances of a given class).
 */
public class ProblemFilterClassCriteria implements IProblemFilter
{
    /**
     *  Construct a purely negative filter.
     *  @param classesToFilterOut - the classes (which may be superclasses)
     *    of problems that should be excluded from the filtered set.
     */
    public ProblemFilterClassCriteria(Class<?>... classesToFilterOut)
    {
        this.classesToFilterOut = new HashSet<Class<?>>(Arrays.asList(classesToFilterOut));
        this.classesToRequire = new HashSet<Class<?>>();
    }

    /**
     *  Construct a filter with positive and negative requirements.
     *  @param classesToFilterOut - the classes (which may be superclasses)
     *    of problems that should be excluded from the filtered set.
     *  @param classesToRequire - the classes (which may be superclasses)
     *    that problems in the filtered set must extend/instantiate.
     */
    public ProblemFilterClassCriteria(Set<Class<?>> classesToFilterOut, Set<Class<?>> classesToRequire)
    {
        this.classesToFilterOut = classesToFilterOut;
        this.classesToRequire = classesToRequire;
    }

    /**
     *  Add a (super)class to be rejected.
     *  @param clazz - the class to be rejected.
     */
    public void addRejectedClass(Class<?> clazz)
    {
        assert(!this.classesToRequire.contains(clazz));
        this.classesToFilterOut.add(clazz);
    }

    /**
     *  Remove a (super)class to was have been rejected.
     *  @param clazz - the class that is now allowed.
     */
    public void removeRejectedClass(Class<?> clazz)
    {
        this.classesToFilterOut.remove(clazz);
    }


    /**
     *  Add a (super)class to be required.
     *  @param clazz - the class to be required.
     */
    public void addRequiredClass(Class<?> clazz)
    {
        assert(!this.classesToFilterOut.contains(clazz));
        this.classesToRequire.add(clazz); 
    }

    @Override
    public boolean accept(ICompilerProblem p)
    {
        for ( Class<?> filter: this.classesToFilterOut )
        {
            //  This is equivalent to (p instanceof <filter class>)
            if ( filter.isInstance(p) )
                return false;
        }

        for ( Class<?> filter: this.classesToRequire )
        {
            if ( ! (filter.isInstance(p) ) )
                return false;
        }

        //  Passed all checks, accept it.
        return true;
    }

    /** Classes members of the result set may not be instances of. */
    private Set<Class<?>> classesToFilterOut;
    /** Classes members of the result set must be instances of. */
    private Set<Class<?>> classesToRequire;
}
