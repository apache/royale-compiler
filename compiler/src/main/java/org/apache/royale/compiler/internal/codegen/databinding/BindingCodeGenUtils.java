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

import static org.apache.royale.abc.ABCConstants.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.visitors.IABCVisitor;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.INamespaceDefinition;
import org.apache.royale.compiler.internal.abc.FunctionGeneratorHelper;
import org.apache.royale.compiler.internal.as.codegen.CmcEmitter;
import org.apache.royale.compiler.internal.as.codegen.CodeGeneratorManager;
import org.apache.royale.compiler.internal.as.codegen.LexicalScope;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.internal.tree.as.*;
import org.apache.royale.compiler.mxml.IMXMLTypeConstants;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IExpressionNode;

/**
 * A bunch of static, low level helpers for generating instruction lists
 * 
 * In this case, low-level means that these utilities don't know anything about
 * our Databinding analysis strategy, what watchers are (other than some sdk class), directive processors etc..
 * 
 * Because of the "low level" nature of these utilties, they should generally take "simple types"
 * as arguments, with the exception is IASNodes. There is no need to be super strict about this, of course.
 * There are already cases here where we pass in BindingInfo rather that jumpthrough hoops (and CPU cycles)
 * to extract the simple types.
 */
public class BindingCodeGenUtils
{

    private static final Name NAME_VOID = new Name(IASLanguageConstants.void_);

    // params for destination function, get set up fully in static block below.
    private static final Vector<Name> DEST_METHOD_PARAMS = new Vector<Name>();
    static
    {
        // 1 arg, no type
        DEST_METHOD_PARAMS.add(null);
    }
    
    /**
     * emits the code for anonymous getter function, then adds to the instruction list for instantiating one onto the stack
     * @param ret The instruction list being built.
     * @param expressions will be codegened to do the bulk of the getter work
     * 
     *   * VM context:
     *      one entry: nothing
     *      exit: new function object for getter on TOS
     */
    public static  void generateGetter(IABCVisitor emitter, InstructionList ret, List<IExpressionNode> expressions, LexicalScope enclosing_scope)
    {
        // get a method info and a scope for the getter we will generate
        MethodInfo mi = createGetterMethodInfo(); 
        
        mi.setMethodName("bind_getter");
        log(ret, "making bind_getter");
         
        // generate the getter function
        CodeGeneratorManager.getCodeGenerator().generateMXMLDataBindingGetterFunction(mi, expressions, enclosing_scope);
        
        // the instruction stream that will be executed by the ctor
        // needs to push the new function onto the stack
        ret.addInstruction(OP_newfunction, mi);
    }

    /**
     * emits the code for anonymous setter function, then adds to the instruction list for instantiating one onto the stack
     * @param ret The instruction list being built.
     * @param expression will be codegened to do the bulk of the setter work
     *
     *   * VM context:
     *      one entry: nothing
     *      exit: new function object for getter on TOS
     */
    public static  void generateSetter(
            InstructionList ret,
            IExpressionNode expression,
            LexicalScope enclosing_scope
            )
    {
        // get a method info and a scope for the getter we will generate
        MethodInfo mi = createSetterMethodInfo();

        log(ret, "making bind_setter");

        // After codegen of the binding expression, we just need to return it
        // TODO: add O_coerce
        InstructionList stuffToFollowExpression = new InstructionList();
        stuffToFollowExpression.addInstruction(OP_returnvoid);

        List<IExpressionNode> exprs = new ArrayList<IExpressionNode>();
        exprs.add(expression);
  
        // CG the function 
        CodeGeneratorManager.getCodeGenerator().generateMXMLDataBindingSetterFunction(mi, expression, enclosing_scope);
        
        // the instruction stream that will be executed by the ctor
        // needs to push the new function onto the stack
        ret.addInstruction(OP_newfunction, mi); 
    }

    /** 
     * Generate a MethodInfo appropriate for an anonymous getter
     */
    private static MethodInfo createGetterMethodInfo()
    {       
        MethodInfo mi = new MethodInfo();
        
        // TODO: consider making us a more useful name for these anonymous functions
        // For debugging with swfdump, you can uncomment the line below:
        // mi.setMethodName("binding_getter");

        // Set return type as '*'
        mi.setReturnType( null);
    
        return mi;
    }

