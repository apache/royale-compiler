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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.royale.compiler.internal.as.codegen.MXMLClassDirectiveProcessor;
import org.apache.royale.compiler.internal.codegen.databinding.WatcherInfoBase.WatcherType;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.mxml.IMXMLBindingNode;
import org.apache.royale.compiler.tree.mxml.IMXMLDataBindingNode;

/**
 * After all the bindings in an MXML document (class) are analyzed, this
 * database will have all the information required to CG the getters, bindings, and watchers
 * 
 * Note that part of the division of labor is that that the database and the analyzers is uses to get
 * its data know about parse tree nodes, but clients of the database should not need to know about nodes.
 * 
 * Conversely the database does not know anything about ABC, but presumably the client of the database does.
 * 
 * Clients are assumed to use the database as follows:
 *      create a single database for an MXML document (class)
 *      call analyze() on each binding node found in the document.
 *      call finishAnalysis().
 *      
 *      Then call various getXXX functions to retrieve information
 */
public class BindingDatabase
{
    
    public BindingDatabase()
    {
        // Testing only: If someone has registered interest, send them "this"
        if (_diagnosticLogger != null)
        {
            _diagnosticLogger.add(this);
        }
    }

    private String nearestAncestorBindings = null;

    public Boolean getHasAncestorBindings(){
        return nearestAncestorBindings != null;
    }

    public void setNearestAncestorWithBindings(String value){
        nearestAncestorBindings = value;
    }

    public String getNearestAncestorWithBindings(){
        return nearestAncestorBindings;
    }
    
    /************** private data ****************/
     
   private Set<BindingInfo> bindingInfoSet = new TreeSet<BindingInfo>();
   
   /**
    * map that relates one binding index to another
    * If the pair is in this map, then there is a two way binding relationship between them
    * Note that key/value makes no difference... Either side of the relationship can be the Key or the Value,
    * but a pair must only be represented once
    */
   private Map<Integer, Integer> twoWayPairs = null;
   
   // Note that we are using a single WatcherInfo as a holder
   // for all the watchers, which are its children.
   // This isn't ideal, but the alternative had a lot of code duplication,
   // which was worse.
   private WatcherInfoBase watcherList = new WatcherInfoBase(null, null, new ArrayList<String>());
 
   private int numWatchers=-1;
   
   private ASScope scope;
   private boolean analysisFinished = false;
   
   private static List<BindingDatabase> _diagnosticLogger;

   /**
    * test only field. Total number of watcher info's of all types
    */
   public int _watchersCreated;     
   
   /**************************** public functions ******************/
   
   /**
    * Unit tests may use this API to get notified of Database creation
    * Here are the rules:
    *       Don't call this from production code - just for unit testsing
    *       Always call later with null to remove the logger
    */
   public static void _setDiagnosticLogger(List<BindingDatabase> logger)
   {
       assert ((_diagnosticLogger == null) || (logger==null));
       _diagnosticLogger = logger;
   }
  
   /**
    * Will do the binding analysis on a single binding node.
    * May find bindings and watchers already marked for creation by other nodes and re-use them.
    * 
    */
   public BindingInfo analyze(IMXMLDataBindingNode node, Collection<ICompilerProblem> problems, MXMLClassDirectiveProcessor host)
   {
       BindingInfo bindingInfo = BindingAnalyzer.analyze(node, this, problems, this.bindingInfoSet.size(), host);
       this.bindingInfoSet.add(bindingInfo);
       
       // now watchers
       WatcherAnalyzer watcherAnalyzer = new WatcherAnalyzer(this, problems, bindingInfo, host.getProject());
       watcherAnalyzer.analyze();   
       return bindingInfo;
   }

    /**
     * Will do the binding analysis on a single binding node.
     * May find bindings and watchers already marked for creation by other nodes and re-use them.
     *
     */
   
