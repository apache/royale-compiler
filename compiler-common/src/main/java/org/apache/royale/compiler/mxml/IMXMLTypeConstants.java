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

package org.apache.royale.compiler.mxml;

import static org.apache.royale.abc.ABCConstants.CONSTANT_Namespace;
import static org.apache.royale.abc.ABCConstants.CONSTANT_PackageNs;

import org.apache.royale.abc.ABCConstants;
import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.constants.IASLanguageConstants;

/**
 * Constants for AS3 types that have special significance to MXML and Flex
 */
public interface IMXMLTypeConstants
{
    // Qualified names of runtime types.
    static final String AddChild = "mx.states.AddChild";
    static final String AddItems = "mx.states.AddItems";
    static final String BindingManager = "mx.binding.BindingManager";
    static final String ChildManager = "mx.managers.systemClasses.ChildManager";
    static final String ClassFactory = "mx.core.ClassFactory";
    static final String Container = "mx.core.Container";
    static final String CrossDomainRSLItem = "mx.core.CrossDomainRSLItem";
    static final String CSSStyleDeclaration = "mx.styles.CSSStyleDeclaration";
    static final String DeferredInstanceFromClass = "mx.core.DeferredInstanceFromClass";
    static final String DeferredInstanceFromFunction = "mx.core.DeferredInstanceFromFunction";
    static final String DesignLayer = "mx.core.DesignLayer";
    static final String DownloadProgressBar = "mx.preloaders.DownloadProgressBar";
    static final String EffectManager = "mx.effects.EffectManager";
    static final String FlexGlobals = "mx.core.FlexGlobals";
    static final String FlexModuleFactory = "mx.core.FlexModuleFactory";
    static final String RoyaleVersion = "mx.core.RoyaleVersion";
    static final String GroupBase = "spark.components.supportClasses.GroupBase";
    static final String HTTPService = "mx.rpc.http.mxml.HTTPService";
    static final String HaloApplication = "mx.core.Application";
    static final String HaloRadioButtonGroup = "mx.controls.RadioButtonGroup";
    static final String HaloWindowedApplication = "mx.core.WindowedApplication";
    static final String IContainer = "mx.core.IContainer";
    static final String IDeferredInstance = "mx.core.IDeferredInstance";
    static final String IDeferredInstantiationUIComponent = "mx.core.IDeferredInstantiationUIComponent";
    static final String IFactory = "mx.core.IFactory";
    static final String IFlexModule = "mx.core.IFlexModule";
    static final String IFlexModuleFactory = "mx.core.IFlexModuleFactory";
    static final String IID = "mx.core.IID";
    static final String IModule = "mx.modules.IModule";
    static final String IMXMLObject = "mx.core.IMXMLObject";
    static final String IOverride = "mx.states.IOverride";
    static final String ISWFContext = "flashx.textLayout.compose.ISWFContext";
    static final String ITransientDeferredInstance = "mx.core.ITransientDeferredInstance";
    static final String IUIComponent = "mx.core.IUIComponent";
    static final String IVisualElement = "mx.core.IVisualElement";
    static final String IVisualElementContainer = "mx.core.IVisualElementContainer";
    static final String MovieClip = "flash.display.MovieClip";
    static final String mx_internal = "mx.core.mx_internal";
    static final String ObjectProxy = "mx.utils.ObjectProxy";
    static final String RemoteObject = "mx.rpc.remoting.mxml.RemoteObject";
    public final String RemoteObjectOperation = "mx.rpc.remoting.mxml.Operation";
    public final String Repeater = "mx.core.Repeater";
    public final String ResourceBundle = "mx.resources.ResourceBundle";
    public final String ResourceManager = "mx.resources.ResourceManager";
    public final String ResourceModuleBase = "flex.compiler.support.ResourceModuleBase";
    public final String RSLData = "mx.core.RSLData";
    public final String SetEventHandler = "mx.states.SetEventHandler";
    public final String SetProperty = "mx.states.SetProperty";
    public final String SetStyle = "mx.states.SetStyle";
    public final String SparkApplication = "spark.components.Application";
    public final String SparkDownloadProgressBar = "mx.preloaders.SparkDownloadProgressBar";
    public final String SpriteVisualElement = "spark.core.SpriteVisualElement";
    public final String State = "mx.states.State";
    public final String StateClient = "mx.core.IStateClient";
    public final String StyleManagerImpl = "mx.styles.StyleManagerImpl";
    public final String TextFieldFactory = "mx.core.TextFieldFactory";
    public final String Transition = "mx.states.Transition";
    public final String UIComponentDescriptor = "mx.core.UIComponentDescriptor";
    public final String WebService = "mx.rpc.soap.mxml.WebService";
    public final String WebServiceOperation = "mx.rpc.soap.mxml.Operation";
    public final String XMLUtil = "mx.utils.XMLUtil";

