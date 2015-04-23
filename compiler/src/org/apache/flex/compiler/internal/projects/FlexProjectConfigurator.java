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

import static org.apache.flex.abc.ABCConstants.CONSTANT_PackageNs;
import static org.apache.flex.abc.ABCConstants.CONSTANT_Qname;

import java.util.HashMap;
import java.util.Map;

import org.apache.flex.abc.semantics.Name;
import org.apache.flex.abc.semantics.Namespace;
import org.apache.flex.abc.semantics.Nsset;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.internal.as.codegen.BindableHelper;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.mxml.IMXMLTypeConstants;

/**
 * This class applies configuration settings to a FlexProject.
 * Currently the settings are hard-coded
 * and only applied when the project is originally created.
 * Eventually they will come from flex-config.xml
 * and this class will support updating with new configuration values.
 */
public class FlexProjectConfigurator
{
    /**
     * These imports will eventually come from flex-config.xml.
     * <p>
     * Note taht we include AIR-specific imports even for non-AIR projects.
     * This does not cause compiler warnings about unknown imports
     * in non-AIR projects because the implicit imports don't get
     * semantically checked.
     * The AIR imports don't increase SWF size if they don't get used
     * because the new compiler resolves multinames down to qnames.
     */
    private static final String[] IMPLICIT_IMPORTS_FOR_MXML = new String[]
    {
        "flash.accessibility.*",
        "flash.data.*", // needed only for AIR
        "flash.debugger.*",
        "flash.desktop.*", // needed only for AIR
        "flash.display.*",
        "flash.errors.*",
        "flash.events.*",
        "flash.external.*",
        "flash.filesystem.*", // needed only for AIR
        "flash.geom.*",
        "flash.html.*", // needed only for AIR
        "flash.html.script.*", // needed only for AIR
        "flash.media.*",
        "flash.net.*",
        "flash.printing.*",
        "flash.profiler.*",
        "flash.system.*",
        "flash.text.*",
        "flash.ui.*",
        "flash.utils.*",
        "flash.xml.*",
        "mx.binding.*",
        "mx.core.ClassFactory",
        "mx.core.DeferredInstanceFromClass",
        "mx.core.DeferredInstanceFromFunction",
        "mx.core.IDeferredInstance",
        "mx.core.IFactory",
        "mx.core.IFlexModuleFactory",
        "mx.core.IPropertyChangeNotifier",
        "mx.core.mx_internal",
        "mx.styles.*"
    };
    
    private static final Map<String, Integer> NAMED_COLORS = new HashMap<String, Integer>();
    
    static
    {
        NAMED_COLORS.put("aqua", 0x00FFFF);
        NAMED_COLORS.put("black", 0x000000);
        NAMED_COLORS.put("blue", 0x0000FF);
        NAMED_COLORS.put("cyan", 0x00FFFF); // nonstandard
        NAMED_COLORS.put("fuchsia", 0xFF00FF);
        NAMED_COLORS.put("gray", 0x808080);
        NAMED_COLORS.put("green", 0x008000);
        NAMED_COLORS.put("lime", 0x00FF00);
        NAMED_COLORS.put("magenta", 0xFF00FF); // nonstandard
        NAMED_COLORS.put("maroon", 0x800000);
        NAMED_COLORS.put("navy", 0x000080);
        NAMED_COLORS.put("olive", 0x808000);
        NAMED_COLORS.put("purple", 0x800080);
        NAMED_COLORS.put("red", 0xFF0000);
        NAMED_COLORS.put("silver", 0xC0C0C0);
        NAMED_COLORS.put("teal", 0x008080);
        NAMED_COLORS.put("white", 0xFFFFFF);
        NAMED_COLORS.put("yellow", 0xFFFF00);
        
        NAMED_COLORS.put("haloBlue", 0x009DFF);
        NAMED_COLORS.put("haloGreen", 0x80FF4D);
        NAMED_COLORS.put("haloOrange", 0xFFB600);
        NAMED_COLORS.put("haloSilver", 0xAECAD9);
        
    }

    public static void configure(FlexProject project)
    {
        configure(project, null);
    }
    
