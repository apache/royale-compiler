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

package org.apache.royale.compiler.internal.projects;

import static org.apache.royale.abc.ABCConstants.CONSTANT_PackageNs;
import static org.apache.royale.abc.ABCConstants.CONSTANT_Qname;

import java.io.File;
import java.util.*;

import org.apache.royale.abc.semantics.Name;
import org.apache.royale.abc.semantics.Namespace;
import org.apache.royale.abc.semantics.Nsset;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.filespecs.FileSpecification;
import org.apache.royale.compiler.fxg.flex.FlexFXG2SWFTranscoder;
import org.apache.royale.compiler.internal.as.codegen.BindableHelper;
import org.apache.royale.compiler.internal.config.RoyaleTargetSettings;
import org.apache.royale.compiler.internal.definitions.ClassDefinition;
import org.apache.royale.compiler.internal.embedding.transcoders.DataTranscoder;
import org.apache.royale.compiler.internal.units.FXGCompilationUnit;
import org.apache.royale.compiler.mxml.IMXMLTypeConstants;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.problems.ANELibraryNotAllowedProblem;
import org.apache.royale.compiler.projects.IRoyaleProject;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.swc.ISWC;
import org.apache.royale.utils.FilenameNormalization;

/**
 * This class applies configuration settings to a RoyaleProject.
 * Currently the settings are hard-coded
 * and only applied when the project is originally created.
 * Eventually they will come from royale-config.xml
 * and this class will support updating with new configuration values.
 */
public class RoyaleProjectConfigurator extends Configurator
{
    /**
     * These imports will eventually come from royale-config.xml.
     * <p>
     * Note that we include AIR-specific imports even for non-AIR projects.
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

    public static void configure(RoyaleProject project)
    {
        configure(project, null);
    }
    
    public static void configure(RoyaleProject project, Configuration configuration)
    {
        String[] imports = null;
        if (configuration != null)
            imports = configuration.getCompilerMxmlImplicitImports();
        if (imports == null)
            imports = IMPLICIT_IMPORTS_FOR_MXML;
        project.setImplicitImportsForMXML(imports);
        
        // Set the qualified names of various runtime types
        // that the compiler needs to know about.
        // They will also eventually come from royale-config.xml.
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
            BindableHelper.STRING_IEVENT_DISPATCHER = configValue;
    
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

            configValue = configuration.getFxgBaseClass();
            FlexFXG2SWFTranscoder.packageSpriteVisualElement = configValue;
            FXGCompilationUnit.fxgBaseClassName = configValue;
        
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

            configValue = configuration.getProxyBaseClass();
            project.setProxyBaseClass(configValue);

            project.setStrictXML(configuration.isStrictXML());
            project.setAllowPrivateNameConflicts(configuration.getCompilerAllowPrivateNameConflicts());

            project.setAllowImportAliases(configuration.getCompilerAllowImportAliases());
            project.setAllowAbstractClasses(configuration.getCompilerAllowAbstractClasses());
            project.setAllowPrivateConstructors(configuration.getCompilerAllowPrivateConstructors());

            project.setStrictIdentifierNames(configuration.getCompilerStrictIdentifierNames());
            
            project.setSwfDebugfileAlias(configuration.getSwfDebugfileAlias());
            if (configuration.getSwfDebugfileAlias() != null)
            	FileSpecification.useCRLFFilter = true;
            DataTranscoder.embedClassName = configuration.getByteArrayEmbedClass();
        }
    }
    
    /**
     * Constructor
     */
    public RoyaleProjectConfigurator(Class<? extends Configuration> configurationClass)
    {
        super(configurationClass);
    }
    
    @Override
    public boolean applyToProject(ICompilerProject project)
    {
        boolean success = super.applyToProject(project);
        if (success && !setupSources((IRoyaleProject)project))
            success = false;
        return success;
    }
    
    @Override
    protected boolean applyConfiguration()
    {
        boolean success = true;
        
        RoyaleProject royaleProject = (RoyaleProject)project;
        RoyaleProjectConfigurator.configure(royaleProject, configuration);
        setupCompatibilityVersion(royaleProject);
        setupConfigVariables(royaleProject);
        setupLocaleSettings(royaleProject);
        setupServices(royaleProject);
        setupThemeFiles(royaleProject);
        setupRoyale(royaleProject);
        setupCodegenOptions(royaleProject);
        royaleProject.setRuntimeSharedLibraryPath(getRSLSettingsFromConfiguration(configuration));
            
        if (!setupProjectLibraries(royaleProject))
            success = false;
            
        setupNamespaces(royaleProject);
        return success;
    }
    
    /**
     * Setup {@code -compatibility-version} level. Royale only support Flex 3+.
     * @param project
     */
    protected void setupCompatibilityVersion(RoyaleProject project)
    {
        final int compatibilityVersion = configuration.getCompilerMxmlCompatibilityVersion();
        if (compatibilityVersion < Configuration.MXML_VERSION_3_0)
            throw new UnsupportedOperationException("Unsupported compatibility version: " + configuration.getCompilerCompatibilityVersionString());
        project.setCompatibilityVersion(configuration.getCompilerMxmlMajorCompatibilityVersion(),
                                        configuration.getCompilerMxmlMinorCompatibilityVersion(),
                                        configuration.getCompilerMxmlRevisionCompatibilityVersion());
    }
    
    /**
     * Transfers configuration settings to the project. Handles:
     *     -services
     *     -context-root
     *
     */
    protected void setupServices(RoyaleProject project)
    {
        if (configuration.getCompilerServices() != null)
            project.setServicesXMLPath(configuration.getCompilerServices().getPath(), configuration.getCompilerContextRoot());
    }
    