    public void analyzeBindingNode(IMXMLBindingNode node, Collection<ICompilerProblem> problems, MXMLClassDirectiveProcessor host)
    {
        BindingInfo info1 = analyzeBindingNode(node, problems, host, false);
        if (node.getTwoWay())
        {
            // If two way nodex, then analyze again, reversing source and destination roles
            BindingInfo info2 = analyzeBindingNode(node, problems, host, true);
            
            // And note the two bindings are "paired"
            info1.setTwoWayCounterpart( info2.getIndex());
            info2.setTwoWayCounterpart( info1.getIndex());
        }
    }
    private  BindingInfo analyzeBindingNode(IMXMLBindingNode node, Collection<ICompilerProblem> problems, MXMLClassDirectiveProcessor host,  boolean reverseSourceAndDest)
    {
        BindingInfo bindingInfo = BindingAnalyzer.analyze(node, this, problems, this.bindingInfoSet.size(), reverseSourceAndDest, host);
        this.bindingInfoSet.add(bindingInfo);

        // now watchers
        WatcherAnalyzer watcherAnalyzer = new WatcherAnalyzer(this, problems, bindingInfo, host.getProject());
        watcherAnalyzer.analyze();
        
        return bindingInfo;
    }

   /**
    * Must be called once after all the nodes in a document have been analyzed
    */
    public void finishAnalysis()
    {
        assert !analysisFinished;
        assignWatchersToSlots();
        makeTwoWayMap();
        analysisFinished = true;
        assert isValid();
    }

    /**
     * @return information on all the bindings.
     * 
     * Note that the set will be sorted in the order the binding were created,
     * which is their "slot index"
     */
    public Set<BindingInfo> getBindingInfo()
    {
        return bindingInfoSet;
    }
    
    /**
     * returns a map relating BindingInfos that are paired in two way bindings
     * If a binding is returns as a key, it will also not be found as a value - each
     * binding only appears once
     * 
     */
    public Map<Integer, Integer> getTwoWayBindingInfoPairs()
    {
        assert twoWayPairs != null;
        return twoWayPairs;
    }
    
    /**
     * @return information about all the watchers - or null if none
     * 
     * For efficiency reasons, we just return the entry set. We could be nice and turn it into
     * a list, but the overhead isn't worth if for a low level function like this
     */
    public Set< Entry<Object, WatcherInfoBase>> getWatcherChains()
    {
        assert analysisFinished;
        return watcherList.getChildren();
    }
    
    /**
     * 
     * @return total number of watchers required for document
     */
    public int getNumWatchers()
    {
        assert analysisFinished;
        return numWatchers;
    }
    
    /**
     * @return true is class requires a propertyGetter function
     */
    public boolean getRequiresPropertyGetter()
    {
        assert analysisFinished;
        
        boolean ret = false;
        Set<Entry<Object, WatcherInfoBase>> rootWatchers = getWatcherChains();
        if (rootWatchers == null)
            return false;
        for (Entry<Object, WatcherInfoBase> rootWatcherEntry : rootWatchers)
        {
            WatcherInfoBase rootWatcher = rootWatcherEntry.getValue();
            if (rootWatcher.type == WatcherType.PROPERTY)
            {
                ret = true;             // we found a non-static property watcher at the root of a chain
            }
        }
        return ret;
    }
    
    /**
     * returns the containing scope of one of the databinding nodes that have been analyzed
     */
    public ASScope getScope()
    {
       return scope;
    }

    public void setScope(ASScope scope)
    {
        assert(this.scope==null || this.scope == scope);
        this.scope = scope;
    }


