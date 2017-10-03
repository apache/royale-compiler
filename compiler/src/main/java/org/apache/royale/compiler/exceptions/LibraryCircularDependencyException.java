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

package org.apache.royale.compiler.exceptions;

import java.util.List;

import org.apache.royale.compiler.internal.projects.LibraryDependencyGraph;
import com.google.common.collect.ImmutableList;

/**
 *  Exception thrown when a {@link LibraryDependencyGraph} is sorted and found
 *  to form a circular dependency. The sorted order of the library is not valid
 *  when a circular dependency exists so this exception is thrown. 
 */
public class LibraryCircularDependencyException extends Exception
{
    private static final long serialVersionUID = -1128789848162235759L;
   
    public LibraryCircularDependencyException(String message, List<String> circularDependency2)
    { 
        this.message = message;
        this.circularDependency = circularDependency2;
    }
    
    private String message;
    
    private List<String> circularDependency;

    @Override
    public String getMessage()
    {
        return message;
    }

    /**
     * Details of the circular dependency.
     * 
     * @return An ordered list of the libraries that caused the circular 
     * dependency.
     */
    public ImmutableList<String> getCircularDependency()
    {
        return ImmutableList.copyOf(circularDependency);
    }
    
}