    //Names known/used by Flex SDK classes
    static final String _CompiledResourceBundleInfo = "_CompiledResourceBundleInfo";

    // mx_internal namespace
    public final Namespace NAMESPACE_MX_INTERNAL = new Namespace(CONSTANT_Namespace, "http://www.adobe.com/2006/flex/mx/internal");

    // AET names of builtin types used in MXMl code generation.
    public final Name NAME_ARRAY = new Name(IASLanguageConstants.Array);
    public final Name NAME_BOOLEAN = new Name(IASLanguageConstants.Boolean);
    public final Name NAME_OBJECT = new Name(IASLanguageConstants.Object);
    public final Name NAME_VOID = new Name(IASLanguageConstants.void_);

    // AET names for accessing runtime properties.
    public final Name NAME_CHILD_DESCRIPTORS = new Name("childDescriptors");
    public final Name NAME_CURRENT_STATE = new Name("currentState");
    public final Name NAME_DEFAULT_FACTORY = new Name("defaultFactory");
    public final Name NAME_DESIGN_LAYER = new Name("designLayer");
    public final Name NAME_DOCUMENT = new Name("document");
    public final Name NAME_EFFECTS = new Name("effects");
    public final Name NAME_EVENTS = new Name("events");
    public final Name NAME_HANDLER_FUNCTION = new Name("handlerFunction");
    public final Name NAME_ID = new Name("id");
    public final Name NAME_INITIALIZE = new Name("initialize");
    public final Name NAME_NAME = new Name("name");
    public final Name NAME_OVERRIDES = new Name("overrides");
    public final Name NAME_PROPERTIES_FACTORY = new Name("propertiesFactory");
    public final Name NAME_SET_DOCUMENT_DESCRIPTOR = new Name(NAMESPACE_MX_INTERNAL, "setDocumentDescriptor");
    public final Name NAME_STYLE_DECLARATION = new Name("styleDeclaration");
    public final Name NAME_STYLE_MANAGER = new Name("styleManager");
    public final Name NAME_STYLES_FACTORY = new Name("stylesFactory");
    public final Name NAME_TARGET = new Name("target");
    public final Name NAME_TYPE = new Name("type");
    public final Name NAME_UNDERBAR_DOCUMENT = new Name(NAMESPACE_MX_INTERNAL, "_document");
    public final Name NAME_UNDERBAR_DOCUMENT_DESCRIPTOR = new Name(NAMESPACE_MX_INTERNAL, "_documentDescriptor");
    public final Name NAME_VALUE = new Name("value");
    public final Name NAME_PROPERTIES = new Name("properties");

