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

package org.apache.royale.compiler.internal.projects;

import org.apache.royale.compiler.common.IDefinitionPriority;

/**
 * This class encapsulates information about the priority of definitions defined in a compilation unit.
 * The "priority" of a definition determines whether that definition shadows definitions from other
 * compilation units that have the same qname.
 */
public final class DefinitionPriority implements IDefinitionPriority
{
    public static enum BasePriority
    {
        // decreasing priority
        SOURCE_LIST(-1, "SOURCE_LIST"),
        SOURCE_PATH(-2, "SOURCE_PATH"),
        CROSS_PROJECT_LIBRARY_PATH(-3, "LIBRARY_PATH"),
        LIBRARY_PATH(-4, "LIBRARY_PATH");
        
        BasePriority(int p, String n)
        {
            priority = p;
            name = n;
        }
        
        public int priority;
        public final String name;
    }
    
    public DefinitionPriority(BasePriority basePriority, long timestamp)
    {
        this(basePriority, timestamp, 0);
    }

    public DefinitionPriority(BasePriority basePriority, long timestamp, int order)
    {
        this.basePriority = basePriority;
        this.timestamp = timestamp;
        this.order = order;
    }
    
    public DefinitionPriority(BasePriority basePriority, DefinitionPriority other)
    {
        this.basePriority = basePriority;
        timestamp = other.timestamp;
        order = 0;
    }
    
    private final BasePriority basePriority;
    private long timestamp;
    private int order;      // the lower the order the higher the priority
    
    /**
     * Set the order of the definition based on the path list
     * the definition came from. 
     * The closer a path is to the beginning of the list 
     * the higher its priority.
     * 
     * @param order the order to set
     */
    public void setOrder(int order)
    {
        this.order = order;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }
    
    @Override
    public int compareTo(IDefinitionPriority o)
    {
        DefinitionPriority other = (DefinitionPriority)o;
        int result = basePriority.priority - other.basePriority.priority;
        if (result != 0)
            return result;
        
        if (timestamp < other.timestamp)
            return -1;
        else if (timestamp > other.timestamp)
            return 1;
        else if (order < other.order)
            return 1;
        else if (order > other.order)
            return -1;
        
        return 0;
    }
    
    @Override
    public String toString()
    {
        return basePriority.name + "(" + String.valueOf(timestamp) + "," + 
               String.valueOf(order) + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof DefinitionPriority))
            return false;
        DefinitionPriority other = (DefinitionPriority)obj;
        return (basePriority == other.basePriority) && (timestamp == other.timestamp);
    }
    
    /**
     * Gets the base priority.
     * @return The base priority.
     */
    public BasePriority getBasePriority()
    {
        return basePriority;
    }

}
