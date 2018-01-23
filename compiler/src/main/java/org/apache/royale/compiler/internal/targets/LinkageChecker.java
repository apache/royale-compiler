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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.royale.compiler.config.RSLSettings;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.targets.ITargetSettings;
import org.apache.royale.compiler.units.ICompilationUnit;
import org.apache.royale.compiler.units.ICompilationUnit.UnitType;
import org.apache.royale.compiler.workspaces.IWorkspace;

/**
 * Class to handle all of the checks for external linkage.
 */
public class LinkageChecker
{

    /**
     * Constructor.
     * 
     * @param project The project.
     * @param targetSettings The target settings.
     */
    public LinkageChecker(ICompilerProject project, 
            ITargetSettings targetSettings)
    {
        this.project = project;
        this.targetSettings = targetSettings;
    }
    
    private ICompilerProject project;
    private ITargetSettings targetSettings;
    private Set<String> externs;
    
    /**
     * Test if a compilation is should be included in the target or not.
     *  
     * @param cu The compilation unit to test.
     * @return true if the compilation should be excluded from the target,
     * false otherwise.
     */
    public boolean isExternal(ICompilationUnit cu) throws InterruptedException
    {
        if (externs == null)
            initExterns();

        // All names in a cu should have the same linkage so just
        // check the first name.
        List<String> qnames = cu.getQualifiedNames();
        if (!qnames.isEmpty() &&
            externs.contains(qnames.get(0)))
        {
            return true;
        }
        
        return false;
    }

    /**
     * Initialize the externs set. Combine the symbols explicitly externed by
     * the user together with the symbols in each of the libraries on the
     * external library path. We use the symbols from external libraries instead
     * of just checking if the compilation unit comes from an external library
     * to fix the case where we would include a symbol like "mx_internal"
     * because it was in both an external library and a "normal" library. With
     * "mx_internal" in the externs table the compilation unit will be excluded
     * no matter which library it is found in. This in being done to match the
     * behavior of the old compiler.
     * 
     * @throws InterruptedException if implementation throws one
     */
    public void initExterns() throws InterruptedException
    {
        if (externs != null) return;
        
        // using a temporary local variable instead of adding to the externs
        // member variable directly. isExternal() checks if the member variable
        // is null. since isExternal() may be called by multiple threads, we
        // don't want to set the member variable before all externs are added,
        // or the isExternal() method could return the wrong result 
        HashSet<String> foundExterns = new HashSet<String>(4096);
        
        // first add the user defined externals
        foundExterns.addAll(targetSettings.getExterns());
        
        // next add all of the symbols from external libraries
        IWorkspace w = project.getWorkspace();
        Collection<File> extLibs = targetSettings.getExternalLibraryPath();
        List<RSLSettings> rsls = targetSettings.getRuntimeSharedLibraryPath();
        List<File> libraries = new ArrayList<File>(
                ((extLibs == null) ? 0 : extLibs.size()) +
                ((rsls == null) ? 0 : rsls.size()));
        if (extLibs != null)
        	libraries.addAll(targetSettings.getExternalLibraryPath());
        if (rsls != null)
        {
	        for (RSLSettings settings : targetSettings.getRuntimeSharedLibraryPath())
	        {
	            libraries.add(settings.getLibraryFile());
	        }
        }        
        
        for (File library : libraries)
        {
            Collection<ICompilationUnit> units = null;
            units = w.getCompilationUnits(library.getAbsolutePath(), project);
            
            for (ICompilationUnit unit : units)
            {
                // Don't allow embed units to be extern'd. Rely on the class
                // containing the embed to either pull in or extern the embed.
                // Royale is different from the old compiler in that embed
                // compilation units generated from files in a SWC have
                // their qname associated with the SWC. This led to 
                // externing the embed compilation unit when the SWC was on
                // the external library path.
                ICompilationUnit.UnitType type = unit.getCompilationUnitType();
                if (type != UnitType.EMBED_UNIT)
                    foundExterns.addAll(unit.getQualifiedNames());
            }
        }
        
        // -includes-classes (compc only) is allow to override externs for
        // monkey patching.
        foundExterns.removeAll(targetSettings.getIncludeClasses());
        externs = foundExterns;
    }
    
}
