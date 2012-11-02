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

import static com.google.common.collect.Lists.transform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import org.apache.flex.abc.semantics.Name;
import org.apache.flex.compiler.asdoc.IASDocBundleDelegate;
import org.apache.flex.compiler.common.DependencyTypeSet;
import org.apache.flex.compiler.common.XMLName;
import org.apache.flex.compiler.config.RSLSettings;
import org.apache.flex.compiler.css.ICSSManager;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IEffectDefinition;
import org.apache.flex.compiler.definitions.IEventDefinition;
import org.apache.flex.compiler.definitions.IGetterDefinition;
import org.apache.flex.compiler.definitions.IStyleDefinition;
import org.apache.flex.compiler.definitions.IVariableDefinition;
import org.apache.flex.compiler.definitions.references.IResolvedQualifiersReference;
import org.apache.flex.compiler.definitions.references.ReferenceFactory;
import org.apache.flex.compiler.exceptions.LibraryCircularDependencyException;
import org.apache.flex.compiler.filespecs.IFileSpecification;
import org.apache.flex.compiler.internal.css.CSSManager;
import org.apache.flex.compiler.internal.definitions.ClassDefinition;
import org.apache.flex.compiler.internal.definitions.PackageDefinition;
import org.apache.flex.compiler.internal.mxml.MXMLDialect;
import org.apache.flex.compiler.internal.mxml.MXMLManifestManager;
import org.apache.flex.compiler.internal.projects.DependencyGraph.Edge;
import org.apache.flex.compiler.internal.scopes.ASProjectScope;
import org.apache.flex.compiler.internal.scopes.ASScope;
import org.apache.flex.compiler.internal.targets.AppSWFTarget;
import org.apache.flex.compiler.internal.targets.FlexAppSWFTarget;
import org.apache.flex.compiler.internal.targets.SWCTarget;
import org.apache.flex.compiler.internal.tree.mxml.MXMLImplicitImportNode;
import org.apache.flex.compiler.internal.workspaces.Workspace;
import org.apache.flex.compiler.mxml.IMXMLLanguageConstants;
import org.apache.flex.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.flex.compiler.mxml.MXMLNamespaceMapping;
import org.apache.flex.compiler.projects.IFlexProject;
import org.apache.flex.compiler.scopes.IDefinitionSet;
import org.apache.flex.compiler.targets.ISWCTarget;
import org.apache.flex.compiler.targets.ISWFTarget;
import org.apache.flex.compiler.targets.ITargetProgressMonitor;
import org.apache.flex.compiler.targets.ITargetSettings;
import org.apache.flex.compiler.tree.as.IImportNode;
import org.apache.flex.compiler.units.ICompilationUnit;
import org.apache.flex.compiler.units.requests.IOutgoingDependenciesRequestResult;
import org.apache.flex.compiler.units.requests.IRequest;
import org.apache.flex.compiler.workspaces.IWorkspace;
import org.apache.flex.swc.ISWC;
import com.google.common.collect.ImmutableList;

/**
 * {@code FlexProject} extends {@code ASProject} to add support for compiling
 * .mxml, .css, and .properties files.
 */
public class FlexProject extends ASProject implements IFlexProject
{
    // TODO Remove the redundant fooClass (a qname String) field
    // when we have a fooClassName (an AET Name) field. We can always
    // derive the qname from the AET name.
    
    /**
     * Constructor
     * 
     * @param workspace The {@code Workspace} containing this project.
     * @param asDocBundleDelegate {@link IASDocBundleDelegate} the new project will use to find
     * ASDoc comments for definitions from SWCs.
     */
    public FlexProject(Workspace workspace, IASDocBundleDelegate asDocBundleDelegate)
    {
        super(workspace, true, asDocBundleDelegate); // the "true" here forces the opening of the AS3 namespace.

        getSourceCompilationUnitFactory().addHandler(MXMLSourceFileHandler.INSTANCE);
        getSourceCompilationUnitFactory().addHandler(ResourceBundleSourceFileHandler.INSTANCE);
        namespaceMappings = new MXMLNamespaceMapping[0];

        implicitImportsForMXML = new String[0];
        implicitImportNodesForMXML = new ArrayList<IImportNode>();
        cssManager = new CSSManager(this);
        localeDependentPaths = new HashMap<String, String>();
        locales = Collections.unmodifiableCollection(Collections.<String>emptySet());
    }

    /**
     * Constructor
     * 
     * @param workspace The {@code Workspace} containing this project.
     */
    public FlexProject(Workspace workspace)
    {
        this(workspace, IASDocBundleDelegate.NIL_DELEGATE);
    }

    private final ICSSManager cssManager;
    
    private Collection<String> locales;
    
    /**
     * Maps for the locale dependent paths for the project. For each entry,
     * the key is the path of a locale dependent path (can only be source path 
     * or library path entry) and the value is the locale it depends on.
     */
    private final Map<String, String> localeDependentPaths;

    // The externally-visible fully-qualified ActionScript type name
    // for an MXML <Component> tag. Currently this is "mx.core.IFactory".
    private String componentTagType;
    
    /**
     * The fully-qualified name of the runtime class 
     * that corresponds to a <State> tag.
     * Currently this is "mx.states.State".
     */
    private String stateClass;
    
    /**
     * The fully-qualified name of the runtime interface 
     * for a component with a <code>currentState</code> property.
     * Currently this is "mx.core.IStateClient".
     */
    private String stateClientInterface;
    
    /**
     * The fully-qualified name of the runtime class
     * that is used to implement a state-dependent instance
     * such as <s:Button includeIn="s1">.
     * This class must implement mx.states.IOverride.
     * Currently this is "mx.states.AddItems".
     */
    private String instanceOverrideClass;
    
    /**
     * The AET Name for instanceOverrideClass.
     */
    private Name instanceOverrideClassName;
    
    /**
     * The fully-qualified name of the runtime class
     * that is used to implement a state-dependent property
     * such as label.s1="OK".
     * This class must implement mx.states.IOverride.
     * Currently this is "mx.states.SetProperty".
     */
    private String propertyOverrideClass;
    
    /**
     * The AET Name for propertyOverrideClass.
     */
    private Name propertyOverrideClassName;
    
    /**
     * The fully-qualified name of the runtime class
     * that is used to implement a state-dependent style
     * such as color.s1="#FF0000".
     * This class must implement mx.states.IOverride.
     * Currently this is "mx.states.SetStyle".
     */
    private String styleOverrideClass;
    
    /**
     * The AET Name for styleOverrideClass.
     */
    private Name styleOverrideClassName;
    
    /**
     * The fully-qualified name of the runtime class
     * that is used to implement a state-dependent event handler
     * such as click.s1="doit()".
     * This class must implement mx.states.IOverride.
     * Currently this is "mx.states.SetEventHandler".
     */
    private String eventOverrideClass;
    
    /**
     * The AET Name for eventOverrideClass.
     */
    private Name eventOverrideClassName;
    
