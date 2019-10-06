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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


import org.apache.royale.compiler.config.Configuration;
import org.apache.royale.compiler.config.Configurator;
import org.apache.royale.compiler.internal.projects.LibraryPathManager;
import org.apache.royale.compiler.projects.ICompilerProject;
import org.apache.royale.compiler.projects.IRoyaleProject;

/**
 * Value object of ITargetSettings.
 * 
 * The only way to create an instance of this object is by calling
 * Configurator.getTargetSettings;
 */
public class RoyaleTargetSettings extends TargetSettings
{
    public RoyaleTargetSettings(Configuration configuration, ICompilerProject project)
    {
        super(configuration, project);
    }
    
    /**
     * @return the externalLibraryPath
     */
    @Override
    public Collection<File> getExternalLibraryPath()
    {
        if (externalLibraryPath == null)
        {
            List<File> files = Configurator.toFileList(project != null ? ((IRoyaleProject)project).getCompilerExternalLibraryPath(configuration) :
            												configuration.getCompilerExternalLibraryPath());
            Set<File> expandedFiles = LibraryPathManager.discoverSWCFilePathsAsFiles(files.toArray(new File[files.size()]));

            externalLibraryPath = new ArrayList<File>(expandedFiles.size());
            for (File swcFile : expandedFiles)
                externalLibraryPath.add(swcFile);
        }

        return externalLibraryPath;
    }
    
    @Override
    public File getLinkReport()
    {
    	if (project != null)
    		return ((IRoyaleProject)project).getLinkReport(configuration);
    	
        return super.getLinkReport();
    }

}
