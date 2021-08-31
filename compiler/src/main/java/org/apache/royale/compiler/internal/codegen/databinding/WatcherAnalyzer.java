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
import java.util.Collections;
import java.util.List;

import org.apache.royale.compiler.constants.IASKeywordConstants;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IConstantDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.IFunctionDefinition;
import org.apache.royale.compiler.definitions.IInterfaceDefinition;
import org.apache.royale.compiler.definitions.ITypeDefinition;
import org.apache.royale.compiler.definitions.IVariableDefinition;
import org.apache.royale.compiler.internal.codegen.databinding.WatcherInfoBase.WatcherType;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.tree.as.FunctionCallNode;
import org.apache.royale.compiler.internal.tree.as.IdentifierNode;
import org.apache.royale.compiler.internal.tree.as.MemberAccessExpressionNode;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.compiler.problems.MXMLDatabindingSourceNotBindableProblem;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.ASTNodeID;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;
import org.apache.royale.compiler.tree.as.IFunctionCallNode;
import org.apache.royale.compiler.tree.as.IIdentifierNode;
import org.apache.royale.compiler.tree.as.IMemberAccessExpressionNode;


/**
 * Analyzes a node that represents a binding expression, an generates all the
 * WatcherInfo objects needed to describe the mx.internal.binding.Watchers we will code gen.
 */
public class WatcherAnalyzer
{
    /**
     * Constructor
     * 
     * @param bindingDataBase - is where the results of the analysis are stored
     * @param problems - is where any semantic problems discuvered during analysis are reported
     * @param bindingInfo - is an object created by the BindingAnalyzer to represing the mx.internal.binding.Binding object that will control
     *          this binding expression
     */
    public WatcherAnalyzer(
            BindingDatabase bindingDataBase,
            Collection<ICompilerProblem> problems,
            BindingInfo bindingInfo,
            ICompilerProject project)
    {
        this.bindingDataBase = bindingDataBase;
        this.problems = problems;
        this.bindingInfo = bindingInfo;
        this.project = project;
    }
    
    //------------------- private state -----------------------------
  
    private final BindingDatabase bindingDataBase;
    private final Collection<ICompilerProblem> problems;
    private final BindingInfo bindingInfo;
    private final ICompilerProject project;

    //------------------------ public functions ----------------------
    
    /**
     * structure to holder onto temporary information is we do a
     *  recursive descent parse of the binding expression node
     */
    static class AnalysisState
    {
        /**
         * If true, we we will chain watchers as we find new variables. ex: {a.b}
         * If false, we will generate independent watchers ex: { foo(a, b) }
         */
        public boolean chaining = false;
        
        /**
         *  as we are building up a chain of dependent property watchers, curChain
         *  points to the lowest "link" that was build. Will be null if we are not yet building 
         *  up a chain.
         *  
         *  It is of the specific type "PropertyWatcherInfo", because that is the only
         *  info class that knows how to chain.
         */
        public WatcherInfoBase curChain = null;
        
        /**
         * While we are parsing a function call node, this will
         * hold the definition for it's name
         */
        public IDefinition functionCallNameDefintion = null;
        
        /**
         * If we are parsing an expression like model.a.b.c.d, 
         *  where "Model" is an ObjectProxy or Model tag
         */
        public boolean isObjectProxyExpression = false;
        
        /**
         * the event name(s) that the ObjectProxy/Model dispatches.
         * We need to pass this back up the parse chain so that the subsequent 
         * property watchers know the event name(s)
         */
        List<String> objectProxyEventNames = null;
    }
    
    public void analyze()
    {
        // When there are multiple expression (concatenating case), then we can
        // just analyze each one independently
         for (IExpressionNode expression : bindingInfo.getExpressionNodesForGetter())
        {
             doAnalyze(expression, new AnalysisState());                  // recursively analyze the sub-tree.   
        }
    }
    
    private  void doAnalyze(IASNode node, AnalysisState state)
    {
        ASTNodeID id = node.getNodeID();
        switch(id)
        {
            case IdentifierID:
                analyzeIdentifierNode((IIdentifierNode)node, state);
                break;
            case MemberAccessExpressionID:
                andalyzeMemberAccessExpression( (IMemberAccessExpressionNode) node, state);
                break;
            case FunctionCallID:
                analyzeFunctionCallNode((IFunctionCallNode) node, state);
                break;
            default:
                // For other kinds of nodes, just recurse down looking for something to do
               analyzeChildren(node, state);
                break;
        } 
    }