    /**
     * Generate a MethodInfo appropriate for an anonymous getter
     */
    private static MethodInfo createSetterMethodInfo()
    {
        MethodInfo mi = new MethodInfo();

        // TODO: consider making us a more useful name for these anonymous functions
        // For debugging with swfdump, you can uncomment the line below:
        // mi.setMethodName("binding_getter");

        mi.setReturnType(NAME_VOID);
        mi.setParamTypes(DEST_METHOD_PARAMS);

        return mi;
    }

    /**
     * generate code to instantiate a binding object.  The code to generate the source and destination functions
     * should be passed in in methodInstr - these instructions will be inserted in the appropriate place.
     *
     * exit: stack = binding object
     *
     * @param insns         IL to add instructions to
     * @param project       Project we are compiling in
     * @param destStr       String representation of the destination expression
     * @param srcString     String representation of the source expression
     * @param methodInstr   IL with instructions to put the source function and destination function
     *                      on the stack.  These will be inserted where they are needed - these will usually
     *                      be OP_newfunction, but could be anything that leaves the methods on the stack
     *
     */
    public static void makeBinding(InstructionList insns, RoyaleProject project, String destStr, String srcString, InstructionList methodInstr)
    {
        log(insns, "making binding dest=" + destStr);
           
        /* instantiate Binding
         *  public function Binding(document:Object, srcFunc:Function,
                            destFunc:Function, destString:String,
                            srcString:String = null)
         */
        
        insns.addInstruction(OP_findpropstrict, project.getBindingClassName());
        
        // stack: mx.Binding
        insns.addInstruction(OP_getlocal0);
        // stack: this, mx.Binding
        insns.addAll(methodInstr);
        // stack: destination_func, getter_func, this, mx.Binding
        pushString(insns, destStr != null ? destStr : "");
        // stack: destStr, dest func, getter_func, this, mx.Binding, getters
       
       
        // push the source string, which may be null
        if (srcString != null)
            insns.addInstruction(OP_pushstring, srcString);
        else
            insns.addInstruction(OP_pushnull);
        
        // stack: sourceString, destStr, null, getter_func, this, mx.Binding, getters
        insns.addInstruction(OP_constructprop,  new Object[] {  project.getBindingClassName(), 5 });
        // stack: binding
    }
    
    /**
     * thin wrapper around aet to prevent pushing null strings
     * 
     * @param insns
     * @param string
     */
    private static void pushString(InstructionList insns, String string)
    {
        assert string != null;
        insns.addInstruction(OP_pushstring, string);
    }
    
    
  
    
    /** 
     * Generates the ABC to instantiate a PropertyWatcher
     * Can only generate one special case: 
     *      single event (Constructor actually takes an array of events. we could easily add that
     *      no property getter function (we use null)
     *      
     * Note that we are passing in references to BindingInfo. Although this somewhat breaks
     * the rules for our low-level CG helpers, it's the only convenient way to pass in the list
     * of indicies that we will need to prove to the property watcher ctor.
     *      
     * 
     *  * VM context:
     *      on entry: nothing on stack
     *                  local1 = this._bindings
     *      exit: new property watcher on stack
     */
    
    public static  void makePropertyWatcher(
            boolean makeStaticWatcher,
            InstructionList insns, 
            String propertyName,
            List<String> eventNames,
            List<BindingInfo> bindingInfo,
            MethodInfo propertyGetterFunction,
            RoyaleProject project
           
            )
    {
        log(insns, "makePropertyWatchercg");
        
        // check input
        assert propertyName != null;
 
        Name watcherClassName = makeStaticWatcher ?
                project.getStaticPropertyWatcherClassName()
                : project.getPropertyWatcherClassName();
        insns.addInstruction(OP_findpropstrict, watcherClassName);
        
        
        // stack: mx.PropertyWatcher
        
        /* ctor arguments:
        PropertyWatcher(
                propertyName:String,
                events:Object,
                listeners:Array,
                propertyGetter:Function = null)
         */
        
        pushString(insns, propertyName);
        // stack: propName, mx.PropertyWatcher
       
        // now make Object { eventName : true }, or null
        makeEventNameArray(insns, eventNames);
        // stack: {events}, propName, mx.PropertyWatcher
                 
        makeArrayOfBindingsForWatcher(insns, bindingInfo);
        // stack: listeners[], {events}, propName, mx.PropertyWatcher
        
        if (propertyGetterFunction == null)
        {
            insns.addInstruction(OP_pushnull);            // null is valid
        }
        else 
        {
            insns.addInstruction(OP_newfunction, propertyGetterFunction);
        }
        // stack: propertyGetter, listeners[], {events}, propName, mx.PropertyWatcher
        
        insns.addInstruction(OP_constructprop,  new Object[] { watcherClassName, 4 });
        
        // stack: watcher
        log(insns, "leave makePropertyWatchercg");
    }
    
