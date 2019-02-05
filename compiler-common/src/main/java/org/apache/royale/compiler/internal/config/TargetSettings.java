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

package org.apache.royale.compiler.internal.config;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.utils.FilenameNormalization;
import com.google.common.collect.ImmutableList;

/**
 * Value object of ITargetSettings.
 * 
 * The only way to create an instance of this object is by calling
 * Configurator.getTargetSettings;
 */
public class TargetSettings implements ITargetSettings
{
    public TargetSettings(Configuration configuration, ICompilerProject project)
    {
        this.configuration = configuration;
        this.project = project;
    }

    protected File output;
    protected File defaultCSS;
    
    protected List<File> themes;
    protected Collection<File> externalLibraryPath;
    protected Collection<File> includeLibraries;
    protected List<RSLSettings> rslLibraryPath;
    protected Set<File> includeSources;
    protected Map<String, File> includeFiles;
    
    protected final Configuration configuration;
    protected final ICompilerProject project;
    
    protected Set<String> externalLinkageLibraries;
    
    /**
     * @return the accessible
     */
    @Override
    public boolean isAccessible()
    {
        return configuration.getCompilerAccessible();
    }

    /**
     * @return the debugEnabled
     */
    @Override
    public boolean isDebugEnabled()
    {
        return configuration.isDebuggingEnabled();
    }

    @Override
    public boolean isTelemetryEnabled() {
        return configuration.isEnableTelemetry();
    }

    /**
     * @return the optimized
     */
    @Override
    public boolean isOptimized()
    {
        return configuration.getCompilerOptimize();
    }

    /**
     * @return the compressed
     */
    @Override
    public boolean useCompression()
    {
        return configuration.useCompression();
    }

    /**
     * @return the verboseStacktracesEnabled
     */
    @Override
    public boolean areVerboseStacktracesEnabled()
    {
        return configuration.debug();
    }

    /**
     * @return the allowSubclassOverrides
     */
    @Override
    public boolean allowSubclassOverrides()
    {
        return configuration.getCompilerAllowSubclassOverrides();
    }

    /**
     * @return the keepAllTypeSelectors
     */
    @Override
    public boolean keepAllTypeSelectors()
    {
        return configuration.keepAllTypeSelectors();
    }

    /**
     * @return the useNetwork
     */
    @Override
    public boolean useNetwork()
    {
        return configuration.getUseNetwork();
    }

    /**
     * @return the removeUnusedRuntimeSharedLibraryPaths
     */
    @Override
    public boolean removeUnusedRuntimeSharedLibraryPaths()
    {
        return configuration.getRemoveUnusedRsls();
    }

    /**
     * @return the verifyDigests
     */
    @Override
    public boolean verifyDigests()
    {
        return configuration.getVerifyDigests();
    }

    /**
     * @return the useResourceBundleMetadata
     */
    @Override
    public boolean useResourceBundleMetadata()
    {
        return true; // TODO: not implemented in configuration
    }

    /**
     * @return the includeLookupOnlyEnabled
     */
    @Override
    public boolean isIncludeLookupOnlyEnabled()
    {
        return configuration.getIncludeLookupOnly();
    }

    /**
     * @return the defaultBackgroundColor
     */
    @Override
    public int getDefaultBackgroundColor()
    {
        return configuration.getDefaultBackgroundColor();
    }

    /**
     * @return the defaultFrameRate
     */
    @Override
    public int getDefaultFrameRate()
    {
        return configuration.getDefaultFrameRate();
    }

    /**
     * @return whether default script limits are set
     */
    @Override
    public boolean areDefaultScriptLimitsSet()
    {
        return configuration.scriptLimitsSet();
    }

    /**
     * @return the defaultScriptTimeLimit
     */
    @Override
    public int getDefaultScriptTimeLimit()
    {
        return configuration.getScriptTimeLimit();
    }

    /**
     * @return the defaultScriptRecursionLimit
     */
    @Override
    public int getDefaultScriptRecursionLimit()
    {
        return configuration.getScriptRecursionLimit();
    }