    private void analyzeChildren(IASNode node, AnalysisState state)
    {
        // For other kinds of nodes, just recurse down looking for something to do
        for (int i=0; i<node.getChildCount(); ++i)
        {
            IASNode ch = node.getChild(i);
            doAnalyze(ch, state);
        }
    }
    
    
    private void analyzeFunctionCallNode(IFunctionCallNode node, AnalysisState state)
    {
        
        assert state.functionCallNameDefintion == null;     // we can't nest (yet?)
        
        // First, let's get the node the represents the function name.
        // That's the one that will have the binding metadata
        IExpressionNode nameNode = node.getNameNode();
        
        IDefinition nameDef = nameNode.resolve(project);
        
        // we ignore non-bindable functions
        // we also ignore functions that don't resolve - they are dynamic, and hence
        // not watchable
        if (nameDef!=null && nameDef.isBindable())
        {
            state.functionCallNameDefintion = nameDef;
        }
        
        analyzeChildren(node, state);           // continue down looking for watchers.
        
        state.functionCallNameDefintion = null; // now we are done with the function call node
      
    }
   
    private void andalyzeMemberAccessExpression(IMemberAccessExpressionNode node, AnalysisState state)
    {
        final boolean wasChaining = state.chaining;

        
        state.chaining = true;      // member access expressions require chained watcher.
                                    // ex: {a.b} - the watcher for b will be a child of a
        final IExpressionNode left = node.getLeftOperandNode();
        final IExpressionNode right = node.getRightOperandNode();
  
        final IDefinition leftDef = left.resolve(project);
        final IDefinition rightDef = right.resolve(project);
        if (leftDef instanceof IClassDefinition)
        {
            // In this case, is foo.prop, where "foo" is a class name.
            // We can skip over the left side ("foo"), because when we
            // analyze the right side we will still know what it is.
            doAnalyze(right, state); 
        }
        else
        {
            if ((rightDef == null) && (leftDef != null))
            {
                // maybe we are something like ObjectProxy.dynamicProperty
                // (someone who extends ObjectProxy, like Model)
                ITypeDefinition leftType= leftDef.resolveType(project);  
                RoyaleProject project = (RoyaleProject)this.project;
                String objectProxyClass = project.getObjectProxyClass();
                boolean isObjectProxy = leftType==null ? false : leftType.isInstanceOf(objectProxyClass, project);
    
                // If we are proxy.prop, we set this info into the parse state. This does two things:
                //      1) tells downstream properties that they can be dynamic, and hence don't need
                //          to be resolvable.
                //      2) Stores off the event names from the proxy so that the chained property watchers
                //          can use the info
                if (isObjectProxy)
                {
                    state.isObjectProxyExpression = true;
                    state.objectProxyEventNames = leftType.getBindableEventNames();
                }
                else
                {
                    String proxyClass = project.getProxyBaseClass();
                    boolean isProxy = leftType==null ? false : leftType.isInstanceOf(proxyClass, project);
                    if (isProxy)
                    {
                        state.isObjectProxyExpression = true;
                        state.objectProxyEventNames = leftType.getBindableEventNames();
                    }
                }
            }
            doAnalyze(left, state);
            doAnalyze(right, state); 
       
        }
        
        // If we are finished generating the chain for the top member access expression,
        // then shut off chaining. Otherwise the next variable we see might get added to this chain,
        // even though is it not related
        if (!wasChaining)
        {
            state.chaining = false;
            state.curChain = null;
        }
        
        // If we are finished with a chain of ObjectProxy.prop.prop things,
        // then clear out the remembered info from the ObjectProxy.
        // Note that this "termination condition" is quite different from the one above.
        // Above the logic was "I'm going to set this, recursively apply to children, then clear.
        // In this case, the logic is "I'm going to set and forget, because my PARENT want these results.
        // Eventually I can clear it when my I can determine that my parent is not part of the chain.
        if ( state.isObjectProxyExpression)
        {
            IASNode parent = node.getParent();
            if (!(parent instanceof IMemberAccessExpressionNode))
            {
                state.isObjectProxyExpression = false;
                state.objectProxyEventNames = null;
            }
        }
    }

    private boolean isLeftXML(MemberAccessExpressionNode maen)
    {
        IDefinition xmlDef = project.getBuiltinType(IASLanguageConstants.BuiltinType.XML);
		if (maen.getLeftOperandNode().getNodeID() == ASTNodeID.Op_AsID)
		{
			if (maen.getLeftOperandNode().getChild(1).getNodeID() == ASTNodeID.IdentifierID)
			{
				IdentifierNode child = (IdentifierNode)maen.getLeftOperandNode().getChild(1);
				IDefinition def = child.resolve(project);
	    		if (def == xmlDef)
	    			return true;
				ITypeDefinition type = child.resolveType(project);
	    		if (type != null && type.isInstanceOf("XML", project))
	    			return true;    				
			}
		} 
		else if (maen.getLeftOperandNode().getNodeID() == ASTNodeID.MemberAccessExpressionID)
		{
			maen = (MemberAccessExpressionNode)maen.getLeftOperandNode();
			if (isLeftXML(maen))
				return true;
		}
    	return false;
    }
    