    /**
     * Generate instructions to instantiate an mx.binding.XMLWatcher
     * 
     * @param insns The instruction list being built.
     * @param propertyName The name of a property.
     * @param bindingInfo The binding information for the property.
     * @param project The compiler project.
     */
    public static void makeXMLWatcher(
          InstructionList insns, 
          String propertyName,
          List<BindingInfo> bindingInfo,
          RoyaleProject project)
    {
        
        /* ctor arguments:
        XMLWatcher(
               (propertyName:String,
                listeners:Array)
         */
        
        Name watcherClassName = project.getXMLWatcherClassName();
        insns.addInstruction(OP_findpropstrict, watcherClassName);
        pushString(insns, propertyName);
        makeArrayOfBindingsForWatcher(insns, bindingInfo);
        insns.addInstruction(OP_constructprop,  new Object[] { watcherClassName, 2});
    }
    
    /**
     * Given an event name, make Object { eventName: true }
     * TODO: support multiple names
     * 
     * returns with new object on stack
     */
    private static boolean makeEventNameArray(InstructionList insns, List<String> eventNames)
    {
        boolean isStyle = false;
        
        if (eventNames.isEmpty())
        {
            insns.addInstruction(OP_pushnull);  // null is acceptable for the events object (old compiler does this)
        }
        else if (eventNames.size() == 1 && eventNames.get(0).equals("isStyle"))
        {
            isStyle = true;
            insns.addInstruction(OP_pushnull);  // null is acceptable for the events object (old compiler does this)            
        }
        else
        {
            for (String eventName : eventNames)
            {
                insns.addInstruction(OP_pushstring, eventName);
                insns.addInstruction(OP_pushtrue);
            }
            insns.addInstruction(OP_newobject, eventNames.size());
        }
        return isStyle;
    }
    
    /** 
     * Generates the ABC to instantiate a FunctionReturnWatcher
     *  * VM context:
     *      on entry: nothing on stack
     *                  local1 = this._bindings
     *      exit: new property watcher on stack
     */
    
    public static  void makeFunctionWatcher(InstructionList insns,
            RoyaleProject project,
            IABCVisitor emitter,
            String functionName,
            List<String> eventNames,
            List<BindingInfo> bindingInfo,
            IExpressionNode[] params,
            LexicalScope lexicalScope)
    {
        
        log(insns, "enter makeFunctionWatchercg");

        Name watcherClassName = project.getFunctionReturnWatcherClassName();
        insns.addInstruction(OP_findpropstrict, watcherClassName);
        // stack: mx.binding.FunctionReturnWatcher
        
        /*
        FunctionReturnWatcher(
                functionName:String,
                document:Object,
                parameterFunction:Function,
                events:Object,
                listeners:Array,
                functionGetter:Function = null,
                isStyle:Boolean = false)
                */
        // arg1: functionName
        pushString(insns, functionName);
        // stack: functionName, watcher class
        
        // arg2: document. which is "this"
        insns.addInstruction(OP_getlocal0);
        // stack: this, functionName, watcher class
        
        // arg3: parameterFunction
        makeParameterFunction(emitter, insns, params, lexicalScope);
        
        // arg4: events
        boolean isStyle = makeEventNameArray(insns, eventNames);
    
        // arg5: listeners
        makeArrayOfBindingsForWatcher(insns, bindingInfo);
        
        // arg 6, propertyGetter. For now, we don't have one, so don't pass (it's optional)

        if (isStyle)
        {
            insns.addInstruction(OP_pushnull);
            insns.addInstruction(OP_pushtrue);
        }

        // now construct the wacher
        insns.addInstruction(OP_constructprop,  new Object[] { watcherClassName, isStyle ? 7 : 5 });
        
        log(insns, "leave makeFunctionWatchercg");
    }
    