    /**
     * The fully-qualified name of the runtime interface
     * that represents a deferred instance.
     * Currently this is "mx.core.IDeferredInstance".
     */
    private String deferredInstanceInterface;
    
    /**
     * The fully-qualified name of the runtime interface
     * that represents a transient deferred instance.
     * Currently this is "mx.core.ITransientDeferredInstance".
     */
    private String transientDeferredInstanceInterface;
    
    /**
     * The fully-qualified name of the runtime class
     * that creates an IDeferredInstance from a class.
     * Currently this is "mx.core.DeferredInstanceFromClass".
     */
    private String deferredInstanceFromClassClass;
    
    /**
     * The fully-qualified name of the runtime class
     * that creates an IDeferredInstance instance from a function.
     * Currently this is "mx.core.DeferredInstanceFromFunction".
     */
    private String deferredInstanceFromFunctionClass;
    
    private Name deferredInstanceFromFunctionName;
    
    /**
     * The fully-qualified name of the runtime interface
     * that represents a factory for creating instances.
     * Currently this is "mx.core.Factory".
     */
    private String factoryInterface;
    
    /**
     * The fully-qualified name of the runtime class
     * that creates an IFactory from a class.
     * Currently this is "mx.core.ClassFactory".
     */
    private String classFactoryClass;
    
    /**
     * The fully-qualified name of the runtime interface
     * representing non-visual elements whose
     * <code>initialized()</code> method should be called.
     * Currently this is "mx.core.IMXMLObject".
     */
    private String mxmlObjectInterface;
    
    /**
     * The fully-qualified name of the runtime interface
     * for MX containers.
     * Currently this is "mx.core.IContainer".
     */
    private String containerInterface;
    
    /**
     * The fully-qualified name of the runtime interface
     * for visual element containers.
     * Currently this is "mx.core.IVisualElementContainer".
     */
    private String visualElementContainerInterface;

    /** 
     * The fully-qualified name of the runtime class
     * that represents a resource bundle.
     * Currently this is "mx.resources.ResourceBundle".
     */
    private String resourceBundleClass;
    
    /** 
     * The fully-qualified name of the runtime class
     * that represents the resource manager.
     * Currently this is "mx.resources.ResourceManager".
     */
    private String resourceManagerClass;
        
    /** 
     * The fully-qualified name of the module base class
     * that represents a resource module instance.
     * Currently this is "flex.compiler.support.ResourceModuleBase".
     */
    private String resourceModuleBaseClass;
    
    /**
     * The AET Name for resourceManagerClass.
     */
    private Name resourceManagerClassName;

    /** 
     * The fully-qualified name of the runtime class
     * that manages databinding.
     * Currently this is "mx.binding.BindingManager".
     */
    private String bindingManagerClass;
    
    /**
     * The AET Name for bindingManagerClass.
     */
    private Name bindingManagerClassName;
    
    /**
     * The fully-qualified name of the runtime class
     * for internal Binding object
     * Currently this is "mx.binding.Binding".
     */
    private String bindingClass;
   
    /**
     * The AET Name for bindingClass
     */
    private Name bindingClassName;
    
    /**
     * List of RSL configured in this application.
     */
    private List<RSLSettings> rslSettingsList;
    
    /**
     * A list of theme files applicable to this project.
     */
    private ImmutableList<IFileSpecification> themeFiles = ImmutableList.of();

    private String propertyWatcherClass;
    private String staticPropertyWatcherClass;
    private Name propertyWatcherClassName;
    private Name staticPropertyWatcherClassName;
    private String functionReturnWatcherClass;
    private Name functionReturnWatcherClassName;
    private String objectProxyClass;
    private String xmlWatcherClass;
    private Name xmlWatcherClassName;
    
    /**
     * The fully-qualified name of the runtime class
     * for MX component descriptors.
     * Currently this is "mx.core.UIComponentDescriptor".
     */
    private String uiComponentDescriptorClass;
    
    /**
     * The AET Name for uiComponentDescriptorClass.
     */
    private Name uiComponentDescriptorClassName;
    
    /**
     * The fully-qualified name of the runtime class
     * implementing an <fx:Model>.
     * Currently this is "mx.utils.ObjectProxy".
     */
    private String modelClass;
    
    /**
     * The AET Name for modelClass.
     */
    private Name modelClassName;
    
    /**
     * The fully-qualified name of the runtime class
     * implementing a CSS rule.
     * Currently this is "mx.styles.CSSStyleDeclaration".
     */
    private String cssStyleDeclarationClass;
    
    /**
     * The AET Name for cssStyleDeclarationClass;
     */
    private Name cssStyleDeclarationClassName;
    
    /**
     * The fully-qualified name of the runtime interface
     * for classes supporting a moduleFactory property.
     * Currently this is "mx.core.IFlexModule".
     */
    private String flexModuleInterface;
    
    /**
     * The fully-qualified name of the runtime interface
     * for UIComponents that support deferred instantiation.
     * Currently this is "mx.core.IDeferredInstantiationUIComponent".
     */
    private String deferredInstantiationUIComponentInterface;
    
    /**
     * A map of color names to RGB color values.
     */
    private Map<String, Integer> namedColors;

    // A list of imports, such as "flash.display.*", that is automatically
    // imported by every MXML file.
    private String[] implicitImportsForMXML;
    
    private List<IImportNode> implicitImportNodesForMXML;
    
    // Keeps track of MXML-namespace-URI-to-manifest-file options such as
    // --namespace=http://foo.whatever.com,mymanifest.xml
    private IMXMLNamespaceMapping[] namespaceMappings;

    // This object maintains MXML-tag-to-ActionScript-class mapping info
    // aggregated from the <component> tags in the catalog.xml files
    // inside the SWCs on the project's library path and from the <component>
    // tags in any manifest files associated with XML namespaces.
    private MXMLManifestManager manifestManager;

    /**
     * The fully qualified name of the XMLUtil class.
     * Currently this is mx.utils.XMLUtil.
     */
    private String xmlUtilClass;
    
    /**
     * The AET Name for xmlUtilClass.
     */
    private Name xmlUtilClassName;
    
    /**
     * An absolute path to the services-config.xml file. Will be null
     * if the -services option is not specified.
     */
    private String servicesXMLPath;
    
    /** 
     * The context root used with servicesXMLPath. 
     */
    private String servicesContextRoot;

    /**
     * QName of the class definition for {@code <s:WebService>} tag.
     */
    private String webServiceQName;

    /**
     * QName of the class definition for {@code <s:operation>} child tag of a
     * {@code <s:WebService>} tag.
     */
    private String webServiceOperationQName;

    /**
     * QName of the class definition for {@code <s:RemoteObject>} tag.
     */
    private String remoteObjectQName;

    /**
     * QName of the class definition for {@code <s:method>} child tag of a
     * {@code <s:RemoteObject>} tag.
     */
    private String remoteObjectMethodQName;

    //
    // Backing variables for new configuration setter API.
    //
    private String actionScriptFileEncoding = "utf-8";
    private Map<File, List<String>> extensions;
    private boolean isFlex = false;

