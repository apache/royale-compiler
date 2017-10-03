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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.royale.compiler.internal.projects.DependencyGraph;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.requests.IABCBytesRequestResult;

/**
 * Helper class to calculate bytes invalidated by changing a {@link ICompilationUnit}
 */
public class InvalidationBytesCalculator
{
    /**
     * @param units A collection of compilation units.
     * @return A {@link Map} from each {@link ICompilationUnit} in units to the number of bytes its ABC bytecode contains
     */
    public static Map<ICompilationUnit, Integer> calculateBytesChanged(Collection<ICompilationUnit> units) throws InterruptedException
    {
        Map<ICompilationUnit, Integer> bytesChanged = new HashMap<ICompilationUnit,Integer>();
        for (ICompilationUnit unit : units)
        {
            assert(unit != null);
            IABCBytesRequestResult res = unit.getABCBytesRequest().get();
            assert(res != null);
            bytesChanged.put(unit, res.getABCBytes().length);
        }
        
        return bytesChanged;
    }
    
    /**
     * Calculates how many bytes needs to be recompiled for each unit in the list of units
     * This will walk through the graph for each unit and add up the number of bytes for 
     * each unique compilation unit that needs to be changed.
     * 
     * @param units The units that we want invalidation byte info for
     * @return A map from each compilation unit in the param units to an {@link Integer} representing
     * how many bytes the change of that unit invalidated
     */
    
    public static Map<ICompilationUnit, Integer> calculateTotalInvalidatedBytesChanged (Collection<ICompilationUnit> units) throws InterruptedException
    {
        Map<ICompilationUnit, Integer> bytesChanged = calculateBytesChanged(units);
        Map<ICompilationUnit, Integer> totalBytesChanged = new HashMap<ICompilationUnit,Integer>();
        ArrayList<ICompilationUnit> singleUnit= new ArrayList<ICompilationUnit>();
        
        singleUnit.add(null);
        for (ICompilationUnit unit : units)
        {
            singleUnit.set(0, unit);
            Set<ICompilationUnit> invalidatedUnits = DependencyGraph.computeInvalidationSet(singleUnit);
            int byteTotal = 0;
            for(ICompilationUnit invalidatedUnit : invalidatedUnits)
            {
                if(!bytesChanged.containsKey(invalidatedUnit))
                {
                    bytesChanged.put(invalidatedUnit, invalidatedUnit.getABCBytesRequest().get().getABCBytes().length);
                }
                byteTotal += bytesChanged.get(invalidatedUnit);
            }
            totalBytesChanged.put(unit, byteTotal);
        }
        return totalBytesChanged;
    }
}
