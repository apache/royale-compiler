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
import java.util.Set;

import org.apache.flex.compiler.common.DependencyType;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.internal.codegen.mxml.flexjs.MXMLFlexJSEmitterTokens;
import org.apache.flex.compiler.internal.css.codegen.CSSCompilationSession;
import org.apache.flex.compiler.internal.definitions.InterfaceDefinition;
import org.apache.flex.compiler.internal.driver.js.flexjs.JSCSSCompilationSession;
import org.apache.flex.compiler.internal.scopes.ASProjectScope.DefinitionPromise;
import org.apache.flex.compiler.internal.tree.mxml.MXMLClassDefinitionNode;
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
        MXMLClassDefinitionNode.GENERATED_ID_BASE = MXMLFlexJSEmitterTokens.ID_PREFIX.getToken();
    }

    private HashMap<ICompilationUnit, HashMap<String, String>> interfaces = new HashMap<ICompilationUnit, HashMap<String, String>>();
    private HashMap<ICompilationUnit, HashMap<String, DependencyType>> requires = new HashMap<ICompilationUnit, HashMap<String, DependencyType>>();

    public ICompilationUnit mainCU;
    public String cssDocument;
    public String cssEncoding;

    @Override
    public void addDependency(ICompilationUnit from, ICompilationUnit to,
            DependencyType dt, String qname)
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
        else
        {
            if (from != to)
            {
                HashMap<String, String> interfacesArr;
                
                if (interfaces.containsKey(from))
                {
                    interfacesArr = interfaces.get(from);
                }
                else
                {
                    interfacesArr = new HashMap<String, String>();
                    interfaces.put(from, interfacesArr);
                }
                
                if (!interfacesArr.containsKey(qname))
                {
                    interfacesArr.put(qname, qname);
                }
            }
        }
        
        super.addDependency(from, to, dt, qname);
    }

    public ArrayList<String> getInterfaces(ICompilationUnit from)
    {
        if (interfaces.containsKey(from))
        {
            HashMap<String, String> map = interfaces.get(from);
            ArrayList<String> arr = new ArrayList<String>();
            Set<String> cus = map.keySet();
            for (String s : cus)
                arr.add(s);
            return arr;
        }
        return null;
    }

    public ArrayList<String> getRequires(ICompilationUnit from)
    {
        if (requires.containsKey(from))
        {
            HashMap<String, DependencyType> map = requires.get(from);
            ArrayList<String> arr = new ArrayList<String>();
            Set<String> cus = map.keySet();
            for (String s : cus)
                arr.add(s);
            return arr;
        }
        return null;
    }

    @Override
    public CSSCompilationSession getCSSCompilationSession()
    {
        return new JSCSSCompilationSession();
    }

}