    /**
     * @return the defaultWidth
     */
    @Override
    public int getDefaultWidth()
    {
        return configuration.getDefaultWidth();
    }

    /**
     * @return the defaultHeight
     */
    @Override
    public int getDefaultHeight()
    {
        return configuration.getDefaultHeight();
    }

    /**
     * @return the output
     */
    @Override
    public File getOutput()
    {
        if (output == null)
            output = FilenameNormalization.normalize(new File(configuration.getOutput()));
        
        return output;
    }

    /**
     * @return the actionScriptMetadataNames
     */
    @Override
    public Collection<String> getASMetadataNames()
    {
        return configuration.getCompilerKeepAs3Metadata();
    }

    /**
     * @return the defaultCSS
     */
    @Override
    public File getDefaultCSS()
    {
        if (defaultCSS == null)
            defaultCSS = FilenameNormalization.normalize(new File(configuration.getCompilerDefaultsCssUrl()));
        
        return defaultCSS;
    }

    /**
     * @return the externs
     */
    @Override
    public Collection<String> getExterns()
    {
        return configuration.getExterns();
    }

    /**
     * @return the implicitImports for MXML
     */
    @Override
    public String[] getMxmlImplicitImports()
    {
        return configuration.getCompilerMxmlImplicitImports();
    }

    /**
     * @return the includes
     */
    @Override
    public Collection<String> getIncludes()
    {
        return configuration.getIncludes();
    }

    /**
     * @return the frameLabels
     */
    @Override
    public List<FrameInfo> getFrameLabels()
    {
        return configuration.getFrameList();
    }

    /**
     * @return the swfMetadata
     */
    @Override
    public String getSWFMetadata()
    {
        return configuration.getRawMetadata();
    }

    /**
     * @return the SWF version
     */
    @Override
    public int getSWFVersion()
    {
        return configuration.getSwfVersion();
    }

    /**
     * @return the preloader class name
     */
    @Override
    public String getPreloaderClassName()
    {
        return configuration.getPreloader();
    }
    
    

    @Override
    public String getRootSourceFileName()
    {
        return configuration.getTargetFile();
    }

    /**
     * @return the rootClassName
     */
    @Override
    public String getRootClassName()
    {
        return configuration.getMainDefinition();
    }

    /**
     * @return the themes
     */
    @Override
    public List<File> getThemes()
    {
        if (themes == null)
            themes = Configurator.toFileList(configuration.getCompilerThemeFiles());
        
        return themes;
    }

    /**
     * @return the externalLibraryPath
     */
    @Override
    public Collection<File> getExternalLibraryPath()
    {
        if (externalLibraryPath == null)
            externalLibraryPath = Configurator.toFileList(configuration.getCompilerExternalLibraryPath());

        return externalLibraryPath;
    }

    /**
     * @return the includeLibraries
     */
    @Override
    public Collection<File> getIncludeLibraries()
    {
        if (includeLibraries == null)
            includeLibraries = Configurator.toFileList(configuration.getCompilerIncludeLibraries());
        
        return includeLibraries;
    }

    /**
     * @return the runtimeSharedLibraries
     */
    @Override
    public List<String> getRuntimeSharedLibraries()
    {
        return configuration.getRuntimeSharedLibraries();
    }

    /**
     * @return the runtimeSharedLibraryPath
     */
    @Override
    public List<RSLSettings> getRuntimeSharedLibraryPath()
    {
        if (rslLibraryPath == null)
            rslLibraryPath = Configurator.getRSLSettingsFromConfiguration(configuration);
        
        return rslLibraryPath;
    }

    /**
     * @return the includeClasses
     */
    @Override
    public Collection<String> getIncludeClasses()
    {
        return configuration.getIncludeClasses();
    }

