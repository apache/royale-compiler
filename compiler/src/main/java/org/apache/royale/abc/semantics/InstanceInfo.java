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

package org.apache.royale.abc.semantics;

import static org.apache.royale.abc.ABCConstants.*;

import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Traits;

/**
 * A class' <a href="http://learn.adobe.com/wiki/display/AVM2/4.7+Instance">instance information</a>.
 */
public class InstanceInfo
{
    /**
     * The class' name.
     */
    public Name name;
    
    /**
     * Superclass name.
     */
    public Name superName;
    
    /**
     * Class flags. Values defined in ABCConstants.
     */
    public int flags;
    
    /**
     * The class' protected namespace.
     */
    public Namespace protectedNs;
    
    public Name[] interfaceNames;
    
    /**
     * The class' instance initializer (constructor)
     */
    public MethodInfo iInit;
    
    /**
     * Traits of the class' instance variables and methods.
     */
    public Traits traits;

    public boolean hasProtectedNs()
    {
        return (flags & CONSTANT_ClassProtectedNs) != 0;
    }

    public boolean isInterface()
    {
        return (flags & CONSTANT_ClassInterface) != 0;
    }

    public boolean isFinal()
    {
        return (flags & CONSTANT_ClassFinal) != 0;
    }

    public boolean isSealed()
    {
        return (flags & CONSTANT_ClassSealed) != 0;
    }
}