    private boolean nodeIsXML(IASNode node)
    {
        IDefinition xmlDef = project.getBuiltinType(IASLanguageConstants.BuiltinType.XML);
    	IASNode parent = node.getParent();
    	while (parent != null && (parent.getNodeID() == ASTNodeID.MemberAccessExpressionID))
    	{
    		MemberAccessExpressionNode maen = ((MemberAccessExpressionNode)parent);
    		IDefinition def = maen.resolve(project);
    		if (def == xmlDef)
    			return true;
    		ITypeDefinition type = maen.resolveType(project);
    		if (type != null && type.isInstanceOf("XML", project))
    			return true;
    		if (def != null)
    			break;
			if (isLeftXML(maen))
				return true;
    		parent = parent.getParent();
    	}
    	return false;
    }
    
    private void analyzeIdentifierNode(IIdentifierNode node, AnalysisState state)
    {
        IDefinition def = node.resolve(project);
        if ((def == null) && !state.isObjectProxyExpression)
        {
            if (node.getName() == IASKeywordConstants.THIS)
                return;     // this is not "defensive programming"!
                        // we fully expect to get non-resolvable identifiers in some cases:
                        //      a) bad code. 
                        //      b) "this" 
                        // It should be fine to skip over these and continue without creating a watcher
        
                        // If, on the other hand, we are in an object proxy, then we
                        // may very well be a dynamic property with no definition,
                        // so will will continue on (with the knowledge that we have no
                        // IDefinition
            if (nodeIsXML(node))
            	return;
            this.problems.add(new MXMLDatabindingSourceNotBindableProblem(node, node.getName()));
            return;
        }
        
        if (def instanceof IConstantDefinition)
        {
            return;     // we can ignore constants  - they can't be watched
        }
       
        assert state.chaining || state.curChain==null;      // we shouldn't have a chain if we aren't chaining
       
        // Now round up the arguments to make the watcher info
        List<String> eventNames = null;
        WatcherType type = WatcherType.ERROR;
        String name = null;
        Object id = null;
        
        if ((def == null) && state.isObjectProxyExpression)
        {
            // we are in a dynamic property situation... 
            type = WatcherType.PROPERTY;                // always use property watcher
            name = node.getName();                      // get the property name from the id node, since
                                                        // we have no definition
            id = node;                                  //use the parse node as identifier for the watcher, since
                                                        // we don't have an identifier to use. Not that this means we can't
                                                        // share dynamic property watchers. too bad!
            eventNames = state.objectProxyEventNames;   // use the event names we remembered from the proxy
        }
        else
        {
            // we are a not a dynamic property
            
            // Check to see if we are a cast. If so, we can ignore
            if (def instanceof IClassDefinition || def instanceof IInterfaceDefinition)
            {
                // we are an identifier that resolves to a class. perhaps we are a cast?
                // we are a case if the node parent is a function call, but in any case
                // if we see a class here it's either a cast, or it's just a class name
                // in an expression. 
                // If it's a cast, there's nothing wrong. If it's a class, we treat it like a constant string
                // bottom line: don't try to make a watcher, and don't generate a problem
                return;
            }
            
            type = determineWatcherType(def);  // figure out what kind of watcher to make
            if (type == WatcherType.ERROR)
            {
                System.err.println("can't get watcher for " + def);
                return;         // This should never happen. If it does, there is presumably a bug.
                                // But - better to recover here, than to go on and NPE.
                                // This is a workaround for CMP-1283
            }
            
            // if it's a function, make sure it is the one from the function call expression we are parsing
            // If it isn't, just return. This might happen if, for example, the function was
            // not bindable - then functionCallNameDefintion would be null.
            // I suspect there are other cases, too.
            if (type == WatcherType.FUNCTION)
            {
                if (def != state.functionCallNameDefintion)
                    return;
            }
            eventNames = WatcherInfoBase.getEventNamesFromDefinition(def, problems, node, project);
            name = def.getBaseName();
            id = def;
            //solves a problem for function bindings where all bindings resolve to the latest one
            if (type == WatcherType.FUNCTION) {
                //make sure we have unique watchers for each functionCallNode
                id = node;//or maybe node.getAncestorOfType(FunctionCallNode.class); ?
            }
        }
        
        makeWatcherOfKnownType(id, type, state, node, eventNames, name, def);
        
        // Now, check if we need to make an XML watcher. These are special:
        //  We make an XMLWatcher and a property watcher to watch the same thing, so two of them
        // are created in this one call
        if (def instanceof IVariableDefinition)
        {
            // Is the def for an XML type variable?
            IVariableDefinition var = (IVariableDefinition)def;
            ITypeDefinition varType = var.resolveType(project);
            
            // note that varType might be null if code is bad
            boolean isVarXML = (varType==null) ? false : varType.isInstanceOf("XML", project);

            if (isVarXML)
            {
                // if XML,then create a watcher for it.
                String key = "XML";     // using this unique key will work, but it means we can never
                                        // share these. Which is OK.
                List<String> empty = Collections.emptyList();
                makeWatcherOfKnownType(key, WatcherType.XML, state, node, empty, name, null);
            }
        }
    }
    
