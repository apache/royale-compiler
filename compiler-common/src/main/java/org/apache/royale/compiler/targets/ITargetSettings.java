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

package org.apache.royale.compiler.targets;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.internal.config.FrameInfo;

/**
 * The settings used to compile a target.
 */
public interface ITargetSettings
{
    /**
     * Returns true if the target file is accessible.
     * 
     * @return true if the target file is accessible, false otherwise.
     */
    boolean isAccessible();
    
    /**
     * Returns true if the target file has debugging instructions
     * included.
     * @return true if the target file has debugging instructions
     * included, false otherwise.
     */
    boolean isDebugEnabled();

    /**
     * Returns true if the target file has advanced telemetry
     * enabled.
     * @return true if the target file has advanced telemetry
     * enabled, false otherwise.
     */
    boolean isTelemetryEnabled();

    /**
     * Returns true if the target should be optimized.
     * 
     * @return true if the target should be optimized, false otherwise.
     */
    boolean isOptimized();
    
    /**
     * Returns true if the target should be output using compression.
     * 
     * @return true if the target should be output using compression, false
     * otherwise.
     */
    boolean useCompression();
    
    /**
     * Returns true if the target is capable of verbose stack traces.
     * 
     * @return true if the target is capable of verbose stack traces, 
     * false otherwise.
     */
    boolean areVerboseStacktracesEnabled();
    
    /**
     * The names of ActionScript metadata that will be preserved in the target.
     * If the target is a SWC, the metadata names to preserve will be 
     * recorded in the SWC's catalog. When the SWC is later linked into
     * a target the metadata names will be appended to the list of metadata
     * names preserved in the target.
     * 
     * @return the list of names of ActionScript metadata that will be 
     * preserved in the target. If null, all metadata is preserved. 
     */
    Collection<String> getASMetadataNames();
    
    /**
     * Returns the location of the default CSS file.
     * 
     * @return an instance of <code>java.io.File</code>.
     */
    File getDefaultCSS();
    
    /**
     * @return Normalized paths in {@code defaults-css-files} configuration option.
     */
    List<String> getDefaultsCSSFiles();
    
    /**
     * @return Normalized paths in {@code exclude-defaults-css-files} configuration option.
     */
    List<String> getExcludeDefaultsCSSFiles();
    
    /**
     * Returns the default background color.
     * 
     * @return the default background color.
     */
    int getDefaultBackgroundColor();
    
    
    /**
     * Returns the default frame rate.
     * 
     * @return the default frame rate in frames per second.
     */
    int getDefaultFrameRate();

    /**
     * Returns if the default script limits have been set.
     * 
     * @return true if limits set, false otherwise.
     */
    boolean areDefaultScriptLimitsSet();

    /**
     * Returns the default script time limit.
     * 
     * @return the default script time limit in seconds.
     */
    int getDefaultScriptTimeLimit();

    /**
     * Returns the default script recursion limit.
     * 
     * @return the default script recursion limit.
     */
    int getDefaultScriptRecursionLimit();
    
    /**
     * Returns the default width.
     * 
     * @return the default width in pixels.
     */
    int getDefaultWidth();
    
    
    /**
     * Returns the default height.
     * 
     * @return the default height in pixels.
     */
    int getDefaultHeight();
    
    /**
     * Returns a list of fully qualified class names that should not be linked 
     * into the target.
     * 
     * @return a list of fully qualified class names that should not be linked 
     * into the target.
     */
    Collection<String> getExterns();
    
    /**
     * Returns a list of fully qualified class names that should be linked 
     * into the target regardless of whether they are required by the target or
     * not.
     * 
     * @return a list of fully qualified class names that should be linked 
     * into the target.
     */
    Collection<String> getIncludes();
    
    /**
     * A {@link List} of {@link FrameInfo} objects that describe extra frames of
     * a SWF. Each frame label has a list of class names that will be linked
     * onto the frame.
     * 
     * @return A collection of {@link FrameInfo} objects in SWF order.
     */
    List<FrameInfo> getFrameLabels();
    
    /**
     * Get the Metadata text. This is a Metadata property.
     * <p>
     * The Metadata tag is an optional tag to describe the SWF file to an
     * external process. The tag embeds XML metadata in the SWF file so that,
     * for example, a search engine can locate this tag, access a title for the
     * SWF file, and display that title in search results.
     * 
     * @return metadata XML text; null if Metadata tag doesn't exit.
     */
    String getSWFMetadata();

