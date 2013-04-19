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
package org.apache.flex.compiler.internal.projects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.scopes.ASProjectScope.DefinitionPromise;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.units.ICompilationUnit;

import com.google.common.collect.ImmutableSet;

/**
 * @author aharui
 *
 */
public class FlexJSProject extends FlexProject 
{

    /**
     * Constructor
     * 
     * @param workspace The {@code Workspace} containing this project.
     */
    public FlexJSProject(Workspace workspace)
    {
        super(workspace);
    }

    private HashMap<ICompilationUnit, HashMap<String, DependencyType>> requires = new HashMap<ICompilationUnit, HashMap<String, DependencyType>>();
    
    public ICompilationUnit mainCU;
    
    @Override
    public void addDependency(ICompilationUnit from, ICompilationUnit to, DependencyType dt, String qname)
    {
        IDefinition def = to.getDefinitionPromises().get(0);
        IDefinition actualDef = ((DefinitionPromise) def).getActualDefinition();
        boolean isInterface = actualDef instanceof InterfaceDefinition;
        if (!isInterface)
        {
        	if (from != to)
        	{
                HashMap<String, DependencyType> reqs;
            	if (requires.containsKey(from))
            		reqs = requires.get(from);
            	else
            	{
            		reqs = new HashMap<String, DependencyType>();
            		requires.put(from, reqs);
            	}
            	if (reqs.containsKey(qname))
            	{
            	    // inheritance is important so remember it
            	    if (reqs.get(qname) != DependencyType.INHERITANCE)
            	    {
            	        reqs.put(qname, dt);
            	    }
            	}
            	else
            	    reqs.put(qname, dt);
        	}
        }
        super.addDependency(from, to, dt, qname);
    }

    private boolean needToDetermineRequires = true;
    
    // this set is computed from the requires list .  we have to strip out any circularities starting from the mainCU
    private HashMap<ICompilationUnit, ArrayList<String>> googrequires = new HashMap<ICompilationUnit, ArrayList<String>>();
    
    private void determineRequires()
    {
        if (mainCU == null)
            return;
        
        needToDetermineRequires = false;
        List<ICompilationUnit> reachableCompilationUnits = 
            getReachableCompilationUnitsInSWFOrder(ImmutableSet
                .of(mainCU));
        
        HashMap<String, String> already = new HashMap<String, String>();
        
        for (ICompilationUnit cu: reachableCompilationUnits)
        {
            if (requires.containsKey(cu))
            {
                HashMap<String, DependencyType> reqs = requires.get(cu);
                Set<String> it = reqs.keySet();
                ArrayList<String> newreqs = new ArrayList<String>();
                for (String req : it)
                {
                    DependencyType dt = reqs.get(req);
                    if (dt == DependencyType.INHERITANCE)
                        newreqs.add(req);
                    else
                    {
                        if (!already.containsKey(req))
                        {
                            newreqs.add(req);
                            already.put(req, req);
                        }
                    }
                }
                googrequires.put(cu, newreqs);
            }
        }
    }
    
    public ArrayList<String> getRequires(ICompilationUnit from)
    {
        if (needToDetermineRequires)
            determineRequires();
        
    	if (googrequires.containsKey(from))
    		return googrequires.get(from);
    	return null;
    }
    
}