    /**
     * Generated the "parameter function" required by the FunctionReturnWatcher
     * constructor.
     * <p>
     * This function is like this:
     * 
     * <pre>
     * function(): Array { return [param1, param2] };
     * </pre>
     * 
     * Since that is actually difficult to generate, we are instead generating a
     * simple function that throws an exception that we know will be caught
     * silently by the Flex SDK.
     * <p>
     * There may be some strange edge cases where this trick won't work, but it
     * will cover the majority of cases BETTER than doing the "right" thing.
     * 
     * @param emitter {@link IABCVisitor} to which generated code is added.
     * @param ret {@link InstructionList} to which code that creates a function
     * closure for the new parameter function should be added.
     */
    
    // version 2: just throw
    
    public static void makeParameterFunction(IABCVisitor emitter, InstructionList ret, IExpressionNode[] params, LexicalScope lexicalScope)
    {
       
        //----------- step 1: build up a method info for the function
        // we are going to make a new func
        MethodInfo mi = new MethodInfo();
          
        // TODO: consider making us a more useful name for these anonymous functions
        // For debugging with swfdump, you can un-comment the line below:
        //mi.setMethodName("parameterFunction");
  
        mi.setReturnType( new Name(IASLanguageConstants.Array));
             
        //---- generate code that throws the exception.
        // This is based on the code that came out of the old compiler
        

        InstructionList parameterFunctionBody = new InstructionList(); 
           
        parameterFunctionBody.addInstruction(OP_getlocal0);
        parameterFunctionBody.addInstruction(OP_pushscope);

        if (params.length == 0) {
            //it is a function call with no params
            parameterFunctionBody.addInstruction(OP_newarray, 0);
            parameterFunctionBody.addInstruction(OP_returnvalue);
        } else {

            //create a temporary ArrayLiteralNode
            ArrayLiteralNode arrayLit = new ArrayLiteralNode();

            for (IASNode param: params) {
                ContainerNode parent = (ContainerNode)param.getParent();
                //make the array literal think it has the param as a child
                arrayLit.getContentsNode().addItem((NodeBase)param);
                //(tricky) now reset the parent of the param, avoid errors with ASScope access during traversal,
                //we only really want the downward traversal from the ArrayLiteralNode as the root, we don't want any sense
                //that it is *actually* a parent, because it is not ASScope aware.
                ((NodeBase) param).setParent(parent);
                if (arrayLit.getSourcePath()==null) {
                    //give the synthetic node a location to avoid an error elsewhere
                    //setting the source path here will prevent an assertion failure in a future call to SemanticUtils.getFileName(iNode) if
                    //asserts are enabled for the compiler runtime (at the time of writing, ant was and maven was not)
                    arrayLit.setSourceLocation(param);
                } else {
                   arrayLit.endAfter(param);
                }
            }

            //generate the instruction list for the Arrayliteral
            InstructionList list = lexicalScope.getGenerator().generateInstructions(arrayLit, CmcEmitter.__array_literal_NT, lexicalScope);
            if (list != null && !list.isEmpty()) {
                //output the array literal and return it
                parameterFunctionBody.addAll(list);
                parameterFunctionBody.addInstruction(OP_returnvalue);
            } else {
                //sanity check, this is the original legacy fail approach @todo review this, hopefully it can be removed if it never happens in practice
                parameterFunctionBody.addInstruction(OP_findpropstrict, IMXMLTypeConstants.NAME_ARGUMENTERROR);

                // Make a new ArugmentError with correct ID
                parameterFunctionBody.addInstruction(OP_pushstring, "unimp param func");
                parameterFunctionBody.pushNumericConstant(1069);
                parameterFunctionBody.addInstruction(OP_constructprop,
                        new Object[] {  IMXMLTypeConstants.NAME_ARGUMENTERROR, 2 });
                // stack : ArgumentError

                parameterFunctionBody.addInstruction(OP_throw);
            }
        }
        
        // now generate the function
        FunctionGeneratorHelper.generateFunction(emitter, mi, parameterFunctionBody);
        
        // the instruction stream that will be executed by the ctor
        // needs to push the new function onto the stack
        ret.addInstruction(OP_newfunction, mi);
    }
    
   
    /**
     * Set this to true so that the runtime trace diagnostic gets 
     * re-routed to use SocketHelper.
     * 
     * This is needed because most of our unit tests don't "see" regular traces,
     * because they use the SocketHelper
     */
    private static boolean useSocketForTrace = false;   // Must check in false
    