    /**
     * QName of the class definition for {@code <s:HTTPService>} tag.
     */
    private String httpServiceQName;

    /**
     * QName of the class definition for {@code <fx:DesignLayer>} tag.
     */
    private String designLayerQName;

    @Override
    public ISWFTarget createSWFTarget(ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor) throws InterruptedException
    {
        ISWFTarget target;

        if (isFlex())
        {
            String rootClassName = targetSettings.getRootClassName();
            target = new FlexAppSWFTarget(ReferenceFactory.packageQualifiedReference(
                    getWorkspace(), rootClassName, true),
                    this, targetSettings, progressMonitor);
        }
        else
        {
            target = new AppSWFTarget(this, targetSettings, progressMonitor);
        }

        return target;
    }

    @Override
    public ISWCTarget createSWCTarget(ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor) throws InterruptedException
    {
        return new SWCTarget(this, targetSettings, progressMonitor);
    }

    /**
     * Convert a color to an integer value.
     * 
     * @param colorValue may be a named color, or a String in 
     * any format supported by Integer.decode(). May not be null.
     * @return the color as an integer.
     * @throws NumberFormatException if the value is not a named color and is not
     * properly formatted for Integer.decode(). 
     * @throws NullPointerException is colorValue is null.
     */
    public int getColorAsInt(String colorValue) throws NumberFormatException
    {
        if (colorValue == null)
            throw new NullPointerException("colorValue may not be null");
        
        if (namedColors != null)
        {
            Integer value = namedColors.get(colorValue);
            if (value != null)
                return value.intValue();
        }
        
        return Integer.decode(colorValue).intValue();
    }
    
    /**
     * Gets the fully-qualified type name corresponding to a <Component> tag.
     * 
     * @return The type name as a String.
     */
    public String getComponentTagType()
    {
        return componentTagType;
    }

    /**
     * Sets the fully-qualified type name corresponding to a <Component> tag.
     * 
     * @param componentTagType The type name as a String.
     */
    public void setComponentTagType(String componentTagType)
    {
        this.componentTagType = componentTagType;
    }
    
    public String getStateClass()
    {
        return stateClass;
    }
    
    public void setStateClass(String stateClass)
    {
        this.stateClass = stateClass;
    }
    
    public String getStateClientInterface()
    {
        return stateClientInterface;
    }
    
    public void setStateClientInterface(String stateClientInterface)
    {
        this.stateClientInterface = stateClientInterface;
    }
    
    public String getInstanceOverrideClass()
    {
        return instanceOverrideClass;
    }
    
    public Name getInstanceOverrideClassName()
    {
        return instanceOverrideClassName;
    }
    
    public void setInstanceOverrideClass(String instanceOverrideClass)
    {
        this.instanceOverrideClass = instanceOverrideClass;
        instanceOverrideClassName = getMNameForQName(instanceOverrideClass);
    }
    
    public String getPropertyOverrideClass()
    {
        return propertyOverrideClass;
    }
    
    public Name getPropertyOverrideClassName()
    {
        return propertyOverrideClassName;
    }
    
    public void setPropertyOverrideClass(String propertyOverrideClass)
    {
        this.propertyOverrideClass = propertyOverrideClass;
        propertyOverrideClassName = getMNameForQName(propertyOverrideClass);
    }
    
    public String getStyleOverrideClass()
    {
        return styleOverrideClass;
    }
    
    public Name getStyleOverrideClassName()
    {
        return styleOverrideClassName;
    }
    
    public void setStyleOverrideClass(String styleOverrideClass)
    {
        this.styleOverrideClass = styleOverrideClass;
        styleOverrideClassName = getMNameForQName(styleOverrideClass);
    }
    
    public String getEventOverrideClass()
    {
        return eventOverrideClass;
    }
    
    public Name getEventOverrideClassName()
    {
        return eventOverrideClassName;
    }
    
    public void setEventOverrideClass(String eventOverrideClass)
    {
        this.eventOverrideClass = eventOverrideClass;
        eventOverrideClassName = getMNameForQName(eventOverrideClass);
    }
    
    public String getDeferredInstanceInterface()
    {
        return deferredInstanceInterface;
    }
    
    public void setDeferredInstanceInterface(String deferredInstanceInterface)
    {
        this.deferredInstanceInterface = deferredInstanceInterface;
    }
    
    public String getTransientDeferredInstanceInterface()
    {
        return transientDeferredInstanceInterface;
    }
    
    public void setTransientDeferredInstanceInterface(String transientDeferredInstanceInterface)
    {
        this.transientDeferredInstanceInterface = transientDeferredInstanceInterface;
    }
    
    public String getDeferredInstanceFromClassClass()
    {
        return deferredInstanceFromClassClass;
    }
    
    public void setDeferredInstanceFromClassClass(String deferredInstanceFromClassClass)
    {
        this.deferredInstanceFromClassClass = deferredInstanceFromClassClass;
    }

    public String getDeferredInstanceFromFunctionClass()
    {
        return deferredInstanceFromFunctionClass;
    }
    
    public Name getDeferredInstanceFromFunctionName()
    {
        return deferredInstanceFromFunctionName;
    }
    
    public void setDeferredInstanceFromFunctionClass(String deferredInstanceFromFunctionClass)
    {
        this.deferredInstanceFromFunctionClass = deferredInstanceFromFunctionClass;
        deferredInstanceFromFunctionName =  getMNameForQName(deferredInstanceFromFunctionClass);
    }
    
    public String getFactoryInterface()
    {
        return factoryInterface;
    }
    
    public void setFactoryInterface(String factoryInterface)
    {
        this.factoryInterface = factoryInterface;
    }
    
    public String getMXMLObjectInterface()
    {
        return mxmlObjectInterface;
    }
    
    public void setMXMLObjectInterface(String mxmlObjectInterface)
    {
        this.mxmlObjectInterface = mxmlObjectInterface;
    }

    public String getClassFactoryClass()
    {
        return classFactoryClass;
    }
    
    public void setClassFactoryClass(String classFactoryClass)
    {
        this.classFactoryClass = classFactoryClass;
    }
    
    public String getContainerInterface()
    {
        return containerInterface;
    }
    
    public void setContainerInterface(String containerInterface)
    {
        this.containerInterface = containerInterface;
    }

    public String getVisualElementContainerInterface()
    {
        return visualElementContainerInterface;
    }
    
    public void setVisualElementContainerInterface(String visualElementContainerInterface)
    {
        this.visualElementContainerInterface = visualElementContainerInterface;
    }

    public String getResourceBundleClass()
    {
        return resourceBundleClass;
    }
    
    public void setResourceBundleClass(String resourceBundleClass)
    {
        this.resourceBundleClass = resourceBundleClass;
    }
    
    public String getResourceManagerClass()
    {
        return resourceManagerClass;
    }
    
    public Name getResourceManagerClassName()
    {
        return resourceManagerClassName;
    }
    
