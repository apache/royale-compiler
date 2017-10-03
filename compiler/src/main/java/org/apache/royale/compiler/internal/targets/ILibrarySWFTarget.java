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

package org.apache.royale.compiler.internal.targets;

import org.apache.royale.compiler.targets.ISWFTarget;
import org.apache.royale.compiler.units.ICompilationUnit;
import com.google.common.collect.ImmutableSet;

/**
 * Interface for library swf targets.
 */
public interface ILibrarySWFTarget extends ISWFTarget
{
    /**
     * Get the compilation units included in this SWF. This includes the
     * rootedCompilationUnits plus their dependencies. The compilation units
     * will be ready after a successful call to the build() method.
     * 
     * @return the set of compilation units included in this SWF.
     */
    ImmutableSet<ICompilationUnit> getCompilationUnits();

    /**
     * Get the name of root class.
     * 
     * @return name of the root class or null if the SWF does not
     * have a root class.
     */
    String getRootClassName();
    
    /**
     * Get the qualified name of the base class of the root class.
     * 
     * @return The qualified name of the base class or null if there
     * is no base class.
     */
    String getBaseClassQName();

}
