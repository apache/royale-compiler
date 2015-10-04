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

package org.apache.flex.compiler.internal.codegen.js;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.ISetterNode;

/**
 * @author Michael Schmalle
 */
public class JSSessionModel
{
    public static final String CONSTRUCTOR_EMPTY = "emptyConstructor";
    public static final String CONSTRUCTOR_FULL = "fullConstructor";
    public static final String SUPER_FUNCTION_CALL = "replaceSuperFunction";

    private IClassDefinition currentClass;

    public static class PropertyNodes
    {
        public IGetterNode getter;
        public ISetterNode setter;
    }

    private static class Context
    {
    	public LinkedHashMap<String, PropertyNodes> propertyMap;
    	public List<String> interfacePropertyMap;
    	public LinkedHashMap<String, PropertyNodes> staticPropertyMap;
    	public ArrayList<String> bindableVars;
    	public IClassDefinition classDefinition;
    }
    private Stack<Context> stack = new Stack<Context>();
    
    private LinkedHashMap<String, PropertyNodes> propertyMap = new LinkedHashMap<String, PropertyNodes>();

    private List<String> interfacePropertyMap = new ArrayList<String>();

    private LinkedHashMap<String, PropertyNodes> staticPropertyMap = new LinkedHashMap<String, PropertyNodes>();

    private ArrayList<String> bindableVars = new ArrayList<String>();

    private int foreachLoopCount = 0;

    public IClassDefinition getCurrentClass()
    {
        return currentClass;
    }

    public void setCurrentClass(IClassDefinition currentClass)
    {
        this.currentClass = currentClass;
    }

    public void pushClass(IClassDefinition currentClass)
    {
    	Context context = new Context();
    	context.bindableVars = bindableVars;
    	context.interfacePropertyMap = interfacePropertyMap;
    	context.propertyMap = propertyMap;
    	context.staticPropertyMap = staticPropertyMap;
    	context.classDefinition = this.currentClass;
    	stack.push(context);
        this.currentClass = currentClass;
        bindableVars = new ArrayList<String>();
        staticPropertyMap = new LinkedHashMap<String, PropertyNodes>();
        interfacePropertyMap = new ArrayList<String>();
        propertyMap = new LinkedHashMap<String, PropertyNodes>();
    }

    public void popClass()
    {
    	Context context = stack.pop();
    	this.currentClass = context.classDefinition;
    	bindableVars = context.bindableVars;
    	staticPropertyMap = context.staticPropertyMap;
    	propertyMap = context.propertyMap;
    	interfacePropertyMap = context.interfacePropertyMap;
    }
    
    public HashMap<String, PropertyNodes> getPropertyMap()
    {
        return propertyMap;
    }

    public List<String> getInterfacePropertyMap()
    {
        return interfacePropertyMap;
    }

    public HashMap<String, PropertyNodes> getStaticPropertyMap()
    {
        return staticPropertyMap;
    }

    public boolean hasBindableVars()
    {
        return bindableVars.size() > 0;
    }

    public List<String> getBindableVars()
    {
        return bindableVars;
    }

    public final void incForeachLoopCount()
    {
        foreachLoopCount++;
    }

    public String getCurrentForeachName()
    {
        return "foreachiter" + Integer.toString(foreachLoopCount);
    }

}
