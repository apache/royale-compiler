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

package org.apache.royale.compiler.internal.as.codegen;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.instructionlist.InstructionList;
import org.apache.royale.abc.semantics.Label;
import org.apache.royale.abc.semantics.MethodInfo;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.abc.semantics.PooledValue;
import org.apache.royale.abc.visitors.ITraitVisitor;
import org.apache.royale.abc.visitors.ITraitsVisitor;
import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.constants.IASLanguageConstants;
import org.apache.royale.compiler.definitions.IAccessorDefinition;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.definitions.IDefinition;
import org.apache.royale.compiler.definitions.metadata.IMetaTag;
import org.apache.royale.compiler.internal.abc.FunctionGeneratorHelper;
import org.apache.royale.compiler.internal.definitions.NamespaceDefinition;
import org.apache.royale.compiler.internal.scopes.ASScope;
import org.apache.royale.compiler.tree.as.IASNode;
import org.apache.royale.compiler.tree.as.IDefinitionNode;

import java.util.Vector;

import static org.apache.royale.abc.ABCConstants.CONSTANT_PackageNs;
import static org.apache.royale.abc.ABCConstants.CONSTANT_PrivateNs;
import static org.apache.royale.abc.ABCConstants.CONSTANT_Qname;
import static org.apache.royale.abc.ABCConstants.OP_callproperty;
import static org.apache.royale.abc.ABCConstants.OP_callpropvoid;
import static org.apache.royale.abc.ABCConstants.OP_constructprop;
import static org.apache.royale.abc.ABCConstants.OP_findpropstrict;
import static org.apache.royale.abc.ABCConstants.OP_getlex;
import static org.apache.royale.abc.ABCConstants.OP_getlocal;
import static org.apache.royale.abc.ABCConstants.OP_getlocal0;
import static org.apache.royale.abc.ABCConstants.OP_getlocal1;
import static org.apache.royale.abc.ABCConstants.OP_getlocal2;
import static org.apache.royale.abc.ABCConstants.OP_getlocal3;
import static org.apache.royale.abc.ABCConstants.OP_getproperty;
import static org.apache.royale.abc.ABCConstants.OP_iffalse;
import static org.apache.royale.abc.ABCConstants.OP_ifstricteq;
import static org.apache.royale.abc.ABCConstants.OP_pushstring;
import static org.apache.royale.abc.ABCConstants.OP_returnvalue;
import static org.apache.royale.abc.ABCConstants.OP_returnvoid;
import static org.apache.royale.abc.ABCConstants.OP_setlocal2;
import static org.apache.royale.abc.ABCConstants.OP_setproperty;
import static org.apache.royale.abc.ABCConstants.TRAIT_Getter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Method;
import static org.apache.royale.abc.ABCConstants.TRAIT_Setter;
import static org.apache.royale.abc.ABCConstants.TRAIT_Var;

/**
 * Contains helper methods to generate the appropriate code for classes and properties
 * marked bindable.
 */
public class BindableHelper
{

    /**
     * Check to see if a class definition is Bindable (for bindable code-gen)
     * @param classDefinition the Class definition to check
     * @return true if the definition has [Bindable]
     */
    public static boolean isClassCodeGenBindable(IClassDefinition classDefinition) {
        IMetaTag[] metaTags = classDefinition.getAllMetaTags();
        boolean isBindable = false;
        for (IMetaTag metaTag : metaTags)
        {
            if (isCodeGenBindable(metaTag)) {
                //if there is a single [Bindable] tag, then others are ignored, and the class is considered Bindable
                isBindable = true;
                break;
            }
        }
        return isBindable;
    }

