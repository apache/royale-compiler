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

package org.apache.royale.compiler.internal.codegen.js;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.tree.as.IFunctionNode;
import org.apache.royale.compiler.tree.as.IGetterNode;
import org.apache.royale.compiler.tree.as.ISetterNode;
import org.apache.royale.compiler.tree.as.IVariableNode;
import org.apache.royale.compiler.tree.metadata.IMetaTagNode;

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
        public ITypeDefinition type;
    }

    public static class BindableVarInfo
    {
        public String namespace;
        public Boolean isStatic;
        public String type;
        public IMetaTagNode[] metaTags;
    }


    public enum ImplicitBindableImplementation {
        NONE,
        EXTENDS,
        IMPLEMENTS
    }

    private static class Context
    {
        public LinkedHashMap<String, PropertyNodes> propertyMap;
        public List<String> interfacePropertyMap;
        public LinkedHashMap<String, PropertyNodes> staticPropertyMap;
        public HashMap<String, BindableVarInfo> bindableVars;
        public ArrayList<IVariableNode> vars;
        public ArrayList<IFunctionNode> methods;
        public IClassDefinition classDefinition;
        public ImplicitBindableImplementation bindableImplementation;
    }
    private Stack<Context> stack = new Stack<Context>();

    public boolean needLanguage = false;
    
    public boolean isExterns = false;

    public boolean inE4xFilter = false;

    public boolean inStaticInitializer = false;
    
    private LinkedHashMap<String, PropertyNodes> propertyMap = new LinkedHashMap<String, PropertyNodes>();

    private List<String> interfacePropertyMap = new ArrayList<String>();

    private LinkedHashMap<String, PropertyNodes> staticPropertyMap = new LinkedHashMap<String, PropertyNodes>();

    private HashMap<String, BindableVarInfo> bindableVars = new HashMap<String, BindableVarInfo>();

    private ArrayList<IVariableNode> vars = new ArrayList<IVariableNode>();

    private ArrayList<IFunctionNode> methods = new ArrayList<IFunctionNode>();

    private HashMap<String, String> internalClasses;

    private int foreachLoopCount = 0;

    private HashMap<IClassDefinition,ImplicitBindableImplementation> implicitBindableImplementations = new HashMap<IClassDefinition, ImplicitBindableImplementation>(100);

    private ImplicitBindableImplementation implicitBindableImplementation = ImplicitBindableImplementation.NONE;

    public IClassDefinition getCurrentClass()
    {
        return currentClass;
    }

    public void setCurrentClass(IClassDefinition currentClass)
    {
        this.currentClass = currentClass;
    }

    public ImplicitBindableImplementation getImplicitBindableImplementation() {
        return implicitBindableImplementation;
    }

    public void registerImplicitBindableImplementation(IClassDefinition classDefinition, ImplicitBindableImplementation type) {
        implicitBindableImplementations.put(classDefinition, type);
    }

    public void unregisterImplicitBindableImplementation(IClassDefinition classDefinition) {
        if (implicitBindableImplementations.keySet().contains(classDefinition)) {
            implicitBindableImplementations.remove(classDefinition);
        }
    }

    public void pushClass(IClassDefinition currentClass)
    {
        Context context = new Context();
        context.classDefinition = this.currentClass;
        context.bindableVars = bindableVars;
        context.interfacePropertyMap = interfacePropertyMap;
        context.propertyMap = propertyMap;
        context.staticPropertyMap = staticPropertyMap;
        context.vars = vars;
        context.methods = methods;
        context.bindableImplementation = implicitBindableImplementation;
        stack.push(context);
        this.currentClass = currentClass;
        bindableVars = new HashMap<String, BindableVarInfo>();
        staticPropertyMap = new LinkedHashMap<String, PropertyNodes>();
        interfacePropertyMap = new ArrayList<String>();
        propertyMap = new LinkedHashMap<String, PropertyNodes>();
        vars = new ArrayList<IVariableNode>();
        methods = new ArrayList<IFunctionNode>();
        implicitBindableImplementation = implicitBindableImplementations.get(currentClass);
        if (implicitBindableImplementation == null)
            implicitBindableImplementation = ImplicitBindableImplementation.NONE;
    }

    public void popClass()
    {
        Context context = stack.pop();
        this.currentClass = context.classDefinition;
        bindableVars = context.bindableVars;
        interfacePropertyMap = context.interfacePropertyMap;
        propertyMap = context.propertyMap;
        staticPropertyMap = context.staticPropertyMap;
        vars = context.vars;
        methods = context.methods;
        implicitBindableImplementation = context.bindableImplementation;
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

    public boolean hasStaticBindableVars()
    {
        for (BindableVarInfo var : bindableVars.values())
        {
            if (var.isStatic) return true;
        }
        return false;
    }

    public HashMap<String, BindableVarInfo> getBindableVars()  { return bindableVars;  }

    public List<IVariableNode> getVars()
    {
        return vars;
    }

    public List<IFunctionNode> getMethods()
    {
        return methods;
    }

    public HashMap<String, String> getInternalClasses()
    {
        if (internalClasses == null)
            internalClasses = new HashMap<String, String>();
        return internalClasses;
    }

    public boolean isInternalClass(String className)
    {
        if (internalClasses == null) return false;

        return internalClasses.containsKey(className);
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