    /**
     * Master "factory function" for Watcher Info
     * You should always use this factory, rather than instantiating the directly, as it is
     * easier and more reliable to user this function.
     * 
     * @param watcherKey is unique object "key" to refer to the created watcher. Often an IDefintion
     * @param type is which kind of watcher we want to create
     * @param state must be passed in so we can chain watchers as we create them during parse time
     * @param node the node that corresponds to the object we will be watching
     * @param eventNames the events that the watcher must listen to
     * @param name the name of the property or function that we will be watching
     * @param optionalDefinition the IDefinition for the node. Only required for Static Property Watchers
     */
    private void makeWatcherOfKnownType(
            Object watcherKey,
            WatcherType type,
            AnalysisState state,
            IASNode node,
            List<String> eventNames,
            String name,
            IDefinition optionalDefinition)
    {
        assert(eventNames != null);
        assert(name != null);
        
        WatcherInfoBase watcherInfo = bindingDataBase.getOrCreateWatcher(
                state.curChain,
                type,
                watcherKey,
                problems,
                node,
                eventNames);
  
        // After doing the "generic creation" we need to do some type specific
        // setup
        if (type == WatcherType.FUNCTION)
        {
            // TODO: couldn't we move this into a ctor somehow?
            FunctionWatcherInfo fwi = (FunctionWatcherInfo)watcherInfo;
           // fwi.setFunctionName(def.getName());
            fwi.setFunctionName(name);
            // Note: we might want to retrieve the function arguments in the future.
            // But they aren't available on this object - they are on the original FunctionCallExpression node
            FunctionCallNode fnNode = (FunctionCallNode)node.getAncestorOfType(FunctionCallNode.class);
            IExpressionNode[] paramNodes = fnNode.getArgumentNodes();
            fwi.params = paramNodes;
        }
        else if ((type == WatcherType.STATIC_PROPERTY) || (type == WatcherType.PROPERTY))
        {
            PropertyWatcherInfo pwi = (PropertyWatcherInfo)watcherInfo;
            pwi.setPropertyName(name);
            // TODO: can we just have a "name" on the base class??
        }
        else if (type == WatcherType.XML)
        {
            XMLWatcherInfo xwi = (XMLWatcherInfo)watcherInfo;
            xwi.setPropertyName(name);
        }
        
        if (type == WatcherType.STATIC_PROPERTY)
        {
            assert optionalDefinition != null;
            
            // static property watchers need this extra call
            StaticPropertyWatcherInfo pwi = (StaticPropertyWatcherInfo)watcherInfo;
            pwi.init(optionalDefinition, project);
        }
         
        watcherInfo.addBinding(bindingInfo);    // associate our binding with this watcher
                                                // note that one watcher can have more than one binding.
        if (state.chaining)
            state.curChain = watcherInfo;                 // mark this as the new bottom link in the chain   
    }
    
    private static  WatcherType determineWatcherType(IDefinition definition)
    {
        WatcherType ret = WatcherType.ERROR;
        
        if (definition == null) return ret; // there are "special" watchers where this will be null
        
        if (definition instanceof IVariableDefinition)
        {
            if (!definition.isStatic())
            {
                // we use regular property watchers on non-static variables
                ret = WatcherType.PROPERTY;
            }
            else
            {           
                ret = WatcherType.STATIC_PROPERTY;
            }
        }
        else if (definition instanceof IFunctionDefinition)
        {
            ret = WatcherType.FUNCTION;
        }
        else assert false;
        return ret;
    }
}
