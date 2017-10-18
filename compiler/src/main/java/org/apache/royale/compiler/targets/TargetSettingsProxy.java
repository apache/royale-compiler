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
 * When implementing a ITargetSettings, this utility abstract class can be
 * extended, rather than having to implement all ITargetSettings methods.
 */
public abstract class TargetSettingsProxy implements ITargetSettings
{
    protected TargetSettingsProxy(ITargetSettings baseTargetSettings)
    {
        super();
        this.baseTargetSettings = baseTargetSettings;
    }

    private ITargetSettings baseTargetSettings;

    @Override
    public boolean isAccessible()
    {
        return baseTargetSettings.isAccessible();
    }

    @Override
    public boolean isDebugEnabled()
    {
        return baseTargetSettings.isDebugEnabled();
    }

    @Override
    public boolean isOptimized()
    {
        return baseTargetSettings.isOptimized();
    }

    @Override
    public boolean useCompression()
    {
        return baseTargetSettings.useCompression();
    }

    @Override
    public boolean areVerboseStacktracesEnabled()
    {
        return baseTargetSettings.areVerboseStacktracesEnabled();
    }

    @Override
    public Collection<String> getASMetadataNames()
    {
        return baseTargetSettings.getASMetadataNames();
    }

    @Override
    public File getDefaultCSS()
    {
        return baseTargetSettings.getDefaultCSS();
    }

    @Override
    public int getDefaultBackgroundColor()
    {
        return baseTargetSettings.getDefaultBackgroundColor();
    }

    @Override
    public int getDefaultFrameRate()
    {
        return baseTargetSettings.getDefaultFrameRate();
    }

    @Override
    public boolean areDefaultScriptLimitsSet()
    {
        return baseTargetSettings.areDefaultScriptLimitsSet();
    }

    @Override
    public int getDefaultScriptTimeLimit()
    {
        return baseTargetSettings.getDefaultScriptTimeLimit();
    }

    @Override
    public int getDefaultScriptRecursionLimit()
    {
        return baseTargetSettings.getDefaultScriptRecursionLimit();
    }

    @Override
    public int getDefaultWidth()
    {
        return baseTargetSettings.getDefaultWidth();
    }

    @Override
    public int getDefaultHeight()
    {
        return baseTargetSettings.getDefaultHeight();
    }

    @Override
    public Collection<String> getExterns()
    {
        return baseTargetSettings.getExterns();
    }

    @Override
    public Collection<String> getIncludes()
    {
        return baseTargetSettings.getIncludes();
    }

    @Override
    public List<FrameInfo> getFrameLabels()
    {
        return baseTargetSettings.getFrameLabels();
    }

    @Override
    public String getSWFMetadata()
    {
        return baseTargetSettings.getSWFMetadata();
    }

    @Override
    public int getSWFVersion()
    {
        return baseTargetSettings.getSWFVersion();
    }

    @Override
    public String getPreloaderClassName()
    {
        return baseTargetSettings.getPreloaderClassName();
    }

    @Override
    public String getRootSourceFileName()
    {
        return baseTargetSettings.getRootSourceFileName();
    }
    
    @Override
    public String getRootClassName()
    {
        return baseTargetSettings.getRootClassName();
    }

    @Override
    public boolean keepAllTypeSelectors()
    {
        return baseTargetSettings.keepAllTypeSelectors();
    }

    @Override
    public boolean useNetwork()
    {
        return baseTargetSettings.useNetwork();
    }

    @Override
    public List<File> getThemes()
    {
        return baseTargetSettings.getThemes();
    }

    @Override
    public boolean removeUnusedRuntimeSharedLibraryPaths()
    {
        return baseTargetSettings.removeUnusedRuntimeSharedLibraryPaths();
    }

    @Override
    public boolean verifyDigests()
    {
        return baseTargetSettings.verifyDigests();
    }

    @Override
    public Collection<File> getExternalLibraryPath()
    {
        return baseTargetSettings.getExternalLibraryPath();
    }

    @Override
    public Collection<File> getIncludeLibraries()
    {
        return baseTargetSettings.getIncludeLibraries();
    }

    @Override
    public List<String> getRuntimeSharedLibraries()
    {
        return baseTargetSettings.getRuntimeSharedLibraries();
    }

    @Override
    public List<RSLSettings> getRuntimeSharedLibraryPath()
    {
        return baseTargetSettings.getRuntimeSharedLibraryPath();
    }

    @Override
    public boolean useResourceBundleMetadata()
    {
        return baseTargetSettings.useResourceBundleMetadata();
    }

    @Override
    public File getOutput()
    {
        return baseTargetSettings.getOutput();
    }

    @Override
    public Collection<String> getIncludeClasses()
    {
        return baseTargetSettings.getIncludeClasses();
    }

    @Override
    public Collection<File> getIncludeSources()
    {
        return baseTargetSettings.getIncludeSources();
    }

    @Override
    public Map<String, File> getIncludeFiles()
    {
        return baseTargetSettings.getIncludeFiles();
    }

    @Override
    public Collection<String> getIncludeNamespaces()
    {
        return baseTargetSettings.getIncludeNamespaces();
    }

    @Override
    public Collection<String> getIncludeResourceBundles()
    {
        return baseTargetSettings.getIncludeResourceBundles();
    }

    @Override
    public Map<String, File> getIncludeStyleSheets()
    {
        return baseTargetSettings.getIncludeStyleSheets();
    }

    @Override
    public boolean isIncludeLookupOnlyEnabled()
    {
        return baseTargetSettings.isIncludeLookupOnlyEnabled();
    }

    @Override
    public boolean isLinkageExternal(String path)
    {
        return baseTargetSettings.isLinkageExternal(path);
    }

    @Override
    public boolean useDirectBlit()
    {
        return baseTargetSettings.useDirectBlit();
    }

    @Override
    public boolean useGPU()
    {
        return baseTargetSettings.useGPU();
    }

    @Override
    public List<String> getDefaultsCSSFiles()
    {
        return baseTargetSettings.getDefaultsCSSFiles();
    }

    @Override
    public File getLinkReport()
    {
        return baseTargetSettings.getLinkReport();
    }
    
    @Override
    public File getSizeReport()
    {
        return baseTargetSettings.getSizeReport();
    }

    @Override
    public String getRoyaleMinimumSupportedVersion()
    {
        return baseTargetSettings.getRoyaleMinimumSupportedVersion();
    }

    @Override
    public boolean getRemoveDeadCode()
    {
        return baseTargetSettings.getRemoveDeadCode();
    }
}