    /**
     * Return the SWF version
     * 
     * @return SWF version
     */
    int getSWFVersion();

    /**
     * Returns the preloader class name
     * 
     * @return preloader class name
     */
    String getPreloaderClassName();

    /**
     * Returns the absolute file name of the source file that defines the root
     * class. If {@link ITarget}s using this {@link ITargetSettings} do not
     * build SWFs ( they could build SWCs instead ), then this method will
     * return null.
     * 
     * @return absolute file name of the source file that defines the root
     * class, or null if this {@link ITargetSettings} is not for building SWFs.
     */
    String getRootSourceFileName();

    /**
     * Returns the root class name for the application SWF. If {@link ITarget}s
     * using this {@link ITargetSettings} do not build SWFs ( they could build
     * SWCs instead ), then this method will return null.
     * 
     * @return rootClassName of the application SWF, or null if this
     * {@link ITargetSettings} is not for building SWFs.
     */
    String getRootClassName();

    /**
     * Returns true if the compiler has disabled the pruning of unused type 
     * selectors.
     *
     * @return true if the compiler has disabled the pruning of unused type 
     * selectors, false otherwise.
     */
    boolean keepAllTypeSelectors();

    /**
     * Returns whether the application SWF is flagged for access to network 
     * resources.
     * 
     * @return true if the application SWF is flagged for access to network 
     * resources, false otherwise.
     */
    boolean useNetwork();
    
    /**
     * Returns whether the application is comprised of code that used
     * subclass overrides.
     * 
     * @return true if the application is comprised of code that used
     * subclass overrides, false otherwise.
     */
    boolean allowSubclassOverrides();
    
    /**
     * Returns a list of CSS and SWC files to apply as a theme.
     * 
     * @return a list of CSS and SWC files to apply as a theme.
     */
    List<File> getThemes();
    
    /**
     * Returns true if RSLs that have no classes used by the application are 
     * not loaded at runtime.
     * 
     * @return true if RSLs that have no classes used by the application are 
     * not loaded at runtime, false otherwise.
     */
    boolean removeUnusedRuntimeSharedLibraryPaths();
    
    /**
     * Returns the default setting for all RSLs as to whether an RSL's digest 
     * should be verified after the RSL is loaded. This setting may be overridden
     * by setting the verify digest flag on an individual RSL. 
     * 
     * This is equivalent to using the <code>verify-digests</code>
     * option in the mxmlc compiler.
     * 
     * @see org.apache.royale.compiler.config.RSLSettings
     */
    boolean verifyDigests();
    
    /**
     * Returns a collection of libraries whose classes are not be linked 
     * into the target.
     * 
     * @return a collection of libraries whose classes are not be linked
     * into the target.
     */
    Collection<File> getExternalLibraryPath();

    /**
     * Returns a collection of libraries whose classes are linked 
     * into the target. All the of classes in each library are linked
     * into the application regardless of whether the classes is referenced
     * by the target.
     * 
     * @return a collection of libraries whose classes will be linked
     * into the target.
     */
    Collection<File> getIncludeLibraries();
    
    /**
     * A list of URLs pointing to RSLs to load. The RSLs will be loaded in the
     * order that they appear in the list.
     * 
     * This is equivalent to using the <code>runtime-shared-libraries</code>
     * option in the mxmlc compiler.
     * 
     * These RSLs are available to support legacy applications. For the latest
     * RSL support see getRuntimeSharedLibraryPath.
     * 
     * @return A list of URLs pointing to RSLs to load.
     */
    List<String> getRuntimeSharedLibraries();
    
    /**
     * A list of RSLs to load, complete with all the settings on how to load
     * the RSLs. The RSLs will be loaded in the order that they appear in the
     * list. This is the complete list of RSLs that could possibly be loaded.
     * This list could be reduced if unused RSLs are being removed.
     * 
     * This is equivalent to using the <code>runtime-shared-library-path</code>
     * option in the mxmlc compiler.

     * @return A list of RSLs to load.
     */
    List<RSLSettings> getRuntimeSharedLibraryPath();

    /**
     * Returns true if resource bundle metadata should be processed.
     * 
     * @return true if resource bundle metadata should be processed, false otherwise.
     */
    boolean useResourceBundleMetadata();

    /**
     * Returns the file that specifies where the target should be created.
     * 
     * @return the file that specifies where the target should be created.
     */
    File getOutput();
    