    public void setResourceManagerClass(String resourceManagerClass)
    {
        this.resourceManagerClass = resourceManagerClass;
        resourceManagerClassName = getMNameForQName(resourceManagerClass);
    }
        
    public String getBindingManagerClass()
    {
        return bindingManagerClass;
    }
    
    public Name getBindingManagerClassName()
    {
        return bindingManagerClassName;
    }
    
    public String getResourceModuleBaseClass()
    {
        return resourceModuleBaseClass;
    }
    
    public void setResourceModuleBaseClass(String resourceModuleBaseClass)
    {
        this.resourceModuleBaseClass = resourceModuleBaseClass;
    }    
    
    public void setBindingManagerClass(String bindingManagerClass)
    {
        this.bindingManagerClass = bindingManagerClass;
        bindingManagerClassName = getMNameForQName(bindingManagerClass);
    }
    
    public String getXMLUtilClass()
    {
        return xmlUtilClass;
    }
    
    public Name getXMLUtilClassName()
    {
        return xmlUtilClassName;
    }
        
    public void setXMLUtilClass(String xmlUtilClass)
	{
        this.xmlUtilClass = xmlUtilClass;
        xmlUtilClassName = getMNameForQName(xmlUtilClass);
	}
    
    /**
     * Get the fully-qualified name of the runtime class
     * for internal Binding object
     * Currently this is "mx.binding.Binding".
     */
    public String getBindingClass()
    {
        return bindingClass;
    }
    
    /**
     * Get the AET Name for bindingClass
     * @see #getBindingClass
     */
    public Name getBindingClassName()
    {
        assert bindingClassName != null;
        return bindingClassName;
    }
    
    public void setBindingClass(String bindingClass)
    {
        this.bindingClass = bindingClass;
        this.bindingClassName = getMNameForQName(bindingClass);
    }
    
    /**
     * Get the fully-qualified name of the runtime class
     * for internal PropertyWatcher object
     * Currently this is "mx.binding.PropertyWatcher".
     */
    public String getPropertyWatcherClass()
    {
        return propertyWatcherClass;
    }
    
    public String getObjectProxyClass()
    {
        return objectProxyClass;
    }
    
    public void setObjectProxyClass(String objectProxyClass)
    {
        this.objectProxyClass = objectProxyClass;
    }
     
    /**
     * Get the AET Name for propertyWatcherClass
     * @see #getPropertyWatcherClass
     */
    public Name getPropertyWatcherClassName()
    {
        assert propertyWatcherClassName != null;
        return propertyWatcherClassName;
    }
    
    public void setPropertyWatcherClass(String propertyWatcherClass)
    {
        this.propertyWatcherClass = propertyWatcherClass;
        this.propertyWatcherClassName = getMNameForQName(propertyWatcherClass);
    }
     
    /**
     * Get the fully-qualified name of the runtime class
     * for internal StaticPropertyWatcher object
     * Currently this is "mx.binding.StaticPropertyWatcher".
     */
    public String getStaticPropertyWatcherClass()
    {
        return staticPropertyWatcherClass;
    }
     
    /**
     * Get the AET Name for staticPropertyWatcherClass
     * @see #getStaticPropertyWatcherClass
     */
    public Name getStaticPropertyWatcherClassName()
    {
        assert staticPropertyWatcherClassName != null;
        return staticPropertyWatcherClassName;
    }
    
    public void setStaticPropertyWatcherClass(String staticPropertyWatcherClass)
    {
        this.staticPropertyWatcherClass = staticPropertyWatcherClass;
        this.staticPropertyWatcherClassName = getMNameForQName(staticPropertyWatcherClass);
    }
    
    public Name getXMLWatcherClassName()
    {
        return xmlWatcherClassName;
    }
    
    public String getXMLWatcherClass()
    {
        return xmlWatcherClass;
    }
    
    public void setXMLWatcherClass(String xmlWatcherClass)
    {
        this.xmlWatcherClass = xmlWatcherClass;
        this.xmlWatcherClassName = getMNameForQName(xmlWatcherClass);
    }
    
    /**
     * Get the fully-qualified name of the runtime class
     * for internal StaticPropertyWatcher object
     * Currently this is "mx.binding.StaticPropertyWatcher".
     */
    public String getFunctionReturnWatcherClass()
    {
        return functionReturnWatcherClass;
    }
    
    /**
     * Get the AET Name for staticPropertyWatcherClass
     * @see #getStaticPropertyWatcherClass
     */
    public Name getFunctionReturnWatcherClassName()
    {
        assert functionReturnWatcherClassName != null;
        return functionReturnWatcherClassName;
    }
    
    public void setFunctionReturnWatcherClass(String staticPropertyWatcherClass)
    {
        this.functionReturnWatcherClass = staticPropertyWatcherClass;
        this.functionReturnWatcherClassName = getMNameForQName(staticPropertyWatcherClass);
    }
    
    public String getUIComponentDescriptorClass()
    {
        return uiComponentDescriptorClass;
    }
    
    public Name getUIComponentDescriptorClassName()
    {
        return uiComponentDescriptorClassName;
    }
    
    public void setUIComponentDescriptorClass(String uiComponentDescriptorClass)
    {
        this.uiComponentDescriptorClass = uiComponentDescriptorClass;
        uiComponentDescriptorClassName = getMNameForQName(uiComponentDescriptorClass);
    }
    
    public String getModelClass()
    {
        return modelClass;
    }
    
    public Name getModelClassName()
    {
        return modelClassName;
    }
    
    public void setModelClass(String modelClass)
    {
        this.modelClass = modelClass;
        modelClassName = getMNameForQName(modelClass);
    }
    
    public String getCSSStyleDeclarationClass()
    {
        return cssStyleDeclarationClass;
    }
    
    public Name getCSSStyleDeclarationClassName()
    {
        return cssStyleDeclarationClassName;
    }
    
    public void setCSSStyleDeclarationClass(String cssStyleDeclarationClass)
    {
        this.cssStyleDeclarationClass = cssStyleDeclarationClass;
        cssStyleDeclarationClassName = getMNameForQName(cssStyleDeclarationClass);
    }
    
    public String getFlexModuleInterface()
    {
        return flexModuleInterface;
    }
    
    public void setFlexModuleInterface(String flexModuleInterface)
    {
        this.flexModuleInterface = flexModuleInterface;
    }
    
    public String getDeferredInstantiationUIComponentInterface()
    {
        return deferredInstantiationUIComponentInterface;
    }
    
    public void setDeferredInstantiationUIComponentInterface(String deferredInstantiationUIComponentInterface)
    {
        this.deferredInstantiationUIComponentInterface = deferredInstantiationUIComponentInterface;
    }
    
    public Integer getNamedColor(String colorName)
    {
        return namedColors != null ? namedColors.get(colorName) : null;
    }
    
    public void setNamedColors(Map<String, Integer> namedColors)
    {
        this.namedColors = namedColors;
    }
        
    /**
     * Gets the imports that are automatically imported into every MXML file.
     * 
     * @return An array of Strings such as <code>flash.display.*"</code>.
     */
    public String[] getImplicitImportsForMXML()
    {
        return implicitImportsForMXML;
    }
    
