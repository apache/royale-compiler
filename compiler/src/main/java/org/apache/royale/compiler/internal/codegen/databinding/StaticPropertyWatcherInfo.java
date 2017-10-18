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

package org.apache.royale.compiler.internal.codegen.databinding;

import java.util.Collection;
import java.util.List;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IScopedDefinition;
import org.apache.royale.compiler.internal.definitions.DefinitionBase;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.scopes.IASScope;
import org.apache.royale.compiler.tree.as.IASNode;

public class StaticPropertyWatcherInfo extends PropertyWatcherInfo
{
    public StaticPropertyWatcherInfo(Collection<ICompilerProblem> problems, IASNode sourceNode, List<String> eventNames)
    {
        super(problems, sourceNode, eventNames);
        this.type = WatcherType.STATIC_PROPERTY;
    }
    
    /**
     * The class that defines the property we will watch
     */
    private Name containingClass = null;
   
    
    public  Name getContainingClass(ICompilerProject project)
    {
        assert containingClass != null;
        return containingClass;
    }
    
    /**
     * After creating a StaticPropertyWatcherInfo, you should immediately call 
     * init.
     * 
     * This would be done with constructor arguments, but unfortunately the factory that creates us would
     * find this awkward.
     * 
     */
    public void init(IDefinition definition, ICompilerProject project)
    {
       
        
        IASScope scope = definition.getContainingScope();
        IScopedDefinition sd = scope.getDefinition();
        if (sd instanceof DefinitionBase)
        {
            DefinitionBase db = (DefinitionBase)sd;
            containingClass = db.getMName(project);
        }

    }
  
    
}
