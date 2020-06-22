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

package org.apache.royale.compiler.projects;

import java.io.File;
import java.util.*;

import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.common.XMLName;
import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.css.ICSSManager;
import org.apache.royale.compiler.css.ICSSRule;
import org.apache.royale.compiler.definitions.IClassDefinition;
import org.apache.royale.compiler.exceptions.LibraryCircularDependencyException;
import org.apache.royale.compiler.internal.codegen.databinding.BindingDatabase;
import org.apache.royale.compiler.internal.config.IWriteOnlyProjectSettings;
import org.apache.royale.compiler.internal.css.CSSManager;
import org.apache.royale.compiler.internal.mxml.MXMLNamespaceMapping;
import org.apache.royale.compiler.mxml.IMXMLNamespaceMapping;
import org.apache.royale.compiler.mxml.IXMLNameResolver;
import org.apache.royale.compiler.targets.ISWCTarget;
import org.apache.royale.compiler.targets.ITargetProgressMonitor;
import org.apache.royale.compiler.targets.ITargetSettings;

/**
 * Base interface for all project types that support all Royale source file types
 * ( mxml, fxg, css, etc ).
 */
public interface IRoyaleProject extends IASProject, IXMLNameResolver, IWriteOnlyProjectSettings
{
    /**
     * Create a SWC target
     * 
     * @param targetSettings The settings to use for this target
     * @param progressMonitor to collect progress information, can be <code>null</code>
     * @return The newly created ISWCTarget
     * @throws InterruptedException
     */
    ISWCTarget createSWCTarget(ITargetSettings targetSettings, ITargetProgressMonitor progressMonitor) throws InterruptedException;

    /**
     * Sets the mappings from MXML namespace URI to manifest files, as specified
     * by -namespace options.
     * 
     * @param namespaceMappings An array of {@code MXMLNamespaceMapping}
     * objects.
     */
    @Override
    void setNamespaceMappings(List<? extends IMXMLNamespaceMapping> namespaceMappings);

    /**
     * Get all the namespace mappings.
     * 
     * @return An array of namespace mappings.
     */
    IMXMLNamespaceMapping[] getNamespaceMappings();
    
    /**
     * Get the project-level CSS manager.
     * 
     * @return {@link CSSManager} of the project.
     */
    ICSSManager getCSSManager();
    
    /**
     * Returns the requested locales for this project.
     * 
     * @return a list of project's locales
     */
    Collection<String> getLocales();
    
    /**
     * Sets the locale dependent path map for the project. For each entry,
     * the key is the path of a locale dependent resource (source path or library path entry) 
     * and the value is the locale it depends on.
     * 
     * @param localeDependentResources map that contains project's locale
     * dependent resources
     */
    void setLocaleDependentResources(Map<String, String> localeDependentResources);
    
    /**
     * Returns the locale of the resource with a given path if the resource
     * is locale dependent. This currently returns the locale for only 
     * source path and library path entries, which are the only resources passed to 
     * compiler and can contain {locale} token.
     * 
     * @param path path of a file/directory for which to determine the locale
     * @return locale of the resource with a given path if the resource
     * is locale dependent or <code>null</code> if the resource is not locale
     * dependent.
     */
    String getResourceLocale(String path);
 
    //
    // New configurator interface 
    //
    /**
     * Returns the file encoding use to compile ActionScript files.
     * 
     * @return the file encoding use to compile ActionScript files.
     */
    String getActionScriptFileEncoding();

    /**
     * Returns a map of extension element files to extension paths.
     * 
     * @return a map of extension element files to extension paths.
     */
    Map<File, List<String>> getExtensionLibraries();

    /**
     * The absolute path of the services-config.xml file. This file used
     * to configure a Royale client to talk to a BlazeDS server.
     * 
     * @return the absolute path of the services-config.xml file. Null
     * if the file has not been configured.
     */
    String getServicesXMLPath();

    /**
     * The list of RSLs configured for this application.
     * 
     * @return a list of {@link RSLSettings} where each entry in the list is an
     * RSL to load at runtime.
     */
    List<RSLSettings> getRuntimeSharedLibraryPath();

    /**
     * Set the RSL library path with an ordered list of RSLs. The runtime will 
     * load the RSLs in the order specified.
     * 
     * @param rsls Each {@link RSLSettings} in the list describes an RSL and 
     * its failovers. If null, the list of RSLs is reset.
     */
    void setRuntimeSharedLibraryPath(List<RSLSettings> rsls);

    /**
     * Get the set of libraries a given library depends on.
     * 
     * @param targetLibrary The library to find dependencies for. May not be 
     * null.
     * @param dependencyTypeSet The types of dependencies to consider when 
     * determining the library's dependencies. If this parameter is null or an empty set, then all
     * dependencies will be considered. 
     * @return A set of Strings; where each String is the location of a library in the file system. 
     */
    Set<String> computeLibraryDependencies(File targetLibrary, DependencyTypeSet dependencyTypeSet) 
        throws LibraryCircularDependencyException;

    /**
     * Get the dependency order of libraries on the internal library path and 
     * external library path of this project.
     * 
     * @param dependencySet The types of dependencies to consider when 
     * determining the dependency order. If this parameter is null or an empty set, then all
     * dependencies will be considered. 
     * @return An ordered list of library dependencies. Each String in the 
     * list is the location of a library in the file system. The first library in the list has no
     * dependencies. Each library in the list has at least the same dependencies as its 
     * predecessor and may be dependent on its predecessor as well. 
     */
    List<String> computeLibraryDependencyOrder(DependencyTypeSet dependencySet) 
        throws LibraryCircularDependencyException;

    /**
     * Get the names of all the themes used in this project.
     * 
     * @return A list of theme names.
     */
    List<String> getThemeNames();
    
    /**
     * Option of enable or prevent various Royale compiler behaviors.
     * This is currently used to enable/disable the generation of a root class for library swfs 
     * and generation of code for application swfs.
     */
    boolean isRoyale();
    
    /**
     * Uses the manifest information to find all the MXML tags that map to a
     * specified fully-qualified ActionScript classname, such as as
     * <code>"spark.controls.Button"</code>
     * 
     * @param className Fully-qualified ActionScript classname, such as as
     * <code>"spark.controls.Button"</code>
     * @return A collection of {@link XMLName}'s representing a MXML tags, such
     * as a <code>"Button"</code> tag in the namespace
     * <code>"library://ns.apache.org/royale/basic"</code>.
     */
    Collection<XMLName> getTagNamesForClass(String className);
    
    /**
     * List of external libraries so it can be overridden
     */
    List<String> getCompilerExternalLibraryPath(Configuration config);

    /**
     * List of libraries so it can be overridden
     */
    List<String> getCompilerLibraryPath(Configuration config);

    /**
     * List of external libraries so it can be overridden
     */
    List<MXMLNamespaceMapping> getCompilerNamespacesManifestMappings(Configuration config);

    /**
     * True if Rule will work on the destination platform
     */
    boolean isPlatformRule(ICSSRule rule);

    /**
     * List of defined variables so it can be overridden
     */
    Map<String, String> getCompilerDefine(Configuration config);
    
    /**
     * The File for the link report
     */
    File getLinkReport(Configuration config);


    /**
     * classifier for the Target type
     * true for targets with static typing at runtime, false for dynamic typing at runtime
     */
    boolean isStaticTypedTarget();


    /**
     * Support for access to BindingData from the class definition as key.
     * @return
     */
    HashMap<IClassDefinition, BindingDatabase> getBindingMap();

}