    /**
     * Gets a list of nodes representing the implicit imports, for CodeModel.
     * 
     * @return A list of {@code MXMLImplicitImportNode} objects.
     */
    public List<IImportNode> getImplicitImportNodesForMXML()
    {
        return implicitImportNodesForMXML;
    }

    /**
     * Sets the imports that are automatically imported into every MXML file.
     * 
     * @param implicitImportsForMXML An array of Strings such as
     * <code>"flash.display.*"</code>.
     */
    public void setImplicitImportsForMXML(String[] implicitImportsForMXML)
    {
        this.implicitImportsForMXML = implicitImportsForMXML;
        
        implicitImportNodesForMXML = new ArrayList<IImportNode>(implicitImportsForMXML.length);
        for (String implicitImport : implicitImportsForMXML)
        {
            implicitImportNodesForMXML.add(new MXMLImplicitImportNode(this, implicitImport));
        }
    }
    
    /**
     * Gets the mappings from MXML namespace URI to manifest files, as specified
     * by -namespace options.
     * 
     * @return An array of {@code IMXMLNamespaceMapping} objects.
     */
    @Override
    public IMXMLNamespaceMapping[] getNamespaceMappings()
    {
        return namespaceMappings;
    }

    /**
     * Delete the current manifest manager. Next time it's used, it will be
     * re-initialized.
     */
    public void invalidateManifestManager()
    {
        manifestManager = null;
    }

    /**
     * Initialize and return {@link MXMLManifestManager} on demand.
     * 
     * @return {@link MXMLManifestManager}
     */
    private MXMLManifestManager getMXMLManifestManager()
    {
        if (manifestManager == null)
            manifestManager = new MXMLManifestManager(this);
        return manifestManager;
    }

    @Override
    public String resolveXMLNameToQualifiedName(XMLName xmlName, MXMLDialect mxmlDialect)
    {
        String qname = getMXMLManifestManager().resolve(xmlName);

        if (qname == null)
        {
            // If the XML name wasn't found in the manifest manager,
            // see if the URI has the form of package namespace.
            // For example, <ns:Foo xmlns="*"> resolves to "Foo"
            // and <ns:Foo xmlns="com.whatever.*" resolves to "com.whatever.Foo".
            String shortName = xmlName.getName();
            String uri = xmlName.getXMLNamespace();
            String packageName = PackageDefinition.getQualifiedPackageNameFromNamespaceURI(uri);
            if (packageName != null)
                qname = packageName.length() == 0 ? shortName : packageName + "." + shortName; 
        }

        if (qname == null)
        {
            // If the XML name still hasn't resolved, check whether it is a <State> tag.
            // If so, it maps to a special framework class (currently "mx.states.State").
            // For MXML 2009 and MXML 2012, recognize it in the
            // "library://ns.adobe.com/flex/spark" namespace.
            // For MXML 2006, recognize it in the "http://www.adobe.com/2006/mxml" namespace.
            if (xmlName.getName().equals(IMXMLLanguageConstants.STATE))
            {
                if ((mxmlDialect.isEqualToOrAfter(MXMLDialect.MXML_2009) &&
                    xmlName.getXMLNamespace().equals("library://ns.adobe.com/flex/spark") ||
                    mxmlDialect == MXMLDialect.MXML_2006 &&
                    xmlName.getXMLNamespace().equals("http://www.adobe.com/2006/mxml")))
                {
                    qname = stateClass;
                }
            }
        }
        
        if (qname == null)
        {
            // If the XML name still hasn't resolved, check whether it is a <Component> tag.
            // If so, it maps to a special framework type (currently "mx.core.IFactory").
            XMLName componentTag = mxmlDialect.resolveComponent();
            if (xmlName.equals(componentTag))
                qname = componentTagType;
        }
        
        return qname;
    }

    @Override
    public IDefinition resolveXMLNameToDefinition(XMLName xmlName, MXMLDialect mxmlDialect)
    {
        String qName = resolveXMLNameToQualifiedName(xmlName, mxmlDialect);
        if (qName != null)
            return resolveQNameToDefinition(qName);
        else
            return null;
    }

    /**
     * Searches the specified class, and its superclasses, for the definition of
     * a specified property, event, or style.
     * 
     * @param classDefinition The class on which the property, event, or style
     * lookup should start.
     * @param specifierName The name of the property, event, or style.
     * @return An {@code IDefinition} object for the specifier, or
     * <code>null</code> if the specified name doesn't resolve to a property, an
     * event, or a style.
     */
    public IDefinition resolveSpecifier(IClassDefinition classDefinition, String specifierName)
    {
        IDefinition definition = null;

        // From DeclarationHandler.invoke, the correct resolution
        // order is:
        // event
        // property
        // effect
        // style
        // dynamic property (if the class is dynamic)

        // Try to resolve the specifier as a event.
        definition = resolveEvent(classDefinition, specifierName);
        if (definition != null)
            return definition;
        
        // Then try to resolve it as an property.
        definition = resolveProperty(classDefinition, specifierName);
        if (definition != null)
            return definition;
        
        // Then try to resolve it as an effect.
        definition = resolveEffect(classDefinition, specifierName);
        if (definition != null)
            return definition;

        // Then try to resolve it as an style.
        definition = resolveStyle(classDefinition, specifierName);
        if (definition != null)
            return definition;

        // Finally try to resolve it as a dynamic property
        
        return null;
    }
    
    /**
     * Searches the specified class, and its superclasses, for a definition
     * of a setter or variable with the specified name.
     * 
     * @param classDefinition The class.
     * @param propertyName The name of the property to search for.
     * @return A setter definition or a variable definition.
     */
    public IDefinition resolveProperty(IClassDefinition classDefinition, String propertyName)
    {
        Iterator<IClassDefinition> classIterator = classDefinition.classIterator(this, true);
        while (classIterator.hasNext())
        {
            IClassDefinition c = classIterator.next();
            
            ASScope classScope = ((ClassDefinition)c).getContainedScope();
            IDefinitionSet definitionSet = classScope.getLocalDefinitionSetByName(propertyName);
            if (definitionSet != null)
            {
                int n = definitionSet.getSize();
                for (int i = 0; i < n; i++)
                {
                    IDefinition definition = definitionSet.getDefinition(i);
                    
                    // Look for vars and setters, but not getters.
                    // Remember that getters and setters implement IVariableDefinition!
                    if (definition instanceof IVariableDefinition &&
                        !(definition instanceof IGetterDefinition))
                    {
                        // TODO Add namespace logic.
                        // Can MXML set protected properties?
                        // Can MXML set mx_internal properties if you've done
                        // 'use namesapce mx_internal' in a <Script>?
                        return definition;
                    }
                }
            }
        }
    
        return null;
    }
    