    /**
     * debugging function to all "trace"
     * 
     * @param insns The instruction list being built.
     * @param string is a constant string to trace
     */
    public static void trace(InstructionList insns, String string)
    {
        pushString(insns, string);
        trace(insns);
        insns.addInstruction(OP_pop);
    }
    
    /**
     * debugging function to call "trace"
     * 
     * will use top of stack as string parameter, doesn't change stack depth
     * @param insns The instruction list being built.
     */
    public static void trace(InstructionList insns)
    {
        insns.addInstruction(OP_dup);
        if (!useSocketForTrace)
        {
            System.out.println("** Warning: diagnostic trace not using socket. Will be invisible when running most unit tests");
            insns.addInstruction(OP_findpropstrict, new Object[] { new Name("trace") } );
            insns.addInstruction(OP_swap);
            insns.addInstruction(OP_callpropvoid, new Object[] { new Name("trace"), 1 } );
        }
        else
        {
            insns.addInstruction(OP_findpropstrict, new Object[] { new Name("SocketHelper") } );
            insns.addInstruction(OP_getproperty, new Object[] { new Name("SocketHelper") } );
            insns.addInstruction(OP_swap);
            insns.addInstruction(OP_callpropvoid, new Object[] { new Name("trace"), 1 } );
            
            
        }
    }
    /**
     * VM Context:
     *      on entry: local1=_bindings
     *              local2=_watchers
     *      on exit: stack = array[ bindings ]
     */
    
    private static void makeArrayOfBindingsForWatcher(InstructionList insns, List<BindingInfo> bindingInfos)
    {
        assert bindingInfos.size() > 0;    // for now this is always true
                                            // in future....?
       for (BindingInfo bindingInfo : bindingInfos)
       {
           insns.addInstruction(OP_getlocal1);         // stack: _bindings
           insns.pushNumericConstant(bindingInfo.getIndex()); // stack: index, _bindings
           insns.addInstruction(OP_getproperty,  IMXMLTypeConstants.NAME_ARRAYINDEXPROP);       // stack: _bindings[index] 
       }
       // stack:  _bindings[k], _bindings[x], ...
       insns.addInstruction(OP_newarray, bindingInfos.size());       // stack: array[ _bindings[0] ]        
    }
    
    /**
     *  Makes an AET name that represents all the open namespaces, but no "name"
     *  use this when we need to lookup a property, but we aren't sure what namespace it's in.
     *  example:  this[propertyName]
     * @param project
     * @param scope
     * @return
     */
    
    private static Name makeNameForPropertyLookup( ICompilerProject project,
            ASScope scope)
    { 
        Set<INamespaceDefinition> namespaceSet = scope.getNamespaceSet(project);

        ArrayList<Namespace> ns_set = new ArrayList<Namespace>(namespaceSet.size());
        for (INamespaceDefinition namespace : namespaceSet)
            ns_set.add(((NamespaceDefinition)namespace).getAETNamespace());
        int nameKind = CONSTANT_MultinameL;
        String name = null;    // We are not providing a name here
        Name n = new Name(nameKind, new Nsset(ns_set), name);
        return n;
    }
  