    /**
     * @return the includeFiles
     */
    @Override
    public Collection<File> getIncludeSources()
    {
        if (includeSources == null)
        {
            includeSources = new HashSet<File>();

            List<File> files = Configurator.toFileList(configuration.getIncludeSources());
            for (File file : files)
            {
                if (file.isFile())
                {
                    includeSources.add(file);
                    continue;
                }
                else if (file.isDirectory())
                {
                    for (File fileInFolder : FileUtils.listFiles(file, new String[] {"as", "mxml"}, true))
                        includeSources.add(fileInFolder);
                }
            }
        }

        return includeSources;
    }

    /**
     * @return the includeFiles
     */
    @Override
    public Map<String, File> getIncludeFiles()
    {
        if (includeFiles == null)
        {
            includeFiles = new TreeMap<String, File>();
            for (Map.Entry<String, String> fileEntry : configuration.getIncludeFiles().entrySet())
            {
                includeFiles.put(fileEntry.getKey(), FilenameNormalization.normalize(new File(fileEntry.getValue())));
            }
        }

        return includeFiles;
    }

    /**
     * @return the includeNamespaces
     */
    @Override
    public Collection<String> getIncludeNamespaces()
    {
        return configuration.getIncludeNamespaces();
    }

    /**
     * @return the includeResourceBundles
     */
    @Override
    public Collection<String> getIncludeResourceBundles()
    {
        return configuration.getIncludeResourceBundles();
    }

    /**
     * @return the styleSheets
     */
    @Override
    public Map<String, File> getIncludeStyleSheets()
    {
        // TODO: the configuration value needs to be fixed. It is only creating
        // a list, not a map. targetSettings.setIncludeStyleSheets(configuration.getIncludeStyleSheets());
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLinkageExternal(String path)
    {
        if (externalLinkageLibraries == null)
        {
            initExternalLinkageLibraries();
        }
        
        return externalLinkageLibraries.contains(path);
    }

    @Override
    public boolean useDirectBlit()
    {
        // no config option for direct blit use, so default to false
        return false;
    }

    @Override
    public boolean useGPU()
    {
        //no config option for GPU use, so default to false
        return false;
    }

    /**
     * Merge external library path and RSL library path
     */
    private void initExternalLinkageLibraries()
    {
        externalLinkageLibraries = new HashSet<String>(getExternalLibraryPath().size() +
                getRuntimeSharedLibraryPath().size());
        
        for (File library : getExternalLibraryPath())
        {
            externalLinkageLibraries.add(library.getAbsolutePath());
        }

        for (RSLSettings rsl : getRuntimeSharedLibraryPath())
        {
            externalLinkageLibraries.add(rsl.getLibraryFile().getAbsolutePath());
        }
    }

    @Override
    public List<String> getDefaultsCSSFiles()
    {
        return ImmutableList.copyOf(configuration.getDefaultsCSSFiles());
    }

    @Override
    public List<String> getExcludeDefaultsCSSFiles()
    {
        return ImmutableList.copyOf(configuration.getExcludeDefaultsCSSFiles());
    }

    @Override
    public File getLinkReport()
    {
        return configuration.getLinkReport();
    }
    
    @Override
    public File getSizeReport()
    {
        return configuration.getSizeReport();
    }

    @Override
    public String getRoyaleMinimumSupportedVersion()
    {
        return configuration.getCompilerMinimumSupportedVersionString();
    }

    @Override
    public boolean getMxmlChildrenAsData()
    {
        return configuration.getCompilerMxmlChildrenAsData();
    }

    @Override
    public boolean getInfoFlex()
    {
        return configuration.getCompilerInfoFlex();
    }

    @Override
    public boolean getAllowSubclassOverrides()
    {
        return configuration.getCompilerAllowSubclassOverrides();
    }

    @Override
    public boolean getRemoveDeadCode()
    {
        return configuration.getRemoveDeadCode();
    }

	@Override
	public String getSWFMetadataDate() {
		return configuration.getMetadataDate();
	}
	
	@Override
	public String getSWFMetadataDateFormat() {
		return configuration.getMetadataDateFormat();
	}

}