    /**
     * Searches the specified class, and its superclasses, for an event
     * definition with the specified name.
     * 
     * @param classDefinition The class.
     * @param eventName The name of the event to search for.
     * @return An event definition.
     */
    public IEventDefinition resolveEvent(IClassDefinition classDefinition, String eventName)
    {
        Iterator<IClassDefinition> classIterator = classDefinition.classIterator(this, true);
        while (classIterator.hasNext())
        {
            IClassDefinition c = classIterator.next();
            IEventDefinition eventDefinition = c.getEventDefinition(getWorkspace(), eventName);
            if (eventDefinition != null)
                return eventDefinition;
        }

        return null;
    }

    /**
     * Searches the specified class, and its superclasses, for a style
     * definition with the specified name.
     * 
     * @param classDefinition The class.
     * @param styleName The name of the style to search for.
     * @return A style definition.
     */
    public IStyleDefinition resolveStyle(IClassDefinition classDefinition, String styleName)
    {
        Iterator<IClassDefinition> classIterator = classDefinition.classIterator(this, true);

        IWorkspace w = getWorkspace();
        while (classIterator.hasNext())
        {
            IClassDefinition c = classIterator.next();
            IStyleDefinition styleDefinition = c.getStyleDefinition(w, styleName);
            if (styleDefinition != null)
                return styleDefinition;
        }

        return null;
    }

    /**
     * Searches the specified class, and its superclasses, for an effect
     * definition with the specified name.
     * 
     * @param classDefinition The class.
     * @param effectName The name of the effect to search for.
     * @return An effect definition.
     */
    public IEffectDefinition resolveEffect(IClassDefinition classDefinition, String effectName)
    {
        IWorkspace w = getWorkspace();

        Iterator<IClassDefinition> classIterator = classDefinition.classIterator(this, true);
        while (classIterator.hasNext())
        {
            IClassDefinition c = classIterator.next();
            IEffectDefinition effectDefinition = c.getEffectDefinition(w, effectName);
            if (effectDefinition != null)
                return effectDefinition;
        }

        return null;
    }

    /**
     * Uses the manifest information to find all the MXML tags that map to a
     * specified fully-qualified ActionScript classname, such as as
     * <code>"spark.controls.Button"</code>
     * 
     * @param className Fully-qualified ActionScript classname, such as as
     * <code>"spark.controls.Button"</code>
     * @return A collection of {@link XMLName}'s representing a MXML tags, such
     * as a <code>"Button"</code> tag in the namespace
     * <code>"library://ns.adobe.com/flex/spark"</code>.
     */
    @Override
    public Collection<XMLName> getTagNamesForClass(String className)
    {
        return getMXMLManifestManager().getTagNamesForClass(className);
    }

    /**
     * Find all the qualified names in the manifest information that have a
     * corresponding MXML tag whose namespace is in the specified set of
     * namespaces. Only qualified names that originate from manifest files are
     * included. Qualified names of components from SWCs are not included.
     * 
     * @param namespaceURIs Set set of MXML tag namespace URIs such as:
     * <code>"library://ns.adobe.com/flex/spark"</code>
     * @return A collection of qualified names (
     * <code>spark.components.Button</code> for example ) that have correponding
     * MXML tags in the manifest information whose namespaces are in the
     * specified set of namespaces.
     * @see MXMLManifestManager#getQualifiedNamesForNamespaces(Set, boolean)
     */
    public Collection<String> getQualifiedClassNamesForManifestNamespaces(
            Set<String> namespaceURIs)
    {
        return getMXMLManifestManager().getQualifiedNamesForNamespaces(
                namespaceURIs, true);
    }

    /**
     * Test is a component is 'lookupOnly' or not. If a component is
     * 'lookupOnly' is means this component it not expect to have source in the
     * target namespace.
     * 
     * @param tagName An {@code XMLName} representing an MXML tag,
     * such as a <code>"Button"</code> tag
     * in the namespace <code>"library://ns.adobe.com/flex/spark"</code>.

     * @return true if the component is 'lookupOnly', false otherwise.
     */
    public boolean isManifestComponentLookupOnly(XMLName tagName)
    {
        return getMXMLManifestManager().isLookupOnly(tagName);
    }
    
    @Override
    public ICSSManager getCSSManager()
    {
        return cssManager;
    }

    /**
     * String that replaces occurrences of "{context.root}" in the 
     * services-config.xml. The context root is specified using the
     * context-root compiler option.
     * 
     * @return represents the context root in the services-config.xml
     * file. Null if the context root has not been set.
     */
    public String getServciesContextRoot()
    {
        return servicesContextRoot;
    }

    /**
     * The absolute path of the services-config.xml file. This file used
     * to configure a Flex client to talk to a BlazeDS server.
     * 
     * @return the absolute path of the services-config.xml file. Null
     * if the file has not been configured.
     */
    @Override
    public String getServicesXMLPath()
    {
        return servicesXMLPath;
    }
    
    /**
     * Configure this project to use a services-config.xml file. 
     * The services-config.xml file is used to configure a Flex client to talk 
     * to a BlazeDS server.
     * 
     * @param path an absolute path to the services-config.xml file.
     * @param contextRoot sets the value of the {context.root} token, which is
     *  often used in channel definitions in the services-config.xml file.
     */
    @Override
    public void setServicesXMLPath(String path, String contextRoot)
    {
        // Support for -services is being disabled because it is 
        // dependent on changes to flex-messaging-common.jar that
        // were never in an official release. The code is commented
        // out instead of removed so it can be revived in open
        // source.
        
//        // Test if path or contextRoot have changed.
//        if (((path != null) && path.equals(this.servicesXMLPath)) &&
//            ((contextRoot != null) && contextRoot.equals(this.servicesContextRoot)))
//        {
//            return;
//        }
//        
//        this.servicesXMLPath = path;
//        this.servicesContextRoot = contextRoot;
//        
//        if (path != null)
//        {
//            // Create a compilation unit and add it to the project.
//            ServicesXMLCompilationUnit scu = new ServicesXMLCompilationUnit(this, path, 
//                    contextRoot);
//           
//           ASProjectScope scope = getScope();
//           IDefinition def = scope.findDefinitionByName(ServicesXMLCompilationUnit.getQname(path));
//           ICompilationUnit unit = null;
//           if (def != null)
//               unit = scope.getCompilationUnitForDefinition(def);
//
//           List<ICompilationUnit> toRemove = null;
//           if (unit == null)
//               toRemove = Collections.<ICompilationUnit>emptyList();
//           else
//               toRemove = Collections.<ICompilationUnit>singletonList(unit);
//
//            updateCompilationUnitsForPathChange(toRemove, 
//                    Collections.<ICompilationUnit>singletonList(scu));
//        }
    }

    @Override
    public Collection<String> getLocales() {
        return locales;
    }
    
    /**
     * Sets the locales of this project.
     * 
     * @param locales locales to set
     */
    @Override
    public void setLocales(Collection<String> locales) 
    {
        assert locales != null : "Locales cannot be set to null";
        this.locales = Collections.unmodifiableCollection(locales);
        clean();
    }
    
    @Override
    public String getResourceLocale(String path) 
    {
        return this.localeDependentPaths.get(path);
    }
    