    protected void setupConfigVariables(IRoyaleProject project)
    {
        final Map<String, String> compilerDefine = project.getCompilerDefine(configuration);
        if (compilerDefine != null)
            project.setDefineDirectives(compilerDefine);
    }
    
    /**
     * Setup the source paths.
     *
     * @return true if successful, false otherwise.
     */
    protected boolean setupSources(IRoyaleProject project)
    {
        // -source-path
        List<String> sourcePath = new ArrayList<String>();
        applySourcePathRules(sourcePath);
        
        project.setSourcePath(toFiles(sourcePath));
        
        // -include-sources
        try
        {
            project.setIncludeSources(toFileList(configuration.getIncludeSources()));
        }
        catch (InterruptedException e)
        {
            assert false : "InterruptedException should never be thrown here";
            return false;
        }
        
        return true;
    }
    
    /**
     * Collect all of the different library paths and set them in the project.
     * The libraries are ordered with RSL and external libraries having the
     * highest priority.
     *
     * <p>Libraries are ordered highest to lowest priority:
     *     <ol>
     *     <li>External Library Path
     *     <li>RSL Library Path
     *     <li>Include Library Path
     *     <li>Library Path
     *     </ol>
     * </p>
     * @param project
     * @return true if successful, false if there is an error.
     */
    protected boolean setupProjectLibraries(IRoyaleProject project)
    {
        LinkedHashSet<File> libraries = new LinkedHashSet<File>();
        
        // add External Library Path
        List<File> externalLibraryFiles = toFileList(project.getCompilerExternalLibraryPath(configuration));
        libraries.addAll(externalLibraryFiles);
        
        // add RSLs
        libraries.addAll(configuration.getRslExcludedLibraries());
        
        // add Include Library Path
        libraries.addAll(toFileList(configuration.getCompilerIncludeLibraries()));
        
        // add Library Path
        libraries.addAll(toFileList(project.getCompilerLibraryPath(configuration)));

        List<String> themes = configuration.getCompilerThemeFiles();
        for (String theme : themes)
        {
        	if (theme.endsWith(".swc"))
        	{
        		File f = new File(theme);
        		libraries.add(f);
        	}
        }
        
        project.setLibraries(new ArrayList<File>(libraries));
        
        // After we set the library path we can check for ANE files more
        // accurately because the library path manager in the project
        // will read in all the swc files found in directories on the
        // paths.
        return validateNoANEFiles(project, externalLibraryFiles);
    }
    
    /**
     * Validate all of the ANE files are on the external library path.
     *
     * @param project the project being configured.
     * @param externalLibraryFiles the external library path.
     * @return true if successful, false if there is an error.
     */
    private boolean validateNoANEFiles(IRoyaleProject project,
                                       List<File>externalLibraryFiles)
    {
        // Get all the library files in the project.
        List<ISWC> libraries = project.getLibraries();
        for (ISWC library : libraries)
        {
            if (library.isANE())
            {
                // must be on the external library path
                if (!isOnExternalLibrayPath(library, externalLibraryFiles))
                {
                    configurationProblems.add(
                                              new ANELibraryNotAllowedProblem(
                                                                              library.getSWCFile().getAbsolutePath()));
                    return false;
                }
            }
        }
        
        return true;
    }
    
    protected void setupNamespaces(IRoyaleProject project)
    {
        final List<? extends IMXMLNamespaceMapping> configManifestMappings =
        project.getCompilerNamespacesManifestMappings(configuration);
        if (configManifestMappings != null)
        {
            project.setNamespaceMappings(configManifestMappings);
        }
    }
    
    /**
     * Setups the locale related settings.
     */
    protected void setupLocaleSettings(IRoyaleProject project)
    {
        project.setLocales(configuration.getCompilerLocales());
        project.setLocaleDependentResources(configuration.getLocaleDependentSources());
    }
    
    private void setupRoyale(RoyaleProject royaleProject)
    {
        royaleProject.setRoyale(configuration.isRoyale());
    }
    
    private void setupCodegenOptions(RoyaleProject royaleProject)
    {
        royaleProject.setEnableInlining(configuration.isInliningEnabled());
    }
    
    /**
     * Setup theme files.
     */
    protected void setupThemeFiles(RoyaleProject project)
    {
        project.setThemeFiles(toFileSpecifications(configuration.getCompilerThemeFiles(),
                                                   project.getWorkspace()));
        
    }
    
    @Override
    protected ITargetSettings instantiateTargetSettings()
    {
        return new RoyaleTargetSettings(configuration, project);
    }
    
    @Override
    protected String computeQNameForTargetFile()
    {
        List<String> computedSourcePath = new ArrayList<String>();
        applySourcePathRules(computedSourcePath);
        String targetSourceFileName = configuration.getTargetFile();
        if (targetSourceFileName == null)
            return null;
        final File targetSourceFile = FilenameNormalization.normalize(new File(configuration.getTargetFile()));
        for (final String sourcePathEntry : computedSourcePath)
        {
            final String computedQName = SourcePathManager.computeQName(new File(sourcePathEntry), targetSourceFile);
            if (computedQName != null)
                return computedQName;
            
        }
        return null;
    }
    
    /**
     * Test if the SWC is explicitly on the external library path.
     *
     * @param library
     * @param externalLibraryFiles
     * @return true if the library is on the external library path, false otherwise.
     */
    private boolean isOnExternalLibrayPath(ISWC library, List<File> externalLibraryFiles)
    {
        File aneFile = library.getSWCFile();
        
        for (File file : externalLibraryFiles)
        {
            if (file.equals(aneFile))
                return true;
        }
        
        return false;
    }
    

}