    /**
     * Searches for appropriate existing watcher, or creates  new one.
     * 
     * If new one is created, it will be added to the correct place in the watcher chains
     * 
     * @param parent - the parent watcher of the watcher we are interested in, or null for root watchers
     * @param type - type type of watcher we want to find/create
     * @param watcherKey - the object that identifies this watcher (map key). Often it is the definition, but we
     *          don't always have a definition for dynamic properties
     * @param problems - if semantic problems are found, report them here.
     * @param sourceNode - parse node we are basing the watcher on
     * @param eventNames - a list of events that the watcher must listen for
     */
    public WatcherInfoBase getOrCreateWatcher(
            WatcherInfoBase parent,
            WatcherInfoBase.WatcherType type,
            Object watcherKey,
            Collection<ICompilerProblem> problems,
            IASNode sourceNode,
            List<String> eventNames )
    {
        assert watcherKey!= null;      // or is it OK?
        WatcherInfoBase ret = null;
        
        // final watcher will either be under "parent", or will be under the root,
        // depending on args passed in
        WatcherInfoBase newParent = (parent != null) ?
                parent : watcherList;
        
        // look to see if the desired one already exists
        WatcherInfoBase existingChild = newParent.getOrCreateChildren().get(watcherKey);
        if (existingChild != null)
        {
            ret = existingChild;
        }
        else
        {  
            switch (type)
            {
                case PROPERTY:
                    ret = new PropertyWatcherInfo(problems, sourceNode, eventNames);
                    break;
                case STATIC_PROPERTY:
                    ret = new StaticPropertyWatcherInfo(problems, sourceNode, eventNames);
                    break;
                case FUNCTION:
                    ret = new FunctionWatcherInfo(problems, sourceNode, eventNames);
                    break;
                case XML:
                    ret = new XMLWatcherInfo(problems, sourceNode, eventNames);
                    break;
                default:
                    assert false;
                    break;   
            }
            ret.isRoot = (parent == null);
            newParent.children.put(watcherKey, ret);
        }
        
        // make sure if we re-use an existing one that it is the correct type
       assert objIsType(ret, type);       
       return ret;
    }
    
    @SuppressWarnings("incomplete-switch")
	private boolean objIsType(WatcherInfoBase obj, WatcherType type)
    {
        boolean ret = false;
        
        switch (type)
        {
            case PROPERTY: ret = obj instanceof PropertyWatcherInfo; break;
            case STATIC_PROPERTY: ret = obj instanceof StaticPropertyWatcherInfo; break;
            case FUNCTION: ret = obj instanceof FunctionWatcherInfo; break;
            case XML: ret = obj instanceof XMLWatcherInfo; break;
        }
        return ret;
    }

    /**
     * just for debugging
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        if (nearestAncestorBindings != null) {
            sb.append("<ancestor bindings also exist at "+ nearestAncestorBindings+ " >");
        }
        
        if (bindingInfoSet.isEmpty())
        {
            sb.append("<no binding info");
        }
        else
        {
            sb.append("bindingInfo:\n");
            boolean first = true;
            for (BindingInfo info : bindingInfoSet)
            {
                if (!first) sb.append("\n");
                sb.append("   " +  info );
                first = false; 
            }
        }
        
        if (watcherList.getChildren() != null)
        {
            sb.append("\nwatcher chains: ");
            for (Entry<Object, WatcherInfoBase> ent : watcherList.getChildren())
            {
                sb.append("\n" + ent.getValue().dump("   "));
            }
        }
        else 
        {
            sb.append("\n<no watcher chains>");
        }
        return sb.toString();
    }
    
    /*************************** private methods ************************/
    
    private void assignWatchersToSlots()
    {
        int curIndex = -1;
        curIndex = watcherList.assignIndex(curIndex);
        numWatchers = curIndex;
    }
    
    /**
     * Find all the two way binding relationships, and put them into this.twoWayPairs
     */
    private void makeTwoWayMap()
    {
        assert twoWayPairs == null;
        twoWayPairs = new LinkedHashMap<Integer, Integer>();
        
   
        for (BindingInfo bindingInfo : this.getBindingInfo())
        {
            if (bindingInfo.getTwoWayCounterpart() >= 0) // if this one part of a two way expression
            {
                if ((twoWayPairs.get(bindingInfo.getIndex())==null) &&
                    (twoWayPairs.get(bindingInfo.getTwoWayCounterpart()))==null)
                {
                    twoWayPairs.put(bindingInfo.getIndex(), bindingInfo.getTwoWayCounterpart());
                }
            }
        }
    }
    
    // just for debugging
    private boolean isValid()
    {
        int i=0;
        for (BindingInfo info : bindingInfoSet)
        {
            if (info.getIndex() != i) return false;
            ++i;
        }
        return true;
    }
}