    /**
     * Sets the locale dependent path map for the project. For each entry,
     * the key is the path of a locale dependent resource (source path or library path entry) 
     * and the value is the locale it depends on.
     * 
     * @param localeDependentResources map that contains project's locale
     * dependent resources
     */
    @Override
    public void setLocaleDependentResources(Map<String, String> localeDependentResources) 
    {
        this.localeDependentPaths.clear();
        this.localeDependentPaths.putAll(localeDependentResources);
        clean();
    }

    private Name getMNameForQName(String qname)
    {
        Workspace workspace = getWorkspace();
        IResolvedQualifiersReference reference =
            ReferenceFactory.packageQualifiedReference(workspace, qname);
        return reference.getMName();
    }

    /**
     * Set QName for the definition of {@code <s:WebService>} tag.
     * 
     * @param name QName of {@code WebService} class.
     */
    public void setWebServiceClass(String name)
    {
        webServiceQName = name;
    }

    /**
     * @return QName of the class definition for <s:WebService> tag.
     */
    public String getWebServiceQName()
    {
        return webServiceQName;
    }

    /**
     * Set QName for the definition of {@code <s:operation>} child tag of a
     * {@code <s:WebService>} tag.
     * 
     * @param name QName of {@code Operation} class.
     */
    public void setWebServiceOperationClass(String name)
    {
        webServiceOperationQName = name;
    }

    /**
     * @return QName of the class definition for {@code <s:operation>} child tag
     * of a {@code <s:WebService>} tag.
     */
    public String getWebServiceOperationQName()
    {
        return webServiceOperationQName;
    }

    /**
     * Set {@code RemoteObjectOperation} QName.
     * 
     * @param name QName.
     */
    public void setRemoteObjectMethodClass(String name)
    {
        remoteObjectMethodQName = name;
    }

    /**
     * @return QName of the class definition for {@code <s:method>} child tag of
     * a {@code <s:RemoteObject>} tag.
     */
    public String getRemoteObjectMethodQName()
    {
        return remoteObjectMethodQName;
    }

    /**
     * Set QName for the definition of {@code <s:RemoteObject>} tag.
     * 
     * @param name QName of {@code RemoteObject} class.
     */
    public void setRemoteObjectClass(String name)
    {
        remoteObjectQName = name;
    }

    /**
     * @return QName of the class definition for {@code <s:RemoteObject>} tag.
     */
    public String getRemoteObjectQName()
    {
        return remoteObjectQName;
    }

    /**
     * Set QName for the definition of {@code <s:HTTPService>} tag.
     * 
     * @param name QName of {@code HTTPService} class.
     */
    public void setHTTPServiceClass(String name)
    {
        httpServiceQName = name;
    }

    /**
     * @return QName of the class definition for {@code <s:HTTPService>} tag.
     */
    public String getHTTPServiceQName()
    {
        return httpServiceQName;
    }

    /**
     * Set QName of the class definition for {@code <fx:DesignLayer>} tag.
     * 
     * @param name QName of {@code DesignLayer} class.
     */
    public void setDesignLayerClass(String name)
    {
        designLayerQName = name;
    }

    /**
     * @return QName of the class definition for {@code <fx:DesignLayer>} tag.
     */
    public String getDesignLayerQName()
    {
        return designLayerQName;
    }
    
    //
    // New implementations of project configuration related getters and setters.
    //
    
    @Override
    public void setIncludeSources(Collection<File> sources) throws InterruptedException
    {
        Collection<File> files = populateIncludedSourceFiles(sources);
        
        super.setIncludeSources(files.toArray(new File[files.size()]));
    }

    @Override
    public void setActionScriptFileEncoding(String encoding)
    {
        actionScriptFileEncoding = encoding;
        clean();
    }

    @Override
    public String getActionScriptFileEncoding()
    {
        return actionScriptFileEncoding;
    }

    @Override
    public void setDefineDirectives(Map<String, String> defines)
    {
        // TODO: This seems strange. Each call to the setter
        // adds new defines. How do you get rid of the old ones?
        addConfigVariables(defines);
        clean();
    }

    @Override
    public void setExtensionLibraries(Map<File, List<String>> extensions)
    {
        this.extensions = extensions;
        clean();
    }

    @Override
    public Map<File, List<String>> getExtensionLibraries()
    {
        return extensions != null ? extensions : Collections.<File, List<String>>emptyMap();
    }

    /**
     * Determines if a two sets of namespace mappings differ.
     * @param oldMappings
     * @param newMappings
     * @return true if the two sets of namespace mappings differ, false otherwise.
     */
    private static boolean namespaceMappingsDiffer(IMXMLNamespaceMapping[] oldMappings, List<? extends IMXMLNamespaceMapping> newMappings)
    {
        if (oldMappings.length != newMappings.size())
            return true;
        int nMappings = oldMappings.length;
        for (int i = 0; i < nMappings; ++i)
        {
            IMXMLNamespaceMapping oldMapping = oldMappings[i];
            IMXMLNamespaceMapping newMapping = newMappings.get(i);
            if (!oldMapping.getManifestFileName().equals(newMapping.getManifestFileName()))
                return true;

            if (!oldMapping.getURI().equals(newMapping.getURI()))
                return true;
        }
        
        return false;
            
    }

    /**
     * Sets the mappings from MXML namespace URI to manifest files, as specified
     * by -namespace options.
     * 
     * @param namespaceMappings An array of {@code IMXMLNamespaceMapping}
     * objects.
     */
    @Override
    public void setNamespaceMappings(List<? extends IMXMLNamespaceMapping> namespaceMappings)
    {
        if (namespaceMappingsDiffer(this.namespaceMappings, namespaceMappings))
        {
            this.namespaceMappings = namespaceMappings.toArray(
                    new MXMLNamespaceMapping[namespaceMappings.size()]);
            clean();
        }
    }

    @Override
    public List<RSLSettings> getRuntimeSharedLibraryPath()
    {
        return rslSettingsList != null ? rslSettingsList : Collections.<RSLSettings>emptyList();
    }

    @Override
    public void setRuntimeSharedLibraryPath(List<RSLSettings> rslSettings)
    {
        this.rslSettingsList = rslSettings;
        clean();
    }

    /**
     * Set a list of theme files for this project. Themes are additive. The
     * theme file closer to the end of the list has higher priorities. Calling
     * this method will remove all the previous set theme files.
     * 
     * @param files A list of theme file specifications.
     */
    public void setThemeFiles(final List<IFileSpecification> files)
    {
        if (files != null)
            this.themeFiles = ImmutableList.copyOf(files);
        else
            this.themeFiles = ImmutableList.of();
        clean();
    }

    /**
     * @return A list of theme files.
     */
    public List<IFileSpecification> getThemeFiles()
    {
        return this.themeFiles;
    }