    /**
     * Returns the file that specifies where the link report should be written.
     * 
     * @return the file that specifies where the link report should be written
     * or null if the link report was not requested.
     */
    File getLinkReport();
    
    /**
     * Returns the file that specifies where the size report should be written.
     * 
     * @return the file that specifies where the size report should be written
     * or null if the size report was not requested.
     */
    File getSizeReport();
    
    //
    // Library specific settings
    //

    /**
     * Returns a collection of fully-qualified class names that are included in
     * the target library. 
     * 
     * @return a collection of fully-qualified class names that are included in
     * the target library.
     */
    Collection<String> getIncludeClasses();

    /**
     * Returns a collection of sources that are included in the target library.
     * 
     * @return a collection of sources that are included in the target library.
     */
    Collection<File> getIncludeSources();

    /**
     * Returns a collection of files that are included in the target library.
     * Each entry in the map is a file to be included. The key is the name
     * of the file in the target library. The value is the file to be included
     * in the library. 
     * 
     * @return a collection of files that are included in the target library.
     */
    Map<String, File> getIncludeFiles();

    /**
     * Returns a collection of namespaces that are included in the target 
     * library. 
     * 
     * @return a collection of namespaces that are included in the target
     * library.
     */
    Collection<String> getIncludeNamespaces();

    /**
     * Returns a collection of resource bundle names that are included
     * in the target library. 
     * 
     * @return a collection of resource bundle names that are included
     * in the target library.
     */
    Collection<String> getIncludeResourceBundles();

    /**
     * Returns a map of style sheet names and files that are included
     * in the target library. 
     * 
     * The map's key/value pairs are as follows:
     *     key:   the name of the file in the target library
     *     value: the included style sheet.

     * @return a collection of style sheet names and files that are included
     * in the target library.
     */
    Map<String, File> getIncludeStyleSheets();

    /**
     * Returns true if only manifest entries with lookupOnly=true are included 
     * in the SWC catalog, false otherwise.
     *  
     * @return true if only manifest entries with lookupOnly=true are included
     * in the SWC catalog, false otherwise.
     */
    boolean isIncludeLookupOnlyEnabled();
    
    /**
     * Determine is a library has external linkage. A library with external 
     * linkage will have none of its classes appear in the target. Two exceptions
     * to this rule are classes on the -include-classes option and classes that
     * are linked into frames preceding the last frame of SWF. 
     * 
     * @param path the absolute path of a library.
     * 
     * @return true if the library has external linkage.
     */
    boolean isLinkageExternal(String path);

    /**
     * Returns whether the application SWF is flagged for direct blit use.
     * 
     * @return true if the application SWF should use direct blit, false otherwise.
     */
    boolean useDirectBlit();

    /**
     * Returns whether the application SWF is flagged for GPU use.
     * 
     * @return true if the application SWF should use the GPU, false otherwise.
     */
    boolean useGPU();

    /**
     * Used when creating a library. Specifies the minimum version of Royale that
     * is written into the catalog of the created library. Royale uses the minimum
     * version when compiling an application to filter out libraries that have a
     * minimum version that is greater than the specified compatibility
     * version.
     * 
     * @return the minimum supported version of Royale.
     */
    String getRoyaleMinimumSupportedVersion();

    /**
     * @return true if MXML child nodes are encoded into a data stream instead
     * of a bunch of functions.
     */
    boolean getMxmlChildrenAsData();
    
    /**
     * @return true if the info() structure should contain fields needed by FlexSDK only (and not Royale).
     */
    boolean getInfoFlex();
    
    /**
     * @return true if the return type of an override can be a subclass instead
     * of an exact match as the base class' return type
     */
    boolean getAllowSubclassOverrides();
    
    /**
     * @return true if the dead code filtering optimization step is enabled.
     */
    boolean getRemoveDeadCode();

    /**
     * Gets the implicit imports for MXML.
     * 
     * @return An array of strings specifying the import targets.
     */
    String[] getMxmlImplicitImports();
    
    /**
     * Gets Date string used in RDF metadata.
     * 
     * @return null or RDF date.
     */
    String getSWFMetadataDate();
    
    /**
     * Gets DateFormat string used in RDF metadata date.
     * 
     * @return null or Java SimpleDateFormat pattern.
     */
    String getSWFMetadataDateFormat();
}

