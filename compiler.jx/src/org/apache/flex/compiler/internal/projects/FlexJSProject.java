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

import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.scopes.ASProjectScope.DefinitionPromise;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.units.ICompilationUnit;

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

    private HashMap<ICompilationUnit, ArrayList<String>> requires = new HashMap<ICompilationUnit, ArrayList<String>>();
    public HashMap<String, ICompilationUnit> alreadyRequired = new HashMap<String, ICompilationUnit>();
    
    @Override
    public void addDependency(ICompilationUnit from, ICompilationUnit to, DependencyType dt, String qname)
    {
        IDefinition def = to.getDefinitionPromises().get(0);
        IDefinition actualDef = ((DefinitionPromise) def).getActualDefinition();
        boolean isInterface = actualDef instanceof InterfaceDefinition;
        if (isInterface)
        {
            //System.out.println("Interface: " + qname);
        }
        else
        {
            
        	ArrayList<String> reqs;
        	if (requires.containsKey(from))
        		reqs = requires.get(from);
        	else
        	{
        		reqs = new ArrayList<String>();
        		requires.put(from, reqs);
        	}
        	// if the class is already required by some other class
        	// don't add it.  Otherwise we can get circular
        	// dependencies.
        	boolean circular = (from == to);
        	if (requires.containsKey(to))
        	{
        		if (alreadyRequired.containsKey(qname))
        			circular = true;
        	}
        	if (!circular || dt == DependencyType.INHERITANCE)
        	{
        		reqs.add(qname);
        		alreadyRequired.put(qname, from);
        	}
        }
        super.addDependency(from, to, dt, qname);
    }
    
    public ArrayList<String> getRequires(ICompilationUnit from)
    {
    	if (requires.containsKey(from))
    		return requires.get(from);
    	return null;
    }
}