    // AET operands for calling runtime methods.
    public final Object[] ADD_EVENT_LISTENER_CALL_OPERANDS = new Object[] {
            new Name("addEventListener"), 2};
    public final Object[] ADD_LAYER_CALL_OPERANDS = new Object[] {
            new Name("addLayer"), 1};
    public final Object[] CREATE_XML_DOCUMENT_CALL_OPERANDS = new Object[] {
            new Name("createXMLDocument"), 1};
    public final Object[] EXECUTE_BINDINGS_CALL_OPERANDS = new Object[] {
            new Name("executeBindings"), 3};
    public final Object[] GET_INSTANCE_CALL_OPERANDS = new Object[] {
            new Name("getInstance"), 0};
    public final Object[] INITIALIZE_CALL_OPERANDS = new Object[] {
            new Name("initialize"), 0};
    public final Object[] INITIALIZED_CALL_OPERANDS = new Object[] {
            new Name("initialized"), 2};
    public final Object[] REGISTER_EFFECTS_CALL_OPERANDS = new Object[] {
            new Name("registerEffects"), 1};
    public final Object[] SET_DOCUMENT_DESCRIPTOR_CALL_OPERANDS = new Object[] {
            new Name(NAMESPACE_MX_INTERNAL, "setDocumentDescriptor"), 1};
    public final Object[] SET_STYLE_CALL_OPERANDS = new Object[] {
            new Name("setStyle"), 2};
    public final Object[] CONCAT_CALL_OPERANDS = new Object[] {
            new Name("concat"), 1};

    //**************** data binding stuff : TODO move into section above ************/

    // Qualified names of runtime types.
    public final String Binding = "mx.binding.Binding";
    public final String PropertyWatcher = "mx.binding.PropertyWatcher";
    public final String StaticPropertyWatcher = "mx.binding.StaticPropertyWatcher";
    public final String FunctionReturnWatcher = "mx.binding.FunctionReturnWatcher";
    public final String XMLWatcherClass = "mx.binding.XMLWatcher";
    public final String ArgumentError = "ArgumentError";

    public final Name NAME_ARGUMENTERROR = new Name(ArgumentError);
    public final Name NAME_ERRORID = new Name("errorID");
    public final Name NAME_PARENTWATCHER = new Name("parentWatcher");

    // AET names for accessing runtime properties.

    // I can't get Flex to work when I use attempt to use mx_internal
    // It almost works, but something in the M7 test app doens't quite work...
    // AJH: 10/30/13 BindingManager is using hasOwnProperty(_bindingsByDestination)
    // which I don't think works with mx_internal
    //public final Name NAME_WATCHERS = new Name (NAMESPACE_MX_INTERNAL,"_watchers");        // member variable of component with bindings
    //public final Name NAME_BINDINGS = new Name (NAMESPACE_MX_INTERNAL,"_bindings");        // member variable of component with bindings
    //public final Name NAME_BINDINGSBYDESTINATION = new Name(NAMESPACE_MX_INTERNAL,"_bindingsByDestination"); // member variable of component with bindings
    //public final Name NAME_BINDINGSBEGINWITHWORD = new Name(NAMESPACE_MX_INTERNAL,"_bindingsBeginWithWord"); // member variable of component with bindings
    public final Name NAME_SETUPBINDINGS = new Name(NAMESPACE_MX_INTERNAL,"setupBindings"); // member function of component with bindings

    public final Name NAME_WATCHERS = new Name("_watchers"); // member variable of component with bindings
    public final Name NAME_BINDINGS = new Name("_bindings"); // member variable of component with bindings
    public final Name NAME_BINDINGSBYDESTINATION = new Name("_bindingsByDestination"); // member variable of component with bindings
    public final Name NAME_BINDINGSBEGINWITHWORD = new Name("_bindingsBeginWithWord"); // member variable of component with bindings

    public final Name NAME_EXECUTE = new Name("execute");
    public final Name NAME_TWOWAYCOUNTERPART = new Name("twoWayCounterpart");

    // AET operands for calling runtime methods.
    public final Object[] ARG_UPDATEPARENT = new Object[] {
            new Name("updateParent"), 1};
    public final Object[] ARG_ADDCHILD = new Object[] {new Name("addChild"), 1};
    public final Object[] ARG_EXECUTE = new Object[] {NAME_EXECUTE, 0};
    public final Object[] ARG_SETUPBINDINGS = new Object[] {NAME_SETUPBINDINGS, 1};

    // this is a "Name" to use for the property "array index"
    public final Name NAME_ARRAYINDEXPROP = new Name(ABCConstants.CONSTANT_MultinameL, new Nsset(new Namespace(CONSTANT_PackageNs)), null);

    /**
     * AET {@code Name} of the {@code operations} property on a
     * {@code WebService} object.
     */
    public final Name NAME_OPERATIONS = new Name("operations");

}