    /**
     * Check to see if the metaTag is a codegen style [Bindable] tag (without any event names specified)
     * @param memberDef the definition to check
     * @param isClassBindable whether the containing class is [Bindable]
     * @return true if the tag is [Bindable]
     */
    public static boolean isCodeGenBindableMember(IDefinition memberDef, boolean isClassBindable) {
        boolean memberHasCodeGenBindable = false;
        boolean memberHasExplicitBindable = false;
        boolean resolved = false;
        IMetaTag[] metaTags = memberDef.getAllMetaTags();
        for (IMetaTag metaTag : metaTags)
        {
            if (!memberHasCodeGenBindable) {
                memberHasCodeGenBindable = isCodeGenBindable(metaTag);
                if (memberHasCodeGenBindable) continue;
            }
            if (!memberHasExplicitBindable) {
                memberHasExplicitBindable = isExplicitBindable(metaTag);
            }
        }

        if (isClassBindable) {
            //if a getter/setter member has at least one explicit Bindable tag , then no code-gen is applied, even if it is also has a 'codegen' [Bindable] tag
           if (memberDef instanceof IAccessorDefinition) {
                resolved = !memberHasExplicitBindable;
            } else {
               //if it is a variable member, extra [Bindable] tags, including explicit Bindable(event='something') tags are ignored (at least for code-gen)
               resolved = true; // @todo avoid checking at all in the above loop for this case
           }
        } else {
            //in this case other explicit Bindable tags are ignored, a [Bindable] is sufficient @todo avoid checking for 'memberHasExplicitBindable' in the above loop for this case
            resolved = memberHasCodeGenBindable;
        }
        return resolved;
    }

    public static boolean hasExplicitBindable(IDefinition definition) {
       boolean hasExplicitBindableTag = false;
       if (definition != null) {
           IMetaTag[] metaTags = definition.getAllMetaTags();
           for (IMetaTag metaTag : metaTags)
           {
               if (isExplicitBindable(metaTag)) {
                   hasExplicitBindableTag = true;
                   break;
               }
           }
       }
       return hasExplicitBindableTag;
    }


    public static boolean hasCodegenBindable(IDefinition definition) {
        boolean hasCodegenBindableTag = false;
        if (definition != null) {
            IMetaTag[] metaTags = definition.getAllMetaTags();
            for (IMetaTag metaTag : metaTags)
            {
                if (isCodeGenBindable(metaTag)) {
                    hasCodegenBindableTag = true;
                    break;
                }
            }
        }
        return hasCodegenBindableTag;
    }

    /**
     * Check to see if the metaTag is a codegen style [Bindable] tag (without any event names specified)
     * @param metaTag the MetaTag to check
     * @return true if the MetaTag is [Bindable]
     */
    public static boolean isCodeGenBindable(IMetaTag metaTag) {
        return metaTag != null
                && metaTag.getTagName().equals(BINDABLE)
                && metaTag.getAllAttributes().length == 0;
    }
    /**
     * Check to see if the metaTag is an explicit [Bindable] tag (with some attributes, such as event='someEvent' specified)
     * @param metaTag the MetaTag to check
     * @return true if the MetaTag is [Bindable('something')]
     */
    public static boolean isExplicitBindable(IMetaTag metaTag) {
        return metaTag != null
                && metaTag.getTagName().equals(BINDABLE)
                && metaTag.getAllAttributes().length > 0;
    }


    /**
     * A convenience method to get the next full node after all the Metadata tags, for when
     * a Binding problem should be reported against the main source node for the definition
     * @param site the full definition with [Bindable]
     * @return the first node after the MetaData tags
     */
    public static IASNode getProblemReportingNode(IDefinition site) {
        IDefinitionNode siteNode = site.getNode();

        int afterMetas = siteNode.getMetaTags().getAbsoluteEnd();

        int childrenCount = siteNode.getChildCount();
        IASNode reportingNode = siteNode;
        for (int i = 1; i < childrenCount; i++) {
            IASNode nextNode = siteNode.getChild(i);
            if (nextNode.getAbsoluteStart() > afterMetas) {
                reportingNode = nextNode;
                break;
            }
        }
        return reportingNode;
    }

