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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.internal.as.codegen.BindableHelper;
import org.apache.royale.compiler.internal.semantics.SemanticUtils;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLDatabindingSourceNotBindableProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;

/**
 * base class for the different watcher info classes
 * Contains all the information needed to CG an mx.binding.Watcher object
 * 
 * There is a one to one relationship between instances of this class, and Flex
 * Watcher objects that will be created at runtime
 * 
 * This class has two purposes:
 *      The main one it to provide a convenient way for the derived classes to inherit functionality
 *      In some cases, also used as a polymorphic case class.
 */
public class WatcherInfoBase
{
    public enum WatcherType { PROPERTY, STATIC_PROPERTY, FUNCTION, XML, ERROR}

    /**
     * 
     * @param problems - problems may be returned here in some cases
     * @param sourceNode - the tree node that the watcher will be watching
     * @param eventNames - the events that are fired when the watched item changes
     */
       
    public WatcherInfoBase(
            Collection<ICompilerProblem> problems,
            IASNode sourceNode,
            List<String> eventNames)
    {
        assert eventNames != null;
        this.eventNames = eventNames;
    }
   
    //------------------------- data fields -------------------------------
    
    /**
     * all of the bindings that wish to listen to this watcher
     */
    protected List<BindingInfo> bindingInfoList = new LinkedList<BindingInfo>();
    
    /**
     * Id of the watcher (index in the _watchers array)
     */
    protected int index = -1;
    protected WatcherType type = WatcherType.ERROR;
    
    /** 
     * the event(s) that the watcher will listen for
     */
    List<String> eventNames;

    /* All watchers are identified by a unique object. This may be anytying,
     * although at this time it is one of:
     *      a) the IDefinition of the thing being watched
     *      b) the IASNode for the thing being watched
     *      c) (in the future)? A unique string
     *      
     * Clinet code  should make no assumptions, however, about the actual types of these objects.
     */
    protected Map<Object, WatcherInfoBase> children = null;

    /**
     * Is this watcher at top of the watcher chain?
     */
    public boolean isRoot = false;

    public WatcherType getType()
    {
        return type;
    }
    
    public void addBinding(BindingInfo bindingInfo)
    {
        bindingInfoList.add(bindingInfo);   
    }
    
    /**
     * 
     * @param i is the index to be assigned to "this"
     * @return the next available index
     */
   
    public int assignIndex(int i)
    {
        this.index = i;
        ++i;
        if (children != null) for (Entry<Object, WatcherInfoBase> ent : children.entrySet())
        {
            i = ent.getValue().assignIndex(i);
        }
        return i;
    }
    

    public int getIndex()
    {
        return index;
    }

    public List<String> getEventNames()
    {
       return eventNames;
    }

    /**
     * get a list of the BindingInfo objects that correspond to  the Binding objects that will
     * be associated with the Watcher
     */
    public List<BindingInfo> getBindings()
    {
        return bindingInfoList;
    }
    
    /**
     * For debug only!
     */
    @Override
    public String toString()
    {
        return dump("");
    }
    
    /**
     * For debug only!
     */
    protected String dump(String leadingSpace)
    {
        String ret = " eventNames";
        for (String s : getEventNames())
        {
            ret += ":" + s;
        }
        ret += " idx=" + index;
        
        boolean firstBinding = true;
        for (BindingInfo b : bindingInfoList)
        {
            if (firstBinding)
                ret += " Binding Index(s): ";
            else 
                ret += ", ";
               
            ret +=  b.index;
            firstBinding = false;
        }
  
        if (children != null)
        {
            leadingSpace += "   ";
            for (Entry<Object, WatcherInfoBase> ent : children.entrySet())
            {
                ret += "\n   " + ent.getValue().dump(leadingSpace);
            }
        }
     
        return ret;
    }
    
    static List<String> getEventNamesFromDefinition(IDefinition def, Collection<ICompilerProblem> problems, IIdentifierNode sourceNode, ICompilerProject project)
    {
        List<String> ret = new LinkedList<String>();
        
        assert ! (def instanceof IConstantDefinition);  // don't call with constants, we don't know what to do with them
    
        Collection<IDefinition> defs;
        
        IDefinition parent = def.getParent();
        if (parent instanceof IClassDefinition)
        {
            defs = new ArrayList<IDefinition>();
            while (parent != null)
            {
                Collection<IDefinition> moredefs = SemanticUtils.getPropertiesByNameForMemberAccess(
                                                                    ((IClassDefinition)parent).getContainedScope(),
                                                                    def.getBaseName(), project);
                if (moredefs != null)
                {
                    defs.addAll(moredefs);
                }
                parent = ((IClassDefinition)parent).resolveBaseClass(project);
            }
        }
        else
        {
            defs = SemanticUtils.getPropertiesByNameForMemberAccess(((IdentifierNode)sourceNode).getASScope(), 
                    def.getBaseName(), project);
            if (defs.size() == 0)
                defs.add(def);
        }
        
        boolean wasBindable = false;
        for (IDefinition d : defs)
        {
            if (d.isBindable())
            {
                wasBindable = true;
                List<String> names = d.getBindableEventNames();
                if (names.isEmpty())
                {
                    if (!ret.contains(BindableHelper.PROPERTY_CHANGE))
                        ret.add(BindableHelper.PROPERTY_CHANGE);       // TODO: should this be on the tag?
                                                                       // this is logged as CMP-1169
                }
                else
                {
                    for (String name : names)
                    {
                        if (!ret.contains(name))
                            ret.add(name);
                    }
                }
            }
        }
        
        if (!wasBindable)
        {
            String s = def.getBaseName();
            problems.add( new MXMLDatabindingSourceNotBindableProblem(sourceNode, s));
        }
        
        assert noDupes(ret);
       
        return ret;
    }
    
    //------------------ private methods -------------------------------------------
  
    /**
     * Debug only function to do "sanity check" of event names
     */
    private static boolean noDupes(List<String> events)
    {
        HashSet<String> temp = new HashSet<String>(events);
        return temp.size() == events.size();
    }

    /**
     * 
     * @return child list, or null if none created
     */
    public Set< Entry<Object, WatcherInfoBase>> getChildren()
    {
        if (children == null) return null;
   
        return children.entrySet();
    }
    
    /**
     * @return the child list. May create it if not created yet
     */
    protected Map<Object, WatcherInfoBase> getOrCreateChildren()
    {
        // delay instantiate the child list
        if (children == null)
            children =  new LinkedHashMap<Object, WatcherInfoBase>();
        return children;
    }
 
}