    public static void configure(FlexProject project, Configuration configuration)
    {
        String[] imports = null;
        if (configuration != null)
            imports = configuration.getCompilerMxmlImplicitImports();
        if (imports == null)
            imports = IMPLICIT_IMPORTS_FOR_MXML;
        project.setImplicitImportsForMXML(imports);
        
        // Set the qualified names of various runtime types
        // that the compiler needs to know about.
        // They will also eventually come from flex-config.xml.
        project.setComponentTagType(IMXMLTypeConstants.IFactory);
        project.setStateClass(IMXMLTypeConstants.State);
        project.setStateClientInterface(IMXMLTypeConstants.StateClient);
        project.setInstanceOverrideClass(IMXMLTypeConstants.AddItems);
        project.setPropertyOverrideClass(IMXMLTypeConstants.SetProperty);
        project.setStyleOverrideClass(IMXMLTypeConstants.SetStyle);
        project.setEventOverrideClass(IMXMLTypeConstants.SetEventHandler);
        project.setDeferredInstanceInterface(IMXMLTypeConstants.IDeferredInstance);
        project.setTransientDeferredInstanceInterface(IMXMLTypeConstants.ITransientDeferredInstance);
        project.setDeferredInstanceFromClassClass(IMXMLTypeConstants.DeferredInstanceFromClass);
        project.setDeferredInstanceFromFunctionClass(IMXMLTypeConstants.DeferredInstanceFromFunction);
        project.setFactoryInterface(IMXMLTypeConstants.IFactory);
        project.setClassFactoryClass(IMXMLTypeConstants.ClassFactory);
        project.setMXMLObjectInterface(IMXMLTypeConstants.IMXMLObject);
        project.setContainerInterface(IMXMLTypeConstants.IContainer);
        project.setVisualElementContainerInterface(IMXMLTypeConstants.IVisualElementContainer);
        project.setResourceBundleClass(IMXMLTypeConstants.ResourceBundle);
        project.setResourceManagerClass(IMXMLTypeConstants.ResourceManager);
        project.setResourceModuleBaseClass(IMXMLTypeConstants.ResourceModuleBase);
        project.setBindingManagerClass(IMXMLTypeConstants.BindingManager);
        project.setCSSStyleDeclarationClass(IMXMLTypeConstants.CSSStyleDeclaration);
        project.setFlexModuleInterface(IMXMLTypeConstants.IFlexModule);
        project.setDeferredInstantiationUIComponentInterface(IMXMLTypeConstants.IDeferredInstantiationUIComponent);

        project.setXMLUtilClass(IMXMLTypeConstants.XMLUtil);
        project.setUIComponentDescriptorClass(IMXMLTypeConstants.UIComponentDescriptor);
        project.setModelClass(IMXMLTypeConstants.ObjectProxy);
        project.setBindingClass(IMXMLTypeConstants.Binding);
        project.setPropertyWatcherClass(IMXMLTypeConstants.PropertyWatcher);
        project.setObjectProxyClass(IMXMLTypeConstants.ObjectProxy);
        project.setStaticPropertyWatcherClass(IMXMLTypeConstants.StaticPropertyWatcher);
        project.setFunctionReturnWatcherClass(IMXMLTypeConstants.FunctionReturnWatcher);
        project.setXMLWatcherClass(IMXMLTypeConstants.XMLWatcherClass);
        
        project.setWebServiceClass(IMXMLTypeConstants.WebService);
        project.setWebServiceOperationClass(IMXMLTypeConstants.WebServiceOperation);
        project.setRemoteObjectClass(IMXMLTypeConstants.RemoteObject);
        project.setRemoteObjectMethodClass(IMXMLTypeConstants.RemoteObjectOperation);
        project.setHTTPServiceClass(IMXMLTypeConstants.HTTPService);
        project.setDesignLayerClass(IMXMLTypeConstants.DesignLayer);
        project.setRepeaterClass(IMXMLTypeConstants.Repeater);
        
        project.setNamedColors(NAMED_COLORS);
        
        if (configuration != null)
        {
            String configValue = configuration.getBindingEventHandlerEvent();
            ClassDefinition.Event = configValue;
            int dotIndex;
            dotIndex = configValue.lastIndexOf(".");
            String packageName = configValue.substring(0, dotIndex);
            String className = configValue.substring(dotIndex + 1);
            BindableHelper.NAME_EVENT = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, packageName)), className);
            BindableHelper.STRING_EVENT = configValue;
            
            configValue = configuration.getBindingEventHandlerClass();
            dotIndex = configValue.lastIndexOf(".");
            packageName = configValue.substring(0, dotIndex);
            className = configValue.substring(dotIndex + 1);
            BindableHelper.NAME_EVENT_DISPATCHER = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, packageName)), className);
            BindableHelper.STRING_EVENT_DISPATCHER = configValue;
            
            configValue = configuration.getBindingEventHandlerInterface();
            dotIndex = configValue.lastIndexOf(".");
            packageName = configValue.substring(0, dotIndex);
            className = configValue.substring(dotIndex + 1);
            BindableHelper.NAME_IEVENT_DISPATCHER = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, packageName)), className);
    
            configValue = configuration.getBindingValueChangeEvent();
            dotIndex = configValue.lastIndexOf(".");
            packageName = configValue.substring(0, dotIndex);
            className = configValue.substring(dotIndex + 1);
            BindableHelper.NAME_PROPERTY_CHANGE_EVENT = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, packageName)), className);
            BindableHelper.NAMESPACE_MX_EVENTS = new Namespace(CONSTANT_PackageNs, packageName);
            BindableHelper.PROPERTY_CHANGE_EVENT = configValue;
            
            configValue = configuration.getBindingValueChangeEventKind();
            dotIndex = configValue.lastIndexOf(".");
            packageName = configValue.substring(0, dotIndex);
            className = configValue.substring(dotIndex + 1);
            BindableHelper.NAME_PROPERTY_CHANGE_EVENT_KIND = new Name(CONSTANT_Qname, new Nsset(new Namespace(CONSTANT_PackageNs, packageName)), className);
        
            configValue = configuration.getBindingValueChangeEventType();
            BindableHelper.PROPERTY_CHANGE = configValue;

            configValue = configuration.getStatesClass();
            project.setStateClass(configValue);
            
            configValue = configuration.getStatesInstanceOverrideClass();
            project.setInstanceOverrideClass(configValue);
            
            configValue = configuration.getStatesPropertyOverrideClass();
            project.setPropertyOverrideClass(configValue);
            
            configValue = configuration.getStatesEventOverrideClass();
            project.setEventOverrideClass(configValue);
            
            configValue = configuration.getStatesStyleOverrideClass();
            project.setStyleOverrideClass(configValue);
            
            configValue = configuration.getComponentFactoryInterface();
            project.setFactoryInterface(configValue);
            
            configValue = configuration.getComponentFactoryClass();
            project.setClassFactoryClass(configValue);

        }
    }
}
