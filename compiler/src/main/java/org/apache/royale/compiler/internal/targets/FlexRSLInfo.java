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

package org.apache.royale.compiler.internal.targets;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.royale.compiler.common.DependencyType;
import org.apache.royale.compiler.common.DependencyTypeSet;
import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.exceptions.LibraryCircularDependencyException;
import org.apache.royale.compiler.internal.projects.RoyaleProject;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.swc.ISWC;

/**
 * Class that contains information about RSLs used by a royale application SWF.
 * <p>
 * The information about RSLs is computed from {@link ITargetSettings} and
 * {@code RoyaleApplicationFrame1Info}.
 */
final class FlexRSLInfo
{
    FlexRSLInfo(RoyaleApplicationFrame1Info frame1Info, RoyaleProject royaleProject, ITargetSettings targetSettings)
    {
        this.frame1Info = frame1Info;
        this.royaleProject = royaleProject;
        this.targetSettings = targetSettings;
        requiredRSLs = new ArrayList<RSLSettings>();
        placeholderRSLs = new ArrayList<RSLSettings>();
        
        // The required RSLs are put in "requiredRSLs", the others are put in "placeholderRSLs".
        final List<RSLSettings> cdRSLs = targetSettings.getRuntimeSharedLibraryPath();
        
        final Set<String> unusedRSLs = getUnusedRSLs(cdRSLs);
        
        for (RSLSettings rslSettings : cdRSLs)
        {
            if (unusedRSLs.contains(rslSettings.getLibraryFile().getAbsolutePath()))
                placeholderRSLs.add(rslSettings);
            else
                requiredRSLs.add(rslSettings);
        }
    }
    
    private final RoyaleApplicationFrame1Info frame1Info;
    private final ITargetSettings targetSettings;
    private final RoyaleProject royaleProject;
    
    /**
     * {@link RSLSettings} for RSLs that must be loaded for the royale application
     * to load properly.
     */
    final ArrayList<RSLSettings> requiredRSLs;
    
    /**
     * {@link RSLSettings} for RSLs were specified by
     * {@link ITargetSettings#getRuntimeSharedLibraryPath()}, but that were not
     * required by the royale application and were not marked as force load by
     * {@link RSLSettings#isForceLoad()}.
     */
    final ArrayList<RSLSettings> placeholderRSLs;
    
    private Set<String> getUnusedRSLs(List<RSLSettings> cdRSLs)
    {
        if (!targetSettings.removeUnusedRuntimeSharedLibraryPaths())
            return Collections.emptySet();
        
        List<String> unusedRSLs = new LinkedList<String>(); // running list of unused rsls
        Set<String>loadedDownstreamRSLs = new HashSet<String>();  // loaded rsls downstream from first unused rsl
        boolean addDownstreamRSL = false;
        
        // Get unused RSLs and verify that there are no downstream RSLs that have
        // and inheritance dependency on them.
        for (RSLSettings info : cdRSLs)
        {
            String swcPath = info.getLibraryFile().getAbsolutePath();
            
            // skip if the associated swc is filtered from our context.
            if (isSWCFiltered(swcPath))
                continue;

            // skip loading the RSL if it does not contribute any classes to 
            // the application and it is not forced.
            if (!frame1Info.contributingSWCs.contains(swcPath) &&
                !info.isForceLoad())
            {
                unusedRSLs.add(swcPath);
                addDownstreamRSL = true;
            }
            else if (addDownstreamRSL)
            {
                loadedDownstreamRSLs.add(swcPath);
            }
        }

        if (unusedRSLs.size() == 0)
            return Collections.emptySet();
        
        Set<String> requiredInheritanceRSLs = filterBasedOnInheritanceDependencies(unusedRSLs, loadedDownstreamRSLs); 
        unusedRSLs.removeAll(requiredInheritanceRSLs);
        if (unusedRSLs.size() == 0)
            return Collections.emptySet();
        
        return new HashSet<String>(unusedRSLs);
    }
    
    /**
     * Create a set of swcs by filtering a list of swcs, choosing the swcs that
     * contain any inheritance dependencies from a set of filter swcs.
     * 
     * @param swcPaths A list of swc paths to filter against the inheritance dependencies 
     * of the swcPathFilters.
     * @param swcPathsFilter set of swc paths used as the inheritance dependendcy filter.
     * 
     * @return subset of the swcPaths that contain inheritance dependencies
     * found in the swcPathsFilter.
     */
    private Set<String> filterBasedOnInheritanceDependencies(List<String>swcPaths, 
                                                    Set<String>swcPathsFilter) 
    {
        if (swcPathsFilter.isEmpty())
            return Collections.emptySet();
        
        // Loop over the filter swcs to see if they have any inheritance 
        // dependencies on input swcs.
        Set<String> inheritanceDependencies = new HashSet<String>(swcPathsFilter.size());
        DependencyTypeSet inheritanceDependency = DependencyTypeSet.of(DependencyType.INHERITANCE);

        // get the inheritance dependencies of all filter swcs
        for (String swcPath : swcPathsFilter)
        {
            Set<String> swcDependencies;
            try
            {
                swcDependencies = royaleProject.computeLibraryDependencies(new File(swcPath), inheritanceDependency);
            }
            catch (LibraryCircularDependencyException e)
            {
                // Should never get a circular dependency when asking for 
                // inheritance dependencies. RSLs could never be loaded in the
                // correct order.
                // Skip this swc and move on to the next.
                assert false : e.getMessage();
                continue;
            }
            
            inheritanceDependencies.addAll(swcDependencies);
        }

        if (inheritanceDependencies.isEmpty())
            return Collections.emptySet();

        // check to see if any of the inheritance dependencies live in the
        // input swcs.
        // move thru the list backwards to we can handle the case where a
        // newly found swc can be used to test an upstream swc.
        Set<String> results = new HashSet<String>();
        for (ListIterator<String> iter = swcPaths.listIterator(swcPaths.size()); iter.hasPrevious();)
        {
            String targetSWC = iter.previous();
            if (inheritanceDependencies.contains(targetSWC))
            {
                results.add(targetSWC);

                // add the found swc to the list of inheritance dependencies.
                if (iter.hasPrevious())
                {
                    try
                    {
                        inheritanceDependencies.addAll(
                                royaleProject.computeLibraryDependencies(new File(targetSWC), 
                                        inheritanceDependency));
                    }
                    catch (LibraryCircularDependencyException e)
                    {
                        // Should never get a circular dependency when asking for 
                        // inheritance dependencies. RSLs could never be loaded in the
                        // correct order.
                        // Skip this swc and move on to the next.
                        assert false : e.getMessage();
                    }
                }
            }
        }

        return results;
    }
    
    /** 
     * Check if this SWC should be ignored because its min supported version is
     * greater than the current compatibility version.
     * 
     * @param swcPath
     * @return true if the SWC should be filtered, false otherwise.
     */
    private boolean isSWCFiltered(String swcPath)
    {
        // if compatibility-version is not set then we won't be doing any filtering.
        if (royaleProject.getCompatibilityVersion() == null)
            return false;
        
        int compatibilityVersion = royaleProject.getCompatibilityVersion();
        ISWC swc = royaleProject.getWorkspace().getSWCManager().get(new File(swcPath));
        if (compatibilityVersion < swc.getVersion().getRoyaleMinSupportedVersionInt())
        {
            return true;
        }
        
        return false;
    }
}