    /**
     * generate this function: function(propertyName:String):* { return
     * target[propertyName]; } where target == "this"
     * 
     * @param emitter The {@link IABCVisitor} that generated code is added to.
     * @param project The {@link ICompilerProject} in which code is being
     * generated.
     * @param scope The {@link ASScope} that contains the data binding
     * expression for which a getter is being generated.
     * @return the MethodInfo that we can use to refer to the generated function
     */
    public static MethodInfo generatePropertyGetterFunction(
            IABCVisitor emitter,
            ICompilerProject project,
            ASScope scope
            )
    {
        MethodInfo mi = new MethodInfo();
        
        // TODO: consider making us a more useful name for these anonymous functions
        // For debugging with swfdump, you can un-comment the line below:
        //mi.setMethodName("propertyGetterFunction");

        // Method returns "*", and takes one string parameter
        mi.setReturnType( null);
        Vector<Name> x = new Vector<Name>();
        x.add( new Name(IASLanguageConstants.String));
        mi.setParamTypes( x);
             
        Name magicName = makeNameForPropertyLookup(project, scope);
        InstructionList propertyGetterBody = new InstructionList(); 
    
        // we are not a member, function, so local 0 does not have "this"
        // Make sure no-one accidently uses it
        propertyGetterBody.addInstruction(OP_kill, 0);
    
        // find "this" of class we are servicing by finding "someone"
        // who has the property we are looking for. The property name is in param 1
        propertyGetterBody.addInstruction(OP_getlocal1);
        propertyGetterBody.addInstruction(OP_findproperty,  magicName);
        // stack: this
        
        // get the name of the property we are supposed to find
        propertyGetterBody.addInstruction(OP_getlocal1);
        // stack: propertyName, this
        
        propertyGetterBody.addInstruction(OP_getproperty, magicName); 
        // stack: this[propertyName]
        
        propertyGetterBody.addInstruction(OP_returnvalue);
        FunctionGeneratorHelper.generateFunction(emitter, mi, propertyGetterBody);    
        return mi;
    }
    
    
    
    /** enumerate all the bindings we have created for this class, and execute each
     * of them so that initial values get assigned to binding targets.
     * 
     * VM context:
     *      one entry: nothing
     *      while executing: 
     *          local1 = index (temporary local var)
     *          local2 = this._bindings.length;             
     *                       
     *      exit: nothing
     */
    
    public static InstructionList fireInitialBindings()
    {
        InstructionList insns = new InstructionList();
        log(insns, "fireInitialBinding");
        
        //-----------------------------------------------------
        // index = 0;
        insns.addInstruction(OP_pushbyte, 0);
        insns.addInstruction(OP_setlocal1);
        
        //--------------------------------------------------------
        // size = _bindings.size
        insns.addInstruction(OP_getlocal0);     // stack: this
        insns.addInstruction(OP_getproperty, IMXMLTypeConstants.NAME_BINDINGS);    // stack: _bindings
        insns.addInstruction(OP_dup);            // stack: _bindings, _bindings
        insns.addInstruction(OP_getproperty, new Name("length"));   // stack: _bindings.length, _bindings
        insns.addInstruction(OP_setlocal2);         
        // now stack: _bindings
      
        //---------------------- LOOP OVER BINDINGS ----------------
        Label label = new Label();
        
        insns.addInstruction(OP_label);
        insns.labelCurrent(label);
        insns.addInstruction(OP_dup);            // stack: _bindings, _bindings
        insns.addInstruction(OP_getlocal1);     // stack: index, _bindings, _bindings
        insns.addInstruction(OP_getproperty,  IMXMLTypeConstants.NAME_ARRAYINDEXPROP);   // stack: _bindings[index], _bindings
        
        // now call execute on TOS
        insns.addInstruction(OP_callpropvoid, IMXMLTypeConstants.ARG_EXECUTE);   // stack: _bindings
        
        // increment index
        insns.addInstruction(OP_inclocal_i, 1);
        
        // if (index < length) goto label
        
        insns.addInstruction(OP_getlocal1);          // stack: index, ....
        insns.addInstruction(OP_getlocal2);         // stack: size, index, ...
        insns.addInstruction(OP_iflt, label);       // stack: _bindings    
        
        insns.addInstruction(OP_pop);               // TODO: is this necessary?
       
        return insns;
    }
 
    
    public static final boolean doLog = false;           // check in false, please
    public static void log(String s)
    {
        if (doLog)
        {
            System.out.println("MXMLBindingDirHelp: " + s);
        }
    }
    
    // this version logs to console, but also to ABC output (as debugfile)
    public static void log(InstructionList insns, String s)
    {
        assert s != null;
        if (doLog)
        {
            System.out.println("MXMLBindingDirHelp: " + s);
            insns.addInstruction(OP_debugfile, s);
        }
    }
     
  
    /**
     * This is a static utility class. It is a mistake to try and instantiate one.
     */
    private BindingCodeGenUtils() {}
}