    @Override
    public Set<String> computeLibraryDependencies(File targetLibrary, DependencyTypeSet dependencySet) 
            throws LibraryCircularDependencyException
    {
        assert targetLibrary != null : "targetLibrary may not be null";
        
        LibraryDependencyGraph libraryGraph = createLibraryDependencyGraph(dependencySet);
        return libraryGraph.getDependencies(targetLibrary.getAbsolutePath());
    }
    
    @Override
    public List<String> computeLibraryDependencyOrder(DependencyTypeSet dependencySet) 
            throws LibraryCircularDependencyException
    {
        LibraryDependencyGraph libraryGraph = createLibraryDependencyGraph(dependencySet);
        return libraryGraph.getDependencyOrder();
    }

    /**
     * Create a library dependency graph based on the both the external 
     * library path and the internal library path of this project.
     * 
     * @param dependencySet The type of dependencies that are included in the 
     * graph. Passing null or an empty set will result in all dependencies 
     * being included in the graph.
     * @return A library dependency graph. May be null if the operation is
     * interrupted.
     */
    public LibraryDependencyGraph createLibraryDependencyGraph(DependencyTypeSet dependencySet)
    {
        // Get the compilation units from the swcs.
        List<ISWC> swcs = getLibraries();
        Workspace w = getWorkspace();
        List<IRequest<IOutgoingDependenciesRequestResult, ICompilationUnit>> requests =
                new ArrayList<IRequest<IOutgoingDependenciesRequestResult, ICompilationUnit>>();
        
        // For each swc, get the compilation units. Request the semantic problems
        // for each compilation unit to cause the dependency graph to be built.
        Set<ICompilationUnit> swcUnits = new HashSet<ICompilationUnit>();
        
        for (ISWC swc : swcs)
        {
            String path = swc.getSWCFile().getAbsolutePath();
            Collection<ICompilationUnit> units = w.getCompilationUnits(path, this);
            swcUnits.addAll(units);
            
            for (ICompilationUnit unit : units)
                requests.add(unit.getOutgoingDependenciesRequest());
        }
        
        // Iterate over the requests to cause the dependency graph to be
        // populated.
        for (IRequest<IOutgoingDependenciesRequestResult, ICompilationUnit> request : requests)
        {
            try
            {
                request.get();
            }
            catch (InterruptedException e)
            {
                assert false : "Unexpected interruption";
                return null;
            }
        }
        
        // Now that the dependency tree of compilation units is populated, 
        // build a graph of library dependencies.
        DependencyGraph unitGraph = getDependencyGraph();
        LibraryDependencyGraph libraryGraph = new LibraryDependencyGraph();
        
        for (ICompilationUnit unit : swcUnits)
        {
            Set<Edge> dependencies = unitGraph.getOutgoingEdges(unit);
            
            // If a unit has no dependencies then make sure we add the swc to
            // the graph to make sure no swcs are missing.
            if (dependencies.size() == 0)
                libraryGraph.addLibrary(unit.getAbsoluteFilename());
            
            // For each edge look to see it the dependency is external to the swc since
            // we only care about external dependencies. 
            // If it is an external dependency then filter based of the dependency types
            // we care about.
            for (Edge dependency : dependencies)
            {
                // Test if the edge is between different swcs
                ICompilationUnit from = dependency.getFrom();
                ICompilationUnit to = dependency.getTo();

                /* Check if the compilation units live in different
                 * swcs. If so, add a dependency between the swcs.
                 */
                if (isInterSWCDependency(from, to))
                {
                    if (dependencySet == null || 
                        dependency.typeInSet(dependencySet) ||
                        dependencySet.isEmpty())
                    {
                        libraryGraph.addLibrary(from.getAbsoluteFilename());
                        libraryGraph.addLibrary(to.getAbsoluteFilename());
                        libraryGraph.addDependency(from.getAbsoluteFilename(), to.getAbsoluteFilename(),
                                dependency.getNamedDependencies());
                    }
                }
            }
        }
        
        return libraryGraph;
    }

    /**
     * Test if the two compilation units live in different SWCs.
     * 
     * @param from
     * @param to
     * @return
     */
    private boolean isInterSWCDependency(ICompilationUnit from, ICompilationUnit to)
    {
        if (from.getAbsoluteFilename() != null &&
            !from.getAbsoluteFilename().equals(to.getAbsoluteFilename()))
        {
            // If the compilation units are from different SWCs, check the 
            // shadowed definitions to make sure the dependency doesn't also
            // live in the same SWC. If the same definition, with the same
            // timestamp is found in the same SWC as a shadowed definition
            // then don't consider this an inter-SWC dependency.

            // Get if the "to" compilation unit has any equivalent shadowed 
            // definitions that are in the same SWC as the "from" unit.
            if (hasShadowedCompulationUnit(to, from.getAbsoluteFilename()))
                return false;
            
            // Test if the "from" unit has an equivalent in the "to" unit's SWC.
            if (hasShadowedCompulationUnit(from, to.getAbsoluteFilename()))
                return false;

            return true;
        }
        
        return false;
    }

    /**
     * Test if the given unit has any shadowed definitions that live
     * in the given swc path.
     * 
     * @param unit
     * @param swcPath
     * @return true if a shadowed definition is found, false otherwise.
     */
    private boolean hasShadowedCompulationUnit(ICompilationUnit unit, String swcPath)
    {
        List<IDefinition> definitionPromises = unit.getDefinitionPromises(); 
        if (definitionPromises == null || definitionPromises.size() == 0)
            return false;

        ASProjectScope scope = getScope();
        Set<IDefinition> toDefs = scope.getShadowedDefinitions(definitionPromises.get(0));
        if (toDefs != null)
        {
            for (IDefinition def : toDefs)
            {
                ICompilationUnit shadowedUnit = scope.getCompilationUnitForDefinition(def);
                
                // Test if a shadowed unit is in the same swc.
                if (swcPath.equals(shadowedUnit.getAbsoluteFilename()))
                {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Files from the parameter list are added as is and directories are recursively 
     * expanded into files.
     * 
     * @param includedSources Collection of files. Can be a mix of files and directories. 
     * @return Set of {@link File} where each {@link File} will return true when isFile()
     * is called. 
     */
    private Set<File> populateIncludedSourceFiles(Collection<File> includedSources)
    {
        final Set<File> includedSourceFiles = new HashSet<File>();
        for (final File file : includedSources)
        {
            if (file.isFile())
            {
                includedSourceFiles.add(file);
                continue;
            }
            else if (file.isDirectory())
            {
                for (final File fileInFolder : 
                     FileUtils.listFiles(file, new String[] {"as", "mxml"}, true))
                {
                    includedSourceFiles.add(fileInFolder);
                }
            }
        }
        return includedSourceFiles;
    }

    @Override
    public List<String> getThemeNames()
    {
        return transform(this.getThemeFiles(), ThemeUtilities.THEME_FILE_TO_NAME);
    }
    
    @Override
    public void clean()
    {
        super.clean();
        manifestManager = null;
    }

    @Override
    public boolean isFlex()
    {
        return this.isFlex;
    }

    @Override
    public void setFlex(boolean value)
    {
        this.isFlex = value;
    }
}
