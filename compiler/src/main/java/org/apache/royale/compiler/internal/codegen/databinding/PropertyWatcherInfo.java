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

import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;

/**
 * Specific watcher info for PropertyWatcher and StaticPropertyWatcher
 */
public class PropertyWatcherInfo extends WatcherInfoBase
{
    /**
     * Constructor
     */
    public PropertyWatcherInfo(Collection<ICompilerProblem> problems, IASNode sourceNode, List<String> eventNames)
    {
        super(problems, sourceNode, eventNames);
        this.type = WatcherType.PROPERTY;
    }
    
    //---------------------- private vars --------------------------------------------

    /**
     * The name of the property we are watching
     */
    protected String propertyName;
  
    //--------------------- public methods ----------------------------
     
    /**
     * After creating a PropertyWatcherInfo, you should immediately call 
     * setPropertyName.
     * 
     * This would be a constructor argument, but unfortunately the factory that creates us would
     * find this awkward.
     * 
     */
    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }
    
    public String getPropertyName()
    {
        assert propertyName != null;
        return propertyName;
    }
   
    /**
     * just for debugging
     */
    @Override
    public String dump(String leadingSpace)
    {
        String ret = leadingSpace + "PropertyWatcher " + 
            "propName=" + getPropertyName() + " " +
            super.dump(leadingSpace);
        
        return ret;
    } 
}