    /**
     * Generate a synthetic getter for a var that is bindable.  The generated getter will simply return the value of
     * the property.
     * @param classScope    the scope of the class to declare the getter in
     * @param propName      the name of the property - this is the original name of the bindable var
     * @param backingName   the name of the property that the value is actually stored in
     * @param propType      the type of the property
     * @return              a ITraitVisitor for the getter
     */
    static ITraitVisitor generateBindableGetter(LexicalScope classScope, Name propName, Name backingName, Name propType)
    {
        // Equivalent AS, member property:
        //
        //    public function get propName():propType
        //    {
        //        return this.backingName;
        //    }
        //
        // Equivalent AS, static property:
        //
        //    public static function get propName():propType
        //    {
        //        return backingName;
        //    }
        //

        MethodInfo mi = new MethodInfo();
        mi.setMethodName(propName.getBaseName());

        mi.setReturnType(propType);

        InstructionList insns = new InstructionList(3);
        insns.addInstruction(OP_getlocal0);
        insns.addInstruction(OP_getproperty, backingName);
        insns.addInstruction(OP_returnvalue);

        FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), mi, insns);

        return classScope.traitsVisitor.visitMethodTrait(TRAIT_Getter, propName, 0, mi);

    }

    /**
     * Generate a synthetic setter for a var that is bindable.  The generated setter will compare the old value
     * of the property using strict equality (===), and if the new value differs from the old value, the method will
     * dispatch a "propertyChange" event.  The generated code is slightly different for static properties vs. instance
     * properties, but the behavior should be the same.
     * @param classScope    the scope of the class to declare the getter in
     * @param propName      the name of the property - this is the original name of the bindable var
     * @param backingName   the name of the property that the value is actually stored in
     * @param propType      the type of the property
     * @param varDef        the Definition of the original property - used to determine static-ness.
     * @return              a ITraitVisitor for the setter
     */
    static ITraitVisitor generateBindableSetter(LexicalScope classScope, Name propName, Name backingName, Name propType, IDefinition varDef)
    {
        // Equivalent AS, member property:
        //
        //    public function set propName(newValue:propType):void
        //    {
        //        var oldValue = backingName;
        //        if(oldValue !== newValue )
        //        {
        //          backingName = newValue;
        //          if( this.hasEventListener("propertyChange") )
        //            this.dispatchEvent( mx.events.PropertyChangeEvent.createUpdateEvent(this, propName, oldValue, newValue)
        //        }
        //        return;
        //    }
        //
        // Equivalent AS, static property:
        //
        //    public function set propName(newValue:propType):void
        //    {
        //        var oldValue = backingName;
        //        if(oldValue !== newValue )
        //        {
        //          backingName = newValue;
        //          if( staticEventDispatcher.hasEventListener("propertyChange") )
        //            staticEventDispatcher.dispatchEvent( mx.events.PropertyChangeEvent.createUpdateEvent(this, propName, oldValue, newValue)
        //        }
        //        return;
        //    }
        //
        if( varDef != null )
        {
            // Set up a dependency btwn the class the setter is being declared in,
            // and mx.events.PropertyChangeEvents.
            ASScope containingScope = (ASScope)varDef.getContainingScope();
            containingScope.findPropertyQualified(classScope.getProject(),
                    NamespaceDefinition.createPackagePublicNamespaceDefinition(NAMESPACE_MX_EVENTS.getName()),
                    NAME_PROPERTY_CHANGE_EVENT.getBaseName(),
                    DependencyType.EXPRESSION);

            // TODO: remove this once mxmlc pulls in the correct dependencies.
            // TODO: This should be a dependency of PropertyChangeEvent, but it doesn't get emitted into the swf
            // TODO: unless the dependency is added here.
            containingScope.findPropertyQualified(classScope.getProject(),
                    NamespaceDefinition.createPackagePublicNamespaceDefinition(NAMESPACE_MX_EVENTS.getName()),
                    NAME_PROPERTY_CHANGE_EVENT_KIND.getBaseName(),
                    DependencyType.EXPRESSION);
        }

        MethodInfo mi = new MethodInfo();
        mi.setMethodName(propName.getBaseName());

        Vector<Name> paramTypes = new Vector<Name>(1);
        paramTypes.add(propType);
        mi.setParamTypes(paramTypes);

        mi.setReturnType(NAME_VOID);

        InstructionList insns = new InstructionList(32);
        // var oldValue = backingName (or propName for an originally defined getter);
        Name oldValuePropName = classScope.hasDeclaredVariableName(propName) ? backingName : propName;
        insns.addInstruction(OP_getlocal0);
        insns.addInstruction(OP_getproperty, oldValuePropName);
        insns.addInstruction(OP_setlocal2);

        // if( oldValue !== newValue )
        insns.addInstruction(OP_getlocal2);
        insns.addInstruction(OP_getlocal1);
        Label tail = new Label();
        insns.addInstruction(OP_ifstricteq, tail);

        // {
        //    backingName = newValue
        insns.addInstruction(OP_getlocal0);
        insns.addInstruction(OP_getlocal1);
        insns.addInstruction(OP_setproperty, backingName);

        //    if( this.hasEventListener("propertyChange") )
        insns.addInstruction(OP_getlocal0);

        if( varDef.isStatic() )
            insns.addInstruction(OP_getproperty, NAME_BINDING_EVENT_DISPATCHER);

        insns.addInstruction(OP_pushstring, PROPERTY_CHANGE);
        insns.addInstruction(OP_callproperty, new Object[]{NAME_HAS_EVENT_LISTENER, 1});
        insns.addInstruction(OP_iffalse, tail);

        //    {
        //      this.dispatchEvent( mx.events.PropertyChangeEvent.createUpdateEvent(this, propName, oldValue, newValue) )
        insns.addInstruction(OP_getlocal0);
        if( varDef.isStatic() )
            insns.addInstruction(OP_getproperty, NAME_BINDING_EVENT_DISPATCHER);

        insns.addInstruction(OP_getlex,
                NAME_PROPERTY_CHANGE_EVENT);
        insns.addInstruction(OP_getlocal0);
        insns.addInstruction(OP_pushstring, propName.getBaseName());
        insns.addInstruction(OP_getlocal2);
        insns.addInstruction(OP_getlocal1);
        insns.addInstruction(OP_callproperty, new Object[]{NAME_CREATE_UPDATE_EVENT, 4});
        insns.addInstruction(OP_callpropvoid, new Object[]{NAME_DISPATCH_EVENT, 1});

        //    }
        //  }
        //  return;
        insns.labelNext(tail);
        insns.addInstruction(OP_returnvoid);

        FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), mi, insns);

        return classScope.traitsVisitor.visitMethodTrait(TRAIT_Setter, propName, 0, mi);

    }

    /**
     * Generate a property to hold the _bindingEventDispatcher, and generate code to initialize the property.
     * @param traits    the ITraitVisitor to declare the property in.
     * @param isStatic  true if this is for a static property, false if this is an instance property.
     * @return          An InstructionList of instructions to initialize the property.
     */
    static InstructionList generateBindingEventDispatcherInit(ITraitsVisitor traits, boolean isStatic)
    {
        //
        //  Equivalent AS, instance property:
        //    hidden-private var _bindingEventDispatcher:EventDispatcher = new EventDispatcher(this);
        //
        //  Equivalent AS, static property:
        //    static hidden-private var _bindingEventDispatcher:EventDispatcher = new EventDispatcher();
        //

        traits.visitSlotTrait(TRAIT_Var, NAME_BINDING_EVENT_DISPATCHER, ITraitsVisitor.RUNTIME_SLOT, NAME_EVENT_DISPATCHER, LexicalScope.noInitializer);

        InstructionList bindingEventDispIsns = new InstructionList(5);
        bindingEventDispIsns.addInstruction(OP_getlocal0); // Get this
        bindingEventDispIsns.addInstruction(OP_findpropstrict, NAME_EVENT_DISPATCHER);
        int argCount;
        if( isStatic )
        {
            argCount = 0;
        }
        else
        {
            bindingEventDispIsns.addInstruction(OP_getlocal0); // Get this
            argCount = 1;
        }
        bindingEventDispIsns.addInstruction(OP_constructprop, new Object[] {NAME_EVENT_DISPATCHER, argCount} );  // Construct an EventDispatcher, passing in this as the arg
        bindingEventDispIsns.addInstruction(OP_setproperty, NAME_BINDING_EVENT_DISPATCHER);
        return bindingEventDispIsns;
    }

    /**
     * Generate a getter method to return the static event dispatcher.
     * @param classScope    the class to declare the getter in
     * @return              A ITraitVisitor for the getter.
     */
    static ITraitVisitor generateStaticEventDispatcherGetter(LexicalScope classScope)
    {
        // Equivalent AS:
        //
        //      public static function get staticEventDispatcher():flash.events.IEventDispatcher
        //      {
        //          return ClassName._bindingEventDispatcher;
        //      }
        //
        MethodInfo mi = new MethodInfo();
        mi.setMethodName(NAME_STATIC_EVENT_DISPATCHER.getBaseName());

        mi.setReturnType(NAME_IEVENT_DISPATCHER);

        InstructionList insns = new InstructionList(3);
        insns.addInstruction(OP_getlocal0);
        insns.addInstruction(OP_getproperty, NAME_BINDING_EVENT_DISPATCHER);
        insns.addInstruction(OP_returnvalue);

        FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), mi, insns);

        return classScope.traitsVisitor.visitMethodTrait(TRAIT_Getter, NAME_STATIC_EVENT_DISPATCHER, 0, mi);
    }

    /**
     * Generate an addEventListener method.
     * @param classScope    the class to declare the method in
     */
    static void generateAddEventListener(LexicalScope classScope)
    {
        // Generate an addEventListenerFunction
        // Equivalent AS:
        //
        //    public function addEventListener(type:String, listener:Function,
        //                                     useCapture:Boolean = false,
        //                                     priority:int = 0,
        //                                     weakRef:Boolean = false):void
        //    {
        //        _bindingEventDispatcher.addEventListener(type, listener, useCapture,
        //                                                 priority, weakRef);
        //    }

        MethodInfo addEventInfo = new MethodInfo();
        addEventInfo.setMethodName("addEventListener");
        Vector<Name> paramTypes = new Vector<Name>(5);

        paramTypes.add(NAME_STRING);
        paramTypes.add(NAME_FUNCTION);
        paramTypes.add(NAME_BOOLEAN);
        paramTypes.add(NAME_INT);
        paramTypes.add(NAME_BOOLEAN);

        addEventInfo.setParamTypes(paramTypes);

        //addEventInfo.setFlags(ABCConstants.HAS_OPTIONAL);

        addEventInfo.addDefaultValue(new PooledValue(false));
        addEventInfo.addDefaultValue(new PooledValue(0));
        addEventInfo.addDefaultValue(new PooledValue(false));

        addEventInfo.setReturnType(NAME_VOID);

        InstructionList addEventInsns = new InstructionList(10);
        addEventInsns.addInstruction(OP_getlocal0);
        addEventInsns.addInstruction(OP_getproperty, NAME_BINDING_EVENT_DISPATCHER);
        addEventInsns.addInstruction(OP_getlocal1);
        addEventInsns.addInstruction(OP_getlocal2);
        addEventInsns.addInstruction(OP_getlocal3);
        addEventInsns.addInstruction(OP_getlocal, 4);
        addEventInsns.addInstruction(OP_getlocal, 5);
        addEventInsns.addInstruction(OP_callpropvoid, new Object[]{NAME_ADDEVENT_LISTENER, 5});
        addEventInsns.addInstruction(OP_returnvoid);

        FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), addEventInfo, addEventInsns);
        
        classScope.traitsVisitor.visitMethodTrait(TRAIT_Method, NAME_ADDEVENT_LISTENER, 0, addEventInfo);
    }

    /**
     * Generate a dispatchEventListener method.
     * @param classScope    the class to declare the method in
     */
    static void generateDispatchEvent(LexicalScope classScope)
    {
        // Generate a dispatchEvent function
        // Equivalent AS:
        //
        //    public function dispatchEvent(event:flash.events.Event):Boolean
        //    {
        //        return _bindingEventDispatcher.dispatchEvent(event);
        //    }

        MethodInfo mi = new MethodInfo();
        mi.setMethodName(NAME_DISPATCH_EVENT.getBaseName());
        Vector<Name> paramTypes = new Vector<Name>(5);

        paramTypes.add(NAME_FLASH_EVENT);

        mi.setParamTypes(paramTypes);

        mi.setReturnType(NAME_BOOLEAN);

        InstructionList insns = new InstructionList(8);
        insns.addInstruction(OP_getlocal0);
        insns.addInstruction(OP_getproperty, NAME_BINDING_EVENT_DISPATCHER);
        insns.addInstruction(OP_getlocal1);
        insns.addInstruction(OP_callproperty, new Object[]{NAME_DISPATCH_EVENT, 1});
        insns.addInstruction(OP_returnvalue);

        FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), mi, insns);

        classScope.traitsVisitor.visitMethodTrait(TRAIT_Method, NAME_DISPATCH_EVENT, 0, mi);
    }

    /**
     * Generate a hasEventListener method.
     * @param classScope    the class to declare the method in
     */
    static void generateHasEventListener(LexicalScope classScope)
    {
        // Equivalent AS:
        //
        //    public function hasEventListener(type:String):Boolean
        //    {
        //        return _bindingEventDispatcher.hasEventListener(type);
        //    }

        MethodInfo mi = new MethodInfo();
        mi.setMethodName(NAME_HAS_EVENT_LISTENER.getBaseName());
        Vector<Name> paramTypes = new Vector<Name>(5);

        paramTypes.add(NAME_STRING);

        mi.setParamTypes(paramTypes);

        mi.setReturnType(NAME_BOOLEAN);

        InstructionList insns = new InstructionList(10);
        insns.addInstruction(OP_getlocal0);
        insns.addInstruction(OP_getproperty, NAME_BINDING_EVENT_DISPATCHER);
        insns.addInstruction(OP_getlocal1);
        insns.addInstruction(OP_callproperty, new Object[]{NAME_HAS_EVENT_LISTENER, 1});
        insns.addInstruction(OP_returnvalue);

        FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), mi, insns);

        classScope.traitsVisitor.visitMethodTrait(TRAIT_Method, NAME_HAS_EVENT_LISTENER, 0, mi);
    }

    /**
     * Generate a removeEventListener method.
     * @param classScope    the class to declare the method in
     */
    static void generateRemoveEventListener(LexicalScope classScope)
    {
        // Equivalent AS:
        //
        //    public function removeEventListener(type:String,
        //                                        listener:Function,
        //                                        useCapture:Boolean = false):void
        //    {
        //        _bindingEventDispatcher.removeEventListener(type, listener, useCapture);
        //    }
        MethodInfo mi = new MethodInfo();
        mi.setMethodName(NAME_REMOVE_EVENT_LISTENER.getBaseName());
        Vector<Name> paramTypes = new Vector<Name>(5);

        paramTypes.add(NAME_STRING);
        paramTypes.add(NAME_FUNCTION);
        paramTypes.add(NAME_BOOLEAN);

        mi.setParamTypes(paramTypes);

        mi.setFlags(ABCConstants.HAS_OPTIONAL);

        mi.addDefaultValue(new PooledValue(false));

        mi.setReturnType(NAME_VOID);

        InstructionList insns = new InstructionList(10);
         insns.addInstruction(OP_getlocal0);
         insns.addInstruction(OP_getproperty, NAME_BINDING_EVENT_DISPATCHER);
         insns.addInstruction(OP_getlocal1);
         insns.addInstruction(OP_getlocal2);
         insns.addInstruction(OP_getlocal3);
         insns.addInstruction(OP_callpropvoid, new Object[]{NAME_REMOVE_EVENT_LISTENER, 3});
         insns.addInstruction(OP_returnvoid);

        FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), mi, insns);

        classScope.traitsVisitor.visitMethodTrait(TRAIT_Method, NAME_REMOVE_EVENT_LISTENER, 0, mi);

    }

    /**
     * Generate a willTrigger method.
     * @param classScope    the class to declare the method in
     */
    static void generateWillTrigger(LexicalScope classScope)
    {
        // Equivalent AS:
        //
        //    public function willTrigger(type:String):Boolean
        //    {
        //        return _bindingEventDispatcher.willTrigger(type);
        //    }

        MethodInfo mi = new MethodInfo();
        mi.setMethodName(NAME_WILL_TRIGGER.getBaseName());
        Vector<Name> paramTypes = new Vector<Name>(5);

        paramTypes.add(NAME_STRING);

        mi.setParamTypes(paramTypes);

        mi.setReturnType(NAME_BOOLEAN);

        InstructionList insns = new InstructionList(8);
        insns.addInstruction(OP_getlocal0);
        insns.addInstruction(OP_getproperty, NAME_BINDING_EVENT_DISPATCHER);
        insns.addInstruction(OP_getlocal1);
        insns.addInstruction(OP_callproperty, new Object[]{NAME_WILL_TRIGGER, 1});
        insns.addInstruction(OP_returnvalue);

        FunctionGeneratorHelper.generateFunction(classScope.getEmitter(), mi, insns);

        classScope.traitsVisitor.visitMethodTrait(TRAIT_Method, NAME_WILL_TRIGGER, 0, mi);
    }

    /**
     * Return the AET Name to use for the backing property of a bindable var.  This name will have the same simple
     * name as the one passed in, but will be in a special, hidden private namespace to avoid conflicts with other
     * properties.
     * @param propName  the Name of the property to generate a hidden name for
     * @return          the Name of the hidden property that will actually store the value
     */
    static Name getBackingPropertyName(Name propName)
    {
        return new Name(CONSTANT_Qname, new Nsset(bindablePrivateNamespace), propName.getBaseName());
    }

    /**
     * Return the AET Name to use for the backing property of a bindable var.  This name will have the same simple
     * name as the one passed in, but will be in a special, hidden private namespace to avoid conflicts with other
     * properties.
     * @param propName  the Name of the property to generate a hidden name for
     * @return          the Name of the hidden property that will actually store the value
     */
    static Name getBackingPropertyName(Name propName, String suffix)
    {
        return new Name(CONSTANT_Qname, new Nsset(bindablePrivateNamespace), propName.getBaseName() + suffix);
    }

    /**
     * The namespace to put compiler generated members into so that they do not conflict with any user defined
     * members.
     */
    private static final Namespace bindablePrivateNamespace = new Namespace(CONSTANT_PrivateNs, ".BindableNamespace");

    /**
     * The namespace to put compiler generated members into so that they do not conflict with any user defined
     * members.
     */
    public static final NamespaceDefinition bindableNamespaceDefinition = NamespaceDefinition.createNamespaceDefinition(bindablePrivateNamespace);
    

    /**
     * The mx.events package namespace
     */
    public static Namespace NAMESPACE_MX_EVENTS = new Namespace(CONSTANT_PackageNs, "mx.events");

    //
    // Following Names are constants to use for various types & properties used in the code generated for Bindable
    //
    public static Name NAME_PROPERTY_CHANGE_EVENT = new Name(CONSTANT_Qname, new Nsset(NAMESPACE_MX_EVENTS), "PropertyChangeEvent");
    public static String PROPERTY_CHANGE_EVENT = "mx.events.PropertyChangeEvent";
    public static Name NAME_PROPERTY_CHANGE_EVENT_KIND = new Name(CONSTANT_Qname, new Nsset(NAMESPACE_MX_EVENTS), "PropertyChangeEventKind");
    private static final Name NAME_CREATE_UPDATE_EVENT = new Name("createUpdateEvent");

    private static final Name NAME_STRING = new Name(IASLanguageConstants.String);
    private static final Name NAME_FUNCTION = new Name(IASLanguageConstants.Function);
    private static final Name NAME_BOOLEAN = new Name(IASLanguageConstants.Boolean);
    private static final Name NAME_INT = new Name(IASLanguageConstants._int);
    private static final Name NAME_VOID = new Name(IASLanguageConstants.void_);


    public static Name NAME_EVENT = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, "flash.events")), "Event");
    public static Name NAME_FLASH_EVENT = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, "flash.events")), "Event");
    private static final Name NAME_ADDEVENT_LISTENER = new Name("addEventListener");
    private static final Name NAME_DISPATCH_EVENT = new Name("dispatchEvent");
    private static final Name NAME_HAS_EVENT_LISTENER = new Name("hasEventListener");
    private static final Name NAME_REMOVE_EVENT_LISTENER = new Name("removeEventListener");
    private static final Name NAME_WILL_TRIGGER = new Name("willTrigger");
    public static Name NAME_EVENT_DISPATCHER = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, "flash.events")), "EventDispatcher");
    public static Name NAME_IEVENT_DISPATCHER = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, "flash.events")), "IEventDispatcher");
    private static final Name NAME_BINDING_EVENT_DISPATCHER = new Name(CONSTANT_Qname, new Nsset(bindablePrivateNamespace), "_bindingEventDispatcher");
    private static final Name NAME_STATIC_EVENT_DISPATCHER = new Name("staticEventDispatcher");

    public static String PROPERTY_CHANGE = "propertyChange";
    public static String BINDABLE = "Bindable";
    public static String STRING_EVENT = "flash.events.Event";
    public static String STRING_EVENT_DISPATCHER = "flash.events.EventDispatcher";
    public static String STRING_IEVENT_DISPATCHER = "flash.events.IEventDispatcher";

}
